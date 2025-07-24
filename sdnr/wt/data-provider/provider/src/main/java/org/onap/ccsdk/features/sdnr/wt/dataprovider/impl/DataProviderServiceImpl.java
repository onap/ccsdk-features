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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.impl;

import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.nodb.NoDbDatabaseDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlDBDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DatabaseDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtUserdataManager;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.InventoryTreeProvider;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.keystore.rev240708.Keystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.keystore.rev240708.keystore.entry.KeyCredential;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.keystore.rev240708.keystore.entry.KeyCredentialKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMediatorServer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMediatorServerOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateNetworkElementConnection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateNetworkElementConnectionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenanceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMediatorServer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMediatorServerOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteNetworkElementConnection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteNetworkElementConnectionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadCmlogList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadCmlogListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadCmlogListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadConnectionlogList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadConnectionlogListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadConnectionlogListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadEventlogList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadEventlogListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadEventlogListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultcurrentList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultcurrentListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultcurrentListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultlogList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultlogListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultlogListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadGuiCutThroughEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadGuiCutThroughEntryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadGuiCutThroughEntryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryDeviceList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryDeviceListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryDeviceListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMaintenanceList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMaintenanceListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMaintenanceListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMediatorServerList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMediatorServerListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMediatorServerListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadNetworkElementConnectionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadNetworkElementConnectionListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadNetworkElementConnectionListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mDeviceList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mDeviceListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mDeviceListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mLtpList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mLtpListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mLtpListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hDeviceList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hDeviceListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hDeviceListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hLtpList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hLtpListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hLtpListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadStatusInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadStatusOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadTlsKeyEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadTlsKeyEntryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadTlsKeyEntryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadTlsKeyEntryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMaintenance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMaintenanceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMediatorServer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMediatorServerOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.tls.key.entry.output.Pagination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.tls.key.entry.output.PaginationBuilder;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProviderServiceImpl implements DataProviderService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DataProviderServiceImpl.class);
    public static final String CONFIGURATIONFILE = "etc/dataprovider.properties";
    private static final long DATABASE_TIMEOUT_MS = 120 * 1000L;
    private static final @NonNull DataObjectIdentifier<Keystore> KEYSTORE_IIF = DataObjectIdentifier.builder(
            Keystore.class).build();
    private static final Pagination EMPTY_PAGINATION = new PaginationBuilder().setSize(Uint32.valueOf(20))
            .setTotal(Uint64.valueOf(0)).setPage(Uint64.valueOf(1)).build();
    private static final long DEFAULT_PAGESIZE = 20;
    private static final long DEFAULT_PAGE = 1;

    private final Registration rpcReg;
    private final DatabaseDataProvider dataProvider;
    private final DataBroker dataBroker;

    public DataProviderServiceImpl(final RpcProviderService rpcProviderService, DataBroker dataBroker) {
        var configuration = new ConfigurationFileRepresentation(CONFIGURATIONFILE);
        var dbConfig = new DataProviderConfig(configuration);
        this.dataBroker = dataBroker;
        if (dbConfig.isEnabled()) {
            this.dataProvider = new SqlDBDataProvider(dbConfig.getMariadbConfig(),
                    dbConfig.getGuicutthroughOverride());
        } else {
            this.dataProvider = new NoDbDatabaseDataProvider(dataBroker);
        }
        this.dataProvider.waitForYellowDatabaseStatus(DATABASE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        // Register ourselves as the REST API RPC implementation
        LOG.info("Register RPC Service {}", DataProviderServiceImpl.class.getSimpleName());
        this.rpcReg = rpcProviderService.registerRpcImplementations(
                List.of(new RpcHelper<>(ReadFaultcurrentList.class, DataProviderServiceImpl.this::readFaultcurrentList),
                        new RpcHelper<>(ReadFaultlogList.class, DataProviderServiceImpl.this::readFaultlogList),
                        new RpcHelper<>(ReadCmlogList.class, DataProviderServiceImpl.this::readCmlogList),
                        new RpcHelper<>(ReadMaintenanceList.class, DataProviderServiceImpl.this::readMaintenanceList),
                        new RpcHelper<>(ReadMediatorServerList.class,
                                DataProviderServiceImpl.this::readMediatorServerList),
                        new RpcHelper<>(ReadNetworkElementConnectionList.class,
                                DataProviderServiceImpl.this::readNetworkElementConnectionList),
                        new RpcHelper<>(ReadPmdata15mList.class, DataProviderServiceImpl.this::readPmdata15mList),
                        new RpcHelper<>(ReadPmdata24hList.class, DataProviderServiceImpl.this::readPmdata24hList),
                        new RpcHelper<>(ReadStatus.class, DataProviderServiceImpl.this::readStatus),
                        new RpcHelper<>(ReadInventoryList.class, DataProviderServiceImpl.this::readInventoryList),
                        new RpcHelper<>(ReadInventoryDeviceList.class,
                                DataProviderServiceImpl.this::readInventoryDeviceList),
                        new RpcHelper<>(ReadPmdata15mLtpList.class, DataProviderServiceImpl.this::readPmdata15mLtpList),
                        new RpcHelper<>(ReadPmdata15mDeviceList.class,
                                DataProviderServiceImpl.this::readPmdata15mDeviceList),
                        new RpcHelper<>(ReadPmdata24hLtpList.class, DataProviderServiceImpl.this::readPmdata24hLtpList),
                        new RpcHelper<>(ReadPmdata24hDeviceList.class,
                                DataProviderServiceImpl.this::readPmdata24hDeviceList),
                        new RpcHelper<>(ReadConnectionlogList.class,
                                DataProviderServiceImpl.this::readConnectionlogList),
                        new RpcHelper<>(CreateNetworkElementConnection.class,
                                DataProviderServiceImpl.this::createNetworkElementConnection),
                        new RpcHelper<>(UpdateNetworkElementConnection.class,
                                DataProviderServiceImpl.this::updateNetworkElementConnection),
                        new RpcHelper<>(DeleteNetworkElementConnection.class,
                                DataProviderServiceImpl.this::deleteNetworkElementConnection),
                        new RpcHelper<>(DeleteMediatorServer.class, DataProviderServiceImpl.this::deleteMediatorServer),
                        new RpcHelper<>(CreateMediatorServer.class, DataProviderServiceImpl.this::createMediatorServer),
                        new RpcHelper<>(CreateMaintenance.class, DataProviderServiceImpl.this::createMaintenance),
                        new RpcHelper<>(DeleteMaintenance.class, DataProviderServiceImpl.this::deleteMaintenance),
                        new RpcHelper<>(UpdateMediatorServer.class, DataProviderServiceImpl.this::updateMediatorServer),
                        new RpcHelper<>(UpdateMaintenance.class, DataProviderServiceImpl.this::updateMaintenance),
                        new RpcHelper<>(ReadEventlogList.class, DataProviderServiceImpl.this::readEventlogList),
                        new RpcHelper<>(ReadGuiCutThroughEntry.class,
                                DataProviderServiceImpl.this::readGuiCutThroughEntry),
                        new RpcHelper<>(ReadTlsKeyEntry.class, DataProviderServiceImpl.this::readTlsKeyEntry)
                ));


    }

    /**
     * @return dataProvider
     */
    public DataProvider getDataProvider() {
        return dataProvider.getDataProvider();
    }


    /**
     * @return data provider for Maintenance()
     */
    public HtDatabaseMaintenance getHtDatabaseMaintenance() {
        return dataProvider.getHtDatabaseMaintenance();
    }


    @Override
    public void close() {
        LOG.info("Close RPC Service");
        if (rpcReg != null) {
            rpcReg.close();
        }
    }

    @Override
    public ListenableFuture<RpcResult<ReadFaultcurrentListOutput>> readFaultcurrentList(
            ReadFaultcurrentListInput input) {
        LOG.debug("RPC Request: readFaultCurrentList with input {}", input);
        RpcResultBuilder<ReadFaultcurrentListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readFaultCurrentList(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReadFaultlogListOutput>> readFaultlogList(ReadFaultlogListInput input) {
        LOG.debug("RPC Request: readFaultlogList with input {}", input);
        RpcResultBuilder<ReadFaultlogListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readFaultLogList(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReadCmlogListOutput>> readCmlogList(ReadCmlogListInput input) {
        LOG.debug("RPC Request: readCMlogList with input {}", input);
        RpcResultBuilder<ReadCmlogListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readCMLogList(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReadMaintenanceListOutput>> readMaintenanceList(ReadMaintenanceListInput input) {
        LOG.debug("RPC Request: readMaintenanceList with input {}", input);
        RpcResultBuilder<ReadMaintenanceListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readMaintenanceList(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReadMediatorServerListOutput>> readMediatorServerList(
            ReadMediatorServerListInput input) {
        LOG.debug("RPC Request: readMediatorServerList with input {}", input);
        throw new RuntimeException("not supported anymore");
    }

    @Override
    public ListenableFuture<RpcResult<ReadNetworkElementConnectionListOutput>> readNetworkElementConnectionList(
            ReadNetworkElementConnectionListInput input) {
        LOG.debug("RPC Request: readNetworkElementConnectionList with input {}", input);
        RpcResultBuilder<ReadNetworkElementConnectionListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readNetworkElementConnectionList(input));
        return result.buildFuture();

    }

    @Override
    public ListenableFuture<RpcResult<ReadPmdata15mListOutput>> readPmdata15mList(ReadPmdata15mListInput input) {
        LOG.debug("RPC Request: readPmdata15mList with input {}", input);
        RpcResultBuilder<ReadPmdata15mListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readPmdata15mList(input));
        return result.buildFuture();

    }

    @Override
    public ListenableFuture<RpcResult<ReadPmdata24hListOutput>> readPmdata24hList(ReadPmdata24hListInput input) {
        LOG.debug("RPC Request: readPmdata24hList with input {}", input);
        RpcResultBuilder<ReadPmdata24hListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readPmdata24hList(input));
        return result.buildFuture();

    }

    @Override
    public ListenableFuture<RpcResult<ReadStatusOutput>> readStatus(ReadStatusInput input) {
        LOG.debug("RPC Request: readStatusEntityList with input {}", input);
        RpcResultBuilder<ReadStatusOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readStatus(input));
        return result.buildFuture();

    }

    @Override
    public ListenableFuture<RpcResult<ReadInventoryListOutput>> readInventoryList(ReadInventoryListInput input) {
        LOG.debug("RPC Request: readInventoryList with input {}", input);
        RpcResultBuilder<ReadInventoryListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readInventoryList(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReadInventoryDeviceListOutput>> readInventoryDeviceList(
            ReadInventoryDeviceListInput input) {
        LOG.debug("RPC Request: readInventoryDeviceList with input {}", input);
        RpcResultBuilder<ReadInventoryDeviceListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readInventoryDeviceList(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReadPmdata15mLtpListOutput>> readPmdata15mLtpList(
            ReadPmdata15mLtpListInput input) {
        LOG.debug("RPC Request: readPmdata15mLtpList with input {}", input);
        RpcResultBuilder<ReadPmdata15mLtpListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readPmdata15mLtpList(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReadPmdata15mDeviceListOutput>> readPmdata15mDeviceList(
            ReadPmdata15mDeviceListInput input) {
        LOG.debug("RPC Request: readPmdata15mDeviceList with input {}", input);
        RpcResultBuilder<ReadPmdata15mDeviceListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readPmdata15mDeviceList(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReadPmdata24hLtpListOutput>> readPmdata24hLtpList(
            ReadPmdata24hLtpListInput input) {
        LOG.debug("RPC Request: readPmdata24hLtpList with input {}", input);
        RpcResultBuilder<ReadPmdata24hLtpListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readPmdata24hLtpList(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReadPmdata24hDeviceListOutput>> readPmdata24hDeviceList(
            ReadPmdata24hDeviceListInput input) {
        LOG.debug("RPC Request: readPmdata24hDeviceList with input {}", input);
        RpcResultBuilder<ReadPmdata24hDeviceListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readPmdata24hDeviceList(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReadConnectionlogListOutput>> readConnectionlogList(
            ReadConnectionlogListInput input) {
        LOG.debug("RPC Request: readConnectionlogList with input {}", input);
        RpcResultBuilder<ReadConnectionlogListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readConnectionlogList(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<CreateNetworkElementConnectionOutput>> createNetworkElementConnection(
            CreateNetworkElementConnectionInput input) {
        LOG.debug("RPC Request: createNetworkElementConnection with input {}", input);
        RpcResultBuilder<CreateNetworkElementConnectionOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.createNetworkElementConnection(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<UpdateNetworkElementConnectionOutput>> updateNetworkElementConnection(
            UpdateNetworkElementConnectionInput input) {
        LOG.debug("RPC Request: updateNetworkElementConnection with input {}", input);
        RpcResultBuilder<UpdateNetworkElementConnectionOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.updateNetworkElementConnection(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<DeleteNetworkElementConnectionOutput>> deleteNetworkElementConnection(
            DeleteNetworkElementConnectionInput input) {
        LOG.debug("RPC Request: deleteNetworkElementConnection with input {}", input);
        RpcResultBuilder<DeleteNetworkElementConnectionOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.deleteNetworkElementConnection(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<DeleteMediatorServerOutput>> deleteMediatorServer(
            DeleteMediatorServerInput input) {
        LOG.debug("RPC Request: deleteMediatorServer with input {}", input);
        throw new RuntimeException("not supported anymore");
    }

    @Override
    public ListenableFuture<RpcResult<CreateMediatorServerOutput>> createMediatorServer(
            CreateMediatorServerInput input) {
        LOG.debug("RPC Request: createMediatorServer with input {}", input);
        throw new RuntimeException("not supported anymore");
    }

    @Override
    public ListenableFuture<RpcResult<CreateMaintenanceOutput>> createMaintenance(CreateMaintenanceInput input) {
        LOG.debug("RPC Request: createMaintenance with input {}", input);
        RpcResultBuilder<CreateMaintenanceOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.createMaintenance(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<DeleteMaintenanceOutput>> deleteMaintenance(DeleteMaintenanceInput input) {
        LOG.debug("RPC Request: deleteMaintenance with input {}", input);
        RpcResultBuilder<DeleteMaintenanceOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.deleteMaintenance(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<UpdateMediatorServerOutput>> updateMediatorServer(
            UpdateMediatorServerInput input) {
        LOG.debug("RPC Request: updateMediatorServer with input {}", input);
        throw new RuntimeException("not supported anymore");
    }

    @Override
    public ListenableFuture<RpcResult<UpdateMaintenanceOutput>> updateMaintenance(UpdateMaintenanceInput input) {
        LOG.debug("RPC Request: updateMaintenance with input {}", input);
        RpcResultBuilder<UpdateMaintenanceOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.updateMaintenance(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReadEventlogListOutput>> readEventlogList(ReadEventlogListInput input) {
        LOG.debug("RPC Request: readEventlogList with input {}", input);
        RpcResultBuilder<ReadEventlogListOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readEventlogList(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReadGuiCutThroughEntryOutput>> readGuiCutThroughEntry(
            ReadGuiCutThroughEntryInput input) {
        LOG.debug("RPC Request: getGuiCutThroughEntry with input {}", input);
        RpcResultBuilder<ReadGuiCutThroughEntryOutput> result =
                read(() -> DataProviderServiceImpl.this.dataProvider.readGuiCutThroughEntry(input));
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<ReadTlsKeyEntryOutput>> readTlsKeyEntry(ReadTlsKeyEntryInput input) {
        LOG.debug("RPC Request: readTlsKeyEntry with input {}", input);
        RpcResultBuilder<ReadTlsKeyEntryOutput> result = read(() -> DataProviderServiceImpl.this.readTlsKeys(input));
        return result.buildFuture();
    }

    // -- private classes and functions

    private ReadTlsKeyEntryOutputBuilder readTlsKeys(ReadTlsKeyEntryInput input) {
        Optional<Keystore> result = Optional.empty();
        // The implicite close is not handled correctly by underlaying opendaylight netconf service
        try (ReadTransaction transaction = this.dataBroker.newReadOnlyTransaction()) {
            try {
                result = transaction.read(LogicalDatastoreType.CONFIGURATION, KEYSTORE_IIF).get();
            } catch (ExecutionException e) {
                LOG.warn("problem reading netconf-keystore: ", e);

            } catch (InterruptedException e) {
                LOG.warn("Interrupted!", e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }
        ReadTlsKeyEntryOutputBuilder output = new ReadTlsKeyEntryOutputBuilder();
        if (result.isEmpty()) {
            return output.setData(Set.of()).setPagination(EMPTY_PAGINATION);
        }
        Map<KeyCredentialKey, KeyCredential> keyCredential = result.get().getKeyCredential();
        if (keyCredential == null) {
            return output.setData(Set.of()).setPagination(EMPTY_PAGINATION);
        }
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Pagination pagination =
                input.getPagination();
        long pageNum = pagination == null ? DEFAULT_PAGE
                : pagination.getPage() == null ? DEFAULT_PAGE : pagination.getPage().longValue();
        long size = pagination == null ? DEFAULT_PAGESIZE
                : pagination.getSize() == null ? DEFAULT_PAGESIZE : pagination.getSize().longValue();
        long from = pageNum > 0 ? (pageNum - 1) * size : 0;
        output.setData(keyCredential.keySet().stream().skip(from).limit(size).map(KeyCredentialKey::getKeyId)
                .collect(Collectors.toSet()));
        output.setPagination(new PaginationBuilder().setPage(Uint64.valueOf(pageNum))
                .setSize(Uint32.valueOf(output.getData().size())).setTotal(Uint64.valueOf(keyCredential.size()))
                .build());
        return output;
    }

    private static String assembleExceptionMessage(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        var buf = new StringBuilder();
        buf.append("Exception: ");
        buf.append(sw);
        return buf.toString();
    }

    public InventoryTreeProvider getInventoryTreeProvider() {
        return this.dataProvider.getInventoryTreeProvider();
    }

    private interface GetEntityInput<O extends DataObject, B> {
        B get() throws IOException;
    }

    private static <O extends DataObject, B> RpcResultBuilder<O> read(
            GetEntityInput<O, B> inputgetter) {
        RpcResultBuilder<O> result;
        try {
            B outputBuilder = inputgetter.get();
            result = RpcResultBuilder.success(YangToolsMapperHelper.callBuild(outputBuilder));
        } catch (Exception e) {
            LOG.info("Exception", e);
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, assembleExceptionMessage(e));
        }
        return result;
    }


    public HtUserdataManager getHtDatabaseUserManager() {
        return this.dataProvider.getUserManager();
    }

    private interface RpcExecutionWrapper<I extends RpcInput, O extends RpcOutput> {

        ListenableFuture<@NonNull RpcResult<@NonNull O>> execute(@NonNull I input);
    }

    private static class RpcHelper<I extends RpcInput, O extends RpcOutput> implements Rpc<I, O> {

        private final RpcExecutionWrapper<I, O> executor;
        private final Class<? extends Rpc<I, O>> implementedInterface;

        RpcHelper(Class<? extends Rpc<I, O>> implementedInterface, RpcExecutionWrapper<I, O> executor) {
            this.implementedInterface = implementedInterface;
            this.executor = executor;
        }

        @Override
        public @NonNull ListenableFuture<@NonNull RpcResult<@NonNull O>> invoke(@NonNull I input) {
            return this.executor.execute(input);
        }

        @Override
        public @NonNull Class<? extends Rpc<I, O>> implementedInterface() {
            return this.implementedInterface;
        }
    }
}
