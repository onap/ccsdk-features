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

package org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl;

import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MountpointNodeStateListenerImpl implements NetconfNodeStateListener, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(MountpointNodeStateListenerImpl.class);
    private NetconfNodeStateService netconfNodeStateService;
    private MountpointStatePublisher mountpointStatePublisher;
    private ListenerRegistration<MountpointNodeStateListenerImpl> registeredNodeStateListener;

    public MountpointNodeStateListenerImpl(NetconfNodeStateService netconfNodeStateService) {
        this.netconfNodeStateService = netconfNodeStateService;
    }

    public void start(MountpointStatePublisher mountpointStatePublisher) {
        this.mountpointStatePublisher = mountpointStatePublisher;
        registeredNodeStateListener = netconfNodeStateService.registerNetconfNodeStateListener(this);
    }

    @Override
    public void onCreated(NodeId nNodeId, NetconfNode netconfNode) {
        Ipv4Address ipv4Address = netconfNode.getHost().getIpAddress().getIpv4Address();
        Ipv6Address ipv6Address = netconfNode.getHost().getIpAddress().getIpv6Address();
        LOG.debug("In onCreated of MountpointNodeStateListenerImpl - nNodeId = {}, IP Address = {}",nNodeId.getValue(),
                ipv4Address != null?ipv4Address.getValue():ipv6Address.getValue());
        JSONObject obj = new JSONObject();
        obj.put(Constants.NODEID, nNodeId.getValue());
        obj.put(Constants.NETCONFNODESTATE, netconfNode.getConnectionStatus().toString());
        obj.put(Constants.TIMESTAMP, java.time.Clock.systemUTC().instant());

        mountpointStatePublisher.addToPublish(obj);
    }

    @Override
    public void onStateChange(NodeId nNodeId, NetconfNode netconfNode) {
        Ipv4Address ipv4Address = netconfNode.getHost().getIpAddress().getIpv4Address();
        Ipv6Address ipv6Address = netconfNode.getHost().getIpAddress().getIpv6Address();
        LOG.debug("In onStateChange of MountpointNodeStateListenerImpl - nNodeId = {}, IP Address = {}",nNodeId.getValue(),
                ipv4Address != null?ipv4Address.getValue():ipv6Address.getValue());
        JSONObject obj = new JSONObject();
        obj.put(Constants.NODEID, nNodeId.getValue());
        obj.put(Constants.NETCONFNODESTATE, netconfNode.getConnectionStatus().toString());
        obj.put(Constants.TIMESTAMP, java.time.Clock.systemUTC().instant());

        mountpointStatePublisher.addToPublish(obj);
    }

    @Override
    public void onRemoved(NodeId nNodeId) {

        LOG.debug("In onRemoved of MountpointNodeStateListenerImpl - nNodeId = {}",nNodeId);
        JSONObject obj = new JSONObject();
        obj.put(Constants.NODEID, nNodeId.getValue());
        obj.put(Constants.NETCONFNODESTATE, "Removed");
        obj.put(Constants.TIMESTAMP, java.time.Clock.systemUTC().instant());

        mountpointStatePublisher.addToPublish(obj);
    }

    public void stop() throws Exception {
        this.close();
    }

    @Override
    public void close() throws Exception {
        registeredNodeStateListener.close();
    }

}
