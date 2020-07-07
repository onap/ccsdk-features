/*******************************************************************************
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt mountpoint-registrar
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
 ******************************************************************************/

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMaaPVESMsgConsumerMain implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(DMaaPVESMsgConsumerMain.class);

    boolean threadsRunning = false;
    static List<DMaaPVESMsgConsumer> consumers = new LinkedList<>();
    public GeneralConfig config;
    public PNFRegistrationConfig pnfRegistrationConfig;
    public FaultConfig faultConfig;

    public DMaaPVESMsgConsumerMain(Map<String, Configuration> configMap) {
        configMap.forEach((k, v) -> initialize(k, v));
    }

    public void initialize(String domain, Configuration domainConfig) {
        LOG.debug("In initialize method : Domain = {} and domainConfig = {}", domain, domainConfig);
        String consumerClass = null;
        Properties consumerProperties = new Properties();
        if (domain.equalsIgnoreCase("pnfregistration")) {
            this.pnfRegistrationConfig = (PNFRegistrationConfig) domainConfig;

            consumerClass = pnfRegistrationConfig.getConsumerClass();
            LOG.debug("Consumer class = " + consumerClass);

            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_TRANSPORTTYPE,
                    pnfRegistrationConfig.getTransportType());
            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_HOST_PORT,
                    pnfRegistrationConfig.getHostPort());
            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_CONTENTTYPE,
                    pnfRegistrationConfig.getContenttype());
            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_GROUP,
                    pnfRegistrationConfig.getConsumerGroup());
            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_ID,
                    pnfRegistrationConfig.getConsumerId());
            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_TOPIC, pnfRegistrationConfig.getTopic());
            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_TIMEOUT,
                    pnfRegistrationConfig.getTimeout());
            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_LIMIT, pnfRegistrationConfig.getLimit());
            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_FETCHPAUSE,
                    pnfRegistrationConfig.getFetchPause());
            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_PROTOCOL,
                    pnfRegistrationConfig.getProtocol());
            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_USERNAME,
                    pnfRegistrationConfig.getUsername());
            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_PASSWORD,
                    pnfRegistrationConfig.getPassword());
            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_CLIENT_READTIMEOUT,
                    pnfRegistrationConfig.getClientReadTimeout());
            consumerProperties.put(PNFRegistrationConfig.PROPERTY_KEY_CONSUMER_CLIENT_CONNECTTIMEOUT,
                    pnfRegistrationConfig.getClientConnectTimeout());
        } else if (domain.equalsIgnoreCase("fault")) {
            this.faultConfig = (FaultConfig) domainConfig;
            consumerClass = faultConfig.getConsumerClass();
            LOG.debug("Consumer class = {}", consumerClass);
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_TRANSPORTTYPE, faultConfig.getTransportType());
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_HOST_PORT, faultConfig.getHostPort());
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_CONTENTTYPE, faultConfig.getContenttype());
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_GROUP, faultConfig.getConsumerGroup());
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_ID, faultConfig.getConsumerId());
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_TOPIC, faultConfig.getTopic());
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_TIMEOUT, faultConfig.getTimeout());
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_LIMIT, faultConfig.getLimit());
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_FETCHPAUSE, faultConfig.getFetchPause());
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_PROTOCOL, faultConfig.getProtocol());
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_USERNAME, faultConfig.getUsername());
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_PASSWORD, faultConfig.getPassword());
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_CLIENT_READTIMEOUT,
                    faultConfig.getClientReadTimeout());
            consumerProperties.put(FaultConfig.PROPERTY_KEY_CONSUMER_CLIENT_CONNECTTIMEOUT,
                    faultConfig.getClientConnectTimeout());
        }

        if (consumerClass != null) {
            LOG.info("Calling createConsumer : {}", consumerClass);
            threadsRunning = createConsumer(consumerClass, consumerProperties);
        }

    }

    private static boolean updateThreadState(List<DMaaPVESMsgConsumer> consumers) {
        boolean threadsRunning = false;
        for (DMaaPVESMsgConsumer consumer : consumers) {
            if (consumer.isRunning()) {
                threadsRunning = true;
            }
        }
        return threadsRunning;
    }

    static boolean createConsumer(String consumerClassName, Properties properties) {
        Class<?> consumerClass = null;

        try {
            consumerClass = Class.forName(consumerClassName);
        } catch (Exception e) {
            LOG.error("Could not find DMaap VES Message consumer class {}", consumerClassName, e);
        }

        if (consumerClass != null) {
            LOG.debug("Calling handleConsumerClass");
            handleConsumerClass(consumerClass, consumerClassName, properties, consumers);
        }
        return !consumers.isEmpty();
    }

    private static boolean handleConsumerClass(Class<?> consumerClass, String consumerClassName, Properties properties,
            List<DMaaPVESMsgConsumer> consumers) {
        DMaaPVESMsgConsumer consumer = null;

        try {
            consumer = (DMaaPVESMsgConsumer) consumerClass.newInstance();
            LOG.debug("Successfully created an instance of consumerClass : {}", consumerClassName);
        } catch (Exception e) {
            LOG.error("Could not create consumer from class {}", consumerClassName, e);
        }

        if (consumer != null) {
            LOG.info("Initializing consumer {}({})", consumerClassName, properties);
            consumer.init(properties);

            if (consumer.isReady()) {
                Thread consumerThread = new Thread(consumer);
                consumerThread.start();
                consumers.add(consumer);

                LOG.info("Started consumer thread ({} : {})", consumerClassName, properties);
                return true;
            } else {
                LOG.debug("Consumer {} is not ready", consumerClassName);
            }
        }
        return false;
    }

    public void run() {
        while (threadsRunning) {
            threadsRunning = updateThreadState(consumers);
            if (!threadsRunning) {
                break;
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }

        LOG.info("No listener threads running - exiting");
    }

    public static List<DMaaPVESMsgConsumer> getConsumers() {
        return consumers;
    }

}
