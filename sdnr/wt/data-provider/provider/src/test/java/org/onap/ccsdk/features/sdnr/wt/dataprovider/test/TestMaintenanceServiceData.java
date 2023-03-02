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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DatabaseDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.impl.ElasticSearchDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.test.util.HostInfoForTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceEntity;

/**
 * - Handling of inital values for Maintenance mode.
 */
public class TestMaintenanceServiceData {

    private static DatabaseDataProvider dbProvider;
    private static HtDatabaseClient dbRawProvider;
    private static HtDatabaseMaintenance service = null;

    private static final String NODEID = "tmsnode1";
    private static final String NODEID2 = "tmsnode2";

    @BeforeClass
    public static void init() throws Exception {

        HostInfo[] hosts = HostInfoForTest.get();
        dbProvider = new ElasticSearchDataProvider(hosts);
        dbProvider.waitForYellowDatabaseStatus(30, TimeUnit.SECONDS);
        dbRawProvider = HtDatabaseClient.getClient(hosts);
        service = dbProvider.getHtDatabaseMaintenance();
    }

    @Test
    public void test() throws InterruptedException {
        clearDbEntity(Entity.Maintenancemode);
        MaintenanceEntity obj = service.createIfNotExists(NODEID);
        assertNotNull("Create first id", obj);
        obj = service.createIfNotExists(NODEID2);
        assertNotNull("Create second id", obj);
        obj = service.getMaintenance(NODEID);
        assertNotNull(obj);
        List<MaintenanceEntity> list = service.getAll();
        assertEquals("Verify for two ids", 2, list.size());
        service.deleteIfNotRequired(NODEID);
        obj = service.getMaintenance(NODEID);
        assertNull("Check if first id was removed", obj);

        obj = service.setMaintenance(createMaintenance(NODEID, true));


    }

    /**
     * @param nodeId
     * @param active
     * @return
     */
    private static MaintenanceEntity createMaintenance(String nodeId, Boolean active) {
        return new MaintenanceBuilder().setNodeId(nodeId).setActive(active).setProblem("problem")
                .setObjectIdRef("idref").build();
    }

    /**
     * Delete
     *
     * @param entity
     */
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

}
