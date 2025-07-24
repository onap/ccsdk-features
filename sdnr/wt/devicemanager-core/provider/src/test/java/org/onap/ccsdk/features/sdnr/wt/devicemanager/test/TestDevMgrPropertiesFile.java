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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.aaiconnector.impl.config.AaiConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.config.DcaeConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.config.PmConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.toggleAlarmFilter.conf.ToggleAlarmConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDevMgrPropertiesFile {

    private static final Logger LOG = LoggerFactory.getLogger(TestDevMgrPropertiesFile.class);

    private static final File FILENAME = new File("testdevmgrpropertiesfile.properties");
    private static final File AAIPROP_FILE = new File("testdevmgrpropertiesfileaaiclient.properties");
    private int hasChanged;

    @Before
    public void init() {
        delete(FILENAME);
        delete(AAIPROP_FILE);
    }

    @After
    public void deinit() {
        this.init();
    }

    @Test
    public void testBasicConfiguration() {

        writeFile(FILENAME, this.getContent1());
        writeFile(AAIPROP_FILE, this.getAaiPropertiesConfig());

        LOG.info("Read and verify");
        ConfigurationFileRepresentation cfg2 = new ConfigurationFileRepresentation(FILENAME.getPath());

        AaiConfig aaiConfig = new AaiConfig(cfg2);
        assertNotNull(aaiConfig);
        DcaeConfig dcaeConfig = new DcaeConfig(cfg2);
        assertNotNull(dcaeConfig);
        PmConfig pmConfig = new PmConfig(cfg2);
        assertNotNull(pmConfig);
        ToggleAlarmConfig toggleAlarmConfig = new ToggleAlarmConfig(cfg2);
        assertNotNull(toggleAlarmConfig);

        LOG.info("Verify {} ", aaiConfig);
    }

    //-- Observer not working with all testcases, because config does not support different file types.
    @Test
    public void testChangeConfiguration() {

        LOG.info("Read and verify");

        writeFile(FILENAME, this.getContent1());
        writeFile(AAIPROP_FILE, this.getAaiPropertiesConfig());

        ConfigurationFileRepresentation cfg2 = new ConfigurationFileRepresentation(FILENAME.getPath());
        hasChanged = 0;
        cfg2.registerConfigChangedListener(() -> {
            hasChanged++;
            LOG.info("file changed listener triggered: {}", hasChanged);
        });

        AaiConfig aaiConfig = new AaiConfig(cfg2);
        assertNotNull(aaiConfig);
        DcaeConfig dcaeConfig = new DcaeConfig(cfg2);
        assertNotNull(dcaeConfig);
        PmConfig pmConfig = new PmConfig(cfg2);
        assertNotNull(pmConfig);
        ToggleAlarmConfig toggleAlarmConfig = new ToggleAlarmConfig(cfg2);
        assertNotNull(toggleAlarmConfig);

        LOG.info("Write new content. Changes {}", hasChanged);
        writeFile(FILENAME, this.getContent2());

        int i = 10;
        while (hasChanged == 0 && i-- > 0) {
            LOG.info("Wait for Change indication.");
            sleep(1000);
        }
        LOG.info("Changes {}", hasChanged);

        assertTrue("fileChanged counter " + hasChanged, hasChanged > 0);
        LOG.info("Test done");

    }


    private static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void writeFile(File f, String content) {
        try {
            Files.asCharSink(f, StandardCharsets.UTF_8).write(content);
        } catch (IOException e) {
            fail(e.getMessage());
        } ;
        sleep(500);
    }

    private void delete(File f) {
        if (f.exists()) {
            f.delete();
        }
    }


    private String getContent2() {
        // @formatter:off
        return "[dcae]\n" +
                "dcaeUserCredentials=admin:admin\n" +
                "dcaeUrl=http://localhost:45451/abc\n" +
                "dcaeHeartbeatPeriodSeconds=120\n" +
                "dcaeTestCollector=no\n" +
                "\n" +
                "[aots]\n" +
                "userPassword=passwd\n" +
                "soapurladd=off\n" +
                "soapaddtimeout=10\n" +
                "soapinqtimeout=20\n" +
                "userName=user\n" +
                "inqtemplate=inqreq.tmpl.xml\n" +
                "assignedto=userid\n" +
                "addtemplate=addreq.tmpl.xml\n" +
                "severitypassthrough=critical,major,minor,warning\n" +
                "systemuser=user\n" +
                "prt-offset=1200\n" +
                "soapurlinq=off\n" +
                "#smtpHost=\n" +
                "#smtpPort=\n" +
                "#smtpUsername=\n" +
                "#smtpPassword=\n" +
                "#smtpSender=\n" +
                "#smtpReceivers=\n" +
                "\n" +
                "[es]\n" +
                "esCluster=sendateodl5\n" +
                "\n" +
                "[aai]\n" +
                "#keep comment\n" +
                "aaiHeaders=[\"X-TransactionId: 9999\"]\n" +
                "aaiUrl=off\n" +
                "aaiUserCredentials=AAI:AAI\n" +
                "aaiDeleteOnMountpointRemove=true\n" +
                "aaiTrustAllCerts=false\n" +
                "aaiApiVersion=aai/v13\n" +
                "aaiPropertiesFile=aaiclient.properties\n" +
                "aaiApplicationId=SDNR\n" +
                "aaiPcks12ClientCertFile=/opt/logs/externals/data/stores/keystore.client.p12\n" +
                "aaiPcks12ClientCertPassphrase=adminadmin\n" +
                "aaiClientConnectionTimeout=30000\n" +
                "\n" +
                "[pm]\n" +
                "pmCluster=sendateodl5\n" +
                "pmEnabled=true\n" +
                "[toggleAlarmFilter]\n" +
                "taEnabled=true\n" +
                "taDelay=5555\n" +
                "";
        // @formatter:on
    }

    private String getContent1() {
        // @formatter:off
        return "[dcae]\n" +
                "dcaeUserCredentials=admin:admin\n" +
                "dcaeUrl=http://localhost:45/abc\n" +
                "dcaeHeartbeatPeriodSeconds=120\n" +
                "dcaeTestCollector=no\n" +
                "\n" +
                "[aots]\n" +
                "userPassword=passwd\n" +
                "soapurladd=off\n" +
                "soapaddtimeout=10\n" +
                "soapinqtimeout=20\n" +
                "userName=user\n" +
                "inqtemplate=inqreq.tmpl.xml\n" +
                "assignedto=userid\n" +
                "addtemplate=addreq.tmpl.xml\n" +
                "severitypassthrough=critical,major,minor,warning\n" +
                "systemuser=user\n" +
                "prt-offset=1200\n" +
                "soapurlinq=off\n" +
                "#smtpHost=\n" +
                "#smtpPort=\n" +
                "#smtpUsername=\n" +
                "#smtpPassword=\n" +
                "#smtpSender=\n" +
                "#smtpReceivers=\n" +
                "\n" +
                "[es]\n" +
                "esCluster=sendateodl5\n" +
                "\n" +
                "[aai]\n" +
                "#keep comment\n" +
                "aaiHeaders=[\"X-TransactionId: 9999\"]\n" +
                "aaiUrl=off\n" +
                "aaiUserCredentials=AAI:AAI\n" +
                "aaiDeleteOnMountpointRemove=true\n" +
                "aaiTrustAllCerts=false\n" +
                "aaiApiVersion=aai/v13\n" +
                "aaiPropertiesFile=aaiclient.properties\n" +
                "\n" +
                "[pm]\n" +
                "pmCluster=sendateodl5\n" +
                "pmEnabled=true\n" +
                "[toggleAlarmFilter]\n" +
                "taEnabled=false\n" +
                "taDelay=5555\n" +
                "";
        // @formatter:on
    }

    private String getAaiPropertiesConfig() {
        // @formatter:off
        return "org.onap.ccsdk.sli.adaptors.aai.ssl.key=keykey\"\"\n" +
                "org.onap.ccsdk.sli.adaptors.aai.ssl.key.psswd=psswdpsswd\"\"\n" +
                "org.onap.ccsdk.sli.adaptors.aai.host.certificate.ignore=\"false\"\n" +
                "org.onap.ccsdk.sli.adaptors.aai.application=appxyz\"\"\n" +
                "org.onap.ccsdk.sli.adaptors.aai.uri=uriu\"\"\n" +
                "connection.timeout=60000\n" +
                "read.timeout=60000";
        // @formatter:on
    }

}
