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

import java.util.Comparator;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONAssert {

    private static final Logger LOG = LoggerFactory.getLogger(JSONAssert.class);

    /**
     * nonstrict comparison means that json array items can be in different orders
     */
    private static final Comparator<JSONObject> nonStrictComarator = new Comparator<>() {

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
                double delta = ((Double) o1 - (Double) o2);
                return delta == 0.0 ? 0 : delta < 0 ? -1 : 1;
            } else if ((o1 instanceof Boolean) && (o2 instanceof Boolean)) {
                return ((Boolean) o1).booleanValue() == ((Boolean) o2).booleanValue() ? 0 : -1;

            } else if ((o1 instanceof String s1) && (o2 instanceof String s2)) {
                return s1.equals(s2) ? 0 : -1;
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
    private static final Comparator<JSONObject> strictComarator = new Comparator<>() {

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
            if ((o1 instanceof Double o1d) && (o2 instanceof Double o2d)) {
                return Double.compare(o1d, o2d);
            }else if ((o1 instanceof Integer o1i) && (o2 instanceof Integer o2i)) {
                return Integer.compare(o1i,o2i);
            } else if ((o1 instanceof Boolean) && (o2 instanceof Boolean)) {
                return ((Boolean) o1).booleanValue() == ((Boolean) o2).booleanValue() ? 0 : -1;

            } else if ((o1 instanceof String s1) && (o2 instanceof String s2)) {
                return s1.equals(s2) ? 0 : -1;
            } else if ((o1 instanceof JSONObject o1j) && (o2 instanceof JSONObject o2j)) {
                if (o1j.isEmpty() && o2j.isEmpty()) {
                    return 0;
                }
                Iterator<?> keys = o1j.keys();
                while (keys.hasNext()) {
                    String key = String.valueOf(keys.next());
                    if (!o2j.has(key)) {
                        return -1;
                    }
                    x = this.test(o1j.get(key), o2j.get(key));
                    if (x != 0) {
                        return x;
                    }
                }
            } else if ((o1 instanceof JSONArray o1a) && (o2 instanceof JSONArray o2a)) {
                if (o1a.length() != o2a.length()) {
                    return o1a.length() - o2a.length() < 0 ? -1 : 1;
                }
                for (int i = 0; i < o1a.length(); i++) {
                    x = this.test(o1a.get(i), o2a.get(i));
                    if (x != 0) {
                        return x;
                    }
                }
            }
            return 0;

        }
    };

    public static boolean assertEquals(String def, String toTest, boolean strict) throws JSONException {
        return assertEquals("", def, toTest, strict);
    }

    public static boolean assertEquals(String message, String def, String toTest, boolean strict) throws JSONException {
        if (strict) {
            return assertEqualsStrict(message, def, toTest);
        } else {
            return assertEqualsNonStrict(message, def, toTest);
        }
    }

    private static boolean assertEqualsNonStrict(String message, String def, String toTest) throws JSONException {

        JSONObject d1 = new JSONObject(def);
        JSONObject d2 = new JSONObject(toTest);
        if (nonStrictComarator.compare(d1, d2) != 0) {
            LOG.error("{}", message);
            return false;
        }
        return true;

    }

    private static boolean assertEqualsStrict(String message, String def, String toTest) throws JSONException {
        JSONObject d1 = new JSONObject(def);
        JSONObject d2 = new JSONObject(toTest);
        if (strictComarator.compare(d1, d2) != 0) {
            LOG.error("{}", message);
            return false;
        }
        return true;
    }


}
