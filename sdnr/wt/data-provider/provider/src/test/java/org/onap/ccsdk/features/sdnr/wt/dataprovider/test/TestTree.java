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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.ElasticSearchDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.DataTreeHttpServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.DataTreeObject;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.DataTreeHttpServlet.EntityWithTree;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.DataTreeHttpServlet.FilterMode;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.impl.DataTreeProviderImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Entity;

/**
 * @author Michael Dürre
 *
 */
public class TestTree {

    public static HostInfo[] hosts = new HostInfo[] {new HostInfo("localhost", Integer
            .valueOf(System.getProperty("databaseport") != null ? System.getProperty("databaseport") : "49200"))};
    private static ElasticSearchDataProvider dbProvider;
    private static HtDatabaseClient dbRawProvider;

    @BeforeClass
    public static void init() throws Exception {

        dbProvider = new ElasticSearchDataProvider(hosts);
        dbProvider.waitForYellowDatabaseStatus(30, TimeUnit.SECONDS);
        dbRawProvider = new HtDatabaseClient(hosts);
    }

    @Test
    public void testInventoryTree() {
        DataTreeProviderImpl provider = new DataTreeProviderImpl();
        provider.setDatabaseClient(dbRawProvider);
        DeleteByQueryRequest query = new DeleteByQueryRequest(Entity.Inventoryequipment.getName(),true);
        query.setQuery(QueryBuilders.matchAllQuery().toJSON());
        try {
			dbRawProvider.deleteByQuery(query);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        try {
            DataTreeObject tree = provider.readInventoryTree(null, null, FilterMode.Lazy);
            //			System.out.println(tree.toJSON());
            //			tree = provider.readInventoryTree(null, "CARD*", FilterMode.Lazy);
            //			System.out.println(tree.toJSON());
            //			tree = provider.readInventoryTree(null, "CARD*", FilterMode.Strict);
            //			System.out.println(tree.toJSON());
            tree = provider.readInventoryTree(Arrays.asList("sim1"), "CARD", FilterMode.Lazy);
            System.out.println(tree.toJSON());
            //			tree = provider.readInventoryTree(Arrays.asList( "sim1", "IDU-1.55.0.0" ), "CARD*", FilterMode.Lazy);
            //			System.out.println(tree.toJSON());
            //			tree = provider.readInventoryTree(Arrays.asList( "sim1", "IDU-1.55.0.0" ), null, FilterMode.Lazy);
            //			System.out.println(tree.toJSON());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testUriConversion() {
        EntityWithTree e = DataTreeHttpServlet.getEntity("/tree/read-inventoryequipment-tree/sim1/sim1%2FODU");
        System.out.println(e);
        e = DataTreeHttpServlet.getEntity("/tree/read-inventoryequipment-tree/");
        System.out.println(e);
    }
}
