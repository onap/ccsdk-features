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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import java.io.IOException;
import org.apache.sshd.common.util.io.IoUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.test.JSONAssert;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.impl.HtUserdataManagerImpl;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.test.util.HostInfoForTest;

public class TestUserdata {

    private static final String USERNAME = "admin132";
    private static final String USERNAME2 = "admin133";

    private static HtDatabaseClient dbRawProvider;
    private static HtUserdataManagerImpl userDbProvider;

    @BeforeClass
    public static void init() throws Exception {

        HostInfo[] hosts = HostInfoForTest.get();
        dbRawProvider = HtDatabaseClient.getClient(hosts);
        userDbProvider = new HtUserdataManagerImpl(dbRawProvider);

    }

    public static void trySleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void test1() {
        String fullContent = "";
        boolean success = false;
        try {
            fullContent = getFileContent("/userdata/full.json");
            success = userDbProvider.setUserdata(USERNAME, fullContent);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue("problem writing data into db",success);

        trySleep(2000);

        String userdata = userDbProvider.getUserdata(USERNAME);
        JSONAssert.assertEquals(fullContent, userdata, false);
        String networkMapContent = "";
        String mergedContent = "";
        try {
            networkMapContent = getFileContent("/userdata/networkmap.json");
            mergedContent = getFileContent("/userdata/merged.json");
            userDbProvider.setUserdata(USERNAME, "networkMap", networkMapContent);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        trySleep(2000);

        userdata = userDbProvider.getUserdata(USERNAME);
        JSONAssert.assertEquals(mergedContent, userdata, false);
    }

    @Test
    public void test2() {
        String fullContent = "";
        boolean success = false;
        try {
            fullContent = getFileContent("/userdata/full.json");
            success = userDbProvider.setUserdata(USERNAME2, fullContent);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue("problem writing data into db",success);

        trySleep(2000);
        // read with complex key
        String userdata = userDbProvider.getUserdata(USERNAME2, "networkMap.styling");
        JSONAssert.assertEquals("{\"theme\": \"dark\"}", userdata, false);
        userdata = userDbProvider.getUserdata(USERNAME2, "networkMap.startupPosition.longitude");
        assertEquals("\"13.35\"", userdata);
        userdata = userDbProvider.getUserdata(USERNAME2, "networkMap.tileOpacity");
        assertEquals("\"26\"", userdata);

        // write with complex key => forbidden
        success = userDbProvider.setUserdata(USERNAME2,"networkMap.styling.theme",null);
        assertFalse(success);
        success = userDbProvider.setUserdata(USERNAME2,"networkMap.themes.key","\"abc\"");
        assertFalse(success);
        // write with complex key => allowed
        success = userDbProvider.setUserdata(USERNAME2,"networkMap.styling.theme","\"test\"");
        assertTrue(success);
        userdata = userDbProvider.getUserdata(USERNAME2, "networkMap.styling.theme");
        assertEquals("\"test\"",userdata);
        success = userDbProvider.setUserdata(USERNAME2,"networkMap.styling.theme","{\"test\":\"abc\"}");
        assertTrue(success);
        userdata = userDbProvider.getUserdata(USERNAME2, "networkMap.styling.theme");
        assertEquals("{\"test\":\"abc\"}",userdata);

        // delete with complex key => forbidden
        success = userDbProvider.removeUserdata(USERNAME2,"networkMap.styling.theme2");
        assertFalse(success);
        // delete with complex key => allowed
        success = userDbProvider.removeUserdata(USERNAME2,"networkMap.styling.theme");
        assertTrue(success);
        userdata = userDbProvider.getUserdata(USERNAME2, "networkMap.styling");
        assertEquals("{}",userdata);


    }

    private static String getFileContent(String filename) throws IOException {
        return String.join("\n", IoUtils.readAllLines(TestUserdata.class.getResourceAsStream(filename)));
    }
}
