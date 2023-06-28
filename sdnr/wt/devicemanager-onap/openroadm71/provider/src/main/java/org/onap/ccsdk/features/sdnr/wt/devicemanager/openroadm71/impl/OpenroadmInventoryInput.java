/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm71.impl;

import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.OrgOpenroadmDevice;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.circuit.packs.CircuitPacks;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.interfaces.grp.Interface;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.org.openroadm.device.Info;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.org.openroadm.device.Xponder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.shelves.Shelves;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shabnam Sultana
 *
 *         Reading the inventory data of an open roadm device
 *
 **/
public class OpenroadmInventoryInput {
    // variable
    private static final Logger log = LoggerFactory.getLogger(OpenroadmInventoryInput.class);
    private static final String NOT_AVAILABLE = "N/A";
    private OrgOpenroadmDevice openRoadmDevice;
    private final NetconfAccessor accessor;
    // end of variables

    // constructors
    public OpenroadmInventoryInput(NetconfAccessor netconfAccessor, OrgOpenroadmDevice roadmDevice) {
        this.openRoadmDevice = roadmDevice;
        this.accessor = netconfAccessor;
    }
    // end of constructors

    // public methods
    public Inventory getInventoryData(Uint32 treeLevel) {
        InventoryBuilder inventoryBuilder = new InventoryBuilder();
        log.debug("Info for device {}", this.openRoadmDevice.getInfo().getNodeId().getValue());
        Info info = this.openRoadmDevice.getInfo();
        inventoryBuilder.setNodeId(this.accessor.getNodeId().getValue())
                .setUuid(info.getNodeId() == null ? NOT_AVAILABLE : info.getNodeId().getValue())
                .setDate(info.getCurrentDatetime() != null ? info.getCurrentDatetime().getValue() : null)
                .setId(this.accessor.getNodeId().getValue() + "/" + info.getNodeId().getValue())
                .setManufacturerIdentifier(info.getVendor()).setModelIdentifier(info.getModel())
                .setSerial(info.getSerialId()).setTreeLevel(treeLevel)
                .setVersion(info.getOpenroadmVersion() != null ? info.getOpenroadmVersion().getName() : null)
                .setDescription("org-openroadm-device").setParentUuid("None").setTypeName(info.getNodeType().getName())
                .setPartTypeId("device");
        log.debug("Inventory data written for device {}", this.openRoadmDevice.getInfo().getNodeId().getValue());
        return inventoryBuilder.build();
    }

    public Inventory getShelvesInventory(Shelves shelf, Uint32 treeLevel) {
        InventoryBuilder inventoryBuilder = new InventoryBuilder();
        inventoryBuilder.setNodeId(this.accessor.getNodeId().getValue())
                .setId(this.accessor.getNodeId().getValue() + "/" + shelf.getShelfName())
                .setDescription((shelf.getUserDescription() == null)
                        ? ("Position: " + shelf.getShelfPosition() + "\nState: " + shelf.getOperationalState())
                        : (shelf.getUserDescription()) + "\nPosition: " + shelf.getShelfPosition() + "\nState: "
                                + shelf.getOperationalState())
                .setSerial(shelf.getSerialId()).setUuid(shelf.getShelfName())
                .setParentUuid(this.openRoadmDevice.getInfo().getNodeId().getValue()).setTreeLevel(treeLevel)
                .setTypeName(shelf.getShelfType()).setPartTypeId(shelf.getClei())
                .setManufacturerIdentifier(shelf.getVendor()).setModelIdentifier(shelf.getModel())
                .setVersion(shelf.getHardwareVersion())
                .setDate(shelf.getManufactureDate() != null ? shelf.getManufactureDate().getValue() : NOT_AVAILABLE);
        log.debug("Inventory data written for Shelf {}", shelf.getShelfName());
        return inventoryBuilder.build();
    }

    public Inventory getInterfacesInventory(Interface deviceInterface, Uint32 treeLevel) {
        InventoryBuilder inventoryBuilder = new InventoryBuilder();
        inventoryBuilder.setNodeId(this.accessor.getNodeId().getValue())
                .setId(this.accessor.getNodeId().getValue() + "/" + deviceInterface.getName())
                .setDescription(
                        (deviceInterface.getDescription() == null) ? NOT_AVAILABLE : deviceInterface.getDescription())
                .setUuid(deviceInterface.getName()).setSerial(deviceInterface.getName())
                .setParentUuid((deviceInterface.getSupportingCircuitPackName() != null)
                        ? deviceInterface.getSupportingCircuitPackName()
                        // : ((deviceInterface.getSupportingInterface() != null) ?
                        // deviceInterface.getSupportingInterface()
                        : this.openRoadmDevice.getInfo().getNodeId().getValue())
                .setTreeLevel(treeLevel)
                .setTypeName((deviceInterface.getType() == null) ? "Interface"
                        : deviceInterface.getType().getClass().getSimpleName())
                .setPartTypeId("Interface").setManufacturerIdentifier(this.openRoadmDevice.getInfo().getVendor())
                .setModelIdentifier(this.openRoadmDevice.getInfo().getModel()).setVersion("N/A")
                .setDate(this.openRoadmDevice.getInfo().getCurrentDatetime().getValue());
        log.debug("Inventory data written for Interface {}", deviceInterface.getName());

        return inventoryBuilder.build();
    }

    public Inventory getCircuitPackInventory(CircuitPacks circuitPack, Uint32 treeLevel) {
        InventoryBuilder inventoryBuilder = new InventoryBuilder();
        inventoryBuilder.setNodeId(this.accessor.getNodeId().getValue()).setUuid(circuitPack.getCircuitPackName())
                .setDate((circuitPack.getManufactureDate() == null) ? NOT_AVAILABLE
                        : circuitPack.getManufactureDate().getValue().substring(0, 19))
                .setId(this.accessor.getNodeId().getValue() + "/" + circuitPack.getCircuitPackName())
                .setManufacturerIdentifier(circuitPack.getVendor()).setModelIdentifier(circuitPack.getModel())
                .setSerial(circuitPack.getSerialId()).setTreeLevel(treeLevel)
                .setVersion(circuitPack.getHardwareVersion())
                .setDescription("ProductCode: " + circuitPack.getProductCode() + "  " + "Mode: "
                        + circuitPack.getCircuitPackMode())
                .setTypeName((circuitPack.getType() == null) ? circuitPack.getCircuitPackType() : circuitPack.getType())
                .setPartTypeId((circuitPack.getClei() == null) ? circuitPack.getType() : circuitPack.getClei())
                .setParentUuid((circuitPack.getParentCircuitPack() != null)
                        ? circuitPack.getParentCircuitPack().getCircuitPackName()
                        : ((circuitPack.getShelf() != null) ? circuitPack.getShelf()
                                : this.openRoadmDevice.getInfo().getNodeId().getValue()));
        log.debug("Inventory data written for CircuitPack {}", circuitPack.getCircuitPackName());

        return inventoryBuilder.build();
    }

    public Inventory getXponderInventory(Xponder xpdr, Uint32 treeLevel) {
        InventoryBuilder inventoryBuilder = new InventoryBuilder();
        inventoryBuilder.setNodeId(this.accessor.getNodeId().getValue())
                .setId(this.accessor.getNodeId().getValue() + "/" + xpdr.getXpdrNumber().toString())
                .setDescription("Xponder\nLifecycleState: " + xpdr.getLifecycleState().getName())
                .setUuid(xpdr.getXpdrNumber().toString()).setSerial(xpdr.getXpdrNumber().toString())
                .setParentUuid(this.openRoadmDevice.getInfo().getNodeId().getValue()).setTreeLevel(treeLevel)
                .setTypeName(xpdr.getXpdrType().getName()).setPartTypeId(xpdr.getXpdrType().getName())
                .setManufacturerIdentifier(this.openRoadmDevice.getInfo().getVendor())
                .setModelIdentifier(this.openRoadmDevice.getInfo().getModel())
                .setVersion(this.openRoadmDevice.getInfo().getOpenroadmVersion().getName())
                .setDate(this.openRoadmDevice.getInfo().getCurrentDatetime().getValue());
        log.debug("Inventory data written for Xponder{}", xpdr.getXpdrNumber());

        return inventoryBuilder.build();
    }
    // end of public methods
}
