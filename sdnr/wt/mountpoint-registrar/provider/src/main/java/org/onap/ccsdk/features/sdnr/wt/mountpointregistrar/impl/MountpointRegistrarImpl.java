/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.FaultConfig;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.GeneralConfig;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.MessageConfig;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.PNFRegistrationConfig;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.ProvisioningConfig;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.StndDefinedFaultConfig;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.StrimziKafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MountpointRegistrarImpl implements AutoCloseable, IConfigChangedListener {

    private static final Logger LOG = LoggerFactory.getLogger(MountpointRegistrarImpl.class);
    private static final String APPLICATION_NAME = "mountpoint-registrar";
    private static final String CONFIGURATIONFILE = "etc/mountpoint-registrar.properties";

    private Thread sKafkaVESMsgConsumerMain = null;

    private GeneralConfig generalConfig;
    private boolean strimziEnabled = false;
    private Map<String, MessageConfig> configMap = new HashMap<>();
    private StrimziKafkaVESMsgConsumerMain sKafkaConsumerMain = null;
    private StrimziKafkaConfig strimziKafkaConfig;

    // Blueprint 1
    public MountpointRegistrarImpl() {
        LOG.info("Creating provider class for {}", APPLICATION_NAME);
    }

    public void init() {
        LOG.info("Init call for {}", APPLICATION_NAME);

        ConfigurationFileRepresentation configFileRepresentation =
                new ConfigurationFileRepresentation(CONFIGURATIONFILE);
        configFileRepresentation.registerConfigChangedListener(this);

        generalConfig = new GeneralConfig(configFileRepresentation);
        strimziKafkaConfig = new StrimziKafkaConfig(configFileRepresentation);
        PNFRegistrationConfig pnfRegConfig = new PNFRegistrationConfig(configFileRepresentation);
        FaultConfig faultConfig = new FaultConfig(configFileRepresentation);
        ProvisioningConfig provisioningConfig = new ProvisioningConfig(configFileRepresentation);
        StndDefinedFaultConfig stndFaultConfig = new StndDefinedFaultConfig(configFileRepresentation);

        configMap.put("pnfRegistration", pnfRegConfig);
        configMap.put("fault", faultConfig);
        configMap.put("provisioning", provisioningConfig);
        configMap.put("stndDefinedFault", stndFaultConfig);

        strimziEnabled = strimziKafkaConfig.getEnabled();
        if (strimziEnabled) { // start Kafka consumer thread only if strimziEnabled=true
            LOG.info("Strimzi Kafka seems to be enabled, starting consumer(s)");
            sKafkaConsumerMain = new StrimziKafkaVESMsgConsumerMain(configMap, generalConfig, strimziKafkaConfig);
            sKafkaVESMsgConsumerMain = new Thread(sKafkaConsumerMain);
            sKafkaVESMsgConsumerMain.start();
        } else {
            LOG.info("Strimzi Kafka seems to be disabled, not starting any consumer(s)");
        }
    }

    /**
     * Reflect status for Unit Tests
     *
     * @return Text with status
     */
    public String isInitializationOk() {
        return "No implemented";
    }

    @Override
    public void onConfigChanged() {
        if (generalConfig == null) { // Included as NullPointerException observed once in docker logs
            LOG.warn("onConfigChange cannot be handled. Unexpected Null");
            return;
        }
        LOG.info("Service configuration state changed. Enabled: {}", strimziKafkaConfig.getEnabled());
        boolean strimziEnabledNewVal = strimziKafkaConfig.getEnabled();
        if (!strimziEnabled && strimziEnabledNewVal) { // Strimzi kafka disabled earlier (or during bundle startup) but enabled later, start Consumer(s)
            LOG.info("Strimzi Kafka is enabled, starting consumer(s)");
            sKafkaConsumerMain = new StrimziKafkaVESMsgConsumerMain(configMap, generalConfig, strimziKafkaConfig);
            sKafkaVESMsgConsumerMain = new Thread(sKafkaConsumerMain);
            sKafkaVESMsgConsumerMain.start();
        } else if (strimziEnabled && !strimziEnabledNewVal) { // Strimzi kafka enabled earlier (or during bundle startup) but disabled later, stop consumer(s)
            LOG.info("Strimzi Kafka is disabled, stopping consumer(s)");
            List<StrimziKafkaVESMsgConsumer> consumers = sKafkaConsumerMain.getConsumers();
            for (StrimziKafkaVESMsgConsumer consumer : consumers) {
                // stop all consumers
                consumer.stopConsumer();
            }
        }
        strimziEnabled = strimziEnabledNewVal;
    }

    @Override
    public void close() throws Exception {
        LOG.info("{} closing ...", this.getClass().getName());
        LOG.info("{} closing done", APPLICATION_NAME);
    }

    /**
     * Used to close all Services, that should support AutoCloseable Pattern
     *
     * @param toClose
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private void close(AutoCloseable... toCloseList) throws Exception {
        for (AutoCloseable element : toCloseList) {
            if (element != null) {
                element.close();
            }
        }
    }
}
