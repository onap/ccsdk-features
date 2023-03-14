/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.ProvisioningConfig;

public class TestProvisioningConfig {

    private static final String TESTCONFIG_CONTENT = "[provisioning]\n"

            + "topic=unauthenticated.SEC_3GPP_PROVISIONING_OUTPUT\n"
            + "consumerGroup=myG\n"
            + "consumerID=C1\n"
            + "timeout=20000\n"
            + "limit=10000\n"
            + "fetchPause=5000\n"
            + "";

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static File configFile;

    @Test
    public void testConfigValuesAssignment() throws IOException {
        configFile = new File(TEMP_DIR, "test.properties");
        Files.asCharSink(configFile, StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT);
        ConfigurationFileRepresentation cfg = new ConfigurationFileRepresentation(configFile);
        ProvisioningConfig provisioningConfig = new ProvisioningConfig(cfg);
        assertEquals("provisioning", provisioningConfig.getSectionName());
        assertEquals("unauthenticated.SEC_3GPP_PROVISIONING_OUTPUT", provisioningConfig.getTopic());
        assertEquals("myG", provisioningConfig.getConsumerGroup());
        assertEquals("C1", provisioningConfig.getConsumerId());
        assertEquals("20000", provisioningConfig.getTimeout());
        assertEquals("10000", provisioningConfig.getLimit());
        assertEquals("5000", provisioningConfig.getFetchPause());
    }

    @After
    public void cleanUp() {
        if (configFile.exists()) {
            System.out.println(String.format("File %s exists, deleting it", configFile.getName()));
            configFile.delete();
        }
    }

}
