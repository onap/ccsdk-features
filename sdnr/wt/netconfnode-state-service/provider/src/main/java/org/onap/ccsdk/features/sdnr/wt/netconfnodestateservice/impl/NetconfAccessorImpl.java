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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.NotificationsService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.StreamNameType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfAccessorImpl implements NetconfAccessor {

    private static final Logger log = LoggerFactory.getLogger(NetconfAccessorImpl.class);

    private static final @NonNull InstanceIdentifier<Topology> NETCONF_TOPO_IID =
            InstanceIdentifier.create(NetworkTopology.class).child(Topology.class,
                    new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));

    private final NodeId nodeId;
    private final DataBroker dataBroker;
    private final TransactionUtils transactionUtils;
    private final MountPoint mountpoint;
    private final NetconfNode netconfNode;
    private final Capabilities capabilities;

    /**
     * Contains all data to access and manage netconf device
     *
     * @param nodeId of managed netconf node
     * @param netconfNode information
     * @param dataBroker to access node
     * @param mountpoint of netconfNode
     * @param transactionUtils with read an write functions
     */
    public NetconfAccessorImpl(NodeId nodeId, NetconfNode netconfNode, DataBroker dataBroker, MountPoint mountpoint,
            TransactionUtils transactionUtils) {
        super();
        this.nodeId = nodeId;
        this.netconfNode = netconfNode;
        this.dataBroker = dataBroker;
        this.mountpoint = mountpoint;
        this.transactionUtils = transactionUtils;

        ConnectionStatus csts = netconfNode != null ? netconfNode.getConnectionStatus() : null;
        if (csts == null) {
            throw new IllegalStateException(String.format("connection status for %s is not connected", nodeId));
        }
        Capabilities tmp = Capabilities.getAvailableCapabilities(netconfNode);
        if (tmp.getCapabilities().size() <= 0) {
            throw new IllegalStateException(String.format("no capabilities found for %s", nodeId));
        }
        this.capabilities = tmp;
    }

    /**
     * @param nodeId with uuid of managed netconf node
     * @param dataBroker to access node
     */
    public NetconfAccessorImpl(String nodeId, NetconfNode netconfNode, DataBroker dataBroker, MountPoint mountpoint,
            TransactionUtils transactionUtils) {
        this(new NodeId(nodeId), netconfNode, dataBroker, mountpoint, transactionUtils);
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    @Override
    public DataBroker getDataBroker() {
        return dataBroker;
    }

    @Override
    public MountPoint getMountpoint() {
        return mountpoint;
    }

    @Override
    public TransactionUtils getTransactionUtils() {
        return transactionUtils;
    }

    @Override
    public NetconfNode getNetconfNode() {
        return netconfNode;
    }

    @Override
    public Capabilities getCapabilites() {
        return capabilities;
    }

    @Override
    public @NonNull <T extends NotificationListener> ListenerRegistration<NotificationListener> doRegisterNotificationListener(
            @NonNull T listener) {
        log.info("Begin register listener for Mountpoint {}", mountpoint.getIdentifier().toString());
        final Optional<NotificationService> optionalNotificationService =
                mountpoint.getService(NotificationService.class);
        final NotificationService notificationService = optionalNotificationService.get();
        final ListenerRegistration<NotificationListener> ranListenerRegistration =
                notificationService.registerNotificationListener(listener);
        log.info("End registration listener for Mountpoint {} Listener: {} Result: {}",
                mountpoint.getIdentifier().toString(), optionalNotificationService, ranListenerRegistration);
        return ranListenerRegistration;
    }

    @Override
    public ListenableFuture<RpcResult<CreateSubscriptionOutput>> registerNotificationsStream(String streamName) {

        String failMessage = "";
        final Optional<RpcConsumerRegistry> optionalRpcConsumerService =
                mountpoint.getService(RpcConsumerRegistry.class);
        if (optionalRpcConsumerService.isPresent()) {
            final RpcConsumerRegistry rpcConsumerRegitry = optionalRpcConsumerService.get();
            @NonNull
            final NotificationsService rpcService = rpcConsumerRegitry.getRpcService(NotificationsService.class);

            final CreateSubscriptionInputBuilder createSubscriptionInputBuilder = new CreateSubscriptionInputBuilder();
            createSubscriptionInputBuilder.setStream(new StreamNameType(streamName));
            log.info("Event listener triggering notification stream {} for node {}", streamName, nodeId);
            try {
                CreateSubscriptionInput createSubscriptionInput = createSubscriptionInputBuilder.build();
                if (createSubscriptionInput == null) {
                    failMessage = "createSubscriptionInput is null for mountpoint " + nodeId;
                } else {
                    return rpcService.createSubscription(createSubscriptionInput);
                }
            } catch (NullPointerException e) {
                failMessage = "createSubscription failed";
            }
        } else {
            failMessage = "No RpcConsumerRegistry avaialble.";
        }
        log.warn(failMessage);
        RpcResultBuilder<CreateSubscriptionOutput> result = RpcResultBuilder.failed();
        result.withError(ErrorType.APPLICATION, failMessage);
        SettableFuture<RpcResult<CreateSubscriptionOutput>> res = SettableFuture.create();
        res.set(result.build());
        return res;
    }


}
