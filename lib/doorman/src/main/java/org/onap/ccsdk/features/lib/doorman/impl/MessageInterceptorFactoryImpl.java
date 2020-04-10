package org.onap.ccsdk.features.lib.doorman.impl;

import java.util.Map;
import org.onap.ccsdk.features.lib.doorman.MessageClassifier;
import org.onap.ccsdk.features.lib.doorman.MessageInterceptor;
import org.onap.ccsdk.features.lib.doorman.MessageInterceptorFactory;
import org.onap.ccsdk.features.lib.doorman.MessageProcessor;
import org.onap.ccsdk.features.lib.doorman.MessageQueueHandler;
import org.onap.ccsdk.features.lib.doorman.dao.MessageDao;
import org.onap.ccsdk.features.lib.rlock.LockHelper;

public class MessageInterceptorFactoryImpl implements MessageInterceptorFactory {

    private MessageClassifier messageClassifier;
    private Map<String, MessageQueueHandler> handlerMap;
    private MessageProcessor messageProcessor;
    private MessageDao messageDao;

    private LockHelper lockHelper;
    private int lockTimeout; // in seconds

    @Override
    public MessageInterceptor create() {
        MessageInterceptorImpl mi = new MessageInterceptorImpl();
        mi.setMessageClassifier(messageClassifier);
        mi.setHandlerMap(handlerMap);
        mi.setMessageProcessor(messageProcessor);
        mi.setMessageDao(messageDao);
        mi.setLockHelper(lockHelper);
        mi.setLockTimeout(lockTimeout);
        return mi;
    }

    public void setMessageDao(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    public void setLockHelper(LockHelper lockHelper) {
        this.lockHelper = lockHelper;
    }

    public void setLockTimeout(int lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    public void setMessageClassifier(MessageClassifier messageClassifier) {
        this.messageClassifier = messageClassifier;
    }

    public void setHandlerMap(Map<String, MessageQueueHandler> handlerMap) {
        this.handlerMap = handlerMap;
    }

    public void setMessageProcessor(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }
}
