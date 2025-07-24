/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2023 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
 */

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.InvalidMessageException;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test.config.GeneralConfigForTest;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.stnddefined.StrimziKafkaStndDefinedFaultVESMsgConsumer;

public class TestStrimziKafkaStndDefinedVESMsgConsumer {

    private static final String CONFIGURATIONFILE = "test2.properties";

    // @formatter:off
    private static final String stndDefinedVESMsg_NotifyNewAlarm =
            "{\n"
                    + "    \"event\": {\n"
                    + "        \"commonEventHeader\": {\n"
                    + "            \"startEpochMicrosec\": 1669022429000000,\n"
                    + "            \"eventId\": \"stndDefined000000001\",\n"
                    + "            \"timeZoneOffset\": \"+00:00\",\n"
                    + "            \"internalHeaderFields\": {\n"
                    + "                \"collectorTimeStamp\": \"Mon, 11 21 2022 09:20:30 UTC\"\n"
                    + "            },\n"
                    + "            \"eventType\": \"5GCell-NodeH_Alarms\",\n"
                    + "            \"priority\": \"Low\",\n"
                    + "            \"version\": \"4.1\",\n"
                    + "            \"nfVendorName\": \"NodeH\",\n"
                    + "            \"reportingEntityName\": \"NodeH-5GCell-1234\",\n"
                    + "            \"sequence\": 5,\n"
                    + "            \"domain\": \"stndDefined\",\n"
                    + "            \"lastEpochMicrosec\": 1669022429000000,\n"
                    + "            \"eventName\": \"StndDefined_5GCell-NodeH_Alarms_MyAlarm\",\n"
                    + "            \"vesEventListenerVersion\": \"7.2.1\",\n"
                    + "            \"sourceName\": \"NodeH-5GCell-1234\",\n"
                    + "            \"stndDefinedNamespace\": \"3GPP-FaultSupervision\",\n"
                    + "            \"nfNamingCode\": \"5GCell\"\n"
                    + "        },\n"
                    + "        \"stndDefinedFields\": {\n"
                    + "            \"stndDefinedFieldsVersion\": \"1.0\",\n"
                    + "            \"data\": {\n"
                    + "                \"additionalInformation\": {\n"
                    + "                    \"equipType\": \"5GCell\",\n"
                    + "                    \"vendor\": \"NodeH\",\n"
                    + "                    \"eventTime\": \"2022-11-21T09:20:29Z\",\n"
                    + "                    \"model\": \"SF1234\"\n"
                    + "                },\n"
                    + "                \"backedUpStatus\": false,\n"
                    + "                \"rootCauseIndicator\": false,\n"
                    + "                \"notificationType\": \"notifyNewAlarm\",\n"
                    + "                \"systemDN\": \"DC=com.Node-H,CN=5GCell\",\n"
                    + "                \"alarmType\": \"COMMUNICATIONS_ALARM\",\n"
                    + "                \"probableCause\": \"My cause\",\n"
                    + "                \"perceivedSeverity\": \"@eventSeverity@\",\n"
                    + "                \"eventTime\": \"2022-11-21T09:20:29Z\",\n"
                    + "                \"alarmId\": \"MyAlarm\",\n"
                    + "                \"proposedRepairActions\": \"Repair me\",\n"
                    + "                \"notificationId\": 0,\n"
                    + "                \"href\": \"http://10.0.33.23/3GPPManagement/FaultSupervisionMnS/17.1.0\"\n"
                    + "            },\n"
                    + "            \"schemaReference\": \"https://forge.3gpp.org/rep/sa5/MnS/-/blob/Rel-18/OpenAPI/TS28532_FaultMnS.yaml#components/schemas/NotifyNewAlarm\"\n"
                    + "        }\n"
                    + "    }\n"
                    + "}";
    // @formatter:on
    // @formatter:off
    private static final String stndDefinedVESMsg_NotifyClearedAlarm = "{\n"
            + "    \"event\": {\n"
            + "        \"commonEventHeader\": {\n"
            + "            \"startEpochMicrosec\": 1669022429000000,\n"
            + "            \"eventId\": \"stndDefined000000001\",\n"
            + "            \"timeZoneOffset\": \"+00:00\",\n"
            + "            \"internalHeaderFields\": {\n"
            + "                \"collectorTimeStamp\": \"Mon, 11 21 2022 09:20:30 UTC\"\n"
            + "            },\n"
            + "            \"eventType\": \"5GCell-NodeH_Alarms\",\n"
            + "            \"priority\": \"Low\",\n"
            + "            \"version\": \"4.1\",\n"
            + "            \"nfVendorName\": \"NodeH\",\n"
            + "            \"reportingEntityName\": \"NodeH-5GCell-1234\",\n"
            + "            \"sequence\": 5,\n"
            + "            \"domain\": \"stndDefined\",\n"
            + "            \"lastEpochMicrosec\": 1669022429000000,\n"
            + "            \"eventName\": \"StndDefined_5GCell-NodeH_Alarms_MyAlarm\",\n"
            + "            \"vesEventListenerVersion\": \"7.2.1\",\n"
            + "            \"sourceName\": \"NodeH-5GCell-1234\",\n"
            + "            \"stndDefinedNamespace\": \"3GPP-FaultSupervision\",\n"
            + "            \"nfNamingCode\": \"5GCell\"\n"
            + "        },\n"
            + "        \"stndDefinedFields\": {\n"
            + "            \"stndDefinedFieldsVersion\": \"1.0\",\n"
            + "            \"data\": {\n"
            + "                \"additionalInformation\": {\n"
            + "                    \"equipType\": \"5GCell\",\n"
            + "                    \"vendor\": \"NodeH\",\n"
            + "                    \"eventTime\": \"2022-11-21T09:20:29Z\",\n"
            + "                    \"model\": \"SF1234\"\n"
            + "                },\n"
            + "                \"backedUpStatus\": false,\n"
            + "                \"rootCauseIndicator\": false,\n"
            + "                \"notificationType\": \"notifyClearedAlarm\",\n"
            + "                \"systemDN\": \"DC=com.Node-H,CN=5GCell\",\n"
            + "                \"alarmType\": \"COMMUNICATIONS_ALARM\",\n"
            + "                \"probableCause\": \"My cause\",\n"
            + "                \"perceivedSeverity\": \"@eventSeverity@\",\n"
            + "                \"eventTime\": \"2022-11-21T09:20:29Z\",\n"
            + "                \"alarmId\": \"MyAlarm\",\n"
            + "                \"proposedRepairActions\": \"Repair me\",\n"
            + "                \"notificationId\": 0,\n"
            + "                \"href\": \"http://10.0.33.23/3GPPManagement/FaultSupervisionMnS/17.1.0\"\n"
            + "            },\n"
            + "            \"schemaReference\": \"https://forge.3gpp.org/rep/sa5/MnS/-/blob/Rel-18/OpenAPI/TS28532_FaultMnS.yaml#components/schemas/NotifyClearedAlarm\"\n"
            + "        }\n"
            + "    }\n"
            + "}";
    // @formatter:on

 // @formatter:off
    private static final String stndDefinedVESMsg_Invalid = "{\n"
            + "    \"event\": {\n"
            + "        \"commonEventHeader\": {\n"
            + "            \"startEpochMicrosec\": 1669022429000000,\n"
            + "            \"eventId\": \"stndDefined000000001\",\n"
            + "            \"timeZoneOffset\": \"+00:00\",\n"
            + "            \"internalHeaderFields\": {\n"
            + "                \"collectorTimeStamp\": \"Mon, 11 21 2022 09:20:30 UTC\"\n"
            + "            },\n"
            + "            \"eventType\": \"5GCell-NodeH_Alarms\",\n"
            + "            \"priority\": \"Low\",\n"
            + "            \"version\": \"4.1\",\n"
            + "            \"nfVendorName\": \"NodeH\",\n"
            + "            \"reportingEntityName\": \"NodeH-5GCell-1234\",\n"
            + "            \"sequence\": 5,\n"
            + "            \"domain\": \"stndDefined\",\n"
            + "            \"lastEpochMicrosec\": 1669022429000000,\n"
            + "            \"eventName\": \"StndDefined_5GCell-NodeH_Alarms_MyAlarm\",\n"
            + "            \"vesEventListenerVersion\": \"7.2.1\",\n"
            + "            \"sourceName\": \"NodeH-5GCell-1234\",\n"
            + "            \"stndDefinedNamespace\": \"3GPP-FaultSupervision\",\n"
            + "            \"nfNamingCode\": \"5GCell\"\n"
            + "        },\n"
            + "        \"stndDefinedFields\": {\n"
            + "            \"stndDefinedFieldsVersion\": \"1.0\",\n"
            + "            \"data\": {\n"
            + "                \"additionalInformation\": {\n"
            + "                    \"equipType\": \"5GCell\",\n"
            + "                    \"vendor\": \"NodeH\",\n"
            + "                    \"eventTime\": \"2022-11-21T09:20:29Z\",\n"
            + "                    \"model\": \"SF1234\"\n"
            + "                },\n"
            + "                \"backedUpStatus\": false,\n"
            + "                \"rootCauseIndicator\": false,\n"
            + "                \"notificationType\": \"notifyChangedAlarm\",\n"
            + "                \"systemDN\": \"DC=com.Node-H,CN=5GCell\",\n"
            + "                \"alarmType\": \"COMMUNICATIONS_ALARM\",\n"
            + "                \"probableCause\": \"My cause\",\n"
            + "                \"perceivedSeverity\": \"@eventSeverity@\",\n"
            + "                \"eventTime\": \"2022-11-21T09:20:29Z\",\n"
            + "                \"alarmId\": \"MyAlarm\",\n"
            + "                \"proposedRepairActions\": \"Repair me\",\n"
            + "                \"notificationId\": 0,\n"
            + "                \"href\": \"http://10.0.33.23/3GPPManagement/FaultSupervisionMnS/17.1.0\"\n"
            + "            },\n"
            + "            \"schemaReference\": \"https://forge.3gpp.org/rep/sa5/MnS/-/blob/Rel-18/OpenAPI/TS28532_FaultMnS.yaml#components/schemas/NotifyClearedAlarm\"\n"
            + "        }\n"
            + "    }\n"
            + "}";
    // @formatter:on
    private GeneralConfigForTest cfgTest;

    @Before
    public void before() throws IOException {
        cfgTest = new GeneralConfigForTest(CONFIGURATIONFILE);
    }

    @After
    public void after() {
        cfgTest.close();
    }


    @Test
    public void testNotifyNewAlarm() throws IOException {
        StrimziKafkaStndDefinedFaultVESMsgConsumer stndDefinedFaultMsgConsumer =
                new StrimziKafkaStndDefinedFaultVESMsgConsumer(cfgTest.getCfg(), null);
        try {

            stndDefinedFaultMsgConsumer
                    .processMsg(stndDefinedVESMsg_NotifyNewAlarm.replace("@eventSeverity@", "CRITICAL"));
            stndDefinedFaultMsgConsumer
                    .processMsg(stndDefinedVESMsg_NotifyNewAlarm.replace("@eventSeverity@", "Major"));
            stndDefinedFaultMsgConsumer
                    .processMsg(stndDefinedVESMsg_NotifyNewAlarm.replace("@eventSeverity@", "minor"));
            stndDefinedFaultMsgConsumer
                    .processMsg(stndDefinedVESMsg_NotifyNewAlarm.replace("@eventSeverity@", "NonAlarmed"));
            stndDefinedFaultMsgConsumer
                    .processMsg(stndDefinedVESMsg_NotifyNewAlarm.replace("@eventSeverity@", "warning"));
            stndDefinedFaultMsgConsumer
                    .processMsg(stndDefinedVESMsg_NotifyNewAlarm.replace("@eventSeverity@", "Unknown"));
            //stndDefinedFaultMsgConsumer.processMsg(faultVESMsg_Incomplete);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception while processing Fault Message - " + e.getMessage());
        }
    }

    @Test
    public void testNotifyClearedAlarm() throws IOException {
        StrimziKafkaStndDefinedFaultVESMsgConsumer stndDefinedFaultMsgConsumer =
                new StrimziKafkaStndDefinedFaultVESMsgConsumer(cfgTest.getCfg(), null);
        try {

            stndDefinedFaultMsgConsumer
                    .processMsg(stndDefinedVESMsg_NotifyClearedAlarm.replace("@eventSeverity@", "cleared"));
            stndDefinedFaultMsgConsumer
                    .processMsg(stndDefinedVESMsg_NotifyClearedAlarm.replace("@eventSeverity@", "Indeterminate"));
            //stndDefinedFaultMsgConsumer.processMsg(faultVESMsg_Incomplete);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception while processing Fault Message - " + e.getMessage());
        }
    }

    @Test(expected = InvalidMessageException.class)
    public void testInvalidStndDefinedMessage() throws InvalidMessageException, JsonProcessingException {
        StrimziKafkaStndDefinedFaultVESMsgConsumer stndDefinedFaultMsgConsumer =
                new StrimziKafkaStndDefinedFaultVESMsgConsumer(cfgTest.getCfg(), null);
        stndDefinedFaultMsgConsumer.processMsg(stndDefinedVESMsg_Invalid.replace("@eventSeverity@", "cleared"));
    }
}
