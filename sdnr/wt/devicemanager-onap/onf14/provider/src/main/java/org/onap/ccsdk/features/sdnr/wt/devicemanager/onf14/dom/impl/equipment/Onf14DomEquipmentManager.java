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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.equipment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.dataprovider.Onf14DomToInternalDataModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.yangspecs.CoreModel14;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Onf14DomEquipmentManager {

    private static final Logger log = LoggerFactory.getLogger(Onf14DomEquipmentManager.class);
    private static final int EQUIPMENTROOTLEVEL = 0;

    private final NetconfDomAccessor netconfDomAccessor;
    private final DataProvider databaseService;
    private final Onf14DomToInternalDataModel onf14Mapper;
    private final List<String> equipmentUuidList;
    private final CoreModel14 qNames;

    public Onf14DomEquipmentManager(NetconfDomAccessor netconfDomAccessor, DataProvider databaseService,
            Onf14DomToInternalDataModel onf14Mapper, CoreModel14 qNames) {
        super();
        this.netconfDomAccessor = Objects.requireNonNull(netconfDomAccessor);
        this.databaseService = Objects.requireNonNull(databaseService);
        this.onf14Mapper = Objects.requireNonNull(onf14Mapper);
        this.qNames = qNames;
        this.equipmentUuidList = new ArrayList<>();
    }

    // public methods
    public List<String> getEquipmentUuidList() {
        return equipmentUuidList;
    }

    /**
     * Set all equipment data from controlConstruct into database and into this manager.
     *
     * @param controlConstruct with complete device data
     */
    public void setEquipmentData(NormalizedNode controlConstruct) {
        Objects.requireNonNull(controlConstruct);

        // the top-level-equipment list contains the root objects of the Equipment Model
        log.debug("Iterating through the list of topLevelEquipment for mountpoint {}", netconfDomAccessor.getNodeId());
        // adding all root Equipment objects to the DB
        List<Inventory> inventoryList = new ArrayList<>();
        for (String uuid : getTopLevelEquipment(controlConstruct)) {
            Optional<NormalizedNode> equipment = readEquipmentInstance(netconfDomAccessor, uuid);
            MapEntryNode equipmentEntry = (MapEntryNode) equipment.get();
            if (equipmentEntry != null) {
                collectEquipment(inventoryList, equipmentEntry, null, EQUIPMENTROOTLEVEL);
            }
        }
        this.databaseService.writeInventory(netconfDomAccessor.getNodeId().getValue(), inventoryList);

    }

    private List<String> getTopLevelEquipment(NormalizedNode transformedInput) {
        List<String> topLvlEqptList = new ArrayList<>();
        Collection<?> topLevelEqptListColl = (Collection<?>) transformedInput.body();
        Iterator<?> childEntryItr = topLevelEqptListColl.iterator();
        while (childEntryItr.hasNext()) {
            LeafSetEntryNode<?> childEntryNode = (LeafSetEntryNode<?>) childEntryItr.next();
            topLvlEqptList.add((String) childEntryNode.body());
        }
        return topLvlEqptList;
    }

    /**
     * @param accessData to access device
     * @param equipmentUuid uuid of equipment to be read
     * @return Optional Equipment
     */
    private Optional<NormalizedNode> readEquipmentInstance(NetconfDomAccessor accessData, String equipmentUuid) {

        log.debug("DBRead Get equipment from mountpoint {} for uuid {}", accessData.getNodeId().getValue(),
                equipmentUuid);

        InstanceIdentifierBuilder equipmentIIDBuilder =
                YangInstanceIdentifier.builder().node(qNames.getQName("control-construct"))
                        .node(qNames.getQName("equipment")).nodeWithKey(qNames.getQName("equipment"),
                                QName.create(qNames.getQName("equipment"), "uuid").intern(), equipmentUuid);

        return accessData.readDataNode(LogicalDatastoreType.CONFIGURATION, equipmentIIDBuilder.build());
    }

    private List<Inventory> collectEquipment(List<Inventory> list, MapEntryNode currentEq, MapEntryNode parentEq,
            long treeLevel) {

        // if the Equipment UUID is already in the list, it was already processed
        // needed for solving possible circular dependencies
        if (equipmentUuidList.contains(Onf14DMDOMUtility.getUuidFromEquipment(currentEq, qNames.getQName("uuid")))) {
            log.debug("Not adding equipment with uuid {} because it was aleady added...",
                    Onf14DMDOMUtility.getUuidFromEquipment(currentEq, qNames.getQName("uuid")));
            return list;
        }

        // we add this to our internal list, such that we avoid circular dependencies
        equipmentUuidList.add(Onf14DMDOMUtility.getUuidFromEquipment(currentEq, qNames.getQName("uuid")));
        log.debug("Adding equipment with uuid {} to the database...",
                Onf14DMDOMUtility.getUuidFromEquipment(currentEq, qNames.getQName("uuid")));

        // we add our current equipment to the database
        list.add(onf14Mapper.getInternalEquipment(netconfDomAccessor.getNodeId(), currentEq, parentEq, treeLevel,
                qNames));

        // we iterate the kids of our current equipment and add them to the database
        // recursively
        // the actual reference is here:
        // /core-model:control-construct/equipment/contained-holder/occupying-fru

        MapNode containedHolderMap =
                (MapNode) currentEq.childByArg(new NodeIdentifier(qNames.getQName("contained-holder")));
        if (containedHolderMap != null) {
            Collection<MapEntryNode> containedHolderCollection = containedHolderMap.body();
            for (MapEntryNode holder : containedHolderCollection) {
                String occupyingFru = Onf14DMDOMUtility.getLeafValue(holder,
                        qNames.getQName("occupying-fru")/*Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_OCCUPYING_FRU*/);

                if (occupyingFru != null) {
                    Optional<NormalizedNode> childEq = readEquipmentInstance(netconfDomAccessor, occupyingFru);
                    if (childEq.isPresent()) {
                        // current becomes parent and tree level increases by 1
                        collectEquipment(list, (MapEntryNode) childEq.get(), currentEq, treeLevel + 1);
                    }
                }
            }
        }

        return list;
    }

}
