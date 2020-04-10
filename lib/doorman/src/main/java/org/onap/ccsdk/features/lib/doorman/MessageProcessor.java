package org.onap.ccsdk.features.lib.doorman;

import org.onap.ccsdk.features.lib.doorman.data.MessageData;

public interface MessageProcessor {

    void processMessage(MessageData request);
}
