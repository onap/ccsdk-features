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

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfAccessorManager {

    private static final Logger log = LoggerFactory.getLogger(NetconfNodeStateServiceImpl.class);

    private static final TransactionUtils TRANSACTIONUTILS = new GenericTransactionUtils();
    private final ConcurrentHashMap<String, NetconfAccessorImpl> accessorList;

    public NetconfAccessorManager() {
        accessorList = new ConcurrentHashMap<>();
    }

    public NetconfAccessor getAccessor(NodeId nNodeId, NetconfNode netconfNode, DataBroker netconfNodeDataBroker,
            MountPoint mountPoint) {
        NetconfAccessorImpl res =
                new NetconfAccessorImpl(nNodeId, netconfNode, netconfNodeDataBroker, mountPoint, TRANSACTIONUTILS);
        NetconfAccessor previouse = accessorList.put(nNodeId.getValue(), res);
        if (Objects.nonNull(previouse)) {
            log.warn("Accessor with name already available. Replaced with new one.");
        }
        return res;
    }

    public boolean containes(NodeId nNodeId) {
        return accessorList.containsKey(nNodeId.getValue());
    }

    public void removeAccessor(NodeId nNodeId) {
        accessorList.remove(nNodeId.getValue());
    }
}
