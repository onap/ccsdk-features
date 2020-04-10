package org.onap.ccsdk.features.lib.doorman.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.onap.ccsdk.features.lib.doorman.MessageQueueHandler;
import org.onap.ccsdk.features.lib.doorman.data.Event;
import org.onap.ccsdk.features.lib.doorman.data.Message;
import org.onap.ccsdk.features.lib.doorman.data.MessageAction;
import org.onap.ccsdk.features.lib.doorman.data.MessageActionValue;
import org.onap.ccsdk.features.lib.doorman.data.MessageData;
import org.onap.ccsdk.features.lib.doorman.data.MessageStatus;
import org.onap.ccsdk.features.lib.doorman.data.MessageStatusValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageHandlerBaseImpl implements MessageQueueHandler {

	private static final Logger log = LoggerFactory.getLogger(MessageHandlerBaseImpl.class);

	private boolean async = false;
	private int maxParallelCount = 1;
	private int maxQueueSize = 0;
	private int timeToLive = 0; // in seconds
	private int maxTimeInQueue = 60; // in seconds
	private int updateWaitTime = 60; // in seconds

	@Override
	public Map<Long, MessageAction> nextAction(Event event, Message msg, List<Message> queue) {
		long t1 = System.currentTimeMillis();
		log.info(">>>>> Handler started for message: " + msg + ": " + event);

		PrioritizedMessage pmsg = null;
		List<PrioritizedMessage> processing = new ArrayList<>();
		List<PrioritizedMessage> waiting = new ArrayList<>();
		Date now = new Date();
		for (Message m : queue) {
			PrioritizedMessage pm = new PrioritizedMessage();
			pm.message = m;
			pm.priority = assignPriority(msg);
			pm.timeInQueue = getTimeInQueue(now, m);
			pm.updateGroup = determineUpdateGroup(m);
			pm.updateSequence = determineUpdateSequence(m);

			if (pm.message.messageId == msg.messageId) {
				pmsg = pm;

				if (event != Event.COMPLETED) {
					waiting.add(pm);
				}
			} else {
				MessageStatusValue s = m.statusHistory.get(0).status;
				if (s == MessageStatusValue.IN_QUEUE) {
					waiting.add(pm);
				} else if (s == MessageStatusValue.PROCESSING_SYNC || s == MessageStatusValue.PROCESSING_ASYNC) {
					processing.add(pm);
				}
			}
		}

		log.info("    Processing:");
		for (PrioritizedMessage pm : processing) {
			log.info("      -- " + pm.message + " | " + pm.priority + " | " + pm.timeInQueue + " | " + pm.updateGroup + " | " + pm.updateSequence);
		}
		log.info("    Waiting:");
		for (PrioritizedMessage pm : waiting) {
			log.info("      -- " + pm.message + " | " + pm.priority + " | " + pm.timeInQueue + " | " + pm.updateGroup + " | " + pm.updateSequence);
		}

		log.info("    Determined actions:");

		Collections.sort(waiting, (pm1, pm2) -> comparePrioritizedMessages(pm1, pm2));

		Map<Long, MessageAction> mm = new HashMap<>();

		List<PrioritizedMessage> skipList = findMessagesToSkip(processing, waiting);
		addNextActionComplete(mm, skipList, Resolution.SKIPPED, now);

		waiting.removeAll(skipList);

		List<PrioritizedMessage> expiredList = findExpiredMessages(processing, waiting);
		addNextActionComplete(mm, expiredList, Resolution.EXPIRED, now);

		waiting.removeAll(expiredList);

		List<PrioritizedMessage> dropList = findMessagesToDrop(processing, waiting);
		addNextActionComplete(mm, dropList, Resolution.DROPPED, now);

		waiting.removeAll(dropList);

		List<PrioritizedMessage> processList = findMessagesToProcess(processing, waiting);
		for (PrioritizedMessage pm : processList) {
			MessageAction a = new MessageAction();
			a.timestamp = now;
			if (async) {
				a.action = MessageActionValue.RETURN_PROCESS;
				a.returnResponse = ackResponse(pm.message);
			} else {
				a.action = MessageActionValue.PROCESS;
			}
			mm.put(pm.message.messageId, a);
			log.info("      -- Next action for message: " + pm.message + ": " + a.action);
		}

		if (event != Event.COMPLETED && !mm.containsKey(pmsg.message.messageId)) {
			MessageAction a = new MessageAction();
			a.timestamp = now;
			a.holdTime = pmsg.updateGroup != null ? updateWaitTime : maxTimeInQueue;
			if (async) {
				a.action = MessageActionValue.RETURN_HOLD;
				a.returnResponse = ackResponse(pmsg.message);
			} else {
				a.action = MessageActionValue.HOLD;
			}
			mm.put(pmsg.message.messageId, a);
			log.info("      -- Next action for message: " + pmsg.message + ": " + a.action + ": " + a.holdTime);

			waiting.remove(pmsg);
		}

		if (event == Event.COMPLETED) {
			MessageAction a = new MessageAction();
			a.timestamp = now;
			a.action = MessageActionValue.RETURN_COMPLETE;
			a.resolution = Resolution.PROCESSED.toString();
			a.returnResponse = completeResponse(Resolution.PROCESSED, msg);
			mm.put(pmsg.message.messageId, a);
			log.info("      -- Next action for message: " + pmsg.message + ": " + a.action + ": " + a.resolution);
		}

		long t2 = System.currentTimeMillis();
		log.info("<<<<< Handler completed for message: " + msg + ": " + event + ": Time: " + (t2 - t1));
		return mm;
	}

	private void addNextActionComplete(Map<Long, MessageAction> mm, List<PrioritizedMessage> skipList, Resolution r, Date now) {
		for (PrioritizedMessage pm : skipList) {
			MessageAction a = new MessageAction();
			a.action = MessageActionValue.RETURN_COMPLETE;
			a.resolution = r.toString();
			a.timestamp = now;
			a.returnResponse = completeResponse(r, pm.message);
			mm.put(pm.message.messageId, a);
			log.info("      -- Next action for message: " + pm.message + ": " + a.action + ": " + a.resolution);
		}
	}

	protected List<PrioritizedMessage> findMessagesToSkip(List<PrioritizedMessage> processing, List<PrioritizedMessage> waiting) {
		List<PrioritizedMessage> ll = new ArrayList<>();
		Map<String, PrioritizedMessage> lastMap = new HashMap<>();
		for (PrioritizedMessage pm : waiting) {
			if (pm.updateGroup != null) {
				PrioritizedMessage last = lastMap.get(pm.updateGroup);
				if (last == null) {
					lastMap.put(pm.updateGroup, pm);
				} else {
					if (pm.updateSequence > last.updateSequence) {
						ll.add(last);
						lastMap.put(pm.updateGroup, pm);
					}
				}
			}
		}
		return ll;
	}

	protected List<PrioritizedMessage> findExpiredMessages(List<PrioritizedMessage> processing, List<PrioritizedMessage> waiting) {
		List<PrioritizedMessage> ll = new ArrayList<>();
		if (timeToLive > 0) {
			for (PrioritizedMessage pm : waiting) {
				if (pm.timeInQueue > timeToLive) {
					ll.add(pm);
				}
			}
		}
		return ll;
	}

	protected List<PrioritizedMessage> findMessagesToDrop(List<PrioritizedMessage> processing, List<PrioritizedMessage> waiting) {
		List<PrioritizedMessage> ll = new ArrayList<>();
		if (maxQueueSize > 0) {
			// Drop the least important messages (last in the prioritized waiting list)
			for (int i = maxQueueSize; i < waiting.size(); i++) {
				ll.add(waiting.get(i));
			}
		}
		return ll;
	}

	protected List<PrioritizedMessage> findMessagesToProcess(List<PrioritizedMessage> processing, List<PrioritizedMessage> waiting) {
		List<PrioritizedMessage> ll = new ArrayList<>();

		if (processing.size() >= maxParallelCount) {
			return ll;
		}

		// Find messages allowed to be processed based on the concurrency rules

		List<List<String>> currentLocks = new ArrayList<>();
		for (PrioritizedMessage m : processing) {
			List<List<String>> locks = determineConcurency(m.message);
			if (locks != null) {
				currentLocks.addAll(locks);
			}
		}

		List<PrioritizedMessage> allowed = new ArrayList<>();
		for (PrioritizedMessage m : waiting) {
			List<List<String>> neededLocks = determineConcurency(m.message);
			if (allowed(currentLocks, neededLocks)) {
				allowed.add(m);
			}
		}

		// Remove messages that are waiting on hold
		Iterator<PrioritizedMessage> ii = allowed.iterator();
		while (ii.hasNext()) {
			PrioritizedMessage pm = ii.next();
			if (pm.updateGroup != null) {
				log.info("              --- Check: " + pm.message + ": " + pm.timeInQueue + " >= " + updateWaitTime);
				if (pm.timeInQueue < updateWaitTime) {
					ii.remove();
				}
			}
		}

		// Limit the number of processing messages to maxParallelCount

		for (int i = 0; i < allowed.size() && i < maxParallelCount - processing.size(); i++) {
			ll.add(allowed.get(i));
		}

		return ll;
	}

	private int comparePrioritizedMessages(PrioritizedMessage pm1, PrioritizedMessage pm2) {
		if (pm1.timeInQueue >= maxTimeInQueue && pm2.timeInQueue < maxTimeInQueue) {
			return -1;
		}
		if (pm1.timeInQueue < maxTimeInQueue && pm2.timeInQueue >= maxTimeInQueue) {
			return 1;
		}
		if (pm1.priority < pm2.priority) {
			return -1;
		}
		if (pm1.priority > pm2.priority) {
			return 1;
		}
		if (pm1.timeInQueue > pm2.timeInQueue) {
			return -1;
		}
		if (pm1.timeInQueue < pm2.timeInQueue) {
			return 1;
		}
		return 0;
	}

	private int getTimeInQueue(Date now, Message m) {
		// Find the elapsed time since message arrived. That will be the timestamp of the first
		// status of the message (last status in the status history list)
		MessageStatus receivedStatus = m.statusHistory.get(m.statusHistory.size() - 1);
		return (int) ((now.getTime() - receivedStatus.timestamp.getTime()) / 1000);
	}

	private boolean allowed(List<List<String>> currentLocks, List<List<String>> neededLocks) {
		if (neededLocks == null) {
			return true;
		}
		for (List<String> neededLockLevels : neededLocks) {
			for (List<String> currentLockLevels : currentLocks) {
				int n = neededLockLevels.size() < currentLockLevels.size() ? neededLockLevels.size() : currentLockLevels.size();
				boolean good = false;
				for (int i = 0; i < n; i++) {
					if (!neededLockLevels.get(i).equals(currentLockLevels.get(i))) {
						good = true;
						break;
					}
				}
				if (!good) {
					return false;
				}
			}
		}
		return true;
	}

	protected void initMessage(Message msg) {
	}

	protected void closeMessage(Message msg) {
	}

	protected long assignPriority(Message msg) {
		return msg.messageId; // FIFO by default
	}

	protected String determineUpdateGroup(Message msg) {
		return null;
	}

	protected long determineUpdateSequence(Message msg) {
		return msg.messageId;
	}

	protected List<List<String>> determineConcurency(Message msg) {
		return null;
	}

	protected MessageData ackResponse(Message msg) {
		return null;
	}

	protected MessageData completeResponse(Resolution r, Message msg) {
		return null;
	}

	protected static enum Resolution {
		PROCESSED, SKIPPED, EXPIRED, DROPPED
	}

	protected static class PrioritizedMessage {

		public Message message;
		public long priority;
		public int timeInQueue;
		public String updateGroup;
		public long updateSequence;
	}

	public void setMaxParallelCount(int maxParallelCount) {
		this.maxParallelCount = maxParallelCount;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	public void setMaxTimeInQueue(int maxTimeInQueue) {
		this.maxTimeInQueue = maxTimeInQueue;
	}

	public void setUpdateWaitTime(int updateWaitTime) {
		this.updateWaitTime = updateWaitTime;
	}

	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}
}
