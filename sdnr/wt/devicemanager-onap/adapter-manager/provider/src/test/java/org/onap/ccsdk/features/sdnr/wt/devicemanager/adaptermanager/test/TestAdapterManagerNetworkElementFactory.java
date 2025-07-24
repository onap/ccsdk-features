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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.adaptermanager.impl.AdapterManagerNetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.yang.gen.v1.urn.o.ran.sc.params.xml.ns.yang.nts.manager.rev210608.simulation.NetworkFunctions;
import org.opendaylight.yangtools.yang.common.QName;

public class TestAdapterManagerNetworkElementFactory {

    static NetconfBindingAccessor accessor;
    static DeviceManagerServiceProvider serviceProvider;
    static Capabilities capabilities;
    QName qCapability;

    @BeforeClass
    public static void init() throws InterruptedException, IOException {
        capabilities = mock(Capabilities.class);
        accessor = mock(NetconfBindingAccessor.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);

        when(accessor.getCapabilites()).thenReturn(capabilities);
        when(accessor.getNetconfBindingAccessor()).thenReturn(Optional.of(accessor));
        when(serviceProvider.getDataProvider()).thenReturn(null);
    }

    @Test
    public void testCreateSimulator() throws Exception {
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(NetworkFunctions.QNAME)).thenReturn(true);
        AdapterManagerNetworkElementFactory factory = new AdapterManagerNetworkElementFactory();
        assertTrue(factory.create(accessor, serviceProvider).isPresent());
    }

    @Test
    public void testCreateNone() throws Exception {
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(NetworkFunctions.QNAME)).thenReturn(false);
        AdapterManagerNetworkElementFactory factory = new AdapterManagerNetworkElementFactory();
        assertFalse(factory.create(accessor, serviceProvider).isPresent());
    }

    @After
    public void cleanUp() throws Exception {

    }
}

