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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;

/**
 * 
 * @author Michael Dürre
 *
 */
public class DataContainer {

    private final Release release;
    private final Date created;
    private final Map<ComponentName, ComponentData> components;
    private final Map<ConfigName, ConfigData> configs;

    public Release getRelease() {
        return this.release;
    }

    public boolean isCurrentRelease() {
        return this.release.equals(Release.CURRENT_RELEASE);
    }

    public Date getCreated() {
        return this.created;
    }

    public Map<ComponentName, ComponentData> getComponents() {
        return this.components;
    }

    public Map<ConfigName, ConfigData> getConfigs() {
        return this.configs;
    }

    public DataContainer() {
        this(Release.CURRENT_RELEASE);
    }

    public DataContainer(Release release) {
        this(release, new Date());
    }

    public DataContainer(Release release, Date dt) {
        this.release = release;
        this.created = dt;
        this.components = new HashMap<>();
        this.configs = new HashMap<>();
    }

    public void addComponent(ComponentName name, ComponentData data) {
        this.components.put(name, data);
    }

    public void addConfig(ConfigName name, ConfigData data) {
        this.configs.put(name, data);
    }

    public static DataContainer load(File file) throws Exception {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        JSONObject o = new JSONObject(new String(Files.readAllBytes(file.toPath())));
        DataContainer c =
                new DataContainer(Release.getValueOf(o.getString("release")), format.parse(o.getString("created")));
        JSONObject comps = o.getJSONObject("components");

        String k;
        ComponentName compKey;
        JSONArray compData;
        for (Object key : comps.keySet()) {
            k = String.valueOf(key);
            // check component if exists
            compKey = ComponentName.getValueOf(k);
            compData = comps.getJSONArray(k);
            c.addComponent(compKey, new ComponentData(compKey, compData));
        }
        ConfigName confKey;
        ConfigData confData;
        JSONObject confs = o.getJSONObject("configs");
        for (Object key : confs.keySet()) {
            k = String.valueOf(key);
            confKey = ConfigName.getValueOf(k);
            confData = new ConfigData(confs.getString(k));
            c.addConfig(confKey, confData);
        }
        return c;
    }

    public String toJSON() {
        JSONObject o = new JSONObject();
        o.put("release", this.release.getValue());
        o.put("created", NetconfTimeStampImpl.getConverter().getTimeStampAsNetconfString(this.created));
        JSONObject compsJson = new JSONObject();
        JSONObject confsJson = new JSONObject();
        for (Entry<ComponentName, ComponentData> entry : this.components.entrySet()) {
            compsJson.put(entry.getKey().getValue(), entry.getValue().toJsonArray());
        }
        for (Entry<ConfigName, ConfigData> entry : this.configs.entrySet()) {
            confsJson.put(entry.getKey().getValue(), entry.getValue().toString());
        }
        o.put("components", compsJson);
        o.put("configs", confsJson);
        return o.toString();
    }

}
