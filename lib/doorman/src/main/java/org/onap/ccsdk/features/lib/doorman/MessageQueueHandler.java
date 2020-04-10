package org.onap.ccsdk.features.lib.doorman;

import java.util.List;
import java.util.Map;
import org.onap.ccsdk.features.lib.doorman.data.Event;
import org.onap.ccsdk.features.lib.doorman.data.Message;
import org.onap.ccsdk.features.lib.doorman.data.MessageAction;

public interface MessageQueueHandler {

    Map<Long, MessageAction> nextAction(Event event, Message msg, List<Message> queue);
}
