/*******************************************************************************
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 ******************************************************************************/

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.DMaaPFaultVESMsgConsumer;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.FaultNotificationClient;

public class TestDMaaPFaultVESMsgConsumer extends DMaaPFaultVESMsgConsumer {

    private static final String DEFAULT_SDNRUSER = "admin";
    private static final String DEFAULT_SDNRPASSWD = "admin";
    private static final String DEFAULT_SDNRBASEURL = "http://localhost:8181";

    private static final String faultVESMsg = "" + "{\"event\":" + "	{\"commonEventHeader\":"
            + "		{	\"domain\":\"fault\"," + "			\"eventId\":\"1e9a28bcd119_50007_2019-11-20T14:59:47.3Z\","
            + "			\"eventName\":\"fault_O_RAN_COMPONENT_Alarms\","
            + "			\"eventType\":\"O_RAN_COMPONENT_Alarms\"," + "			\"sequence\":1,"
            + "			\"priority\":\"Low\"," + "			\"reportingEntityId\":\"\","
            + "			\"reportingEntityName\":\"1e9a28bcd119_50007\"," + "			\"sourceId\":\"\","
            + "			\"sourceName\":\"1e9a28bcd119_50007\"," + "			\"startEpochMicrosec\":94801033822670,"
            + "			\"lastEpochMicrosec\":94801033822670," + "			\"nfNamingCode\":\"sdn controller\","
            + "			\"nfVendorName\":\"sdn\"," + "			\"timeZoneOffset\":\"+00:00\","
            + "			\"version\":\"4.0.1\"," + "			\"vesEventListenerVersion\":\"7.0.1\"" + "		},"
            + "		\"faultFields\":" + "		{" + "			\"faultFieldsVersion\":\"4.0\","
            + "			\"alarmCondition\":\"8\"," + "			\"alarmInterfaceA\":\"dkom32\","
            + "			\"eventSourceType\":\"O_RAN_COMPONENT\","
            + "			\"specificProblem\":\"dsonj32 don32 mdson32pk654\","
            + "			\"eventSeverity\":\"@eventSeverity@\"," + "			\"vfStatus\":\"Active\","
            + "			\"alarmAdditionalInformation\":" + "			{"
            + "				\"eventTime\":\"2019-11-20T14:59:47.3Z\"," + "				\"equipType\":\"O-RAN-sim\","
            + "				\"vendor\":\"Melacon\"," + "				\"model\":\"Simulated Device\"" + "			}"
            + "		}" + "	}" + "}";

    private static final String faultVESMsg_Incomplete = "" + "{\"event\":" + "	{\"commonEventHeader\":"
            + "		{	\"domain\":\"fault\"," + "			\"eventId\":\"1e9a28bcd119_50007_2019-11-20T14:59:47.3Z\","
            + "			\"eventName\":\"fault_O_RAN_COMPONENT_Alarms\","
            + "			\"eventType\":\"O_RAN_COMPONENT_Alarms\"," + "			\"sequence\":1,"
            + "			\"priority\":\"Low\"," + "			\"reportingEntityId\":\"\","
            + "			\"reportingEntityName\":\"1e9a28bcd119_50007\"," + "			\"sourceId\":\"\","
            + "			\"sourceName\":\"1e9a28bcd119_50007\"," + "			\"startEpochMicrosec\":94801033822670,"
            + "			\"lastEpochMicrosec\":94801033822670," + "			\"nfNamingCode\":\"sdn controller\","
            + "			\"nfVendorName\":\"sdn\"," + "			\"timeZoneOffset\":\"+00:00\","
            + "			\"version\":\"4.0.1\"," + "			\"vesEventListenerVersion\":\"7.0.1\"," + "		},"
            + "		\"faultFields\":" + "		{" + "			\"faultFieldsVersion\":\"4.0\","
            + "			\"alarmCondition\":\"8\"," + "			\"alarmInterfaceA\":\"dkom32\","
            + "			\"eventSourceType\":\"O_RAN_COMPONENT\","
            + "			\"specificProblem\":\"dsonj32 don32 mdson32pk654\","
            + "			\"eventSeverity\":\"CRITICAL\"," + "			\"vfStatus\":\"Active\","
            + "			\"alarmAdditionalInformation\":" + "			{"
            + "				\"eventTime\":\"2019-11-20T14:59:47.3Z\"," + "				\"equipType\":\"O-RAN-sim\","
            + "				\"vendor\":\"Melacon\"," + "				\"model\":\"Simulated Device\"" + "			}"
            + "		}" + "	}" + "}";

    @Test
    public void test() {
        DMaaPFaultVESMsgConsumer faultMsgConsumer = new TestDMaaPFaultVESMsgConsumer();
        try {

            faultMsgConsumer.processMsg(faultVESMsg.replace("@eventSeverity@", "CRITICAL"));
            faultMsgConsumer.processMsg(faultVESMsg.replace("@eventSeverity@", "Major"));
            faultMsgConsumer.processMsg(faultVESMsg.replace("@eventSeverity@", "minor"));
            faultMsgConsumer.processMsg(faultVESMsg.replace("@eventSeverity@", "NonAlarmed"));
            faultMsgConsumer.processMsg(faultVESMsg.replace("@eventSeverity@", "warning"));
            faultMsgConsumer.processMsg(faultVESMsg.replace("@eventSeverity@", "Unknown"));
            faultMsgConsumer.processMsg(faultVESMsg_Incomplete);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception while processing Fault Message - " + e.getMessage());
        }
    }

    @Override
    public FaultNotificationClient getFaultNotificationClient(String baseUrl) {
        return new TestFaultNotificationClient();
    }

    @Override
    public String getSDNRUser() {
        return DEFAULT_SDNRUSER;
    }

    @Override
    public String getSDNRPasswd() {
        return DEFAULT_SDNRPASSWD;
    }

    @Override
    public String getBaseUrl() {
        return DEFAULT_SDNRBASEURL;
    }

    @Test
    public void Test1() {
        TestGeneralConfig cfgTest = new TestGeneralConfig();
        cfgTest.test();
        DMaaPFaultVESMsgConsumer faultConsumer = new DMaaPFaultVESMsgConsumer();
        System.out.println(faultConsumer.getBaseUrl());
        System.out.println(faultConsumer.getSDNRUser());
        System.out.println(faultConsumer.getSDNRPasswd());
    }

    /*
     * @Test public void Test2() { TestGeneralConfig cfgTest = new
     * TestGeneralConfig(); cfgTest.test1(); //cfgTest.test();
     * DMaaPFaultVESMsgConsumer faultConsumer = new DMaaPFaultVESMsgConsumer();
     * System.out.println(faultConsumer.getBaseUrl());
     * System.out.println(faultConsumer.getSDNRUser());
     * System.out.println(faultConsumer.getSDNRPasswd()); }
     */
}
