/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
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
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl.OpenroadmNetworkElementBase;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
public class TestOpenroadmNetworkElementBase {
 // variables
    private NetconfBindingAccessor netconfAccessor = mock(NetconfBindingAccessor.class);
    private DeviceManagerServiceProvider deviceManagerSvcProvider = mock(DeviceManagerServiceProvider.class);
    private DataProvider databaseService = mock(DataProvider.class);
    private NodeId nodeId = new NodeId("RoadmA");
    private OpenroadmNetworkElementBase openRoadmNetElementBase = new OpenroadmNetworkElementBase(netconfAccessor,deviceManagerSvcProvider);
 // end of variables

 // public methods
    @Before
    public void init() {

        when(netconfAccessor.getNodeId()).thenReturn(nodeId);

   }

    @Test
    public void testGetNodeId() {
               assertNotNull(openRoadmNetElementBase.getNodeId());

    }

    @Test
    public void testGetDeviceType() {
        assertNotNull(openRoadmNetElementBase.getDeviceType());
    }

}
