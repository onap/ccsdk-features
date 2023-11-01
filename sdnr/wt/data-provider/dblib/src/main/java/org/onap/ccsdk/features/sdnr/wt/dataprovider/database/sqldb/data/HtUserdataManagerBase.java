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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtUserdataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HtUserdataManagerBase implements HtUserdataManager {

    private static final Logger LOG = LoggerFactory.getLogger(HtUserdataManagerBase.class);

    private static final String USERDATA_DEFAULTS_FILENAME = "etc/userdata-defaults.json";
    private static final JSONObject USERDATA_DEFAULTS_CONTENT = loadDefaults();

    protected static JSONObject loadDefaults() {
        File f = new File(USERDATA_DEFAULTS_FILENAME);
        String content;
        JSONObject o = null;
        if (f.exists()) {
            try {
                content = Files.readString(f.toPath());
                o = new JSONObject(content);
            } catch (IOException e) {
                LOG.warn("problem loading defaults: ", e);
            } catch (JSONException e) {
                LOG.warn("problem parsing defaults: ", e);
            }
        }
        return o;
    }

    protected abstract String readUserdata(String username, String defaultValue);

    @Override
    public String getUserdata(String username) {
        String json = this.readUserdata(username, "{}");
        if (USERDATA_DEFAULTS_CONTENT != null) {
            JSONObject merge = mergeData(new JSONObject(json), USERDATA_DEFAULTS_CONTENT);
            json = merge.toString();
        }
        return json;
    }

    @Override
    public String getUserdata(String username, String key) {
        final String EMPTY_JSON_OBJECT = "{}";
        JSONObject o = new JSONObject(this.getUserdata(username));
        if(key==null || !key.contains("."))
            return o.has(key) ? o.get(key).toString() : EMPTY_JSON_OBJECT;
        final String[] keys = key.split("\\.");
        Object tmp;
        for (int i=0;i<keys.length-1;i++){
            if(!o.has(keys[i])){
                return EMPTY_JSON_OBJECT;
            }
            tmp = o.get(keys[i]);
            if(!(tmp instanceof JSONObject)) {
                return EMPTY_JSON_OBJECT;
            }
            o = (JSONObject)tmp;
        }
        tmp= o.has(keys[keys.length-1])? o.get(keys[keys.length-1]):EMPTY_JSON_OBJECT;
        if(tmp instanceof JSONObject){
            return ((JSONObject)tmp).toString();
        }
        if(tmp instanceof JSONArray){
            return ((JSONArray)tmp).toString();
        }
        if( tmp instanceof String){
            return String.format("\"%s\"",tmp);
        }
        return String.valueOf(tmp);
    }

    @Override
    public boolean setUserdata(String username, String key, String data) {
        if(key == null || data == null){
            return false;
        }
        JSONObject o = new JSONObject(this.getUserdata(username));
        if(!key.contains(".")) {
            o.put(key, new JSONObject(data));
        }
        else{
            final String[] keys = key.split("\\.");
            JSONObject tmp = o;
            Object tmpObject;
            for(int i=0;i<keys.length-1;i++){
                if(!tmp.has(keys[i])){
                    tmp.put(keys[i], new JSONObject());
                }
                tmpObject = tmp.get(keys[i]);
                 if(!(tmpObject instanceof JSONObject)){
                    return false;
                }
                tmp = (JSONObject) tmpObject;
            }
            if(data.startsWith("{")){
                tmp.put(keys[keys.length-1], new JSONObject(data));
            }
            else if (data.startsWith("[")){
                tmp.put(keys[keys.length-1], new JSONArray(data));
            }
            else if(data.startsWith("\"") && data.endsWith("\"")){
                tmp.put(keys[keys.length-1],data.substring(1,data.length()-1));
            }
            else if("true".equals(data) || "false".equals(data)){
                tmp.put(keys[keys.length-1],"true".equals(data));
            }
            else {
                try {
                    tmp.put(keys[keys.length - 1], Double.parseDouble(data));
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return this.setUserdata(username, o.toString());
    }

    @Override
    public boolean removeUserdata(String username, String key) {
        if(key == null){
            return false;
        }
        JSONObject o = new JSONObject(this.getUserdata(username));
        if(!key.contains(".")) {
            if (o.has(key)) {
                o.remove(key);
                return this.setUserdata(username, o.toString());
            }
        }
        else {
            final String[] keys = key.split("\\.");
            JSONObject tmp = o;
            Object tmpObject;
            for(int i=0;i<keys.length-1;i++){
                if(!tmp.has(keys[i])){
                    return false;
                }
                tmpObject = tmp.get(keys[i]);
                if(!(tmpObject instanceof JSONObject)){
                    return false;
                }
                tmp = (JSONObject) tmpObject;
            }
            if(!tmp.has(keys[keys.length-1])){
                return false;
            }
            tmp.remove(keys[keys.length-1]);
            return this.setUserdata(username, o.toString());

        }
        return false;
    }

    protected static JSONObject mergeData(JSONObject o, String key, JSONObject subObject) {
        if (!o.has(key)) {
            o.put(key, subObject);
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put(key, subObject);
            o = mergeData(tmp, o);
        }
        return o;
    }

    protected static JSONObject mergeData(JSONObject source, JSONObject target) throws JSONException {
        String[] keys = JSONObject.getNames(source);
        if (keys == null) {
            return target;
        }
        for (String key : keys) {
            Object value = source.get(key);
            if (!target.has(key)) {
                // new value for "key":
                target.put(key, value);
            } else {
                // existing value for "key" - recursively deep merge:
                if (value instanceof JSONObject) {
                    JSONObject valueJson = (JSONObject) value;
                    mergeData(valueJson, target.getJSONObject(key));
                } else {
                    target.put(key, value);
                }
            }
        }
        return target;
    }

}
