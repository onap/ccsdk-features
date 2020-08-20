/*
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
 */

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
    private static final String _PNFREG_CLASS =
            "org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.DMaaPPNFRegVESMsgConsumer";
    private static final String _FAULT_CLASS =
            "org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.DMaaPFaultVESMsgConsumer";
    private static final String _PNFREG_DOMAIN = "pnfRegistration";
    private static final String _FAULT_DOMAIN = "fault";

    boolean threadsRunning = false;
    List<DMaaPVESMsgConsumer> consumers = new LinkedList<>();
    private PNFRegistrationConfig pnfRegistrationConfig;
    private FaultConfig faultConfig;
    private GeneralConfig generalConfig;

    public DMaaPVESMsgConsumerMain(Map<String, Configuration> configMap, GeneralConfig generalConfig) {
        this.generalConfig = generalConfig;
        configMap.forEach((k, v) -> initialize(k, v));
    }

    public void initialize(String domain, Configuration domainConfig) {
        LOG.debug("In initialize method : Domain = {} and domainConfig = {}", domain, domainConfig);
        String consumerClass = null;
        Properties consumerProperties = new Properties();
        if (domain.equalsIgnoreCase(_PNFREG_DOMAIN)) {
            this.pnfRegistrationConfig = (PNFRegistrationConfig) domainConfig;

            consumerClass = _PNFREG_CLASS;
            LOG.debug("Consumer class = {}",consumerClass);

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
            threadsRunning = createConsumer(_PNFREG_DOMAIN, consumerProperties);
        } else if (domain.equalsIgnoreCase(_FAULT_DOMAIN)) {
            this.faultConfig = (FaultConfig) domainConfig;
            consumerClass = _FAULT_CLASS;
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
            threadsRunning = createConsumer(_FAULT_DOMAIN, consumerProperties);
        }
    }

    private boolean updateThreadState(List<DMaaPVESMsgConsumer> consumers) {
        boolean threadsRunning = false;
        for (DMaaPVESMsgConsumer consumer : consumers) {
            if (consumer.isRunning()) {
                threadsRunning = true;
            }
        }
        return threadsRunning;
    }

    public boolean createConsumer(String consumerType, Properties properties) {
        DMaaPVESMsgConsumerImpl consumer = null;

        if (consumerType.equalsIgnoreCase(_PNFREG_DOMAIN))
            consumer = new DMaaPPNFRegVESMsgConsumer(generalConfig);
        else if (consumerType.equalsIgnoreCase(_FAULT_DOMAIN))
            consumer = new DMaaPFaultVESMsgConsumer(generalConfig);

        handleConsumer(consumer, properties, consumers);
        return !consumers.isEmpty();
    }

    private boolean handleConsumer(DMaaPVESMsgConsumer consumer, Properties properties,
            List<DMaaPVESMsgConsumer> consumers) {
        if (consumer != null) {
            consumer.init(properties);

            if (consumer.isReady()) {
                Thread consumerThread = new Thread(consumer);
                consumerThread.start();
                consumers.add(consumer);

                LOG.info("Started consumer thread ({} : {})", consumer.getClass().getSimpleName(), properties);
                return true;
            } else {
                LOG.debug("Consumer {} is not ready", consumer.getClass().getSimpleName());
            }
        }
        return false;
    }

    @Override
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

    public List<DMaaPVESMsgConsumer> getConsumers() {
        return consumers;
    }

}
