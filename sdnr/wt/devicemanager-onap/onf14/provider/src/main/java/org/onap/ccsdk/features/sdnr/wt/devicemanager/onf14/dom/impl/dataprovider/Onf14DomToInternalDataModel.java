package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.dataprovider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DevicemanagerQNames;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Onf14DomToInternalDataModel {
    private static final Logger LOG = LoggerFactory.getLogger(Onf14DomToInternalDataModel.class);

    public Inventory getInternalEquipment(NodeId nodeId, MapEntryNode currentEq, MapEntryNode parentEq,
            long treeLevel) {

        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(currentEq);

        InventoryBuilder inventoryBuilder = new InventoryBuilder();
        String parentUuid = parentEq != null ? Onf14DMDOMUtility.getUuidFromEquipment(parentEq) : "None";

        // General
        inventoryBuilder.setNodeId(nodeId.getValue());
        inventoryBuilder.setTreeLevel(Uint32.valueOf(treeLevel));
        inventoryBuilder.setUuid(Onf14DMDOMUtility.getUuidFromEquipment(currentEq));
        inventoryBuilder.setParentUuid(parentUuid);

        Set<String> containedHolderKeyList = new HashSet<>();
        MapNode containedHolderMap = (MapNode) currentEq
                .childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_CONTAINED_HOLDER));
        if (containedHolderMap != null) {
            Collection<MapEntryNode> containedHolderCollection = containedHolderMap.body();
            for (MapEntryNode holder : containedHolderCollection) {
                String occupyingFru = Onf14DMDOMUtility.getLeafValue(holder,
                        Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_OCCUPYING_FRU);

                if (occupyingFru != null) {
                    containedHolderKeyList.add(occupyingFru);
                }
            }
        }
        inventoryBuilder.setContainedHolder(containedHolderKeyList);

        // actual-equipment
        ContainerNode actualEquipment = (ContainerNode) currentEq
                .childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_ACTUAL_EQUIPMENT));
        if (actualEquipment != null) {
            ContainerNode manThing = (ContainerNode) actualEquipment
                    .childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_MANUFACTURED_THING));
            if (manThing != null) {
                // Manufacturer properties
                ContainerNode props = (ContainerNode) manThing
                        .childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_MANUFACTURER_PROPS));
                if (props != null) {
                    inventoryBuilder.setManufacturerName(Onf14DMDOMUtility.getLeafValue(props,
                            Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_MANUFACTURER_NAME));

                    inventoryBuilder.setManufacturerIdentifier(Onf14DMDOMUtility.getLeafValue(props,
                            Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_MANUFACTURER_ID));

                } else {
                    LOG.debug("manufacturer-properties is not present in Equipment with uuid={}",
                            Onf14DMDOMUtility.getUuidFromEquipment(currentEq));
                }

                // Equipment instance
                ContainerNode equipmentInstance = (ContainerNode) manThing.childByArg(
                        new NodeIdentifier(Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_INSTANCE));
                if (equipmentInstance != null) {
                    inventoryBuilder.setSerial(Onf14DMDOMUtility.getLeafValue(equipmentInstance,
                            Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_INSTANCE_SERIAL_NUM));

                    inventoryBuilder.setDate(Onf14DMDOMUtility.getLeafValue(equipmentInstance,
                            Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_INSTANCE_MANUFACTURED_DATE));

                } else {
                    LOG.debug("equipment-instance is not present in Equipment with uuid={}",
                            Onf14DMDOMUtility.getUuidFromEquipment(currentEq));
                }

                // Equipment type
                ContainerNode equipmentType = (ContainerNode) manThing.childByArg(
                        new NodeIdentifier(Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_TYPE));
                if (equipmentType != null) {
                    inventoryBuilder.setVersion(Onf14DMDOMUtility.getLeafValue(equipmentType,
                            Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_TYPE_VERSION));

                    inventoryBuilder.setDescription(Onf14DMDOMUtility.getLeafValue(equipmentType,
                            Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_TYPE_DESCRIPTION));

                    inventoryBuilder.setPartTypeId(Onf14DMDOMUtility.getLeafValue(equipmentType,
                            Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_TYPE_PART_TYPE_ID));

                    inventoryBuilder.setModelIdentifier(Onf14DMDOMUtility.getLeafValue(equipmentType,

                            Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_TYPE_MODEL_ID));

                    inventoryBuilder.setTypeName(Onf14DMDOMUtility.getLeafValue(equipmentType,
                            Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_ACTUAL_EQPT_EQPT_TYPE_TYPE_NAME));

                } else {
                    LOG.debug("equipment-type is not present in Equipment with uuid={}",
                            Onf14DMDOMUtility.getUuidFromEquipment(currentEq));
                }
            } else {
                LOG.debug("manufactured-thing is not present in Equipment with uuid={}",
                        Onf14DMDOMUtility.getUuidFromEquipment(currentEq));
            }
        } else {
            LOG.debug("actual-equipment is not present in Equipment with uuid={}",
                    Onf14DMDOMUtility.getUuidFromEquipment(currentEq));
        }

        return inventoryBuilder.build();
    }
}
