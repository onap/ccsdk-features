/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
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

package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.concurrent.GuardedBy;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.DeviceMonitor;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler.ODLEventListenerHandler;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeConnectListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev221225.ConnectionOper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev221225.ConnectionOper.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceManagerNetconfConnectHandler extends DeviceManagerNetconfNotConnectHandler
        implements NetconfNodeConnectListener {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerNetconfConnectHandler.class);

    private final Object networkelementLock;
    /** Contains all connected devices */
    @GuardedBy("networkelementLock")
    private final ConcurrentHashMap<String, NetworkElement> connectedNetworkElementRepresentations;

    private final @NonNull ListenerRegistration<DeviceManagerNetconfConnectHandler> registerNetconfNodeConnectListener;

    public DeviceManagerNetconfConnectHandler(@NonNull NetconfNodeStateService netconfNodeStateService,
            @NonNull ClusterSingletonServiceProvider clusterSingletonServiceProvider,
            @NonNull ODLEventListenerHandler odlEventListenerHandler, @NonNull DeviceMonitor deviceMonitor,
            @NonNull DeviceManagerServiceProvider serviceProvider, @NonNull List<NetworkElementFactory> factoryList) {

        super(netconfNodeStateService, clusterSingletonServiceProvider, odlEventListenerHandler, deviceMonitor,
                serviceProvider, factoryList);

        this.networkelementLock = new Object();
        this.connectedNetworkElementRepresentations = new ConcurrentHashMap<>();

        this.registerNetconfNodeConnectListener = netconfNodeStateService.registerNetconfNodeConnectListener(this);
    }

    @Override
    public void onEnterConnected(@NonNull NetconfAccessor acessor) {
        //@NonNull NodeId nNodeId, @NonNull NetconfNode netconfNode,
        //@NonNull MountPoint mountPoint, @NonNull DataBroker netconfNodeDataBroker
        String mountPointNodeName = acessor.getNodeId().getValue();
        LOG.debug("onEnterConnected - starting Event listener on Netconf for mountpoint {}", mountPointNodeName);

        LOG.debug("Master mountpoint {}", mountPointNodeName);

        // It is master for mountpoint and all data are available.
        // Make sure that specific mountPointNodeName is handled only once.
        // be aware that startListenerOnNodeForConnectedState could be called multiple
        // times for same mountPointNodeName.
        // networkElementRepresentations contains handled NEs at master node.

        if (isInNetworkElementRepresentations(mountPointNodeName)) {
            LOG.warn("Mountpoint {} already registered. Leave startup procedure.", mountPointNodeName);
            return;
        }
        // update db with connect status
        NetconfNode netconfNode = acessor.getNetconfNode();
        sendUpdateNotification(acessor.getNodeId(), netconfNode.getConnectionStatus(), netconfNode);

        for (NetworkElementFactory f : getFactoryList()) {
            Optional<NetworkElement> optionalNe = f.create(acessor, getServiceProvider());
            if (optionalNe.isPresent()) {
                // sendUpdateNotification(mountPointNodeName, nNode.getConnectionStatus(), nNode);
                handleNeStartup(acessor.getNodeId(), optionalNe.get());
                break; // Use the first provided
            }
        }
    }

    @Override
    public void onLeaveConnected(@NonNull NodeId nNodeId, @NonNull Optional<NetconfNode> optionalNetconfNode) {

        LOG.debug("onLeaveConnected {}", nNodeId);
        String mountPointNodeName = nNodeId.getValue();

        if (optionalNetconfNode.isPresent()) {
            NetconfNode nNode = optionalNetconfNode.get();
            ConnectionStatus csts = nNode.getConnectionStatus();
            sendUpdateNotification(nNodeId, csts, nNode);
        }

        // Handling if mountpoint exist. connected -> connecting/UnableToConnect
        stopListenerOnNodeForConnectedState(mountPointNodeName);
        if (isDeviceMonitorEnabled()) {
            getDeviceMonitor().deviceDisconnectIndication(mountPointNodeName);
        }
    }

    @Override
    public void close() {
        if (Objects.nonNull(registerNetconfNodeConnectListener)) {
            registerNetconfNodeConnectListener.close();
        }
        super.close();
    }

    public @Nullable NetworkElement getConnectedNeByMountpoint(String mountpoint) {
        return this.connectedNetworkElementRepresentations.get(mountpoint);
    }

    /*--------------------------------------------
     * Private functions
     */

    /**
     * Do all tasks necessary to move from mountpoint state connected -> connecting
     *
     * @param mountPointNodeName provided
     */
    private void stopListenerOnNodeForConnectedState(String mountPointNodeName) {
        NetworkElement ne = connectedNetworkElementRepresentations.remove(mountPointNodeName);
        if (ne != null) {
            ne.deregister();
        }
    }

    private boolean isInNetworkElementRepresentations(String mountPointNodeName) {
        synchronized (networkelementLock) {
            return connectedNetworkElementRepresentations.contains(mountPointNodeName);
        }
    }


    private void handleNeStartup(NodeId nodeId, NetworkElement inNe) {

        LOG.debug("NE Management for {} with {}", nodeId.getValue(), inNe.getClass().getName());
        NetworkElement result;
        synchronized (networkelementLock) {
            result = connectedNetworkElementRepresentations.put(nodeId.getValue(), inNe);
        }
        if (result != null) {
            LOG.warn("NE list was not empty as expected, but contained {} ", result.getNodeId());
        } else {
            LOG.debug("refresh necon entry for {} with type {}", nodeId.getValue(), inNe.getDeviceType());
            if (isOdlEventListenerHandlerEnabled()) {
                getOdlEventListenerHandler().connectIndication(nodeId, inNe.getDeviceType());
            }
        }
        if (isDeviceMonitorEnabled()) {
            getDeviceMonitor().deviceConnectMasterIndication(nodeId.getValue(), inNe);
        }

        inNe.register();
    }

    private void sendUpdateNotification(NodeId nodeId, ConnectionStatus csts, NetconfNode nNode) {
        LOG.debug("update ConnectedState for device :: Name : {} ConnectionStatus {}", nodeId.getValue(), csts);
        if (isOdlEventListenerHandlerEnabled()) {
            getOdlEventListenerHandler().updateRegistration(nodeId, ConnectionOper.ConnectionStatus.class.getSimpleName(),
                    csts != null ? csts.getName() : "null", nNode);
        }
    }

    @Override
    public void onCreated(NodeId nNodeId, NetconfNode netconfNode) {

    }

    @Override
    public void onStateChange(NodeId nNodeId, NetconfNode netconfNode) {

    }
}
