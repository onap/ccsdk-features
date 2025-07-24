/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Update Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.entity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.mariadb.jdbc.Driver;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBConfig;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBReaderWriter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBReaderWriterFault;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBReaderWriterInventory;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBReaderWriterPm;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GranularityPeriodType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Guicutthrough;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event service, writing all events into the database into the appropriate index.
 *
 * @author herbert
 */
public class HtDatabaseEventsService implements DataProvider {
    private static final Logger LOG = LoggerFactory.getLogger(HtDatabaseEventsService.class);

    private static final NetconfTimeStamp NETCONFTIME_CONVERTER = NetconfTimeStampImpl.getConverter();;

    protected final SqlDBClient dbClient;
    protected String controllerId;
    protected final SqlDBReaderWriter<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.connectionlog.list.output.Data> connectionlogRW;
    protected final SqlDBReaderWriter<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.eventlog.list.output.Data> eventlogRW;
    protected final SqlDBReaderWriter<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.faultlog.list.output.Data> eventRWFaultLog;
    protected final SqlDBReaderWriterFault<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.faultcurrent.list.output.Data> eventRWFaultCurrent;
    protected final SqlDBReaderWriterInventory<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.Data> equipmentRW;
    protected final SqlDBReaderWriter<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.gui.cut.through.entry.output.Data> guicutthroughRW;
    protected final SqlDBReaderWriter<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.network.element.connection.list.output.Data> networkelementConnectionRW;
    protected final SqlDBReaderWriterPm<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data> pm15mRW;
    protected final SqlDBReaderWriterPm<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.list.output.Data> pm24hRW;
    protected final SqlDBReaderWriter<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.cmlog.list.output.Data> eventRWCMLog;
    protected final String controllerTableName;

    public HtDatabaseEventsService(SqlDBConfig config) {
        LOG.debug("Creating dummy instance of org.mariadb.jdbc.Driver");
        @SuppressWarnings("unused")
        Driver dvr = new org.mariadb.jdbc.Driver();
        dvr = null;
        this.controllerId = config.getControllerId();
        this.controllerTableName = SqlDBMapper.TABLENAME_CONTROLLER + config.getDbSuffix();
        this.dbClient = new SqlDBClient(config.getUrl(), config.getUsername(), config.getPassword());
        this.connectionlogRW = new SqlDBReaderWriter<>(dbClient, Entity.Connectionlog, config.getDbSuffix(),
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.connectionlog.list.output.Data.class,
                this.controllerId);
        this.eventlogRW = new SqlDBReaderWriter<>(dbClient, Entity.Eventlog, config.getDbSuffix(),
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.eventlog.list.output.Data.class,
                this.controllerId);
        this.eventRWFaultLog = new SqlDBReaderWriter<>(dbClient, Entity.Faultlog, config.getDbSuffix(),
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.faultlog.list.output.Data.class,
                this.controllerId);
        this.eventRWFaultCurrent = new SqlDBReaderWriterFault<>(dbClient, Entity.Faultcurrent, config.getDbSuffix(),
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.faultcurrent.list.output.Data.class,
                this.controllerId);
        this.equipmentRW = new SqlDBReaderWriterInventory<>(dbClient, Entity.Inventoryequipment, config.getDbSuffix(),
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.Data.class,
                this.controllerId);
        this.guicutthroughRW = new SqlDBReaderWriter<>(dbClient,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity.Guicutthrough,
                config.getDbSuffix(),
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.gui.cut.through.entry.output.Data.class,
                this.controllerId).setWriteInterface(Guicutthrough.class);
        this.networkelementConnectionRW = new SqlDBReaderWriter<>(dbClient, Entity.NetworkelementConnection,
                config.getDbSuffix(),
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.network.element.connection.list.output.Data.class,
                this.controllerId);
        this.networkelementConnectionRW.setWriteInterface(NetworkElementConnectionEntity.class);

        this.pm15mRW = new SqlDBReaderWriterPm<>(dbClient, Entity.Historicalperformance15min, config.getDbSuffix(),
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data.class,
                this.controllerId);

        this.pm24hRW = new SqlDBReaderWriterPm<>(dbClient, Entity.Historicalperformance24h, config.getDbSuffix(),
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.list.output.Data.class,
                this.controllerId);

        this.eventRWCMLog = new SqlDBReaderWriter<>(dbClient, Entity.Cmlog, config.getDbSuffix(),
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.cmlog.list.output.Data.class,
                this.controllerId);

    }

    @Override
    public void writeConnectionLog(ConnectionlogEntity event) {
        this.connectionlogRW.write(event, null);
    }

    @Override
    public void writeEventLog(EventlogEntity event) {
        this.eventlogRW.write(event, null);

    }

    @Override
    public void writeFaultLog(FaultlogEntity fault) {
        this.eventRWFaultLog.write(fault, null);
    }

    @Override
    public void writeCMLog(CmlogEntity cm) {
        this.eventRWCMLog.write(cm, null);
    }

    @Override
    public void updateFaultCurrent(FaultcurrentEntity fault) {
        final String id = DatabaseIdGenerator.getFaultcurrentId(fault);
        if (FaultEntityManager.isManagedAsCurrentProblem(fault)) {
            if (FaultEntityManager.isNoAlarmIndication(fault)) {
                LOG.debug("Remove from currentFaults: {}", fault.toString());
                this.eventRWFaultCurrent.remove(id);
            } else {
                LOG.debug("Write to currentFaults: {}", fault.toString());
                this.eventRWFaultCurrent.updateOrInsert(fault, id);
            }
        } else {
            LOG.debug("Ingnore for currentFaults: {}", fault.toString());
        }
    }

    @Override
    public int clearFaultsCurrentOfNode(String nodeName) {
        return this.eventRWFaultCurrent
                .remove(Arrays.asList(new FilterBuilder().setProperty("node-id").setFiltervalue(nodeName).build()));
    }

    @Override
    public int clearFaultsCurrentOfNodeWithObjectId(String nodeName, String objectId) {
        return this.eventRWFaultCurrent
                .remove(Arrays.asList(new FilterBuilder().setProperty("node-id").setFiltervalue(nodeName).build(),
                        new FilterBuilder().setProperty("object-id").setFiltervalue(objectId).build()));
    }

    @Override
    public List<String> getAllNodesWithCurrentAlarms() {
        return this.eventRWFaultCurrent.getAllNodes();
    }

    @Override
    public void writeInventory(String nodeId, List<Inventory> list) {
        for (Inventory internalEquipment : list) {
            this.equipmentRW.updateOrInsert(internalEquipment,
                    internalEquipment.getId() != null ? internalEquipment.getId()
                            : DatabaseIdGenerator.getInventoryId(internalEquipment));
        }
    }

    @Override
    public void writeGuiCutThroughData(Guicutthrough gcData, String nodeId) {
        this.guicutthroughRW.write(gcData, nodeId);
    }

    @Override
    public int clearGuiCutThroughEntriesOfNode(String nodeName) {
        return this.guicutthroughRW.remove(nodeName);
    }

    @Override
    public boolean updateNetworkConnectionDeviceType(NetworkElementConnectionEntity networkElementConnectionEntitiy,
            String nodeId) {
        return this.networkelementConnectionRW.updateOrInsert(networkElementConnectionEntitiy, nodeId) != null;
    }

    @Override
    public boolean updateNetworkConnection22(NetworkElementConnectionEntity ne, String nodeId) {
        return this.networkelementConnectionRW.updateOrInsert(ne, nodeId) != null;
    }

    /**
     * Remove network element connection if not required
     * This function is called onDisconnect event for netconf node
     */
    @Override
    public void removeNetworkConnection(String nodeId) {
        NetworkElementConnectionEntity e = this.networkelementConnectionRW.read(nodeId);
        Boolean isRequired = e!=null? e.getIsRequired():null;
        if (e != null && isRequired  != null) {
            if (isRequired) {
                LOG.debug("updating connection status for {} of required ne to disconnected", nodeId);
                this.networkelementConnectionRW.update(new UpdateNetworkElementConnectionInputBuilder()
                        .setStatus(ConnectionLogStatus.Disconnected).build(), nodeId);
            } else {
                LOG.debug("remove networkelement-connection for {} entry because of non-required", nodeId);
                this.networkelementConnectionRW.remove(nodeId);
            }
        } else {
            LOG.warn("Unable to update connection-status. dbentry for {} not found in networkelement-connection",
                    nodeId);
        }


    }

    @Override
    public int doIndexClean(Date olderAreOutdated) {
        String netconfTimeStamp = NETCONFTIME_CONVERTER.getTimeStampAsNetconfString(olderAreOutdated);
        List<Filter> filter = new ArrayList<>();
        filter.add(new FilterBuilder().setProperty("timestamp").setFiltervalue(String.format("<%s", netconfTimeStamp))
                .build());
        if (this.controllerId != null) {
            filter.add(
                    new FilterBuilder().setProperty(SqlDBMapper.ODLID_DBCOL).setFiltervalue(this.controllerId).build());
        }
        int removed = 0;

        removed += this.eventlogRW.remove(filter);
        removed += this.eventRWFaultLog.remove(filter);
        removed += this.eventRWCMLog.remove(filter);
        return removed;
    }

    @Override
    public long getNumberOfOldObjects(Date olderAreOutdated) {
        List<Filter> filter = Arrays.asList(FaultEntityManager.getOlderOrEqualFilter(olderAreOutdated));
        try {
            return this.eventRWFaultLog.count(filter, this.controllerId);
        } catch (SQLException e) {
            LOG.warn("problem counting faults older than {}: ", olderAreOutdated, e);
        }
        return 0;
    }

    @Override
    public List<NetworkElementConnectionEntity> getNetworkElementConnections() {
        return this.networkelementConnectionRW.readAll(NetworkElementConnectionEntity.class);
    }

    @Override
    public void doWritePerformanceData(List<PmdataEntity> list) {
        list.stream().forEach((pmData) -> {
            GranularityPeriodType granularityPeriod =
                    pmData.getGranularityPeriod() != null ? pmData.getGranularityPeriod()
                            : GranularityPeriodType.Unknown;
            switch (granularityPeriod) {
                case Period15Min:
                    this.pm15mRW.write(pmData);
                    break;
                case Period24Hours:
                    this.pm24hRW.write(pmData);
                    break;
                case Unknown:
                default:
                    LOG.debug("Unknown granularity {}", granularityPeriod);
                    break;
            }
        });

    }
}
