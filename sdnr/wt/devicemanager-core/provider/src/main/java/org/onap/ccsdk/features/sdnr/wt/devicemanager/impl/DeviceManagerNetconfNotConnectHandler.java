/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl;

import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.HtAssert;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.DeviceMonitor;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler.ODLEventListenerHandler;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.OdlClusterSingleton;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceManagerNetconfNotConnectHandler implements NetconfNodeStateListener {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerNetconfNotConnectHandler.class);

    private final @NonNull ListenerRegistration<NetconfNodeStateListener> registerNetconfNodeStateListener;

    private final @NonNull ODLEventListenerHandler odlEventListenerHandler;
    private final @NonNull DeviceMonitor deviceMonitor;
    private final @NonNull List<NetworkElementFactory> factoryList;
    private final @NonNull DeviceManagerServiceProvider serviceProvider;


    private final boolean odlEventListenerHandlerEnabled;
    private final boolean deviceMonitorEnabled;

    private final OdlClusterSingleton singleton;

    public DeviceManagerNetconfNotConnectHandler(@NonNull NetconfNodeStateService netconfNodeStateService,
            @NonNull ClusterSingletonServiceProvider clusterSingletonServiceProvider,
            @NonNull ODLEventListenerHandler odlEventListenerHandler, @NonNull DeviceMonitor deviceMonitor,
            @NonNull DeviceManagerServiceProvider serviceProvider, @NonNull List<NetworkElementFactory> factoryList) {

        HtAssert.nonnull(netconfNodeStateService, this.odlEventListenerHandler = odlEventListenerHandler,
                this.deviceMonitor = deviceMonitor, this.serviceProvider = serviceProvider,
                this.factoryList = factoryList, odlEventListenerHandler);

        /* Used for debug purpose */
        this.odlEventListenerHandlerEnabled = true;
        this.deviceMonitorEnabled = false;

        this.singleton = new OdlClusterSingleton(clusterSingletonServiceProvider);
        this.registerNetconfNodeStateListener = netconfNodeStateService.registerNetconfNodeStateListener(this);

    }

    @Override
    public void onCreated(NodeId nNodeId, NetconfNode netconfNode) {
        LOG.info("onCreated {}", nNodeId);
        if (isOdlEventListenerHandlerMaster()) {
            odlEventListenerHandler.registration(nNodeId, netconfNode);
        }
        if (deviceMonitorEnabled) {
            deviceMonitor.deviceDisconnectIndication(nNodeId.getValue());
        }
    }

    @Override
    public void onStateChange(NodeId nNodeId, NetconfNode netconfNode) {
        LOG.info("onStateChange {}", nNodeId);
        if (isOdlEventListenerHandlerMaster()) {
            odlEventListenerHandler.onStateChangeIndication(nNodeId, netconfNode);
        }
    }

    @Override
    public void onRemoved(NodeId nNodeId) {
        LOG.info("mountpointNodeRemoved {}", nNodeId.getValue());

        if (deviceMonitorEnabled) {
            deviceMonitor.removeMountpointIndication(nNodeId.getValue());
        }
        if (isOdlEventListenerHandlerMaster()) {
            odlEventListenerHandler.deRegistration(nNodeId); //Additional indication for log
        }
    }

    @Override
    public void close() {
        if (Objects.nonNull(registerNetconfNodeStateListener)) {
            registerNetconfNodeStateListener.close();
        }
    }

    /*--------------------------------------------
     * Private functions
     */

    private boolean isOdlEventListenerHandlerMaster() {
        return odlEventListenerHandlerEnabled && singleton.isMaster();
    }

    protected @NonNull DeviceManagerServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    protected @NonNull List<NetworkElementFactory> getFactoryList() {
        return factoryList;
    }


    protected boolean isDeviceMonitorEnabled() {
        return deviceMonitorEnabled;
    }

    protected @NonNull DeviceMonitor getDeviceMonitor() {
        return deviceMonitor;
    }

    protected boolean isOdlEventListenerHandlerEnabled() {
        return odlEventListenerHandlerEnabled;
    }

    protected @NonNull ODLEventListenerHandler getOdlEventListenerHandler() {
        return odlEventListenerHandler;
    }

}
