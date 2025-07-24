/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.dom;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.config.ORanDMConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDeviceManagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.yangspecs.ORANFM;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.yangspecs.OnapSystem;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

public class TestORanNetworkElementFactory {

    private static String NODEIDSTRING = "nSky";

    private static NetconfAccessor accessor;
    private static NetconfDomAccessor domAccessor;
    private static DeviceManagerServiceProvider serviceProvider;
    private static Capabilities capabilities;
    private static VESCollectorService vesCollectorService;
    private static FaultService faultService;
    private static WebsocketManagerService notificationService;
    private static DataProvider databaseService;
    private static ORanDMConfig oranDmConfig;
    private static ConfigurationFileRepresentation oranCfg;
    private static NodeId nodeId = new NodeId(NODEIDSTRING);

    private static String fileName = "test1.properties";
    // @formatter:off
    private static final String TESTCONFIG_CONTENT = "[ORAN-SUPERVISION]\n"
            + "supervision-notification-interval=60\n"
            + "guard-timer-overhead=10\n"
            + "";
    // @formatter:on


    @BeforeClass
    public static void init() throws InterruptedException, IOException {
        accessor = mock(NetconfAccessor.class);
        domAccessor = mock(NetconfDomAccessor.class);
        capabilities = mock(Capabilities.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);
        vesCollectorService = mock(VESCollectorService.class);
        faultService = mock(FaultService.class);
        notificationService = mock(WebsocketManagerService.class);
        databaseService = mock(DataProvider.class);
        oranDmConfig = mock(ORanDMConfig.class);

        when(domAccessor.getCapabilites()).thenReturn(capabilities);
        when(domAccessor.getNodeId()).thenReturn(nodeId);
        when(accessor.getCapabilites()).thenReturn(capabilities);
        when(accessor.getNetconfDomAccessor()).thenReturn(Optional.of(domAccessor));
        when(serviceProvider.getFaultService()).thenReturn(faultService);
        when(serviceProvider.getWebsocketService()).thenReturn(notificationService);
        when(serviceProvider.getDataProvider()).thenReturn(databaseService);
        when(serviceProvider.getVESCollectorService()).thenReturn(vesCollectorService);
        when(capabilities.isSupportingNamespaceAndRevision(
                QNameModule.of(XMLNamespace.of(ORANFM.NAMESPACE), Revision.of("2022-08-15")))).thenReturn(true);
        when(capabilities.isSupportingNamespaceAndRevision(
                QNameModule.of(XMLNamespace.of(OnapSystem.NAMESPACE), Revision.of("2022-11-04")))).thenReturn(true);
        Files.asCharSink(new File(fileName), StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT);
        oranCfg = new ConfigurationFileRepresentation(fileName);

    }

    @Test
    public void testCreateORANHWComponent() throws Exception {
        when(domAccessor.getCapabilites().isSupportingNamespace(ORanDeviceManagerQNames.ORAN_HW_COMPONENT)).thenReturn(true);
        ORanNetworkElementFactory factory = new ORanNetworkElementFactory(oranCfg, oranDmConfig);
        assertTrue((factory.create(accessor, serviceProvider)).isPresent());
    }

    @Test
    public void testCreateNone() throws Exception {
        when(domAccessor.getCapabilites().isSupportingNamespace(ORanDeviceManagerQNames.ORAN_HW_COMPONENT)).thenReturn(false);
        ORanNetworkElementFactory factory = new ORanNetworkElementFactory(oranCfg, oranDmConfig);
        assertTrue(!(factory.create(accessor, serviceProvider).isPresent()));
    }

    @After
    public void cleanUp() throws Exception {
        File file = new File(fileName);
        if (file.exists()) {
            System.out.println("File exists, Deleting it");
            file.delete();
        }

    }
}

