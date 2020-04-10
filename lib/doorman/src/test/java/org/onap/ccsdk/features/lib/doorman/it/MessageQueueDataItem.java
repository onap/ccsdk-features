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
        if (status != null && (other.status == null || status.getStatus() != other.status.getStatus())) {
            return false;
        }
        if (action != null) {
            if (other.action == null || action.getAction() != other.action.getAction()) {
                return false;
            }
            if (action.getAction() == MessageActionValue.HOLD || action.getAction() == MessageActionValue.RETURN_HOLD) {
                if (action.getHoldTime() != other.action.getHoldTime()) {
                    return false;
                }
            } else if (action.getAction() == MessageActionValue.RETURN_COMPLETE) {
                if (!action.getResolution().equals(other.action.getResolution())) {
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
            ss.append(status.getStatus());
        } else {
            ss.append(action.getAction());
            if (action.getHoldTime() > 0) {
                ss.append(" | ").append(action.getHoldTime());
            }
            if (action.getResolution() != null) {
                ss.append(" | ").append(action.getResolution());
            }
        }
        return ss.toString();
    }

    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
}
