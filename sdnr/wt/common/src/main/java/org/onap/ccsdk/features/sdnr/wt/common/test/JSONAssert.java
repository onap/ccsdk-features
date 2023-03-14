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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONAssert {

    /**
     * nonstrict comparison means that json array items can be in different orders
     */
    private static Comparator<JSONObject> nonStrictComarator = new Comparator<JSONObject>() {

        @Override
        public int compare(JSONObject o1, JSONObject o2) {
            if (o1.equals(o2)) {
                return 0;
            }
            Iterator<?> keys = o1.keys();
            while (keys.hasNext()) {
                String key = String.valueOf(keys.next());
                int x = this.test(o1.get(key), o2.get(key));
                if (x != 0) {
                    return x;
                } else {
                }
            }
            return 0;
        }

        private int test(Object o1, Object o2) {
            int x;
            if ((o1 instanceof Double) && (o2 instanceof Double)) {
                double delta = (((Double) o1).doubleValue() - ((Double) o2).doubleValue());
                return delta == 0.0 ? 0 : delta < 0 ? -1 : 1;
            } else if ((o1 instanceof Boolean) && (o2 instanceof Boolean)) {
                return ((Boolean) o1).booleanValue() == ((Boolean) o2).booleanValue() ? 0 : -1;

            } else if ((o1 instanceof String) && (o2 instanceof String)) {

                return ((String) o1).equals((o2)) ? 0 : -1;
            } else if ((o1 instanceof JSONObject) && (o2 instanceof JSONObject)) {
                if (((JSONObject) o1).length() != ((JSONObject) o2).length()) {
                    return ((JSONObject) o1).length() - ((JSONObject) o2).length() < 0 ? -1 : 1;
                }
                Iterator<?> keys = ((JSONObject) o1).keys();
                while (keys.hasNext()) {
                    String key = String.valueOf(keys.next());
                    if (!((JSONObject) o2).has(key)) {
                        return -1;
                    }
                    x = this.test(((JSONObject) o1).get(key), ((JSONObject) o2).get(key));
                    if (x != 0) {
                        return x;
                    }
                }
            } else if ((o1 instanceof JSONArray) && (o2 instanceof JSONArray)) {
                if (((JSONArray) o1).length() != ((JSONArray) o2).length()) {
                    return ((JSONArray) o1).length() - ((JSONArray) o2).length() < 0 ? -1 : 1;
                }
                for (int i = 0; i < ((JSONArray) o1).length(); i++) {
                    x = this.findInArray(((JSONArray) o1).get(i), (JSONArray) o2);
                    if (x != 0) {
                        return x;
                    }
                }
                for (int i = 0; i < ((JSONArray) o2).length(); i++) {
                    x = this.findInArray(((JSONArray) o2).get(i), (JSONArray) o1);
                    if (x != 0) {
                        return x;
                    }
                }
            }
            return 0;

        }

        private int findInArray(Object node, JSONArray o) {
            for (int i = 0; i < o.length(); i++) {
                if (this.test(o.get(i), node) == 0) {
                    return 0;
                }
            }
            return -1;
        }
    };
    /**
     * strict comparison means that json array items have to be in the same order
     */
    private static Comparator<JSONObject> strictComarator = new Comparator<JSONObject>() {

        @Override
        public int compare(JSONObject o1, JSONObject o2) {
            if (o1.equals(o2)) {
                return 0;
            }
            Iterator<?> keys = o1.keys();
            while (keys.hasNext()) {
                String key = String.valueOf(keys.next());
                int x = this.test(o1.get(key), o2.get(key));
                if (x != 0) {
                    return x;
                }
            }
            return 0;
        }

        private int test(Object o1, Object o2) {
            int x;
            if ((o1 instanceof Double) && (o2 instanceof Double)) {

                return (((Double) o1).doubleValue() - ((Double) o2).doubleValue()) < 0 ? -1 : 1;
            } else if ((o1 instanceof Boolean) && (o2 instanceof Boolean)) {
                return ((Boolean) o1).booleanValue() == ((Boolean) o2).booleanValue() ? 0 : -1;

            } else if ((o1 instanceof String) && (o2 instanceof String)) {

                return ((String) o1).equals((o2)) ? 0 : -1;
            } else if ((o1 instanceof JSONObject) && (o2 instanceof JSONObject)) {
                if (((JSONObject) o1).length() == 0 && ((JSONObject) o2).length() == 0) {
                    return 0;
                }
                Iterator<?> keys = ((JSONObject) o1).keys();
                while (keys.hasNext()) {
                    String key = String.valueOf(keys.next());
                    if (!((JSONObject) o2).has(key)) {
                        return -1;
                    }
                    x = this.test(((JSONObject) o1).get(key), ((JSONObject) o2).get(key));
                    if (x != 0) {
                        return x;
                    }
                }
            } else if ((o1 instanceof JSONArray) && (o2 instanceof JSONArray)) {
                if (((JSONArray) o1).length() != ((JSONArray) o2).length()) {
                    return ((JSONArray) o1).length() - ((JSONArray) o2).length() < 0 ? -1 : 1;
                }
                for (int i = 0; i < ((JSONArray) o1).length(); i++) {
                    x = this.test(((JSONArray) o1).get(i), ((JSONArray) o2).get(i));
                    if (x != 0) {
                        return x;
                    }
                }
            }
            return 0;

        }
    };

    public static void assertEquals(String def, String toTest, boolean strict) throws JSONException {
        assertEquals("", def, toTest, strict);
    }

    public static void assertEquals(String message, String def, String toTest, boolean strict) throws JSONException {
        if (strict) {
            assertEqualsStrict(message, def, toTest);
        } else {
            assertEqualsNonStrict(message, def, toTest);
        }
    }


    public static void assertContainsOnlyKey(JSONObject o, String key) {
        if (o == null) {
            throw new AssertionError("object is null");
        }
        if (key == null) {
            throw new AssertionError("key is null");
        }

        Object[] keys = o.keySet().toArray();
        if (keys.length > 1) {
            throw new AssertionError("more than one key found: " + Arrays.toString(keys));
        }
        if (keys.length == 0) {
            throw new AssertionError("no key found");
        }
        if (!key.equals(keys[0])) {
            throw new AssertionError("different key found " + key + " <=> " + keys[0]);
        }
    }


    public static void assertContainsExactKeys(JSONObject o, String[] keys) {
        if (o == null) {
            throw new AssertionError("object is null");
        }
        if (keys == null) {
            throw new AssertionError("keys is null");
        }
        Object[] okeys = o.keySet().toArray();
        if (okeys.length != keys.length) {
            throw new AssertionError(
                    "found different amount of keys: " + Arrays.toString(okeys) + " expected:" + Arrays.toString(keys));
        }
        for (String k : keys) {
            if (!o.keySet().contains(k)) {
                throw new AssertionError("key " + k + " not found");
            }
        }
    }

    public static void assertContainsNoKeys(JSONObject o) {
        if (o == null) {
            throw new AssertionError("object is null");
        }

        Object[] okeys = o.keySet().toArray();
        if (okeys.length != 0) {
            throw new AssertionError("found keys");
        }
    }

    private static void assertEqualsNonStrict(String message, String def, String toTest) throws JSONException {

        JSONObject d1 = new JSONObject(def);
        JSONObject d2 = new JSONObject(toTest);
        if (nonStrictComarator.compare(d1, d2) != 0) {
            throw new AssertionError(message);
        }

    }

    private static void assertEqualsStrict(String message, String def, String toTest) throws JSONException {
        JSONObject d1 = new JSONObject(def);
        JSONObject d2 = new JSONObject(toTest);
        if (strictComarator.compare(d1, d2) != 0) {
            throw new AssertionError(message);
        }
    }



}
