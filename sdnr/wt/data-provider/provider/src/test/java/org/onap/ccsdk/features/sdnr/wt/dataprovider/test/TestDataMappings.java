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

import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.DatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.SearchResponse;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.EsDataObjectReaderWriter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.YangToolsMapper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.pmdata._15m.list.output.Data;
import org.opendaylight.yangtools.yang.binding.DataObject;

public class TestDataMappings {

    private static final HostInfo[] HOSTINFOS = new HostInfo[] {HostInfo.getDefault()};
    //public static final String ESDATATYPENAME = "faultcurrent";

    private static class HtDatabaseClientHelper extends HtDatabaseClient {

        private final String json;

        public HtDatabaseClientHelper(String jsonResponse, HostInfo[] hosts) throws Exception {
            super(hosts);
            this.json = jsonResponse;

        }

        @Override
        public SearchResult<SearchHit> doReadByQueryJsonData(String dataTypeName, QueryBuilder qb) {
            return new SearchResult<>(new SearchResponse(this.json).getHits());
        }
    }

    private static class MapResult<T extends DataObject> {
        public final List<T> mappedData;

        public MapResult(String dataType, Class<T> cls, String dbJson) throws Exception {
            System.out.println(dbJson);
            DatabaseClient db = new HtDatabaseClientHelper(dbJson, HOSTINFOS);
            EsDataObjectReaderWriter<T> dbrw = new EsDataObjectReaderWriter<>(db, dataType, cls);
            this.mappedData = dbrw.doReadAll().getHits();
        }
    }

    private static final String SEARCHJSON_FORMAT = "{\n" + "\"took\": 0,\n" + "\"timed_out\": false,\n"
            + "\"_shards\": {\n" + "\"total\": 5,\n" + "\"successful\": 5,\n" + "\"skipped\": 0,\n" + "\"failed\": 0\n"
            + "},\n" + "\"hits\": {\n" + "\"total\": 1,\n" + "\"max_score\": 1,\n" + "\"hits\": [\n" + "{\n"
            + "\"_index\": \"%s\",\n" + "\"_type\": \"%s\",\n" + "\"_id\": \"%s\",\n" + "\"_score\": 1,\n"
            + "\"_source\": %s}\n" + "]\n" + "}\n" + "}";

    private static final String MEDIATORSERVER_DB_ID = "LumwSG0BFvcE3yf8MBM5";
    private static final String MEDIATOR_SERVERDB_JSON =
            "{\"url\":\"https://10.45.44.223:7590\",\"name\":\"test mediator server\"}";

    private static final String FAULTCURRENT_DB_ID = "LumwSG0BFvcE3yf8MBM5";
    private static final String FAULTCURRENT_DB_NODEID = "sim1";
    private static final int FAULTCURRENT_DB_COUNTER = 3;
    private static final String FAULTCURRENT_DB_OBJECTID = "LPS-MWT-01";
    private static final String FAULTCURRENT_DB_PROBLEM = "rlsExceeded";
    private static final String FAULTCURRENT_DB_SEVERITY = "critical";
    private static final DateAndTime FAULTCURRENT_DB_TIMESTAMP =
            DateAndTime.getDefaultInstance("2019-09-18T13:07:05.8Z");

    private static final String FAULTCURRENT_SERVERDB_JSON = "{\"node-id\":\"" + FAULTCURRENT_DB_NODEID + "\","
            + "\"counter\":" + FAULTCURRENT_DB_COUNTER + "," + "\"object-id\":\"" + FAULTCURRENT_DB_OBJECTID + "\","
            + "\"problem\":\"" + FAULTCURRENT_DB_PROBLEM + "\"," + "\"timestamp\":\""
            + FAULTCURRENT_DB_TIMESTAMP.getValue() + "\"," + "\"severity\":\"" + FAULTCURRENT_DB_SEVERITY + "\"" + "}";


    private static final String PMDATA15M_SERVERDB_JSON = "{\n" + "\"node-name\": \"sim2\",\n"
            + "\"uuid-interface\": \"LP-MWPS-TTP-01\",\n" + "\"layer-protocol-name\": \"MWPS\",\n"
            + "\"radio-signal-id\": \"Test11\",\n" + "\"time-stamp\": \"2017-07-04T14:00:00.0Z\",\n"
            + "\"granularity-period\": \"PERIOD_15MIN\",\n" + "\"scanner-id\": \"PM_RADIO_15M_9\",\n"
            + "\"performance-data\": {\n" + "\"es\": 0,\n" + "\"rx-level-avg\": -41,\n" + "\"time2-states\": -1,\n"
            + "\"time4-states-s\": 0,\n" + "\"time4-states\": 0,\n" + "\"time8-states\": 0,\n"
            + "\"time16-states-s\": -1,\n" + "\"time16-states\": 0,\n" + "\"time32-states\": 0,\n"
            + "\"time64-states\": 0,\n" + "\"time128-states\": 0,\n" + "\"time256-states\": 900,\n"
            + "\"time512-states\": -1,\n" + "\"time512-states-l\": -1,\n" + "\"time1024-states\": -1,\n"
            + "\"time1024-states-l\": -1,\n" + "\"time2048-states\": -1,\n" + "\"time2048-states-l\": -1,\n"
            + "\"time4096-states\": -1,\n" + "\"time4096-states-l\": -1,\n" + "\"time8192-states\": -1,\n"
            + "\"time8192-states-l\": -1,\n" + "\"snir-min\": -99,\n" + "\"snir-max\": -99,\n" + "\"snir-avg\": -99,\n"
            + "\"xpd-min\": -99,\n" + "\"xpd-max\": -99,\n" + "\"xpd-avg\": -99,\n" + "\"rf-temp-min\": -99,\n"
            + "\"rf-temp-max\": -99,\n" + "\"rf-temp-avg\": -99,\n" + "\"defect-blocks-sum\": -1,\n"
            + "\"time-period\": 900,\n" + "\"tx-level-min\": 25,\n" + "\"tx-level-max\": 25,\n"
            + "\"tx-level-avg\": 25,\n" + "\"rx-level-min\": -41,\n" + "\"rx-level-max\": -41,\n"
            + "\"unavailability\": 0,\n" + "\"ses\": 0,\n" + "\"cses\": 0\n" + "},\n"
            + "\"suspect-interval-flag\": false\n" + "}";

    //@Test
    //	public void testMediatorServer() throws ClassNotFoundException {
    //
    //		MapResult<EsMediatorServer> result = new MapResult<EsMediatorServer>(EsMediatorServer.ESDATATYPENAME,
    //				EsMediatorServer.class,
    //				getSearchJson(EsMediatorServer.ESDATATYPENAME,MEDIATORSERVER_DB_ID,MEDIATOR_SERVERDB_JSON));
    //		assertEquals("test mediator server", result.mappedData.get(0).getName());
    //		assertEquals("https://10.45.44.223:7590", result.mappedData.get(0).getUrl());
    //		assertEquals(MEDIATORSERVER_DB_ID, result.mappedData.get(0).getId());
    //
    //	}
    //@Test
    //	public void testFaultCurrent() {
    //
    //		MapResult<FaultcurrentEntity> result = new MapResult<FaultcurrentEntity>(ESDATATYPENAME, EsFaultCurrent.class,
    //				getSearchJson(ESDATATYPENAME, FAULTCURRENT_DB_ID, FAULTCURRENT_SERVERDB_JSON));
    //		assertEquals(FAULTCURRENT_DB_ID, result.mappedData.get(0).getId());
    //		assertEquals(FAULTCURRENT_DB_NODEID, result.mappedData.get(0).getNodeId());
    //		assertEquals(FAULTCURRENT_DB_COUNTER, result.mappedData.get(0).getCounter().intValue());
    //		assertEquals(FAULTCURRENT_DB_OBJECTID, result.mappedData.get(0).getObjectId());
    //		assertEquals(FAULTCURRENT_DB_PROBLEM, result.mappedData.get(0).getProblem());
    //		assertEquals(FAULTCURRENT_DB_SEVERITY, result.mappedData.get(0).getSeverity());
    //		assertEquals(FAULTCURRENT_DB_TIMESTAMP, result.mappedData.get(0).getTimestamp());
    //	}
    //
    @Test
    public void testPmData15m() {

        YangToolsMapper mapper = new YangToolsMapper();
        try {
            Data data = mapper.readValue(PMDATA15M_SERVERDB_JSON.getBytes(), Data.class);
            System.out.println(data);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private String getSearchJson(String dataType, String dbId, String source) {
        return String.format(SEARCHJSON_FORMAT, dataType, dataType, dbId, source);
    }

}
