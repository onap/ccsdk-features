/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Update Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=======================================================
 *
 */

package org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl;

import java.io.IOException;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class MountpointStateProviderImpl implements AutoCloseable, IConfigChangedListener {

    private static final Logger LOG = LoggerFactory.getLogger(MountpointStateProviderImpl.class);
    private static final String APPLICATION_NAME = "mountpoint-state-provider";
    private static final String CONFIGURATIONFILE = "etc/mountpoint-state-provider.properties";

    private NetconfNodeStateService netconfNodeStateService;

    private GeneralConfig generalConfig;
    private boolean dmaapEnabled = false;
    private Thread mountpointStatePublisher = null;

    MountpointNodeConnectListenerImpl nodeConnectListener = new MountpointNodeConnectListenerImpl();
    MountpointNodeStateListenerImpl nodeStateListener = new MountpointNodeStateListenerImpl();

    public MountpointStateProviderImpl() {
        LOG.info("Creating provider class for {}", APPLICATION_NAME);
    }

    public void setNetconfNodeStateService(NetconfNodeStateService netconfNodeStateService) {
        this.netconfNodeStateService = netconfNodeStateService;
    }

    public void init() {
        LOG.info("Init call for {}", APPLICATION_NAME);
        ConfigurationFileRepresentation configFileRepresentation =
                new ConfigurationFileRepresentation(CONFIGURATIONFILE);
        configFileRepresentation.registerConfigChangedListener(this);

        generalConfig = new GeneralConfig(configFileRepresentation);
        if (generalConfig.getEnabled()) { //dmaapEnabled
            mountpointStatePublisher = new Thread(new MountpointStatePublisher(generalConfig));
            mountpointStatePublisher.start();
            netconfNodeStateService.registerNetconfNodeConnectListener(nodeConnectListener);
            netconfNodeStateService.registerNetconfNodeStateListener(nodeStateListener);
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
        LOG.info("Service configuration state changed. Enabled: {}", generalConfig.getEnabled());
        boolean dmaapEnabledNewVal = generalConfig.getEnabled();

        // DMaap disabled earlier (or during bundle startup) but enabled later, start Consumer(s)
        if (!dmaapEnabled && dmaapEnabledNewVal) {
            LOG.info("DMaaP is enabled, starting Publisher");
            mountpointStatePublisher = new Thread(new MountpointStatePublisher(generalConfig));
            mountpointStatePublisher.start();
            netconfNodeStateService.registerNetconfNodeConnectListener(nodeConnectListener);
            netconfNodeStateService.registerNetconfNodeStateListener(nodeStateListener);
        } else if (dmaapEnabled && !dmaapEnabledNewVal) {
            // DMaap enabled earlier (or during bundle startup) but disabled later, stop consumer(s)
            LOG.info("DMaaP is disabled, stop publisher");
            try {
                MountpointStatePublisher.stopPublisher();
            } catch (IOException | InterruptedException e) {
                LOG.error("Exception while stopping publisher ", e);
            }
        }
        dmaapEnabled = dmaapEnabledNewVal;
    }

    @Override
    public void close() throws Exception {
        LOG.info("{} closing ...", this.getClass().getName());
        //close(updateService, configService, mwtnService); issue#1
        try {
            MountpointStatePublisher.stopPublisher();
        } catch (IOException | InterruptedException e) {
            LOG.error("Exception while stopping publisher ", e);
        }
        //close(updateService, mwtnService);
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
