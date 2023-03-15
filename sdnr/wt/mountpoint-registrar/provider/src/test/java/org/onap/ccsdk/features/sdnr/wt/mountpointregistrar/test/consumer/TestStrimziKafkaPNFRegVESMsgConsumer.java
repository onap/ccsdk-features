/*
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
 */

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test.consumer;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test.config.GeneralConfigForTest;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.pnfreg.StrimziKafkaPNFRegVESMsgConsumer;

public class TestStrimziKafkaPNFRegVESMsgConsumer {

    private static final String DEFAULT_SDNRUSER = "admin";
    private static final String DEFAULT_SDNRPASSWD = "admin";
    private static final String DEFAULT_SDNRBASEURL = "http://localhost:8181";
    private static final String CONFIGURATIONFILE = "test4.properties";

    // @formatter:off
    private static final String pnfRegMsg_TLS = "{\n"
    + "  \"event\": {\n"
            + "    \"commonEventHeader\": {\n"
            + "      \"domain\": \"pnfRegistration\",\n"
            + "      \"eventId\": \"NSHMRIACQ01M01123401_1234 BestInClass\",\n"
            + "      \"eventName\": \"pnfRegistration_EventType5G\",\n"
            + "      \"eventType\": \"EventType5G\",\n"
            + "      \"sequence\": 0,\n"
            + "      \"priority\": \"Low\",\n"
            + "      \"reportingEntityId\": \"\",\n"
            + "      \"reportingEntityName\": \"pendurty-virtual-machine\",\n"
            + "      \"sourceId\": \"\",\n"
            + "      \"sourceName\": \"NSHMRIACQ01M01123401\",\n"
            + "      \"startEpochMicrosec\": 1571300004203,\n"
            + "      \"lastEpochMicrosec\": 1571300004203,\n"
            + "      \"nfNamingCode\": \"1234\",\n"
            + "      \"nfVendorName\": \"VENDORA\",\n"
            + "      \"timeZoneOffset\": \"+00:00\",\n"
            + "      \"version\": \"4.0.1\",\n"
            + "      \"vesEventListenerVersion\":\"7.0.1\"\n"
            + "    },\n"
            + "    \"pnfRegistrationFields\": {\n"
            + "      \"pnfRegistrationFieldsVersion\": \"2.0\",\n"
            + "		\"additionalFields\": \n"
            + "		{ \n"
            + "			\"protocol\":\"TLS\",\n"
            + "			\"keyId\":\"netconf\",\n"
            + "			\"oamPort\":\"50000\",\n"
            + "			\"betweenAttemptsTimeout\":\"2000\",\n"
            + "			\"keepaliveDelay\":\"120\",\n"
            + "			\"sleep-factor\":\"1.5\",\n"
            + "			\"reconnectOnChangedSchema\":\"false\",\n"
            + "			\"connectionTimeout\":\"20000\",\n"
            + "			\"maxConnectionAttempts\":\"100\",\n"
            + "			\"username\":\"netconf\",\n"
            + "			\"tcpOnly\":\"false\"\n"
            + "		},\n"
            + "      \"lastServiceDate\":\"2019-08-16\",\n"
            + "      \"macAddress\":\"02:42:f7:d4:62:ce\",\n"
            + "      \"manufactureDate\":\"2019-08-16\",\n"
            + "      \"modelNumbsdnrer\": \"1234 BestInClass\",\n"
            + "      \"oamV4IpAddress\": \"10.10.10.11\",\n"
            + "	   \"oamPort\":\"17380\",\n"
            + "      \"oamV6IpAddress\": \"0:0:0:0:0:ffff:a0a:011\",\n"
            + "      \"serialNumber\": \"VENDORA-1234-10.10.10.11-1234 BestInClass\",\n"
            + "      \"softwareVersion\": \"2.3.5\",\n"
            + "      \"unitFamily\": \"VENDORA-1234\",\n"
            + "      \"unitType\": \"1234\",\n"
            + "      \"vendorName\": \"VENDORA\"\n"
            + "    }\n"
            + "  }\n"
            + "}\n"
            + "";
    private static final String pnfRegMsg_SSH = "{\n"
            + "  \"event\": {\n"
            + "    \"commonEventHeader\": {\n"
            + "      \"domain\": \"pnfRegistration\",\n"
            + "      \"eventId\": \"NSHMRIACQ01M01123401_1234 BestInClass\",\n"
            + "      \"eventName\": \"pnfRegistration_EventType5G\",\n"
            + "      \"eventType\": \"EventType5G\",\n"
            + "      \"sequence\": 0,\n"
            + "      \"priority\": \"Low\",\n"
            + "      \"reportingEntityId\": \"\",\n"
            + "      \"reportingEntityName\": \"pendurty-virtual-machine\",\n"
            + "      \"sourceId\": \"\",\n"
            + "      \"sourceName\": \"NSHMRIACQ01M01123401\",\n"
            + "      \"startEpochMicrosec\": 1571300004203,\n"
            + "      \"lastEpochMicrosec\": 1571300004203,\n"
            + "      \"nfNamingCode\": \"1234\",\n"
            + "      \"nfVendorName\": \"VENDORA\",\n"
            + "      \"timeZoneOffset\": \"+00:00\",\n"
            + "      \"version\": \"4.0.1\",\n"
            + "      \"vesEventListenerVersion\":\"7.0.1\"\n"
            + "    },\n"
            + "    \"pnfRegistrationFields\": {\n"
            + "      \"pnfRegistrationFieldsVersion\": \"2.0\",\n"
            + "		\"additionalFields\": \n"
            + "		{ \n"
            + "			\"protocol\":\"SSH\",\n"
            + "			\"password\":\"netconf\",\n"
            + "			\"oamPort\":\"50000\",\n"
            + "			\"betweenAttemptsTimeout\":\"2000\",\n"
            + "			\"keepaliveDelay\":\"120\",\n"
            + "			\"sleep-factor\":\"1.5\",\n"
            + "			\"reconnectOnChangedSchema\":\"false\",\n"
            + "			\"connectionTimeout\":\"20000\",\n"
            + "			\"maxConnectionAttempts\":\"100\",\n"
            + "			\"username\":\"netconf\",\n"
            + "			\"tcpOnly\":\"false\"\n"
            + "		},\n"
            + "      \"lastServiceDate\":\"2019-08-16\",\n"
            + "      \"macAddress\":\"02:42:f7:d4:62:ce\",\n"
            + "      \"manufactureDate\":\"2019-08-16\",\n"
            + "      \"modelNumbsdnrer\": \"1234 BestInClass\",\n"
            + "      \"oamV4IpAddress\": \"10.10.10.11\",\n"
            + "	   \"oamPort\":\"17380\",\n"
            + "      \"oamV6IpAddress\": \"0:0:0:0:0:ffff:a0a:011\",\n"
            + "      \"serialNumber\": \"VENDORA-1234-10.10.10.11-1234 BestInClass\",\n"
            + "      \"softwareVersion\": \"2.3.5\",\n"
            + "      \"unitFamily\": \"VENDORA-1234\",\n"
            + "      \"unitType\": \"1234\",\n"
            + "      \"vendorName\": \"VENDORA\"\n"
            + "    }\n"
            + "  }\n"
            + "}\n"
            + "";
    private static final String pnfRegMsg_OTHER = "{\n"
            + "  \"event\": {\n"
            + "    \"commonEventHeader\": {\n"
            + "      \"domain\": \"pnfRegistration\",\n"
            + "      \"eventId\": \"NSHMRIACQ01M01123401_1234 BestInClass\",\n"
            + "      \"eventName\": \"pnfRegistration_EventType5G\",\n"
            + "      \"eventType\": \"EventType5G\",\n"
            + "      \"sequence\": 0,\n"
            + "      \"priority\": \"Low\",\n"
            + "      \"reportingEntityId\": \"\",\n"
            + "      \"reportingEntityName\": \"pendurty-virtual-machine\",\n"
            + "      \"sourceId\": \"\",\n"
            + "      \"sourceName\": \"NSHMRIACQ01M01123401\",\n"
            + "      \"startEpochMicrosec\": 1571300004203,\n"
            + "      \"lastEpochMicrosec\": 1571300004203,\n"
            + "      \"nfNamingCode\": \"1234\",\n"
            + "      \"nfVendorName\": \"VENDORA\",\n"
            + "      \"timeZoneOffset\": \"+00:00\",\n"
            + "      \"version\": \"4.0.1\",\n"
            + "      \"vesEventListenerVersion\":\"7.0.1\"\n"
            + "    },\n"
            + "    \"pnfRegistrationFields\": {\n"
            + "      \"pnfRegistrationFieldsVersion\": \"2.0\",\n"
            + "		\"additionalFields\": \n"
            + "		{ \n"
            + "			\"protocol\":\"OTHER\",\n"
            + "			\"password\":\"netconf\",\n"
            + "			\"oamPort\":\"50000\",\n"
            + "			\"betweenAttemptsTimeout\":\"2000\",\n"
            + "			\"keepaliveDelay\":\"120\",\n"
            + "			\"sleep-factor\":\"1.5\",\n"
            + "			\"reconnectOnChangedSchema\":\"false\",\n"
            + "			\"connectionTimeout\":\"20000\",\n"
            + "			\"maxConnectionAttempts\":\"100\",\n"
            + "			\"username\":\"netconf\",\n"
            + "			\"tcpOnly\":\"false\"\n"
            + "		},\n"
            + "      \"lastServiceDate\":\"2019-08-16\",\n"
            + "      \"macAddress\":\"02:42:f7:d4:62:ce\",\n"
            + "      \"manufactureDate\":\"2019-08-16\",\n"
            + "      \"modelNumbsdnrer\": \"1234 BestInClass\",\n"
            + "      \"oamV4IpAddress\": \"10.10.10.11\",\n"
            + "	   \"oamPort\":\"17380\",\n"
            + "      \"oamV6IpAddress\": \"0:0:0:0:0:ffff:a0a:011\",\n"
            + "      \"serialNumber\": \"VENDORA-1234-10.10.10.11-1234 BestInClass\",\n"
            + "      \"softwareVersion\": \"2.3.5\",\n"
            + "      \"unitFamily\": \"VENDORA-1234\",\n"
            + "      \"unitType\": \"1234\",\n"
            + "      \"vendorName\": \"VENDORA\"\n"
            + "    }\n"
            + "  }\n"
            + "}\n"
            + "";
    private static final String pnfRegMsg = "{\n"
            + "  \"event\": {\n"
            + "    \"commonEventHeader\": {\n"
            + "      \"domain\": \"pnfRegistration\",\n"
            + "      \"eventId\": \"NSHMRIACQ01M01123401_1234 BestInClass\",\n"
            + "      \"eventName\": \"pnfRegistration_EventType5G\",\n"
            + "      \"eventType\": \"EventType5G\",\n"
            + "      \"sequence\": 0,\n"
            + "      \"priority\": \"Low\",\n"
            + "      \"reportingEntityId\": \"\",\n"
            + "      \"reportingEntityName\": \"pendurty-virtual-machine\",\n"
            + "      \"sourceId\": \"\",\n"
            + "      \"sourceName\": \"NSHMRIACQ01M01123401\",\n"
            + "      \"startEpochMicrosec\": 1571300004203,\n"
            + "      \"lastEpochMicrosec\": 1571300004203,\n"
            + "      \"nfNamingCode\": \"1234\",\n"
            + "      \"nfVendorName\": \"VENDORA\",\n"
            + "      \"timeZoneOffset\": \"+00:00\",\n"
            + "      \"version\": \"4.0.1\",\n"
            + "      \"vesEventListenerVersion\":\"7.0.1\"\n"
            + "    },\n"
            + "    \"pnfRegistrationFields\": {\n"
            + "      \"pnfRegistrationFieldsVersion\": \"2.0\",\n"
            + "      \"lastServiceDate\":\"2019-08-16\",\n"
            + "      \"macAddress\":\"02:42:f7:d4:62:ce\",\n"
            + "      \"manufactureDate\":\"2019-08-16\",\n"
            + "      \"modelNumbsdnrer\": \"1234 BestInClass\",\n"
            + "      \"oamV4IpAddress\": \"10.10.10.11\",\n"
            + "	   \"oamPort\":\"17380\",\n"
            //+ "      \"oamV6IpAddress\": \"\",\n"
            + "      \"serialNumber\": \"VENDORA-1234-10.10.10.11-1234 BestInClass\",\n"
            + "      \"softwareVersion\": \"2.3.5\",\n"
            + "      \"unitFamily\": \"VENDORA-1234\",\n"
            + "      \"unitType\": \"1234\",\n"
            + "      \"vendorName\": \"VENDORA\"\n"
            + "    }\n"
            + "  }\n"
            + "}\n"
            + "";
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
    public void processMsgTest() {

        StrimziKafkaPNFRegVESMsgConsumer pnfRegMsgConsumer = new StrimziKafkaPNFRegVESMsgConsumer(cfgTest.getCfg());
        try {
            pnfRegMsgConsumer.processMsg(pnfRegMsg);
            pnfRegMsgConsumer.processMsg(pnfRegMsg_SSH);
            pnfRegMsgConsumer.processMsg(pnfRegMsg_TLS);
            pnfRegMsgConsumer.processMsg(pnfRegMsg_OTHER);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception while processing PNF Registration Message - " + e.getMessage());
        }
    }

    @Test
    public void Test1() {
        StrimziKafkaPNFRegVESMsgConsumer pnfConsumer = new StrimziKafkaPNFRegVESMsgConsumer(cfgTest.getCfg());
        System.out.println(pnfConsumer.getBaseUrl());
        System.out.println(pnfConsumer.getSDNRUser());
        System.out.println(pnfConsumer.getSDNRPasswd());
    }

}
