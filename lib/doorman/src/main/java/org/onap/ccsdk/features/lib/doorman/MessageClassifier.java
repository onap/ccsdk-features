package org.onap.ccsdk.features.lib.doorman;

import org.onap.ccsdk.features.lib.doorman.data.MessageData;
import org.onap.ccsdk.features.lib.doorman.data.Queue;

public interface MessageClassifier {

	Queue determineQueue(MessageData request);

	String getExtMessageId(MessageData request);
}
