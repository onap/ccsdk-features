package org.onap.ccsdk.features.lib.doorman.dao;

import java.util.Date;
import java.util.List;
import org.onap.ccsdk.features.lib.doorman.data.Message;
import org.onap.ccsdk.features.lib.doorman.data.MessageAction;
import org.onap.ccsdk.features.lib.doorman.data.MessageData;
import org.onap.ccsdk.features.lib.doorman.data.MessageStatus;
import org.onap.ccsdk.features.lib.doorman.data.Queue;

public interface MessageDao {

    long addArrivedMessage(String extMessageId, MessageData request, Queue queue, Date timestamp);

    void updateMessageStarted(long messageId, Date timestamp);

    void updateMessageCompleted(long messageId, String resolution, Date timestamp);

    void updateMessageResponse(long messageId, Date timestamp, MessageData response);

    void addStatus(long messageId, MessageStatus status);

    void addAction(long messageId, MessageAction action);

    void updateActionDone(long actionId, Date now);

    List<Message> readMessageQueue(Queue queue);

    MessageAction getNextAction(long messageId);
}
