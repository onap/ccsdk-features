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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom;

import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.Onf14DomNetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

public class TestOnf14NetworkElementFactory extends Mockito {

    private static final QNameModule qnm = QNameModule.create(XMLNamespace.of("urn:onf:yang:core-model-1-4"), Revision.of("2019-11-27"));
    private static NetconfAccessor accessor;
    private static NetconfDomAccessor domAccessor;
    private static Capabilities capabilities;
    private static DeviceManagerServiceProvider serviceProvider;
    private static String filename = "test.properties";


    @BeforeClass
    public static void init() throws InterruptedException, IOException {
        capabilities = mock(Capabilities.class);
        accessor = mock(NetconfAccessor.class);
        domAccessor = mock(NetconfDomAccessor.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);

        when(domAccessor.getCapabilites()).thenReturn(capabilities);
        when(serviceProvider.getDataProvider()).thenReturn(mock(DataProvider.class));
        when(serviceProvider.getFaultService()).thenReturn(mock(FaultService.class));

    }

    @Test
    public void testCreateOnf14Dom() throws Exception {
        when(accessor.getNetconfDomAccessor()).thenReturn(Optional.of(domAccessor));
        when(capabilities.isSupportingNamespaceAndRevision(
                QNameModule.create(XMLNamespace.of("urn:onf:yang:core-model-1-4"), Revision.of("2019-11-27"))))
                        .thenReturn(true);
        when(capabilities.isSupportingNamespaceAndRevision(
                QNameModule.create(XMLNamespace.of("urn:onf:yang:alarms-1-0"), Revision.of("2022-03-02"))))
                        .thenReturn(true);
        Onf14DomNetworkElementFactory factory = new Onf14DomNetworkElementFactory();
        factory.init(serviceProvider);
        assertTrue((factory.create(accessor, serviceProvider)).isPresent());
    }

    @Test
    public void testCreateNone() throws Exception {
        when(accessor.getNetconfDomAccessor()).thenReturn(Optional.of(domAccessor));
        when(capabilities.isSupportingNamespaceAndRevision(
                QNameModule.create(XMLNamespace.of("urn:onf:yang:core-model-1-4"), Revision.of("2019-11-27"))))
                        .thenReturn(false);
        Onf14DomNetworkElementFactory factory = new Onf14DomNetworkElementFactory();
        assertTrue(factory.create(accessor, serviceProvider).isEmpty());
    }

    @AfterClass
    public static void cleanUp() {
        File file = new File(filename);
        if (file.exists()) {
            System.out.println("File exists, Deleting it");
            file.delete();
        }
    }
}

