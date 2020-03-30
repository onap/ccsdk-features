package org.onap.ccsdk.features.lib.doorman.data;

import java.util.Date;
import java.util.List;

public class Message {

	public long messageId;
	public String extMessageId;
	public MessageData request;
	public MessageData response;
	public Date arrivedTimestamp;
	public Date startedTimestamp;
	public Date completedTimestamp;
	public Date responseTimestamp;
	public Queue queue;

	public List<MessageStatus> statusHistory;
	public List<MessageAction> actionHistory;

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
}
