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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.equipment;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.Equipment;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.equipment.g.ContainedHolder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.equipment.g.ManufacturedThing;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.manufactured.thing.g.EquipmentInstance;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.manufactured.thing.g.EquipmentType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.manufactured.thing.g.ManufacturerProperties;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend the eqipment type of core model with additional parameters
 */
public class ExtendedEquipment {

    private static final Logger LOG = LoggerFactory.getLogger(ExtendedEquipment.class);

    public static final String ESDATATYPENAME = "inventoryequipment";
    private final String parentUuid;
    private final int treeLevel;
    private final Equipment equipment;
    private final String nodeId;
    private final String path;

    /**
     * Equipment with additional information beside NETCONF equipment
     *
     * @param parentUuid of parent equipment
     * @param equipment NETCONF Equipment
     * @param treeLevel level of tree starting with root at 0
     */
    public ExtendedEquipment(String nodeId, String parentUuid, Equipment equipment, String path, int treeLevel) {
        super();
        this.nodeId = nodeId;
        this.parentUuid = parentUuid;
        this.equipment = equipment;
        this.path = path;
        this.treeLevel = treeLevel;
    }

    public String getParentUuid() {
        return parentUuid;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public int getTreeLevel() {
        return treeLevel;
    }

    public String getNodeId() {
        return nodeId;
    }

    public Inventory getCreateInventoryInput() {

        InventoryBuilder inventoryBuilder = new InventoryBuilder();

        // General
        inventoryBuilder.setNodeId(getNodeId());
        inventoryBuilder.setParentUuid(getParentUuid());
        inventoryBuilder.setTreeLevel(Uint32.valueOf(getTreeLevel()));

        if (getEquipment() != null) {
            inventoryBuilder.setUuid(getEquipment().getUuid().getValue());
            // -- String list with ids of holders
            Set<String> containerHolderKeyList = new HashSet<>();
            Collection<ContainedHolder> containerHolderList = YangHelper.getCollection(getEquipment().getContainedHolder());
            if (containerHolderList != null) {
                for (ContainedHolder containerHolder : containerHolderList) {
                    containerHolderKeyList.add(containerHolder.getUuid().getValue());
                }
            }
            inventoryBuilder.setContainedHolder(containerHolderKeyList);

            // -- Manufacturer related things
            ManufacturedThing mThing = getEquipment().getManufacturedThing();
            if (mThing != null) {
                ManufacturerProperties mProperties = mThing.getManufacturerProperties();
                if (mProperties != null) {
                    inventoryBuilder.setManufacturerName(mProperties.getManufacturerName());
                    inventoryBuilder.setManufacturerIdentifier(mProperties.getManufacturerIdentifier());
                }
                EquipmentType mType = mThing.getEquipmentType();
                if (mType != null) {
                    inventoryBuilder.setDescription(mType.getDescription());
                    inventoryBuilder.setModelIdentifier(mType.getModelIdentifier());
                    inventoryBuilder.setPartTypeId(mType.getPartTypeIdentifier());
                    inventoryBuilder.setTypeName(mType.getTypeName());
                    inventoryBuilder.setVersion(mType.getVersion());
                }
                EquipmentInstance mInstance = mThing.getEquipmentInstance();
                if (mInstance != null) {
                    String manufacturedDateString = mInstance.getManufactureDate();
                    if (manufacturedDateString != null && !manufacturedDateString.isEmpty()) {
                        try {
                            inventoryBuilder.setDate(manufacturedDateString);
                        } catch (IllegalArgumentException e) {
                            LOG.debug("Format problem", e);
                        }
                    }
                    inventoryBuilder.setSerial(mInstance.getSerialNumber());
                }
            }
        }

        return inventoryBuilder.build();
    }

    @Override
    public String toString() {
        return "ExtendedEquipment [parentUuid=" + parentUuid + ", treeLevel=" + treeLevel + ", equipment=" + equipment
                + ", nodeId=" + nodeId + ", path=" + path + "]";
    }

}
