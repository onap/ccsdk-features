package org.onap.ccsdk.features.lib.doorman.data;

import java.util.Date;

public class MessageAction {

    private long actionId;
    private MessageActionValue action;
    private ActionStatus actionStatus;
    private String resolution;
    private Date timestamp;
    private Date doneTimestamp;
    private int holdTime; // in seconds
    private MessageData returnResponse;

    public MessageAction(long actionId, MessageActionValue action, ActionStatus actionStatus, String resolution,
            Date timestamp, Date doneTimestamp, int holdTime, MessageData returnResponse) {
        this.actionId = actionId;
        this.action = action;
        this.actionStatus = actionStatus;
        this.resolution = resolution;
        this.timestamp = timestamp;
        this.doneTimestamp = doneTimestamp;
        this.holdTime = holdTime;
        this.returnResponse = returnResponse;
    }

    public MessageAction(MessageActionValue action, String resolution, int holdTime, MessageData returnResponse) {
        this.action = action;
        actionStatus = ActionStatus.PENDING;
        this.resolution = resolution;
        timestamp = new Date();
        this.holdTime = holdTime;
        this.returnResponse = returnResponse;
    }

    public void setDone() {
        actionStatus = ActionStatus.DONE;
        doneTimestamp = new Date();
    }

    @Override
    public String toString() {
        return action.toString() + " | " + actionStatus + " | " + resolution + " | " + holdTime + " | "
                + returnResponse;
    }

    public boolean same(MessageAction a2) {
        if (action != a2.action) {
            return false;
        }
        if (action == MessageActionValue.HOLD || action == MessageActionValue.RETURN_HOLD) {
            if (holdTime != a2.holdTime) {
                return false;
            }
        }
        if (action == MessageActionValue.RETURN_COMPLETE || action == MessageActionValue.RETURN_HOLD
                || action == MessageActionValue.RETURN_PROCESS) {
            if (returnResponse == null != (a2.returnResponse == null)) {
                return false;
            }
            if (returnResponse != null) {
                if (returnResponse.getParam() == null != (a2.returnResponse.getParam() == null)) {
                    return false;
                }
                if (returnResponse.getParam() != null && !returnResponse.getParam().equals(a2.returnResponse.getParam())) {
                    return false;
                }
                if (returnResponse.getBody() == null != (a2.returnResponse.getBody() == null)) {
                    return false;
                }
                if (returnResponse.getBody() != null && !returnResponse.getBody().equals(a2.returnResponse.getBody())) {
                    return false;
                }
            }
        }
        if (action == MessageActionValue.RETURN_COMPLETE) {
            if (resolution == null != (a2.resolution == null)) {
                return false;
            }
            if (resolution != null && !resolution.equals(a2.resolution)) {
                return false;
            }
        }
        return true;
    }

    public long getActionId() {
        return actionId;
    }

    public MessageActionValue getAction() {
        return action;
    }

    public ActionStatus getActionStatus() {
        return actionStatus;
    }

    public String getResolution() {
        return resolution;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Date getDoneTimestamp() {
        return doneTimestamp;
    }

    public int getHoldTime() {
        return holdTime;
    }

    public MessageData getReturnResponse() {
        return returnResponse;
    }
}
