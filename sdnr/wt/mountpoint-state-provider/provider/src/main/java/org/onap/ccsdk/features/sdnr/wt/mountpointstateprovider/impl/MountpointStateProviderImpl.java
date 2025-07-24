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

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NetconfNetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorConfigChangeListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeConnectListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.netconf.node.augment.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MountpointStateProviderImpl implements VESCollectorConfigChangeListener, NetconfNodeConnectListener,
        NetconfNodeStateListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MountpointStateProviderImpl.class);
    private static final String APPLICATION_NAME = "mountpoint-state-provider";

    private NetconfNodeStateService netconfNodeStateService;
    private NetconfNetworkElementService netconfNetworkElementService;
    private VESCollectorService vesCollectorService;
    private boolean vesCollectorEnabled = false; //Current value
    private MountpointStateVESMessageFormatter vesMessageFormatter;

    public MountpointStateProviderImpl() {
        LOG.info("Creating provider class for {}", APPLICATION_NAME);

    }

    public void setNetconfNodeStateService(NetconfNodeStateService netconfNodeStateService) {
        this.netconfNodeStateService = netconfNodeStateService;
    }

    public void setNetconfNetworkElementService(NetconfNetworkElementService netconfNetworkElementService) {
        this.netconfNetworkElementService = netconfNetworkElementService;
    }

    public void init() {
        LOG.info("Init call for {}", APPLICATION_NAME);
        this.vesCollectorService = netconfNetworkElementService.getServiceProvider().getVESCollectorService();
        this.vesCollectorEnabled = vesCollectorService.getConfig().isVESCollectorEnabled();
        this.vesMessageFormatter = new MountpointStateVESMessageFormatter(vesCollectorService);
        // regsiter for live configuration changes
        vesCollectorService.registerForChanges(this);
        // register for node changes
        netconfNodeStateService.registerNetconfNodeConnectListener(this);
        netconfNodeStateService.registerNetconfNodeStateListener(this);

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
    public void close() throws Exception {
        LOG.info("{} closing ...", this.getClass().getName());
        vesCollectorService.deregister(this);
        LOG.info("{} closing done", APPLICATION_NAME);
    }


    private void publishStateChange(String nodeId, String connectionStatus) {
        this.vesCollectorService.publishVESMessage(
                vesMessageFormatter.createVESMessage(nodeId, connectionStatus, java.time.Clock.systemUTC().instant()));
    }

    @Override
    public void notify(VESCollectorCfgService cfg) {
        boolean vesCollectorEnabledPV = cfg.isVESCollectorEnabled(); // Pending value a.k.a new value
        if (vesCollectorEnabledPV != vesCollectorEnabled) {
            this.vesCollectorEnabled = vesCollectorEnabledPV;
        }
    }

    @Override
    public void onEnterConnected(@NonNull NetconfAccessor accessor) {
        if (!this.vesCollectorEnabled) {
            return;
        }
        NodeId nNodeId = accessor.getNodeId();
        NetconfNode netconfNode = accessor.getNetconfNode();

        Ipv4Address ipv4Address = netconfNode.getHost().getIpAddress().getIpv4Address();
        Ipv6Address ipv6Address = netconfNode.getHost().getIpAddress().getIpv6Address();
        LOG.debug("In onEnterConnected of MountpointNodeConnectListenerImpl - nNodeId = {}, IP Address = {}",
                nNodeId.getValue()
                , ipv4Address != null ? ipv4Address.getValue() : ipv6Address.getValue());
        this.publishStateChange(nNodeId.getValue(), netconfNode.getConnectionStatus().toString());
    }

    @Override
    public void onLeaveConnected(NodeId nNodeId, Optional<NetconfNode> optionalNetconfNode) {
        if (!this.vesCollectorEnabled) {
            return;
        }
        LOG.debug("In onLeaveConnected of MountpointNodeConnectListenerImpl - nNodeId = {}", nNodeId);
        this.publishStateChange(nNodeId.getValue(), "Unmounted");
    }

    @Override
    public void onCreated(NodeId nNodeId, NetconfNode netconfNode) {
        if (!this.vesCollectorEnabled) {
            return;
        }
        Ipv4Address ipv4Address = netconfNode.getHost().getIpAddress().getIpv4Address();
        Ipv6Address ipv6Address = netconfNode.getHost().getIpAddress().getIpv6Address();
        LOG.debug("In onCreated of MountpointNodeStateListenerImpl - nNodeId = {}, IP Address = {}", nNodeId.getValue(),
                ipv4Address != null ? ipv4Address.getValue() : ipv6Address.getValue());
        this.publishStateChange(nNodeId.getValue(), netconfNode.getConnectionStatus().toString());


    }

    @Override
    public void onStateChange(NodeId nNodeId, NetconfNode netconfNode) {
        if (!this.vesCollectorEnabled) {
            return;
        }
        Ipv4Address ipv4Address = netconfNode.getHost().getIpAddress().getIpv4Address();
        Ipv6Address ipv6Address = netconfNode.getHost().getIpAddress().getIpv6Address();
        LOG.debug("In onStateChange of MountpointNodeStateListenerImpl - nNodeId = {}, IP Address = {}",
                nNodeId.getValue(),
                ipv4Address != null ? ipv4Address.getValue() : ipv6Address.getValue());
        this.publishStateChange(nNodeId.getValue(), netconfNode.getConnectionStatus().toString());


    }

    @Override
    public void onRemoved(NodeId nNodeId) {
        if (!this.vesCollectorEnabled) {
            return;
        }
        LOG.debug("In onRemoved of MountpointNodeStateListenerImpl - nNodeId = {}", nNodeId);
        this.publishStateChange(nNodeId.getValue(), "Removed");

    }

}
