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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.json.JSONException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DbFilter;

public class TestDatabaseFilterConversion {

    @Test
    public void testStartsWith() {
        String re = DbFilter.createDatabaseRegex("abc*");
        assertEquals("abc.*", re);
    }

    @Test
    public void testEndsWith() {
        String re = DbFilter.createDatabaseRegex("*abc");
        assertEquals(".*abc", re);
    }

    @Test
    public void testMultiple() {
        String re = DbFilter.createDatabaseRegex("abc*ff*fa");
        assertEquals("abc.*ff.*fa", re);
    }

    @Test
    public void testPlaceholder() {
        String re = DbFilter.createDatabaseRegex("abc?ef");
        assertEquals("abc.{1,1}ef", re);
    }

    @Test
    public void testCombined() {
        String re = DbFilter.createDatabaseRegex("abc?ff*fa");
        assertEquals("abc.{1,1}ff.*fa", re);
    }

    @Test
    public void testFilterCheck() {
        assertTrue(DbFilter.hasSearchParams("abc?"));
        assertTrue(DbFilter.hasSearchParams("bac*"));
        assertFalse(DbFilter.hasSearchParams("abc+"));
    }

    @Test
    public void testRangeConversion() {
        try {
            JSONAssert.assertEquals("", "{\"query\":{\"range\":{\"port\":{\"gte\":2230,\"boost\":2}}}}",
                    DbFilter.getRangeQuery("port", ">=2230").toJSON(), true);
            JSONAssert.assertEquals("", "{\"query\":{\"range\":{\"port\":{\"gt\":2230,\"boost\":2}}}}",
                    DbFilter.getRangeQuery("port", ">2230").toJSON(), true);
            JSONAssert.assertEquals("", "{\"query\":{\"range\":{\"port\":{\"lte\":2230,\"boost\":2}}}}",
                    DbFilter.getRangeQuery("port", "<=2230").toJSON(), true);
            JSONAssert.assertEquals("", "{\"query\":{\"range\":{\"port\":{\"lt\":2230,\"boost\":2}}}}",
                    DbFilter.getRangeQuery("port", "<2230").toJSON(), true);
            JSONAssert.assertEquals("",
                    "{\"query\":{\"range\":{\"timestamp\":{\"lt\":\"2018-01-01T23:59:59.0Z\",\"boost\":2}}}}",
                    DbFilter.getRangeQuery("timestamp", "<2018-01-01T23:59:59.0Z").toJSON(), true);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

}
