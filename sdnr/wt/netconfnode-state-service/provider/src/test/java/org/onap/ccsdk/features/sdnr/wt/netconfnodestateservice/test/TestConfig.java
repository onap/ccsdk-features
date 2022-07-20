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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test;

import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section.EnvGetter;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.conf.NetconfStateConfig;

public class TestConfig {

    private static final String FILENAME="test.config";
    @BeforeClass
    @AfterClass
    public static void clearFiles(){
        try {
            Files.deleteIfExists(new File(FILENAME).toPath());
        } catch (IOException e) {

        }
    }
    @Test
    public void test() {

        ConfigurationFileRepresentation cfg = new ConfigurationFileRepresentation(FILENAME);
        NetconfStateConfig config = new NetconfStateConfig(cfg);

        Section.setEnvGetter(new EnvGetter() {

            @Override
            public String getenv(String env) {
                if("SDNR_ASYNC_HANDLING".equals(env)) {
                    return "true";
                }
                return null;
            }
        });
        assertTrue(config.handleAsync());
    }
}
