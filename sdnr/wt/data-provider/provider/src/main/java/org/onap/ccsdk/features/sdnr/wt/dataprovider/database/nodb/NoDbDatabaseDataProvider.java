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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.nodb;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
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

public class NoDbDatabaseDataProvider implements DatabaseDataProvider {

    private final HtUserdataManager usermanger;
    private final HtDatabaseMediatorserver mediatorserver;
    private final HtDatabaseMaintenance maintenance;
    private final DataProvider dataprovider;
    private final InventoryTreeProvider inventoryTreeProvider;

    public NoDbDatabaseDataProvider() {
        this.usermanger = new NoDbHtUserdataManager();
        this.mediatorserver = new NoDbHtDatabaseMediatorserver();
        this.maintenance = new NoDbHtDatabaseMaintenance();
        this.dataprovider = new NoDbDataProvider();
        this.inventoryTreeProvider = new NoDbInventoryTreeProvider();
    }
    @Override
    public HtDatabaseClient getRawClient() {
        return null;
    }

    @Override
    public ReadFaultcurrentListOutputBuilder readFaultCurrentList(EntityInput input) {
        return new ReadFaultcurrentListOutputBuilder();
    }

    @Override
    public ReadFaultlogListOutputBuilder readFaultLogList(EntityInput input) {
        return new ReadFaultlogListOutputBuilder();
    }

    @Override
    public ReadCmlogListOutputBuilder readCMLogList(EntityInput input) {
        return new ReadCmlogListOutputBuilder();
    }

    @Override
    public ReadMaintenanceListOutputBuilder readMaintenanceList(EntityInput input) {
        return new ReadMaintenanceListOutputBuilder();
    }

    @Override
    public ReadMediatorServerListOutputBuilder readMediatorServerList(EntityInput input) {
        return new ReadMediatorServerListOutputBuilder();
    }

    @Override
    public ReadNetworkElementConnectionListOutputBuilder readNetworkElementConnectionList(EntityInput input) {
        return new ReadNetworkElementConnectionListOutputBuilder();
    }

    @Override
    public ReadInventoryListOutputBuilder readInventoryList(EntityInput input) {
        return new ReadInventoryListOutputBuilder();
    }

    @Override
    public ReadConnectionlogListOutputBuilder readConnectionlogList(EntityInput input) {
        return new ReadConnectionlogListOutputBuilder();
    }

    @Override
    public ReadEventlogListOutputBuilder readEventlogList(EntityInput input) throws IOException {
        return new ReadEventlogListOutputBuilder();
    }

    @Override
    public ReadPmdata15mListOutputBuilder readPmdata15mList(EntityInput input) {
        return new ReadPmdata15mListOutputBuilder();
    }

    @Override
    public ReadPmdata24hListOutputBuilder readPmdata24hList(EntityInput input) {
        return new ReadPmdata24hListOutputBuilder();
    }

    @Override
    public ReadPmdata15mLtpListOutputBuilder readPmdata15mLtpList(EntityInput input) throws IOException {
        return new ReadPmdata15mLtpListOutputBuilder();
    }

    @Override
    public ReadPmdata15mDeviceListOutputBuilder readPmdata15mDeviceList(EntityInput input) throws IOException {
        return new ReadPmdata15mDeviceListOutputBuilder();
    }

    @Override
    public ReadPmdata24hLtpListOutputBuilder readPmdata24hLtpList(EntityInput input) throws IOException {
        return new ReadPmdata24hLtpListOutputBuilder();
    }

    @Override
    public ReadPmdata24hDeviceListOutputBuilder readPmdata24hDeviceList(EntityInput input) throws IOException {
        return new ReadPmdata24hDeviceListOutputBuilder();
    }

    @Override
    public ReadStatusOutputBuilder readStatus(EntityInput input) throws IOException {
        return new ReadStatusOutputBuilder();
    }

    @Override
    public boolean waitForYellowDatabaseStatus(long timeout, TimeUnit unit) {
        return true;
    }

    @Override
    public CreateNetworkElementConnectionOutputBuilder createNetworkElementConnection(
            NetworkElementConnectionEntity input) throws IOException {
        return new CreateNetworkElementConnectionOutputBuilder();
    }

    @Override
    public UpdateNetworkElementConnectionOutputBuilder updateNetworkElementConnection(
            UpdateNetworkElementConnectionInput input) throws IOException {
        return new UpdateNetworkElementConnectionOutputBuilder();
    }

    @Override
    public DeleteNetworkElementConnectionOutputBuilder deleteNetworkElementConnection(
            DeleteNetworkElementConnectionInput input) throws IOException {
        return new DeleteNetworkElementConnectionOutputBuilder();
    }

    @Override
    public DeleteMediatorServerOutputBuilder deleteMediatorServer(DeleteMediatorServerInput input) throws IOException {
        return new DeleteMediatorServerOutputBuilder();
    }

    @Override
    public DeleteMaintenanceOutputBuilder deleteMaintenance(DeleteMaintenanceInput input) throws IOException {
        return new DeleteMaintenanceOutputBuilder();
    }

    @Override
    public UpdateMaintenanceOutputBuilder updateMaintenance(UpdateMaintenanceInput input) throws IOException {
        return new UpdateMaintenanceOutputBuilder();
    }

    @Override
    public UpdateMediatorServerOutputBuilder updateMediatorServer(UpdateMediatorServerInput input) throws IOException {
        return new UpdateMediatorServerOutputBuilder();
    }

    @Override
    public CreateMaintenanceOutputBuilder createMaintenance(CreateMaintenanceInput input) throws IOException {
        return new CreateMaintenanceOutputBuilder();
    }

    @Override
    public CreateMediatorServerOutputBuilder createMediatorServer(CreateMediatorServerInput input) throws IOException {
        return new CreateMediatorServerOutputBuilder();
    }

    @Override
    public ReadGuiCutThroughEntryOutputBuilder readGuiCutThroughEntry(EntityInput input) {
        return new ReadGuiCutThroughEntryOutputBuilder();
    }

    @Override
    public DataProvider getDataProvider() {
        return dataprovider;
    }

    @Override
    public HtDatabaseMaintenance getHtDatabaseMaintenance() {
        return this.maintenance;
    }

    @Override
    public HtDatabaseMediatorserver getHtDatabaseMediatorServer() {
        return this.mediatorserver;
    }

    @Override
    public HtUserdataManager getUserManager() {
        return this.usermanger;
    }

    @Override
    public InventoryTreeProvider getInventoryTreeProvider() {
        return this.inventoryTreeProvider;
    }

    @Override
    public ReadInventoryDeviceListOutputBuilder readInventoryDeviceList(EntityInput input) {
        return new ReadInventoryDeviceListOutputBuilder();
    }
}
