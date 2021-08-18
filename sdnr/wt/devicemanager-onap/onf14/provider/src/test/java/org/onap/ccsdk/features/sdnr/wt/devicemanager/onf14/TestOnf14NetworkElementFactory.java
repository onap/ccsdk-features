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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14;

import static org.junit.Assert.assertTrue;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.Onf14NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.ControlConstruct;

public class TestOnf14NetworkElementFactory extends Mockito {

    private static NetconfAccessor accessor;
    private static Optional<NetconfDomAccessor> domAccessor;
    private static Capabilities capabilities;
    private static DeviceManagerServiceProvider serviceProvider;
    private static ConfigurationFileRepresentation configurationRepresentation;
    private static String filename = "test.properties";

    // @formatter:off
    private static final String TESTCONFIG_CONTENT = "[dmonf14]\n"
            + "useDomApi=true\n"
            + "";
    // @formatter:on


    @BeforeClass
    public static void init() throws InterruptedException, IOException {
        Files.asCharSink(new File(filename), StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT);
        configurationRepresentation = new ConfigurationFileRepresentation(filename);
        capabilities = mock(Capabilities.class);
        accessor = mock(NetconfAccessor.class);
        domAccessor = Optional.of(mock(NetconfDomAccessor.class));
        serviceProvider = mock(DeviceManagerServiceProvider.class);

        when(accessor.getCapabilites()).thenReturn(capabilities);
        when(serviceProvider.getDataProvider()).thenReturn(mock(DataProvider.class));
        when(serviceProvider.getConfigurationFileRepresentation()).thenReturn(configurationRepresentation);
    }

    @Test
    public void testCreateOnf14Dom() throws Exception {
        when(accessor.getCapabilites().isSupportingNamespace(ControlConstruct.QNAME)).thenReturn(true);
        when(accessor.getNetconfDomAccessor()).thenReturn(domAccessor);
        when(domAccessor.get().getBindingNormalizedNodeSerializer()).thenReturn(mock(BindingNormalizedNodeSerializer.class));
        Onf14NetworkElementFactory factory = new Onf14NetworkElementFactory();
        factory.init(serviceProvider);
        assertTrue((factory.create(accessor, serviceProvider)).isPresent());
    }

    @Test
    public void testCreateOnf14Binding() throws Exception {
        when(accessor.getCapabilites().isSupportingNamespace(ControlConstruct.QNAME)).thenReturn(true);
        when(accessor.getNetconfBindingAccessor()).thenReturn(Optional.of(mock(NetconfBindingAccessor.class)));
        Onf14NetworkElementFactory factory = new Onf14NetworkElementFactory();
        assertTrue(factory.create(accessor, serviceProvider).isPresent());
    }

    @Test
    public void testCreateNone() throws Exception {
        when(accessor.getNetconfBindingAccessor()).thenReturn(Optional.of(mock(NetconfBindingAccessor.class)));
        when(accessor.getCapabilites().isSupportingNamespace(ControlConstruct.QNAME)).thenReturn(false);
        Onf14NetworkElementFactory factory = new Onf14NetworkElementFactory();
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

