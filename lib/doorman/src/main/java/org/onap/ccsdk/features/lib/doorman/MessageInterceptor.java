package org.onap.ccsdk.features.lib.doorman;

import org.onap.ccsdk.features.lib.doorman.data.MessageData;

public interface MessageInterceptor {

	MessageData processRequest(MessageData request);

	void processResponse(MessageData response);
}
