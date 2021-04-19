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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.ORanNetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationProxyParser;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.yang.gen.v1.urn.o.ran.hardware._1._0.rev190328.ORANHWCOMPONENT;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.QName;

public class TestORanNetworkElement {

    private static final QName OneCell =
            QName.create("urn:onf:otcc:wireless:yang:radio-access:commscope-onecell", "2020-06-22", "onecell").intern();
    private static String NODEIDSTRING = "nSky";
    private static NodeId nodeId = new NodeId(NODEIDSTRING);
    private static NodeId nNodeId = new NodeId("nSky");

    private static NetconfAccessor accessor;
    private static DeviceManagerServiceProvider serviceProvider;
    private static Capabilities capabilities;
    private static TransactionUtils transactionUtils;
    private static NetconfBindingAccessor bindingCommunicator;
    private static VESCollectorService vesCollectorService;
    private static NotificationProxyParser notificationProxyParser;
    private static VESCollectorCfgService vesCfgService;

    @BeforeClass
    public static void init() throws InterruptedException, IOException {
        capabilities = mock(Capabilities.class);
        accessor = mock(NetconfAccessor.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);
        transactionUtils = mock(TransactionUtils.class);
        bindingCommunicator = mock(NetconfBindingAccessor.class);
        vesCollectorService = mock(VESCollectorService.class);
        notificationProxyParser = mock(NotificationProxyParser.class);
        vesCfgService = mock(VESCollectorCfgService.class);

        when(accessor.getCapabilites()).thenReturn(capabilities);
        when(accessor.getNodeId()).thenReturn(nNodeId);
        when(accessor.getNetconfBindingAccessor()).thenReturn(Optional.of(bindingCommunicator));
        when(bindingCommunicator.getTransactionUtils()).thenReturn(transactionUtils);
        when(bindingCommunicator.getNodeId()).thenReturn(nodeId);
        when(vesCollectorService.getNotificationProxyParser()).thenReturn(notificationProxyParser);

        DataProvider dataProvider = mock(DataProvider.class);
        when(serviceProvider.getDataProvider()).thenReturn(dataProvider);
        when(serviceProvider.getVESCollectorService()).thenReturn(vesCollectorService);
        when(vesCollectorService.getConfig()).thenReturn(vesCfgService);
        when(vesCfgService.isVESCollectorEnabled()).thenReturn(true);

    }

    @Test
    public void test() {

        NodeId nodeId = new NodeId(NODEIDSTRING);
        when(bindingCommunicator.getTransactionUtils()).thenReturn(mock(TransactionUtils.class));
        when(bindingCommunicator.getNodeId()).thenReturn(nodeId);

        Optional<NetworkElement> oRanNe;
        when(capabilities.isSupportingNamespace(ORANHWCOMPONENT.QNAME)).thenReturn(true);
        when(capabilities.isSupportingNamespace(OneCell)).thenReturn(false);
        ORanNetworkElementFactory factory = new ORanNetworkElementFactory();
        oRanNe = factory.create(accessor, serviceProvider);
        assertTrue(factory.create(accessor, serviceProvider).isPresent());
        oRanNe.get().register();
        oRanNe.get().deregister();
        oRanNe.get().getAcessor();
        oRanNe.get().getDeviceType();
        assertEquals(oRanNe.get().getNodeId().getValue(), "nSky");
    }

 }
