/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import java.io.File;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.exception.ConfigurationException;
import org.onap.ccsdk.features.sdnr.wt.common.util.ResourceFileLoader;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.EsConfig;

public class TestConfiguration {

    private static final String CONFIGURATIONTESTFILE = "test.properties"; // for

    @Test
    public void test1() throws ConfigurationException {

        System.out.println("Configuration file " + CONFIGURATIONTESTFILE);

        File testConfigurationFile = ResourceFileLoader.getFile(this, CONFIGURATIONTESTFILE);
        System.out.println("Located at: " + testConfigurationFile.getAbsolutePath());

        ConfigurationFileRepresentation configuration = new ConfigurationFileRepresentation(testConfigurationFile);

        System.out.println("Configuration: " + configuration.getSection(EsConfig.SECTION_MARKER_ES));
        EsConfig esConfig1 = new EsConfig(configuration);

        System.out.println("ES config getArchiveLifetimeSeconds: " + esConfig1.getArchiveLifetimeSeconds());


        // fail("Not yet implemented");
    }

    @Test
    public void test2() {

    }


}
