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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Optional;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.yang.gen.v1.urn.o.ran.hardware._1._0.rev190328.ORANHWCOMPONENT;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class TestORanNetworkElementFactory {

    private static String NODEIDSTRING = "nSky";

    private static NetconfBindingAccessor accessor;
    private static DeviceManagerServiceProvider serviceProvider;
    private static Capabilities capabilities;
    private static VESCollectorService vesCollectorService;
    private static FaultService faultService;
    private static WebsocketManagerService notificationService;
    private static DataProvider databaseService;

    @BeforeClass
    public static void init() throws InterruptedException, IOException {
        NetconfBindingAccessor bindingAccessor = mock(NetconfBindingAccessor.class);
        NodeId nodeId = new NodeId(NODEIDSTRING);
        when(bindingAccessor.getTransactionUtils()).thenReturn(mock(TransactionUtils.class));
        when(bindingAccessor.getNodeId()).thenReturn(nodeId);

        capabilities = mock(Capabilities.class);
        accessor = mock(NetconfBindingAccessor.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);
        vesCollectorService = mock(VESCollectorService.class);
        faultService = mock(FaultService.class);
        notificationService = mock(WebsocketManagerService.class);
        databaseService = mock(DataProvider.class);

        when(accessor.getCapabilites()).thenReturn(capabilities);
        when(accessor.getNetconfBindingAccessor()).thenReturn(Optional.of(bindingAccessor));
        when(serviceProvider.getFaultService()).thenReturn(faultService);
        when(serviceProvider.getWebsocketService()).thenReturn(notificationService);
        when(serviceProvider.getDataProvider()).thenReturn(databaseService);
        when(serviceProvider.getVESCollectorService()).thenReturn(vesCollectorService);

    }

    @Test
    public void testCreateORANHWComponent() throws Exception {
        when(accessor.getCapabilites().isSupportingNamespace(ORANHWCOMPONENT.QNAME)).thenReturn(true);
        ORanNetworkElementFactory factory = new ORanNetworkElementFactory();
        assertTrue((factory.create(accessor, serviceProvider)).isPresent());
    }

    @Test
    public void testCreateNone() throws Exception {
        when(accessor.getCapabilites().isSupportingNamespace(ORANHWCOMPONENT.QNAME)).thenReturn(false);
        ORanNetworkElementFactory factory = new ORanNetworkElementFactory();
        assertTrue(!(factory.create(accessor, serviceProvider).isPresent()));
    }

    @After
    public void cleanUp() throws Exception {

    }
}

