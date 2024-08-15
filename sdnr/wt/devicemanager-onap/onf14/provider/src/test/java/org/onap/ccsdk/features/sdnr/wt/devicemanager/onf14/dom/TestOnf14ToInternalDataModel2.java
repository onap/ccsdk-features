/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.dataprovider.Onf14DomToInternalDataModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.yangspecs.CoreModel14;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.util.Onf14DomTestUtils;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.xml.sax.SAXException;

public class TestOnf14ToInternalDataModel2 extends Mockito {

    private static final QNameModule qnm =
            QNameModule.create(XMLNamespace.of("urn:onf:yang:core-model-1-4"), Revision.of("2019-11-27"));
    NetconfDomAccessor netconfDomAccessor;
    Capabilities capabilities;

    @Before
    public void prepare() {
        netconfDomAccessor = mock(NetconfDomAccessor.class);
        capabilities = mock(Capabilities.class);
        when(netconfDomAccessor.getCapabilites()).thenReturn(capabilities);
        when(capabilities.isSupportingNamespaceAndRevision(qnm)).thenReturn(true);
    }

    @After
    public void cleanUp() {
        Onf14DomTestUtils.cleanup();
    }

    @Test
    public void testWithNormalizedNodeFromJson() throws IOException, URISyntaxException {

        Onf14DomToInternalDataModel model = new Onf14DomToInternalDataModel();

        ContainerNode cn = (ContainerNode) Onf14DomTestUtils.getNormalizedNodeFromJson();
        System.out.println("Container Node = "+cn);
        MapNode equipmentMap = (MapNode) cn.getChildByArg(new NodeIdentifier(QName.create(qnm, "equipment")));
        List<Inventory> inventoryList = new ArrayList<Inventory>();

        Collection<MapEntryNode> containerMapEntries = equipmentMap.body();
        for (MapEntryNode mapEntryNode : containerMapEntries) {
            inventoryList.add(model.getInternalEquipment(new NodeId("nSky"), mapEntryNode, null, 0,
                    CoreModel14.getModule(netconfDomAccessor).get()));
        }
        assertEquals("All elements", 1, inventoryList.size());

    }

    @Test
    public void testWithNormalizedNodeFromXML()
            throws IOException, URISyntaxException, XMLStreamException, SAXException {

        Onf14DomToInternalDataModel model = new Onf14DomToInternalDataModel();

        ContainerNode cn = (ContainerNode) Onf14DomTestUtils.getNormalizedNodeFromXML();
        MapNode equipmentMap = (MapNode) cn.getChildByArg(new NodeIdentifier(QName.create(qnm, "equipment")));
        List<Inventory> inventoryList = new ArrayList<Inventory>();

        Collection<MapEntryNode> containerMapEntries = equipmentMap.body();
        for (MapEntryNode mapEntryNode : containerMapEntries) {
            inventoryList.add(model.getInternalEquipment(new NodeId("nSky"), mapEntryNode, null, 0,
                    CoreModel14.getModule(netconfDomAccessor).get()));
        }
        assertEquals("All elements", 1, inventoryList.size());

    }

}
