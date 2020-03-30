package org.onap.ccsdk.features.lib.doorman.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.onap.ccsdk.features.lib.doorman.MessageClassifier;
import org.onap.ccsdk.features.lib.doorman.MessageInterceptor;
import org.onap.ccsdk.features.lib.doorman.MessageProcessor;
import org.onap.ccsdk.features.lib.doorman.MessageQueueHandler;
import org.onap.ccsdk.features.lib.doorman.dao.MessageDao;
import org.onap.ccsdk.features.lib.doorman.data.ActionStatus;
import org.onap.ccsdk.features.lib.doorman.data.Event;
import org.onap.ccsdk.features.lib.doorman.data.Message;
import org.onap.ccsdk.features.lib.doorman.data.MessageAction;
import org.onap.ccsdk.features.lib.doorman.data.MessageActionValue;
import org.onap.ccsdk.features.lib.doorman.data.MessageData;
import org.onap.ccsdk.features.lib.doorman.data.MessageStatus;
import org.onap.ccsdk.features.lib.doorman.data.MessageStatusValue;
import org.onap.ccsdk.features.lib.doorman.data.Queue;
import org.onap.ccsdk.features.lib.rlock.LockHelper;
import org.onap.ccsdk.features.lib.rlock.SynchronizedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageInterceptorImpl implements MessageInterceptor {

	private static final Logger log = LoggerFactory.getLogger(MessageInterceptorImpl.class);

	private MessageClassifier messageClassifier;
	private Map<String, MessageQueueHandler> handlerMap;
	private MessageProcessor messageProcessor;
	private MessageDao messageDao;

	private LockHelper lockHelper;
	private int lockTimeout = 10; // in seconds

	private Message message = null;
	private MessageQueueHandler handler = null;

	@Override
	public MessageData processRequest(MessageData request) {
		Date now = new Date();

		Queue q = null;
		String extMessageId = null;
		if (messageClassifier != null) {
			q = messageClassifier.determineQueue(request);
			extMessageId = messageClassifier.getExtMessageId(request);
		}

		long id = messageDao.addArrivedMessage(extMessageId, request, q, now);

		MessageStatus arrivedStatus = new MessageStatus();
		arrivedStatus.status = MessageStatusValue.ARRIVED;
		arrivedStatus.timestamp = now;
		messageDao.addStatus(id, arrivedStatus);

		message = new Message();
		message.messageId = id;
		message.request = request;
		message.arrivedTimestamp = now;
		message.queue = q;
		message.extMessageId = extMessageId;

		log.info("Message received: " + message);

		if (q != null && handlerMap != null) {
			handler = handlerMap.get(q.type);
		}

		if (q == null || handler == null) {
			processSync();
			return null; // Do nothing, normal message processing
		}

		Event event = Event.ARRIVED;

		while (true) {
			MessageFunction func = new MessageFunction(event);
			func.exec();

			MessageAction nextAction = func.getNextAction();
			if (nextAction == null) {
				processSync();
				return null;
			}

			switch (nextAction.action) {

				case PROCESS:
					processSync();
					return null;

				case HOLD: {
					event = waitForNewAction(nextAction.holdTime);
					break;
				}

				case RETURN_COMPLETE:
					returnComplete(nextAction);
					return nextAction.returnResponse;

				case RETURN_PROCESS:
					processAsync(nextAction.returnResponse);
					return nextAction.returnResponse;

				case RETURN_HOLD: {
					returnHold(nextAction);
					return nextAction.returnResponse;
				}
			}
		}
	}

	private void processSync() {
		messageDao.updateMessageStarted(message.messageId, new Date());
		log.info("Message processing started: " + message);
	}

	private void processAsync(MessageData returnResponse) {
		Thread t = new Thread(() -> processMessage(message.request), message.queue.type + "::" + message.queue.id + "::" + message.messageId);
		t.start();

		messageDao.updateMessageResponse(message.messageId, new Date(), returnResponse);
	}

	private void processMessage(MessageData request) {
		messageDao.updateMessageStarted(message.messageId, new Date());
		log.info("Message processing started: " + message);
		if (messageProcessor != null) {
			messageProcessor.processMessage(request);
		}

		MessageFunction func = new MessageFunction(Event.COMPLETED);
		func.exec();
		MessageAction nextAction = func.getNextAction();

		messageDao.updateMessageCompleted(message.messageId, nextAction.resolution, new Date());
		log.info("Message processing completed: " + message);
	}

	private void returnComplete(MessageAction nextAction) {
		Date now = new Date();
		messageDao.updateMessageResponse(message.messageId, now, nextAction.returnResponse);
		messageDao.updateMessageCompleted(message.messageId, nextAction.resolution, now);
		log.info("Message processing completed: " + message);
	}

	private void returnHold(MessageAction nextAction) {
		Thread t = new Thread(() -> asyncQueue(nextAction), message.queue.type + "::" + message.queue.id + "::" + message.messageId);
		t.start();
		messageDao.updateMessageResponse(message.messageId, new Date(), nextAction.returnResponse);
	}

	private void asyncQueue(MessageAction nextAction) {
		Event event = waitForNewAction(nextAction.holdTime);

		while (true) {
			MessageFunction func = new MessageFunction(event);
			func.exec();

			nextAction = func.getNextAction();
			if (nextAction == null) {
				processMessage(message.request);
				return;
			}

			switch (nextAction.action) {
				case PROCESS:
					processMessage(message.request);
					return;

				case HOLD: {
					event = waitForNewAction(nextAction.holdTime);
					break;
				}

				default:
					return;
			}
		}
	}

	private Event waitForNewAction(int holdTime) {
		long startTime = System.currentTimeMillis();
		long currentTime = startTime;
		while (currentTime - startTime <= (holdTime + 1) * 1000) {
			try {
				Thread.sleep(5000);
			} catch (Exception e) {
			}

			MessageAction nextAction = messageDao.getNextAction(message.messageId);
			if (nextAction != null && nextAction.action != MessageActionValue.HOLD) {
				return Event.AWAKEN;
			}

			currentTime = System.currentTimeMillis();
		}
		return Event.CHECK;
	}

	@Override
	public void processResponse(MessageData response) {
		if (message == null) {
			return;
		}

		String resolution = null;
		if (message.queue != null && handler != null) {
			MessageFunction func = new MessageFunction(Event.COMPLETED);
			func.exec();
			MessageAction nextAction = func.getNextAction();
			if (nextAction != null) {
				resolution = nextAction.resolution;
			}
		}

		Date now = new Date();
		messageDao.updateMessageResponse(message.messageId, now, response);
		messageDao.updateMessageCompleted(message.messageId, resolution, now);
		log.info("Message processing completed: " + message);
	}

	private class MessageFunction extends SynchronizedFunction {

		private Event event;

		private MessageAction nextAction = null; // Output

		public MessageFunction(Event event) {
			super(lockHelper, Collections.singleton(message.queue.type + "::" + message.queue.id), lockTimeout);
			this.event = event;
		}

		@Override
		protected void _exec() {
			List<Message> messageQueue = messageDao.readMessageQueue(message.queue);
			if (event == Event.AWAKEN) {
				for (Message m : messageQueue) {
					if (m.messageId == message.messageId) {
						nextAction = m.actionHistory.get(0);
					}
				}
				if (nextAction != null) {
					messageDao.updateActionDone(nextAction.actionId, new Date());
				}
			} else {
				Map<Long, MessageAction> nextActionMap = handler.nextAction(event, message, messageQueue);
				if (nextActionMap != null) {
					for (Message m : messageQueue) {
						MessageAction action = nextActionMap.get(m.messageId);
						if (action != null) {
							if (m.messageId == message.messageId) {
								action.actionStatus = ActionStatus.DONE;
								action.doneTimestamp = new Date();
								messageDao.addAction(m.messageId, action);
								nextAction = action;
							} else {
								MessageAction lastAction = m.actionHistory.get(0);
								if (lastAction.actionStatus != ActionStatus.PENDING || !action.same(lastAction)) {
									action.actionStatus = ActionStatus.PENDING;
									messageDao.addAction(m.messageId, action);
								}
							}
						}
					}
				}
			}
			if (nextAction != null) {
				log.info("Next message action: " + message + ":" + nextAction.action);
				MessageStatus status = determineStatus(nextAction);
				if (status != null) {
					messageDao.addStatus(message.messageId, status);
					log.info("Updating message status: " + message + ":" + status.status);
				}
			}
		}

		public MessageAction getNextAction() {
			return nextAction;
		}
	}

	private MessageStatus determineStatus(MessageAction action) {
		if (action == null) {
			return null;
		}

		MessageStatus s = new MessageStatus();
		s.timestamp = new Date();

		switch (action.action) {
			case PROCESS:
				s.status = MessageStatusValue.PROCESSING_SYNC;
				break;
			case HOLD:
			case RETURN_HOLD:
				s.status = MessageStatusValue.IN_QUEUE;
				break;
			case RETURN_PROCESS:
				s.status = MessageStatusValue.PROCESSING_ASYNC;
				break;
			case RETURN_COMPLETE:
				s.status = MessageStatusValue.COMPLETED;
				break;
		}

		return s;
	}

	public void setMessageDao(MessageDao messageDao) {
		this.messageDao = messageDao;
	}

	public void setLockHelper(LockHelper lockHelper) {
		this.lockHelper = lockHelper;
	}

	public void setLockTimeout(int lockTimeout) {
		this.lockTimeout = lockTimeout;
	}

	public void setMessageClassifier(MessageClassifier messageClassifier) {
		this.messageClassifier = messageClassifier;
	}

	public void setHandlerMap(Map<String, MessageQueueHandler> handlerMap) {
		this.handlerMap = handlerMap;
	}

	public void setMessageProcessor(MessageProcessor messageProcessor) {
		this.messageProcessor = messageProcessor;
	}
}
