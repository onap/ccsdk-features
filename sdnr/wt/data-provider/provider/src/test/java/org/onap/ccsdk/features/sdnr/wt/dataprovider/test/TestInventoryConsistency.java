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

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.DatabaseDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.impl.ElasticSearchDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.test.util.HostInfoForTest;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;

public class TestInventoryConsistency {

    private static final String TEST1NODEID = "sim1";
    private static DatabaseDataProvider dbProvider;

    @BeforeClass
    public static void init() throws Exception {
        HostInfo[] hosts = HostInfoForTest.get();
        dbProvider = new ElasticSearchDataProvider(hosts);
        dbProvider.waitForYellowDatabaseStatus(30, TimeUnit.SECONDS);
    }

    @Test
    public void test1() {
        YangToolsMapper mapper = new YangToolsMapper();
        try {
            SearchHit[] hits = TestTree.loadEntries("test1.json");
            List<Inventory> inventoryList = new ArrayList<>();
            for(SearchHit hit:hits) {
                inventoryList.add(mapper.readValue(hit.getSourceAsString(), Inventory.class));
            }
            dbProvider.getDataProvider().writeInventory(TEST1NODEID, inventoryList);
        } catch (IOException e) {

            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
