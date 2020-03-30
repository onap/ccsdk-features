package org.onap.ccsdk.features.lib.doorman.it;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.onap.ccsdk.features.lib.doorman.data.MessageAction;
import org.onap.ccsdk.features.lib.doorman.data.MessageActionValue;
import org.onap.ccsdk.features.lib.doorman.data.MessageStatus;

public class MessageQueueDataItem implements Comparable<MessageQueueDataItem> {

	public Date timeStamp;
	public String extMessageId;
	public MessageStatus status;
	public MessageAction action;

	@Override
	public int compareTo(MessageQueueDataItem other) {
		int c = timeStamp.compareTo(other.timeStamp);
		if (c == 0) {
			if (action != null && other.status != null) {
				return -1;
			}
			if (status != null && other.action != null) {
				return 1;
			}
		}
		return c;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(extMessageId);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof MessageQueueDataItem)) {
			return false;
		}
		MessageQueueDataItem other = (MessageQueueDataItem) o;
		if (!extMessageId.equals(other.extMessageId)) {
			return false;
		}
		if (status != null && (other.status == null || status.status != other.status.status)) {
			return false;
		}
		if (action != null) {
			if (other.action == null || action.action != other.action.action) {
				return false;
			}
			if (action.action == MessageActionValue.HOLD || action.action == MessageActionValue.RETURN_HOLD) {
				if (action.holdTime != other.action.holdTime) {
					return false;
				}
			} else if (action.action == MessageActionValue.RETURN_COMPLETE) {
				if (!action.resolution.equals(other.action.resolution)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder ss = new StringBuilder();
		if (timeStamp != null) {
			ss.append(df.format(timeStamp)).append(" | ");
		}
		if (status != null) {
			ss.append("STATUS: ");
		} else {
			ss.append("ACTION: ");
		}
		ss.append(String.format("%-20s | ", extMessageId));
		if (status != null) {
			ss.append(status.status);
		} else {
			ss.append(action.action);
			if (action.holdTime > 0) {
				ss.append(" | ").append(action.holdTime);
			}
			if (action.resolution != null) {
				ss.append(" | ").append(action.resolution);
			}
		}
		return ss.toString();
	}

	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
}
