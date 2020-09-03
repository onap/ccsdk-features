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
package org.onap.ccsdk.features.sdnr.wt.common.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.HtAssert;

public class TestJsonAssert {

    @Test
    public void testGenericTypes() {
        try {
            JSONAssert.assertEquals("test boolean", "{ \"test\":true}", "{ \"test\":true}", true);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
        try {
            JSONAssert.assertEquals("test int", "{ \"test\":2}", "{ \"test\":2}", true);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
        try {
            JSONAssert.assertEquals("test string", "{ \"test\":\"abc\"}", "{ \"test\":\"abc\"}", true);
        } catch (JSONException e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void testGenericTypesFails() {
        try {
            JSONAssert.assertEquals("test boolean", "{ \"test\":true}", "{ \"test\":false}", true);
            fail("test boolean not failed, but has to");
        } catch (JSONException e) {
            fail("problem with json");
        } catch (AssertionError e) {

        }
        try {
            JSONAssert.assertEquals("test int", "{ \"test\":2}", "{ \"test\":3}", true);
            fail("test int not failed, but has to");
        } catch (JSONException e) {
            fail("problem with json");
        } catch (AssertionError e) {

        }
        try {
            JSONAssert.assertEquals("test string", "{ \"test\":\"abc\"}", "{ \"test\":\"abcd\"}", true);
            fail("test string not failed, but has to");
        } catch (JSONException e) {
            fail("problem with json");
        } catch (AssertionError e) {

        }

    }

    @Test
    public void testObject() {
        try {
            JSONAssert.assertEquals("test object", "{ \"test\":{\"more\":{\"x\":1,\"y\":\"2\",\"z\":{}}}}",
                    "{ \"test\":{\"more\":{\"x\":1,\"z\":{},\"y\":\"2\"}}}", true);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testObjectFails() {
        try {
            JSONAssert.assertEquals("test object", "{ \"test\":{\"more\":{\"x\":1,\"y\":\"2\",\"z\":{}}}}",
                    "{ \"test\":{\"more\":{\"x\":1,\"z\":{}}}}", true);
            fail("test object not failed, but has to");
        } catch (JSONException e) {
            fail("problem with json");
        } catch (AssertionError e) {

        }
    }

    @Test
    public void testArrayStrict() {
        try {
            JSONAssert.assertEquals("test array strict", "{ \"test\":[\"a\",\"b\",\"c\"]}",
                    "{ \"test\":[\"a\",\"b\",\"c\"]}", true);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testArrayStrictFails() {
        try {
            JSONAssert.assertEquals("test array strict", "{ \"test\":[\"a\",\"b\",\"c\"]}",
                    "{ \"test\":[\"a\",\"c\",\"b\"]}", true);
            fail("test object not failed, but has to");
        } catch (JSONException e) {
            fail("problem with json");
        } catch (AssertionError e) {

        }
    }

    @Test
    public void testArrayNonStrict() {
        try {
            JSONAssert.assertEquals("test array strict", "{ \"test\":[\"a\",\"b\",\"c\"]}",
                    "{ \"test\":[\"a\",\"c\",\"b\"]}", false);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testArrayNonStrictFails() {
        try {
            JSONAssert.assertEquals("test array strict", "{ \"test\":[\"a\",\"b\",\"c\"]}",
                    "{ \"test\":[\"a\",\"c\",\"b\",\"d\"]}", false);
            fail("test object not failed, but has to");
        } catch (JSONException e) {
            fail("problem with json");
        } catch (AssertionError e) {

        }
        try {
            JSONAssert.assertEquals("test array strict", "{ \"test\":[\"a\",\"b\",\"c\"]}",
                    "{ \"test\":[\"a\",\"b\",\"d\"]}", false);
            fail("test object not failed, but has to");
        } catch (JSONException e) {
            fail("problem with json");
        } catch (AssertionError e) {

        }
    }

    @Test
    public void testNullParamCheck() {

        try {

            HtAssert.nonnull("test", new JSONArray(), null);
            fail("exception not thrown");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("org.onap.ccsdk.features.sdnr.wt.common.test.TestJsonAssert")
                    && e.getMessage().contains("testNullParamCheck"));
        }
    }
}
