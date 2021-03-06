/*
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK.apps.sdnr.wt.apigateway
 * ================================================================================
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
package org.onap.ccsdk.features.sdnr.wt.apigateway.test;

import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.apigateway.MyProperties;

public class TestProperties {

    private static final boolean DEFAULT_CORSENABLED = false;
    private static final boolean DEFAULT_AAIOFF = true;
    private static final boolean DEFAULT_ESOFF = false;
    private static final boolean DEFAULT_TRUSTINSECURE = false;
    private static final String DEFAULT_AAIBASEURL = "off";
    private static Map<String, String> DEFAULT_AAIHEADERS = new HashMap<String, String>();
    private static final String DEFAULT_ESBASEURL = "http://sdnrdb:9200";

    private static final boolean CUSTOM_CORSENABLED = true;
    private static final boolean CUSTOM_AAIOFF = false;
    private static final boolean CUSTOM_ESOFF = false;
    private static final boolean CUSTOM_TRUSTINSECURE = true;
    private static final String CUSTOM_AAIBASEURL = "https://aai.tld:2214";
    private static Map<String, String> CUSTOM_AAIHEADERS = new HashMap<String, String>();
    private static final String CUSTOM_ESBASEURL = "http://localhost:9200";

    private static final String LR = "\n";
    final String tmpFilename = "tmp2.cfg";

    @Before
    @After
    public void init() {
        File f = new File(tmpFilename);
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void test() {
        DEFAULT_AAIHEADERS.put("X-FromAppId", "SDNR");
        DEFAULT_AAIHEADERS.put("Authorization", "Basic QUFJOkFBSQ==");
        CUSTOM_AAIHEADERS.put("X-FromAppId", "SDNC");
        CUSTOM_AAIHEADERS.put("Authorization", "Basic 1234");
        final String TESTPROPERTYFILECONTENT = "aai=" + CUSTOM_AAIBASEURL + LR + "aaiHeaders=[\"X-FromAppId:"
                + CUSTOM_AAIHEADERS.get("X-FromAppId") + "\",\"Authorization:" + CUSTOM_AAIHEADERS.get("Authorization")
                + "\"]" + LR + "database=" + CUSTOM_ESBASEURL + LR + "insecure=" + (CUSTOM_TRUSTINSECURE ? "1" : "0")
                + LR + "cors=" + (CUSTOM_CORSENABLED ? "1" : "0");
        File ftest = new File(tmpFilename);
        MyProperties prop = null;
        ftest = new File(tmpFilename);
        try {
            prop = MyProperties.Instantiate(ftest, true);
        } catch (Exception e) {
            fail("error instantiating properties");
        }
        assertNotNull("problem without exception instantiating properties", prop);

        assertEquals("default config file was not created", true, ftest.exists());

        // test default values
        assertEquals("default value is not correct", DEFAULT_CORSENABLED, prop.corsEnabled());
        assertEquals("default value is not correct", DEFAULT_AAIOFF, prop.isAAIOff());
        assertEquals("default value is not correct", DEFAULT_ESOFF, prop.isEsOff());
        assertEquals("default value is not correct", DEFAULT_TRUSTINSECURE, prop.trustInsecure());
        assertEquals("default value is not correct", DEFAULT_AAIBASEURL, prop.getAAIBaseUrl());
        assertEquals("default value is not correct", DEFAULT_AAIHEADERS, prop.getAAIHeaders());
        assertEquals("default value is not correct", DEFAULT_ESBASEURL, prop.getEsBaseUrl());

        try {
            prop.load(new ByteArrayInputStream(TESTPROPERTYFILECONTENT.getBytes()));
        } catch (Exception e) {
            fail("error loading custom values into properties");
        }

        // test custom values
        assertEquals("custom value is not correct", CUSTOM_CORSENABLED, prop.corsEnabled());
        assertEquals("custom value is not correct", CUSTOM_AAIOFF, prop.isAAIOff());
        assertEquals("custom value is not correct", CUSTOM_ESOFF, prop.isEsOff());
        assertEquals("custom value is not correct", CUSTOM_TRUSTINSECURE, prop.trustInsecure());
        assertEquals("custom value is not correct", CUSTOM_AAIBASEURL, prop.getAAIBaseUrl());
        assertEquals("custom value is not correct", CUSTOM_AAIHEADERS, prop.getAAIHeaders());
        assertEquals("custom value is not correct", CUSTOM_ESBASEURL, prop.getEsBaseUrl());

        // delete autogenerated testfile
        ftest.delete();

    }

}
