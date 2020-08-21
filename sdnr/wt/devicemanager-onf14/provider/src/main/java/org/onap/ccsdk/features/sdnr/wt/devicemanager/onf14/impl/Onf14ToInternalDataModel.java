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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.Equipment;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.EquipmentInstance;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.EquipmentType;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.ManufacturedThing;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.ManufacturerProperties;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.equipment.ActualEquipment;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.equipment.ContainedHolder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.InventoryBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author alexs
 *
 */
public class Onf14ToInternalDataModel {

    private static final Logger log = LoggerFactory.getLogger(Onf14ToInternalDataModel.class);

    public Inventory getInternalEquipment(NodeId nodeId, Equipment currentEq, Equipment parentEq, long treeLevel) {

        InventoryBuilder inventoryBuilder = new InventoryBuilder();

        @Nullable
        ActualEquipment component = currentEq.getActualEquipment();
        if (component != null) {

            // General
            inventoryBuilder.setNodeId(nodeId.getValue());

            inventoryBuilder.setTreeLevel(treeLevel);
            inventoryBuilder.setUuid(currentEq.getUuid().getValue());

            if (parentEq != null) {
                inventoryBuilder.setParentUuid(parentEq.getUuid().getValue());
            } else {
                inventoryBuilder.setParentUuid("None");
            }

            List<String> containedHolderKeyList = new ArrayList<String>();
            @NonNull
            List<ContainedHolder> containedHolderList = currentEq.nonnullContainedHolder();
            for (ContainedHolder holder : containedHolderList) {
                @Nullable
                UniversalId occupyingFru = holder.getOccupyingFru();

                if (occupyingFru != null) {
                    containedHolderKeyList.add(occupyingFru.getValue());
                }
            }
            inventoryBuilder.setContainedHolder(containedHolderKeyList);

            @Nullable
            ManufacturedThing manThing = component.getManufacturedThing();
            if (manThing != null) {
                // Manufacturer properties
                @Nullable
                ManufacturerProperties manProp = manThing.getManufacturerProperties();
                if (manProp != null) {
                    inventoryBuilder.setManufacturerName(manProp.getManufacturerName());
                    inventoryBuilder.setManufacturerIdentifier(manProp.getManufacturerIdentifier());
                } else {
                    log.debug("manufacturer-properties is not present in Equipment with uuid={}",
                            currentEq.getUuid().getValue());
                }

                // Equipment instance
                @Nullable
                EquipmentInstance eqInstance = manThing.getEquipmentInstance();
                if (eqInstance != null) {
                    inventoryBuilder.setSerial(eqInstance.getSerialNumber());
                    inventoryBuilder.setDate(eqInstance.getManufactureDate().getValue());
                } else {
                    log.debug("equipment-instance is not present in Equipment with uuid={}",
                            currentEq.getUuid().getValue());
                }

                // Equipment type
                @Nullable
                EquipmentType eqType = manThing.getEquipmentType();
                if (eqType != null) {
                    inventoryBuilder.setVersion(eqType.getVersion());
                    inventoryBuilder.setDescription(eqType.getDescription());
                    inventoryBuilder.setPartTypeId(eqType.getPartTypeIdentifier());
                    inventoryBuilder.setModelIdentifier(eqType.getModelIdentifier());
                    inventoryBuilder.setTypeName(eqType.getTypeName());
                } else {
                    log.debug("equipment-type is not present in Equipment with uuid={}",
                            currentEq.getUuid().getValue());
                }
            } else {
                log.debug("manufactured-thing is not present in Equipment with uuid={}",
                        currentEq.getUuid().getValue());
            }
        } else {
            log.debug("actual-equipment is not present in Equipment with uuid={}", currentEq.getUuid().getValue());
        }

        return inventoryBuilder.build();
    }

}
