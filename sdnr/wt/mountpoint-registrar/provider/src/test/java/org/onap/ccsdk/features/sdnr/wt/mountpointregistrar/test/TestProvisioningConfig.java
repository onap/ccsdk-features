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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test;

import static org.junit.Assert.assertEquals;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.ProvisioningConfig;

public class TestProvisioningConfig {

    private static final String TESTCONFIG_CONTENT = "[provisioning]\n"
            + "TransportType=HTTPNOAUTH\n"
            + "Protocol=http\n"
            + "username=username\n"
            + "password=password\n"
            + "host=onap-dmap:3904\n"
            + "topic=unauthenticated.SEC_3GPP_PROVISIONING_OUTPUT\n"
            + "contenttype=application/json\n"
            + "group=myG\n"
            + "id=C1\n"
            + "timeout=20000\n"
            + "limit=10000\n"
            + "fetchPause=5000\n"
            + "jersey.config.client.readTimeout=25000\n"
            + "jersey.config.client.connectTimeout=25000\n"
            + "jersey.config.client.proxy.uri=http://http-proxy\n"
            + "jersey.config.client.proxy.username=proxy-user\n"
            + "jersey.config.client.proxy.password=proxy-password\n"
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
        assertEquals("HTTPNOAUTH", provisioningConfig.getTransportType());
        assertEquals("onap-dmap:3904", provisioningConfig.getHostPort());
        assertEquals("unauthenticated.SEC_3GPP_PROVISIONING_OUTPUT", provisioningConfig.getTopic());
        assertEquals("application/json", provisioningConfig.getContenttype());
        assertEquals("myG", provisioningConfig.getConsumerGroup());
        assertEquals("C1", provisioningConfig.getConsumerId());
        assertEquals("20000", provisioningConfig.getTimeout());
        assertEquals("10000", provisioningConfig.getLimit());
        assertEquals("5000", provisioningConfig.getFetchPause());
        assertEquals("http", provisioningConfig.getProtocol());
        assertEquals("username", provisioningConfig.getUsername());
        assertEquals("password", provisioningConfig.getPassword());
        assertEquals("25000", provisioningConfig.getClientReadTimeout());
        assertEquals("25000", provisioningConfig.getClientConnectTimeout());
        assertEquals("http://http-proxy", provisioningConfig.getHTTPProxyURI());
        assertEquals("proxy-user", provisioningConfig.getHTTPProxyUsername());
        assertEquals("proxy-password", provisioningConfig.getHTTPProxyPassword());
    }

    @After
    public void cleanUp() {
        if (configFile.exists()) {
            System.out.println(String.format("File %s exists, deleting it", configFile.getName()));
            configFile.delete();
        }
    }

}
