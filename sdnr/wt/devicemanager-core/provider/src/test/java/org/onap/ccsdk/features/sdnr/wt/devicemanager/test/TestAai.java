/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
 *  ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.io.Files;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.aaiconnector.impl.AaiProviderClient;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.aaiconnector.impl.config.AaiConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.InventoryInformationDcae;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class TestAai {

    private static final String CONFIGURATIONTESTFILE = "test.properties"; // for
    private static final String ENABLEDAAI_TESTCONFIG_FILENAME = "test2.properties";
    private static final File ENABLEDAAI_TESTCONFIG_FILE = new File(ENABLEDAAI_TESTCONFIG_FILENAME);
    private static final int AAI_SERVER_PORT = 45454;
    private static final String TESTCONFIG_CONTENT = "[dcae]\n" + "dcaeUserCredentials=admin:admin\n" + "dcaeUrl=off\n"
            + "dcaeHeartbeatPeriodSeconds=120\n" + "dcaeTestCollector=no\n" + "\n" + "[aots]\n"
            + "userPassword=passwd\n" + "soapurladd=off\n" + "soapaddtimeout=10\n" + "soapinqtimeout=20\n"
            + "userName=user\n" + "inqtemplate=inqreq.tmpl.xml\n" + "assignedto=userid\n"
            + "addtemplate=addreq.tmpl.xml\n" + "severitypassthrough=critical,major,minor,warning\n"
            + "systemuser=user\n" + "prt-offset=1200\n" + "soapurlinq=off\n" + "#smtpHost=\n" + "#smtpPort=\n"
            + "#smtpUsername=\n" + "#smtpPassword=\n" + "#smtpSender=\n" + "#smtpReceivers=\n" + "\n" + "[es]\n"
            + "esCluster=sendateodl5\n" + "\n" + "[aai]\n" + "#keep comment\n"
            + "aaiHeaders=[\"X-TransactionId: 9999\"]\n" + "aaiUrl=http://localhost:" + AAI_SERVER_PORT + "\n"
            + "aaiUserCredentials=AAI:AAI\n" + "aaiDeleteOnMountpointRemove=true\n" + "aaiTrustAllCerts=false\n"
            + "aaiApiVersion=aai/v13\n" + "aaiPropertiesFile=aaiclient.properties\n" + "aaiApplicationId=SDNR\n"
            + "aaiPcks12ClientCertFile=/opt/logs/externals/data/stores/keystore.client.p12\n"
            + "aaiPcks12ClientCertPassphrase=adminadmin\n" + "aaiClientConnectionTimeout=30000\n" + "\n" + "[pm]\n"
            + "pmCluster=sendateodl5\n" + "pmEnabled=true\n" + "\n" + "";

    private static final String EXT_TEST_URL = "https://testaai.onap.org:8443";
    private static final String EXT_TEST_KEY = "test.key";
    private static final String EXT_TEST_PASSWD = "test123";
    private static final String EXT_TEST_APPLICATIONID = "SDNC";
    private static final long EXT_TEST_CONN_TIMEOUT = 6000;


    private static final CharSequence TESTCONFIG_CONTENT_EXT = "[aai]\n" + "#keep comment\n"
            + "aaiHeaders=[\"X-TransactionId: 9999\"]\n" + "aaiUrl=http://localhost:" + AAI_SERVER_PORT + "\n"
            + "aaiUserCredentials=AAI:AAI\n" + "aaiDeleteOnMountpointRemove=true\n" + "aaiTrustAllCerts=false\n"
            + "aaiApiVersion=aai/v13\n" + "aaiPropertiesFile=aaiclient.properties\n" +
            //            "aaiApplicationId=SDNR\n" +
            //            "aaiPcks12ClientCertFile=/opt/logs/externals/data/stores/keystore.client.p12\n" +
            //            "aaiPcks12ClientCertPassphrase=adminadmin\n" +
            //            "aaiClientConnectionTimeout=30000\n" +
            "\n";
    private static final CharSequence TESTCONFIG_CONTENT_EXT2 = "org.onap.ccsdk.sli.adaptors.aai.ssl.key="
            + EXT_TEST_KEY + "\n" + "org.onap.ccsdk.sli.adaptors.aai.ssl.key.psswd=" + EXT_TEST_PASSWD + "\n"
            + "org.onap.ccsdk.sli.adaptors.aai.host.certificate.ignore=false\n"
            + "org.onap.ccsdk.sli.adaptors.aai.application=" + EXT_TEST_APPLICATIONID + "\n"
            + "org.onap.ccsdk.sli.adaptors.aai.uri=" + EXT_TEST_URL + "\n" + "connection.timeout="
            + EXT_TEST_CONN_TIMEOUT + "\n";
    private static HttpServer server;
    private static ExecutorService httpThreadPool;
    private static ConfigurationFileRepresentation globalCfg;
    public static File getFile(Object o, String fileName) {
        ClassLoader classLoader = o.getClass().getClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }
    @Test
    public void test() {

        String testConfigurationFileName = getFile(this, CONFIGURATIONTESTFILE).getAbsolutePath();
        ConfigurationFileRepresentation cfg = new ConfigurationFileRepresentation(testConfigurationFileName);

        AaiProviderClient provider = new AaiProviderClient(cfg, null);

        String mountPointName = "testDevice 01";
        String type = "Unit";
        String model = "Horizon Compact+";
        String vendor = "DragonWave-X";
        String ipv4 = "127.0.0.1";
        String ipv6 = "::1";
        List<String> ifInfos = new ArrayList<>();
        ifInfos.add("LP-MWPS-RADIO");
        InventoryInformationDcae ii = new InventoryInformationDcae(type, model, vendor, ipv4, ipv6, ifInfos);
        System.out.println("registering device");
        provider.onDeviceRegistered(new NodeId(mountPointName), ii);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("Break sleep : " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        System.out.println("unregistering device");
        provider.onDeviceUnregistered(new NodeId(mountPointName));
        System.out.println("finished");
        try {
            provider.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            System.out.println("Break sleep : " + e1.getMessage());
            Thread.currentThread().interrupt();
        }
        AaiProviderClient provider = new AaiProviderClient(globalCfg, null);

        String mountPointName = "testDevice 01";
        String type = "Unit";
        String model = "Horizon Compact+";
        String vendor = "DragonWave-X";
        String ipv4 = "127.0.0.1";
        String ipv6 = "::1";
        List<String> ifInfos = new ArrayList<>();
        ifInfos.add("LP-MWPS-RADIO");
        InventoryInformationDcae ii = new InventoryInformationDcae(type, model, vendor, ipv4, ipv6, ifInfos);
        System.out.println("registering device");
        provider.onDeviceRegistered(new NodeId(mountPointName));
        provider.onDeviceRegistered(new NodeId(mountPointName), ii);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("Break sleep : " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        System.out.println("unregistering device");
        provider.onDeviceUnregistered(new NodeId(mountPointName));
        System.out.println("finished");
        try {
            provider.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExtendedProperties() {
        File testConfigurationFileName = new File("abbsads.properties");
        File extfile = new File("aaiclient.properties");
        if (testConfigurationFileName.exists()) {
            testConfigurationFileName.delete();
        }
        if (extfile.exists()) {
            extfile.delete();
        }
        try {
            Files.asCharSink(testConfigurationFileName, StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT_EXT);
        } catch (IOException e) {
            e.printStackTrace();
            fail("problem writing a test config file: " + e.getMessage());
        }
        try {
            Files.asCharSink(extfile, StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT_EXT2);
        } catch (IOException e) {
            e.printStackTrace();
            fail("problem writing a second test config file: " + e.getMessage());
        }

        ConfigurationFileRepresentation cfg = new ConfigurationFileRepresentation(testConfigurationFileName);
        AaiConfig config = new AaiConfig(cfg);
        System.out.println(config.toString());

        assertTrue(config.getBaseUrl().startsWith(EXT_TEST_URL));
        assertEquals(EXT_TEST_KEY, config.getPcks12CertificateFilename());
        assertEquals(EXT_TEST_PASSWD, config.getPcks12CertificatePassphrase());
        assertEquals(EXT_TEST_APPLICATIONID, config.getHeaders().get("X-FromAppId"));
        assertEquals(EXT_TEST_CONN_TIMEOUT, config.getConnectionTimeout());

        if (testConfigurationFileName.exists()) {
            testConfigurationFileName.delete();
        }
        if (extfile.exists()) {
            extfile.delete();
        }

    }

    @BeforeClass
    public static void initAaiTestWebserver() throws IOException {
        try {
            Files.asCharSink(ENABLEDAAI_TESTCONFIG_FILE, StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT);
        } catch (IOException e1) {
            fail(e1.getMessage());
        }
        //globalCfg=HtDevicemanagerConfiguration.getTestConfiguration(ENABLEDAAI_TESTCONFIG_FILENAME,true);
        globalCfg = new ConfigurationFileRepresentation(ENABLEDAAI_TESTCONFIG_FILENAME);
        server = HttpServer.create(new InetSocketAddress(AAI_SERVER_PORT), 0);
        httpThreadPool = Executors.newFixedThreadPool(5);
        server.setExecutor(httpThreadPool);
        AaiConfig config = new AaiConfig(globalCfg);
        server.createContext(config.getBaseUri(), new MyHandler());
        //server.createContext("/", new MyRootHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("http server started");
    }

    @AfterClass
    public static void stopTestWebserver() {
        if (server != null) {
            server.stop(0);
            httpThreadPool.shutdownNow();
            System.out.println("http server stopped");
        }
        if (ENABLEDAAI_TESTCONFIG_FILE.exists()) {
            ENABLEDAAI_TESTCONFIG_FILE.delete();
        }

    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            System.out.println("req method: " + method);
            OutputStream os = null;
            try {
                String res = "";
                if (method.equals("GET")) {
                    t.sendResponseHeaders(404, res.length());
                    os = t.getResponseBody();
                    os.write(res.getBytes());
                } else if (method.equals("DELETE")) {
                    t.sendResponseHeaders(200, res.length());
                    os = t.getResponseBody();
                    os.write(res.getBytes());
                } else if (method.equals("PUT")) {
                    t.sendResponseHeaders(200, res.length());
                    os = t.getResponseBody();
                    os.write(res.getBytes());
                } else {
                    t.sendResponseHeaders(404, 0);
                }
                System.out.println("req handled successful");

            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (os != null) {
                    os.close();
                }
            }
        }
    }
}
