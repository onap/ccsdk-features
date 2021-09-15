/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt mountpoint-registrar
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl;

import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.onap.dmaap.mr.client.MRClientFactory;
import org.onap.dmaap.mr.client.MRConsumer;
import org.onap.dmaap.mr.client.response.MRConsumerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DMaaPVESMsgConsumerImpl implements DMaaPVESMsgConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(DMaaPVESMsgConsumerImpl.class);
    private static final String DEFAULT_SDNRUSER = "admin";
    private static final String DEFAULT_SDNRPASSWD = "admin";

    private final String name = this.getClass().getSimpleName();
    private Properties properties = null;
    private MRConsumer consumer = null;
    private boolean running = false;
    private boolean ready = false;
    private int fetchPause = 5000; // Default pause between fetch - 5 seconds
    private int timeout = 15000; // Default timeout - 15 seconds
    protected final GeneralConfig generalConfig;

    protected DMaaPVESMsgConsumerImpl(GeneralConfig generalConfig) {
        this.generalConfig = generalConfig;
    }

    /*
     * Thread to fetch messages from the DMaaP topic. Waits for the messages to arrive on the topic until a certain timeout and returns.
     * If no data arrives on the topic, sleeps for a certain time period before checking again
     */
    @Override
    public void run() {

        if (ready) {
            running = true;
            while (running) {
                try {
                    boolean noData = true;
                    MRConsumerResponse consumerResponse = null;
                    consumerResponse = consumer.fetchWithReturnConsumerResponse(timeout, -1);
                    for (String msg : consumerResponse.getActualMessages()) {
                        noData = false;
                        LOG.debug("{} received ActualMessage from DMaaP VES Message topic {}", name,msg);
                        processMsg(msg);
                    }

                    if (noData) {
                        LOG.debug("{} received ResponseCode: {}", name, consumerResponse.getResponseCode());
                        LOG.debug("{} received ResponseMessage: {}", name, consumerResponse.getResponseMessage());
                        if ((consumerResponse.getResponseCode() == null)
                                && (consumerResponse.getResponseMessage().contains("SocketTimeoutException"))) {
                            LOG.warn("Client timeout while waiting for response from Server {}",
                                    consumerResponse.getResponseMessage());
                        }
                        pauseThread();
                    }
                } catch (Exception e) {
                    LOG.error("Caught exception reading from DMaaP VES Message Topic", e);
                    running = false;
                }
            }
        }
    }

    protected JsonNode convertMessageToJsonNode(String message) throws InvalidMessageException {
        try {
            return new ObjectMapper().readTree(message);
        } catch (JsonProcessingException e) {
            LOG.warn("Cannot convert message to JsonNode");
            throw new InvalidMessageException("Cannot convert message to JsonNode");
        }
    }

    protected abstract boolean areDetailedEventFieldsValid(JsonNode jsonNode) throws InvalidMessageException;

    protected boolean areBasicEventFieldsValid(JsonNode dmaapMessageRootNode) throws InvalidMessageException {
        List<String> fieldNamesToValid = List.of("domain", "reportingEntityName", "eventName", "eventId",
                "vesEventListenerVersion", "priority");
        try {
            JsonNode commonEventHeader = dmaapMessageRootNode.get("event").get("commonEventHeader").requireNonNull();
            fieldNamesToValid.forEach(checkIfFieldExistsInNode(commonEventHeader));
        } catch (NullPointerException e) {
            String info = e.getMessage() != null ? e.getMessage() : "One of required fields doesn't exist in the node";
            throw new InvalidMessageException(info);
        }
        return true;
    }

    protected Consumer<String> checkIfFieldExistsInNode(JsonNode jsonNode) throws NullPointerException {
        return fieldName -> {
            JsonNode fieldNode = jsonNode.get(fieldName).requireNonNull();
            JsonNodeType fieldNodeType = fieldNode.getNodeType();
            if (fieldNodeType.equals(JsonNodeType.STRING)) {
                if (fieldNode.textValue().isEmpty()) {
                    LOG.warn("Node '{}' has no value!", fieldName);
                    throw new NullPointerException(String.format("Node '%s' has no value!", fieldName));
                }
            } else if(fieldNodeType.equals(JsonNodeType.OBJECT) || fieldNodeType.equals(JsonNodeType.ARRAY)) {
                if (fieldNode.size() == 0) {
                    LOG.warn("Node '{}' has size == 0 thus no values inside!", fieldName);
                    throw new NullPointerException(
                            String.format("Node '%s' has size == 0 thus no values inside!", fieldName));
                }
            }
        };
    }


    /*
     * Create a consumer by specifying  properties containing information such as topic name, timeout, URL etc
     */
    @Override
    public void init(Properties properties) {

        try {

            String timeoutStr = properties.getProperty("timeout");
            LOG.debug("timeoutStr: {}", timeoutStr);

            if ((timeoutStr != null) && (timeoutStr.length() > 0)) {
                timeout = parseTimeOutValue(timeoutStr);
            }

            String fetchPauseStr = properties.getProperty("fetchPause");
            LOG.debug("fetchPause(Str): {}",fetchPauseStr);
            if ((fetchPauseStr != null) && (fetchPauseStr.length() > 0)) {
                fetchPause = parseFetchPause(fetchPauseStr);
            }
            LOG.debug("fetchPause: {} ",fetchPause);

            this.consumer = MRClientFactory.createConsumer(properties);
            ready = true;
        } catch (Exception e) {
            LOG.error("Error initializing DMaaP VES Message consumer from file {} {}",properties, e);
        }
    }

    private int parseTimeOutValue(String timeoutStr) {
        try {
            return Integer.parseInt(timeoutStr);
        } catch (NumberFormatException e) {
            LOG.error("Non-numeric value specified for timeout ({})",timeoutStr);
        }
        return timeout;
    }

    private int parseFetchPause(String fetchPauseStr) {
        try {
            return Integer.parseInt(fetchPauseStr);
        } catch (NumberFormatException e) {
            LOG.error("Non-numeric value specified for fetchPause ({})",fetchPauseStr);
        }
        return fetchPause;
    }

    private void pauseThread() throws InterruptedException {
        if (fetchPause > 0) {
            LOG.debug("No data received from fetch.  Pausing {} ms before retry", fetchPause);
            Thread.sleep(fetchPause);
        } else {
            LOG.debug("No data received from fetch.  No fetch pause specified - retrying immediately");
        }
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public String getProperty(String name) {
        return properties.getProperty(name, "");
    }

    @Override
    public void stopConsumer() {
        running = false;
    }


    public String getBaseUrl() {
        return generalConfig.getBaseUrl();
    }

    public String getSDNRUser() {
        return generalConfig.getSDNRUser() != null ? generalConfig.getSDNRUser() : DEFAULT_SDNRUSER;
    }

    public String getSDNRPasswd() {
        return generalConfig.getSDNRPasswd() != null ? generalConfig.getSDNRPasswd() : DEFAULT_SDNRPASSWD;
    }
}
