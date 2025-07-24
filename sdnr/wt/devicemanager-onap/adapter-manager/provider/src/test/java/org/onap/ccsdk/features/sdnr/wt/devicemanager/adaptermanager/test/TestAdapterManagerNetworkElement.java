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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.adaptermanager.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.adaptermanager.impl.AdapterManagerNetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.yang.gen.v1.urn.o.ran.sc.params.xml.ns.yang.nts.manager.rev210608.simulation.NetworkFunctions;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.QName;

public class TestAdapterManagerNetworkElement {

    static NetconfBindingAccessor accessor;
    static DeviceManagerServiceProvider serviceProvider;
    static Capabilities capabilities;
    QName qCapability;

    @BeforeClass
    public static void init() throws InterruptedException, IOException {
        capabilities = mock(Capabilities.class);
        //accessor = mock(NetconfAccessorMock.class);
        accessor = mock(NetconfBindingAccessor.class); //spy(new NetconfAccessorMock(null, null, null, null));
        serviceProvider = mock(DeviceManagerServiceProvider.class);

        NodeId nNodeId = new NodeId("nSky");
        when(accessor.getCapabilites()).thenReturn(capabilities);
        when(accessor.getNodeId()).thenReturn(nNodeId);
        when(accessor.getTransactionUtils()).thenReturn(mock(TransactionUtils.class));
        when(accessor.getNetconfBindingAccessor()).thenReturn(Optional.of(accessor));

        DataProvider dataProvider = mock(DataProvider.class);
        when(serviceProvider.getDataProvider()).thenReturn(dataProvider);
    }

    @Test
    public void test() {
        Optional<NetworkElement> adapterManagerNe;
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(NetworkFunctions.QNAME)).thenReturn(true);
        AdapterManagerNetworkElementFactory factory = new AdapterManagerNetworkElementFactory();
        adapterManagerNe = factory.create(accessor, serviceProvider);
        assertTrue(adapterManagerNe.isPresent());
        adapterManagerNe.get().register();
        adapterManagerNe.get().deregister();
        adapterManagerNe.get().getAcessor();
        adapterManagerNe.get().getDeviceType();
        assertEquals(adapterManagerNe.get().getNodeId().getValue(), "nSky");
    }

    @After
    public void cleanUp() throws Exception {

    }
}
