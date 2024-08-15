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

import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NetconfNetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorConfigChangeListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MountpointStateProviderImpl implements VESCollectorConfigChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MountpointStateProviderImpl.class);
    private static final String APPLICATION_NAME = "mountpoint-state-provider";

    private NetconfNodeStateService netconfNodeStateService;
    private NetconfNetworkElementService netconfNetworkElementService;

    private MountpointNodeConnectListenerImpl nodeConnectListener;
    private MountpointNodeStateListenerImpl nodeStateListener;
    private MountpointStatePublisher mountpointStatePublisher;
    private VESCollectorService vesCollectorService;
    private boolean vesCollectorEnabledCV = false; //Current value

    public MountpointStateProviderImpl() {
        LOG.info("Creating provider class for {}", APPLICATION_NAME);
        nodeConnectListener = null;
        nodeStateListener = null;
    }

    public void setNetconfNodeStateService(NetconfNodeStateService netconfNodeStateService) {
        this.netconfNodeStateService = netconfNodeStateService;
    }

    public void setNetconfNetworkElementService(NetconfNetworkElementService netconfNetworkElementService) {
        this.netconfNetworkElementService = netconfNetworkElementService;
    }

    public void init() {
        LOG.info("Init call for {}", APPLICATION_NAME);

        nodeConnectListener = new MountpointNodeConnectListenerImpl(netconfNodeStateService);
        nodeStateListener = new MountpointNodeStateListenerImpl(netconfNodeStateService);
        vesCollectorService = netconfNetworkElementService.getServiceProvider().getVESCollectorService();
        vesCollectorService.registerForChanges(this);
        boolean vesCollectorEnabled = vesCollectorService.getConfig().isVESCollectorEnabled();

        if (vesCollectorEnabled) {
            vesCollectorEnabledCV = true;
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

    public void startPublishing() {
        mountpointStatePublisher = new MountpointStatePublisher(
                netconfNetworkElementService.getServiceProvider().getVESCollectorService());
        Thread t = new Thread(mountpointStatePublisher);
        t.start();

        nodeConnectListener.start(mountpointStatePublisher);
        nodeStateListener.start(mountpointStatePublisher);
    }

    public void stopPublishing() throws Exception {
        mountpointStatePublisher.stop();
        close(nodeConnectListener, nodeStateListener);
    }

    @Override
    public void close() throws Exception {
        LOG.info("{} closing ...", this.getClass().getName());
        mountpointStatePublisher.stop();
        vesCollectorService.deregister(this);
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

    @Override
    public void notify(VESCollectorCfgService cfg) {
        boolean vesCollectorEnabledPV = cfg.isVESCollectorEnabled(); // Pending value a.k.a new value
        if (vesCollectorEnabledPV != vesCollectorEnabledCV) {
            vesCollectorEnabledCV = vesCollectorEnabledPV;
            if (vesCollectorEnabledPV) {
                startPublishing();
            } else {
                try {
                    stopPublishing();
                } catch (Exception e) {
                    LOG.debug("{}", e);
                }
            }
        }
    }

}
