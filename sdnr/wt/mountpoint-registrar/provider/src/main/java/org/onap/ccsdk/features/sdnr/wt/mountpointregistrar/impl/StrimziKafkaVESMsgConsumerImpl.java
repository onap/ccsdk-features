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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.Admin;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.GeneralConfig;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.kafka.VESMsgKafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StrimziKafkaVESMsgConsumerImpl
        implements StrimziKafkaVESMsgConsumer, StrimziKafkaVESMsgValidator {

    private static final Logger LOG = LoggerFactory.getLogger(StrimziKafkaVESMsgConsumerImpl.class);
    private static final String DEFAULT_SDNRUSER = "admin";
    private static final String DEFAULT_SDNRPASSWD = "admin";

    private final String name = this.getClass().getSimpleName();
    private VESMsgKafkaConsumer consumer = null;
    private boolean running = false;
    private boolean ready = false;
    private int fetchPause = 5000; // Default pause between fetch - 5 seconds
    protected final GeneralConfig generalConfig;
    Admin kafkaAdminClient = null;

    protected StrimziKafkaVESMsgConsumerImpl(GeneralConfig generalConfig, Admin kafkaAdminClient) {
        this.generalConfig = generalConfig;
        this.kafkaAdminClient = kafkaAdminClient;
    }

    /*
     * Thread to fetch messages from the Kafka topic. Waits for the messages to
     * arrive on the topic until a certain timeout and returns. If no data arrives
     * on the topic, sleeps for a certain time period before checking again
     */
    @Override
    public void run() {
        if (ready) {
            running = true;
            while (running) {
                try {
                    boolean noData = true;
                    List<String> consumerResponse = null;
                    if (isTopicExists(consumer.getTopicName())) {
                        consumerResponse = consumer.poll();
                        for (String msg : consumerResponse) {
                            noData = false;
                            LOG.debug("{} received ActualMessage from Kafka VES Message topic {}", name, msg);
                            if (isMessageValid(msg)) {
                                processMsg(msg);
                            }
                        }
                    }
                    if (noData) {
                        pauseThread();
                    }
                } catch (InterruptedException e) {
                    LOG.warn("Caught exception reading from Kafka Message Topic", e);
                    Thread.currentThread().interrupt();
                } catch (JsonProcessingException jsonProcessingException) {
                    LOG.warn("Failed to convert message to JsonNode: {}", jsonProcessingException.getMessage());
                } catch (InvalidMessageException invalidMessageException) {
                    LOG.warn("Message is invalid because of: {}", invalidMessageException.getMessage());
                } catch (Exception e) {
                    LOG.error("Caught exception reading from Kafka Message Topic", e);
                    running = false;
                }
            }
        }
    }

    @Override
    public boolean isMessageValid(String message) {
        return true;
    }

    protected JsonNode convertMessageToJsonNode(String message) throws JsonProcessingException {
        return new ObjectMapper().readTree(message);
    }

    /*
     * Create a Kafka consumer by specifying properties containing information such as
     * topic name, timeout, URL etc
     */
    @Override
    public void init(Properties strimziKafkaProperties, Properties consumerProperties) {

        try {
            this.consumer = new VESMsgKafkaConsumer(strimziKafkaProperties, consumerProperties);
            this.consumer.subscribe(consumerProperties.getProperty("topic"));
            ready = true;
        } catch (Exception e) {
            LOG.error("Error initializing Kafka Message consumer from file {} {}", consumerProperties, e);
        }
    }

    private void pauseThread() throws InterruptedException {
        if (fetchPause > 0) {
            LOG.debug("No data received from fetch.  Pausing {} ms before retry", fetchPause);
            Thread.sleep(fetchPause);
        } else {
            LOG.debug("No data received from fetch.  No fetch pause specified - retrying immediately");
        }
    }

    private boolean isTopicExists(String topicName) {
        LOG.trace("Checking for existence of topic - {}", topicName);
        try {
            for (String kafkaTopic : kafkaAdminClient.listTopics().names().get()) {
                if (kafkaTopic.equals(topicName))
                    return true;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Exception in isTopicExists method - ", e);
        }
        return false;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /*
     * public String getProperty(String name) { return properties.getProperty(name,
     * ""); }
     */
    @Override
    public void stopConsumer() {
        consumer.stop();
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
