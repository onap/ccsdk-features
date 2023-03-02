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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DatabaseDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.entity.FaultEntityManager;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.entity.HtDatabaseEventsService;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.impl.ElasticSearchDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.YangHelper2;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.test.util.HostInfoForTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmNotificationType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmOperation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmSourceIndicator;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * @author Michael Dürre
 *
 */
public class TestEventService {
    private static DatabaseDataProvider dbProvider;
    private static HtDatabaseClient dbRawProvider;
    private static HtDatabaseEventsService service = null;

    private static final String NODEID = "node1";
    private static final String NODEID2 = "node2";
    private static final String NODEID3 = "node3";
    private static final String OBJECTREFID1 = "objid1";
    private static final String OBJECTREFID2 = "objid2";

    @BeforeClass
    public static void init() throws Exception {

        HostInfo[] hosts = HostInfoForTest.get();
        dbProvider = new ElasticSearchDataProvider(hosts);
        dbProvider.waitForYellowDatabaseStatus(30, TimeUnit.SECONDS);
        dbRawProvider = HtDatabaseClient.getClient(hosts);

        try {
            service = new HtDatabaseEventsService(dbRawProvider);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testClearFaultsCurrent() {

        service.clearFaultsCurrentOfNode(NODEID);
        service.clearFaultsCurrentOfNode(NODEID2);

        List<String> nodeIds = service.getAllNodesWithCurrentAlarms();
        if (nodeIds.size() > 0) {
            for (String nodeId : nodeIds) {
                service.clearFaultsCurrentOfNode(nodeId);
            }
        }
        service.updateFaultCurrent(createFault(NODEID, OBJECTREFID1, "abc", SeverityType.Major));
        service.updateFaultCurrent(createFault(NODEID, OBJECTREFID1, "abcde", SeverityType.Major));
        service.updateFaultCurrent(createFault(NODEID, OBJECTREFID2, "abcde", SeverityType.Major));
        service.updateFaultCurrent(createFault(NODEID2, OBJECTREFID2, "abcde", SeverityType.Major));
        nodeIds = service.getAllNodesWithCurrentAlarms();
        assertTrue(nodeIds.size() == 2);
        service.clearFaultsCurrentOfNodeWithObjectId(NODEID, OBJECTREFID1);
        nodeIds = service.getAllNodesWithCurrentAlarms();
        assertTrue(nodeIds.size() == 2);
        service.updateFaultCurrent(createFault(NODEID, OBJECTREFID2, "abcde", SeverityType.NonAlarmed));
        nodeIds = service.getAllNodesWithCurrentAlarms();
        assertTrue(nodeIds.size() == 1);
    }


    @Test
    public void testGenSpecificEsId() {
        String objectRefOld = FaultEntityManager.genSpecificEsId(createFault(NODEID, "[layerProtocol="+OBJECTREFID1+"]", "abc", SeverityType.Major));
        assertEquals(String.format("%s/%s/%s", NODEID, OBJECTREFID1, "abc"), objectRefOld);
        String objectRefOld2 = FaultEntityManager.genSpecificEsId(createFault(NODEID2, "[layerProtocol="+OBJECTREFID2+"]", "abcde", SeverityType.Major));
        assertEquals(String.format("%s/%s/%s", NODEID2, OBJECTREFID2, "abcde"), objectRefOld2);
        String objectRef = FaultEntityManager.genSpecificEsId(createFault(NODEID, OBJECTREFID1, "abc", SeverityType.Major));
        assertEquals(String.format("%s/%s/%s", NODEID, OBJECTREFID1, "abc"), objectRef);
        String objectRef2 = FaultEntityManager.genSpecificEsId(createFault(NODEID2, OBJECTREFID2, "abcde", SeverityType.Major));
        assertEquals(String.format("%s/%s/%s", NODEID2, OBJECTREFID2, "abcde"), objectRef2);
    }

    private static FaultcurrentEntity createFault(String nodeId, String objectRefId, String problem,
            SeverityType severity) {
        return createFault(nodeId, objectRefId, problem, severity, NetconfTimeStampImpl.getConverter().getTimeStamp());
    }

    /**
     * @param nODENAME
     * @param problem
     * @param ts
     * @param severity
     * @return
     */
    private static FaultcurrentEntity createFault(String nodeId, String objectRefId, String problem,
            SeverityType severity, DateAndTime ts) {
        return new FaultcurrentBuilder().setNodeId(nodeId).setObjectId(objectRefId).setTimestamp(ts)
                .setSeverity(severity).setProblem(problem).build();
    }

    @Test
    public void testIndexClean() {
        Date now = new Date();
        service.doIndexClean(now);
        clearDbEntity(Entity.Eventlog);
        clearDbEntity(Entity.Faultlog);
        clearDbEntity(Entity.Cmlog);
        TestCRUDforDatabase.trySleep(1000);
        service.writeEventLog(createEventLog(NODEID, OBJECTREFID1, "aaa", "abc", 1));
        service.writeEventLog(createEventLog(NODEID, OBJECTREFID1, "aaa", "avasvas", 2));

        service.writeFaultLog(createFaultLog(NODEID, OBJECTREFID2, "problem", SeverityType.Major, 1));
        service.writeFaultLog(createFaultLog(NODEID, OBJECTREFID2, "problem", SeverityType.NonAlarmed, 2));
        service.writeFaultLog(createFaultLog(NODEID2, OBJECTREFID2, "problem", SeverityType.Major, 1));

        service.writeCMLog(createCMLog(NODEID3, 1, CmNotificationType.NotifyMOIChanges,
            "1", CmSourceIndicator.MANAGEMENTOPERATION, CmOperation.CREATE, "value"));

        TestCRUDforDatabase.trySleep(100);
        now = new Date();
        long numOlds = service.getNumberOfOldObjects(now);
        assertEquals(6, numOlds);
        TestCRUDforDatabase.trySleep(100);
        service.writeFaultLog(createFaultLog(NODEID, OBJECTREFID2, "problem", SeverityType.Major, 3));
        service.writeFaultLog(createFaultLog(NODEID, OBJECTREFID2, "problem", SeverityType.NonAlarmed, 5));
        service.writeFaultLog(createFaultLog(NODEID, OBJECTREFID2, "problem", SeverityType.Major, 6));

        service.writeCMLog(createCMLog(NODEID3, 2, CmNotificationType.NotifyMOIChanges,
            "2", CmSourceIndicator.SONOPERATION, CmOperation.REPLACE, "value2"));

        numOlds = service.getNumberOfOldObjects(now);
        assertEquals(6, numOlds);
        now = new Date();
        numOlds = service.getNumberOfOldObjects(now);
        assertEquals(10, numOlds);
        service.doIndexClean(now);
        numOlds = service.getNumberOfOldObjects(now);
        assertEquals(0, numOlds);

    }

    @Test
    public void testPm() {
        final String IFNAME1 = "if1";
        final String SCNID1 = "scn1";
        List<PmdataEntity> list =
                Arrays.asList(createPmData(NODEID, IFNAME1, SCNID1), createPmData(NODEID, IFNAME1, SCNID1),
                        createPmData(NODEID, IFNAME1, SCNID1), createPmData(NODEID, IFNAME1, SCNID1)

                );
        service.doWritePerformanceData(list);
    }

    /**
     * @param ifname
     * @param ifUuid
     * @param scannerId
     * @param nodename3
     * @return
     */
    private static PmdataEntity createPmData(String nodeId, String ifUuid, String scannerId) {
        return new PmdataEntityBuilder().setNodeName(nodeId).setGranularityPeriod(GranularityPeriodType.Period15Min)
                .setUuidInterface(ifUuid).setScannerId(scannerId).setLayerProtocolName("NETCONF")
                .setPerformanceData(null).setSuspectIntervalFlag(true)
                .setTimeStamp(NetconfTimeStampImpl.getConverter().getTimeStamp()).build();
    }

    @Test
    public void testNeConnection() {
        service.removeNetworkConnection(NODEID);
        service.removeNetworkConnection(NODEID2);

        clearDbEntity(Entity.NetworkelementConnection);
        List<NetworkElementConnectionEntity> nes = service.getNetworkElementConnections();
        assertEquals(0, nes.size());
        service.updateNetworkConnection22(createNeConnection(NODEID, NetworkElementDeviceType.Unknown, null), NODEID);
        service.updateNetworkConnection22(createNeConnection(NODEID, NetworkElementDeviceType.Unknown, "old"), NODEID);
        service.updateNetworkConnection22(createNeConnection(NODEID2, NetworkElementDeviceType.ORAN, "old"), NODEID2);
        nes = service.getNetworkElementConnections();
        assertEquals(2, nes.size());
        service.updateNetworkConnectionDeviceType(createNeConnection(NODEID, NetworkElementDeviceType.Wireless,"old"),
                NODEID);
        nes = service.getNetworkElementConnections();
        assertEquals(2, nes.size());
        boolean found = false;
        for (NetworkElementConnectionEntity ne : nes) {
            if (NODEID.equals(ne.getNodeId()) && ne.getDeviceType() == NetworkElementDeviceType.Wireless) {
                found = true;
            }
        }
        assertTrue(found);

    }

    @Test
    public void testConnectionLog() {
        clearDbEntity(Entity.Connectionlog);
        service.writeConnectionLog(createConnectionLog(NODEID, ConnectionLogStatus.Mounted));
        service.writeConnectionLog(createConnectionLog(NODEID, ConnectionLogStatus.Mounted));
        assertEquals(2, getDbEntityEntries(Entity.Connectionlog).getTotal());
    }

    /**
     * @param nodeId
     * @param status
     * @return
     */
    private static ConnectionlogEntity createConnectionLog(String nodeId, ConnectionLogStatus status) {
        return new ConnectionlogBuilder().setNodeId(nodeId)
                .setTimestamp(NetconfTimeStampImpl.getConverter().getTimeStamp()).setStatus(status).build();
    }

    @Test
    public void testInventory() {
        clearDbEntity(Entity.Inventoryequipment);
        service.writeInventory(NODEID,Arrays.asList(createEquipment(NODEID, "uuid1"), createEquipment(NODEID, "uuid2"),
                createEquipment(NODEID, "uuid3"), createEquipment(NODEID, "uuid4"),
                createEquipment(NODEID, "uuid5")));
        assertEquals(5, getDbEntityEntries(Entity.Inventoryequipment).getTotal());
    }

    private static SearchResult<SearchHit> getDbEntityEntries(Entity entity) {
        return dbRawProvider.doReadAllJsonData(entity.getName());
    }

    private static void clearDbEntity(Entity entity) {
        DeleteByQueryRequest query = new DeleteByQueryRequest(entity.getName());
        query.setQuery(QueryBuilders.matchAllQuery().toJSON());
        try {
            dbRawProvider.deleteByQuery(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TestCRUDforDatabase.trySleep(1000);
    }

    /**
     * @param nodeId
     * @param uuid
     * @return
     */
    private Inventory createEquipment(String nodeId, String uuid) {
        return new InventoryBuilder().setNodeId(nodeId).setParentUuid(null).setDescription("desc")
                .setTreeLevel(Uint32.valueOf(0)).setManufacturerName("manu")
                .setDate(NetconfTimeStampImpl.getConverter().getTimeStampAsNetconfString()).setUuid(uuid).build();
    }

    /**
     * @param devType
     * @param mountMethod
     * @param nodename3
     * @return
     */
    private static NetworkElementConnectionEntity createNeConnection(String nodeId, NetworkElementDeviceType devType, String mountMethod) {
        return new NetworkElementConnectionBuilder().setNodeId(nodeId).setHost("host")
                .setPort(YangHelper2.getLongOrUint32(1234L)).setCoreModelCapability("123")//.setMountMethod(mountMethod)
                .setStatus(ConnectionLogStatus.Connected).setDeviceType(devType).setIsRequired(true).build();
    }

    /**
     * @param nodeId
     * @param counter
     * @param notificationType
     * @param notificationId
     * @param sourceIndicator
     * @param operation
     * @param value
     * @return
     */
    private static CmlogEntity createCMLog(String nodeId, int counter, CmNotificationType notificationType,
                                           String notificationId, CmSourceIndicator sourceIndicator,
                                           CmOperation operation, String value) {
        return new CmlogBuilder()
            .setNodeId(nodeId)
            .setCounter(counter)
            .setNotificationType(notificationType)
            .setNotificationId(notificationId)
            .setSourceIndicator(sourceIndicator)
            .setOperation(operation)
            .setValue(value)
            .setTimestamp(NetconfTimeStampImpl.getConverter().getTimeStamp())
            .build();
    }

    /**
     * @param nodeId
     * @param objectId
     * @param problem
     * @param severity
     * @param counter
     * @return
     */
    private static FaultlogEntity createFaultLog(String nodeId, String objectId, String problem, SeverityType severity,
            int counter) {
        return new FaultlogBuilder().setNodeId(nodeId).setObjectId(objectId).setProblem(problem).setSeverity(severity)
                .setCounter(counter).setTimestamp(NetconfTimeStampImpl.getConverter().getTimeStamp())
                .setSourceType(SourceType.Netconf).build();
    }

    /**
     * @param nodeId
     * @param objectId
     * @param attributeName
     * @param newValue
     * @param counter
     * @return
     */
    private static EventlogEntity createEventLog(String nodeId, String objectId, String attributeName, String newValue,
            int counter) {
        return new EventlogBuilder().setNodeId(nodeId).setObjectId(objectId).setAttributeName(attributeName)
                .setNewValue(newValue).setCounter(counter)
                .setTimestamp(NetconfTimeStampImpl.getConverter().getTimeStamp()).setSourceType(SourceType.Netconf)
                .build();
    }
}
