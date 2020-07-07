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
package org.onap.ccsdk.features.sdnr.wt.common.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.IsEsObject;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.CreateIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.GetIndexRequest;

public class TestDbClient {

    private static HtDatabaseClient dbClient;
    private static HostInfo[] hosts = new HostInfo[] {new HostInfo("localhost", Integer
            .valueOf(System.getProperty("databaseport") != null ? System.getProperty("databaseport") : "49200"))};

    @BeforeClass
    public static void init() throws Exception {

        dbClient = new HtDatabaseClient(hosts);
        dbClient.waitForYellowStatus(20000);

    }

    @Test
    public void testCRUD() {
        final String IDX = "test23-knmoinsd";
        final String ID = "abcddd";
        final String JSON = "{\"data\":{\"inner\":\"more\"}}";
        final String JSON2 = "{\"data\":{\"inner\":\"more2\"}}";

        try {
            if (!dbClient.indicesExists(new GetIndexRequest(IDX))) {
                dbClient.createIndex(new CreateIndexRequest(IDX));
            }
        } catch (IOException e) {
            fail("unable to create index");
        }
        clearIndexData(IDX);
        //Create
        String esId = dbClient.doWriteRaw(IDX, ID, JSON);
        assertEquals("inserted id is wrong", ID, esId);
        //Read
        SearchResult<SearchHit> result = dbClient.doReadByQueryJsonData(IDX, QueryBuilders.matchQuery("_id", ID));
        assertEquals("amount of results is wrong", 1, result.getTotal());
        assertEquals("data not valid", JSON, result.getHits().get(0).getSourceAsString());
        //Update
        esId = dbClient.doUpdateOrCreate(IDX, ID, JSON2);
        assertEquals("update response not successfull", ID, esId);
        //check that update with null fails
        assertNull("update with id null should not be possible", dbClient.doUpdateOrCreate(IDX, null, JSON2));
        //Verify update
        result = dbClient.doReadByQueryJsonData(IDX, QueryBuilders.matchQuery("_id", ID));
        assertEquals("amount of results is wrong", 1, result.getTotal());
        assertEquals("data not valid", JSON2, result.getHits().get(0).getSourceAsString());
        //test second read
        String resStr = dbClient.doReadJsonData(IDX, new IsEsObject() {

            @Override
            public void setEsId(String id) {

            }

            @Override
            public String getEsId() {
                return ID;
            }
        });
        //test all read
        result = dbClient.doReadAllJsonData(IDX);
        assertNotNull("all read not working", result);

        assertEquals("read works not as expected", JSON2, resStr);
        //Delete
        boolean del = dbClient.doRemove(IDX, new IsEsObject() {

            @Override
            public void setEsId(String id) {

            }

            @Override
            public String getEsId() {
                return ID;
            }
        });
        assertTrue("item not deleted", del);
        //Verify
        result = dbClient.doReadByQueryJsonData(IDX, QueryBuilders.matchQuery("_id", ID));
        assertEquals("amount of results is wrong", 0, result.getTotal());

    }

    /**
     * @param iDX
     */
    private void clearIndexData(String idx) {
        try {
            dbClient.deleteByQuery(new DeleteByQueryRequest(idx, true).source(QueryBuilders.matchAllQuery()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testCRUD2() {
        final String IDX = "test23-knmoins3d";
        final String ID = "abcddd";
        final String JSON = "{\"data\":{\"inner\":\"more\"}}";
        final String JSON2 = "{\"data\":{\"inner\":\"more2\"}}";
        try {
            if (!dbClient.indicesExists(new GetIndexRequest(IDX))) {
                dbClient.createIndex(new CreateIndexRequest(IDX));
            }
        } catch (IOException e) {
            fail("unable to create index");
        }
        //Create
        String esId = dbClient.doWriteRaw(IDX, ID, JSON);
        assertEquals("inserted id is wrong", ID, esId);
        //Read
        SearchResult<SearchHit> result = dbClient.doReadByQueryJsonData(IDX, QueryBuilders.matchQuery("_id", ID));
        assertEquals("amount of results is wrong", 1, result.getTotal());
        assertEquals("data not valid", JSON, result.getHits().get(0).getSourceAsString());
        QueryBuilder matchQuery = QueryBuilders.matchQuery("_id", ID);
        //Update
        assertTrue("update response not successfull", dbClient.doUpdate(IDX, JSON2, matchQuery));
        //check that update with null fails
        assertNull("update with id null should not be possible", dbClient.doUpdateOrCreate(IDX, null, JSON2));
        //Verify update
        result = dbClient.doReadByQueryJsonData(IDX, QueryBuilders.matchQuery("_id", ID));
        assertEquals("amount of results is wrong", 1, result.getTotal());
        assertEquals("data not valid", JSON2, result.getHits().get(0).getSourceAsString());
        //test second read
        String resStr = dbClient.doReadJsonData(IDX, new IsEsObject() {

            @Override
            public void setEsId(String id) {

            }

            @Override
            public String getEsId() {
                return ID;
            }
        });
        //test all read
        result = dbClient.doReadAllJsonData(IDX);
        assertNotNull("all read not working", result);

        assertEquals("read works not as expected", JSON2, resStr);
        //Delete
        int del = dbClient.doRemove(IDX, matchQuery);
        assertTrue("item not deleted", del > 0);
        //Verify
        result = dbClient.doReadByQueryJsonData(IDX, QueryBuilders.matchQuery("_id", ID));
        assertEquals("amount of results is wrong", 0, result.getTotal());

    }

}
