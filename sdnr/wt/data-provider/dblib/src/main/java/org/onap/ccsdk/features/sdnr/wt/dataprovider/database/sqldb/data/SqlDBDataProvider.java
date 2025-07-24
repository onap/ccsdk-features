/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBConfig;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.entity.HtDatabaseEventsService;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.entity.HtDatabaseMaintenanceService;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.rpctypehelper.QueryResult;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBReaderWriter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBReaderWriterUserdata;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBStatusReader;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.DeleteQuery;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DatabaseDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMediatorserver;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtUserdataManager;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.InventoryTreeProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Guicutthrough;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GuicutthroughBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MediatorServerEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadCmlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadConnectionlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadEventlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultcurrentListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadGuiCutThroughEntryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryDeviceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMaintenanceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadNetworkElementConnectionListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mDeviceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mLtpListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hDeviceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hLtpListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.mediator.server.list.output.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlDBDataProvider extends HtDatabaseEventsService implements DatabaseDataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SqlDBDataProvider.class);

    private static final String EXCEPTION_UNABLE_TO_WRITE_IN_DATABASE = "unable to write data to database";
    private static final String EXCEPTION_UNABLE_TO_UPDATE_IN_DATABASE = "unable to update data in database";
    private static final String EXCEPTION_UNABLE_TO_REMOVE_FROM_DATABASE = "unable to remove data from database";

    private final HtDatabaseMediatorserver dbMediatorServerService;
    private final HtDatabaseMaintenance dbMaintenanceService;
    private final SqlDBReaderWriter<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.mediator.server.list.output.Data> mediatorserverRW;
    private final SqlDBReaderWriter<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.maintenance.list.output.Data> maintenanceRW;
    private final SqlDBStatusReader readStatus;
    private final HtUserdataManager usermanager;
    private final InventoryTreeProvider inventoryTreeProvider;
    private final String guicutthroughOverride;

    public SqlDBReaderWriter<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.maintenance.list.output.Data> getMaintenanceReaderWriter() {
        return this.maintenanceRW;
    }

    public SqlDBReaderWriter<Data> getMediatorServerReaderWriter() {
        return this.mediatorserverRW;
    }

    public SqlDBDataProvider(SqlDBConfig config, String guicutthroughOverride) {
        this(config, true, guicutthroughOverride);
    }

    public SqlDBDataProvider(SqlDBConfig config, boolean initControllerId, String guicutthroughOverride) {
        super(config);
        this.guicutthroughOverride = guicutthroughOverride;

        this.mediatorserverRW = new SqlDBReaderWriter<>(this.dbClient, Entity.MediatorServer, config.getDbSuffix(),
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.mediator.server.list.output.Data.class,
                this.controllerId).setWriteInterface(MediatorServerEntity.class);

        this.maintenanceRW = new SqlDBReaderWriter<>(this.dbClient, Entity.Maintenancemode, config.getDbSuffix(),
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.maintenance.list.output.Data.class,
                this.controllerId).setWriteInterface(MaintenanceEntity.class);

        this.readStatus = new SqlDBStatusReader(this.dbClient, this.controllerId);

        this.dbMediatorServerService = new HtDatabaseMediatorserver() {

            @Override
            public List<MediatorServerEntity> getAll() {
                return SqlDBDataProvider.this.mediatorserverRW.readAll(MediatorServerEntity.class);
            }
        };
        this.dbMaintenanceService = new HtDatabaseMaintenanceService(this);
        this.usermanager = new HtUserdataManagerImpl(
                new SqlDBReaderWriterUserdata(this.dbClient, Entity.Userdata, config.getDbSuffix()));
        if (initControllerId) {
            try {
                this.setControllerId();
            } catch (SQLException e) {
                LOG.warn("problem setting controllerId: ", e);
            }
        }
        this.inventoryTreeProvider = new SqlDbInventoryTreeProvider(this.dbClient, this.getControllerId());

    }

    /*-------------------------
     * Provide access to model API
     */

    @Override
    public ReadFaultcurrentListOutputBuilder readFaultCurrentList(EntityInput input) {

        ReadFaultcurrentListOutputBuilder outputBuilder = new ReadFaultcurrentListOutputBuilder();

        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.faultcurrent.list.output.Data> result =
                this.eventRWFaultCurrent.getData(input);
        outputBuilder.setData(result.getResult());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.faultcurrent.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadFaultlogListOutputBuilder readFaultLogList(EntityInput input) {
        ReadFaultlogListOutputBuilder outputBuilder = new ReadFaultlogListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.faultlog.list.output.Data> result =
                this.eventRWFaultLog.getData(input);
        outputBuilder.setData(result.getResult());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.faultlog.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadCmlogListOutputBuilder readCMLogList(EntityInput input) {
        ReadCmlogListOutputBuilder outputBuilder = new ReadCmlogListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.cmlog.list.output.Data> result =
                this.eventRWCMLog.getData(input);
        outputBuilder.setData(result.getResult());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.cmlog.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadMaintenanceListOutputBuilder readMaintenanceList(EntityInput input) {
        ReadMaintenanceListOutputBuilder outputBuilder = new ReadMaintenanceListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.maintenance.list.output.Data> result =
                this.maintenanceRW.getData(input);
        outputBuilder.setData(result.getResult());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.maintenance.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadNetworkElementConnectionListOutputBuilder readNetworkElementConnectionList(EntityInput input) {
        ReadNetworkElementConnectionListOutputBuilder outputBuilder =
                new ReadNetworkElementConnectionListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.network.element.connection.list.output.Data> result =
                this.networkelementConnectionRW.getData(input);
        outputBuilder.setData(result.getResult());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.network.element.connection.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadInventoryListOutputBuilder readInventoryList(EntityInput input) {
        ReadInventoryListOutputBuilder outputBuilder = new ReadInventoryListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.Data> result =
                this.equipmentRW.getData(input);
        outputBuilder.setData(result.getResult());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadInventoryDeviceListOutputBuilder readInventoryDeviceList(EntityInput input) {
        ReadInventoryDeviceListOutputBuilder outputBuilder = new ReadInventoryDeviceListOutputBuilder();
        QueryResult<String> result = this.equipmentRW.getDataDeviceList(input);
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.device.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResultSet());
        return outputBuilder;
    }

    @Override
    public ReadConnectionlogListOutputBuilder readConnectionlogList(EntityInput input) {
        ReadConnectionlogListOutputBuilder outputBuilder = new ReadConnectionlogListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.connectionlog.list.output.Data> result =
                this.connectionlogRW.getData(input);
        outputBuilder.setData(result.getResult());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.connectionlog.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadEventlogListOutputBuilder readEventlogList(EntityInput input) throws IOException {
        ReadEventlogListOutputBuilder outputBuilder = new ReadEventlogListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.eventlog.list.output.Data> result =
                this.eventlogRW.getData(input);
        outputBuilder.setData(result.getResult());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.eventlog.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadPmdata15mListOutputBuilder readPmdata15mList(EntityInput input) {
        ReadPmdata15mListOutputBuilder outputBuilder = new ReadPmdata15mListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data> result =
                this.pm15mRW.getData(input);
        LOG.debug("Read data: readPmdata15mList: {}", result);
        outputBuilder.setData(result.getResult());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadPmdata24hListOutputBuilder readPmdata24hList(EntityInput input) {
        ReadPmdata24hListOutputBuilder outputBuilder = new ReadPmdata24hListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.list.output.Data> result =
                this.pm24hRW.getData(input);
        outputBuilder.setData(result.getResult());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadPmdata15mLtpListOutputBuilder readPmdata15mLtpList(EntityInput input) throws IOException {
        ReadPmdata15mLtpListOutputBuilder outputBuilder = new ReadPmdata15mLtpListOutputBuilder();
        QueryResult<String> result = this.pm15mRW.getDataLtpList(input);
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.ltp.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResultSet());
        return outputBuilder;
    }

    @Override
    public ReadPmdata15mDeviceListOutputBuilder readPmdata15mDeviceList(EntityInput input) throws IOException {
        ReadPmdata15mDeviceListOutputBuilder outputBuilder = new ReadPmdata15mDeviceListOutputBuilder();
        QueryResult<String> result = this.pm15mRW.getDataDeviceList(input);
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.device.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResultSet());
        return outputBuilder;
    }

    @Override
    public ReadPmdata24hLtpListOutputBuilder readPmdata24hLtpList(EntityInput input) throws IOException {

        QueryResult<String> result = this.pm24hRW.getDataLtpList(input);

        ReadPmdata24hLtpListOutputBuilder outputBuilder = new ReadPmdata24hLtpListOutputBuilder();
        new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.ltp.list.output.PaginationBuilder();
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.ltp.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResultSet());
        return outputBuilder;
    }

    @Override
    public ReadPmdata24hDeviceListOutputBuilder readPmdata24hDeviceList(EntityInput input) throws IOException {

        QueryResult<String> result = pm24hRW.getDataDeviceList(input);

        ReadPmdata24hDeviceListOutputBuilder outputBuilder = new ReadPmdata24hDeviceListOutputBuilder();
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.device.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResultSet());
        return outputBuilder;
    }

    @Override
    public ReadStatusOutputBuilder readStatus(EntityInput input) throws IOException {
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.status.output.Data> result =
                readStatus.getDataStatus(input);

        ReadStatusOutputBuilder outputBuilder = new ReadStatusOutputBuilder();
        outputBuilder.setData(result.getResult());
        return outputBuilder;
    }

    @Override
    public CreateNetworkElementConnectionOutputBuilder createNetworkElementConnection(
            NetworkElementConnectionEntity input) throws IOException {
        String id = this.networkelementConnectionRW.updateOrInsert(input, input.getNodeId());
        if (id == null) {
            throw new IOException(EXCEPTION_UNABLE_TO_WRITE_IN_DATABASE);
        }
        CreateNetworkElementConnectionOutputBuilder builder = new CreateNetworkElementConnectionOutputBuilder();
        builder.setId(id).setNodeId(input.getNodeId()).setHost(input.getHost()).setPort(input.getPort())
                .setUsername(input.getUsername()).setPassword(input.getPassword()).setIsRequired(input.getIsRequired())
                .setCoreModelCapability(input.getCoreModelCapability()).setDeviceType(input.getDeviceType());
        return builder;
    }

    @Override
    public UpdateNetworkElementConnectionOutputBuilder updateNetworkElementConnection(
            UpdateNetworkElementConnectionInput input) throws IOException {
        String id = this.networkelementConnectionRW.update(input, input.getId());
        if (id == null) {
            throw new IOException(EXCEPTION_UNABLE_TO_UPDATE_IN_DATABASE);
        }
        UpdateNetworkElementConnectionOutputBuilder builder = new UpdateNetworkElementConnectionOutputBuilder();
        builder.setId(id).setNodeId(input.getNodeId()).setHost(input.getHost()).setPort(input.getPort())
                .setUsername(input.getUsername()).setPassword(input.getPassword())
                .setCoreModelCapability(input.getCoreModelCapability()).setDeviceType(input.getDeviceType());
        return builder;
    }

    @Override
    public DeleteNetworkElementConnectionOutputBuilder deleteNetworkElementConnection(
            DeleteNetworkElementConnectionInput input) throws IOException {
        boolean removed = this.networkelementConnectionRW.remove(input.getId()) > 0;
        if (!removed) {
            throw new IOException(EXCEPTION_UNABLE_TO_REMOVE_FROM_DATABASE);
        }
        return new DeleteNetworkElementConnectionOutputBuilder();
    }

    @Override
    public DeleteMaintenanceOutputBuilder deleteMaintenance(DeleteMaintenanceInput input) throws IOException {
        boolean removed = this.maintenanceRW.remove(input.getId()) > 0;
        if (!removed) {
            throw new IOException(EXCEPTION_UNABLE_TO_REMOVE_FROM_DATABASE);
        }
        return new DeleteMaintenanceOutputBuilder();
    }

    @Override
    public UpdateMaintenanceOutputBuilder updateMaintenance(UpdateMaintenanceInput input) throws IOException {
        if (input.getId() == null) {
            throw new IOException("please give the id for updating entry");
        }
        String id = this.maintenanceRW.update(input, input.getId());
        if (id == null) {
            throw new IOException(EXCEPTION_UNABLE_TO_UPDATE_IN_DATABASE);
        }
        UpdateMaintenanceOutputBuilder builder = new UpdateMaintenanceOutputBuilder(input).setId(id);
        return builder;
    }

    @Override
    public CreateMaintenanceOutputBuilder createMaintenance(CreateMaintenanceInput input) throws IOException {
        String id = this.maintenanceRW.write(input, input.getNodeId());
        if (id == null) {
            throw new IOException(EXCEPTION_UNABLE_TO_WRITE_IN_DATABASE);
        }
        CreateMaintenanceOutputBuilder builder = new CreateMaintenanceOutputBuilder(input).setId(id);
        return builder;
    }

    @Override
    public ReadGuiCutThroughEntryOutputBuilder readGuiCutThroughEntry(EntityInput input) {
        ReadGuiCutThroughEntryOutputBuilder outputBuilder = new ReadGuiCutThroughEntryOutputBuilder();

        if (!guicutthroughOverride.isEmpty()) {
            Map<FilterKey, Filter> inputFilter = input == null ? null : input.getFilter();
            if (inputFilter != null) {
                // Iterate through the Filter map, get the ID and populate the GuicutThrough object accordingly.
                List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.gui.cut.through.entry.output.Data> gcData =
                        new ArrayList<>();
                for (FilterKey fk : inputFilter.keySet()) {
                    String fkVal = inputFilter.get(fk).getFiltervalue();
                    if (fkVal != null) {
                        addGcItem(gcData, fkVal);
                    }
                    for (String fkVals : inputFilter.get(fk).getFiltervalues()) {
                        addGcItem(gcData, fkVals);
                    }
                }
                outputBuilder.setData(gcData);
            }
        } else {
            QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.gui.cut.through.entry.output.Data> result =
                    this.guicutthroughRW.getData(input);
            outputBuilder.setData(result.getResult());
        }
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.gui.cut.through.entry.output.PaginationBuilder()
                        .build());
        return outputBuilder;
    }

    private void addGcItem(
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.gui.cut.through.entry.output.Data> gcData,
            String value) {
        Guicutthrough gcItem = new GuicutthroughBuilder().setId(value).setName(value)
                .setWeburi(guicutthroughOverride + "/" + value).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.gui.cut.through.entry.output.DataBuilder gcDataBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.gui.cut.through.entry.output.DataBuilder(
                        gcItem);
        gcData.add(gcDataBuilder.build());
    }

    @Override
    public boolean waitForYellowDatabaseStatus(long timeout, TimeUnit unit) {
        return true;
    }

    @Override
    public DataProvider getDataProvider() {
        return this;
    }

    @Override
    public HtDatabaseMaintenance getHtDatabaseMaintenance() {
        return this.dbMaintenanceService;
    }

    public boolean delete(Entity e, List<Filter> filters) throws SQLException {
        DeleteQuery query = new DeleteQuery(e, filters);
        return this.dbClient.write(query.toSql());

    }

    public SqlDBClient getDBService() {
        return this.dbClient;
    }

    public boolean setControllerId() throws SQLException {
        if (this.controllerId == null) {
            return true;
        }
        LOG.info("set controllerId {}", this.controllerId);
        String query = String.format("SELECT * FROM `%s` WHERE `id`='%s'", this.controllerTableName, this.controllerId);
        LOG.trace(query);
        ResultSet data = this.dbClient.read(query);

        if (data == null || !data.next()) {
            query = String.format("INSERT INTO `%s` (`id`,`desc`) VALUES ('%s','%s')", this.controllerTableName,
                    this.controllerId, "");
            LOG.trace(query);
            try {
                if (data != null) {
                    data.close();
                }
            } catch (SQLException ignore) {
            }
            return this.dbClient.write(query);
        } else {
            this.controllerId = data.getString(1);
            LOG.trace("controllerId already set");
        }
        return true;
    }

    public void waitForDatabaseReady(int i, TimeUnit unit) {
        this.dbClient.waitForYellowStatus(unit.convert(i, TimeUnit.MILLISECONDS));
    }

    public String getControllerId() {
        return this.controllerId;
    }

    @Override
    public HtUserdataManager getUserManager() {
        return this.usermanager;
    }

    @Override
    public InventoryTreeProvider getInventoryTreeProvider() {
        return this.inventoryTreeProvider;
    }

}
