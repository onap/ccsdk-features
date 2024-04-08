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

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.StrimziKafkaConfig;

public class TestStrimziKafkaConfig {

    // @formatter:off
    private static final String TESTCONFIG_CONTENT = "[strimzi-kafka]\n"
            + "strimziEnabled=false\n"
            + "bootstrapServers=onap-strimzi-kafka-0:9094,onap-strimzi-kafka-1:9094\n"
            + "securityProtocol=PLAINTEXT\n"
            + "saslMechanism=PLAIN\n"
            + "saslJaasConfig=PLAIN\n"
            + "";
     // @formatter:on

    private StrimziKafkaConfig sKafkaCfg;
    private static final String CONFIGURATIONFILE = "test2.properties";

    public TestStrimziKafkaConfig(String filename) throws IOException {
        Files.asCharSink(new File(filename), StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT);
        ConfigurationFileRepresentation globalCfg = new ConfigurationFileRepresentation(filename);
        this.sKafkaCfg = new StrimziKafkaConfig(globalCfg);
    }

    public StrimziKafkaConfig getCfg() {
        return sKafkaCfg;
    }

    //@Test
    public void test() throws IOException {
        new TestStrimziKafkaConfig(CONFIGURATIONFILE);
        assertEquals("strimzi-kafka", getCfg().getSectionName());
        assertEquals("onap-strimzi-kafka-0:9094,onap-strimzi-kafka-1:9094", getCfg().getBootstrapServers());
        assertEquals("PLAINTEXT", getCfg().getSecurityProtocol());
        assertEquals(false, getCfg().getEnabled());
        assertEquals("PLAIN", getCfg().getSaslJaasConfig());
        assertEquals("PLAIN", getCfg().getSaslMechanism());
    }

    //@After
    public void cleanUp() {
        File file = new File(CONFIGURATIONFILE);
        if (file.exists()) {
            System.out.println("File exists, Deleting it");
            file.delete();
        }

    }

}
