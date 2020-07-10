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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.BaseRequest;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.ElasticSearchDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.YangToolsMapper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateMaintenanceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateMediatorServerInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateMediatorServerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateNetworkElementConnectionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.DeleteMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.DeleteMaintenanceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.DeleteMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.DeleteMediatorServerInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.DeleteNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.DeleteNetworkElementConnectionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Faultlog;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.GranularityPeriodType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadConnectionlogListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadConnectionlogListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadConnectionlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadEventlogListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadEventlogListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadEventlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadFaultcurrentListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadFaultcurrentListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadFaultcurrentListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadFaultlogListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadFaultlogListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadFaultlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadInventoryListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadInventoryListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadInventoryListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadMaintenanceListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadMaintenanceListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadMaintenanceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadMediatorServerListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadMediatorServerListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadMediatorServerListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadNetworkElementConnectionListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadNetworkElementConnectionListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadNetworkElementConnectionListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata15mDeviceListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata15mDeviceListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata15mDeviceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata15mListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata15mListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata15mListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata15mLtpListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata15mLtpListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata15mLtpListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata24hDeviceListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata24hDeviceListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata24hDeviceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata24hListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata24hListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata24hListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata24hLtpListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata24hLtpListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ReadPmdata24hLtpListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateMaintenanceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateMediatorServerInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateMediatorServerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateNetworkElementConnectionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.UpdateNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.FilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.Pagination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.PaginationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.mediator.server.list.output.Data;

public class TestCRUDforDatabase {

    private static ElasticSearchDataProvider dbProvider;
    private static HtDatabaseClient dbRawProvider;
    public static HostInfo[] hosts = new HostInfo[] {new HostInfo("localhost", Integer
            .valueOf(System.getProperty("databaseport") != null ? System.getProperty("databaseport") : "49200"))};

    @BeforeClass
    public static void init() throws Exception {

        dbProvider = new ElasticSearchDataProvider(hosts);
        dbProvider.waitForYellowDatabaseStatus(30, TimeUnit.SECONDS);
        dbRawProvider = new HtDatabaseClient(hosts);
    }

    public static void trySleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void trySleep() {
        trySleep(0);
    }

    @Test
    public void testStatus() throws IOException {

        //== CLEAR AND CREATE ================================
        clearAndCreatefaultEntity("1", Entity.Faultcurrent.getName(),
                "org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateFaultcurrentInput",
                SeverityType.Critical);
        createFaultEntity("Lorem Ipsum", Entity.Faultcurrent.getName(),
                "org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateFaultcurrentInput",
                SeverityType.Major);
        createFaultEntity("3", Entity.Faultcurrent.getName(),
                "org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateFaultcurrentInput",
                SeverityType.Minor);
        createFaultEntity("4", Entity.Faultcurrent.getName(),
                "org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateFaultcurrentInput",
                SeverityType.Warning);

        //== READ ================================

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.status.output.Data> readOutput =
                dbProvider.readStatus().getData();
        System.out.println(readOutput);

        assertEquals(1, readOutput.get(0).getFaults().getMajors().intValue());
        assertEquals(1, readOutput.get(0).getFaults().getMinors().intValue());
        assertEquals(1, readOutput.get(0).getFaults().getWarnings().intValue());
        assertEquals(1, readOutput.get(0).getFaults().getCriticals().intValue());

        //== DELETE ================================

        System.out.println("try to delete entries");
        try {
            dbRawProvider.doRemove(Entity.Faultcurrent.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting entry: " + e.getMessage());
        }

        //== VERIFY DELETE ===========================
        System.out.println("verify entries were deleted");
        readOutput = dbProvider.readStatus().getData();
        assertEquals(0, readOutput.get(0).getFaults().getMajors().intValue());
        assertEquals(0, readOutput.get(0).getFaults().getMinors().intValue());
        assertEquals(0, readOutput.get(0).getFaults().getWarnings().intValue());
        assertEquals(0, readOutput.get(0).getFaults().getCriticals().intValue());
    }

    @Test
    public void testMediatorServer() {
        final String NAME = "ms1";
        final String URL = "http://11.23.45.55:4599";
        final String NAME2 = "ms1-nu";
        final String URL2 = "http://11.23.45.56:4599";

        // ==CLEAR BEFORE TEST============================
        System.out.println("try to clear entry");
        try {
            dbRawProvider.doRemove(Entity.MediatorServer.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting entry: " + e.getMessage());
        }
        trySleep();
        // ==CREATE============================
        System.out.println("try to create entry");
        CreateMediatorServerOutputBuilder createOutput = null;
        CreateMediatorServerInput input = new CreateMediatorServerInputBuilder().setName(NAME).setUrl(URL).build();
        String dbId = null;

        try {
            createOutput = dbProvider.createMediatorServer(input);
            dbId = createOutput.getId();
            System.out.println(createOutput);
        } catch (Exception e) {
            fail("failed to create " + input.toString() + ":" + e.getMessage());
        }
        assertNotNull(createOutput);
        assertNotNull(dbId);
        trySleep();
        // ==READ===========================
        System.out.println("try to read entry");
        ReadMediatorServerListInput readinput = new ReadMediatorServerListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("id").setFiltervalue(dbId).build()))
                .setPagination(getPagination(20, 1)).build();
        ReadMediatorServerListOutputBuilder readoutput = dbProvider.readMediatorServerList(readinput);
        List<Data> data = readoutput.getData();
        assertNotNull(data);
        assertEquals("no entry found", 1, data.size());
        assertEquals(NAME, data.get(0).getName());
        assertEquals(URL, data.get(0).getUrl());
        String dbId2 = data.get(0).getId();
        assertEquals(dbId, dbId2);
        System.out.println(data);
        // ==UPDATE============================
        System.out.println("try to update entry");
        UpdateMediatorServerInput updateInput =
                new UpdateMediatorServerInputBuilder().setId(dbId2).setName(NAME2).setUrl(URL2).build();
        UpdateMediatorServerOutputBuilder updateOutput = null;
        try {
            updateOutput = dbProvider.updateMediatorServer(updateInput);
            System.out.println(updateOutput);
        } catch (Exception e) {
            fail("problem updating entry:" + e.getMessage());
        }
        assertNotNull(updateOutput);
        trySleep();
        // ==READ============================
        System.out.println("try to read entry");
        readinput = new ReadMediatorServerListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("name").setFiltervalue(NAME2).build()))
                .setPagination(getPagination(20, 1)).build();
        readoutput = dbProvider.readMediatorServerList(readinput);
        data = readoutput.getData();
        System.out.println(data);
        assertNotNull("no update response", data);
        assertEquals("update not verifiied", 1, data.size());
        assertEquals("update not verifiied", NAME2, data.get(0).getName());
        assertEquals("update not verifiied", URL2, data.get(0).getUrl());
        assertEquals("update not verifiied", dbId, data.get(0).getId());
        // ==DELETE============================
        System.out.println("try to delete entry");
        DeleteMediatorServerInput deleteInput = new DeleteMediatorServerInputBuilder().setId(dbId).build();
        try {
            dbProvider.deleteMediatorServer(deleteInput);
        } catch (Exception e) {
            fail("problem deleting entry: " + e.getMessage());
        }

        trySleep();
        // ==READ/VERIFY DELETE============================
        System.out.println("try to read entry");
        readinput = new ReadMediatorServerListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("name").setFiltervalue(NAME2).build()))
                .setPagination(getPagination(20, 1)).build();
        readoutput = dbProvider.readMediatorServerList(readinput);
        data = readoutput.getData();
        assertNotNull("delete not verifiied", data);
        assertEquals("delete not verifiied", 0, data.size());
    }

    @Test
    public void testNetworkElementConnectionCurrent() {

        System.out.println("networkElementConnection test start");

        // ==CLEAR BEFORE TEST============================
        System.out.println("try to clear entry");
        try {
            dbRawProvider.doRemove(Entity.NetworkelementConnection.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting entry: " + e.getMessage());
        }

        // ==CREATE============================
        System.out.println("try to create");
        final String name = "sim87";
        final String url = "10.5.10.1";
        final long port = 5959;

        CreateNetworkElementConnectionOutputBuilder create = null;
        CreateNetworkElementConnectionInput input = new CreateNetworkElementConnectionInputBuilder().setNodeId(name)
                .setIsRequired(true).setHost(url).setPort(port).build();
        String dbId = null;

        try {
            create = dbProvider.createNetworkElementConnection(input);
            dbId = create.getId();
        } catch (Exception e) {
            fail("networkElementConnection create failed" + e.getMessage());
        }

        assertNotNull(dbId);
        assertNotNull(create);

        // ==READ===========================

        ReadNetworkElementConnectionListInput readInput = new ReadNetworkElementConnectionListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("id").setFiltervalue(dbId).build()))
                .setPagination(getPagination(20, 1)).build();

        ReadNetworkElementConnectionListOutputBuilder readOperation =
                dbProvider.readNetworkElementConnectionList(readInput);
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.network.element.connection.list.output.Data> data =
                readOperation.getData();

        assertNotNull(data);
        assertEquals(dbId, data.get(0).getId());
        assertEquals(name, data.get(0).getNodeId());
        assertEquals(url, data.get(0).getHost());
        assertEquals(port, data.get(0).getPort().longValue());

        // ==UPDATE============================
        System.out.println("Trying to update...");
        final String name2 = "sim88";
        final String url2 = "10.5.10.2";
        final long port2 = 5960;

        UpdateNetworkElementConnectionInput updateInput = new UpdateNetworkElementConnectionInputBuilder().setId(dbId)
                .setHost(url2).setPort(port2).setIsRequired(false).build();
        UpdateNetworkElementConnectionOutputBuilder updateOutput = null;
        try {
            updateOutput = dbProvider.updateNetworkElementConnection(updateInput);
        } catch (Exception e) {
            fail("update failed: " + e.getMessage());
        }

        assertNotNull(updateOutput);

        // == Verify UPDATE============================
        System.out.println("Verfiying update...");

        readOperation = dbProvider.readNetworkElementConnectionList(readInput);
        data = readOperation.getData();

        assertNotNull(data);
        assertEquals(url2, data.get(0).getHost());
        assertEquals(port2, data.get(0).getPort().longValue());

        // ==PARTIAL UPDATE============================
        System.out.println("Try partial update...");
        assertEquals(false, data.get(0).isIsRequired());
        updateInput = new UpdateNetworkElementConnectionInputBuilder().setId(dbId).setIsRequired(true).build();
        try {
            updateOutput = dbProvider.updateNetworkElementConnection(updateInput);
        } catch (Exception e) {
            fail("update failed: " + e.getMessage());
        }

        assertNotNull(updateOutput);

        readOperation = dbProvider.readNetworkElementConnectionList(readInput);
        data = readOperation.getData();

        assertEquals(true, data.get(0).isIsRequired());
        assertEquals(url2, data.get(0).getHost());
        assertEquals(port2, data.get(0).getPort().longValue());

        // ==DELETE============================
        System.out.println("Try delete...");

        DeleteNetworkElementConnectionInput deleteInput =
                new DeleteNetworkElementConnectionInputBuilder().setId(dbId).build();
        try {
            dbProvider.deleteNetworkElementConnection(deleteInput);
        } catch (Exception e) {
            fail("problem deleting " + e.getMessage());
        }

        readInput = new ReadNetworkElementConnectionListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("id").setFiltervalue(dbId).build()))
                .setPagination(getPagination(20, 1)).build();
        readOperation = dbProvider.readNetworkElementConnectionList(readInput);
        data = readOperation.getData();
        assertEquals(0, data.size());

    }

    @Test
    public void testMaintenance() {
        System.out.println("Starting Maintenance tests...");

        // ==CLEAR BEFORE TEST============================
        System.out.println("try to clear entry");
        try {
            dbRawProvider.doRemove(Entity.Maintenancemode.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting entry: " + e.getMessage());
        }

        // ==CREATE============================

        final String nodeId = "Lorem Ipsum";
        final boolean isActive = true;

        CreateMaintenanceOutputBuilder create = null;
        CreateMaintenanceInput input =
                new CreateMaintenanceInputBuilder().setNodeId(nodeId).setActive(isActive).build();
        String dbId = null;
        try {
            create = dbProvider.createMaintenance(input);
            dbId = create.getId();
        } catch (Exception e) {
            fail("Failed to create:" + e.getMessage());
        }

        System.out.println(dbId);
        assertNotNull(create);
        assertNotNull(dbId);

        // ==READ===========================
        System.out.println("Try read...");

        ReadMaintenanceListInput readinput = new ReadMaintenanceListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("id").setFiltervalue(dbId).build()))
                .setPagination(getPagination(20, 1)).build();
        ReadMaintenanceListOutputBuilder readResult = dbProvider.readMaintenanceList(readinput);
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.maintenance.list.output.Data> data =
                readResult.getData();

        assertNotEquals(0, data.size());
        assertNotNull(data);
        assertEquals(nodeId, data.get(0).getNodeId());
        assertEquals(isActive, data.get(0).isActive());

        // ==UPDATE============================

        System.out.println("Trying to update...");
        final String nodeId2 = "Name2";
        final boolean isActive2 = false;

        UpdateMaintenanceInput updateInput =
                new UpdateMaintenanceInputBuilder().setId(dbId).setNodeId(nodeId2).setActive(isActive2).build();
        UpdateMaintenanceOutputBuilder updateResult = null;
        try {
            updateResult = dbProvider.updateMaintenance(updateInput);
        } catch (Exception e) {
            fail("maintenance update failed..." + e.getMessage());
        }

        assertNotNull(updateResult);

        // == VERIFY UPDATE============================
        System.out.println("Verfify update...");
        readResult = dbProvider.readMaintenanceList(readinput);
        data = readResult.getData();

        assertNotNull(data);
        assertEquals(nodeId2, data.get(0).getNodeId());
        assertEquals(isActive2, data.get(0).isActive());

        // ==DELETE================================
        System.out.println("Trying to delete...");

        DeleteMaintenanceInput deleteInput = new DeleteMaintenanceInputBuilder().setId(dbId).build();
        try {
            dbProvider.deleteMaintenance(deleteInput);
        } catch (Exception e) {
            fail("Maintenance entry couldn't be deleted" + e.getMessage());
        }

        readResult = dbProvider.readMaintenanceList(readinput);
        data = readResult.getData();

        assertEquals(0, data.size());
    }

    @Test
    public void testFaultLog() {

        System.out.println("Starting fault log tests...");
        String dbId = clearAndCreatefaultEntity("1", Entity.Faultlog.getName(),
                "org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateFaultlogInput",
                SeverityType.Critical);

        // ==READ===========================
        System.out.println("try to read entry");

        ReadFaultlogListInput readinput = new ReadFaultlogListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("id").setFiltervalue(dbId).build()))
                .setPagination(getPagination(20, 1)).build();

        ReadFaultlogListOutputBuilder readResult = null;
        try {
            readResult = dbProvider.readFaultLogList(readinput);

        } catch (Exception e) {
            fail("Fault log not read: " + e.getMessage());
        }

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultlog.list.output.Data> data =
                readResult.getData();

        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals("signalIsLost", data.get(0).getProblem());
        assertEquals("Critical", data.get(0).getSeverity().toString());
        assertEquals("s1", data.get(0).getNodeId());

        //== UPDATE ================================
        System.out.println("try to update entry");

        dbRawProvider.doUpdateOrCreate(Entity.Faultlog.getName(), "1",
                "{'problem': 'CableLOS', 'severity': 'Major', 'node-id': 'test4657-78'}");

        System.out.println("try to search entry 1");
        readinput = new ReadFaultlogListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("node-id").setFiltervalue("test").build()))
                .setPagination(getPagination(20, 1)).build();

        //== VERIFY UPDATE ================================
        readResult = dbProvider.readFaultLogList(readinput);
        data = readResult.getData();


        assertNotNull(data);
        System.out.println(data);
        assertEquals(0, data.size());

        System.out.println("try to search entry 2");

        readinput = new ReadFaultlogListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("node-id").setFiltervalue("test*").build()))
                .setPagination(getPagination(20, 1)).build();

        readResult = dbProvider.readFaultLogList(readinput);
        data = readResult.getData();


        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals("CableLOS", data.get(0).getProblem());
        assertEquals("Major", data.get(0).getSeverity().toString());
        assertEquals("test4657-78", data.get(0).getNodeId());

        //== DELETE ================================

        System.out.println("try to clear entry");
        try {
            dbRawProvider.doRemove(Entity.Faultlog.getName(), dbId);
        } catch (Exception e) {
            fail("problem deleting entry: " + e.getMessage());
        }

        //== VERIFY DELETE ===========================
        System.out.println("verify entries deleted");
        readResult = dbProvider
                .readFaultLogList(new ReadFaultlogListInputBuilder().setPagination(getPagination(20, 1)).build());
        data = readResult.getData();
        assertEquals(0, data.size());
    }

    @Test
    public void testFaultCurrent() {
        System.out.println("Starting faultCurrent test...");
        String dbId = null;
        dbId = clearAndCreatefaultEntity("1", Entity.Faultcurrent.getName(),
                "org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateFaultcurrentInput",
                SeverityType.NonAlarmed);
        assertEquals("1", dbId);

        // ==READ===========================
        System.out.println("Trying to read...");


        ReadFaultcurrentListInput readinput = new ReadFaultcurrentListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("id").setFiltervalue(dbId).build()))
                .setPagination(getPagination(20, 1)).build();

        ReadFaultcurrentListOutputBuilder readResult = null;
        try {
            readResult = dbProvider.readFaultCurrentList(readinput);

        } catch (Exception e) {
            fail("Fault log not read: " + e.getMessage());
        }

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.faultcurrent.list.output.Data> data =
                readResult.getData();


        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals("signalIsLost", data.get(0).getProblem());
        assertEquals("NonAlarmed", data.get(0).getSeverity().toString());
        assertEquals("s1", data.get(0).getNodeId());
        assertEquals(4340, data.get(0).getCounter().intValue());
        assertEquals(new DateAndTime("2019-10-28T11:55:58.3Z"), data.get(0).getTimestamp());
        assertEquals(4340, data.get(0).getCounter().intValue());
        assertEquals("LP-MWPS-RADIO", data.get(0).getObjectId());


        // ==UPDATE============================
        System.out.println("Trying to update...");

        String json = "{\n" + "\"timestamp\": \"2019-12-28T11:55:58.3Z\",\n" + "\"node-id\": \"SDN-Controller-0\",\n"
                + "\"counter\": 75,\n" + "\"problem\": \"connectionLossNeOAM\",\n" + "}";

        String updatedDbId = dbRawProvider.doUpdateOrCreate(Entity.Faultcurrent.getName(), dbId, json);
        assertEquals(dbId, updatedDbId);

        // ==READ============================

        try {
            readResult = dbProvider.readFaultCurrentList(readinput);

        } catch (Exception e) {
            fail("Fault log not read: " + e.getMessage());
        }

        data = readResult.getData();

        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals("connectionLossNeOAM", data.get(0).getProblem());
        assertEquals("SDN-Controller-0", data.get(0).getNodeId());
        assertEquals(75, data.get(0).getCounter().intValue());
        assertEquals("LP-MWPS-RADIO", data.get(0).getObjectId());

        // ==DELETE============================
        try {
            dbRawProvider.doRemove(Entity.Faultcurrent.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting: " + e.getMessage());
        }

        // ==READ/VERIFY DELETE============================

        try {
            readResult = dbProvider.readFaultCurrentList(readinput);

        } catch (Exception e) {
            fail("Fault log not read: " + e.getMessage());
        }

        data = readResult.getData();

        assertNotNull(data);
        assertEquals(0, data.size());
    }

    @Test
    public void testConnectionLog() {

        // ==CLEAR================================
        System.out.println("Clear before test");
        try {
            dbRawProvider.doRemove(Entity.Connectionlog.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting: " + e.getMessage());
        }

        // ==CREATE================================

        System.out.println("Try create entry");
        final String initialDbId = "1";
        String dbId = null;
        String json = "{\n" + "\"timestamp\": \"2019-11-01T11:28:34.7Z\",\n" + "\"status\": \"Connecting\",\n"
                + "\"node-id\": \"sim2230\",\n"
                + "\"implemented-interface\": \"org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateConnectionlogInput\"\n"
                + "}";

        dbId = dbRawProvider.doUpdateOrCreate(Entity.Connectionlog.getName(), initialDbId, json);

        assertEquals(initialDbId, dbId);

        // ==READ================================
        System.out.println("Try read entry");

        ReadConnectionlogListInput readinput = new ReadConnectionlogListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("id").setFiltervalue(dbId).build()))
                .setPagination(getPagination(20, 1)).build();

        ReadConnectionlogListOutputBuilder readResult = null;
        try {
            readResult = dbProvider.readConnectionlogList(readinput);

        } catch (Exception e) {
            fail("Connection log not read: " + e.getMessage());
        }

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.connectionlog.list.output.Data> data =
                readResult.getData();

        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals("Connecting", data.get(0).getStatus().toString());
        assertEquals("sim2230", data.get(0).getNodeId());


        // ==UPDATE================================
        System.out.println("Try update entry");

        dbRawProvider.doUpdateOrCreate(Entity.Connectionlog.getName(), dbId, "{'status' : 'Connected'}");

        // ==READ 2================================
        System.out.println("Try read updated entry");

        readinput = new ReadConnectionlogListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("status").setFiltervalue("Connected").build()))
                .setPagination(getPagination(20, 1)).build();

        try {
            readResult = dbProvider.readConnectionlogList(readinput);

        } catch (Exception e) {
            fail("Connection log not read: " + e.getMessage());
        }

        data = readResult.getData();

        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals("Connected", data.get(0).getStatus().toString());
        assertEquals("sim2230", data.get(0).getNodeId());

        //== DELETE ================================

        System.out.println("try to clear entry");
        try {
            dbRawProvider.doRemove(Entity.Connectionlog.getName(), dbId);
        } catch (Exception e) {
            fail("problem deleting entry: " + e.getMessage());
        }

        //== VERIFY DELETE ===========================
        System.out.println("verify entries deleted");
        readResult = dbProvider.readConnectionlogList(
                new ReadConnectionlogListInputBuilder().setPagination(getPagination(20, 1)).build());
        data = readResult.getData();
        assertEquals(0, data.size());

    }

    @Test
    public void testEventLog() {
        System.out.println("Test event log starting...");

        // ==CLEAR================================
        System.out.println("Clear before test");
        try {
            dbRawProvider.doRemove(Entity.Eventlog.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting: " + e.getMessage());
        }
        // ==CREATE============================

        String dbId = null;
        String json = " {\n" + "\"timestamp\": \"2019-11-08T16:39:23.0Z\",\n" + "\"new-value\": \"done\",\n"
                + "\"object-id\": \"SDN-Controller-0\",\n" + "\"attribute-name\": \"startup\",\n" + "\"counter\": 0,\n"
                + "\"implemented-interface\": \"org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Eventlog\",\n"
                + "\"node-id\": \"SDN-Controller-0\"\n" + "}";

        dbId = dbRawProvider.doUpdateOrCreate(Entity.Eventlog.getName(), "1", json);
        assertNotNull(dbId);

        // ==READ===========================

        ReadEventlogListInput readinput = new ReadEventlogListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("id").setFiltervalue(dbId).build()))
                .setPagination(getPagination(20, 1)).build();
        ReadEventlogListOutputBuilder readResult = null;
        try {
            readResult = dbProvider.readEventlogList(readinput);

        } catch (Exception e) {
            fail("problem reading eventlog");
        }

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.eventlog.list.output.Data> data =
                readResult.getData();
        assertEquals(1, data.size());

        //== DELETE ================================

        System.out.println("try to clear entry");
        try {
            dbRawProvider.doRemove(Entity.Eventlog.getName(), dbId);
        } catch (Exception e) {
            fail("problem deleting entry: " + e.getMessage());
        }

        //== VERIFY DELETE ===========================
        System.out.println("verify entries deleted");
        try {
            readResult = dbProvider
                    .readEventlogList(new ReadEventlogListInputBuilder().setPagination(getPagination(20, 1)).build());
        } catch (IOException e) {
            fail("problem reading eventlog");
        }
        data = readResult.getData();
        assertEquals(0, data.size());

    }

    @Test
    public void testInventory() {

        System.out.println("Test inventory starting...");

        // ==CLEAR================================
        System.out.println("Clear before test");
        try {
            dbRawProvider.doRemove(Entity.Inventoryequipment.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting: " + e.getMessage());
        }


        // ==CREATE============================

        String dbId = null;
        String json = " {\"tree-level\": 1,\n" + "    \"parent-uuid\": \"SHELF-1.1.0.0\",\n"
                + "    \"node-id\": \"sim2\",\n" + "    \"uuid\": \"CARD-1.1.8.0\",\n"
                + "    \"contained-holder\": [ ],\n" + "    \"manufacturer-name\": \"Lorem Ipsum\",\n"
                + "    \"manufacturer-identifier\": \"ONF-Wireless-Transport\",\n" + "    \"serial\": \"sd-dsa-eqw\",\n"
                + "    \"date\": \"2008-10-21T00:00:00.0Z\",\n"
                + "\"implemented-interface\": \"org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Inventory\",\n"
                + "    \"version\": \"unknown\",\n" + "    \"description\": \"WS/DS3\",\n"
                + "    \"part-type-id\": \"unknown\",\n" + "    \"model-identifier\": \"model-id-s3s\",\n"
                + "    \"type-name\": \"p4.module\"}";

        dbId = dbRawProvider.doUpdateOrCreate(Entity.Inventoryequipment.getName(), "1 1", json);
        assertNotNull(dbId);

        // ==READ===========================
        ReadInventoryListInput readinput = new ReadInventoryListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("id").setFiltervalue(dbId).build()))
                .setPagination(getPagination(20, 1)).build();
        ReadInventoryListOutputBuilder readResult = null;
        try {
            readResult = dbProvider.readInventoryList(readinput);

        } catch (Exception e) {
            fail("Problem reading inventory list" + e.getMessage());
        }

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.inventory.list.output.Data> data =
                readResult.getData();
        assertEquals(1, data.size());
        assertEquals("Lorem Ipsum", data.get(0).getManufacturerName());
        assertEquals("ONF-Wireless-Transport", data.get(0).getManufacturerIdentifier());
        assertEquals("sim2", data.get(0).getNodeId());
        assertEquals("unknown", data.get(0).getVersion());
        assertEquals("WS/DS3", data.get(0).getDescription());
        assertEquals("2008-10-21T00:00:00.0Z", data.get(0).getDate());
        assertEquals("sd-dsa-eqw", data.get(0).getSerial());
        System.out.println(data.get(0).getDate());

        // ==UPDATE============================
        String updatedDbId = null;
        final String[] holderArray = {"Lorem Ipsum 1", "Lorem Ipsum 2", "Lorem Ipsum &%/$_2"};
        String updatejson = " {" + "    \"node-id\": \"sim5\",\n"
                + "    \"contained-holder\": [ \"Lorem Ipsum 1\", \"Lorem Ipsum 2\", \"Lorem Ipsum &%/$_2\" ],\n"
                + "    \"serial\": \"sd-dsa-eww\",\n" + "    \"date\": \"2008-11-21T00:00:00.0Z\",\n"
                + "    \"part-type-id\": \"not unknown\",\n" + "}";

        updatedDbId = dbRawProvider.doUpdateOrCreate(Entity.Inventoryequipment.getName(), dbId, updatejson);
        assertEquals(dbId, updatedDbId);

        try {
            readResult = dbProvider.readInventoryList(readinput);

        } catch (Exception e) {
            fail("Problem reading inventory list" + e.getMessage());
        }

        data = readResult.getData();

        assertEquals(1, data.size());
        assertEquals("Lorem Ipsum", data.get(0).getManufacturerName());
        assertEquals("ONF-Wireless-Transport", data.get(0).getManufacturerIdentifier());
        assertEquals("sim5", data.get(0).getNodeId());
        assertEquals("not unknown", data.get(0).getPartTypeId());
        assertEquals("WS/DS3", data.get(0).getDescription());
        assertEquals("2008-11-21T00:00:00.0Z", data.get(0).getDate());
        assertEquals("sd-dsa-eww", data.get(0).getSerial());
        assertEquals(holderArray.length, data.get(0).getContainedHolder().size());
        assertEquals(holderArray[0], data.get(0).getContainedHolder().get(0));
        assertEquals(holderArray[1], data.get(0).getContainedHolder().get(1));
        assertEquals(holderArray[2], data.get(0).getContainedHolder().get(2));

        // ==DELETE============================

        System.out.println("delete after test");
        try {
            dbRawProvider.doRemove(Entity.Inventoryequipment.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting: " + e.getMessage());
        }

        // ==VERIFY DELETE ============================

        try {
            readResult = dbProvider.readInventoryList(readinput);

        } catch (Exception e) {
            fail("Problem reading inventory list" + e.getMessage());
        }

        data = readResult.getData();
        assertEquals(0, data.size());

    }

    @Test
    public void test15MinPerformanceReadLtpListWithoutNodeIdSetThrowsException() {

        System.out.println("Reading 15m ltp list without node id filter set throws an exception test start...");

        try {
            dbRawProvider.doRemove(Entity.Historicalperformance15min.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting: " + e.getMessage());
        }

        System.out.println("create entries...");

        createPerformanceData("1", GranularityPeriodType.Period15Min, "PM_RADIO_15M_6", "LP-MWPS-TTP-02", "a2");
        createPerformanceData("2", GranularityPeriodType.Period15Min, "PM_RADIO_15M_4", "LP-MWPS-TTP-01", "a2");

        createPerformanceData("4", GranularityPeriodType.Period15Min, "PM_RADIO_15M_6", "LP-MWPS-TTP-02", "a3");
        createPerformanceData("5", GranularityPeriodType.Period15Min, "PM_RADIO_15M_4", "LP-MWPS-TTP-01", "a3");
        createPerformanceData("6", GranularityPeriodType.Period15Min, "PM_RADIO_15M_7", "LP-MWPS-TTP-03", "a3");
        createPerformanceData("3", GranularityPeriodType.Period15Min, "PM_RADIO_15M_7", "LP-MWPS-TTP-05", "a3");

        System.out.println("trying to read, should throw exception...");


        ReadPmdata15mLtpListInput readLtp =
                new ReadPmdata15mLtpListInputBuilder().setPagination(getPagination(20, 1)).build();

        ReadPmdata15mLtpListOutputBuilder readltpResult = null;

        try {
            readltpResult = dbProvider.readPmdata15mLtpList(readLtp);
            fail("No exception thrown!");
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("no nodename in filter found ", e.getMessage());
        }

        assertNull(readltpResult);

        //== DELETE ================================

        System.out.println("try to clear entry");
        try {
            dbRawProvider.doRemove(Entity.Historicalperformance15min.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting entry: " + e.getMessage());
        }

        //== VERIFY DELETE ===========================
        System.out.println("verify entries deleted");
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._15m.list.output.Data> data =
                dbProvider
                        .readPmdata15mList(
                                new ReadPmdata15mListInputBuilder().setPagination(getPagination(20, 1)).build())
                        .getData();

        assertEquals(0, data.size());
    }

    @Test
    public void test15MinPerformanceData() {
        // == CLEAR BEFORE TESTS ============================
        System.out.println("Test 15 min performance...");

        try {
            dbRawProvider.doRemove(Entity.Historicalperformance15min.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting: " + e.getMessage());
        }

        // == CREATE ============================

        System.out.println("create entries...");

        createPerformanceData("1", GranularityPeriodType.Period15Min, "PM_RADIO_15M_6", "LP-MWPS-TTP-02", "a2");
        createPerformanceData("2", GranularityPeriodType.Period15Min, "PM_RADIO_15M_4", "LP-MWPS-TTP-01", "a2");

        createPerformanceData("4", GranularityPeriodType.Period15Min, "PM_RADIO_15M_6", "LP-MWPS-TTP-02", "a3");
        createPerformanceData("5", GranularityPeriodType.Period15Min, "PM_RADIO_15M_4", "LP-MWPS-TTP-01", "a3");
        createPerformanceData("6", GranularityPeriodType.Period15Min, "PM_RADIO_15M_7", "LP-MWPS-TTP-03", "a3");
        createPerformanceData("3", GranularityPeriodType.Period15Min, "PM_RADIO_15M_7", "LP-MWPS-TTP-05", "a3");

        // == READ ============================
        System.out.println("read list entries...");

        ReadPmdata15mListInput read = new ReadPmdata15mListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("node-name").setFiltervalue("a2").build()))
                .setPagination(getPagination(20, 1)).build();

        ReadPmdata15mListOutputBuilder readResult = null;

        try {
            readResult = dbProvider.readPmdata15mList(read);
        } catch (Exception e) {
            fail("Problem reading 15m data");
        }

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._15m.list.output.Data> data =
                readResult.getData();

        assertNotNull(data);
        assertEquals(2, data.size());

        System.out.println("read ltp entries with node name set...");

        ReadPmdata15mLtpListInput readLtp = new ReadPmdata15mLtpListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("node-name").setFiltervalue("a2").build()))
                .setPagination(getPagination(20, 1)).build();

        ReadPmdata15mLtpListOutputBuilder readltpResult = null;

        try {
            readltpResult = dbProvider.readPmdata15mLtpList(readLtp);
        } catch (Exception e) {
            fail("Problem reading 15m ltp data");
        }

        List<String> dataLtp = readltpResult.getData();

        assertNotNull(dataLtp);
        assertEquals(2, dataLtp.size());
        assertTrue(dataLtp.contains("LP-MWPS-TTP-02"));
        assertTrue(dataLtp.contains("LP-MWPS-TTP-01"));

        System.out.println("read device entries...");

        ReadPmdata15mDeviceListInput readDevices =
                new ReadPmdata15mDeviceListInputBuilder().setPagination(getPagination(20, 1)).build();

        ReadPmdata15mDeviceListOutputBuilder readDeviceResult = null;

        try {
            readDeviceResult = dbProvider.readPmdata15mDeviceList(readDevices);
        } catch (Exception e) {
            fail("Problem reading 15m device data");
        }

        List<String> dataDevice = readDeviceResult.getData();

        assertNotNull(dataDevice);
        assertEquals(2, dataDevice.size());
        assertTrue(dataDevice.contains("a2"));
        assertTrue(dataDevice.contains("a3"));

        //== DELETE ================================

        System.out.println("try to clear entry");
        try {
            dbRawProvider.doRemove(Entity.Historicalperformance15min.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting entry: " + e.getMessage());
        }

        //== VERIFY DELETE ===========================
        System.out.println("verify entries deleted");
        readResult = dbProvider
                .readPmdata15mList(new ReadPmdata15mListInputBuilder().setPagination(getPagination(20, 1)).build());
        data = readResult.getData();
        assertEquals(0, data.size());

    }

    @Test
    public void test24hPerformanceData() {
        System.out.println("Test 24h performance...");

        try {
            dbRawProvider.doRemove(Entity.Historicalperformance24h.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting: " + e.getMessage());
        }

        System.out.println("create entries...");
        GranularityPeriodType timeInterval = GranularityPeriodType.Period24Hours;
        createPerformanceData("1", timeInterval, "PM_RADIO_15M_6", "LP-MWPS-TTP-02", "a2");
        createPerformanceData("2", timeInterval, "PM_RADIO_15M_4", "LP-MWPS-TTP-01", "a2");
        String aDbId = createPerformanceData("4", timeInterval, "PM_RADIO_15M_6", "LP-MWPS-TTP-06", "a2");

        createPerformanceData("5", timeInterval, "PM_RADIO_15M_4", "LP-MWPS-TTP-01", "a3");
        createPerformanceData("6", timeInterval, "PM_RADIO_15M_7", "LP-MWPS-TTP-03", "a3");
        createPerformanceData("3", timeInterval, "PM_RADIO_15M_7", "LP-MWPS-TTP-05", "a3");

        System.out.println("read all list entries...");

        ReadPmdata24hListInput read = new ReadPmdata24hListInputBuilder().setPagination(getPagination(20, 1)).build();

        ReadPmdata24hListOutputBuilder readResult = null;

        try {
            readResult = dbProvider.readPmdata24hList(read);
        } catch (Exception e) {
            fail("Problem reading 24h data");
        }

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._24h.list.output.Data> data =
                readResult.getData();

        assertNotNull(data);
        assertEquals(6, data.size());


        System.out.println("filter list entries...");

        read = new ReadPmdata24hListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("node-name").setFiltervalue("a2").build()))
                .setPagination(getPagination(20, 1)).build();

        readResult = null;

        try {
            readResult = dbProvider.readPmdata24hList(read);
        } catch (Exception e) {
            fail("Problem reading 24h data");
        }

        data = readResult.getData();

        assertNotNull(data);
        assertEquals(3, data.size());

        System.out.println("read ltp entries with node name set...");

        ReadPmdata24hLtpListInput readLtp = new ReadPmdata24hLtpListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("node-name").setFiltervalue("a2").build()))
                .setPagination(getPagination(20, 1)).build();

        ReadPmdata24hLtpListOutputBuilder readltpResult = null;

        try {
            readltpResult = dbProvider.readPmdata24hLtpList(readLtp);
        } catch (Exception e) {
            fail("Problem reading 24h ltp data");
        }

        List<String> dataLtp = readltpResult.getData();

        assertNotNull(dataLtp);
        assertEquals(3, dataLtp.size());
        assertTrue(dataLtp.contains("LP-MWPS-TTP-02"));
        assertTrue(dataLtp.contains("LP-MWPS-TTP-01"));
        assertTrue(dataLtp.contains("LP-MWPS-TTP-06"));


        System.out.println("read device entries...");

        ReadPmdata24hDeviceListInput readDevices =
                new ReadPmdata24hDeviceListInputBuilder().setPagination(getPagination(20, 1)).build();

        ReadPmdata24hDeviceListOutputBuilder readDeviceResult = null;

        try {
            readDeviceResult = dbProvider.readPmdata24hDeviceList(readDevices);
        } catch (Exception e) {
            fail("Problem reading 24h device data");
        }

        List<String> dataDevice = readDeviceResult.getData();

        assertNotNull(dataDevice);
        assertEquals(2, dataDevice.size());
        assertTrue(dataDevice.contains("a2"));
        assertTrue(dataDevice.contains("a3"));

        // == UPDATE ==============================

        boolean success = dbRawProvider.doUpdate(Entity.Historicalperformance24h.getName(),
                "{'uuid-interface':'LTP-TEST-MWP-097'}", QueryBuilders.termQuery("_id", aDbId));
        assertTrue("update dbentry not succeeded", success);
        try {
            readltpResult = dbProvider.readPmdata24hLtpList(readLtp);
        } catch (Exception e) {
            fail("Problem reading 24h ltp data");
        }

        // == VERIFY UPDATE ==============================

        dataLtp = readltpResult.getData();

        assertNotNull(dataLtp);
        assertEquals(3, dataLtp.size());
        assertTrue(dataLtp.contains("LP-MWPS-TTP-02"));
        assertTrue(dataLtp.contains("LP-MWPS-TTP-01"));
        assertTrue(dataLtp.contains("LTP-TEST-MWP-097"));



        //== DELETE ===========================

        System.out.println("try to clear entries");
        try {
            dbRawProvider.doRemove(Entity.Historicalperformance24h.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting entry: " + e.getMessage());
        }

        //== VERIFY DELETE ===========================
        System.out.println("verify entries deleted");
        readResult = dbProvider
                .readPmdata24hList(new ReadPmdata24hListInputBuilder().setPagination(getPagination(20, 1)).build());
        data = readResult.getData();
        assertEquals(0, data.size());
    }

    @Test
    public void test24hPerformanceDataReadLtpListWithoutNodeIdSetThrowsException() {
        System.out.println("Test 24 hour tp list without node id filter set throws an exception test start...\"...");

        try {
            dbRawProvider.doRemove(Entity.Historicalperformance24h.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting: " + e.getMessage());
        }

        System.out.println("create entries...");

        GranularityPeriodType timeInterval = GranularityPeriodType.Period24Hours;
        createPerformanceData("1", timeInterval, "PM_RADIO_15M_6", "LP-MWPS-TTP-02", "a2");
        createPerformanceData("2", timeInterval, "PM_RADIO_15M_4", "LP-MWPS-TTP-01", "a2");

        createPerformanceData("4", timeInterval, "PM_RADIO_15M_6", "LP-MWPS-TTP-02", "a3");
        createPerformanceData("5", timeInterval, "PM_RADIO_15M_4", "LP-MWPS-TTP-01", "a3");
        createPerformanceData("6", timeInterval, "PM_RADIO_15M_7", "LP-MWPS-TTP-03", "a3");
        createPerformanceData("3", timeInterval, "PM_RADIO_15M_7", "LP-MWPS-TTP-05", "a3");

        System.out.println("trying to read, should throw exception...");


        ReadPmdata24hLtpListInput readLtp =
                new ReadPmdata24hLtpListInputBuilder().setPagination(getPagination(20, 1)).build();

        ReadPmdata24hLtpListOutputBuilder readltpResult = null;

        try {
            readltpResult = dbProvider.readPmdata24hLtpList(readLtp);
            fail("No exception thrown!");
        } catch (Exception e) {
            System.out.println(e);
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("no nodename in filter found ", e.getMessage());
        }

        assertNull(readltpResult);

        try {
            dbRawProvider.doRemove(Entity.Historicalperformance24h.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting: " + e.getMessage());
        }
    }

    @Test
    public void testUrlEncoding() {
        System.out.println("Testing url encding");

        final String test = "Lorem Ipsum";
        final String test1 = "Lorem/Ipsum";
        final String test2 = "Lorem_Ipsum";
        final String test3 = "Lorem%Ipsum";

        assertEquals("Lorem%20Ipsum", BaseRequest.urlEncodeValue(test));
        assertEquals("Lorem%2FIpsum", BaseRequest.urlEncodeValue(test1));
        assertEquals("Lorem_Ipsum", BaseRequest.urlEncodeValue(test2));
        assertEquals("Lorem%25Ipsum", BaseRequest.urlEncodeValue(test3));
    }

    @Test
    public void testDoUpdateOrCreateWithNullId() {
        System.out.println("Test DoUpdateOrCreate doesn't create new database entry if null is passed");

        String dbId = clearAndCreatefaultEntity(null, Entity.Faultlog.getName(),
                "org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateFaultlogInput",
                SeverityType.Critical);
        assertNull(dbId);
    }

    @Test
    public void readTestFaultCurrentViaRawDbProvider() {
        System.out.println("Starting faultCurrent test...");
        String dbId = null;
        dbId = clearAndCreatefaultEntity("1", Entity.Faultcurrent.getName(),
                "org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateFaultcurrentInput",
                SeverityType.Critical);
        assertEquals("1", dbId);

        // ==READ===========================
        System.out.println("Trying to read...");

        String readResult = null;
        try {
            readResult = dbRawProvider.doReadJsonData(Entity.Faultcurrent.getName(), dbId);

        } catch (Exception e) {
            fail("Fault log not read: " + e.getMessage());
        }


        String expectedDbResult =
                "{\"severity\":\"Critical\",\"node-id\":\"s1\",\"problem\":\"signalIsLost\",\"counter\":4340,\"object-id\":\"LP-MWPS-RADIO\",\"implemented-interface\":\"org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.CreateFaultcurrentInput\",\"type\":\"ProblemNotificationXml\",\"timestamp\":\"2019-10-28T11:55:58.3Z\"}";

        System.out.println(readResult);
        assertNotNull(readResult);
        assertEquals(expectedDbResult, readResult);

        SearchResult<SearchHit> searchResult = dbRawProvider.doReadAllJsonData(Entity.Faultcurrent.getName());
        assertNotNull(searchResult);

        List<SearchHit> hits = searchResult.getHits();

        assertNotNull(hits);
        assertEquals(1, searchResult.getTotal());
        assertEquals(expectedDbResult, hits.get(0).getSourceAsString());

        //== DELETE ==============================
        try {
            dbRawProvider.doRemove(Entity.Faultcurrent.getName(), QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting: " + e.getMessage());
        }
        //== VERIFY DELETE ========================
        searchResult = dbRawProvider.doReadAllJsonData(Entity.Faultcurrent.getName());
        hits = searchResult.getHits();
        assertNotNull(hits);
        assertEquals(0, searchResult.getTotal());

    }

    @Test
    public void testOutputCamelCase() {
        try {
            String jsonString = "{\n" + "\"timestamp\": \"2020-02-20T09:31:22.3Z\",\n"
                    + "\"object-id\": \"LP-MWPS-RADIO\",\n" + "\"severity\": \"Critical\",\n" + "\"counter\": 10,\n"
                    + "\"implemented-interface\": \"org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Faultlog\",\n"
                    + "\"source-type\": \"Netconf\",\n" + "\"node-id\": \"sim4\",\n" + "\"problem\": \"signalIsLost\"\n"
                    + "}";
            YangToolsMapper yangtoolsMapper = new YangToolsMapper();
            FaultlogEntity log = yangtoolsMapper.readValue(jsonString, Faultlog.class);
            System.out.println(log);
            System.out.println(yangtoolsMapper.writeValueAsString(log));
        } catch (IOException e) {
            fail(e.getMessage());
        }

    }

    private Pagination getPagination(long pageSize, int page) {
        return new PaginationBuilder().setPage(BigInteger.valueOf(page)).setSize(pageSize).build();
    }


    private String clearAndCreatefaultEntity(String initialDbId, String entityType, String implementedInterface,
            SeverityType severity) {
        // ==CLEAR BEFORE TEST============================
        System.out.println("try to clear entry");
        try {
            dbRawProvider.doRemove(entityType, QueryBuilders.matchAllQuery());
        } catch (Exception e) {
            fail("problem deleting: " + e.getMessage());
        }


        return createFaultEntity(initialDbId, entityType, implementedInterface, severity);
    }

    private String createFaultEntity(String initialDbId, String entityType, String implementedInterface,
            SeverityType severity) {
        // ==CREATE============================
        System.out.println("try to create entry");
        String dbId = null;

        try {

            dbId = dbRawProvider.doUpdateOrCreate(entityType, initialDbId,
                    "{\n" + "\"timestamp\": \"2019-10-28T11:55:58.3Z\",\n" + "\"object-id\": \"LP-MWPS-RADIO\",\n"
                            + "\"severity\": \"" + severity.toString() + "\",\n" + "\"node-id\": \"s1\",\n"
                            + "\"implemented-interface\": \"" + implementedInterface + "\",\n" + "\"counter\": 4340,\n"
                            + "\"problem\": \"signalIsLost\",\n" + "\"type\": \"ProblemNotificationXml\"\n" + "}");



        } catch (Exception e) {
            fail("Problem creating fault log entry" + e.getMessage());
        }

        return dbId;
    }

    private String createPerformanceData(String initialDbId, GranularityPeriodType timeInterval, String scannerId,
            String uuidInterface, String nodename) {

        String json = "{\n" + "\"node-name\": \"" + nodename + "\",\n" + "\"uuid-interface\": \"" + uuidInterface
                + "\",\n" + "\"layer-protocol-name\": \"MWPS\",\n" + "\"radio-signal-id\": \"Test8\",\n"
                + "\"time-stamp\": \"2017-03-01T06:15:00.0Z\",\n" + "\"granularity-period\": \""
                + timeInterval.toString() + "\",\n" + "\"scanner-id\": \"" + scannerId + "\",\n"
                + "\"performance-data\": {\n" + "\"cses\": 0,\n" + "\"ses\": 0,\n" + "\"es\": 0,\n"
                + "\"tx-level-max\": 3,\n" + "\"tx-level-avg\": 3,\n" + "\"rx-level-min\": -44,\n"
                + "\"rx-level-max\": -45,\n" + "\"rx-level-avg\": -44,\n" + "\"time2-states\": 0,\n"
                + "\"time4-states-s\": 0,\n" + "\"time4-states\": 0,\n" + "\"time8-states\": -1,\n"
                + "\"time16-states-s\": -1,\n" + "\"time16-states\": 0,\n" + "\"time32-states\": -1,\n"
                + "\"time64-states\": 900,\n" + "\"time128-states\": -1,\n" + "\"time256-states\": -1,\n"
                + "\"time512-states\": -1,\n" + "\"time512-states-l\": -1,\n" + "\"unavailability\": 0,\n"
                + "\"tx-level-min\": 3,\n" + "\"time1024-states\": -1,\n" + "\"time1024-states-l\": -1,\n"
                + "\"time2048-states\": -1,\n" + "\"time2048-states-l\": -1,\n" + "\"time4096-states\": -1,\n"
                + "\"time4096-states-l\": -1,\n" + "\"time8192-states\": -1,\n" + "\"time8192-states-l\": -1,\n"
                + "\"snir-min\": -99,\n" + "\"snir-max\": -99,\n" + "\"snir-avg\": -99,\n" + "\"xpd-min\": -99,\n"
                + "\"xpd-max\": -99,\n" + "\"xpd-avg\": -99,\n" + "\"rf-temp-min\": -99,\n" + "\"rf-temp-max\": -99,\n"
                + "\"rf-temp-avg\": -99,\n" + "\"defect-blocks-sum\": -1,\n" + "\"time-period\": 900\n" + "},\n"
                + "\"suspect-interval-flag\": false\n" + "}";

        if (timeInterval.equals(GranularityPeriodType.Period15Min)) {
            return dbRawProvider.doUpdateOrCreate(Entity.Historicalperformance15min.getName(), initialDbId, json);
        } else {
            return dbRawProvider.doUpdateOrCreate(Entity.Historicalperformance24h.getName(), initialDbId, json);
        }
    }

}
