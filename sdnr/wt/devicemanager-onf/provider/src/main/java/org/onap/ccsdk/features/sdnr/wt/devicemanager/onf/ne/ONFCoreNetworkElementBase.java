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
/**
 *
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ne;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ONFCoreNetworkElementRepresentation;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.InternalConnectionStatus;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.NetworkElement;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.credentials.Credentials;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.credentials.credentials.LoginPassword;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.network.element.connection.entity.NodeDetailsBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author herbert
 *
 */
@SuppressWarnings("deprecation")
public abstract class ONFCoreNetworkElementBase implements AutoCloseable, ONFCoreNetworkElementRepresentation {

    private static final Logger LOG = LoggerFactory.getLogger(ONFCoreNetworkElementBase.class);

    protected static final String EMPTY = "";

    private final String mountPointNodeName;
    private final NodeId nodeId;
    private final DataBroker netconfNodeDataBroker;
    private final NetconfAccessor acessor;

    protected ONFCoreNetworkElementBase(NetconfAccessor acessor) {
        LOG.info("Create ONFCoreNetworkElementBase");
        this.mountPointNodeName = acessor.getNodeId().getValue();
        this.nodeId = acessor.getNodeId();
        this.netconfNodeDataBroker = acessor.getDataBroker();
        this.acessor = acessor;

    }

    @Override
    public Optional<NetconfAccessor> getAcessor() {
        return Optional.of(acessor);
    }

    @Override
    public String getMountPointNodeName() {
        return mountPointNodeName;
    }

    /**
     * @return the netconfNodeDataBroker
     */
    public DataBroker getNetconfNodeDataBroker() {
        return netconfNodeDataBroker;
    }

    @Override
    public void warmstart() {
        int problems = removeAllCurrentProblemsOfNode();
        LOG.debug("Removed all {} problems from database at deregistration for {}", problems, mountPointNodeName);
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    public TransactionUtils getGenericTransactionUtils() {
        return acessor.getTransactionUtils();
    }

    /*---------------------------------------------------------------
     * Getter/ Setter
     */
    @Override
    public String getMountpoint() {
        return mountPointNodeName;
    }

    @Override
    public DataBroker getDataBroker() {
        return netconfNodeDataBroker;
    }

}
