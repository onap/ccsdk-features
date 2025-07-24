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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl.OpenroadmNetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.OrgOpenroadmDevice;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TestOpenRoadmNetworkElementFactory {

    private static NetconfBindingAccessor bindingAccessor;
    private static DeviceManagerServiceProvider serviceProvider;
    private static Capabilities capabilities;
    private static TransactionUtils transactionUtils;
    private static DataBroker dataBroker;

    @Before
    public void init() throws InterruptedException, IOException {
        bindingAccessor = mock(NetconfBindingAccessor.class);
        capabilities = mock(Capabilities.class);
        dataBroker = mock(DataBroker.class);
        transactionUtils = mock(TransactionUtils.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);

        when(bindingAccessor.getCapabilites()).thenReturn(capabilities);
        when(bindingAccessor.getNetconfBindingAccessor()).thenReturn(Optional.of(bindingAccessor));
        when(bindingAccessor.getNodeId()).thenReturn(new NodeId("RoadmA2"));
        when(bindingAccessor.getNodeId()).thenReturn(new NodeId("RoadmA2"));
        when(bindingAccessor.getDataBroker()).thenReturn(dataBroker);
        when(bindingAccessor.getTransactionUtils()).thenReturn(transactionUtils);
        when(serviceProvider.getDataProvider()).thenReturn(null);

        final Class<OrgOpenroadmDevice> openRoadmDev = OrgOpenroadmDevice.class;
        InstanceIdentifier<OrgOpenroadmDevice> deviceId = InstanceIdentifier.builder(openRoadmDev).build();
        when(bindingAccessor.getTransactionUtils().readData(bindingAccessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                deviceId)).thenReturn(mock(OrgOpenroadmDevice.class));

        when(bindingAccessor.getTransactionUtils()).thenReturn(mock(TransactionUtils.class));
    }

    @Test
    public void testCapabiltiesAvailable1() {
        when(bindingAccessor.getCapabilites().isSupportingNamespaceAndRevision(OrgOpenroadmDevice.QNAME)).thenReturn(true);
        OpenroadmNetworkElementFactory factory = new OpenroadmNetworkElementFactory();
        assertTrue((factory.create(bindingAccessor, serviceProvider)).isPresent());
    }

    @Test
    public void testCapabiltiesAvailable2() {
        when(bindingAccessor.getCapabilites().isSupportingNamespaceAndRevision("http://org/openroadm/device", "2018-10-19"))
                .thenReturn(true);
        OpenroadmNetworkElementFactory factory = new OpenroadmNetworkElementFactory();
        assertTrue((factory.create(bindingAccessor, serviceProvider)).isPresent());
    }

    @Test
    public void testCapabiltiesNotAvailable() throws Exception {
        when(bindingAccessor.getCapabilites().isSupportingNamespaceAndRevision(OrgOpenroadmDevice.QNAME)).thenReturn(false);
        OpenroadmNetworkElementFactory factory = new OpenroadmNetworkElementFactory();
        assertFalse(factory.create(bindingAccessor, serviceProvider).isPresent());
    }

    @After
    public void cleanUp() throws Exception {

    }

}
