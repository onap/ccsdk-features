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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl.OpenroadmNetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.test.mock.NetconfAccessorMock;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.OrgOpenroadmDevice;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;

public class TestOpenRoadmNetworkElementFactory {

    static NetconfAccessor accessor;
    static DeviceManagerServiceProvider serviceProvider;
    static Capabilities capabilities;
    QName qCapability;
    private NodeId nodeId = new NodeId("RoadmA2");
    static OrgOpenroadmDevice device;
    static TransactionUtils transactionUtils;
    static DataBroker dataBroker;

    @BeforeClass
    public static void init() throws InterruptedException, IOException {
        accessor = mock(NetconfAccessorMock.class);
        capabilities = mock(Capabilities.class);
        dataBroker = mock(DataBroker.class);
        transactionUtils = mock(TransactionUtils.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);
        when(accessor.getCapabilites()).thenReturn(capabilities);
        when(accessor.getDataBroker()).thenReturn(dataBroker);
        when(accessor.getTransactionUtils()).thenReturn(transactionUtils);
        when(serviceProvider.getDataProvider()).thenReturn(null);
        device = mock(OrgOpenroadmDevice.class);
        final Class<OrgOpenroadmDevice> openRoadmDev = OrgOpenroadmDevice.class;
        InstanceIdentifier<OrgOpenroadmDevice> deviceId = InstanceIdentifier.builder(openRoadmDev).build();
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                deviceId)).thenReturn(device);
    }

    @Test
    public void testCapabilties() {
        when(accessor.getNodeId()).thenReturn(nodeId);
        when(accessor.getCapabilites().isSupportingNamespace(OrgOpenroadmDevice.QNAME)).thenReturn(true);

        // when(accessor.getCapabilites().isSupportingNamespace(SimulatorStatus.QNAME)).thenReturn(false);
        OpenroadmNetworkElementFactory factory = new OpenroadmNetworkElementFactory();
        assertTrue((factory.create(accessor, serviceProvider)).isPresent());
    }

    @Test
    public void testCreateNone() throws Exception {
        when(accessor.getCapabilites().isSupportingNamespace(OrgOpenroadmDevice.QNAME)).thenReturn(false);
        // when(accessor.getCapabilites().isSupportingNamespace(SimulatorStatus.QNAME)).thenReturn(false);
        OpenroadmNetworkElementFactory factory = new OpenroadmNetworkElementFactory();
        assertTrue(!(factory.create(accessor, serviceProvider).isPresent()));
    }

    @After
    public void cleanUp() throws Exception {

    }


}
