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

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.DMaaPVESMsgConsumer;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.DMaaPVESMsgConsumerMain;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.FaultConfig;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.GeneralConfig;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.PNFRegistrationConfig;

import com.google.common.io.Files;

public class TestDMaaPVESMsgConsumerMain {

    private static final String CONFIGURATIONFILE = "test.properties";
    private static final String TESTCONFIG_GENERAL = "[general]\n" + "dmaapEnabled=false\n"
            + "baseUrl=http://localhost:8181\n" + "sdnrUser=admin\n" + "sdnrPasswd=admin\n" + "\n"
            + "[pnfRegistration]\n"
            + "pnfRegConsumerClass=org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test.impl.DummyPNFRegVESMsgConsumer\n"
            + "TransportType=HTTPNOAUTH\n" + "host=onap-dmap:3904\n" + "topic=unauthenticated.VES_PNFREG_OUTPUT\n"
            + "contenttype=application/json\n" + "group=myG\n" + "id=C1\n" + "timeout=20000\n" + "limit=10000\n" + "\n"
            + "[fault]\n"
            + "faultConsumerClass=org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test.impl.DummyFaultVESMsgConsumer\n"
            + "TransportType=HTTPNOAUTH\n" + "host=onap-dmap:3904\n" + "topic=unauthenticated.SEC_FAULT_OUTPUT\n"
            + "contenttype=application/json\n" + "group=myG\n" + "id=C1\n" + "timeout=20000\n" + "limit=10000\n"
            + "fetchPause=10000\n" + "\n" + "";

    private static final String TESTCONFIG_GENERAL_INVALID = "[general]\n" + "dmaapEnabled=false\n"
            + "baseUrl=http://localhost:8181\n" + "sdnrUser=admin\n" + "sdnrPasswd=admin\n" + "\n"
            + "[pnfRegistration]\n"
            + "pnfRegConsumerClass=org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test.impl.DummyPNFRegVESMsgConsumer\n"
            + "TransportType=HTTPNOAUTH\n" + "host=onap-dmap:3904\n" + "topic=unauthenticated.VES_PNFREG_OUTPUT\n"
            + "contenttype=application/json\n" + "group=myG\n" + "id=C1\n" + "timeout=20000\n" + "limit=10000\n" + "\n"
            + "[fault]\n"
            + "faultConsumerClass=org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test.impl.DummyFaultVESMsgConsumer\n"
            + "TransportType=HTTPNOAUTH\n" + "host=onap-dmap:3904\n" + "topic=unauthenticated.SEC_FAULT_OUTPUT\n"
            + "contenttype=application/json\n" + "group=myG\n" + "id=C1\n" + "timeout=HELLO\n" + "limit=10000\n"
            + "fetchPause=WORLD\n" + "\n" + "";
    public GeneralConfig generalConfig;
    Map<String, Configuration> configMap = new HashMap<String, Configuration>();
    DMaaPVESMsgConsumerMain dmaapMain;

    //	@Before
    public void preTest1() {
        try {
            Files.asCharSink(new File(CONFIGURATIONFILE), StandardCharsets.UTF_8).write(TESTCONFIG_GENERAL);
            ConfigurationFileRepresentation configFileRepresentation =
                    new ConfigurationFileRepresentation(CONFIGURATIONFILE);

            generalConfig = new GeneralConfig(configFileRepresentation);
            PNFRegistrationConfig pnfRegConfig = new PNFRegistrationConfig(configFileRepresentation);
            FaultConfig faultConfig = new FaultConfig(configFileRepresentation);

            configMap.put("pnfRegistration", pnfRegConfig);
            configMap.put("fault", faultConfig);
        } catch (Exception e) {
            System.out.println("Failed in preTest execution " + e.getMessage());
        }
    }

    public void preTest2() {
        try {
            Files.asCharSink(new File(CONFIGURATIONFILE), StandardCharsets.UTF_8).write(TESTCONFIG_GENERAL_INVALID);
            ConfigurationFileRepresentation configFileRepresentation =
                    new ConfigurationFileRepresentation(CONFIGURATIONFILE);

            generalConfig = new GeneralConfig(configFileRepresentation);
            PNFRegistrationConfig pnfRegConfig = new PNFRegistrationConfig(configFileRepresentation);
            FaultConfig faultConfig = new FaultConfig(configFileRepresentation);

            configMap.put("pnfRegistration", pnfRegConfig);
            configMap.put("fault", faultConfig);
        } catch (Exception e) {
            System.out.println("Failed in preTest execution " + e.getMessage());
        }
    }

    @Test
    public void testDMaaPVESMsgConsumerMainMapOfStringConfiguration() {
        preTest1();
        assertNotNull(configMap);
        dmaapMain = new DMaaPVESMsgConsumerMain(configMap);
    }

    @Test
    public void testDMaaPVESMsgConsumerMainMapOfStringConfiguration1() {
        preTest2();
        assertNotNull(configMap);
        dmaapMain = new DMaaPVESMsgConsumerMain(configMap);
    }

    @After
    public void postTest() {
        File file = new File("test.properties");
        if (file.exists()) {
            System.out.println("File exists, Deleting it");
            file.delete();
        }
        List<DMaaPVESMsgConsumer> consumers = DMaaPVESMsgConsumerMain.getConsumers();
        for (DMaaPVESMsgConsumer consumer : consumers) {
            // stop all consumers
            consumer.stopConsumer();
        }
    }
}


