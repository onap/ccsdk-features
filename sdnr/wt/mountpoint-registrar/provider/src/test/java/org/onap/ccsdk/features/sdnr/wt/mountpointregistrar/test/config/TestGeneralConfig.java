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
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.GeneralConfig;

public class TestGeneralConfig {

    private static final String CONFIGURATIONFILE = "test1.properties";

    private GeneralConfigForTest config;

    @Before
    public void before() throws IOException {
        config = new GeneralConfigForTest(CONFIGURATIONFILE);
    }

    @Test
    public void test() throws IOException {
            GeneralConfig cfg = config.getCfg();

            assertEquals("http://localhost:8181", cfg.getBaseUrl());
            assertEquals("admin", cfg.getSDNRUser());
            assertEquals("admin", cfg.getSDNRPasswd());
            assertEquals("general", cfg.getSectionName());
    }

    @After
    public void cleanUp() {
        config.close();
    }
}
