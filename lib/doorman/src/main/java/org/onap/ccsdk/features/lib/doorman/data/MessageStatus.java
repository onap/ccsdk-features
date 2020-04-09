package org.onap.ccsdk.features.lib.doorman.data;

import java.util.Date;

public class MessageStatus {

    private MessageStatusValue status;
    private Date timestamp;

    public MessageStatus(MessageStatusValue status, Date timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }

    public MessageStatus(MessageStatusValue status) {
        this.status = status;
        timestamp = new Date();
    }

    public MessageStatusValue getStatus() {
        return status;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
