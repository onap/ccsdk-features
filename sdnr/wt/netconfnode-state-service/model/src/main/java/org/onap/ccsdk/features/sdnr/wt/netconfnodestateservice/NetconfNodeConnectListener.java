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

import java.util.EventListener;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

/**
 * Indicate if device is connected or not. A NetconfNode (Mountpoint) is providing the status. If this is Master and
 * connected, this function is calles.
 */

public interface NetconfNodeConnectListener extends EventListener, AutoCloseable {

    /**
     * Called if device state changes to "connected" for a netconf master node.
     * 
     * @param acessor containing <br>
     *        - nNodeId name of mount point<br>
     *        - netconfNode with related information<br>
     *        - mountPoint of the node<br>
     *        -netconfNodeDataBroker to access connected netconf device
     */
    public void onEnterConnected(@NonNull NetconfAccessor acessor);

    /**
     * Notify of device state change to "not connected" mount point supervision for master mountpoint HINT: This
     * callback could be called multiple times also the onEnterConnected state was not called.
     *
     * @param nNodeId name of mount point
     * @param optionalNetconfNode with new status or if removed not present
     */
    public void onLeaveConnected(@NonNull NodeId nNodeId, @NonNull Optional<NetconfNode> optionalNetconfNode);

}
