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
import org.json.JSONObject;

public class DataTreeObject extends HashMap<String, DataTreeChildObject> {

    private static final long serialVersionUID = 1L;
    private final String parentKey;
    private final String childKey;

    public DataTreeObject(String parentKey, String childKey) {
        this.parentKey = parentKey;
        this.childKey = childKey;
    }

    public void put(long treeLevel, String id, DataTreeChildObject data) {
        for (DataTreeChildObject entry : this.values()) {
            if (entry.putChild(treeLevel, id, data, this.parentKey, this.childKey)) {
                break;
            }
        }
    }

    public void putIfNotExists(long treeLevel, String id, DataTreeChildObject data) {
        for (DataTreeChildObject entry : this.values()) {
            if (entry.putChildIfNotExists(treeLevel, id, data, this.parentKey, this.childKey)) {
                break;
            }
        }
    }

    public void putIfNotExists(String id, DataTreeChildObject data) {
        if (!this.containsKey(id)) {
            this.put(id, data);
        }
    }

    public String toJSON() {
        JSONObject o = new JSONObject();
        for (Entry<String, DataTreeChildObject> entry : this.entrySet()) {
            o.put(entry.getKey(), entry.getValue().toJSONObject());
        }
        return o.toString();
    }

    public void removeUnmatchedPaths() {
        List<String> toRemove = new ArrayList<>();
         for (Entry<String,DataTreeChildObject> entry : this.entrySet()) {
            entry.getValue().removeUnmatchedPaths();
            if(!entry.getValue().isMatch() && !entry.getValue().hasChildren()) {
                toRemove.add(entry.getKey());
            }
        }
        for(String toRemoveKey:toRemove) {
            this.remove(toRemoveKey);
        }

    }


    public static String[] slice(String[] source, int start) {
        String[] r = new String[source.length - start];
        for (int i = 0; i < r.length; i++) {
            r[i] = source[i + start];
        }
        return r;

    }
}
