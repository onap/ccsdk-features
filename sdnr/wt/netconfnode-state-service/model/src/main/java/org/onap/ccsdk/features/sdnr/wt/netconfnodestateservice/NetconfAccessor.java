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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice;

import java.util.Optional;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

/**
 * Interface handling netconf connection.
 */
public interface NetconfAccessor {

    static String DefaultNotificationsStream = "NETCONF";

    /**
     * @return the Controller DataBroker
     */
    DataBroker getControllerBindingDataBroker();

    /**
     * @return the nodeId
     */
    NodeId getNodeId();

    /**
     * @return NetconfNode of this connection
     */
    NetconfNode getNetconfNode();

    /**
     * @return Capabilites
     */
    Capabilities getCapabilites();


    Optional<NetconfBindingAccessor> getNetconfBindingAccessor();

    Optional<NetconfDomAccessor> getNetconfDomAccessor();
}
