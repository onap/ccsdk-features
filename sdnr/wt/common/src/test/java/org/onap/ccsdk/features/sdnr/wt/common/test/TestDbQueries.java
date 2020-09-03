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

import static org.junit.Assert.fail;

import org.json.JSONException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DbFilter;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.BoolQueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.SortOrder;


public class TestDbQueries {

    // @formatter:off 
    private static final String MATCH_ALL_QUERY =
            "{\n"
            + "    \"query\": {\n"
            + "        \"match_all\" : {\n"
            + "        }\n"
            + "    }\n"
            + "}";
    private static final String MATCH_QUERY_KEY = "is-required";
    private static final Object MATCH_QUERY_VALUE = true;
    private static final String MATCH_QUERY = "{\n"
            + "    \"query\": {\n"
            + "        \"match\" : {\n"
            + "            \""
            + MATCH_QUERY_KEY + "\" : "
            + MATCH_QUERY_VALUE + "\n"
            + "        }\n"
            + "    }\n"
            + "}";
    private static final String MATCH_QUERY_KEY2 = "node-id";
    private static final Object MATCH_QUERY_VALUE2 = "sim2";
    private static final String BOOL_QUERY_MUST =
            "{\n"
            + "    \"query\": {\n"
            + "        \"bool\": {\n"
            + "            \"must\": [\n"
            + "                {\n"
                    + "                    \"match\": {\n"
            + "                        \""
            + MATCH_QUERY_KEY + "\": "
                    + MATCH_QUERY_VALUE + "\n"
            + "                    }\n"
            + "                },\n"
                    + "                {\n"
            + "                    \"match\": {\n"
            + "                        \""
                    + MATCH_QUERY_KEY2 + "\":"
            + MATCH_QUERY_VALUE2 + " \n"
            + "                    }\n"
                    + "                }\n"
            + "            ]\n"
            + "        }\n"
            + "    }\n"
            + "}";
    private static final String BOOL_QUERY_MUST_SINGLE = "{\n"
            + "    \"query\": {\n"
            + "        \"bool\": {\n"
            + "            \"must\": {\n"
            + "                    \"match\": {\n"
            + "                        \""
            + MATCH_QUERY_KEY + "\": "
            + MATCH_QUERY_VALUE + "\n"
            + "                    }\n"
            + "                }\n"
            + "        }\n"
            + "    }\n"
            + "}";
    private static final String BOOL_QUERY_SHOULD = "{\n"
            + "    \"query\": {\n"
            + "        \"bool\": {\n"
            + "            \"should\": [\n"
            + "                {\n"
            + "                    \"match\": {\n"
            + "                        \""
            + MATCH_QUERY_KEY + "\": "
            + MATCH_QUERY_VALUE + "\n"
            + "                    }\n"
            + "                },\n"
            + "                {\n"
            + "                    \"match\": {\n"
            + "                        \""
            + MATCH_QUERY_KEY2 + "\":"
            + MATCH_QUERY_VALUE2 + " \n"
            + "                    }\n"
            + "                }\n"
            + "            ]\n"
            + "        }\n"
            + "    }\n"
            + "}";
    private static final String BOOL_QUERY_SHOULD_SINGLE = "{\n"
            + "    \"query\": {\n"
            + "        \"bool\": {\n"
            + "            \"should\": {\n"
            + "                    \"match\": {\n"
            + "                        \""
            + MATCH_QUERY_KEY + "\": "
            + MATCH_QUERY_VALUE + "\n"
            + "                    }\n"
            + "                }\n"
            + "        }\n"
            + "    }\n"
            + "}";
    private static final String RANGE_QUERY_KEY = "timestamp";
    private static final String RANGE_QUERY_LTEND = "2017-08-10T20:00:00.0Z";
    private static final String RANGE_QUERY = "{\n"
            + "    \"query\": {\n"
            + "        \"range\" : {\n"
            + "            \"" + RANGE_QUERY_KEY + "\" : {\n"
            + "                \"lte\" : \"" + RANGE_QUERY_LTEND + "\",\n"
            + "                \"boost\": 2.0\n"
            + "            }\n"
            + "        }\n"
            + "    }\n"
            + "}";
    private static final String RANGEBOOL_QUERY = "{\n"
            + "    \"query\": {\n"
            + "        \"bool\": {\n"
            + "            \"must\": [\n"
            + "                {\n"
            + "                    \"match\": {\n"
            + "                        \"is-required\": true\n"
            + "                    }\n"
            + "                },\n"
            + "                {\n"
            + "                    \"regexp\": {\n"
            + "                        \"node-id\": {\n"
            + "                            \"max_determinized_states\": 10000,\n"
            + "                            \"flags\": \"ALL\",\n"
            + "                            \"value\": \"sim.*\"\n"
            + "                        }\n"
            + "                    }\n"
            + "                }\n"
            + "            ]\n"
            + "        }\n"
            + "    }\n"
            + "}";
    private static final String AGG_FIELD = "severity";
    private static final String AGG_QUERY =
            "{\n"
            + "    \"query\": {\n"
            + "        \"match_all\": {}\n"
            + "    },\n"
            + "    \"aggs\": {\n"
            + "        \"severity\": {\n"
            + "            \"terms\": {\n"
            + "                \"field\": \"" + AGG_FIELD + "\"\n"
            + "            }\n"
            + "        }\n"
            + "    }\n"
            + "}";
    private static final long FROMANDSIZE_QUERY_SIZE = 20;
    private static final long FROMANDSIZE_QUERY_FROM = 120;
    private static final String FROMANDSIZE_QUERY = "{\n"
            + "    \"size\": " + FROMANDSIZE_QUERY_SIZE + ",\n"
            + "    \"query\": {\n"
            + "        \"match_all\": {}\n"
            + "    },\n"
            + "    \"from\":" + FROMANDSIZE_QUERY_FROM + "\n"
            + "}";
    private static final String TERMQUERY_KEY = "node-id";
    private static final String TERMQUERY_VALUE = "abc";
    private static final String TERM_QUERY = "{\n"
            + "    \"query\": {\n"
            + "        \"term\": {\n"
            + "            \"" + TERMQUERY_KEY + "\": \"" + TERMQUERY_VALUE + "\"\n"
            + "        }\n"
            + "    }\n"
            + "}";
    private static final String SORTING_PROPERTY = "node-id";
    private static final String SORTING_QUERY_ASC = "{\n"
            + "    \"query\": {\n"
            + "        \"match_all\": {}\n"
            + "    },\n"
            + "    \"sort\": [\n"
            + "        {\n"
            + "            \"" + SORTING_PROPERTY + "\": {\n"
            + "                \"order\": \"asc\"\n"
            + "            }\n"
            + "        }\n"
            + "    ]\n"
            + "}";
    private static final String SORTING_QUERY_DESC = "{\n"
            + "    \"query\": {\n"
            + "        \"match_all\": {}\n"
            + "    },\n"
            + "    \"sort\": [\n"
            + "        {\n"
            + "            \"" + SORTING_PROPERTY + "\": {\n"
            + "                \"order\": \"desc\"\n"
            + "            }\n"
            + "        }\n"
            + "    ]\n"
            + "}";
    // @formatter:on
    private void testEquals(String message, String json, QueryBuilder query) {
        this.testEquals(message, json, query, true);
    }

    private void testEquals(String message, String json, QueryBuilder query, boolean strict) {

        try {
            System.out.println("===test if " + message + "===================");
            System.out.println("orig  : " + trim(json));
            System.out.println("totest: " + query.toJSON().trim());
            JSONAssert.assertEquals(json, query.toJSON(), strict);
        } catch (JSONException e) {
            fail(message);
        }
    }

    private String trim(String json) {
        return json.trim().replaceAll("\n", "").replaceAll(" ", "");
    }

    @Test
    public void testMatchAll() {
        testEquals("match all query is wrong", MATCH_ALL_QUERY, QueryBuilders.matchAllQuery());
    }

    @Test
    public void testMatch() {
        testEquals("match query is wrong", MATCH_QUERY, QueryBuilders.matchQuery(MATCH_QUERY_KEY, MATCH_QUERY_VALUE));
    }

    @Test
    public void testBoolMust() {
        testEquals("bool query is wrong", BOOL_QUERY_MUST,
                QueryBuilders.boolQuery().must(QueryBuilders.matchQuery(MATCH_QUERY_KEY, MATCH_QUERY_VALUE))
                        .must(QueryBuilders.matchQuery(MATCH_QUERY_KEY2, MATCH_QUERY_VALUE2)));
    }

    @Test
    public void testBoolMustSingle() {
        testEquals("bool single query is wrong", BOOL_QUERY_MUST_SINGLE,
                QueryBuilders.boolQuery().must(QueryBuilders.matchQuery(MATCH_QUERY_KEY, MATCH_QUERY_VALUE)));
    }

    @Test
    public void testBoolShould() {
        testEquals("bool query is wrong", BOOL_QUERY_SHOULD,
                QueryBuilders.boolQuery().should(QueryBuilders.matchQuery(MATCH_QUERY_KEY, MATCH_QUERY_VALUE))
                        .should(QueryBuilders.matchQuery(MATCH_QUERY_KEY2, MATCH_QUERY_VALUE2)));
    }

    @Test
    public void testBoolShouldSingle() {
        testEquals("bool single query is wrong", BOOL_QUERY_SHOULD_SINGLE,
                QueryBuilders.boolQuery().should(QueryBuilders.matchQuery(MATCH_QUERY_KEY, MATCH_QUERY_VALUE)));
    }

    @Test
    public void testRange() {
        testEquals("range query is wrong", RANGE_QUERY,
                QueryBuilders.rangeQuery(RANGE_QUERY_KEY).lte(RANGE_QUERY_LTEND));

    }

    @Test
    public void testAggregation() {
        testEquals("aggregation query is wrong", AGG_QUERY, QueryBuilders.matchAllQuery().aggregations(AGG_FIELD));
    }

    @Test
    public void testSizeAndFrom() {
        testEquals("aggregation query is wrong", FROMANDSIZE_QUERY,
                QueryBuilders.matchAllQuery().size(FROMANDSIZE_QUERY_SIZE).from(FROMANDSIZE_QUERY_FROM));
    }

    @Test
    public void testRegex() {
        testEquals("range and bool query is wrong1", RANGEBOOL_QUERY,
                QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("is-required", true))
                        .must(QueryBuilders.regex("node-id", DbFilter.createDatabaseRegex("sim*"))),
                false);
        BoolQueryBuilder q =
                QueryBuilders.boolQuery().must(QueryBuilders.regex("node-id", DbFilter.createDatabaseRegex("sim*")));
        q.must(QueryBuilders.matchQuery("is-required", true));
        testEquals("range and bool query is wrong2", RANGEBOOL_QUERY, q, false);
    }

    @Test
    public void testTerm() {
        testEquals("term query is wrong", TERM_QUERY, QueryBuilders.termQuery(TERMQUERY_KEY, TERMQUERY_VALUE));
    }

    @Test
    public void testSorting() {
        testEquals("sortorder is wrong", SORTING_QUERY_ASC,
                QueryBuilders.matchAllQuery().sort(SORTING_PROPERTY, SortOrder.ASCENDING));
        testEquals("sortorder is wrong", SORTING_QUERY_DESC,
                QueryBuilders.matchAllQuery().sort(SORTING_PROPERTY, SortOrder.DESCENDING));
    }
}
