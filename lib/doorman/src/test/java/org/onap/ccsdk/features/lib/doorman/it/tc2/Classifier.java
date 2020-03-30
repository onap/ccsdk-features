package org.onap.ccsdk.features.lib.doorman.it.tc2;

import org.onap.ccsdk.features.lib.doorman.MessageClassifier;
import org.onap.ccsdk.features.lib.doorman.data.MessageData;
import org.onap.ccsdk.features.lib.doorman.data.Queue;

public class Classifier implements MessageClassifier {

	@Override
	public Queue determineQueue(MessageData request) {
		Queue q = new Queue();
		q.type = "Cluster";
		q.id = "test-queue";
		return q;
	}

	@Override
	public String getExtMessageId(MessageData request) {
		return (String) request.param.get("request_id");
	}
}
