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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.HtAssert;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.DeviceMonitor;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler.ODLEventListenerHandler;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeConnectListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceManagerNetconfConnectHandler implements NetconfNodeConnectListener, NetconfNodeStateListener {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerNetconfConnectHandler.class);

    private final @NonNull ListenerRegistration<DeviceManagerNetconfConnectHandler> registerNetconfNodeConnectListener;
    private final @NonNull ListenerRegistration<NetconfNodeStateListener> registerNetconfNodeStateListener;

    private final @NonNull ODLEventListenerHandler odlEventListenerHandler;
    private final @NonNull DeviceMonitor deviceMonitor;
    private final @NonNull List<NetworkElementFactory> factoryList;
    private final @NonNull DeviceManagerServiceProvider serviceProvider;

    private final Object networkelementLock;
    private final ConcurrentHashMap<String, NetworkElement> networkElementRepresentations;

    public DeviceManagerNetconfConnectHandler(@NonNull NetconfNodeStateService netconfNodeStateService,
            @NonNull ODLEventListenerHandler odlEventListenerHandler, @NonNull DeviceMonitor deviceMonitor,
            @NonNull DeviceManagerServiceProvider serviceProvider, @NonNull List<NetworkElementFactory> factoryList) {

        HtAssert.nonnull(netconfNodeStateService, this.odlEventListenerHandler = odlEventListenerHandler,
                this.deviceMonitor = deviceMonitor, this.serviceProvider = serviceProvider,
                this.factoryList = factoryList);

        this.networkelementLock = new Object();
        this.networkElementRepresentations = new ConcurrentHashMap<>();

        this.registerNetconfNodeConnectListener = netconfNodeStateService.registerNetconfNodeConnectListener(this);
        this.registerNetconfNodeStateListener = netconfNodeStateService.registerNetconfNodeStateListener(this);
    }

    @Override
    public void onEnterConnected(@NonNull NetconfAccessor acessor) {
        //@NonNull NodeId nNodeId, @NonNull NetconfNode netconfNode,
        //@NonNull MountPoint mountPoint, @NonNull DataBroker netconfNodeDataBroker
        String mountPointNodeName = acessor.getNodeId().getValue();
        LOG.info("onEnterConnected - starting Event listener on Netconf for mountpoint {}", mountPointNodeName);

        LOG.info("Master mountpoint {}", mountPointNodeName);

        // It is master for mountpoint and all data are available.
        // Make sure that specific mountPointNodeName is handled only once.
        // be aware that startListenerOnNodeForConnectedState could be called multiple
        // times for same mountPointNodeName.
        // networkElementRepresentations contains handled NEs at master node.

        synchronized (networkelementLock) {
            if (networkElementRepresentations.containsKey(mountPointNodeName)) {
                LOG.warn("Mountpoint {} already registered. Leave startup procedure.", mountPointNodeName);
                return;
            }
        }
        // update db with connect status
        NetconfNode netconfNode = acessor.getNetconfNode();
        sendUpdateNotification(mountPointNodeName, netconfNode.getConnectionStatus(), netconfNode);

        for (NetworkElementFactory f : factoryList) {
            Optional<NetworkElement> optionalNe = f.create(acessor, serviceProvider);
            if (optionalNe.isPresent()) {
                // sendUpdateNotification(mountPointNodeName, nNode.getConnectionStatus(), nNode);
                NetworkElement inNe = optionalNe.get();
                LOG.info("NE Management for {} with {}", mountPointNodeName, inNe.getClass().getName());
                putToNetworkElementRepresentations(mountPointNodeName, inNe);
                deviceMonitor.deviceConnectMasterIndication(mountPointNodeName, inNe);

                inNe.register();
                break; // Use the first provided
            }
        }
    }

    @Override
    public void onLeaveConnected(@NonNull NodeId nNodeId, @NonNull Optional<NetconfNode> optionalNetconfNode) {

        LOG.info("onLeaveConnected {}", nNodeId);
        String mountPointNodeName = nNodeId.getValue();

        if (optionalNetconfNode.isPresent()) {
            NetconfNode nNode = optionalNetconfNode.get();
            ConnectionStatus csts = nNode.getConnectionStatus();
            sendUpdateNotification(mountPointNodeName, csts, nNode);
        }

        // Handling if mountpoint exist. connected -> connecting/UnableToConnect
        stopListenerOnNodeForConnectedState(mountPointNodeName);
        deviceMonitor.deviceDisconnectIndication(mountPointNodeName);
    }

    @Override
    public void onCreated(NodeId nNodeId, NetconfNode netconfNode) {
        LOG.info("onCreated {}", nNodeId);
        odlEventListenerHandler.mountpointCreatedIndication(nNodeId.getValue(), netconfNode);

    }

    @Override
    public void onStateChange(NodeId nNodeId, NetconfNode netconfNode) {
        LOG.info("onStateChange {}", nNodeId);
        odlEventListenerHandler.onStateChangeIndication(nNodeId.getValue(), netconfNode);
    }

    @Override
    public void onRemoved(NodeId nNodeId) {
        String mountPointNodeName = nNodeId.getValue();
        LOG.info("mountpointNodeRemoved {}", nNodeId.getValue());

        stopListenerOnNodeForConnectedState(mountPointNodeName);
        deviceMonitor.removeMountpointIndication(mountPointNodeName);
        odlEventListenerHandler.deRegistration(mountPointNodeName); //Additional indication for log
    }

    @Override
    public void close() {
        if (registerNetconfNodeConnectListener != null) {
            registerNetconfNodeConnectListener.close();
        }
        if (registerNetconfNodeStateListener != null) {
            registerNetconfNodeStateListener.close();
        }
    }

    /*--------------------------------------------
     * Private functions
     */

    /**
     * Do all tasks necessary to move from mountpoint state connected -> connecting
     * 
     * @param mountPointNodeName provided
     * @param ne representing the device connected to mountpoint
     */
    private void stopListenerOnNodeForConnectedState(String mountPointNodeName) {
        NetworkElement ne = networkElementRepresentations.remove(mountPointNodeName);
        if (ne != null) {
            ne.deregister();
        }
    }

    private void putToNetworkElementRepresentations(String mountPointNodeName, NetworkElement ne) {
        NetworkElement result;
        synchronized (networkelementLock) {
            result = networkElementRepresentations.put(mountPointNodeName, ne);
        }
        if (result != null) {
            LOG.warn("NE list was not empty as expected, but contained {} ", result.getNodeId());
        } else {
            LOG.debug("refresh necon entry for {} with type {}", mountPointNodeName, ne.getDeviceType());
            odlEventListenerHandler.connectIndication(mountPointNodeName, ne.getDeviceType());
        }
    }

    private void sendUpdateNotification(String mountPointNodeName, ConnectionStatus csts, NetconfNode nNode) {
        LOG.info("update ConnectedState for device :: Name : {} ConnectionStatus {}", mountPointNodeName, csts);
        odlEventListenerHandler.updateRegistration(mountPointNodeName, ConnectionStatus.class.getSimpleName(),
                csts != null ? csts.getName() : "null", nNode);
    }

}
