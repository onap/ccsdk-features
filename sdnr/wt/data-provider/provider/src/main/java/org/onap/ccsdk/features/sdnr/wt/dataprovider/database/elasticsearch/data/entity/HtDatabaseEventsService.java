/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Update Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
 * =================================================================================================
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.BoolQueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.RangeQueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.EsDataObjectReaderWriter2;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.ArchiveCleanProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataInconsistencyException;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GranularityPeriodType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Guicutthrough;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GuicutthroughBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GuicutthroughEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event service, writing all events into the database into the appropriate index.
 *
 * @author herbert
 */
public class HtDatabaseEventsService implements ArchiveCleanProvider, DataProvider {
    private static final Logger LOG = LoggerFactory.getLogger(HtDatabaseEventsService.class);

    private static final NetconfTimeStamp NETCONFTIME_CONVERTER = NetconfTimeStampImpl.getConverter();

    private static final int ROOT_TREE_LEVEL = 0;

    private HtDatabaseClient client;
    private EsDataObjectReaderWriter2<EventlogEntity> eventRWEventLogDevicemanager;
    private EsDataObjectReaderWriter2<InventoryEntity> eventRWEquipment;
    private EsDataObjectReaderWriter2<FaultcurrentEntity> eventRWFaultCurrentDB;
    private EsDataObjectReaderWriter2<FaultlogEntity> eventRWFaultLogDB;
    private EsDataObjectReaderWriter2<CmlogEntity> eventRWFCMLogDB;
    private EsDataObjectReaderWriter2<ConnectionlogEntity> eventRWConnectionLogDB;
    private final EsDataObjectReaderWriter2<NetworkElementConnectionEntity> networkelementConnectionDB;
    private final EsDataObjectReaderWriter2<GuicutthroughEntity> guiCutThroughDB;
    private final EsDataObjectReaderWriter2<PmdataEntity> pmData15mDB;
    private final EsDataObjectReaderWriter2<PmdataEntity> pmData24hDB;
    // --- Construct and initialize

    public HtDatabaseEventsService(HtDatabaseClient client) throws Exception {

        LOG.info("Create {} start", HtDatabaseEventsService.class);

        try {
            // Create control structure
            this.client = client;

            eventRWEventLogDevicemanager = new EsDataObjectReaderWriter2<>(client, Entity.Eventlog,
                    EventlogEntity.class, EventlogBuilder.class);

            eventRWEquipment = new EsDataObjectReaderWriter2<>(client, Entity.Inventoryequipment, InventoryEntity.class,
                    InventoryBuilder.class);

            eventRWFaultCurrentDB = new EsDataObjectReaderWriter2<>(client, Entity.Faultcurrent,
                    FaultcurrentEntity.class, FaultcurrentBuilder.class);

            eventRWFaultLogDB = new EsDataObjectReaderWriter2<>(client, Entity.Faultlog, FaultlogEntity.class,
                    FaultlogBuilder.class);

            eventRWFCMLogDB =
                    new EsDataObjectReaderWriter2<>(client, Entity.Cmlog, CmlogEntity.class, CmlogBuilder.class);

            eventRWConnectionLogDB = new EsDataObjectReaderWriter2<>(client, Entity.Connectionlog,
                    ConnectionlogEntity.class, ConnectionlogBuilder.class);

            networkelementConnectionDB = new EsDataObjectReaderWriter2<>(client, Entity.NetworkelementConnection,
                    NetworkElementConnectionEntity.class, NetworkElementConnectionBuilder.class, true)
                            .setEsIdAttributeName("_id");

            guiCutThroughDB = new EsDataObjectReaderWriter2<>(client, Entity.Guicutthrough, GuicutthroughEntity.class,
                    GuicutthroughBuilder.class);

            pmData15mDB = new EsDataObjectReaderWriter2<>(client, Entity.Historicalperformance15min, PmdataEntity.class,
                    PmdataEntityBuilder.class);

            pmData24hDB = new EsDataObjectReaderWriter2<>(client, Entity.Historicalperformance24h, PmdataEntity.class,
                    PmdataEntityBuilder.class);

        } catch (Exception e) {
            LOG.error("Can not start database client. Exception: {}", e);
            throw new Exception("Can not start database client. Exception: {}", e);
        }
        LOG.info("Create {} finished. DB Service {} started.", HtDatabaseEventsService.class,
                client != null ? "sucessfully" : "not");
    }

    // --- Function

    // -- Connection log
    @Override
    public void writeConnectionLog(ConnectionlogEntity event) {
        if (assertIfClientNull(event)) {
            return;
        }
        LOG.debug("Write event: {}", event);
        eventRWConnectionLogDB.write(event, null);

    }

    // -- Event log
    @Override
    public void writeEventLog(EventlogEntity event) {
        if (assertIfClientNull("No client to write {}", event)) {
            return;
        }

        LOG.debug("Write event: {}", event.toString());
        eventRWEventLogDevicemanager.write(event, null);
    }

    // -- Fault log

    @Override
    public void writeFaultLog(FaultlogEntity fault) {
        if (assertIfClientNull(fault)) {
            return;
        }

        LOG.debug("Write fault to faultlog: {}", fault.toString());
        eventRWFaultLogDB.write(fault, null);
    }

    //-- CM log

    @Override
    public void writeCMLog(CmlogEntity cm) {
        if (assertIfClientNull(cm)) {
            return;
        }

        LOG.debug("Write CM to cmlog: {}", cm.toString());
        eventRWFCMLogDB.write(cm, null);
    }

    // -- Fault current

    @Override
    public void updateFaultCurrent(FaultcurrentEntity fault) {
        if (assertIfClientNull(fault)) {
            return;
        }

        if (FaultEntityManager.isManagedAsCurrentProblem(fault)) {
            if (FaultEntityManager.isNoAlarmIndication(fault)) {
                LOG.debug("Remove from currentFaults: {}", fault.toString());
                eventRWFaultCurrentDB.remove(FaultEntityManager.genSpecificEsId(fault));
            } else {
                LOG.debug("Write to currentFaults: {}", fault.toString());
                eventRWFaultCurrentDB.write(fault, FaultEntityManager.genSpecificEsId(fault));
            }
        } else {
            LOG.debug("Ingnore for currentFaults: {}", fault.toString());
        }
    }

    /**
     * Remove all entries for one node
     *
     * @param nodeName contains the mountpointname
     * @return number of deleted entries
     */
    @Override
    public int clearFaultsCurrentOfNode(String nodeName) {
        if (assertIfClientNullForNodeName(nodeName)) {
            return -1;
        }

        LOG.debug("Remove from currentFaults all faults for node: {}", nodeName);
        return eventRWFaultCurrentDB.remove(EsFaultCurrent.getQueryForOneNode(nodeName));
    }

    /**
     * Remove all entries for one node
     *
     * @param nodeName contains the mountpointname
     * @param objectId of element to be deleted
     * @return number of deleted entries
     */
    @Override
    public int clearFaultsCurrentOfNodeWithObjectId(String nodeName, String objectId) {
        if (assertIfClientNullForNodeName(nodeName)) {
            return -1;
        }

        LOG.debug("Remove from currentFaults all faults for node/objectId: {}/{}", nodeName, objectId);
        return eventRWFaultCurrentDB.remove(EsFaultCurrent.getQueryForOneNodeAndObjectId(nodeName, objectId));
    }

    /**
     * Deliver list with all mountpoint/node-names in the database.
     *
     * @return List of all mountpoint/node-names the had active alarms.
     */
    @Override
    public @Nonnull List<String> getAllNodesWithCurrentAlarms() {
        if (assertIfClientNull("No DB, can not delete for all nodes", null)) {
            return new ArrayList<>();
        }

        LOG.debug("Remove from currentFaults faults for all node");
        List<String> nodeNames = new ArrayList<>();

        for (FaultcurrentEntity fault : eventRWFaultCurrentDB.doReadAll().getHits()) {
            String nodeName = fault.getNodeId();
            if (!nodeNames.contains(nodeName)) {
                // this.clearFaultsCurrentOfNode(nodeName); -> Function shifted
                nodeNames.add(nodeName);
            }
        }
        return nodeNames;
    }

    // -- Inventory and equipment current

    /**
     * write internal equipment to database
     *
     * @param internalEquipment with mandatory fields.
     */

    private void writeInventory(Inventory internalEquipment) {

        if (internalEquipment.getManufacturerIdentifier() == null) {
            internalEquipment = new InventoryBuilder(internalEquipment).setManufacturerIdentifier("").build();
        }
        if (internalEquipment.getDate() == null) {
            internalEquipment = new InventoryBuilder(internalEquipment).setDate("").build();
        }
        eventRWEquipment.write(internalEquipment, internalEquipment.getNodeId() + "/" + internalEquipment.getUuid());
    }

    /**
     * write internal equipment to database
     *
     * @param nodeId
     * @param list
     */
    @Override
    public void writeInventory(String nodeId, List<Inventory> list) {

        try {
            checkConsistency(nodeId, list);
        } catch (DataInconsistencyException e) {
            LOG.warn("inventory list for node {} is not consistent", nodeId, e);
            list = e.getRepairedList();
        }

        for (Inventory internalEquipment : list) {
            this.writeInventory(internalEquipment);
        }
    }

    private static void checkConsistency(String nodeId, List<Inventory> list) throws DataInconsistencyException {
        final String UNBOUND_INVENTORY_UUID = "unbound";
        List<String> failures = new ArrayList<>();
        long treeLevel;
        int failCounter = 0;
        Map<String, Inventory> repairList = new HashMap<>();
        InventoryBuilder repairedItem;
        InventoryBuilder unboundItem = new InventoryBuilder().setNodeId(nodeId).setUuid(UNBOUND_INVENTORY_UUID)
                .setTreeLevel(Uint32.valueOf(0));;
        for (Inventory item : list) {
            repairedItem = new InventoryBuilder(item);
            // check for bad node-id
            if (!nodeId.equals(item.getNodeId())) {
                failures.add(String.format("missing node-id for equipment(uuid=%s)", item.getUuid()));
                repairedItem.setNodeId(nodeId);
                failCounter++;
            }
            // check missing tree-level
            if (item.getTreeLevel() == null) {
                failures.add(String.format("missing tree-level for equipment(uuid=%s)", item.getUuid()));
                repairedItem.setTreeLevel(Uint32.valueOf(ROOT_TREE_LEVEL));
                failCounter++;

            } else {
                treeLevel = item.getTreeLevel().longValue();
                if (treeLevel > ROOT_TREE_LEVEL) {
                    // check non root elem and missing parent
                    if (item.getParentUuid() == null) {
                        failures.add(String.format("Non root level element (uuid=%s) has to have a parent element",
                                item.getUuid()));
                        failCounter++;
                        repairedItem.setParentUuid(UNBOUND_INVENTORY_UUID);
                        repairList.put(unboundItem.getUuid(), unboundItem.build());
                    }
                    // check that parent exists in list and is tree-level -1
                    else {
                        Optional<Inventory> parent =
                                list.stream().filter(e -> item.getParentUuid().equals(e.getUuid())).findFirst();
                        if (parent.isEmpty()) {
                            failures.add(String.format("no parent found for uuid=%s with parent-uuid=%s",
                                    item.getUuid(), item.getParentUuid()));
                            repairedItem.setParentUuid(UNBOUND_INVENTORY_UUID);
                            failCounter++;
                        }
                    }
                }
                // check for duplicated uui
                Optional<Inventory> duplicate = list
                        .stream().filter(e -> !item.equals(e) && item.getUuid() != null
                                && item.getUuid().equals(e.getUuid()) && repairList.containsKey(e.getUuid()))
                        .findFirst();
                if (duplicate.isPresent()) {
                    failures.add(String.format("found duplicate uuid=%s", item.getUuid()));
                    failCounter++;
                    continue;

                }
                if (failCounter > 0) {
                    repairList.put(repairedItem.getUuid(), repairedItem.build());
                } else {
                    repairList.put(item.getUuid(), item);
                }
            }
        }

        if (failures.size() > 0) {
            throw new DataInconsistencyException(new ArrayList<>(repairList.values()),
                    "inventory list is not consistent;\n" + String.join("\n", failures));
        }
    }

    // -- Networkelement

    /**
     * join base with parameters of toJoin (only non null values)
     *
     * @param base base object
     * @param toJoin object with new property values
     * @return new joined object
     */
    @SuppressWarnings("unused")
    private NetworkElementConnectionEntity joinNe(NetworkElementConnectionEntity base,
            NetworkElementConnectionEntity toJoin) {
        if (base == null) {
            return toJoin;
        }
        NetworkElementConnectionBuilder builder = new NetworkElementConnectionBuilder(base);
        if (toJoin != null) {
            if (toJoin.requireIsRequired() != null) {
                builder.setIsRequired(toJoin.requireIsRequired());
            }
            if (toJoin.getCoreModelCapability() != null) {
                builder.setCoreModelCapability(toJoin.getCoreModelCapability());
            }
            if (toJoin.getDeviceType() != null) {
                builder.setDeviceType(toJoin.getDeviceType());
            }
            if (toJoin.getHost() != null) {
                builder.setHost(toJoin.getHost());
            }
            if (toJoin.getNodeDetails() != null) {
                builder.setNodeDetails(toJoin.getNodeDetails());
            }
            if (toJoin.getPassword() != null) {
                builder.setPassword(toJoin.getPassword());
            }
            if (toJoin.getPort() != null) {
                builder.setPort(toJoin.getPort());
            }
            if (toJoin.getStatus() != null) {
                builder.setStatus(toJoin.getStatus());
            }
            if (toJoin.getUsername() != null) {
                builder.setUsername(toJoin.getUsername());
            }
        }
        return builder.build();
    }

    /**
     *
     * @param networkElementConnectionEntitiy to wirte to DB
     * @param nodeId Id for this DB element
     */
    @Override
    public boolean updateNetworkConnectionDeviceType(NetworkElementConnectionEntity networkElementConnectionEntitiy,
            String nodeId) {
        return this.networkelementConnectionDB.update(networkElementConnectionEntitiy, nodeId) != null;
        // NetworkElementConnectionEntity e =
        // this.networkelementConnectionDB.read(nodeId);
        // this.networkelementConnectionDB.write(this.joinNe(e,
        // networkElementConnectionEntitiy), nodeId);
    }

    /**
     * Update after new mountpoint registration
     *
     * @param networkElementConnectionEntitiy data
     * @param nodeId of device (mountpoint name)
     */
    @Override
    public boolean updateNetworkConnection22(NetworkElementConnectionEntity networkElementConnectionEntitiy,
            String nodeId) {
        LOG.debug("update networkelement-connection for {} with data {}", nodeId, networkElementConnectionEntitiy);
        return this.networkelementConnectionDB.updateOrCreate(networkElementConnectionEntitiy, nodeId,
                Arrays.asList("is-required", "username", "password")) != null;
        // NetworkElementConnectionEntity e =
        // this.networkelementConnectionDB.read(nodeId);
        // this.networkelementConnectionDB.write(this.joinNe(e,
        // networkElementConnectionEntitiy), nodeId);

    }

    /* please do not remove */
    // public void cleanNetworkElementConnections() {
    // this.networkelementConnectionDB.remove(QueryBuilders.matchQuery("is-required",
    // false));
    // CreateNetworkElementConnectionInput x = new
    // CreateNetworkElementConnectionInputBuilder().setStatus(ConnectionLogStatus.Disconnected).build();
    // this.networkelementConnectionDB.update(x,QueryBuilders.matchAllQuery());
    // }

    @Override
    public void removeNetworkConnection(String nodeId) {
        Boolean isRequired;
        NetworkElementConnectionEntity e = this.networkelementConnectionDB.read(nodeId);
        if (e != null && (isRequired = e.requireIsRequired()) != null) {
            if (isRequired) {
                LOG.debug("updating connection status for {} of required ne to disconnected", nodeId);
                this.networkelementConnectionDB.update(new UpdateNetworkElementConnectionInputBuilder()
                        .setStatus(ConnectionLogStatus.Disconnected).build(), nodeId);
            } else {
                LOG.debug("remove networkelement-connection for {} entry because of non-required", nodeId);
                this.networkelementConnectionDB.remove(nodeId);
            }
        } else {
            LOG.warn("Unable to update connection-status. dbentry for {} not found in networkelement-connection",
                    nodeId);
        }
    }

    // -- Multiple areas

    @Override
    public int doIndexClean(Date olderAreOutdated) {

        String netconfTimeStamp = NETCONFTIME_CONVERTER.getTimeStampAsNetconfString(olderAreOutdated);
        int removed = 0;

        QueryBuilder queryEventBase = EsEventBase.getQueryForTimeStamp(netconfTimeStamp);
        removed += eventRWEventLogDevicemanager.remove(queryEventBase);

        QueryBuilder queryFaultLog = EsFaultLogDevicemanager.getQueryForTimeStamp(netconfTimeStamp);
        removed += eventRWFaultLogDB.remove(queryFaultLog);

        QueryBuilder queryCMLog = EsCMLogDevicemanager.getQueryForTimeStamp(netconfTimeStamp);
        removed += eventRWFCMLogDB.remove(queryCMLog);

        return removed;
    }

    @Override
    public long getNumberOfOldObjects(Date olderAreOutdated) {

        String netconfTimeStamp = NETCONFTIME_CONVERTER.getTimeStampAsNetconfString(olderAreOutdated);
        int numberOfElements = 0;

        QueryBuilder queryEventBase = EsEventBase.getQueryForTimeStamp(netconfTimeStamp);
        numberOfElements += eventRWEventLogDevicemanager.doReadAll(queryEventBase).getTotal();

        QueryBuilder queryFaultLog = EsFaultLogDevicemanager.getQueryForTimeStamp(netconfTimeStamp);
        numberOfElements += eventRWFaultLogDB.doReadAll(queryFaultLog).getTotal();

        QueryBuilder queryCMLog = EsCMLogDevicemanager.getQueryForTimeStamp(netconfTimeStamp);
        numberOfElements += eventRWFCMLogDB.doReadAll(queryCMLog).getTotal();

        return numberOfElements;
    }

    // -- Helper

    /**
     * Verify status of client
     *
     * @param event that is printed with message
     * @return true if client is null
     */
    private boolean assertIfClientNull(Object event) {
        return assertIfClientNull("No DB, can not write: {}", event);
    }

    private boolean assertIfClientNullForNodeName(Object object) {
        return assertIfClientNull("No DB, can not handle node: {}", object);
    }

    /**
     * Verify status of client
     *
     * @param message to print including {} for object printout.
     * @return true if client is null
     */
    private boolean assertIfClientNull(String message, Object object) {
        if (client == null) {
            LOG.debug(message, object);
            return true;
        }
        return false;
    }

    // ### sub classes

    private static class EsEventBase {
        /**
         * Query to get older Elements
         *
         * @param netconfTimeStamp to identify older Elements
         * @return QueryBuilder for older elements related to timestamp
         */
        private static QueryBuilder getQueryForTimeStamp(String netconfTimeStamp) {
            return new RangeQueryBuilder("timestamp").lte(netconfTimeStamp);
        }
    }

    private static class EsFaultLogDevicemanager {
        /**
         * Get older Elements
         *
         * @param netconfTimeStamp to identify query elements older than this timestamp.
         * @return QueryBuilder for related elements
         */
        public static QueryBuilder getQueryForTimeStamp(String netconfTimeStamp) {
            return new RangeQueryBuilder("timestamp").lte(netconfTimeStamp);
        }
    }

    private static class EsCMLogDevicemanager {
        /**
         * Get older Elements
         *
         * @param netconfTimeStamp to identify query elements older than this timestamp.
         * @return QueryBuilder for related elements
         */
        public static QueryBuilder getQueryForTimeStamp(String netconfTimeStamp) {
            return new RangeQueryBuilder("timestamp").lte(netconfTimeStamp);
        }
    }

    public static class EsFaultCurrent {
        /**
         * @param nodeName name of the node
         * @return query builder
         */
        public static QueryBuilder getQueryForOneNode(String nodeName) {
            return QueryBuilders.matchQuery("node-id", nodeName);
        }

        public static QueryBuilder getQueryForOneNodeAndObjectId(String nodeName, String objectId) {
            BoolQueryBuilder bq = QueryBuilders.boolQuery();
            bq.must(QueryBuilders.matchQuery("node-id", nodeName));
            bq.must(QueryBuilders.matchQuery("object-id", objectId));
            return bq;
        }
    }

    @Override
    public List<NetworkElementConnectionEntity> getNetworkElementConnections() {
        return this.networkelementConnectionDB.doReadAll().getHits();
    }

    @Override
    public void doWritePerformanceData(List<PmdataEntity> list) {

        list.forEach(elem -> {
            GranularityPeriodType granularityPeriod = nnGetGranularityPeriodType(elem.getGranularityPeriod());
            // _id": "Sim12600/LP-MWPS-TTP-01/2017-07-04T15:15:00.0+00:00"
            StringBuffer id = new StringBuffer();
            DateAndTime date = elem.getTimeStamp();
            id.append(elem.getNodeName());
            id.append("/");
            id.append(elem.getUuidInterface());
            id.append("/");
            id.append(date != null ? date.getValue() : "null");

            switch (granularityPeriod) {
                case Period15Min:
                    pmData15mDB.write(elem, id.toString());
                    break;
                case Period24Hours:
                    pmData24hDB.write(elem, id.toString());
                    break;
                case Unknown:
                default:
                    LOG.debug("Unknown granularity {} id {}", granularityPeriod, id);
                    break;
            }
        });

    }

    @NonNull
    GranularityPeriodType nnGetGranularityPeriodType(@Nullable GranularityPeriodType granularityPeriod) {
        return granularityPeriod != null ? granularityPeriod : GranularityPeriodType.Unknown;
    }

    @Override
    public HtDatabaseClient getRawClient() {
        return this.client;
    }

    @Override
    public void writeGuiCutThroughData(Guicutthrough gcData, String nodeId) {
        guiCutThroughDB.write(gcData, nodeId);
    }

    @Override
    public int clearGuiCutThroughEntriesOfNode(String nodeName) {
        guiCutThroughDB.remove(nodeName);
        return 0;
    }

}
