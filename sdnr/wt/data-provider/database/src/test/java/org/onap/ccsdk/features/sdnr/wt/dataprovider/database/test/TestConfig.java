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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.test;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.database.config.EsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConfig {

    private static final Logger LOG = LoggerFactory.getLogger(TestConfig.class);

    private static final String TESTFILENAME = "testconfig.properties";

    @After
    @Before
    public void afterAndBefore() {
        File f=new File(TESTFILENAME);
        if(f.exists()) {
            LOG.info("Remove {}", f.getAbsolutePath());
            f.delete();
        }
    }
    @Test
    public void test() {
        ConfigurationFileRepresentation configuration=new ConfigurationFileRepresentation(TESTFILENAME);

        EsConfig esConfig = new EsConfig(configuration);
        LOG.info("Defaultconfiguration: {}", esConfig.toString());
        assertEquals("http", esConfig.getHosts()[0].protocol.getValue());
        assertEquals(9200, esConfig.getHosts()[0].port);
        assertEquals("sdnrdb", esConfig.getHosts()[0].hostname);

    }

}
