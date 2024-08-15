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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.dblib.test;

import ch.vorburger.exec.ManagedProcessException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlDBDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBReaderWriter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.DeleteQuery;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.dblib.test.util.MariaDBTestBase;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtUserdataManager;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmNotificationType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmSourceIndicator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMediatorServerInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMediatorServerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenanceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMediatorServerInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteNetworkElementConnectionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Guicutthrough;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GuicutthroughBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadCmlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadConnectionlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadEventlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultcurrentListInputBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMaintenanceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMediatorServerInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMediatorServerInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.PaginationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.network.element.connection.list.output.Data;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class TestMariaDataProvider {


    private static final String NODEID1 = "node1";
    private static final String NODEID2 = "node2";
    private static final String NODEID22 = "node22";
    private static final String NODEID3 = "node3";
    private static final String NODEID4 = "node4";
    private static final String NODEID5 = "node5";
    private static final String PROBLEM1 = "problem1";
    private static final String TIME1 = "2021-05-25T05:12:55.0Z";
    private static final String TIME2 = "2021-05-25T05:12:56.0Z";
    private static final String TIME3 = "2021-05-25T05:12:57.0Z";
    private static final String PROBLEM2 = "problem2";
    private static final String URI1 = "http://localhost:8181";
    private static final String URI2 = "http://localhost:8181";
    private static final String URI3 = "http://localhost:8181";
    private static final String PATH = "https://samsung.com/3GPP/simulation/network-function/ves";
    private static final String USERNAME = "admin";
    private static MariaDBTestBase testBase;
    private static SqlDBDataProvider dbProvider;
    private static SqlDBDataProvider dbProviderOverall;
    private static SqlDBClient dbClient;
    private static String CONTROLLERID;

    @BeforeClass
    public static void init() throws Exception {

        testBase = new MariaDBTestBase();
        dbProvider = testBase.getDbProvider();
        dbProvider.waitForDatabaseReady(30, TimeUnit.SECONDS);
        dbClient = testBase.createRawClient();
        MariaDBTestBase.testCreateTableStructure(dbClient);
        dbProvider.setControllerId();
        CONTROLLERID = dbProvider.getControllerId();
        dbProviderOverall = testBase.getOverallDbProvider();

    }

    @AfterClass
    public static void close() {
        try {
            testBase.close();
        } catch (ManagedProcessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFaultcurrent() {
        dbProvider.clearFaultsCurrentOfNode(NODEID1);
        ReadFaultcurrentListOutputBuilder faultCurrents =
                dbProvider.readFaultCurrentList(createInput("node-id", NODEID1, 1, 20));
        assertEquals(0, faultCurrents.getData().size());
        FaultcurrentEntity faultCurrent1 = new FaultcurrentBuilder().setNodeId(NODEID1).setCounter(1).setObjectId("obj")
                .setProblem(PROBLEM1).setTimestamp(DateAndTime.getDefaultInstance(TIME1))
                .setSeverity(SeverityType.Major).setId(String.format("%s/%s", NODEID1, PROBLEM1)).build();
        dbProvider.updateFaultCurrent(faultCurrent1);
        FaultcurrentEntity faultCurrent2 = new FaultcurrentBuilder().setNodeId(NODEID1).setCounter(1).setObjectId("obj")
                .setProblem(PROBLEM2).setTimestamp(DateAndTime.getDefaultInstance(TIME1))
                .setSeverity(SeverityType.Minor).setId(String.format("%s/%s", NODEID1, PROBLEM2)).build();
        dbProvider.updateFaultCurrent(faultCurrent2);
        faultCurrents = dbProvider.readFaultCurrentList(createInput("node-id", NODEID1, 1, 20));
        assertEquals(2, faultCurrents.getData().size());
        ReadStatusOutputBuilder status = null;
        try {
            EntityInput input = null;
            status = dbProvider.readStatus(input);
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to read status");
        }
        assertEquals(0, status.getData().get(0).getFaults().getCriticals().intValue());
        assertEquals(1, status.getData().get(0).getFaults().getMajors().intValue());
        assertEquals(1, status.getData().get(0).getFaults().getMinors().intValue());
        assertEquals(0, status.getData().get(0).getFaults().getWarnings().intValue());

        List<String> nodeList = dbProvider.getAllNodesWithCurrentAlarms();
        assertTrue(nodeList.contains(NODEID1));
        assertEquals(1, nodeList.size());

        faultCurrent1 = new FaultcurrentBuilder().setNodeId(NODEID1).setCounter(1).setObjectId("obj")
                .setProblem(PROBLEM1).setTimestamp(DateAndTime.getDefaultInstance(TIME1))
                .setSeverity(SeverityType.NonAlarmed).setId(String.format("%s/%s", NODEID1, PROBLEM1)).build();
        dbProvider.updateFaultCurrent(faultCurrent1);
        faultCurrents = dbProvider.readFaultCurrentList(createInput("node-id", NODEID1, 1, 20));
        assertEquals(1, faultCurrents.getData().size());


    }

    @Test
    public void testSerializeDeserialize() {

        try {
            CreateNetworkElementConnectionOutputBuilder necon = dbProvider.createNetworkElementConnection(
                    new NetworkElementConnectionBuilder().setNodeId(NODEID1).setIsRequired(Boolean.TRUE).build());
            List<Data> netestList =
                    dbProvider.readNetworkElementConnectionList(createInput("node-id", NODEID1, 1, 20)).getData();

            assertNotNull(necon);
            assertEquals(1, netestList.size());
            assertTrue(netestList.get(0).getIsRequired());
            SqlDBReaderWriter<Data> dbrw = new SqlDBReaderWriter<>(dbClient, Entity.NetworkelementConnection,
                    MariaDBTestBase.SUFFIX,
                    Data.class,
                    CONTROLLERID);
            Data e = dbrw.read(NODEID1);
            assertNotNull(e);

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testFaultlog() {
        try {
            dbClient.delete(new DeleteQuery(Entity.Faultcurrent, null).toSql());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("problem clearing faultlog");
        }
        ReadFaultlogListOutputBuilder faultlogs = dbProvider.readFaultLogList(createInput(1, 20));
        assertEquals(0, faultlogs.getData().size());
        FaultlogEntity fault1 = new FaultlogBuilder().setCounter(1).setNodeId(NODEID1).setObjectId("obj")
                .setProblem(PROBLEM1).setSeverity(SeverityType.Major).setSourceType(SourceType.Netconf)
                .setTimestamp(DateAndTime.getDefaultInstance(TIME1)).build();
        dbProvider.writeFaultLog(fault1);
        FaultlogEntity fault2 = new FaultlogBuilder().setCounter(2).setNodeId(NODEID1).setObjectId("obj")
                .setProblem(PROBLEM2).setSeverity(SeverityType.Major).setSourceType(SourceType.Netconf)
                .setTimestamp(DateAndTime.getDefaultInstance(TIME1)).build();
        dbProvider.writeFaultLog(fault2);
        faultlogs = dbProvider.readFaultLogList(createInput("node-id", NODEID1, 1, 20));
        assertEquals(2, faultlogs.getData().size());

    }

    @Test
    public void testCMlog() {
        ReadCmlogListOutputBuilder cmlogs = dbProvider.readCMLogList(createInput(1, 20));
        assertEquals(0, cmlogs.getData().size());

        CmlogEntity cm1 =
                new CmlogBuilder().setNodeId(NODEID2).setCounter(1).setTimestamp(DateAndTime.getDefaultInstance(TIME1))
                        .setObjectId("obj").setNotificationType(CmNotificationType.NotifyMOIChanges)
                        .setNotificationId("1").setSourceIndicator(CmSourceIndicator.MANAGEMENTOPERATION).setPath(PATH)
                        .setValue("pnf-registration: true").build();
        CmlogEntity cm2 =
                new CmlogBuilder().setNodeId(NODEID2).setCounter(2).setTimestamp(DateAndTime.getDefaultInstance(TIME2))
                        .setObjectId("obj").setNotificationType(CmNotificationType.NotifyMOIChanges)
                        .setNotificationId("2").setSourceIndicator(CmSourceIndicator.UNKNOWN).setPath(PATH)
                        .setValue("pnf-registration: false").build();

        dbProvider.writeCMLog(cm1);
        dbProvider.writeCMLog(cm2);
        cmlogs = dbProvider.readCMLogList(createInput("node-id", NODEID2, 1, 20));
        assertEquals(2, cmlogs.getData().size());

        List<CmlogEntity> cmLogEntityList = List.of(cm1, cm2);
        assertEquals("node2", cmLogEntityList.get(0).getNodeId());
        assertEquals("obj", cmLogEntityList.get(0).getObjectId());
        assertEquals(CmNotificationType.NotifyMOIChanges, cmLogEntityList.get(0).getNotificationType());
        assertEquals("2", cmLogEntityList.get(1).getNotificationId());

    }

    @Test
    public void testConnectionlog() {
        try {
            dbClient.delete(new DeleteQuery(Entity.Connectionlog, null).toSql());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("problem clearing faultlog");
        }
        ReadConnectionlogListOutputBuilder logs = dbProvider.readConnectionlogList(createInput(1, 20));
        assertEquals(0, logs.getData().size());
        ConnectionlogEntity log1 = new ConnectionlogBuilder().setNodeId(NODEID1)
                .setTimestamp(DateAndTime.getDefaultInstance(TIME1)).setStatus(ConnectionLogStatus.Mounted).build();
        dbProvider.writeConnectionLog(log1);
        ConnectionlogEntity log2 = new ConnectionlogBuilder().setNodeId(NODEID1)
                .setTimestamp(DateAndTime.getDefaultInstance(TIME2)).setStatus(ConnectionLogStatus.Connecting).build();
        dbProvider.writeConnectionLog(log2);
        ConnectionlogEntity log3 = new ConnectionlogBuilder().setNodeId(NODEID1)
                .setTimestamp(DateAndTime.getDefaultInstance(TIME3)).setStatus(ConnectionLogStatus.Connected).build();
        dbProvider.writeConnectionLog(log3);
        logs = dbProvider.readConnectionlogList(createInput(1, 20));
        assertEquals(3, logs.getData().size());
    }

    @Test
    public void testEventlog() {
        try {
            dbClient.delete(new DeleteQuery(Entity.Eventlog, null).toSql());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("problem clearing faultlog");
        }
        ReadEventlogListOutputBuilder logs = null;
        try {
            logs = dbProvider.readEventlogList(createInput(1, 20));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(0, logs.getData().size());
        EventlogEntity log1 = new EventlogBuilder().setCounter(1).setNodeId(NODEID1).setObjectId("obj")
                .setTimestamp(DateAndTime.getDefaultInstance(TIME1)).setAttributeName("attr").setNewValue("new-value")
                .setSourceType(SourceType.Netconf).build();
        dbProvider.writeEventLog(log1);
        EventlogEntity log2 = new EventlogBuilder().setCounter(1).setNodeId(NODEID1).setObjectId("obj")
                .setTimestamp(DateAndTime.getDefaultInstance(TIME2)).setAttributeName("attr").setNewValue("new-value2")
                .setSourceType(SourceType.Netconf).build();
        dbProvider.writeEventLog(log2);
        EventlogEntity log3 = new EventlogBuilder().setCounter(1).setNodeId(NODEID1).setObjectId("obj")
                .setTimestamp(DateAndTime.getDefaultInstance(TIME3)).setAttributeName("attr").setNewValue("new-value3")
                .setSourceType(SourceType.Netconf).build();
        dbProvider.writeEventLog(log3);
        try {
            logs = dbProvider.readEventlogList(createInput(1, 20));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(3, logs.getData().size());
    }

    @Test
    public void testGuicutthrough() {
        try {
            dbClient.delete(new DeleteQuery(Entity.Guicutthrough, null).toSql());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("problem clearing faultlog");
        }
        Guicutthrough gc1 = new GuicutthroughBuilder().setName(NODEID1).setWeburi(URI1).build();
        dbProvider.writeGuiCutThroughData(gc1, NODEID1);
        Guicutthrough gc2 = new GuicutthroughBuilder().setName(NODEID2).setWeburi(URI2).build();
        dbProvider.writeGuiCutThroughData(gc2, NODEID2);
        Guicutthrough gc3 = new GuicutthroughBuilder().setName(NODEID3).setWeburi(URI3).build();
        dbProvider.writeGuiCutThroughData(gc3, NODEID3);
        ReadGuiCutThroughEntryOutputBuilder data = dbProvider.readGuiCutThroughEntry(createInput(1, 20));
        assertEquals(3, data.getData().size());
        data = dbProvider.readGuiCutThroughEntry(createInput("name", NODEID1, 1, 20));
        assertEquals(1, data.getData().size());

    }

    @Test
    public void testInventory() {
        try {
            dbClient.delete(new DeleteQuery(Entity.Inventoryequipment, null).toSql());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("problem clearing faultlog");
        }
        ReadInventoryListOutputBuilder data = dbProvider.readInventoryList(createInput(1, 20));
        assertEquals(0, data.getData().size());
        List<Inventory> list = null;
        try {
            list = loadListFile("/inventory.json", Inventory.class);
        } catch (IOException e) {
            e.printStackTrace();
            fail("problem loading inventory data");

        }
        dbProvider.writeInventory(NODEID1, list);
        data = dbProvider.readInventoryList(createInput(1, 50));
        assertEquals(22, data.getData().size());
        ReadInventoryDeviceListOutputBuilder data2 = dbProvider.readInventoryDeviceList(createInput(1, 20));
        assertEquals(2, data2.getData().size());
        assertTrue(data2.getData().contains("sim1") && data2.getData().contains("sim2"));
        data = dbProvider.readInventoryList(createInput("tree-level", "0", 1, 50));
        assertEquals(5, data.getData().size());

        try {
            dbProvider.writeInventory("sim3", loadListFile("/inventory2.json", Inventory.class));
        } catch (IOException e) {
            fail("problem loading inventory data2");
        }
        data2 = dbProvider.readInventoryDeviceList(createInput(1, 20));
        assertEquals(3, data2.getData().size());
        assertTrue(data2.getData().contains("sim1") && data2.getData().contains("sim2") &&
                data2.getData().contains("sim3"));
    }

    @Test
    public void testInventoryWithComplexTypes() {
        try {
            dbClient.delete(new DeleteQuery(Entity.Inventoryequipment, null).toSql());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("problem clearing inventoryequipment");
        }
        ReadInventoryListOutputBuilder data = dbProvider.readInventoryList(createInput(1, 20));
        assertEquals(0, data.getData().size());
        try {
            Inventory inventory = new InventoryBuilder()
                    .setContainedHolder(new HashSet<>(
                            Arrays.asList("STM1-1", "Radio-2A", "LAN-3-SFP", "Radio-1A", "STM1-2", "LAN-4-SFP")))
                    .setSerial("14209652001003620").setDescription("INDOOR UNIT ALCPlus2e")
                    .setTreeLevel(Uint32.valueOf(0)).setNodeId("NTS_ONF14").build();
            dbProvider.writeInventory(NODEID1, new ArrayList<Inventory>(Arrays.asList(inventory)));
        } catch (Exception e) {
            e.printStackTrace();
            fail("problem loading inventory data");

        }
        data = dbProvider.readInventoryList(createInput(1, 50));
        assertEquals(1, data.getData().size());
    }

    @Test
    public void testMaintenance() {
        try {
            dbClient.delete(new DeleteQuery(Entity.Maintenancemode, null).toSql());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("problem clearing faultlog");
        }
        ReadMaintenanceListOutputBuilder data = dbProvider.readMaintenanceList(createInput(1, 20));
        assertEquals(0, data.getData().size());
        CreateMaintenanceInput maint1 = new CreateMaintenanceInputBuilder().setId(NODEID1).setNodeId(NODEID1)
                .setActive(true).setDescription("desc").setObjectIdRef("ref").setProblem("problem")
                .setStart(DateAndTime.getDefaultInstance(TIME1)).setEnd(DateAndTime.getDefaultInstance(TIME3)).build();
        CreateMaintenanceInput maint2 = new CreateMaintenanceInputBuilder().setId(NODEID2).setNodeId(NODEID2)
                .setActive(true).setDescription("desc").setObjectIdRef("ref").setProblem("problem2")
                .setStart(DateAndTime.getDefaultInstance(TIME1)).setEnd(DateAndTime.getDefaultInstance(TIME3)).build();
        CreateMaintenanceInput maint3 = new CreateMaintenanceInputBuilder().setId(NODEID3).setNodeId(NODEID3)
                .setActive(true).setDescription("desc").setObjectIdRef("ref").setProblem("problem3")
                .setStart(DateAndTime.getDefaultInstance(TIME1)).setEnd(DateAndTime.getDefaultInstance(TIME3)).build();
        try {
            dbProvider.createMaintenance(maint1);
            dbProvider.createMaintenance(maint2);
            dbProvider.createMaintenance(maint3);
        } catch (IOException e) {
            e.printStackTrace();
            fail("unable to create maintenance data");
        }
        data = dbProvider.readMaintenanceList(createInput(1, 20));
        assertEquals(3, data.getData().size());

        UpdateMaintenanceInput update1 =
                new UpdateMaintenanceInputBuilder().setId(NODEID1).setNodeId(NODEID1).setActive(false).build();
        try {
            dbProvider.updateMaintenance(update1);
        } catch (IOException e) {
            e.printStackTrace();
            fail("unable to update maintenance data");
        }
        data = dbProvider.readMaintenanceList(createInput("active", "false", 1, 20));
        assertEquals(1, data.getData().size());
        DeleteMaintenanceInput delete1 = new DeleteMaintenanceInputBuilder().setId(NODEID1).build();
        try {
            dbProvider.deleteMaintenance(delete1);
        } catch (IOException e) {
            e.printStackTrace();
            fail("unable to delete maintenance data");
        }
        data = dbProvider.readMaintenanceList(createInput(1, 20));
        assertEquals(2, data.getData().size());
        try {
            dbClient.delete(new DeleteQuery(Entity.Maintenancemode, null).toSql());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("problem clearing faultlog");
        }
        final String nodeId = "maint_node1";
        HtDatabaseMaintenance maintenanceService = dbProvider.getHtDatabaseMaintenance();
        MaintenanceEntity e = maintenanceService.createIfNotExists(nodeId);
        assertNotNull(e);
        assertEquals(nodeId, e.getNodeId());
        MaintenanceEntity e2 = new CreateMaintenanceInputBuilder(e).setActive(true).build();
        e = maintenanceService.setMaintenance(e2);
        assertNotNull(e);
        assertEquals(nodeId, e.getNodeId());
        assertTrue(e.getActive());
        maintenanceService.deleteIfNotRequired(nodeId);
        data = dbProvider.readMaintenanceList(createInput("node-id", nodeId, 1, 20));
        assertEquals(0, data.getData().size());

    }

    @Test
    public void testMediatorserver() {
        try {
            dbClient.delete(new DeleteQuery(Entity.MediatorServer, null).toSql());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("problem clearing mediator server");
        }
        ReadMediatorServerListOutputBuilder data = dbProvider.readMediatorServerList(createInput(1, 20));
        assertEquals(0, data.getData().size());
        CreateMediatorServerInput mediator1 =
                new CreateMediatorServerInputBuilder().setName("server1").setUrl("http://10.20.30.40:7070").build();
        CreateMediatorServerInput mediator2 =
                new CreateMediatorServerInputBuilder().setName("server2").setUrl("http://10.20.30.42:7070").build();
        CreateMediatorServerInput mediator3 =
                new CreateMediatorServerInputBuilder().setName("server3").setUrl("http://10.20.30.43:7070").build();
        CreateMediatorServerOutputBuilder output1 = null, output2 = null;
        try {
            output1 = dbProvider.createMediatorServer(mediator1);
            output2 = dbProvider.createMediatorServer(mediator2);
            dbProvider.createMediatorServer(mediator3);
        } catch (IOException e) {
            e.printStackTrace();
            fail("problem creating mediator servers");
        }
        data = dbProvider.readMediatorServerList(createInput(1, 20));
        assertEquals(3, data.getData().size());
        UpdateMediatorServerInput update1 = new UpdateMediatorServerInputBuilder().setId(output1.getId())
                .setName("server1").setUrl("http://10.20.30.40:7071").build();
        try {
            dbProvider.updateMediatorServer(update1);
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to update mediator server");
        }
        data = dbProvider.readMediatorServerList(createInput("id", output1.getId(), 1, 20));
        assertEquals(1, data.getData().size());
        assertEquals(output1.getId(), data.getData().get(0).getId());
        assertEquals("server1", data.getData().get(0).getName());
        assertEquals("http://10.20.30.40:7071", data.getData().get(0).getUrl());

        DeleteMediatorServerInput delete2 = new DeleteMediatorServerInputBuilder().setId(output2.getId()).build();
        try {
            dbProvider.deleteMediatorServer(delete2);
        } catch (IOException e) {
            e.printStackTrace();
            fail("unable to delete mediator server");
        }
        data = dbProvider.readMediatorServerList(createInput("id", output2.getId(), 1, 20));
        assertEquals(0, data.getData().size());
        data = dbProvider.readMediatorServerList(createInput(1, 20));
        assertEquals(2, data.getData().size());
    }

    @Test
    public void testNeConnection() {
        try {
            dbClient.delete(new DeleteQuery(Entity.NetworkelementConnection, null).toSql());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("problem clearing neconnection");
        }
        ReadNetworkElementConnectionListOutputBuilder data =
                dbProvider.readNetworkElementConnectionList(createInput(1, 20));
        assertEquals(0, data.getData().size());
        NetworkElementConnectionEntity ne1 = new NetworkElementConnectionBuilder().setNodeId(NODEID1)
                .setHost("10.20.30.50").setPort(Uint32.valueOf(8300)).setIsRequired(true).setUsername("user")
                .setPassword("passwd").build();
        NetworkElementConnectionEntity ne2 = new NetworkElementConnectionBuilder().setNodeId(NODEID2)
                .setHost("10.20.30.55").setPort(Uint32.valueOf(8300)).setIsRequired(false).setUsername("user")
                .setPassword("passwd").setStatus(ConnectionLogStatus.Connecting).build();
        NetworkElementConnectionEntity ne22 = new NetworkElementConnectionBuilder().setNodeId(NODEID22)
                .setHost("10.20.30.55").setPort(Uint32.valueOf(8300)).setIsRequired(false).setUsername("user")
                .setPassword("passwd").setStatus(ConnectionLogStatus.Connected).build();
        NetworkElementConnectionEntity ne3 = new NetworkElementConnectionBuilder().setNodeId(NODEID3)
                .setHost("10.20.30.55").setPort(Uint32.valueOf(8300)).setIsRequired(false).setUsername("user")
                .setPassword("passwd").setStatus(ConnectionLogStatus.Connecting).build();
        try {
            dbProvider.createNetworkElementConnection(ne1);
            dbProvider.createNetworkElementConnection(ne2);
            dbProvider.createNetworkElementConnection(ne22);
            dbProvider.updateNetworkConnection22(ne3, NODEID3);
        } catch (IOException e) {
            e.printStackTrace();
            fail("problem creating neconnection");
        }
        data = dbProvider.readNetworkElementConnectionList(createInput(1, 20));
        assertEquals(4, data.getData().size());
        NetworkElementConnectionEntity update1 = new NetworkElementConnectionBuilder()
                .setStatus(ConnectionLogStatus.Connected).setDeviceType(NetworkElementDeviceType.ORAN).build();
        dbProvider.updateNetworkConnectionDeviceType(update1, NODEID1);
        data = dbProvider.readNetworkElementConnectionList(createInput("node-id", NODEID1, 1, 20));
        assertEquals(1, data.getData().size());
        assertEquals(NetworkElementDeviceType.ORAN, data.getData().get(0).getDeviceType());
        assertEquals(true, data.getData().get(0).getIsRequired());
        UpdateNetworkElementConnectionInput update2 = new UpdateNetworkElementConnectionInputBuilder().setId(NODEID2)
                .setHost("10.20.55.44").setIsRequired(true).build();
        try {
            dbProvider.updateNetworkElementConnection(update2);
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to update neconnection");
        }
        data = dbProvider.readNetworkElementConnectionList(createInput("node-id", NODEID2, 1, 20));
        assertEquals(1, data.getData().size());
        assertEquals("10.20.55.44", data.getData().get(0).getHost());
        assertEquals(true, data.getData().get(0).getIsRequired());

        ReadStatusOutputBuilder status = null;
        try {
            EntityInput input = null;
            status = dbProvider.readStatus(input);
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to read status");
        }
        assertEquals(2, status.getData().get(0).getNetworkElementConnections().getConnected().intValue());
        assertEquals(2, status.getData().get(0).getNetworkElementConnections().getConnecting().intValue());
        assertEquals(0, status.getData().get(0).getNetworkElementConnections().getDisconnected().intValue());
        assertEquals(0, status.getData().get(0).getNetworkElementConnections().getMounted().intValue());
        assertEquals(4, status.getData().get(0).getNetworkElementConnections().getTotal().intValue());
        assertEquals(0, status.getData().get(0).getNetworkElementConnections().getUnableToConnect().intValue());
        assertEquals(0, status.getData().get(0).getNetworkElementConnections().getUndefined().intValue());
        assertEquals(0, status.getData().get(0).getNetworkElementConnections().getUnmounted().intValue());

        ReadStatusOutputBuilder status2 = null;
        try {
            EntityInput input = createInput("node-id", "node2*", 1, 20);
            status = dbProvider.readStatus(input);
            status2 = dbProviderOverall.readStatus(input);
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to read status");
        }
        assertEquals(1, status.getData().get(0).getNetworkElementConnections().getConnected().intValue());
        assertEquals(1, status.getData().get(0).getNetworkElementConnections().getConnecting().intValue());
        assertEquals(0, status.getData().get(0).getNetworkElementConnections().getDisconnected().intValue());
        assertEquals(0, status.getData().get(0).getNetworkElementConnections().getMounted().intValue());
        assertEquals(2, status.getData().get(0).getNetworkElementConnections().getTotal().intValue());
        assertEquals(0, status.getData().get(0).getNetworkElementConnections().getUnableToConnect().intValue());
        assertEquals(0, status.getData().get(0).getNetworkElementConnections().getUndefined().intValue());
        assertEquals(0, status.getData().get(0).getNetworkElementConnections().getUnmounted().intValue());

        assertEquals(1, status2.getData().get(0).getNetworkElementConnections().getConnected().intValue());
        assertEquals(1, status2.getData().get(0).getNetworkElementConnections().getConnecting().intValue());
        assertEquals(0, status2.getData().get(0).getNetworkElementConnections().getDisconnected().intValue());
        assertEquals(0, status2.getData().get(0).getNetworkElementConnections().getMounted().intValue());
        assertEquals(2, status2.getData().get(0).getNetworkElementConnections().getTotal().intValue());
        assertEquals(0, status2.getData().get(0).getNetworkElementConnections().getUnableToConnect().intValue());
        assertEquals(0, status2.getData().get(0).getNetworkElementConnections().getUndefined().intValue());
        assertEquals(0, status2.getData().get(0).getNetworkElementConnections().getUnmounted().intValue());

        DeleteNetworkElementConnectionInput delete1 =
                new DeleteNetworkElementConnectionInputBuilder().setId(NODEID1).build();
        try {
            dbProvider.deleteNetworkElementConnection(delete1);
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to delete neconnection");
        }
        data = dbProvider.readNetworkElementConnectionList(createInput("node-id", NODEID1, 1, 20));
        assertEquals(0, data.getData().size());
        data = dbProvider.readNetworkElementConnectionList(createInput(1, 20));
        assertEquals(3, data.getData().size());

    }

    @Test
    public void testUserdata() {
        HtUserdataManager mgr = dbProvider.getUserManager();
        String userdata = mgr.getUserdata(USERNAME);
        assertEquals("{}", userdata);
        JSONObject o = new JSONObject();
        o.put("key1", false);
        o.put("key2", "value2");
        boolean result = mgr.setUserdata(USERNAME, o.toString());
        assertTrue(result);
        userdata = mgr.getUserdata(USERNAME);
        o = new JSONObject(userdata);
        assertEquals(false, o.getBoolean("key1"));
        assertEquals("value2", o.getString("key2"));
        o = new JSONObject();
        o.put("enabled", true);
        o.put("name", "abcdef");
        result = mgr.setUserdata(USERNAME, "app1", o.toString());
        assertTrue(result);
        userdata = mgr.getUserdata(USERNAME);
        o = new JSONObject(userdata);
        assertEquals(false, o.getBoolean("key1"));
        assertEquals("value2", o.getString("key2"));
        JSONObject app = o.getJSONObject("app1");
        assertNotNull(app);
        assertEquals(true, app.getBoolean("enabled"));
        assertEquals("abcdef", app.getString("name"));

    }

    @Test
    public void testpm15m() {
        try {
            dbClient.delete(new DeleteQuery(Entity.Historicalperformance15min, null).toSql());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("problem clearing pmdata15m");
        }
        ReadPmdata15mListOutputBuilder data = dbProvider.readPmdata15mList(createInput(1, 20));
        assertEquals(0, data.getData().size());
        List<PmdataEntity> list = null;
        try {
            list = loadListFile("/pmdata15m.json", PmdataEntity.class);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            fail("failed to load pmdata15m");
        }
        dbProvider.doWritePerformanceData(list);
        data = dbProvider.readPmdata15mList(createInput(1, 20));
        assertEquals(10, data.getData().size());
        ReadPmdata15mLtpListOutputBuilder ltpdata = null;
        try {
            ltpdata = dbProvider.readPmdata15mLtpList(createInput("node-name", "sim12600", 1, 20));
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to read pmdata15m ltp list");
        }
        assertEquals(3, ltpdata.getData().size());
        ReadPmdata15mDeviceListOutputBuilder devicedata = null;
        try {
            devicedata = dbProvider.readPmdata15mDeviceList(createInput(1, 20));
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to read pmdata15m devices list");
        }
        assertEquals(1, devicedata.getData().size());
    }

    @Test
    public void testpm24h() {
        try {
            dbClient.delete(new DeleteQuery(Entity.Historicalperformance24h, null).toSql());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("problem clearing pmdata24h");
        }
        ReadPmdata24hListOutputBuilder data = dbProvider.readPmdata24hList(createInput(1, 20));
        assertEquals(0, data.getData().size());
        List<PmdataEntity> list = null;
        try {
            list = loadListFile("/pmdata24h.json", PmdataEntity.class);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            fail("failed to load pmdata24h");
        }
        dbProvider.doWritePerformanceData(list);
        data = dbProvider.readPmdata24hList(createInput(1, 20));
        assertEquals(1, data.getData().size());
        ReadPmdata24hLtpListOutputBuilder ltpdata = null;
        try {
            ltpdata = dbProvider.readPmdata24hLtpList(createInput("node-name", "test", 1, 20));
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to read pmdata15m ltp list");
        }
        assertEquals(1, ltpdata.getData().size());
        ReadPmdata24hDeviceListOutputBuilder devicedata = null;
        try {
            devicedata = dbProvider.readPmdata24hDeviceList(createInput(1, 20));
        } catch (IOException e) {
            e.printStackTrace();
            fail("failed to read pmdata15m devices list");
        }
        assertEquals(1, devicedata.getData().size());
    }

    static EntityInput createInput(int page, int size) {
        return createInput(null, null, page, size);
    }

    private static <T> List<T> loadListFile(String filename, Class<T> clazz) throws JSONException, IOException {
        List<T> list = new ArrayList<>();
        JSONArray a = new JSONArray(loadFile(filename));
        for (int i = 0; i < a.length(); i++) {
            list.add(loadData(a.getJSONObject(i).toString(), clazz));
        }
        return list;
    }

    private static <T> T loadData(String content, Class<T> clazz) throws IOException {
        YangToolsMapper mapper = new YangToolsMapper();
        return mapper.readValue(content, clazz);
    }

    private static String loadFile(String filename) throws IOException {
        return String.join("\n",
                Files.readAllLines(new File(TestMariaDataProvider.class.getResource(filename).getFile()).toPath()));

    }

    static EntityInput createInput(String filter, String filterValue, int page, int size) {
        ReadFaultcurrentListInputBuilder builder = new ReadFaultcurrentListInputBuilder().setPagination(
                new PaginationBuilder().setPage(Uint64.valueOf(page)).setSize(Uint32.valueOf(size)).build());
        if (filter != null && filterValue != null) {
            Filter f = new FilterBuilder().setProperty(filter).setFiltervalue(filterValue).build();
            Map<FilterKey, Filter> fmap = new HashMap<>();
            fmap.put(f.key(), f);
            builder.setFilter(fmap);
        }
        return builder.build();
    }

}
