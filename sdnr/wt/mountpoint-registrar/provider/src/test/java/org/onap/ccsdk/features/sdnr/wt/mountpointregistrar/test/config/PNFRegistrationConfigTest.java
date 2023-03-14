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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test.config;

import static org.junit.Assert.assertEquals;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.PNFRegistrationConfig;

public class PNFRegistrationConfigTest {

    // @formatter:off
    private static final String TESTCONFIG_CONTENT = "[pnfRegistration]\n"
            + "topic=unauthenticated.VES_PNFREG_OUTPUT\n"
            + "consumerGroup=myG\n"
            + "consumerID=C1\n"
            + "timeout=20000\n"
            + "limit=10000\n"
            + "fetchPause=5000\n"
            + "";
    // @formatter:on
    private ConfigurationFileRepresentation cfg;
    private static final String configFile = "test.properties";

    @Test
    public void test() {
        try {
            Files.asCharSink(new File(configFile), StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT);
            cfg = new ConfigurationFileRepresentation(configFile);
            PNFRegistrationConfig pnfCfg = new PNFRegistrationConfig(cfg);
            assertEquals("pnfRegistration", pnfCfg.getSectionName());
            assertEquals("unauthenticated.VES_PNFREG_OUTPUT", pnfCfg.getTopic());
            assertEquals("myG", pnfCfg.getConsumerGroup());
            assertEquals("C1", pnfCfg.getConsumerId());
            assertEquals("20000", pnfCfg.getTimeout());
            assertEquals("10000", pnfCfg.getLimit());
            assertEquals("5000", pnfCfg.getFetchPause());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @After
    public void cleanUp() {
        File file = new File(configFile);
        if (file.exists()) {
            System.out.println("File exists, Deleting it");
            file.delete();
        }

    }

}
