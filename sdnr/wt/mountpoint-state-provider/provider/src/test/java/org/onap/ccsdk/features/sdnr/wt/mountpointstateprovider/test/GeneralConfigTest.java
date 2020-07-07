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

package org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.GeneralConfig;

import com.google.common.io.Files;

public class GeneralConfigTest {

    private static final String TESTCONFIG_CONTENT =
            "[general]\n" + "dmaapEnabled=false\n" + "TransportType=HTTPNOAUTH\n" + "host=onap-dmap:3904\n"
                    + "topic=unauthenticated.SDNR_MOUNTPOINT_STATE_INFO\n" + "contenttype=application/json\n"
                    + "timeout=20000\n" + "limit=10000\n" + "maxBatchSize=100\n" + "maxAgeMs=250\n"
                    + "MessageSentThreadOccurance=50\n";

    private ConfigurationFileRepresentation globalCfg;

    @Test
    public void test() {
        try {
            Files.asCharSink(new File("test.properties"), StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT);
            globalCfg = new ConfigurationFileRepresentation("test.properties");
            GeneralConfig cfg = new GeneralConfig(globalCfg);
            assertEquals("onap-dmap:3904", cfg.getHostPort());
            assertEquals(false, cfg.getEnabled());
            assertEquals("unauthenticated.SDNR_MOUNTPOINT_STATE_INFO", cfg.getTopic());
            assertEquals("application/json", cfg.getContenttype());
            assertEquals("20000", cfg.getTimeout());
            assertEquals("10000", cfg.getLimit());
            assertEquals("100", cfg.getMaxBatchSize());
            assertEquals("250", cfg.getMaxAgeMs());
            assertEquals("50", cfg.getMessageSentThreadOccurrence());
            assertEquals("HTTPNOAUTH", cfg.getTransportType());
            assertEquals("general", cfg.getSectionName());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @After
    public void cleanUp() {
        File file = new File("test.properties");
        if (file.exists()) {
            System.out.println("File exists, Deleting it");
            file.delete();
        }

    }
}
