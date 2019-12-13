/**
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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfAccessor implements INetconfAcessor {

    private static final Logger log = LoggerFactory.getLogger(NetconfAccessor.class);

    private final NodeId nodeId;
    private final DataBroker dataBroker;
    private final TransactionUtils transactionUtils;
    private final MountPoint mountpoint;
    private final NetconfNode netconfNode;
    private final Capabilities capabilities;

    /**
     * @param nodeId     of managed netconf node
     * @param dataBroker to access node
     */
    public NetconfAccessor(NodeId nodeId, NetconfNode netconfNode, DataBroker dataBroker, MountPoint mountpoint,
            TransactionUtils transactionUtils) {
        super();
        this.nodeId = nodeId;
        this.netconfNode = netconfNode;
        this.dataBroker = dataBroker;
        this.mountpoint = mountpoint;
        this.transactionUtils = transactionUtils;

        ConnectionStatus csts = netconfNode != null ? netconfNode.getConnectionStatus() : null;
        this.capabilities = Capabilities.getAvailableCapabilities(csts != null ? netconfNode : null);
    }

    /**
     * @param nodeId     with uuid of managed netconf node
     * @param dataBroker to access node
     */
    public NetconfAccessor(String nodeId, NetconfNode netconfNode, DataBroker dataBroker, MountPoint mountpoint,
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
    public @NonNull <T extends NotificationListener> ListenerRegistration<NotificationListener> doRegisterNotificationListener(@NonNull T listener) {
        log.info("Begin register listener for Mountpoint {}", mountpoint.getIdentifier().toString());
        final Optional<NotificationService> optionalNotificationService = mountpoint
                .getService(NotificationService.class);
        final NotificationService notificationService = optionalNotificationService.get();
        final ListenerRegistration<NotificationListener> ranListenerRegistration = notificationService
                .registerNotificationListener(listener);
        log.info("End registration listener for Mountpoint {} Listener: {} Result: {}",
                mountpoint.getIdentifier().toString(), optionalNotificationService, ranListenerRegistration);
        return ranListenerRegistration;
    }

}
