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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.test.JSONAssert;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentData;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ConfigData;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ConfigName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DataContainer;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DataMigrationReport;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.MavenDatabasePluginInitFile;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;

/**
 * @author Michael Dürre
 *
 */
public class TestData {
    // @formatter:off 
    private static final JSONObject EVENTLOG_SEARCHHIT = new JSONObject("{\n"
            + "\"_index\": \"sdnevents_v1\",\n"
            + "\"_type\": \"eventlog\",\n"
            + "\"_id\": \"AXB7cJHlZ_FApnwi29xq\",\n"
            + "\"_version\": 1,\n"
            + "\"_score\": 1,\n"
            + "\"_source\": {\n"
            + "\"event\": {\n"
            + "\"nodeName\": \"SDN-Controller-465e2ae306ca\",\n"
            + "\"counter\": \"1\",\n"
            + "\"timeStamp\": \"2020-02-25T08:22:19.8Z\",\n"
            + "\"objectId\": \"sim2230\",\n"
            + "\"attributeName\": \"ConnectionStatus\",\n"
            + "\"newValue\": \"connecting\",\n"
            + "\"type\": \"AttributeValueChangedNotificationXml\"\n"
            + "}\n"
            + "}\n"
            + "}");
    private static final JSONObject EVENTLOG_SEARCHHIT2 = new JSONObject("{\n"
            + "\"_index\": \"sdnevents_v1\",\n"
            + "\"_type\": \"eventlog\",\n"
            + "\"_id\": \"AXB7cJHlZ_FApnwi29xq\",\n"
            + "\"_version\": 1,\n"
            + "\"_score\": 1,\n"
            + "\"_source\": {\n"
            + "\"event\": {\n"
            + "\"nodeName\": \"SDN-Controller-465e2ae306ca\",\n"
            + "\"counter\": \"3\",\n"
            + "\"timeStamp\": \"2020-02-22T08:22:19.8Z\",\n"
            + "\"objectId\": \"sim2230\",\n"
            + "\"attributeName\": \"ConnectionStatus\",\n"
            + "\"newValue\": \"connected\",\n"
            + "\"type\": \"AttributeValueChangedNotificationXml\"\n"
            + "}\n"
            + "}\n"
            + "}");
    private static final String CONFIG_CONTENT = "[dcae]\n"
            + "dcaeUserCredentials=admin:admin\n"
            + "dcaeUrl=off\n"
            + "dcaeHeartbeatPeriodSeconds=120\n"
            + "dcaeTestCollector=no\n"
            + "\n"
            + "[es]\n"
            + "esCluster=sendateodl5\n"
            + "#time limit to keep increasing data in database [in seconds]\n"
            + "#60*60*24*30 (30days)\n"
            + "esArchiveLimit=2592000\n"
            + "#folder where removed data will be stored\n"
            + "esArchiveFolder=./backup\n"
            + "#interval to archive database [in seconds]\n"
            + "#60*60*24 (1day)\n"
            + "esArchiveInterval=86400\n"
            + "\n"
            + "[aai]\n"
            + "#keep comment\n"
            + "aaiHeaders=[\"X-TransactionId: 9999\"]\n"
            + "aaiUrl=http://localhost:81\n"
            + "aaiUserCredentials=AAI:AAI\n"
            + "aaiDeleteOnMountpointRemove=false\n"
            + "aaiTrustAllCerts=false\n"
            + "aaiApiVersion=aai/v13\n"
            + "aaiPropertiesFile=aaiclient.properties\n"
            + "aaiApplicationId=SDNR\n"
            + "aaiPcks12ClientCertFile=/opt/logs/externals/data/stores/keystore.client.p12\n"
            + "aaiPcks12ClientCertPassphrase=adminadmin\n"
            + "aaiClientConnectionTimeout=30000\n"
            + "\n"
            + "[pm]\n"
            + "pmCluster=sendateodl5\n"
            + "pmEnabled=true\n"
            + "";
    // @formatter:on 

    @Test
    public void testComponentData() {
        JSONArray hits = new JSONArray();
        hits.put(EVENTLOG_SEARCHHIT);
        hits.put(EVENTLOG_SEARCHHIT2);
        ComponentData data = new ComponentData(ComponentName.EVENTLOG, hits);
        JSONArray out = data.toJsonArray();
        assertEquals(ComponentName.EVENTLOG, data.getName());
        JSONAssert.assertEquals(EVENTLOG_SEARCHHIT.toString(), out.getJSONObject(0).toString(), false);
        JSONAssert.assertEquals(EVENTLOG_SEARCHHIT2.toString(), out.getJSONObject(1).toString(), false);

    }

    @Test
    public void testConfigData() {
        ConfigData data = new ConfigData(CONFIG_CONTENT);
        assertTrue(data.getLines().length > 10);
    }

    @Test
    public void testConfigName() {
        ConfigName name = ConfigName.APIGATEWAY;
        assertEquals("apigateway", name.getValue());
        try {
            assertEquals(ConfigName.APIGATEWAY, ConfigName.getValueOf("apigateway"));
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void testDataContainer() {
        DataContainer container = new DataContainer();
        assertEquals(Release.CURRENT_RELEASE, container.getRelease());
        assertNotNull(container.getCreated());

        try {
            container = DataContainer.load(new File("src/test/resources/test.bak.json"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertNotNull(container);
        assertEquals(Release.EL_ALTO, container.getRelease());
        assertNotNull(container.getCreated());
        assertTrue(container.getComponents().size() > 0);
        assertTrue(container.getConfigs().size() == 0);
    }

    @Test
    public void testReport() {
        DataMigrationReport report = new DataMigrationReport();

        assertFalse(report.completed());
        long myvar = 42;
        String myvar2 = "come";
        report.log("%d was wrong", myvar);
        report.error("%s to me", myvar2);
        assertTrue(report.toString().contains("42 was wrong"));
        assertTrue(report.toString().contains("come to me"));
        report.setCompleted(true);
        assertTrue(report.completed());

    }

    @Test
    public void TestPluginFileCreation() {

        final String TESTFILE = "asi324po.sa";
        try {
            MavenDatabasePluginInitFile.create(Release.FRANKFURT_R1, TESTFILE);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        File f = new File(TESTFILE);
        if (f.exists()) {
            f.delete();
        }
    }

}
