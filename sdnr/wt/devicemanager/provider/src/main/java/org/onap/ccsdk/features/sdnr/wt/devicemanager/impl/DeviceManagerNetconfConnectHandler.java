/*******************************************************************************
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
 ******************************************************************************/

package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeConnectListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceManagerNetconfConnectHandler implements NetconfNodeConnectListener {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerNetconfConnectHandler.class);

    private ListenerRegistration<DeviceManagerNetconfConnectHandler> registerNetconfNodeConnectListener;

    public DeviceManagerNetconfConnectHandler(@Nullable NetconfNodeStateService netconfNodeStateService) {
        if (netconfNodeStateService == null) {
            registerNetconfNodeConnectListener = new ListenerRegistration<DeviceManagerNetconfConnectHandler>() {
                @Override
                public void close() {
                }
                @Override
                public @NonNull DeviceManagerNetconfConnectHandler getInstance() {
                    return DeviceManagerNetconfConnectHandler.this;
                }
            };
        } else {
			this.registerNetconfNodeConnectListener = netconfNodeStateService.registerNetconfNodeConnectListener(this);
		}
    }

    @Override
    public void onEnterConnected(NodeId nNodeId, NetconfNode netconfNode, DataBroker netconfNodeDataBroker) {
        LOG.info("onEnterConnected {}", nNodeId);
        //o-ran-interfaces .. spec for RAN Devices to be used as fingerprint
    }

    @Override
    public void onLeaveConnected(NodeId nNodeId) {
        LOG.info("onLeaveConnected {}", nNodeId);
    }

    @Override
    public void close() throws Exception {
         registerNetconfNodeConnectListener.close();
    }

}
