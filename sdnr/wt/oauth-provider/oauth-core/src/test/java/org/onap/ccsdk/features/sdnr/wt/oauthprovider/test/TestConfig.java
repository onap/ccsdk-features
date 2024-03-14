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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.Config;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.InvalidConfigurationException;

public class TestConfig {

    public static String TEST_CONFIG_FILENAME = "src/test/resources/test.config.json";
    public static String TEST_OOMCONFIG_FILENAME = "src/test/resources/oom.test.config.json";
    public static String TEST_RS256_FILENAME = "src/test/resources/test.configRS256.json";
    public static String TEST_RS256INVALID_FILENAME = "src/test/resources/test.configRS256-invalid.json";
    public static String TEST_RS512_FILENAME = "src/test/resources/test.configRS512.json";


    @Test
    public void test() throws IOException, InvalidConfigurationException {

        Config config = Config.load(TEST_CONFIG_FILENAME);
        System.out.println("config="+config);
        assertEquals(60*60,config.getTokenLifetime());
        assertNotNull(config.getAlgorithm());
        assertNotNull(config.getTokenSecret());
        //assertNotNull(config.getPublicKey());
        assertEquals(Config.TOKENALG_HS256, config.getAlgorithm());
    }
    @Test
    public void testOom() throws IOException, InvalidConfigurationException {

        Config config = Config.load(TEST_OOMCONFIG_FILENAME);
        System.out.println("config="+config);
        assertEquals(30*60,config.getTokenLifetime());

    }
    @Test
    public void testRS256() throws IOException, InvalidConfigurationException {

        Config config = Config.load(TEST_RS256_FILENAME);
        System.out.println("config="+config);
        assertEquals(60*60,config.getTokenLifetime());

    }
    @Test
    public void testRS512() throws IOException, InvalidConfigurationException {

        Config config = Config.load(TEST_RS512_FILENAME);
        System.out.println("config="+config);
        assertEquals(60*60,config.getTokenLifetime());

    }
    @Test(expected = InvalidConfigurationException.class)
    public void testRS256Invalid() throws IOException, InvalidConfigurationException {

        Config.load(TEST_RS256INVALID_FILENAME);
    }
}
