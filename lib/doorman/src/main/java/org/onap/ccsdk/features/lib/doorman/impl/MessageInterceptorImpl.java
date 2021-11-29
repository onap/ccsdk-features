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

        Queue queue = null;
        String extMessageId = null;
        if (messageClassifier != null) {
            queue = messageClassifier.determineQueue(request);
            extMessageId = messageClassifier.getExtMessageId(request);
        }

        long id = messageDao.addArrivedMessage(extMessageId, request, queue, now);

        MessageStatus arrivedStatus = new MessageStatus(MessageStatusValue.ARRIVED, now);
        messageDao.addStatus(id, arrivedStatus);

        message = new Message(id, extMessageId, request, queue);

        log.info("Message received: " + message);

        if (queue != null && handlerMap != null) {
            handler = handlerMap.get(queue.getType());
        }

        if (queue == null || handler == null) {
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

            switch (nextAction.getAction()) {

                case PROCESS:
                    processSync();
                    return null;

                case HOLD: {
                    event = waitForNewAction(nextAction.getHoldTime());
                    break;
                }

                case RETURN_COMPLETE:
                    returnComplete(nextAction);
                    return nextAction.getReturnResponse();

                case RETURN_PROCESS:
                    processAsync(nextAction.getReturnResponse());
                    return nextAction.getReturnResponse();

                case RETURN_HOLD: {
                    returnHold(nextAction);
                    return nextAction.getReturnResponse();
                }
            }
        }
    }

    private void processSync() {
        messageDao.updateMessageStarted(message.getMessageId(), new Date());
        log.info("Message processing started: " + message);
    }

    private void processAsync(MessageData returnResponse) {
        Thread t = new Thread(() -> processMessage(message.getRequest()),
                message.getQueue().getType() + "::" + message.getQueue().getId() + "::" + message.getMessageId());
        t.start();

        messageDao.updateMessageResponse(message.getMessageId(), new Date(), returnResponse);
    }

    private void processMessage(MessageData request) {
        messageDao.updateMessageStarted(message.getMessageId(), new Date());
        log.info("Message processing started: " + message);
        if (messageProcessor != null) {
            messageProcessor.processMessage(request);
        }

        MessageFunction func = new MessageFunction(Event.COMPLETED);
        func.exec();
        MessageAction nextAction = func.getNextAction();

        messageDao.updateMessageCompleted(message.getMessageId(), nextAction.getResolution(), new Date());
        log.info("Message processing completed: " + message);
    }

    private void returnComplete(MessageAction nextAction) {
        Date now = new Date();
        messageDao.updateMessageResponse(message.getMessageId(), now, nextAction.getReturnResponse());
        messageDao.updateMessageCompleted(message.getMessageId(), nextAction.getResolution(), now);
        log.info("Message processing completed: " + message);
    }

    private void returnHold(MessageAction nextAction) {
        Thread t = new Thread(() -> asyncQueue(nextAction),
                message.getQueue().getType() + "::" + message.getQueue().getId() + "::" + message.getMessageId());
        t.start();
        messageDao.updateMessageResponse(message.getMessageId(), new Date(), nextAction.getReturnResponse());
    }

    private void asyncQueue(MessageAction nextAction) {
        Event event = waitForNewAction(nextAction.getHoldTime());

        while (true) {
            MessageFunction func = new MessageFunction(event);
            func.exec();

            nextAction = func.getNextAction();
            if (nextAction == null) {
                processMessage(message.getRequest());
                return;
            }

            switch (nextAction.getAction()) {
                case PROCESS:
                    processMessage(message.getRequest());
                    return;

                case HOLD: {
                    event = waitForNewAction(nextAction.getHoldTime());
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
        while (currentTime - startTime <= (holdTime + 1) * 1000L) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.info("Break sleep : " + e.getMessage());
                Thread.currentThread().interrupt();
            }

            MessageAction nextAction = messageDao.getNextAction(message.getMessageId());
            if (nextAction != null && nextAction.getAction() != MessageActionValue.HOLD) {
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
        if (message.getQueue() != null && handler != null) {
            MessageFunction func = new MessageFunction(Event.COMPLETED);
            func.exec();
            MessageAction nextAction = func.getNextAction();
            if (nextAction != null) {
                resolution = nextAction.getResolution();
            }
        }

        Date now = new Date();
        messageDao.updateMessageResponse(message.getMessageId(), now, response);
        messageDao.updateMessageCompleted(message.getMessageId(), resolution, now);
        log.info("Message processing completed: " + message);
    }

    private class MessageFunction extends SynchronizedFunction {

        private Event event;

        private MessageAction nextAction = null; // Output

        public MessageFunction(Event event) {
            super(lockHelper, Collections.singleton(message.getQueue().getType() + "::" + message.getQueue().getId()),
                    lockTimeout);
            this.event = event;
        }

        @Override
        protected void _exec() {
            List<Message> messageQueue = messageDao.readMessageQueue(message.getQueue());
            if (event == Event.AWAKEN) {
                for (Message m : messageQueue) {
                    if (m.getMessageId() == message.getMessageId()) {
                        nextAction = m.getActionHistory().get(0);
                    }
                }
                if (nextAction != null) {
                    messageDao.updateActionDone(nextAction.getActionId(), new Date());
                }
            } else {
                Map<Long, MessageAction> nextActionMap = handler.nextAction(event, message, messageQueue);
                if (nextActionMap != null) {
                    for (Message m : messageQueue) {
                        MessageAction action = nextActionMap.get(m.getMessageId());
                        if (action != null) {
                            if (m.getMessageId() == message.getMessageId()) {
                                action.setDone();
                                messageDao.addAction(m.getMessageId(), action);
                                nextAction = action;
                            } else {
                                MessageAction lastAction = m.getActionHistory().get(0);
                                if (lastAction.getActionStatus() != ActionStatus.PENDING || !action.same(lastAction)) {
                                    messageDao.addAction(m.getMessageId(), action);
                                }
                            }
                        }
                    }
                }
            }
            if (nextAction != null) {
                log.info("Next message action: " + message + ":" + nextAction.getAction());
                MessageStatus status = determineStatus(nextAction);
                if (status != null) {
                    messageDao.addStatus(message.getMessageId(), status);
                    log.info("Updating message status: " + message + ":" + status.getStatus());
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

        switch (action.getAction()) {
            case PROCESS:
                return new MessageStatus(MessageStatusValue.PROCESSING_SYNC);
            case HOLD:
            case RETURN_HOLD:
                return new MessageStatus(MessageStatusValue.IN_QUEUE);
            case RETURN_PROCESS:
                return new MessageStatus(MessageStatusValue.PROCESSING_ASYNC);
            case RETURN_COMPLETE:
                return new MessageStatus(MessageStatusValue.COMPLETED);
        }
        return null;
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
