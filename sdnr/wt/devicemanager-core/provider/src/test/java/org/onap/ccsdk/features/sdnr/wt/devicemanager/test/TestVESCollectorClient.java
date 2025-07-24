/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESMessage;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.vescollectorconnector.impl.VESCollectorServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.vescollectorconnector.impl.config.VESCollectorCfgImpl;

public class TestVESCollectorClient {

    private static final String TESTCONFIG_CONTENT_NO_AUTH = "[VESCollector]\n" + "VES_COLLECTOR_IP=127.0.0.1\n"
            + "VES_COLLECTOR_PORT=8080\n" + "VES_COLLECTOR_TLS_ENABLED=false\n" + "VES_COLLECTOR_USERNAME=sample1\n"
            + "VES_COLLECTOR_PASSWORD=sample1\n" + "VES_COLLECTOR_VERSION=v7\n" + "REPORTING_ENTITY_NAME=ONAP SDN-R\n" + "";

    private static final String TESTCONFIG_CONTENT_AUTH = "[VESCollector]\n" + "VES_COLLECTOR_IP=127.0.0.1\n"
            + "VES_COLLECTOR_PORT=8080\n" + "VES_COLLECTOR_TLS_ENABLED=true\n" + "VES_COLLECTOR_USERNAME=sample1\n"
            + "VES_COLLECTOR_PASSWORD=sample1\n" + "VES_COLLECTOR_VERSION=v7\n" + "REPORTING_ENTITY_NAME=ONAP SDN-R\n" + "";

    private static final VESMessage message = new VESMessage("Test Message");
    private static final String CONFIG_FILE = "test.properties";
    private static final String CONFIG_FILE2 = "test2.properties";

    @Test
    public void testNoAuth() throws Exception {
        ConfigurationFileRepresentation vesCfg;
        VESCollectorServiceImpl vesClient;

        Files.asCharSink(new File(CONFIG_FILE), StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT_NO_AUTH);
        vesCfg = new ConfigurationFileRepresentation(CONFIG_FILE);
        vesClient = new VESCollectorServiceImpl(vesCfg);

        vesClient.publishVESMessage(message);
        vesClient.close();

    }

    @Test
    public void testAuth() throws Exception {
        ConfigurationFileRepresentation vesCfg;
        VESCollectorServiceImpl vesClient;

        Files.asCharSink(new File("test.properties"), StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT_AUTH);
        vesCfg = new ConfigurationFileRepresentation("test.properties");

        vesClient = new VESCollectorServiceImpl(vesCfg);
        vesClient.publishVESMessage(message);
        vesClient.close();
    }

    @Test
    public void testDefaultConfigValues() throws IOException {
        Files.asCharSink(new File(CONFIG_FILE2), StandardCharsets.UTF_8).write("");
        ConfigurationFileRepresentation cfg = new ConfigurationFileRepresentation(CONFIG_FILE2);
        VESCollectorCfgImpl vesConfig = new VESCollectorCfgImpl(cfg);
        assertEquals("ONAP SDN-R", vesConfig.getReportingEntityName());
        assertEquals("SHORT", vesConfig.getEventLogMsgDetail());
        assertEquals("v7",vesConfig.getVersion());
        assertFalse(vesConfig.isVESCollectorEnabled());
        assertFalse(vesConfig.isTrustAllCerts());

    }


    @Before
    @After
    public void after() throws InterruptedException, IOException {

        delete(new File(CONFIG_FILE));
        delete(new File(CONFIG_FILE2));

    }

    private static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if(f.exists()) {
            if (!f.delete()) {
                throw new FileNotFoundException("Failed to delete file: " + f);
            }
        }
    }
}
