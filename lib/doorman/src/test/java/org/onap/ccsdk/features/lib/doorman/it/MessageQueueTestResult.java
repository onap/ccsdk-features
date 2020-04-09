package org.onap.ccsdk.features.lib.doorman.it;

import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.onap.ccsdk.features.lib.doorman.dao.MessageDaoImpl;
import org.onap.ccsdk.features.lib.doorman.data.Message;
import org.onap.ccsdk.features.lib.doorman.data.MessageAction;
import org.onap.ccsdk.features.lib.doorman.data.MessageActionValue;
import org.onap.ccsdk.features.lib.doorman.data.MessageStatus;
import org.onap.ccsdk.features.lib.doorman.data.MessageStatusValue;
import org.onap.ccsdk.features.lib.doorman.data.Queue;
import org.onap.ccsdk.features.lib.doorman.testutil.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageQueueTestResult {

    private static final Logger log = LoggerFactory.getLogger(MessageQueueTest.class);

    public static List<MessageQueueDataItem> readResult(String queueType, String queueId) {
        MessageDaoImpl messageDao = new MessageDaoImpl();
        messageDao.setDataSource(DbUtil.getDataSource());

        Queue q = new Queue(queueType, queueId);

        List<Message> messageList = messageDao.readMessageQueue(q);

        List<MessageQueueDataItem> ll = new ArrayList<>();
        if (messageList != null) {
            for (Message m : messageList) {
                if (m.getStatusHistory() != null) {
                    for (MessageStatus s : m.getStatusHistory()) {
                        MessageQueueDataItem item = new MessageQueueDataItem();
                        item.extMessageId = m.getExtMessageId();
                        item.status = s;
                        item.timeStamp = s.getTimestamp();
                        ll.add(item);
                    }
                }
                if (m.getActionHistory() != null) {
                    for (MessageAction a : m.getActionHistory()) {
                        MessageQueueDataItem item = new MessageQueueDataItem();
                        item.extMessageId = m.getExtMessageId();
                        item.action = a;
                        item.timeStamp = a.getTimestamp();
                        ll.add(item);
                    }
                }
            }
        }
        Collections.sort(ll);
        return ll;
    }

    @SuppressWarnings("unchecked")
    public static void checkResult(Map<String, Object> testResult) {
        List<Object> resultSetupList = (List<Object>) testResult.get("queue_list");
        if (resultSetupList != null) {
            for (Object o : resultSetupList) {
                Map<String, Object> resultSetup = (Map<String, Object>) o;
                String queueType = (String) resultSetup.get("queue_type");
                String queueId = (String) resultSetup.get("queue_id");
                log.info(queueType + "::" + queueId);
                List<MessageQueueDataItem> itemList = MessageQueueTestResult.readResult(queueType, queueId);
                for (MessageQueueDataItem item : itemList) {
                    log.info("    --- " + item);
                }

                List<Object> checkSequenceList = (List<Object>) resultSetup.get("check_sequence_list");
                for (int i = 0; i < checkSequenceList.size(); i++) {
                    List<Object> checkSequence = (List<Object>) checkSequenceList.get(i);
                    List<MessageQueueDataItem> checkItemList = new ArrayList<>();
                    for (Object o2 : checkSequence) {
                        String[] ss = ((String) o2).split("\\|");
                        MessageQueueDataItem item = new MessageQueueDataItem();
                        item.extMessageId = ss[1].trim();
                        if (ss[0].trim().equals("STATUS")) {
                            item.status = new MessageStatus(MessageStatusValue.valueOf(ss[2].trim()));
                        } else {
                            MessageActionValue action = MessageActionValue.valueOf(ss[2].trim());
                            int holdTime = 0;
                            String resolution = null;
                            if (action == MessageActionValue.HOLD || action == MessageActionValue.RETURN_HOLD) {
                                holdTime = Integer.parseInt(ss[3].trim());
                            } else if (action == MessageActionValue.RETURN_COMPLETE) {
                                resolution = ss[3].trim();
                            }
                            item.action = new MessageAction(action, resolution, holdTime, null);
                        }
                        checkItemList.add(item);
                    }
                    List<MessageQueueDataItem> itemList1 = new ArrayList<>(itemList);
                    itemList1.retainAll(checkItemList);
                    if (!itemList1.equals(checkItemList)) {
                        log.info("Expected sequence #" + i + " not found");
                        log.info("Expected sequence:");
                        for (MessageQueueDataItem item : checkItemList) {
                            log.info("    --- " + item);
                        }
                        log.info("Found sequence:");
                        for (MessageQueueDataItem item : itemList1) {
                            log.info("    --- " + item);
                        }
                        fail("Expected sequence #" + i + " not found");
                    } else {
                        log.info("Expected sequence #" + i + " found in the result:");
                        for (MessageQueueDataItem item : checkItemList) {
                            log.info("    --- " + item);
                        }
                    }
                }
            }
        }
    }
}
