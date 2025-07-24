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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.dblib.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section.EnvGetter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBConfig;

public class TestConfig {


    private static final String TEST_CONFIG1 = "test.properties";
    private static final String TEST_CONFIG2 = "test2.properties";
    private static final String TEST_CONFIG3 = "test3.properties";
    protected static final String SDNR_CONTROLLERID_ENV_VALUE = "903f3091-24f6-11ec-9cc3-0242ac130003";
    private static final String CONTENT_CONTROLLER_NULL = "dbType=${SDNRDBTYPE}\n"
            + "\n"
            + "[mariadb]\n"
            + "url=${SDNRDBURL}\n"
            + "username=${SDNRDBUSERNAME}\n"
            + "password=${SDNRDBPASSWORD}\n"
            + "controllerId=null\n"
            + "suffix=-v6";

    @Test
    public void testEnvControllerId() {

        ConfigurationFileRepresentation cfg = new ConfigurationFileRepresentation(TEST_CONFIG1);
        Section.setEnvGetter(new EnvGetter() {

            @Override
            public String getenv(String substring) {
                if("SDNRCONTROLLERID".equals(substring)) {
                    return SDNR_CONTROLLERID_ENV_VALUE;
                }
                return "";
            }
        });
        SqlDBConfig config = new SqlDBConfig(cfg);
        assertEquals(SDNR_CONTROLLERID_ENV_VALUE, config.getControllerId());
    }

    @Test
    public void testGeneratedControllerId() {
        ConfigurationFileRepresentation cfg = new ConfigurationFileRepresentation(TEST_CONFIG2);
        Section.setEnvGetter(new EnvGetter() {

            @Override
            public String getenv(String substring) {
                return "";
            }
        });
        SqlDBConfig config = new SqlDBConfig(cfg);
        final String controllerId = config.getControllerId();
        assertNotNull(controllerId);
        assertFalse(controllerId.isBlank());
        final String controllerId2 = config.getControllerId();
        assertEquals(controllerId, controllerId2);
    }

    @Test
    public void testNullControllerId() throws IOException {
        Files.asCharSink(new File(TEST_CONFIG3), StandardCharsets.UTF_8).write(CONTENT_CONTROLLER_NULL);
        ConfigurationFileRepresentation cfg = new ConfigurationFileRepresentation(TEST_CONFIG3);
        Section.setEnvGetter(new EnvGetter() {

            @Override
            public String getenv(String substring) {
                return "";
            }
        });
        SqlDBConfig config = new SqlDBConfig(cfg);
        final String controllerId = config.getControllerId();
        assertNull(controllerId);
        final String controllerId2 = config.getControllerId();
        assertNull(controllerId2);
    }

    @Before
    @After
    public void after() throws InterruptedException, IOException {

        delete(new File(TEST_CONFIG1));
        delete(new File(TEST_CONFIG2));
        delete(new File(TEST_CONFIG3));

    }

    private static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (f.exists()) {
            if (!f.delete()) {
                throw new FileNotFoundException("Failed to delete file: " + f);
            }
        }
    }
}
