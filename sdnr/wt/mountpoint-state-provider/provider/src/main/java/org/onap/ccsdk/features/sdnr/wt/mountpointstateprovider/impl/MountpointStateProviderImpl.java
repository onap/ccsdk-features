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

public class MountpointStateProviderImpl implements AutoCloseable, IConfigChangedListener {

    private static final Logger LOG = LoggerFactory.getLogger(MountpointStateProviderImpl.class);
    private static final String APPLICATION_NAME = "mountpoint-state-provider";
    private static final String CONFIGURATIONFILE = "etc/mountpoint-state-provider.properties";

    private NetconfNodeStateService netconfNodeStateService;
    private GeneralConfig generalConfig;
    private boolean dmaapEnabled = false;

    private MountpointNodeConnectListenerImpl nodeConnectListener;
    private MountpointNodeStateListenerImpl nodeStateListener;
    private MountpointStatePublisherMain mountpointStatePublisher;

    public MountpointStateProviderImpl() {
        LOG.info("Creating provider class for {}", APPLICATION_NAME);
        nodeConnectListener = null;
        nodeStateListener = null;
    }

    public void setNetconfNodeStateService(NetconfNodeStateService netconfNodeStateService) {
        this.netconfNodeStateService = netconfNodeStateService;
    }

    public void init() {
        LOG.info("Init call for {}", APPLICATION_NAME);
        ConfigurationFileRepresentation configFileRepresentation =
                new ConfigurationFileRepresentation(CONFIGURATIONFILE);
        configFileRepresentation.registerConfigChangedListener(this);

        nodeConnectListener = new MountpointNodeConnectListenerImpl(netconfNodeStateService);
        nodeStateListener = new MountpointNodeStateListenerImpl(netconfNodeStateService);

        generalConfig = new GeneralConfig(configFileRepresentation);
        if (generalConfig.getEnabled()) { //dmaapEnabled
            startPublishing();
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

        // DMaap disabled earlier (or during bundle startup) but enabled later, start publisher(s)
        if (!dmaapEnabled && dmaapEnabledNewVal) {
            LOG.info("DMaaP is enabled, starting Publisher");
            startPublishing();
        } else if (dmaapEnabled && !dmaapEnabledNewVal) {
            // DMaap enabled earlier (or during bundle startup) but disabled later, stop publisher(s)
            LOG.info("DMaaP is disabled, stop publisher");
            stopPublishing();
        }
        dmaapEnabled = dmaapEnabledNewVal;
    }

    public void startPublishing() {
        mountpointStatePublisher = new MountpointStatePublisherMain(generalConfig);
        mountpointStatePublisher.start();

        nodeConnectListener.start(mountpointStatePublisher);
        nodeStateListener.start(mountpointStatePublisher);
    }

    public void stopPublishing() {
        try {
            nodeConnectListener.stop();
            nodeStateListener.stop();
            mountpointStatePublisher.stop();
        } catch (Exception e) {
            LOG.error("Exception while stopping publisher ", e);
        }
    }

    @Override
    public void close() throws Exception {
        LOG.info("{} closing ...", this.getClass().getName());
        try {
            mountpointStatePublisher.stop();
        } catch (IOException | InterruptedException e) {
            LOG.error("Exception while stopping publisher ", e);
        }
        close(nodeConnectListener, nodeStateListener);
        LOG.info("{} closing done", APPLICATION_NAME);
    }

    /**
     * Used to close all Services, that should support AutoCloseable Pattern
     *
     * @param toClose
     * @throws Exception
     */
    private void close(AutoCloseable... toCloseList) throws Exception {
        for (AutoCloseable element : toCloseList) {
            if (element != null) {
                element.close();
            }
        }
    }
}
