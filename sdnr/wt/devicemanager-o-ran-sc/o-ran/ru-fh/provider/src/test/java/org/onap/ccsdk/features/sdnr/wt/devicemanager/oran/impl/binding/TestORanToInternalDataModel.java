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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.Component;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestORanToInternalDataModel {

    private static final Logger LOG = LoggerFactory.getLogger(TestORanToInternalDataModel.class);

    NodeId nodeId = new NodeId("ORan-1000");

    @Test
    public void testInventory() {
        String dateTimeString = "2020-02-05T12:30:45.283Z";
        String name = "Slot-0";

        Component testComponent = ComponentHelper.get(name, dateTimeString);
        Optional<Inventory> oInventory = ORanToInternalDataModel.getInternalEquipment(nodeId, testComponent, 0);

        assertTrue(oInventory.isPresent());
        Inventory inventory = oInventory.get();
        assertEquals(name, inventory.getUuid());
        assertEquals(dateTimeString, inventory.getDate());
        assertEquals(nodeId.getValue(), inventory.getNodeId());
    }

    @Test
    public void testInventoryList() throws IOException, ClassNotFoundException {
        List<Component> componentList = ComponentHelper.getComponentList("/Device-ietf-hardware-Output.json");
        List<Inventory> inventoryList = ORanToInternalDataModel.getInventoryList(nodeId, componentList);
        //componentList.forEach(System.out::println);
        assertEquals("All elements", 27, inventoryList.size());
        assertEquals("Fully parseable", componentList.size(), inventoryList.size());
        assertEquals("Treelevel always there", 0,
                inventoryList.stream().filter(inventory -> inventory.getTreeLevel() == null).count());
        listAsTree(inventoryList);
    }

    private void listAsTree(List<Inventory> inventoryList) {
        //Walk through complete list and print parameters
        IntConsumer action = level -> IntStream.range(0, inventoryList.size())
                .filter(idx -> inventoryList.get(idx).getTreeLevel().intValue() == level)
                .forEach(idx2 -> printElements(idx2, level, inventoryList.get(idx2)));
        //Walk trough 10 levels
        IntStream.range(0, 10)
                .forEach(action);
    }

    private void printElements(int idx2, int level, Inventory inventory) {
        System.out.println(level + ": " + inventory.getParentUuid() + " "
                + inventory.getUuid());
    }

    @SuppressWarnings("unused")
    private boolean compareLevel(int idx, List<Component> componentList, List<Inventory> inventoryList) {
        @Nullable
        Integer relPos = componentList.get(idx).getParentRelPos();
        @Nullable
        Uint32 treeLevel = inventoryList.get(idx).getTreeLevel();
        LOG.warn("Treelevel relPos: {} treeLevel: {}", relPos, treeLevel);
        if (relPos != null && treeLevel != null) {
            return relPos == treeLevel.intValue();
        }
        return false;
    }
}
