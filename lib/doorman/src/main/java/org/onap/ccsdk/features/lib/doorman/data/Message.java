package org.onap.ccsdk.features.lib.doorman.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Message {

    private long messageId;
    private String extMessageId;
    private MessageData request;
    private MessageData response;
    private Date arrivedTimestamp;
    private Date startedTimestamp;
    private Date completedTimestamp;
    private Date responseTimestamp;
    private Queue queue;

    private List<MessageStatus> statusHistory;
    private List<MessageAction> actionHistory;

    public Message(long messageId, String extMessageId, MessageData request, Queue queue) {
        this.messageId = messageId;
        this.extMessageId = extMessageId;
        this.request = request;
        this.queue = queue;
    }

    public Message(long messageId, String extMessageId, MessageData request, MessageData response,
            Date arrivedTimestamp, Date startedTimestamp, Date completedTimestamp, Date responseTimestamp, Queue queue,
            List<MessageStatus> statusHistory, List<MessageAction> actionHistory) {
        this.messageId = messageId;
        this.extMessageId = extMessageId;
        this.request = request;
        this.response = response;
        this.arrivedTimestamp = arrivedTimestamp;
        this.startedTimestamp = startedTimestamp;
        this.completedTimestamp = completedTimestamp;
        this.responseTimestamp = responseTimestamp;
        this.queue = queue;
        this.statusHistory = new ArrayList<>(statusHistory);
        this.actionHistory = new ArrayList<>(actionHistory);
    }

    @Override
    public String toString() {
        StringBuilder ss = new StringBuilder();
        ss.append(messageId);
        if (extMessageId != null) {
            ss.append("::").append(extMessageId);
        }
        if (queue != null) {
            ss.append("::").append(queue);
        }
        return ss.toString();
    }

    public int getTimeInQueue(Date now) {
        // Find the elapsed time since message arrived. That will be the timestamp of the first
        // status of the message (last status in the status history list)
        if (statusHistory != null && !statusHistory.isEmpty()) {
            MessageStatus receivedStatus = statusHistory.get(statusHistory.size() - 1);
            return (int) ((now.getTime() - receivedStatus.getTimestamp().getTime()) / 1000);
        }
        return 0;
    }

    public long getMessageId() {
        return messageId;
    }

    public String getExtMessageId() {
        return extMessageId;
    }

    public MessageData getRequest() {
        return request;
    }

    public MessageData getResponse() {
        return response;
    }

    public Date getArrivedTimestamp() {
        return arrivedTimestamp;
    }

    public Date getStartedTimestamp() {
        return startedTimestamp;
    }

    public Date getCompletedTimestamp() {
        return completedTimestamp;
    }

    public Date getResponseTimestamp() {
        return responseTimestamp;
    }

    public Queue getQueue() {
        return queue;
    }

    public List<MessageStatus> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    public List<MessageAction> getActionHistory() {
        return Collections.unmodifiableList(actionHistory);
    }
}
