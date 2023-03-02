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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;

public class DataTreeChildObject {

    private final String label;
    private final boolean isSearchMatch;
    private final Map<String, DataTreeChildObject> children;
    private final Map<String, Object> properties;

    public DataTreeChildObject(String label, boolean isMatch, Map<String, DataTreeChildObject> children,
            String ownSeverity, String childrenSeveritySummary) {
        this.label = label;
        this.isSearchMatch = isMatch;
        this.children = children;
        this.properties = new HashMap<>();
    }

    public DataTreeChildObject setProperty(String key, Object value) {
        this.properties.put(key, value);
        return this;
    }

    public DataTreeChildObject(String label, boolean isMatch) {
        this(label, isMatch, new HashMap<>(), null, null);
    }

    public boolean isMatch() {
        return this.isSearchMatch;
    }

    public Object getProperty(String key, Object defaultValue) {
        return this.properties.getOrDefault(key, defaultValue);
    }

    public boolean putChild(long treeLevel, String id, DataTreeChildObject data, String parentKey, String childKey) {
        Object itemValue;
        Object itemValueToMatch = data.getProperty(parentKey, null);
        if (itemValueToMatch == null) {
            return false;
        }
        if (treeLevel > 0) {
            if (this.children != null) {
                for (DataTreeChildObject child : this.children.values()) {
                    if (child.putChild(treeLevel - 1, id, data, parentKey, childKey)) {
                        return true;
                    }
                }
            }
        } else {
            itemValue = this.getProperty(childKey, null);
            if (itemValue != null && itemValue.equals(itemValueToMatch)) {
                this.children.put(id, data);
                return true;
            }
        }
        return false;
    }

    public boolean putChildIfNotExists(long treeLevel, String id, DataTreeChildObject data, String parentKey,
            String childKey) {
        Object itemValue;
        Object itemValueToMatch = data.getProperty(parentKey, null);
        if (itemValueToMatch == null) {
            return false;
        }
        if (treeLevel > 0) {
            if (this.children != null) {
                for (DataTreeChildObject child : this.children.values()) {
                    if (child.putChildIfNotExists(treeLevel - 1, id, data, parentKey, childKey)) {
                        return true;
                    }
                }
            }
        } else {
            itemValue = this.getProperty(childKey, null);
            if (itemValue != null && itemValue.equals(itemValueToMatch)) {
                if (!this.children.containsKey(id)) {
                    this.children.put(id, data);
                }
            }
        }
        return false;
    }

    public JSONObject toJSONObject() {
        JSONObject o = new JSONObject();
        o.put("label", this.label);
        o.put("isMatch", this.isSearchMatch);
        JSONObject c = new JSONObject();
        if (this.children != null) {
            for (Entry<String, DataTreeChildObject> entry : this.children.entrySet()) {
                c.put(entry.getKey(), entry.getValue().toJSONObject());
            }
        }
        o.put("children", c);
        return o;
    }

    public boolean hasChildMatching() {
        boolean match = false;
        for (DataTreeChildObject child : this.children.values()) {
            match = match || child.hasChildMatching() || this.isSearchMatch;
            if (match) {
                break;
            }
        }
        return match;
    }

    public void removeUnmatchedPaths() {
        List<String> toRemove = new ArrayList<>();
        for (Entry<String, DataTreeChildObject> entry : this.children.entrySet()) {
            if (!(entry.getValue().hasChildMatching() || entry.getValue().isSearchMatch)) {
                toRemove.add(entry.getKey());
            } else {
                entry.getValue().removeUnmatchedPaths();
            }
        }
        for (String key : toRemove) {
            this.children.remove(key);
        }
    }

    public boolean hasChildren() {
        return this.children != null && !this.children.isEmpty();
    }
}
