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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm71.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm71.impl.OpenroadmNetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.OrgOpenroadmDevice;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TestOpenRoadmNetworkElementFactory {

    private static NetconfBindingAccessor accessor;
    private static DeviceManagerServiceProvider serviceProvider;
    private static Capabilities capabilities;
    private static TransactionUtils transactionUtils;
    private static DataBroker dataBroker;

    @BeforeClass
    public static void init() throws InterruptedException, IOException {
        accessor = mock(NetconfBindingAccessor.class);
        capabilities = mock(Capabilities.class);
        dataBroker = mock(DataBroker.class);
        transactionUtils = mock(TransactionUtils.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);
        when(accessor.getNodeId()).thenReturn(new NodeId("RoadmA2"));
        when(accessor.getCapabilites()).thenReturn(capabilities);
        when(accessor.getDataBroker()).thenReturn(dataBroker);
        when(accessor.getTransactionUtils()).thenReturn(transactionUtils);
        when(accessor.getNetconfBindingAccessor()).thenReturn(Optional.of(accessor));
        when(serviceProvider.getDataProvider()).thenReturn(null);

        final Class<OrgOpenroadmDevice> openRoadmDev = OrgOpenroadmDevice.class;
        InstanceIdentifier<OrgOpenroadmDevice> deviceId = InstanceIdentifier.create(openRoadmDev);
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                deviceId)).thenReturn(mock(OrgOpenroadmDevice.class));

        when(accessor.getTransactionUtils()).thenReturn(mock(TransactionUtils.class));
    }

    @Test
    public void testCapabiltiesAvailable1() {
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(OrgOpenroadmDevice.QNAME)).thenReturn(true);
        OpenroadmNetworkElementFactory factory = new OpenroadmNetworkElementFactory();
        assertTrue((factory.create(accessor, serviceProvider)).isPresent());
    }

    @Test
    public void testCapabiltiesAvailable2() {
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision("http://org/openroadm/device", "2018-10-19"))
                .thenReturn(true);
        OpenroadmNetworkElementFactory factory = new OpenroadmNetworkElementFactory();
        assertTrue((factory.create(accessor, serviceProvider)).isPresent());
    }

    @Test
    public void testCapabiltiesNotAvailable() throws Exception {
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(OrgOpenroadmDevice.QNAME)).thenReturn(false);
        OpenroadmNetworkElementFactory factory = new OpenroadmNetworkElementFactory();
        assertFalse(factory.create(accessor, serviceProvider).isPresent());
    }

    @After
    public void cleanUp() throws Exception {

    }

}
