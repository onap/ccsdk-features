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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DatabaseDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.impl.ElasticSearchDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.impl.MediatorServerDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.test.util.HostInfoForTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMediatorServerInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMediatorServerOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;

/**
 * @author Michael Dürre
 */
public class TestMediatorServerService {
    private static DatabaseDataProvider dbProvider;
    private static HtDatabaseClient dbRawProvider;
    private static MediatorServerDataProvider service = null;


    @BeforeClass
    public static void init() throws Exception {
        HostInfo[] hosts = HostInfoForTest.get();
        dbProvider = new ElasticSearchDataProvider(hosts);
        dbProvider.waitForYellowDatabaseStatus(30, TimeUnit.SECONDS);
        dbRawProvider = HtDatabaseClient.getClient(hosts);
        service = new MediatorServerDataProvider(dbProvider.getHtDatabaseMediatorServer());
    }

    @Test
    public void test() {
        clearDbEntity(Entity.MediatorServer);
        System.out.println(service.triggerReloadSync());
        String dbServerId = "abc";
        String host = service.getHostUrl(dbServerId);
        assertNull(host);
        final String NAME = "ms1";
        final String HOST = "http://10.20.30.40:7070";
        CreateMediatorServerOutputBuilder output = null;
        try {
            output = dbProvider
                    .createMediatorServer(new CreateMediatorServerInputBuilder().setName(NAME).setUrl(HOST).build());
        } catch (IOException e) {
            e.printStackTrace();
            fail("unable to create ms entry: " + e.getMessage());
        }
        System.out.println(service.triggerReloadSync());
        host = service.getHostUrl(output.getId());
        assertEquals(HOST, host);

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
}
