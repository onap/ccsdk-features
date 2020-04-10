package org.onap.ccsdk.features.lib.doorman.data;

import java.util.Date;

public class MessageAction {

	public long actionId;
	public MessageActionValue action;
	public ActionStatus actionStatus;
	public String resolution;
	public Date timestamp;
	public Date doneTimestamp;
	public int holdTime; // in seconds
	public MessageData returnResponse;

	@Override
	public String toString() {
		return action.toString() + " | " + actionStatus + " | " + resolution + " | " + holdTime + " | " + returnResponse;
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
		if (action == MessageActionValue.RETURN_COMPLETE || action == MessageActionValue.RETURN_HOLD || action == MessageActionValue.RETURN_PROCESS) {
			if (returnResponse == null != (a2.returnResponse == null)) {
				return false;
			}
			if (returnResponse != null) {
				if (returnResponse.param == null != (a2.returnResponse.param == null)) {
					return false;
				}
				if (returnResponse.param != null && !returnResponse.param.equals(a2.returnResponse.param)) {
					return false;
				}
				if (returnResponse.body == null != (a2.returnResponse.body == null)) {
					return false;
				}
				if (returnResponse.body != null && !returnResponse.body.equals(a2.returnResponse.body)) {
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
}
