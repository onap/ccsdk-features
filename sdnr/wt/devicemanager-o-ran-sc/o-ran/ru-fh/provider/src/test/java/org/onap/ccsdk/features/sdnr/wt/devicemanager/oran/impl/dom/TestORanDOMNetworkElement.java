/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.startup.ORanNetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationProxyParser;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.QName;

public class TestORanDOMNetworkElement {

    private static final QName OneCell =
            QName.create("urn:onf:otcc:wireless:yang:radio-access:commscope-onecell", "2020-06-22", "onecell").intern();
    private static final @NonNull QName OnapSystem =
            QName.create("urn:onap:system", "2020-10-26", "onap-system").intern();
    private static String NODEIDSTRING = "nSky";
    private static NodeId nodeId = new NodeId(NODEIDSTRING);

    private static NetconfAccessor accessor;
    private static DeviceManagerServiceProvider serviceProvider;
    private static Capabilities capabilities;
    private static NetconfDomAccessor domAccessor;
    private static VESCollectorService vesCollectorService;
    private static NotificationProxyParser notificationProxyParser;
    private static VESCollectorCfgService vesCfgService;
    private static WebsocketManagerService websocketManagerService;

    @BeforeClass
    public static void init() throws InterruptedException, IOException {
        capabilities = mock(Capabilities.class);
        accessor = mock(NetconfAccessor.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);
        domAccessor = mock(NetconfDomAccessor.class);
        vesCollectorService = mock(VESCollectorService.class);
        notificationProxyParser = mock(NotificationProxyParser.class);
        vesCfgService = mock(VESCollectorCfgService.class);
        websocketManagerService = mock(WebsocketManagerService.class);

        when(accessor.getCapabilites()).thenReturn(capabilities);
        when(accessor.getNodeId()).thenReturn(nodeId);
        when(accessor.getNetconfDomAccessor()).thenReturn(Optional.of(domAccessor));
        when(domAccessor.getNodeId()).thenReturn(nodeId);
        when(domAccessor.getCapabilites()).thenReturn(capabilities);
        when(vesCollectorService.getNotificationProxyParser()).thenReturn(notificationProxyParser);

        DataProvider dataProvider = mock(DataProvider.class);
        FaultService faultService = mock(FaultService.class);
        when(serviceProvider.getWebsocketService()).thenReturn(websocketManagerService);
        when(serviceProvider.getFaultService()).thenReturn(faultService);
        when(serviceProvider.getDataProvider()).thenReturn(dataProvider);
        when(serviceProvider.getVESCollectorService()).thenReturn(vesCollectorService);
        when(vesCollectorService.getConfig()).thenReturn(vesCfgService);
        when(vesCfgService.isVESCollectorEnabled()).thenReturn(true);

    }

    @Test
    public void test() {
        Optional<NetworkElement> oRanNe;
        when(capabilities.isSupportingNamespace(ORanDeviceManagerQNames.ORAN_HW_COMPONENT)).thenReturn(true);
        when(capabilities.isSupportingNamespace(OneCell)).thenReturn(false);
        when(capabilities.isSupportingNamespace(OnapSystem)).thenReturn(false);

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
