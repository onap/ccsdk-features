/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.test;

import java.io.IOException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.Config;

public class TestConfig {

    public static String TEST_CONFIG_FILENAME = "src/test/resources/test.config.json";
    public static String TEST_OOMCONFIG_FILENAME = "src/test/resources/oom.test.config.json";
    @Test
    public void test() throws IOException {

        Config config = Config.load(TEST_CONFIG_FILENAME);
        System.out.println("config="+config);
    }
    @Test
    public void testOom() throws IOException {

        Config config = Config.load(TEST_OOMCONFIG_FILENAME);
        System.out.println("config="+config);

    }
}
