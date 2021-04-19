/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.hardware.rev180313.HardwareClass;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.Component;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.onap.system.rev201026.System1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Guicutthrough;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GuicutthroughBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert data to data-provider model and perform consistency checks.<br>
 * <b>Component list characteristics:</b><br>
 * <ul>
 * <li>component list is a flat list tree structure specified
 * <li>via "component.getParent()":
 * <ul>
 * <li>If null we have a root element
 * <li>if not null it is a child element with generated child level<br>
 * </ul>
 * </ul>
 * Example of List:<br>
 *
 *
 */
public class ORanToInternalDataModel {

    private static final Logger log = LoggerFactory.getLogger(ORanToInternalDataModel.class);

    public static List<Inventory> getInventoryList(NodeId nodeId, Collection<Component> componentList) {

        List<Inventory> inventoryResultList = new ArrayList<Inventory>();
        for (Component component : getRootComponents(componentList)) {
            inventoryResultList = recurseGetInventory(nodeId, component, componentList, 0, inventoryResultList);
        }
        // Verify if result is complete
        if (componentList.size() != inventoryResultList.size()) {
            log.warn(
                    "Not all data were written to the Inventory. Potential entries with missing "
                            + "contained-child. Node Id = {}, Components Found = {}, Entries written to Database = {}",
                    nodeId.getValue(), componentList.size(), inventoryResultList.size());
        }
        return inventoryResultList;
    }

    private static List<Inventory> recurseGetInventory(NodeId nodeId, Component component,
            Collection<Component> componentList, int treeLevel, List<Inventory> inventoryResultList) {

        //Add element to list, if conversion successfull
        Optional<Inventory> oInventory = getInternalEquipment(nodeId, component, treeLevel);
        if (oInventory.isPresent()) {
            inventoryResultList.add(oInventory.get());
        }
        //Walk trough list of child keys and add to list
        for (String childUuid : CodeHelpers.nonnull(component.getContainsChild())) {
            for (Component c : getComponentsByName(childUuid, componentList)) {
                inventoryResultList = recurseGetInventory(nodeId, c, componentList, treeLevel + 1, inventoryResultList);
            }
        }
        return inventoryResultList;
    }

    public static List<Component> getRootComponents(Collection<Component> componentList) {
        List<Component> resultList = new ArrayList<>();
        for (Component c : componentList) {
            if (c.getParent() == null) { // Root elements do not have a parent
                resultList.add(c);
            }
        }
        return resultList;
    }

    private static List<Component> getComponentsByName(String name, Collection<Component> componentList) {
        List<Component> resultList = new ArrayList<>();
        for (Component c : componentList) {
            if (name.equals(c.getName())) { // <-- Component list is flat search for child's of name
                resultList.add(c);
            }
        }
        return resultList;
    }

    /**
     * Convert equipment into Inventory. Decide if inventory can by created from content or not.
     * Public for test case.
     * @param nodeId of node (Similar to mountpointId)
     * @param component to handle
     * @param treeLevel of components
     * @return Inventory if possible to be created.
     */
    public static Optional<Inventory> getInternalEquipment(NodeId nodeId, Component component, int treeLevel) {

        // Make sure that expected data are not null
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(component);

        // Read manadatory data

        @Nullable
        String nodeIdString = nodeId.getValue();
        @Nullable
        String uuid = component.getName();
        @Nullable
        String idParent = component.getParent();
        @Nullable
        String uuidParent = idParent != null ? idParent : uuid; //<- Passt nicht

        // do consistency check if all mandatory parameters are there
        if (treeLevel >= 0 && nodeIdString != null && uuid != null && uuidParent != null) {

            // Build output data

            InventoryBuilder inventoryBuilder = new InventoryBuilder();

            // General assumed as mandatory
            inventoryBuilder.setNodeId(nodeIdString);
            inventoryBuilder.setUuid(uuid);
            inventoryBuilder.setParentUuid(uuidParent);
            inventoryBuilder.setTreeLevel(Uint32.valueOf(treeLevel));

            // -- String list with ids of holders (optional)
            inventoryBuilder.setContainedHolder(CodeHelpers.nonnull(component.getContainsChild()));

            // -- Manufacturer related things (optional)
            @Nullable
            String mfgName = component.getMfgName();
            inventoryBuilder.setManufacturerName(mfgName);
            inventoryBuilder.setManufacturerIdentifier(mfgName);

            // Equipment type (optional)
            inventoryBuilder.setDescription(component.getDescription());
            inventoryBuilder.setModelIdentifier(component.getModelName());
            @Nullable
            Class<? extends HardwareClass> xmlClass = component.getXmlClass();
            if (xmlClass != null) {
                inventoryBuilder.setPartTypeId(xmlClass.getName());
            }
            inventoryBuilder.setTypeName(component.getName());
            inventoryBuilder.setVersion(component.getHardwareRev());

            // Equipment instance (optional)
            @Nullable
            DateAndTime mfgDate = component.getMfgDate();
            if (mfgDate != null) {
                inventoryBuilder.setDate(mfgDate.getValue());
            }
            inventoryBuilder.setSerial(component.getSerialNum());

            return Optional.of(inventoryBuilder.build());
        }
        return Optional.empty();
    }

    public static Optional<Guicutthrough> getGuicutthrough(@Nullable System1 sys) {
        if (sys != null) {
            String name = sys.getName();
            @Nullable
            Uri uri = sys.getWebUi();
            if (uri != null) {
                GuicutthroughBuilder gcBuilder = new GuicutthroughBuilder();
                if (name != null) {
                    gcBuilder.setName(name);
                }
                gcBuilder.setWeburi(uri.getValue());
                return Optional.of(gcBuilder.build());
            }
            log.warn("Uri not set to invoke a Gui cut through session to the device. Please set the Uri in the device");
        }
        log.warn("Retrieving augmented System details failed. Gui cut through information not available");
        return Optional.empty();
    }

}
