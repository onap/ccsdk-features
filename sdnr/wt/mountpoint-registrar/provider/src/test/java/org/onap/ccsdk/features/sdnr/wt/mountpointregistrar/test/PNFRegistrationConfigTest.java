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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test;

import static org.junit.Assert.assertEquals;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.PNFRegistrationConfig;

public class PNFRegistrationConfigTest {

    // @formatter:off
    private static final String TESTCONFIG_CONTENT = "[pnfRegistration]\n"
            + "TransportType=HTTPNOAUTH\n"
            + "Protocol=http\n"
            + "username=username\n"
            + "password=password\n"
            + "host=onap-dmap:3904\n"
            + "topic=unauthenticated.VES_PNFREG_OUTPUT\n"
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
    // @formatter:on
    private ConfigurationFileRepresentation cfg;
    private static final String configFile = "test.properties";

    @Test
    public void test() {
        try {
            Files.asCharSink(new File(configFile), StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT);
            cfg = new ConfigurationFileRepresentation(configFile);
            PNFRegistrationConfig pnfCfg = new PNFRegistrationConfig(cfg);
            assertEquals("pnfRegistration", pnfCfg.getSectionName());
            assertEquals("HTTPNOAUTH", pnfCfg.getTransportType());
            assertEquals("onap-dmap:3904", pnfCfg.getHostPort());
            assertEquals("unauthenticated.VES_PNFREG_OUTPUT", pnfCfg.getTopic());
            assertEquals("application/json", pnfCfg.getContenttype());
            assertEquals("myG", pnfCfg.getConsumerGroup());
            assertEquals("C1", pnfCfg.getConsumerId());
            assertEquals("20000", pnfCfg.getTimeout());
            assertEquals("10000", pnfCfg.getLimit());
            assertEquals("5000", pnfCfg.getFetchPause());
            assertEquals("http", pnfCfg.getProtocol());
            assertEquals("username", pnfCfg.getUsername());
            assertEquals("password", pnfCfg.getPassword());
            assertEquals("25000", pnfCfg.getClientReadTimeout());
            assertEquals("25000", pnfCfg.getClientConnectTimeout());
            assertEquals("http://http-proxy", pnfCfg.getHTTPProxyURI());
            assertEquals("proxy-user", pnfCfg.getHTTPProxyUsername());
            assertEquals("proxy-password", pnfCfg.getHTTPProxyPassword());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @After
    public void cleanUp() {
        File file = new File(configFile);
        if (file.exists()) {
            System.out.println("File exists, Deleting it");
            file.delete();
        }

    }

}
