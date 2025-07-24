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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.impl.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.WrapperMicrowaveModelRev170324;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.WrapperMicrowaveModelRev180907;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.WrapperMicrowaveModelRev181010;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.impl.DeviceManagerOnfConfiguration;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.impl.ONFCoreNetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.NetworkElement;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.QName;

public class TestONFCoreNetworkElementFactory {

    static NetconfBindingAccessor accessor;
    static DeviceManagerServiceProvider serviceProvider;
    static Capabilities capabilities;
    static DataBroker dataBroker;
    static NodeId nNodeId;
    static DeviceManagerOnfConfiguration configuration;
    QName qCapability;
    static File configFile;


    @BeforeClass
    public static void init() throws InterruptedException, IOException {

        capabilities = mock(Capabilities.class);
        accessor = mock(NetconfBindingAccessor.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);
        dataBroker = mock(DataBroker.class);

        configFile = new File("devicemanager.properties");
        ConfigurationFileRepresentation configFileRep = new ConfigurationFileRepresentation(configFile);
        configuration = new DeviceManagerOnfConfiguration(configFileRep);

        when(accessor.getCapabilites()).thenReturn(capabilities);
        //when(serviceProvider.getDataProvider()).thenReturn(dataProvider);
        nNodeId = new NodeId("nSky");
        NetconfBindingAccessor bindingAccessor = mock(NetconfBindingAccessor.class);
        when(bindingAccessor.getNodeId()).thenReturn(nNodeId);
        when(bindingAccessor.getCapabilites()).thenReturn(capabilities);

        when(accessor.getNodeId()).thenReturn(nNodeId);
        when(accessor.getNetconfBindingAccessor()).thenReturn(Optional.of(bindingAccessor));
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(NetworkElement.QNAME)).thenReturn(true);

    }

    @AfterClass
    public static void after() {
        configFile.delete();
    }

    @Test
    public void testCreateMWModelRev170324() throws Exception {
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev170324.QNAME))
                .thenReturn(true);
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev180907.QNAME))
                .thenReturn(false);
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev181010.QNAME))
                .thenReturn(false);
        ONFCoreNetworkElementFactory factory = new ONFCoreNetworkElementFactory(configuration);
        assertTrue(factory.create(accessor, serviceProvider).isPresent());
    }

    @Test
    public void testCreateMWModelRev180907() throws Exception {
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev170324.QNAME))
                .thenReturn(false);
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev180907.QNAME))
                .thenReturn(true);
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev181010.QNAME))
                .thenReturn(false);
        ONFCoreNetworkElementFactory factory = new ONFCoreNetworkElementFactory(configuration);
        assertTrue(factory.create(accessor, serviceProvider).isPresent());
    }

    @Test
    public void testCreateMWModelRev181010() throws Exception {
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev170324.QNAME))
                .thenReturn(false);
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev180907.QNAME))
                .thenReturn(false);
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev181010.QNAME))
                .thenReturn(true);
        ONFCoreNetworkElementFactory factory = new ONFCoreNetworkElementFactory(configuration);
        assertTrue(factory.create(accessor, serviceProvider).isPresent());
    }

    @After
    public void cleanUp() throws Exception {

    }
}

