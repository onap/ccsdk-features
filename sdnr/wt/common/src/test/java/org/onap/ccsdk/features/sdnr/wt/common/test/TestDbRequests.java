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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.ClusterHealthRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.ClusterSettingsRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.CreateAliasRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.CreateIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteAliasRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.GetIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.GetRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.IndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.NodeStatsRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.RefreshIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.SearchRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.UpdateByQueryRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.UpdateRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.ClusterHealthResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.ClusterSettingsResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.CreateAliasResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.CreateIndexResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.DeleteByQueryResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.DeleteIndexResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.DeleteResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.GetResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.ListIndicesResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.NodeStatsResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.SearchResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.UpdateByQueryResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.UpdateResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class TestDbRequests {

    private static HtDatabaseClient dbClient;
    private static HostInfo[] hosts = new HostInfo[] {new HostInfo("localhost", Integer
            .valueOf(System.getProperty("databaseport") != null ? System.getProperty("databaseport") : "49200"))};

    @BeforeClass
    public static void init() throws Exception {

        dbClient = new HtDatabaseClient(hosts);

    }

    @AfterClass
    public static void deinit() {
        if (dbClient != null) {
            dbClient.close();
        }
    }

    @Test
    public void testHealth() {

        ClusterHealthResponse response = null;
        ClusterHealthRequest request = new ClusterHealthRequest();
        request.timeout(10);
        try {
            response = dbClient.health(request);
        } catch (UnsupportedOperationException | IOException | JSONException e) {
            fail(e.getMessage());
        }
        assertNotNull("response is null", response);
        assertTrue(response.isStatusMinimal(ClusterHealthResponse.HEALTHSTATUS_YELLOW));
    }

    @Test
    public void testCount() {

    }

    @Test
    public void testIndexAndAliasList() {
        final String ALIAS = "asdoi32kmsasd";
        final String IDX = ALIAS + "-v1";
        CreateIndexRequest request = new CreateIndexRequest(IDX);
        CreateIndexResponse response = null;
        try {
            response = dbClient.createIndex(request);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response);

        CreateAliasRequest request3 = new CreateAliasRequest(IDX, ALIAS);
        CreateAliasResponse response3 = null;
        try {
            response3 = dbClient.createAlias(request3);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response3);
        assertTrue(response3.isResponseSucceeded());

        assertTrue("index not existing", dbClient.isExistsIndex(IDX));
        ListIndicesResponse response2 = null;
        try {
            response2 = dbClient.getIndices();
        } catch (ParseException | IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response2);
        assertNotNull(response2.getEntries());
        assertTrue(response2.getEntries().size() > 0);

        DeleteIndexRequest request11 = new DeleteIndexRequest(IDX);

        DeleteIndexResponse response11 = null;
        try {
            response11 = dbClient.deleteIndex(request11);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response11);
        assertFalse("index still existing", dbClient.isExistsIndex(IDX));
        this.deleteAlias(IDX, ALIAS);
        this.deleteIndex(IDX);
    }

    @Test
    public void testCreateAndDeleteIndex() {
        final String IDX = "testcidx1";
        CreateIndexRequest request = new CreateIndexRequest(IDX);
        CreateIndexResponse response = null;
        try {
            response = dbClient.createIndex(request);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response);

        assertTrue("index not existing", dbClient.isExistsIndex(IDX));

        DeleteIndexRequest request2 = new DeleteIndexRequest(IDX);

        DeleteIndexResponse response2 = null;
        try {
            response2 = dbClient.deleteIndex(request2);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response2);
        assertFalse("index still existing", dbClient.isExistsIndex(IDX));
        this.deleteIndex(IDX);
    }

    @Test
    public void testInsertAndDelete() {
        final String IDX = "tesnt23-knmoinsd";
        final String ID = "abcddd";
        final String JSON = "{\"data\":{\"inner\":\"more\"}}";
        try {
            if (!dbClient.indicesExists(new GetIndexRequest(IDX))) {
                dbClient.createIndex(new CreateIndexRequest(IDX).mappings(defaultMappings(IDX, false)));
            }
        } catch (IOException e) {
            fail("unable to create index");
        }
        this.insert(IDX, ID, JSON);
        // delete data
        DeleteRequest request2 = new DeleteRequest(IDX, IDX, ID);
        DeleteResponse response2 = null;
        try {
            response2 = dbClient.delete(request2);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response2);
        assertTrue(response2.isDeleted());
        try {
            dbClient.refreshIndex(new RefreshIndexRequest(IDX));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        // verify data deleted
        GetRequest request4 = new GetRequest(IDX, IDX, ID);
        GetResponse response4 = null;
        try {
            response4 = dbClient.get(request4);
        } catch (IOException e1) {
            fail(e1.getMessage());
        }
        assertNotNull(response4);
        assertFalse("data still existing", response4.isExists());
        this.deleteIndex(IDX);
    }

    /**
     * @param b
     * @return
     */
    private JSONObject defaultMappings(String idx, boolean useStrict) {
        String mapping = "{}";
        return new JSONObject(String.format("{\"%s\":{%s\"properties\":%s}}", idx,
                useStrict ? "\"dynamic\": false," : "\"dynamic\": true,", mapping));
    }

    @Test
    public void testInsertAndDeleteByQuery() {
        final String IDX = "test534-knmoinsd";
        final String ID = "abcdddseae";
        final String JSON = "{\"data\":{\"inner\":\"more\"}}";
        try {
            if (!dbClient.indicesExists(new GetIndexRequest(IDX))) {
                dbClient.createIndex(new CreateIndexRequest(IDX));
            }
        } catch (IOException e) {
            fail("unable to create index");
        }
        this.insert(IDX, ID, JSON);

        // delete data
        DeleteByQueryRequest request2 = new DeleteByQueryRequest(IDX);
        request2.source(QueryBuilders.matchQuery("_id", ID));
        DeleteByQueryResponse response2 = null;
        try {
            response2 = dbClient.deleteByQuery(request2);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response2);
        assertTrue(response2.isResponseSucceeded());
        try {
            dbClient.refreshIndex(new RefreshIndexRequest(IDX));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        // verify data deleted
        GetRequest request4 = new GetRequest(IDX, IDX, ID);
        GetResponse response4 = null;
        try {
            response4 = dbClient.get(request4);
        } catch (IOException e1) {
            fail(e1.getMessage());
        }
        assertNotNull(response4);
        assertFalse("data still existing", response4.isExists());
        this.deleteIndex(IDX);
    }

    private void insert(String IDX, String ID, String JSON) {

        // create data
        IndexRequest request = new IndexRequest(IDX, IDX, ID);
        request.source(JSON);
        String responseId = null;
        responseId = dbClient.doWriteRaw(IDX, ID, JSON);
        assertNotNull(responseId);
        if (ID != null) {
            assertEquals("id not correct", ID, responseId);
        } else {
            ID = responseId;
        }
        // do db refresh
        try {
            dbClient.refreshIndex(new RefreshIndexRequest(IDX));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        // verify data exists
        String response3 = null;
        response3 = dbClient.doReadJsonData(IDX, ID);
        assertNotNull(response3);
        JSONAssert.assertEquals("could not verify update", JSON, response3, true);
    }

    @Test
    public void testSearch() {
        final String IDX = "testb44-moinsd";
        final String ID = "abe";
        final String JSON = "{\"data\":{\"inner\":\"more\"}}";
        final String ID2 = "abe2";
        final String JSON2 = "{\"data\":{\"inner\":\"more2\"}}";
        final String ID3 = "abe3";
        final String JSON3 = "{\"data\":{\"inner\":\"more3\"}}";
        try {
            if (!dbClient.indicesExists(new GetIndexRequest(IDX))) {
                dbClient.createIndex(new CreateIndexRequest(IDX));
            }
        } catch (IOException e) {
            fail("unable to create index");
        }
        this.insert(IDX, ID, JSON);
        this.insert(IDX, ID2, JSON2);
        this.insert(IDX, ID3, JSON3);
        SearchRequest request = new SearchRequest(IDX, IDX);
        request.setQuery(QueryBuilders.matchAllQuery());
        SearchResponse response = null;
        try {
            response = dbClient.search(request);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response);
        assertEquals("not all items found", 3, response.getHits().length);
        assertEquals("incorrect index", IDX, response.getHits()[0].getIndex());
        assertEquals("incorrect type", IDX, response.getHits()[0].getType());
        this.deleteIndex(IDX);
    }

    @Test
    public void testUpdate() {
        final String IDX = "test45134-moinsd";
        final String ID = "assbe";
        final String JSON = "{\"data\":{\"inner\":\"more\"}}";
        final String JSON2 = "{\"data\":{\"inner\":\"more2\"},\"data2\":\"value2\",\"data3\":true}";
        try {
            if (!dbClient.indicesExists(new GetIndexRequest(IDX))) {
                dbClient.createIndex(new CreateIndexRequest(IDX));
            }
        } catch (IOException e) {
            fail("unable to create index");
        }
        this.insert(IDX, ID, JSON);
        UpdateRequest request = new UpdateRequest(IDX, IDX, ID);
        UpdateResponse response = null;
        try {
            request.source(new JSONObject(JSON2));
            response = dbClient.update(request);
        } catch (JSONException | IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response);
        assertTrue(response.succeeded());
        // refresh index
        try {
            dbClient.refreshIndex(new RefreshIndexRequest(IDX));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        // verify update
        GetRequest request3 = new GetRequest(IDX, IDX, ID);
        GetResponse response3 = null;
        try {
            response3 = dbClient.get(request3);
        } catch (IOException e1) {
            fail(e1.getMessage());
        }
        assertNotNull(response3);
        JSONAssert.assertEquals("could not verify update", JSON2, response3.getSourceAsBytesRef(), true);
        this.deleteIndex(IDX);
    }

    @Test
    public void testUpdateByQuery() {
        final String IDX = "test224534k-moinsd";
        final String ID = "asssabe";
        final String JSON = "{\"data\":{\"inner\":\"more\"}}";
        final String JSON2 = "{\"data\":{\"inner\":\"more2\"},\"data2\":\"value2\",\"data3\":true}";
        try {
            if (!dbClient.indicesExists(new GetIndexRequest(IDX))) {
                dbClient.createIndex(new CreateIndexRequest(IDX));
            }
        } catch (IOException e) {
            fail("unable to create index");
        }
        this.insert(IDX, ID, JSON);
        UpdateByQueryRequest request = new UpdateByQueryRequest(IDX, IDX);
        UpdateByQueryResponse response = null;
        try {
            request.source(ID, new JSONObject(JSON2));
            response = dbClient.update(request);
        } catch (JSONException | IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response);
        assertTrue(response.isUpdated());
        // refresh index
        try {
            dbClient.refreshIndex(new RefreshIndexRequest(IDX));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        // verify update
        GetRequest request3 = new GetRequest(IDX, IDX, ID);
        GetResponse response3 = null;
        try {
            response3 = dbClient.get(request3);
        } catch (IOException e1) {
            fail(e1.getMessage());
        }
        assertNotNull(response3);
        JSONAssert.assertEquals("could not verify update", JSON2, response3.getSourceAsBytesRef(), true);
        this.deleteIndex(IDX);
    }

    @Test
    public void testAggregations() {
        final String IDX = "test3227533677-moisnsd";
        final String JSON = "{ \"node-id\":\"sim1\",\"severity\":\"critical\"}";
        final String JSON2 = "{ \"node-id\":\"sim2\",\"severity\":\"critical\"}";
        final String JSON3 = "{ \"node-id\":\"sim3\",\"severity\":\"minor\"}";
        final String JSON4 = "{ \"node-id\":\"sim4\",\"severity\":\"warning\"}";
        final String JSON5 = "{ \"node-id\":\"sim5\",\"severity\":\"major\"}";
        final String MAPPINGS = String.format("{\"" + IDX + "\":{\"properties\":%s}}",
                "{\"node-id\":{\"type\": \"keyword\"},\"severity\": {\"type\": \"keyword\"}}");
        // create index with mapping keyword
        CreateIndexResponse iresponse = null;
        try {
            if (!dbClient.isExistsIndex(IDX)) {
                iresponse = dbClient.createIndex(new CreateIndexRequest(IDX).mappings(new JSONObject(MAPPINGS)));
                assertNotNull(iresponse);
                assertTrue(iresponse.isAcknowledged());
            }
        } catch (IOException e1) {
            this.deleteIndex(IDX);
            fail("unable to create index: " + e1.getMessage());
        }

        // fill index
        this.insert(IDX, null, JSON);
        this.insert(IDX, null, JSON2);
        this.insert(IDX, null, JSON3);
        this.insert(IDX, null, JSON4);
        this.insert(IDX, null, JSON5);
        // refresh index
        try {
            dbClient.refreshIndex(new RefreshIndexRequest(IDX));
        } catch (IOException e) {
            fail(e.getMessage());
        }

        SearchRequest request = new SearchRequest(IDX, IDX);
        request.setQuery(QueryBuilders.matchAllQuery().aggregations("severity").size(0));
        SearchResponse response = null;
        try {
            response = dbClient.search(request);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response);
        assertTrue(response.hasAggregations());
        assertEquals("aggregation size not correct", 4, response.getAggregations("severity").size());

        List<String> items1 = Arrays.asList(response.getAggregations("severity").getKeysAsPagedStringList(2, 0));
        List<String> items2 = Arrays.asList(response.getAggregations("severity").getKeysAsPagedStringList(2, 2));
        assertEquals("pagination does not work", 2, items1.size());
        assertEquals("pagination does not work", 2, items2.size());
        for (String s : items1) {
            assertFalse("pagination overlap is not allowed", items2.contains(s));
        }
        for (String s : items2) {
            assertFalse("pagination overlap is not allowed", items1.contains(s));
        }

        this.deleteIndex(IDX);
    }

    @Test
    public void testStatistics() {
        NodeStatsResponse stats = null;
        try {
            stats = dbClient.stats(new NodeStatsRequest());
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(stats);
        System.out.println(stats.getNodesInfo());
        System.out.println(stats.getNodeStatistics());
    }

    // @Test
    public void testPreventAutoCreateIndex() {
        final String IDX1 = "acidx1";
        final String ID1 = "acid1";
        final String IDX2 = "acidx2";
        final String ID2 = "acid2";
        final String OBJ = "{\"test\":5}";

        ClusterSettingsResponse settingsResponse = null;
        String esId = null;
        // set setting to allow autocreate
        try {
            settingsResponse = dbClient.setupClusterSettings(new ClusterSettingsRequest(true));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(settingsResponse);
        assertTrue(settingsResponse.isAcknowledged());
        // test if something new can be created
        esId = dbClient.doWriteRaw(IDX1, IDX1, ID1, OBJ);
        assertEquals(ID1, esId);
        // set setting to deny autocreate
        try {
            settingsResponse = dbClient.setupClusterSettings(new ClusterSettingsRequest(false));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(settingsResponse);
        assertTrue(settingsResponse.isAcknowledged());
        // test if something new cannot be created
        esId = dbClient.doWriteRaw(IDX2, IDX2, ID2, OBJ);
        assertNull(esId);
        // set setting to allow autocreate
        try {
            settingsResponse = dbClient.setupClusterSettings(new ClusterSettingsRequest(true));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(settingsResponse);
        assertTrue(settingsResponse.isAcknowledged());

    }

    private void deleteAlias(String idx, String alias) {
        try {
            dbClient.deleteAlias(new DeleteAliasRequest(idx, alias));
        } catch (IOException e) {

        }
    }

    private void deleteIndex(String idx) {
        try {
            dbClient.deleteIndex(new DeleteIndexRequest(idx));
        } catch (IOException e) {

        }
    }

}
