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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.config.ORanDMConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDeviceManagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.yangspecs.ORANFM;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.yangspecs.OnapSystem;
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
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

public class TestORanDOMNetworkElement {

    private static final QName OneCell =
            QName.create("urn:onf:otcc:wireless:yang:radio-access:commscope-onecell", "2020-06-22", "onecell").intern();
    private static final @NonNull QName OnapSystem1 =
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
    private static ORanDMConfig oranDmConfig;
    private static ConfigurationFileRepresentation oranCfg;

    private static String fileName = "test1.properties";
    // @formatter:off
    private static final String TESTCONFIG_CONTENT = "[ORAN-SUPERVISION]\n"
            + "supervision-notification-interval=60\n"
            + "guard-timer-overhead=10\n"
            + "";
    // @formatter:on

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
        oranDmConfig = mock(ORanDMConfig.class);

        when(accessor.getCapabilites()).thenReturn(capabilities);
        when(accessor.getNodeId()).thenReturn(nodeId);
        when(accessor.getNetconfDomAccessor()).thenReturn(Optional.of(domAccessor));
        when(domAccessor.getNodeId()).thenReturn(nodeId);
        when(domAccessor.getCapabilites()).thenReturn(capabilities);
        when(vesCollectorService.getNotificationProxyParser()).thenReturn(notificationProxyParser);
        when(capabilities.isSupportingNamespaceAndRevision(
                QNameModule.create(XMLNamespace.of(ORANFM.NAMESPACE), Revision.of("2022-08-15")))).thenReturn(true);
        when(capabilities.isSupportingNamespaceAndRevision(
                QNameModule.create(XMLNamespace.of(OnapSystem.NAMESPACE), Revision.of("2022-11-04")))).thenReturn(true);

        DataProvider dataProvider = mock(DataProvider.class);
        FaultService faultService = mock(FaultService.class);
        when(serviceProvider.getWebsocketService()).thenReturn(websocketManagerService);
        when(serviceProvider.getFaultService()).thenReturn(faultService);
        when(serviceProvider.getDataProvider()).thenReturn(dataProvider);
        when(serviceProvider.getVESCollectorService()).thenReturn(vesCollectorService);
        when(vesCollectorService.getConfig()).thenReturn(vesCfgService);
        when(vesCfgService.isVESCollectorEnabled()).thenReturn(true);

        Files.asCharSink(new File(fileName), StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT);
        oranCfg = new ConfigurationFileRepresentation(fileName);
    }

    @Test
    public void test() {
        Optional<NetworkElement> oRanNe;
        when(capabilities.isSupportingNamespace(ORanDeviceManagerQNames.ORAN_HW_COMPONENT)).thenReturn(true);
        when(capabilities.isSupportingNamespace(OneCell)).thenReturn(false);
        when(capabilities.isSupportingNamespace(OnapSystem1)).thenReturn(false);

        ORanNetworkElementFactory factory = new ORanNetworkElementFactory(oranCfg, oranDmConfig);
        oRanNe = factory.create(accessor, serviceProvider);
        assertTrue(factory.create(accessor, serviceProvider).isPresent());
        oRanNe.get().register();
        oRanNe.get().deregister();
        oRanNe.get().getAcessor();
        oRanNe.get().getDeviceType();
        assertEquals(oRanNe.get().getNodeId().getValue(), "nSky");
    }
}
