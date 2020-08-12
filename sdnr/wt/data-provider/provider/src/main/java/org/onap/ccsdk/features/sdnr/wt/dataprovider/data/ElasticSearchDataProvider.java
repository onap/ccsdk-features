/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.data;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.acessor.DataObjectAcessorPm;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.acessor.DataObjectAcessorPm.Intervall;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.acessor.DataObjectAcessorStatus;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.acessor.DataObjectAcessorWithId;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.entity.HtDatabaseEventsService;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.entity.HtDatabaseMaintenanceService;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.rpctypehelper.QueryResult;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateMediatorServerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.DeleteMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.DeleteMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.DeleteMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.DeleteMediatorServerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.DeleteNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.DeleteNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.MaintenanceEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.MediatorServerEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadConnectionlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadEventlogListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadEventlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadFaultcurrentListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadFaultlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadInventoryListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadMaintenanceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadMediatorServerListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadNetworkElementConnectionListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata15mDeviceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata15mListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata15mLtpListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata24hDeviceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata24hListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata24hLtpListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateMediatorServerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateNetworkElementConnectionOutputBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchDataProvider /*extends BaseStatusProvider /* implements IEntityDataProvider*/ {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchDataProvider.class);

    private static final String EXCEPTION_UNABLE_TO_WRITE_IN_DATABASE = "unable to write data to database";
    private static final String EXCEPTION_UNABLE_TO_UPDATE_IN_DATABASE = "unable to update data in database";
    private static final String EXCEPTION_UNABLE_TO_REMOVE_FROM_DATABASE = "unable to remove data from database";

    private final HtDatabaseClient dbClient;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultcurrent.list.output.Data> eventRWFaultCurrent;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultlog.list.output.Data> eventRWFaultLog;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.mediator.server.list.output.Data> mediatorserverRW;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.maintenance.list.output.Data> maintenanceRW;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.inventory.list.output.Data> equipmentRW;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.connectionlog.list.output.Data> connnectionlogRW;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.eventlog.list.output.Data> eventlogRW;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.network.element.connection.list.output.Data> networkelementConnectionRW;
    private final DataObjectAcessorPm<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._15m.list.output.Data> pm15mRW;
    private final DataObjectAcessorPm<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._24h.list.output.Data> pm24hRW;

    private final DataObjectAcessorStatus readStatus;
    private final HtDatabaseEventsService databaseService;
    private final HtDatabaseMaintenanceService databaseMaintenanceService;

    public HtDatabaseClient getRawClient() {
        return this.dbClient;
    }

    public ElasticSearchDataProvider(HostInfo[] hosts) throws Exception {
        this(hosts, null, null, HtDatabaseClient.TRUSTALL_DEFAULT);
    }

    public ElasticSearchDataProvider(HostInfo[] hosts, String authUsername, String authPassword, boolean trustAllCerts)
            throws Exception {
        super();
        LOG.info("Start {}", this.getClass().getName());


        this.dbClient = HtDatabaseClient.getClient(hosts, authUsername, authPassword, trustAllCerts);
        this.mediatorserverRW = new DataObjectAcessorWithId<>(dbClient, Entity.MediatorServer,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.mediator.server.list.output.Data.class);
        this.mediatorserverRW.setWriteInterface(MediatorServerEntity.class);

        this.maintenanceRW = new DataObjectAcessorWithId<>(dbClient, Entity.Maintenancemode,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.maintenance.list.output.Data.class);
        this.maintenanceRW.setWriteInterface(MaintenanceEntity.class);

        this.equipmentRW = new DataObjectAcessorWithId<>(dbClient, Entity.Inventoryequipment,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.inventory.list.output.Data.class);

        this.eventRWFaultCurrent = new DataObjectAcessorWithId<>(dbClient, Entity.Faultcurrent,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultcurrent.list.output.Data.class);

        this.eventRWFaultLog = new DataObjectAcessorWithId<>(dbClient, Entity.Faultlog,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultlog.list.output.Data.class);

        this.connnectionlogRW = new DataObjectAcessorWithId<>(dbClient, Entity.Connectionlog,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.connectionlog.list.output.Data.class);

        this.eventlogRW = new DataObjectAcessorWithId<>(dbClient, Entity.Eventlog,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.eventlog.list.output.Data.class);

        this.networkelementConnectionRW = new DataObjectAcessorWithId<>(dbClient, Entity.NetworkelementConnection,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.network.element.connection.list.output.Data.class);
        this.networkelementConnectionRW.setWriteInterface(NetworkElementConnectionEntity.class);

        this.pm15mRW = new DataObjectAcessorPm<>(dbClient, Intervall.PMDATA15M, Entity.Historicalperformance15min,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._15m.list.output.Data.class);

        this.pm24hRW = new DataObjectAcessorPm<>(dbClient, Intervall.PMDATA24H, Entity.Historicalperformance24h,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._24h.list.output.Data.class);

        this.readStatus = new DataObjectAcessorStatus(dbClient, Entity.Faultcurrent);

        this.databaseService = new HtDatabaseEventsService(dbClient, this);
        this.databaseMaintenanceService = new HtDatabaseMaintenanceService(dbClient);
    }

    /*-------------------------
     * Provide access to model API
     */

    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultcurrent.list.output.Data
    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultcurrent.list.output.PaginationBuilder
    //eventRWFaultCurrent
    public ReadFaultcurrentListOutputBuilder readFaultCurrentList(EntityInput input) {

        ReadFaultcurrentListOutputBuilder outputBuilder = new ReadFaultcurrentListOutputBuilder();

        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultcurrent.list.output.Data> result =
                this.eventRWFaultCurrent.getData(input);
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultcurrent.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultlog.list.output.Data
    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultlog.list.output.PaginationBuilder
    //eventRWFaultLog
    public ReadFaultlogListOutputBuilder readFaultLogList(EntityInput input) {
        ReadFaultlogListOutputBuilder outputBuilder = new ReadFaultlogListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultlog.list.output.Data> result =
                this.eventRWFaultLog.getData(input);
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultlog.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.maintenance.list.output.Data
    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.maintenance.list.output.PaginationBuilder
    //maintenanceRW
    public ReadMaintenanceListOutputBuilder readMaintenanceList(EntityInput input) {
        ReadMaintenanceListOutputBuilder outputBuilder = new ReadMaintenanceListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.maintenance.list.output.Data> result =
                this.maintenanceRW.getData(input);
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.maintenance.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.mediator.server.list.output.Data
    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.mediator.server.list.output.Pagination
    //mediatorserverRW
    public ReadMediatorServerListOutputBuilder readMediatorServerList(EntityInput input) {

        ReadMediatorServerListOutputBuilder outputBuilder = new ReadMediatorServerListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.mediator.server.list.output.Data> result =
                this.mediatorserverRW.getData(input);
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.mediator.server.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.network.element.connection.list.output.Data
    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.network.element.connection.list.output.PaginationBuilder
    //networkelementConnectionRW
    public ReadNetworkElementConnectionListOutputBuilder readNetworkElementConnectionList(EntityInput input) {
        ReadNetworkElementConnectionListOutputBuilder outputBuilder =
                new ReadNetworkElementConnectionListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.network.element.connection.list.output.Data> result =
                this.networkelementConnectionRW.getData(input);
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.network.element.connection.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.inventory.list.output.Data
    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.inventory.list.output.PaginationBuilder
    //equipmentRW
    public ReadInventoryListOutputBuilder readInventoryList(EntityInput input) {
        ReadInventoryListOutputBuilder outputBuilder = new ReadInventoryListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.inventory.list.output.Data> result =
                this.equipmentRW.getData(input);
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.inventory.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.inventory.list.output.Data
    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.inventory.list.output.PaginationBuilder
    //connnectionlogRW
    public ReadConnectionlogListOutputBuilder readConnectionlogList(EntityInput input) {
        ReadConnectionlogListOutputBuilder outputBuilder = new ReadConnectionlogListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.connectionlog.list.output.Data> result =
                this.connnectionlogRW.getData(input);
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.connectionlog.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.eventlog.list.output.Data
    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.eventlog.list.output.PaginationBuilder
    //eventlogRW
    public ReadEventlogListOutputBuilder readEventlogList(ReadEventlogListInput input) throws IOException {
        ReadEventlogListOutputBuilder outputBuilder = new ReadEventlogListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.eventlog.list.output.Data> result =
                this.eventlogRW.getData(input);
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.eventlog.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._15m.list.output.Data
    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._15m.list.output.PaginationBuilder
    public ReadPmdata15mListOutputBuilder readPmdata15mList(EntityInput input) {
        ReadPmdata15mListOutputBuilder outputBuilder = new ReadPmdata15mListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._15m.list.output.Data> result =
                this.pm15mRW.getData(input);
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._15m.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._24h.list.output.Data
    //org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._24h.list.output.Pagination
    public ReadPmdata24hListOutputBuilder readPmdata24hList(EntityInput input) {
        ReadPmdata24hListOutputBuilder outputBuilder = new ReadPmdata24hListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._24h.list.output.Data> result =
                this.pm24hRW.getData(input);
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._24h.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    public ReadPmdata15mLtpListOutputBuilder readPmdata15mLtpList(EntityInput input) throws IOException {
        ReadPmdata15mLtpListOutputBuilder outputBuilder = new ReadPmdata15mLtpListOutputBuilder();
        QueryResult<String> result = pm15mRW.getDataLtpList(input);
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._15m.ltp.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResult().getHits());
        return outputBuilder;
    }

    public ReadPmdata15mDeviceListOutputBuilder readPmdata15mDeviceList(EntityInput input) throws IOException {
        ReadPmdata15mDeviceListOutputBuilder outputBuilder = new ReadPmdata15mDeviceListOutputBuilder();
        QueryResult<String> result = pm15mRW.getDataDeviceList(input);
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._15m.device.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResult().getHits());
        return outputBuilder;
    }

    public ReadPmdata24hLtpListOutputBuilder readPmdata24hLtpList(EntityInput input) throws IOException {

        QueryResult<String> result = pm24hRW.getDataLtpList(input);

        ReadPmdata24hLtpListOutputBuilder outputBuilder = new ReadPmdata24hLtpListOutputBuilder();
        new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._24h.ltp.list.output.PaginationBuilder();
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._24h.ltp.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResult().getHits());
        return outputBuilder;
    }

    public ReadPmdata24hDeviceListOutputBuilder readPmdata24hDeviceList(EntityInput input) throws IOException {

        QueryResult<String> result = pm24hRW.getDataDeviceList(input);

        ReadPmdata24hDeviceListOutputBuilder outputBuilder = new ReadPmdata24hDeviceListOutputBuilder();
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._24h.device.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResult().getHits());
        return outputBuilder;
    }

    public ReadStatusOutputBuilder readStatus() throws IOException {
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.status.output.Data> result =
                readStatus.getDataStatus();

        ReadStatusOutputBuilder outputBuilder = new ReadStatusOutputBuilder();
        outputBuilder.setData(result.getResult().getHits());
        return outputBuilder;
    }

    public boolean waitForYellowDatabaseStatus(long timeout, TimeUnit unit) {
        return this.dbClient.waitForYellowStatus(unit.toMillis(timeout));
    }

    public CreateNetworkElementConnectionOutputBuilder createNetworkElementConnection(
            NetworkElementConnectionEntity input) throws IOException {
        String id = this.networkelementConnectionRW.update(input, input.getNodeId());
        if (id == null) {
            throw new IOException(EXCEPTION_UNABLE_TO_WRITE_IN_DATABASE);
        }
        CreateNetworkElementConnectionOutputBuilder builder = new CreateNetworkElementConnectionOutputBuilder();
        builder.setId(id).setNodeId(input.getNodeId()).setHost(input.getHost()).setPort(input.getPort())
                .setUsername(input.getUsername()).setPassword(input.getPassword()).setIsRequired(input.isIsRequired())
                .setCoreModelCapability(input.getCoreModelCapability()).setDeviceType(input.getDeviceType());
        return builder;
    }

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

    public DeleteNetworkElementConnectionOutputBuilder deleteNetworkElementConnection(
            DeleteNetworkElementConnectionInput input) throws IOException {
        boolean removed = this.networkelementConnectionRW.remove(input.getId());
        if (!removed) {
            throw new IOException(EXCEPTION_UNABLE_TO_REMOVE_FROM_DATABASE);
        }
        return new DeleteNetworkElementConnectionOutputBuilder();
    }

    public DeleteMediatorServerOutputBuilder deleteMediatorServer(DeleteMediatorServerInput input) throws IOException {
        boolean removed = this.mediatorserverRW.remove(input.getId());
        if (!removed) {
            throw new IOException(EXCEPTION_UNABLE_TO_REMOVE_FROM_DATABASE);
        }
        return new DeleteMediatorServerOutputBuilder();
    }

    public DeleteMaintenanceOutputBuilder deleteMaintenance(DeleteMaintenanceInput input) throws IOException {
        boolean removed = this.maintenanceRW.remove(input.getId());
        if (!removed) {
            throw new IOException(EXCEPTION_UNABLE_TO_REMOVE_FROM_DATABASE);
        }
        return new DeleteMaintenanceOutputBuilder();
    }

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

    public UpdateMediatorServerOutputBuilder updateMediatorServer(UpdateMediatorServerInput input) throws IOException {
        if (input.getId() == null) {
            throw new IOException("please give the id for updating entry");
        }
        String id = this.mediatorserverRW.update(input, input.getId());
        if (id == null) {
            throw new IOException(EXCEPTION_UNABLE_TO_UPDATE_IN_DATABASE);
        }
        UpdateMediatorServerOutputBuilder builder = new UpdateMediatorServerOutputBuilder();
        builder.setId(id).setName(input.getName()).setUrl(input.getUrl());
        return builder;
    }

    public CreateMaintenanceOutputBuilder createMaintenance(CreateMaintenanceInput input) throws IOException {
        String id = this.maintenanceRW.write(input, input.getNodeId());
        if (id == null) {
            throw new IOException(EXCEPTION_UNABLE_TO_WRITE_IN_DATABASE);
        }
        CreateMaintenanceOutputBuilder builder = new CreateMaintenanceOutputBuilder(input).setId(id);
        return builder;
    }

    public CreateMediatorServerOutputBuilder createMediatorServer(CreateMediatorServerInput input) throws IOException {
        String id = this.mediatorserverRW.write(input, null);

        if (id == null) {
            throw new IOException(EXCEPTION_UNABLE_TO_WRITE_IN_DATABASE);
        }
        CreateMediatorServerOutputBuilder builder = new CreateMediatorServerOutputBuilder();
        builder.setId(id).setName(input.getName()).setUrl(input.getUrl());
        return builder;
    }

    public DataProvider getDataProvider() {
        return this.databaseService;
    }

    public HtDatabaseMaintenance getHtDatabaseMaintenance() {
        return this.databaseMaintenanceService;
    }

}
