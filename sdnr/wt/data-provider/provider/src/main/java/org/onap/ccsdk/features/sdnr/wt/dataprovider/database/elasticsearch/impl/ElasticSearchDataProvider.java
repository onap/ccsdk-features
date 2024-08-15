/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.EsConfig;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.acessor.DataObjectAcessorInventory;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.acessor.DataObjectAcessorPm;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.acessor.DataObjectAcessorPm.Intervall;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.acessor.DataObjectAcessorStatus;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.acessor.DataObjectAcessorWithId;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.entity.HtDatabaseEventsService;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.entity.HtDatabaseMaintenanceService;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.rpctypehelper.QueryResult;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.impl.DataTreeProviderImpl;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DatabaseDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMediatorserver;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtUserdataManager;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.InventoryTreeProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMediatorServerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMediatorServerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Guicutthrough;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMediatorServerListOutputBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMediatorServerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class ElasticSearchDataProvider implements DatabaseDataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchDataProvider.class);

    private static final String EXCEPTION_UNABLE_TO_WRITE_IN_DATABASE = "unable to write data to database";
    private static final String EXCEPTION_UNABLE_TO_UPDATE_IN_DATABASE = "unable to update data in database";
    private static final String EXCEPTION_UNABLE_TO_REMOVE_FROM_DATABASE = "unable to remove data from database";

    private final HtDatabaseClient dbClient;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.faultcurrent.list.output.Data> eventRWFaultCurrent;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.faultlog.list.output.Data> eventRWFaultLog;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.cmlog.list.output.Data> eventRWCMLog;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.mediator.server.list.output.Data> mediatorserverRW;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.maintenance.list.output.Data> maintenanceRW;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.gui.cut.through.entry.output.Data> guicutthroughRW;
    private final DataObjectAcessorInventory<Data> equipmentRW;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.connectionlog.list.output.Data> connnectionlogRW;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.eventlog.list.output.Data> eventlogRW;
    private final DataObjectAcessorWithId<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.network.element.connection.list.output.Data> networkelementConnectionRW;
    private final DataObjectAcessorPm<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data> pm15mRW;
    private final DataObjectAcessorPm<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.list.output.Data> pm24hRW;

    private final DataObjectAcessorStatus readStatus;
    private final HtDatabaseEventsService databaseService;
    private final HtDatabaseMaintenanceService databaseMaintenanceService;
    private final HtUserdataManager usermanager;
    private final InventoryTreeProvider inventoryTreeProvider;

    private final HtDatabaseMediatorserver dbMediatorServerService = new HtDatabaseMediatorserver() {

        @Override
        public List<MediatorServerEntity> getAll() {
            return new ArrayList<>(ElasticSearchDataProvider.this.mediatorserverRW.doReadAll().getHits());
        }
    };


    @Override
    public HtDatabaseClient getRawClient() {
        return this.dbClient;
    }

    public ElasticSearchDataProvider(HostInfo[] hosts) throws Exception {
        this(hosts, null, null, HtDatabaseClient.TRUSTALL_DEFAULT, false);
    }

    public ElasticSearchDataProvider(EsConfig esConfig) throws Exception {
        this(esConfig.getHosts(), esConfig.getBasicAuthUsername(), esConfig.getBasicAuthPassword(),
                esConfig.trustAllCerts(), esConfig.doFullsizeRequests());
    }

    public ElasticSearchDataProvider(HostInfo[] hosts, String authUsername, String authPassword, boolean trustAllCerts,
            boolean doFullsizeRequests) throws Exception {
        super();
        LOG.info("Start {}", this.getClass().getName());


        this.dbClient = HtDatabaseClient.getClient(hosts, authUsername, authPassword, trustAllCerts);
        this.mediatorserverRW = new DataObjectAcessorWithId<>(dbClient, Entity.MediatorServer,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.mediator.server.list.output.Data.class,
                doFullsizeRequests);
        this.mediatorserverRW.setWriteInterface(MediatorServerEntity.class);

        this.maintenanceRW = new DataObjectAcessorWithId<>(dbClient, Entity.Maintenancemode,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.maintenance.list.output.Data.class,
                doFullsizeRequests);
        this.maintenanceRW.setWriteInterface(MaintenanceEntity.class);

        this.guicutthroughRW = new DataObjectAcessorWithId<>(dbClient,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity.Guicutthrough,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.gui.cut.through.entry.output.Data.class,
                doFullsizeRequests);
        this.guicutthroughRW.setWriteInterface(Guicutthrough.class);

        this.equipmentRW = new DataObjectAcessorInventory<>(dbClient, Entity.Inventoryequipment,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.Data.class,
                doFullsizeRequests);

        this.eventRWFaultCurrent = new DataObjectAcessorWithId<>(dbClient, Entity.Faultcurrent,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.faultcurrent.list.output.Data.class,
                doFullsizeRequests);

        this.eventRWFaultLog = new DataObjectAcessorWithId<>(dbClient, Entity.Faultlog,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.faultlog.list.output.Data.class,
                doFullsizeRequests);

        this.eventRWCMLog = new DataObjectAcessorWithId<>(dbClient, Entity.Cmlog,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.cmlog.list.output.Data.class,
                doFullsizeRequests);

        this.connnectionlogRW = new DataObjectAcessorWithId<>(dbClient, Entity.Connectionlog,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.connectionlog.list.output.Data.class,
                doFullsizeRequests);

        this.eventlogRW = new DataObjectAcessorWithId<>(dbClient, Entity.Eventlog,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.eventlog.list.output.Data.class,
                doFullsizeRequests);

        this.networkelementConnectionRW = new DataObjectAcessorWithId<>(dbClient, Entity.NetworkelementConnection,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.network.element.connection.list.output.Data.class,
                doFullsizeRequests);
        this.networkelementConnectionRW.setWriteInterface(NetworkElementConnectionEntity.class);

        this.pm15mRW = new DataObjectAcessorPm<>(dbClient, Intervall.PMDATA15M, Entity.Historicalperformance15min,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data.class,
                doFullsizeRequests);

        this.pm24hRW = new DataObjectAcessorPm<>(dbClient, Intervall.PMDATA24H, Entity.Historicalperformance24h,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.list.output.Data.class,
                doFullsizeRequests);

        this.readStatus = new DataObjectAcessorStatus(dbClient, Entity.Faultcurrent, doFullsizeRequests);

        this.databaseService = new HtDatabaseEventsService(dbClient);
        this.databaseMaintenanceService = new HtDatabaseMaintenanceService(dbClient);
        this.usermanager = new HtUserdataManagerImpl(this.dbClient);
        this.inventoryTreeProvider = new DataTreeProviderImpl(this.dbClient);
    }

    /*-------------------------
     * Provide access to model API
     */


    @Override
    public ReadFaultcurrentListOutputBuilder readFaultCurrentList(EntityInput input) {

        ReadFaultcurrentListOutputBuilder outputBuilder = new ReadFaultcurrentListOutputBuilder();

        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.faultcurrent.list.output.Data> result =
                this.eventRWFaultCurrent.getData(input);
        outputBuilder.setData(result.getResult().getHits());
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
        outputBuilder.setData(result.getResult().getHits());
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
        outputBuilder.setData(result.getResult().getHits());
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
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.maintenance.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadMediatorServerListOutputBuilder readMediatorServerList(EntityInput input) {

        ReadMediatorServerListOutputBuilder outputBuilder = new ReadMediatorServerListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.mediator.server.list.output.Data> result =
                this.mediatorserverRW.getData(input);
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.mediator.server.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadNetworkElementConnectionListOutputBuilder readNetworkElementConnectionList(EntityInput input) {
        ReadNetworkElementConnectionListOutputBuilder outputBuilder =
                new ReadNetworkElementConnectionListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.network.element.connection.list.output.Data> result =
                this.networkelementConnectionRW.getData(input);
        outputBuilder.setData(result.getResult().getHits());
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
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadInventoryDeviceListOutputBuilder readInventoryDeviceList(EntityInput input) {
        ReadInventoryDeviceListOutputBuilder outputBuilder = new ReadInventoryDeviceListOutputBuilder();
        QueryResult<String> result = equipmentRW.getDataDeviceList(input);
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.device.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResult().getHitSets());
        return outputBuilder;
    }

    @Override
    public ReadConnectionlogListOutputBuilder readConnectionlogList(EntityInput input) {
        ReadConnectionlogListOutputBuilder outputBuilder = new ReadConnectionlogListOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.connectionlog.list.output.Data> result =
                this.connnectionlogRW.getData(input);
        outputBuilder.setData(result.getResult().getHits());
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
        outputBuilder.setData(result.getResult().getHits());
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
        outputBuilder.setData(result.getResult().getHits());
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
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.list.output.PaginationBuilder(
                        result.getPagination()).build());
        return outputBuilder;
    }

    @Override
    public ReadPmdata15mLtpListOutputBuilder readPmdata15mLtpList(EntityInput input) throws IOException {
        ReadPmdata15mLtpListOutputBuilder outputBuilder = new ReadPmdata15mLtpListOutputBuilder();
        QueryResult<String> result = pm15mRW.getDataLtpList(input);
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.ltp.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResult().getHitSets());
        return outputBuilder;
    }

    @Override
    public ReadPmdata15mDeviceListOutputBuilder readPmdata15mDeviceList(EntityInput input) throws IOException {
        ReadPmdata15mDeviceListOutputBuilder outputBuilder = new ReadPmdata15mDeviceListOutputBuilder();
        QueryResult<String> result = pm15mRW.getDataDeviceList(input);
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.device.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResult().getHitSets());
        return outputBuilder;
    }

    @Override
    public ReadPmdata24hLtpListOutputBuilder readPmdata24hLtpList(EntityInput input) throws IOException {

        QueryResult<String> result = pm24hRW.getDataLtpList(input);

        ReadPmdata24hLtpListOutputBuilder outputBuilder = new ReadPmdata24hLtpListOutputBuilder();
        new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.ltp.list.output.PaginationBuilder();
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.ltp.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResult().getHitSets());
        return outputBuilder;
    }

    @Override
    public ReadPmdata24hDeviceListOutputBuilder readPmdata24hDeviceList(EntityInput input) throws IOException {

        QueryResult<String> result = pm24hRW.getDataDeviceList(input);

        ReadPmdata24hDeviceListOutputBuilder outputBuilder = new ReadPmdata24hDeviceListOutputBuilder();
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.device.list.output.PaginationBuilder(
                        result.getPagination()).build());
        outputBuilder.setData(result.getResult().getHitSets());
        return outputBuilder;
    }


    @Override
    public ReadStatusOutputBuilder readStatus(EntityInput input) throws IOException {
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.status.output.Data> result =
                readStatus.getDataStatus(input);

        ReadStatusOutputBuilder outputBuilder = new ReadStatusOutputBuilder();
        outputBuilder.setData(result.getResult().getHits());
        return outputBuilder;
    }

    @Override
    public boolean waitForYellowDatabaseStatus(long timeout, TimeUnit unit) {
        return this.dbClient.waitForYellowStatus(unit.toMillis(timeout));
    }

    @Override
    public CreateNetworkElementConnectionOutputBuilder createNetworkElementConnection(
            NetworkElementConnectionEntity input) throws IOException {
        String id = this.networkelementConnectionRW.update(input, input.getNodeId());
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
        boolean removed = this.networkelementConnectionRW.remove(input.getId());
        if (!removed) {
            throw new IOException(EXCEPTION_UNABLE_TO_REMOVE_FROM_DATABASE);
        }
        return new DeleteNetworkElementConnectionOutputBuilder();
    }

    @Override
    public DeleteMediatorServerOutputBuilder deleteMediatorServer(DeleteMediatorServerInput input) throws IOException {
        boolean removed = this.mediatorserverRW.remove(input.getId());
        if (!removed) {
            throw new IOException(EXCEPTION_UNABLE_TO_REMOVE_FROM_DATABASE);
        }
        return new DeleteMediatorServerOutputBuilder();
    }

    @Override
    public DeleteMaintenanceOutputBuilder deleteMaintenance(DeleteMaintenanceInput input) throws IOException {
        boolean removed = this.maintenanceRW.remove(input.getId());
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
    public CreateMediatorServerOutputBuilder createMediatorServer(CreateMediatorServerInput input) throws IOException {
        String id = this.mediatorserverRW.write(input, null);

        if (id == null) {
            throw new IOException(EXCEPTION_UNABLE_TO_WRITE_IN_DATABASE);
        }
        CreateMediatorServerOutputBuilder builder = new CreateMediatorServerOutputBuilder();
        builder.setId(id).setName(input.getName()).setUrl(input.getUrl());
        return builder;
    }

    @Override
    public ReadGuiCutThroughEntryOutputBuilder readGuiCutThroughEntry(EntityInput input) {
        ReadGuiCutThroughEntryOutputBuilder outputBuilder = new ReadGuiCutThroughEntryOutputBuilder();
        QueryResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.gui.cut.through.entry.output.Data> result =
                this.guicutthroughRW.getData(input);
        outputBuilder.setData(result.getResult().getHits());
        outputBuilder.setPagination(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.gui.cut.through.entry.output.PaginationBuilder()
                        .build());
        return outputBuilder;
    }

    @Override
    public DataProvider getDataProvider() {
        return this.databaseService;
    }

    @Override
    public HtDatabaseMaintenance getHtDatabaseMaintenance() {
        return this.databaseMaintenanceService;
    }

    @Override
    public HtDatabaseMediatorserver getHtDatabaseMediatorServer() {
        return dbMediatorServerService;
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
