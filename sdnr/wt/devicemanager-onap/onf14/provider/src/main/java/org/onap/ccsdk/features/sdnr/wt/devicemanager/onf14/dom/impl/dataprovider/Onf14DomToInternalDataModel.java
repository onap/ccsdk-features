package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.dataprovider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.yangspecs.CoreModel14;
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

    public Inventory getInternalEquipment(NodeId nodeId, MapEntryNode currentEq, MapEntryNode parentEq, long treeLevel,
            CoreModel14 qNames) {

        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(currentEq);

        InventoryBuilder inventoryBuilder = new InventoryBuilder();
        String parentUuid =
                parentEq != null ? Onf14DMDOMUtility.getUuidFromEquipment(parentEq, qNames.getQName("uuid")) : "None";

        // General
        inventoryBuilder.setNodeId(nodeId.getValue());
        inventoryBuilder.setTreeLevel(Uint32.valueOf(treeLevel));
        inventoryBuilder.setUuid(Onf14DMDOMUtility.getUuidFromEquipment(currentEq, qNames.getQName("uuid")));
        inventoryBuilder.setParentUuid(parentUuid);

        Set<String> containedHolderKeyList = new HashSet<>();
        MapNode containedHolderMap =
                (MapNode) currentEq.childByArg(new NodeIdentifier(qNames.getQName("contained-holder")));
        if (containedHolderMap != null) {
            Collection<MapEntryNode> containedHolderCollection = containedHolderMap.body();
            for (MapEntryNode holder : containedHolderCollection) {
                String occupyingFru = Onf14DMDOMUtility.getLeafValue(holder, qNames.getQName("occupying-fru"));

                if (occupyingFru != null) {
                    containedHolderKeyList.add(occupyingFru);
                }
            }
        }
        inventoryBuilder.setContainedHolder(containedHolderKeyList);

        // actual-equipment
        ContainerNode actualEquipment =
                (ContainerNode) currentEq.childByArg(new NodeIdentifier(qNames.getQName("actual-equipment")));
        if (actualEquipment != null) {
            ContainerNode manThing = (ContainerNode) actualEquipment
                    .childByArg(new NodeIdentifier(qNames.getQName("manufactured-thing")));
            if (manThing != null) {
                // Manufacturer properties
                ContainerNode props = (ContainerNode) manThing
                        .childByArg(new NodeIdentifier(qNames.getQName("manufacturer-properties")));
                if (props != null) {
                    inventoryBuilder.setManufacturerName(
                            Onf14DMDOMUtility.getLeafValue(props, qNames.getQName("manufacturer-name")));

                    inventoryBuilder.setManufacturerIdentifier(
                            Onf14DMDOMUtility.getLeafValue(props, qNames.getQName("manufacturer-identifier")));

                } else {
                    LOG.debug("manufacturer-properties is not present in Equipment with uuid={}",
                            Onf14DMDOMUtility.getUuidFromEquipment(currentEq, qNames.getQName("uuid")));
                }

                // Equipment instance
                ContainerNode equipmentInstance =
                        (ContainerNode) manThing.childByArg(new NodeIdentifier(qNames.getQName("equipment-instance")));
                if (equipmentInstance != null) {
                    inventoryBuilder.setSerial(
                            Onf14DMDOMUtility.getLeafValue(equipmentInstance, qNames.getQName("serial-number")));

                    inventoryBuilder.setDate(
                            Onf14DMDOMUtility.getLeafValue(equipmentInstance, qNames.getQName("manufactured-date")));

                } else {
                    LOG.debug("equipment-instance is not present in Equipment with uuid={}",
                            Onf14DMDOMUtility.getUuidFromEquipment(currentEq, qNames.getQName("uuid")));
                }

                // Equipment type
                ContainerNode equipmentType =
                        (ContainerNode) manThing.childByArg(new NodeIdentifier(qNames.getQName("equipment-type")));
                if (equipmentType != null) {
                    inventoryBuilder
                            .setVersion(Onf14DMDOMUtility.getLeafValue(equipmentType, qNames.getQName("version")));

                    inventoryBuilder.setDescription(
                            Onf14DMDOMUtility.getLeafValue(equipmentType, qNames.getQName("description")));

                    inventoryBuilder.setPartTypeId(
                            Onf14DMDOMUtility.getLeafValue(equipmentType, qNames.getQName("part-type-identifier")));

                    inventoryBuilder.setModelIdentifier(Onf14DMDOMUtility.getLeafValue(equipmentType,

                            qNames.getQName("model-identifier")));

                    inventoryBuilder
                            .setTypeName(Onf14DMDOMUtility.getLeafValue(equipmentType, qNames.getQName("type-name")));

                } else {
                    LOG.debug("equipment-type is not present in Equipment with uuid={}",
                            Onf14DMDOMUtility.getUuidFromEquipment(currentEq, qNames.getQName("uuid")));
                }
            } else {
                LOG.debug("manufactured-thing is not present in Equipment with uuid={}",
                        Onf14DMDOMUtility.getUuidFromEquipment(currentEq, qNames.getQName("uuid")));
            }
        } else {
            LOG.debug("actual-equipment is not present in Equipment with uuid={}",
                    Onf14DMDOMUtility.getUuidFromEquipment(currentEq, qNames.getQName("uuid")));
        }

        return inventoryBuilder.build();
    }
}
