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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.apache.sshd.common.util.io.IoUtils;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.common.test.JSONAssert;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.ElasticSearchDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.DataTreeHttpServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.DataTreeHttpServlet.EntityWithTree;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.DataTreeHttpServlet.FilterMode;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.DataTreeObject;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.impl.DataTreeProviderImpl;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.test.util.HostInfoForTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Entity;

/**
 * @author Michael Dürre
 *
 */
public class TestTree {

    private static ElasticSearchDataProvider dbProvider;
    private static HtDatabaseClient dbRawProvider;

    @BeforeClass
    public static void init() throws Exception {
        HostInfo[] hosts = HostInfoForTest.get();
        dbProvider = new ElasticSearchDataProvider(hosts);
        dbProvider.waitForYellowDatabaseStatus(30, TimeUnit.SECONDS);
        dbRawProvider = HtDatabaseClient.getClient(hosts);
        DeleteByQueryRequest query = new DeleteByQueryRequest(Entity.Inventoryequipment.getName(), true);
        query.setQuery(QueryBuilders.matchAllQuery().toJSON());
        dbRawProvider.deleteByQuery(query);
        fillTestData();
    }

    private static void fillTestData() throws IOException {
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/a2.module-1.1.5.5", getFileContent("/testequipment/1.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/CARD-1.1.7.0", getFileContent("/testequipment/2.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/a2.module-1.55.1.2", getFileContent("/testequipment/3.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/a2.module-1.65.1.2", getFileContent("/testequipment/4.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/SHELF-1.1.0.0", getFileContent("/testequipment/5.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/a2.module-1.1.1.5", getFileContent("/testequipment/6.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/a2.module-1.1.1.8", getFileContent("/testequipment/7.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/a2.module-1.1.6.5", getFileContent("/testequipment/8.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/ODU-1.56.0.0", getFileContent("/testequipment/9.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/a2.module-1.56.1.2", getFileContent("/testequipment/10.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/IDU-1.65.0.0", getFileContent("/testequipment/11.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/CARD-1.65.1.4", getFileContent("/testequipment/12.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/CARD-1.1.6.0", getFileContent("/testequipment/13.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/CARD-1.1.8.0", getFileContent("/testequipment/14.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/CARD-1.1.9.0", getFileContent("/testequipment/15.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/CARD-1.55.1.4", getFileContent("/testequipment/16.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/a2.module-1.1.1.7", getFileContent("/testequipment/17.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/IDU-1.55.0.0", getFileContent("/testequipment/18.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/CARD-1.1.1.0", getFileContent("/testequipment/19.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/CARD-1.1.5.0", getFileContent("/testequipment/20.json"));
        dbRawProvider.doWriteRaw(Entity.Inventoryequipment.getName(), "sim1/a2.module-1.1.5.6", getFileContent("/testequipment/21.json"));

    }
    /**
     * @param string
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    private static String getFileContent(String filename) throws IOException {
        return String.join("\n",IoUtils.readAllLines(TestTree.class.getResourceAsStream(filename)));
    }

    @Test
    public void testInventoryTree() throws IOException {
        DataTreeProviderImpl provider = new DataTreeProviderImpl();
        provider.setDatabaseClient(dbRawProvider);


        DataTreeObject tree = provider.readInventoryTree(null, null, FilterMode.Lazy);
        System.out.println(tree.toJSON());
        JSONObject o = new JSONObject(tree.toJSON());
        JSONAssert.assertContainsOnlyKey(o, "sim1");
        JSONObject children = o.getJSONObject("sim1").getJSONObject("children");
        this.assertSim1(children);

        tree = provider.readInventoryTree(Arrays.asList("sim1"), "*", FilterMode.Lazy);
        this.assertSim1(new JSONObject(tree.toJSON()));
        System.out.println(tree.toJSON());

    }

    private void assertSim1(JSONObject sim1Children) {
        JSONAssert.assertContainsExactKeys(sim1Children,new String[] {"sim1/ODU-1.56.0.0", "sim1/IDU-1.55.0.0", "sim1/IDU-1.65.0.0", "sim1/SHELF-1.1.0.0"});
        JSONObject c1 = sim1Children.getJSONObject("sim1/ODU-1.56.0.0");
        JSONObject c2 = sim1Children.getJSONObject("sim1/IDU-1.55.0.0");
        JSONObject c3 = sim1Children.getJSONObject("sim1/IDU-1.65.0.0");
        JSONObject c4 = sim1Children.getJSONObject("sim1/SHELF-1.1.0.0");
        JSONAssert.assertContainsExactKeys(c1.getJSONObject("children"),new String[] {"sim1/a2.module-1.56.1.2"});
        JSONAssert.assertContainsExactKeys(c2.getJSONObject("children"),new String[] {"sim1/a2.module-1.55.1.2","sim1/CARD-1.55.1.4"});
        JSONAssert.assertContainsExactKeys(c3.getJSONObject("children"),new String[] {"sim1/a2.module-1.65.1.2","sim1/CARD-1.65.1.4"});
        JSONAssert.assertContainsExactKeys(c4.getJSONObject("children"),new String[] {"sim1/CARD-1.1.1.0",
                "sim1/CARD-1.1.5.0", "sim1/CARD-1.1.7.0","sim1/CARD-1.1.6.0", "sim1/CARD-1.1.9.0","sim1/CARD-1.1.8.0"});
        JSONObject c41 = c4.getJSONObject("children").getJSONObject("sim1/CARD-1.1.1.0");
        JSONObject c42 = c4.getJSONObject("children").getJSONObject("sim1/CARD-1.1.5.0");
        JSONObject c43 = c4.getJSONObject("children").getJSONObject("sim1/CARD-1.1.7.0");
        JSONObject c44 = c4.getJSONObject("children").getJSONObject("sim1/CARD-1.1.6.0");
        JSONObject c45 = c4.getJSONObject("children").getJSONObject("sim1/CARD-1.1.9.0");
        JSONObject c46 = c4.getJSONObject("children").getJSONObject("sim1/CARD-1.1.8.0");
        JSONAssert.assertContainsExactKeys(c41.getJSONObject("children"),new String[] {"sim1/a2.module-1.1.1.7","sim1/a2.module-1.1.1.5","sim1/a2.module-1.1.1.8"});
        JSONAssert.assertContainsExactKeys(c42.getJSONObject("children"),new String[] {"sim1/a2.module-1.1.5.6","sim1/a2.module-1.1.5.5"});
        JSONAssert.assertContainsNoKeys(c43.getJSONObject("children"));
        JSONAssert.assertContainsExactKeys(c44.getJSONObject("children"),new String[] {"sim1/a2.module-1.1.6.5"});
        JSONAssert.assertContainsNoKeys(c45.getJSONObject("children"));
        JSONAssert.assertContainsNoKeys(c46.getJSONObject("children"));
    }
    @Test
    public void testUriConversion() {
        EntityWithTree e = DataTreeHttpServlet.getEntity("/tree/read-inventoryequipment-tree/sim1/sim1%2FODU");
        System.out.println(e);
        e = DataTreeHttpServlet.getEntity("/tree/read-inventoryequipment-tree/");
        System.out.println(e);
    }
}
