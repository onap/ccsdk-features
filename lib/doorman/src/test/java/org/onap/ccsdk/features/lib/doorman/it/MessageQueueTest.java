package org.onap.ccsdk.features.lib.doorman.it;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.onap.ccsdk.features.lib.doorman.MessageClassifier;
import org.onap.ccsdk.features.lib.doorman.MessageInterceptor;
import org.onap.ccsdk.features.lib.doorman.MessageInterceptorFactory;
import org.onap.ccsdk.features.lib.doorman.MessageProcessor;
import org.onap.ccsdk.features.lib.doorman.MessageQueueHandler;
import org.onap.ccsdk.features.lib.doorman.dao.MessageDaoImpl;
import org.onap.ccsdk.features.lib.doorman.data.MessageData;
import org.onap.ccsdk.features.lib.doorman.data.Queue;
import org.onap.ccsdk.features.lib.doorman.impl.MessageInterceptorFactoryImpl;
import org.onap.ccsdk.features.lib.doorman.testutil.DbUtil;
import org.onap.ccsdk.features.lib.doorman.util.JsonUtil;
import org.onap.ccsdk.features.lib.rlock.LockHelperImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class MessageQueueTest {

    private static final Logger log = LoggerFactory.getLogger(MessageQueueTest.class);

    private String test;

    public MessageQueueTest(String test) {
        this.test = test;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws Exception {
        log.info("#########################################################################");
        log.info("MessageQueueTest: " + test + " started");

        String testSetupJson = readResource("it/" + test + "/test.json");
        Map<String, Object> testSetup = (Map<String, Object>) JsonUtil.jsonToData(testSetupJson);

        Map<String, MessageQueueHandler> handlerMap = new HashMap<>();

        for (Object o : (List<Object>) testSetup.get("handler_list")) {
            Map<String, Object> handlerSetup = (Map<String, Object>) o;
            String queueType = (String) handlerSetup.get("queue_type");
            String handlerClassName = (String) handlerSetup.get("handler_class");
            try {
                Class<?> handlerClass =
                        Class.forName("org.onap.ccsdk.features.lib.doorman.it." + test + "." + handlerClassName);
                MessageQueueHandler handler = (MessageQueueHandler) handlerClass.newInstance();
                handlerMap.put(queueType, handler);
                log.info("Handler found for queue type: " + queueType + ": " + handlerClass.getName());
            } catch (Exception e) {
            }
        }

        MessageInterceptorFactory factory =
                setupMessageInterceptorFactory(handlerMap, new Classifier(), new Processor());

        List<Object> requestList = (List<Object>) testSetup.get("request_list");

        List<Thread> threadList = new ArrayList<>();

        for (int i = 0; i < requestList.size(); i++) {
            Map<String, Object> requestSetup = (Map<String, Object>) requestList.get(i);

            Map<String, Object> requestParam = (Map<String, Object>) requestSetup.get("request_param");
            Map<String, Object> requestBodyData = (Map<String, Object>) requestSetup.get("request_body");
            String requestBody = null;
            if (requestBodyData != null) {
                requestBody = JsonUtil.dataToJson(requestBodyData);
            } else {
                requestBody = readResource("it/" + test + "/" + requestSetup.get("request_body_file"));
            }
            MessageData request = new MessageData(requestParam, requestBody);

            Map<String, Object> responseParam = (Map<String, Object>) requestSetup.get("response_param");
            Map<String, Object> responseBodyData = (Map<String, Object>) requestSetup.get("response_body");
            String responseBody = null;
            if (responseBodyData != null) {
                responseBody = JsonUtil.dataToJson(responseBodyData);
            } else {
                responseBody = readResource("it/" + test + "/" + requestSetup.get("response_body_file"));
            }
            MessageData response = new MessageData(responseParam, responseBody);

            long startTime = (Long) requestSetup.get("start_time");
            long processTime = (Long) requestSetup.get("process_time");

            MessageInterceptor interceptor = factory.create();

            Thread t = new Thread((Runnable) () -> {
                try {
                    Thread.sleep(startTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                MessageData r = interceptor.processRequest(request);

                if (r == null) {
                    try {
                        Thread.sleep(processTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    interceptor.processResponse(response);
                }

            }, "Message-" + i);

            threadList.add(t);
            t.start();
        }

        for (Thread t : threadList) {
            t.join();
        }

        log.info("MessageQueueTest: " + test + " completed");
        log.info("Result:");

        String testResultJson = readResource("it/" + test + "/result.json");
        Map<String, Object> testResult = (Map<String, Object>) JsonUtil.jsonToData(testResultJson);
        MessageQueueTestResult.checkResult(testResult);
    }

    private static class Classifier implements MessageClassifier {

        @Override
        public Queue determineQueue(MessageData request) {
            String queueType = (String) request.getParam().get("queue_type");
            String queueId = (String) request.getParam().get("queue_id");
            return new Queue(queueType, queueId);
        }

        @Override
        public String getExtMessageId(MessageData request) {
            return (String) request.getParam().get("request_id");
        }
    }

    private static class Processor implements MessageProcessor {

        @Override
        public void processMessage(MessageData request) {
            long processTime = (Long) request.getParam().get("process_time");
            try {
                Thread.sleep(processTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static MessageInterceptorFactory setupMessageInterceptorFactory(Map<String, MessageQueueHandler> handlerMap,
            MessageClassifier classifier, MessageProcessor processor) {
        LockHelperImpl lockHelper = new LockHelperImpl();
        lockHelper.setDataSource(DbUtil.getDataSource());
        lockHelper.setLockWait(5);
        lockHelper.setRetryCount(10);

        MessageDaoImpl messageDao = new MessageDaoImpl();
        messageDao.setDataSource(DbUtil.getDataSource());

        MessageInterceptorFactoryImpl f = new MessageInterceptorFactoryImpl();
        f.setMessageDao(messageDao);
        f.setLockHelper(lockHelper);
        f.setLockTimeout(20);
        f.setHandlerMap(handlerMap);
        f.setMessageClassifier(classifier);
        f.setMessageProcessor(processor);
        return f;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> allTests() {
        List<Object[]> ll = new ArrayList<>();

        String[] tcList = list("it");
        for (String tc : tcList) {
            ll.add(new Object[] {tc});
        }
        return ll;
    }

    private static String[] list(String dir) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource(dir);
            String path = url.getPath();
            return new File(path).list();
        } catch (Exception e) {
            log.warn("Error getting directory list for: " + dir + ": " + e.getMessage(), e);
            return new String[0];
        }
    }

    private static String readResource(String resource) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream ins = loader.getResourceAsStream(resource);
            BufferedReader in = new BufferedReader(new InputStreamReader(ins));
            StringBuilder ss = new StringBuilder();
            String line = in.readLine();
            while (line != null) {
                ss.append(line).append('\n');
                line = in.readLine();
            }
            in.close();
            return ss.toString();
        } catch (Exception e) {
            log.warn("Error reading resource: " + resource + ": " + e.getMessage(), e);
            return null;
        }
    }
}
