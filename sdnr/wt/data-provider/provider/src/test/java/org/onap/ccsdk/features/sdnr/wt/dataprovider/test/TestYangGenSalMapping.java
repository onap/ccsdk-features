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
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONObject;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.BoolQueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.CreateIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.EsDataObjectReaderWriter2;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.YangHelper2;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.test.util.HostInfoForTest;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.DataProviderYangToolsMapper;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.connection.parameters.OdlHelloMessageCapabilitiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.credentials.credentials.LoginPasswordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMediatorServerInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mListInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SortOrder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.PaginationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Sortorder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata15m.entity.PerformanceDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.DataBuilder;

public class TestYangGenSalMapping {

    // Create mapper for serialization and deserialization
    DataProviderYangToolsMapper mapper = new DataProviderYangToolsMapper();

    @Test
    public void test1() throws IOException {

        // Create test object
        NetconfNodeBuilder netconfNodeBuilder = new NetconfNodeBuilder();
        netconfNodeBuilder.setConnectedMessage("ConnMessage");

        LoginPasswordBuilder loginPasswordBuilder = new LoginPasswordBuilder();
        loginPasswordBuilder.setUsername("myTestUsername");
        loginPasswordBuilder.setPassword("myTestPassword");
        netconfNodeBuilder.setCredentials(loginPasswordBuilder.build());

        OdlHelloMessageCapabilitiesBuilder odlHelloMessageCapabilitiesBuilder =
                new OdlHelloMessageCapabilitiesBuilder();
        Set<Uri> uriList = new HashSet<>();
        uriList.add(new Uri("test.uri"));
        odlHelloMessageCapabilitiesBuilder.setCapability(uriList);
        netconfNodeBuilder.setOdlHelloMessageCapabilities(odlHelloMessageCapabilitiesBuilder.build());

        NetconfNode netconfNode = netconfNodeBuilder.build();
        out(netconfNode.toString());

        // Map Object to JSON String
        String res = mapper.writeValueAsString(netconfNode);
        JSONObject json = new JSONObject(res); // Convert text to object
        out(json.toString(4)); // Print it with specified indentation

        // Map to JSON String to Object
        NetconfNode generatedNode = mapper.readValue(res.getBytes(), NetconfNode.class);
        out(generatedNode.toString()); // Print it with specified indentation
        // Compare result
        //TODO - Guilin
        //out("Equal?  "+netconfNode.equals(generatedNode));
    }

    @Test
    public void test2() throws Exception {

        final String idx = "inventorytest";
        HostInfo[] hostInfo = HostInfoForTest.get();
        HtDatabaseClient db = HtDatabaseClient.getClient(hostInfo);

        EsDataObjectReaderWriter2<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.Data> dataRW =
                new EsDataObjectReaderWriter2<>(db, Entity.Inventoryequipment,
                        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.Data.class);
        if (!db.isExistsIndex(idx)) {
            db.createIndex(new CreateIndexRequest(idx));
        }
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.Data d1;
        d1 = getInventoryDataBuilder("MyDescription", 23L).build();
        String id = dataRW.write(d1, null);

        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.Data d2;
        d2 = dataRW.read(id);

        out(d2.toString());
        if (db.isExistsIndex(idx)) {
            db.deleteIndex(new DeleteIndexRequest(idx));
        }

    }

    @Test
    public void test3() throws IOException {

        PerformanceDataBuilder performanceBuilder = new PerformanceDataBuilder();
        performanceBuilder.setEs(99);
        DataBuilder pmData15MinutesBuilder = new DataBuilder();
        pmData15MinutesBuilder.setLayerProtocolName("fdsaf");
        pmData15MinutesBuilder.setTimeStamp(new DateAndTime("2017-03-01T09:15:00.0Z"));
        pmData15MinutesBuilder.setPerformanceData(performanceBuilder.build());

        // Map Object to JSON String
        String res = mapper.writeValueAsString(pmData15MinutesBuilder.build());
        JSONObject json = new JSONObject(res); // Convert text to object
        out(json.toString(4)); // Print it with specified indentation

        // Map to JSON String to Object
        Data generatedNode = mapper.readValue(res.getBytes(), Data.class);
        out(generatedNode.toString()); // Print it with specified indentation
    }

    @Test
    public void test4() throws IOException {
     // @formatter:off
     String jsonString = "{\n"
                + "\"node-name\": \"Sim2230\",\n"
                + "\"uuid-interface\": \"LP-MWPS-TTP-RADIO\",\n"
                + "\"layer-protocol-name\": \"MWPS\",\n"
                + "\"radio-signal-id\": \"Test8\",\n"
                + "\"time-stamp\": \"2017-03-01T09:15:00.0Z\",\n"
                + "\"granularity-period\": \"Period15Min\",\n"
                + "\"scanner-id\": \"PM_RADIO_15M_4\",\n"
                + "\"performance-data\": {\n"
                +     "\"unavailability\": 0,\n"
                +     "\"tx-level-max\": 3,\n"
                +     "\"tx-level-avg\": 3,\n"
                +     "\"rx-level-min\": -44,\n"
                +     "\"rx-level-max\": -45,\n"
                +     "\"rx-level-avg\": -44,\n"
                +     "\"time2-states\": 0,\n"
                +     "\"time4-states-s\": 0,\n"
                +     "\"time4-states\": 0,\n"
                +     "\"time8-states\": -1,\n"
                +     "\"time16-states-s\": -1,\n"
                +     "\"time16-states\": 0,\n"
                +     "\"time32-states\": -1,\n"
                +     "\"time64-states\": 900,\n"
                +     "\"time128-states\": -1,\n"
                +     "\"time256-states\": -1,\n"
                +     "\"time512-states\": -1,\n"
                +     "\"time512-states-l\": -1,\n"
                +     "\"time1024-states\": -1,\n"
                +     "\"time1024-states-l\": -1,\n"
                +     "\"time8192-states-l\": -1,\n"
                +     "\"time8192-states\": -1,\n"
                +     "\"time2048-states\": -1,\n"
                +     "\"snir-min\": -99,\n"
                +     "\"snir-max\": -99,\n"
                +     "\"snir-avg\": -99,\n"
                +     "\"xpd-min\": -99,\n"
                +     "\"xpd-max\": -99,\n"
                +     "\"xpd-avg\": -99,\n"
                +     "\"rf-temp-min\": -99,\n"
                +     "\"rf-temp-max\": -99,\n"
                +     "\"rf-temp-avg\": -99,\n"
                +     "\"defect-blocks-sum\": -1,\n"
                +     "\"time-period\": 900,\n"
                +     "\"cses\": 0,\n"
                +     "\"time4096-states-l\": -1,\n"
                +     "\"tx-level-min\": 3,\n"
                +     "\"es\": 0,\n"
                +     "\"time2048-states-l\": -1,\n"
                +     "\"time4096-states\": -1,\n"
                +     "\"ses\": 0\n"
                + "},\n"
                + "\"suspect-interval-flag\": false\n"
                + "}\n"
                + "}";
        // @formatter:on
        // Map to JSON String to Object
        Data generatedNode = mapper.readValue(jsonString.getBytes(), Data.class);
        out(generatedNode.toString()); // Print it with specified indentation
    }

    @Test
    public void test5() throws IOException {
        // @formatter:off
        String jsonString = "{\n"
                + "    \"time-stamp\": \"2017-03-01T06:45:00.0Z\",\n"
                + "    \"node-name\": \"Sim2230\",\n"
                + "    \"uuid-interface\": \"LP-MWPS-TTP-RADIO\",\n"
                + "    \"scanner-id\": \"PM_RADIO_15M_14\",\n"
                + "    \"layer-protocol-name\": \"MWPS\",\n"
                + "    \"granularity-period\": \"Period15Min\",\n"
                + "    \"radio-signal-id\": \"Test8\",\n"
                + "    \"suspect-interval-flag\": false,\n"
                + "    \"performance-data\": {\n"
                + "        \"time4096-states-l\": -1,\n"
                + "        \"time16-states-s\": -1,\n"
                + "        \"tx-level-max\": 3,\n"
                + "        \"snir-max\": -99,\n"
                + "        \"time16-states\": 0,\n"
                + "        \"time64-states\": 900,\n"
                + "        \"unavailability\": 0,\n"
                + "        \"time8192-states-l\": -1,\n"
                + "        \"time512-states\": -1,\n"
                + "        \"xpd-min\": -99,\n"
                + "        \"xpd-avg\": -99,\n"
                + "        \"tx-level-avg\": 3,\n"
                + "        \"tx-level-min\": 3,\n"
                + "        \"rf-temp-min\": -99,\n"
                + "        \"rf-temp-avg\": -99,\n"
                + "        \"snir-avg\": -99,\n"
                + "        \"snir-min\": -99,\n"
                + "        \"time-period\": 900,\n"
                + "        \"time2-states\": 0,\n"
                + "        \"time4-states\": 0,\n"
                + "        \"time8-states\": -1,\n"
                + "        \"ses\": 0,\n"
                + "        \"time2048-states-l\": -1,\n"
                + "        \"time2048-states\": -1,\n"
                + "        \"xpd-max\": -99,\n"
                + "        \"rf-temp-max\": -99,\n"
                + "        \"time8192-states\": -1,\n"
                + "        \"time128-states\": -1,\n"
                + "        \"time256-states\": -1,\n"
                + "        \"rx-level-min\": -44,\n"
                + "        \"rx-level-avg\": -44,\n"
                + "        \"time1024-states-l\": -1,\n"
                + "        \"es\": 0,\n"
                + "        \"cses\": 0,\n"
                + "        \"time4-states-s\": 0,\n"
                + "        \"time1024-states\": -1,\n"
                + "        \"time512-states-l\": -1,\n"
                + "        \"time4096-states\": -1,\n"
                + "        \"rx-level-max\": -45,\n"
                + "        \"defect-blocks-sum\": -1,\n"
                + "        \"time32-states\": -1\n"
                + "    }\n"
                + "}";
        // @formatter:on
        // Map to JSON String to Object
        Data generatedNode = mapper.readValue(jsonString.getBytes(), Data.class);
        out(generatedNode.toString()); // Print it with specified indentation
    }

    @Test
    public void test6() throws Exception {
        out(method());
        HostInfo[] hostInfo = HostInfoForTest.get();
        HtDatabaseClient dbClient = HtDatabaseClient.getClient(hostInfo);

        EsDataObjectReaderWriter2<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data> pm15mRW =
                new EsDataObjectReaderWriter2<>(dbClient, Entity.Historicalperformance15min,
                        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data.class);
        pm15mRW.setEsIdAttributeName("_nodeName");

        ReadPmdata15mListInputBuilder inputBuilder = new ReadPmdata15mListInputBuilder();
        PaginationBuilder paginationBuilder = new PaginationBuilder();
        paginationBuilder.setPage(YangHelper2.getBigIntegerOrUint64(new BigInteger("1")));
        paginationBuilder.setSize(YangHelper2.getLongOrUint32(20L));
        inputBuilder.setPagination(paginationBuilder.build());

        ReadPmdata15mListInput input = inputBuilder.build();


        ReadPmdata15mListOutputBuilder outputBuilder = new ReadPmdata15mListOutputBuilder();
        long page = getPage(input);
        long pageSize = getPageSize(input);

        QueryBuilder query = fromFilter(YangHelper.getList(input.getFilter())).from((page - 1) * pageSize).size(pageSize);
        setSortOrder(query, YangHelper.getList(input.getSortorder()));

        SearchResult<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data> result =
                pm15mRW.doReadAll(query);

        out("Found: " + result.getHits().size());
        int t = 0;
        for (Data hit : result.getHits()) {
            out("Hit " + t++ + ":" + hit);
        }
        setPagination(outputBuilder, page, pageSize, result.getTotal());
        outputBuilder.setData(result.getHits());
    }

    @Test
    public void test7() throws Exception {
        out(method());
        HostInfo[] hostInfo = HostInfoForTest.get();
        HtDatabaseClient dbClient = HtDatabaseClient.getClient(hostInfo);

        EsDataObjectReaderWriter2<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.mediator.server.list.output.Data> mediatorserverRW;
        mediatorserverRW = new EsDataObjectReaderWriter2<>(dbClient, Entity.MediatorServer,
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.mediator.server.list.output.Data.class)
                        .setWriteInterface(
                                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MediatorServerEntity.class)
                        .setEsIdAttributeName("_id");

        CreateMediatorServerInputBuilder inputBuilder = new CreateMediatorServerInputBuilder();
        inputBuilder.setName("Hans");
        inputBuilder.setUrl("MyGreatUrl");

        String id = mediatorserverRW.write(inputBuilder.build(), "testid");
        System.out.println(id);

    }

    @Test
    public void test8() throws IOException {
        out(method());
        String input;
        input = "id-dd-dd";
        System.out.println("Map " + input + " to " + YangToolsMapperHelper.toCamelCaseAttributeName(input));
        input = "idDdGg";
        System.out.println("Map " + input + " to " + YangToolsMapperHelper.toCamelCaseAttributeName(input));
        input = "_idDdGg";
        System.out.println("Map " + input + " to " + YangToolsMapperHelper.toCamelCaseAttributeName(input));
        input = "--ff--gfg";
        System.out.println("Map " + input + " to " + YangToolsMapperHelper.toCamelCaseAttributeName(input));
        input = "";
        System.out.println("Map " + input + " to " + YangToolsMapperHelper.toCamelCaseAttributeName(input));
    }

    /* ---------------------------------
     * Private
     */
    private static String method() {
        String nameofCurrMethod = new Throwable().getStackTrace()[1].getMethodName();
        return nameofCurrMethod;
    }

    private org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.DataBuilder getInventoryDataBuilder(
            String description, long treeLevel) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.DataBuilder dataBuilder =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.inventory.list.output.DataBuilder();
        dataBuilder.setDescription(description);
        dataBuilder.setTreeLevel(YangHelper2.getLongOrUint32(treeLevel));
        return dataBuilder;
    }


    private static void out(String text) {
        System.out.println("----------------------");
        System.out.println(text);
    }

    private static long getPage(EntityInput input) {
        return getPage(input, 1);
    }

    private static long getPage(EntityInput input, long defaultValue) {
        return input.getPagination() != null ? input.getPagination().getPage().longValue() : defaultValue;
    }

    private static long getPageSize(EntityInput input) {
        return getPageSize(input, 1);
    }

    private static long getPageSize(EntityInput input, long defaultValue) {
        return input.getPagination() != null ? input.getPagination().getSize().longValue() : defaultValue;
    }

    private static QueryBuilder fromFilter(@Nullable List<Filter> filters) {
        return fromFilter(filters, "");
    }

    private static QueryBuilder fromFilter(@Nullable List<Filter> filters, String prefix) {
        if (filters == null || filters.size() == 0) {
            return QueryBuilders.matchAllQuery();

        } else if (filters.size() == 1) {
            return QueryBuilders.matchQuery(filters.get(0).getProperty(), filters.get(0).getFiltervalue());
        } else {
            BoolQueryBuilder query = new BoolQueryBuilder();
            for (Filter fi : filters) {
                query.must(QueryBuilders.matchQuery((prefix != null ? prefix : "") + fi.getProperty(),
                        fi.getFiltervalue()));
            }
            return query;
        }

    }

    private static QueryBuilder setSortOrder(QueryBuilder query, @Nullable List<Sortorder> sortorder) {
        return setSortOrder(query, sortorder, "");
    }

    private static QueryBuilder setSortOrder(QueryBuilder query, @Nullable List<Sortorder> sortorder, String prefix) {
        if (sortorder != null && sortorder.size() > 0) {
            for (Sortorder so : sortorder) {
                query.sort((prefix != null ? prefix : "") + so.getProperty(),
                        so.getSortorder() == SortOrder.Ascending
                                ? org.onap.ccsdk.features.sdnr.wt.common.database.queries.SortOrder.ASCENDING
                                : org.onap.ccsdk.features.sdnr.wt.common.database.queries.SortOrder.DESCENDING);
            }
        }
        return query;

    }

    private static void setPagination(ReadPmdata15mListOutputBuilder outputBuilder, long page, long pageSize,
            long totalSize) {
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Pagination value =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.PaginationBuilder()
                .setPage(YangHelper2.getBigIntegerOrUint64(BigInteger.valueOf(page))).setSize(YangHelper2.getLongOrUint32(pageSize))
                .setTotal(YangHelper2.getBigIntegerOrUint64(BigInteger.valueOf(totalSize))).build();
        outputBuilder.setPagination(value);
    }

}
