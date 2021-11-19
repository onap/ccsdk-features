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
package org.onap.ccsdk.features.sdnr.wt.yang.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.IdentifierDeserializer;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.parameters.OdlHelloMessageCapabilitiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.credentials.Credentials;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.credentials.credentials.LoginPasswordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.grp.MeasurementKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata15m.entity.PerformanceDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        List<Uri> uriList = new ArrayList<>();
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

    private static void out(String text) {
        System.out.println("----------------------");
        System.out.println(text);
    }

    private static class DataProviderYangToolsMapper extends YangToolsMapper {

        @SuppressWarnings("unused")
        private final Logger LOG = LoggerFactory.getLogger(DataProviderYangToolsMapper.class);
        private static final long serialVersionUID = 1L;

        public DataProviderYangToolsMapper() {
            super();
            this.addDeserializer(Credentials.class, LoginPasswordBuilder.class.getName());
            this.addKeyDeserializer(MeasurementKey.class, new IdentifierDeserializer());
        }


    }

}
