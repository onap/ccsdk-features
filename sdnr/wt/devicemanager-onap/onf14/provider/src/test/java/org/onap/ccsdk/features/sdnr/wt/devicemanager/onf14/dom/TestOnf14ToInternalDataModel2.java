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
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.dataprovider.Onf14DomToInternalDataModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DevicemanagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.util.Onf14DomTestUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.xml.sax.SAXException;

public class TestOnf14ToInternalDataModel2 extends Mockito {

    private static NodeId nodeId;

    @BeforeClass
    public static void init() {
        nodeId = mock(NodeId.class);
    }

    @After
    public void cleanUp() {
        Onf14DomTestUtils.cleanup();
    }

    @Test
    public void testWithNormalizedNodeFromJson() throws IOException, URISyntaxException {

        Onf14DomToInternalDataModel model = new Onf14DomToInternalDataModel();

        ContainerNode cn = (ContainerNode) Onf14DomTestUtils.getNormalizedNodeFromJson();
        MapNode equipmentMap =
                (MapNode) cn.getChildByArg(new NodeIdentifier(Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT));
        List<Inventory> inventoryList = new ArrayList<Inventory>();

        Collection<MapEntryNode> containerMapEntries = equipmentMap.body();
        for (MapEntryNode mapEntryNode : containerMapEntries) {
            inventoryList.add(model.getInternalEquipment(new NodeId("nSky"), mapEntryNode, null, 0));
        }
        assertEquals("All elements", 1, inventoryList.size());

    }

    @Test
    public void testWithNormalizedNodeFromXML()
            throws IOException, URISyntaxException, XMLStreamException, SAXException {

        Onf14DomToInternalDataModel model = new Onf14DomToInternalDataModel();

        ContainerNode cn = (ContainerNode) Onf14DomTestUtils.getNormalizedNodeFromXML();
        MapNode equipmentMap =
                (MapNode) cn.getChildByArg(new NodeIdentifier(Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT));
        List<Inventory> inventoryList = new ArrayList<Inventory>();

        Collection<MapEntryNode> containerMapEntries = equipmentMap.body();
        for (MapEntryNode mapEntryNode : containerMapEntries) {
            inventoryList.add(model.getInternalEquipment(new NodeId("nSky"), mapEntryNode, null, 0));
        }
        assertEquals("All elements", 1, inventoryList.size());

    }

}
