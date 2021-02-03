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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.equipment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.dataprovider.Onf14ToInternalDataModel;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.ControlConstruct;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.Equipment;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.EquipmentKey;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.equipment.ContainedHolder;
import org.opendaylight.yangtools.util.UnmodifiableCollection;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Onf14DomEquipmentManager {


    // constants
    private static final Logger log = LoggerFactory.getLogger(Onf14DomEquipmentManager.class);
    private static final int EQUIPMENTROOTLEVEL = 0;
    // end of constants

    // variables
    private final NetconfDomAccessor netconfDomAccessor;
    private final DataProvider databaseService;
    private final Onf14ToInternalDataModel onf14Mapper;
    // for storing the Equipment UUIDs that are inserted in the DB
    private final List<UniversalId> equipmentUuidList = new ArrayList<>();
    // end of variables

    // constructors
    public Onf14DomEquipmentManager(NetconfDomAccessor netconfDomAccessor, DataProvider databaseService,
            Onf14ToInternalDataModel onf14Mapper) {
        super();
        this.netconfDomAccessor = Objects.requireNonNull(netconfDomAccessor);
        this.databaseService = Objects.requireNonNull(databaseService);
        this.onf14Mapper = Objects.requireNonNull(onf14Mapper);
    }
    // end of constructors

    // getters and setters
    public List<UniversalId> getEquipmentUuidList() {
        return equipmentUuidList;
    }
    // end of getters and setters

    // private methods
    private void addEquipmentToDb(Equipment currentEq, Equipment parentEq, long treeLevel,
            Map<EquipmentKey, Equipment> equipmentMap, EquipmentKey equipmentKey) {
        if (currentEq == null) {
            log.info("Ignore empty equipment with key {}", equipmentKey);
            return;
        }

        // if the Equipment UUID is already in the list, it was already processed
        // needed for solving possible circular dependencies
        if (equipmentUuidList.contains(currentEq.getUuid())) {
            log.debug("Not adding equipment with uuid {} because it was aleady added...",
                    currentEq.getUuid().getValue());
            return;
        }

        // we add this to our internal list, such that we avoid circular dependencies
        equipmentUuidList.add(currentEq.getUuid());
        log.debug("Adding equipment with uuid {} to the database...", currentEq.getUuid().getValue());

        // we add our current equipment to the database
        databaseService.writeInventory(
                onf14Mapper.getInternalEquipment(netconfDomAccessor.getNodeId(), currentEq, parentEq, treeLevel));

        // we iterate the kids of our current equipment and add them to the database recursively
        // the actual reference is here: /core-model:control-construct/equipment/contained-holder/occupying-fru
        for (ContainedHolder holder : YangHelper.getCollection(currentEq.nonnullContainedHolder())) {
            @Nullable
            UniversalId occupyingFru = holder.getOccupyingFru();
            if (occupyingFru != null) {
                equipmentKey = new EquipmentKey(occupyingFru);
                addEquipmentToDb(equipmentMap.get(equipmentKey), currentEq, treeLevel + 1, equipmentMap, equipmentKey);
            }
        }
    }
    // end of private methods

    // public methods
    /**
     * Set all equipment data from controlConstruct into database and into this manager.
     *
     * @param controlConstruct with complete device data
     */
    public void setEquipmentData(ControlConstruct controlConstruct) {
        Objects.requireNonNull(controlConstruct);

        // the top-level-equipment list contains the root objects of the Equipment Model
        log.debug("Getting list of topLevelEquipment for mountpoint {}", netconfDomAccessor.getNodeId());
        // adding all root Equipment objects to the DB
        for (UniversalId uuid : CodeHelpers.nonnull(controlConstruct.getTopLevelEquipment())) {
            log.debug("Got back topLevelEquipment with uuid {}", uuid.getValue());
            EquipmentKey equipmentKey = new EquipmentKey(uuid);

            // adding all root Equipment objects to the DB
            Map<EquipmentKey, Equipment> equipmentMap = controlConstruct.nonnullEquipment();
            // recursively adding the root equipment and all its children into the DB
            addEquipmentToDb(equipmentMap.get(equipmentKey), null, EQUIPMENTROOTLEVEL, equipmentMap, equipmentKey);
        }
    }

    /**
     * Read one equipment from device
     *
     * @param accessData to access device
     * @param equipmentUuid uuid of equipment to be read
     * @return Optional Equipment
     */
    public Optional<Equipment> readEquipmentInstance(NetconfDomAccessor accessData, UniversalId equipmentUuid) {

        final Class<?> clazzPac = Equipment.class;

        log.info("DBRead Get equipment for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                accessData.getNodeId().getValue(), equipmentUuid.getValue());

        InstanceIdentifierBuilder equipmentIIDBuilder =
                YangInstanceIdentifier.builder().node(ControlConstruct.QNAME).node(Equipment.QNAME).nodeWithKey(
                        Equipment.QNAME, QName.create(Equipment.QNAME, "uuid").intern(), equipmentUuid.getValue());

        return accessData.readData(LogicalDatastoreType.CONFIGURATION, equipmentIIDBuilder.build(), Equipment.class);
    }

    /**
     * Read one equipment list from device
     *
     * @param accessData to access device
     * @param equipmentUuid uuid of equipment to be read
     * @return Optional Equipment
     */
    public Optional<ControlConstruct> readEquipmentList(NetconfDomAccessor accessData, UniversalId equipmentUuid) {

        log.info("DBRead Get equipment-list for mountpoint {} for uuid {}", accessData.getNodeId().getValue(),
                equipmentUuid.getValue());

        YangInstanceIdentifier equipmentIIDBuilder = YangInstanceIdentifier.builder()
        		.node(ControlConstruct.QNAME)
        		.node(Equipment.QNAME)
        		.node(NodeIdentifierWithPredicates.of(Equipment.QNAME))
        		.build();

        return accessData.readData(LogicalDatastoreType.CONFIGURATION, equipmentIIDBuilder,
                ControlConstruct.class);
    }

    /**
     * Read one equipment list from device
     *
     * @param accessData to access device
     * @param equipmentUuid uuid of equipment to be read
     * @return Optional Equipment
     */
    public void readTopLevelEquipment(NetconfDomAccessor accessData) {

        log.info("DBRead Get top-level-equipment for mountpoint {}", accessData.getNodeId().getValue());

        InstanceIdentifierBuilder equipmentIIDBuilder = YangInstanceIdentifier.builder().node(ControlConstruct.QNAME)
                .node(QName.create(ControlConstruct.QNAME, "top-level-equipment"));

        Optional<NormalizedNode<?, ?>> oData =
                accessData.readDataNode(LogicalDatastoreType.CONFIGURATION, equipmentIIDBuilder.build());
        NormalizedNode<?, ?> data = oData.get();
        Object value = data.getValue();
        log.info("DataNode: {} {}", data.getNodeType(), data.getIdentifier());
        if (value != null) {
            log.info("DataNode value: {} {}", value.getClass().getName(), value);
            if (value instanceof UnmodifiableCollection) {
                @SuppressWarnings("unchecked")
                UnmodifiableCollection<LeafSetEntryNode<String>> topLevelEquipmentCollection = (UnmodifiableCollection<LeafSetEntryNode<String>>) value;
                @NonNull
                Iterator<LeafSetEntryNode<String>> it = topLevelEquipmentCollection.iterator();
                while (it.hasNext()) {
                    LeafSetEntryNode<String> topLevelEquipmentUuid = it.next();
                    if (topLevelEquipmentUuid != null) {
                        log.info("LeafSetEntryNode: {} {} {}", topLevelEquipmentUuid.getValue(), topLevelEquipmentUuid.getNodeType() ,topLevelEquipmentUuid.getValue().getClass().getName());
                    }
                }
            }
        }
    }
    // end of public methods

    // static methods
    // end of static methods

    // private classes
    // end of private classes
}
