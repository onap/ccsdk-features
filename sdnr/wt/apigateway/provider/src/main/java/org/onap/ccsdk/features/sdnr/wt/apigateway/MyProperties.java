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
package org.onap.ccsdk.features.sdnr.wt.apigateway;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyProperties {

    private static Logger LOG = LoggerFactory.getLogger(MyProperties.class);
    public static final String PROPFILE = "etc/apigateway.properties";
    private static final String DEFAULT_AAI_HEADERS = "[\"X-FromAppId:SDNR\",\"Authorization:Basic QUFJOkFBSQ==\"]";
    private static final String DEFAULT_CORSENABLED = "0";
    private static final String DEFAULT_TRUSTINSECURE = "0";
    private static final String DEFAULT_ESDATABASE = "http://sdnrdb:9200";
    private static final String DEFAULT_AAI = "off";
    private static final String DEFAULT_URL_OFF = "off";
    private static final String DEFAULT_TILES = "${TILEURL}";
    private static final String DEFAULT_TOPOLOGY = "${TOPOURL}";
    private static MyProperties mObj;
    private static final String ENVVARIABLE = "${";
    private static final String REGEXENVVARIABLE = "(\\$\\{[A-Z0-9_-]+\\})";
    private static final Pattern ENV_PATTERN = Pattern.compile(REGEXENVVARIABLE);


    private String aaiBase;
    private Map<String, String> aaiHeaders;
    private String esBase;
    private String tilesBase;
    private String topologyBase;

    private boolean trustInsecure;

    private boolean corsEnabled;

    public boolean isAAIOff() {
        return this.aaiBase == null ? true : this.aaiBase.equals("off");
    }

    public boolean isEsOff() {
        return this.esBase == null ? true : this.esBase.equals("off");
    }

    public boolean isTilesOff() {
        return this.tilesBase == null ? true : this.tilesBase.equals("off");
    }

    public boolean isTopologyOff() {
        return this.topologyBase == null ? true : this.topologyBase.equals("off");
    }

    public String getAAIBaseUrl() {
        return this.aaiBase;
    }

    public String getEsBaseUrl() {
        return this.esBase;
    }

    public String getTilesBaseUrl() {
        return this.tilesBase;
    }

    public String getTopologyBaseUrl() {
        return this.topologyBase;
    }

    public Map<String, String> getAAIHeaders() {
        return this.aaiHeaders;
    }

    public boolean trustInsecure() {
        return this.trustInsecure;
    }

    public boolean corsEnabled() {
        return this.corsEnabled;
    }

    public static MyProperties Instantiate() throws IOException, NumberFormatException {
        return Instantiate(new File(PROPFILE));
    }

    public static MyProperties Instantiate(File file) throws IOException, NumberFormatException {

        return Instantiate(file, false);
    }

    public static MyProperties Instantiate(File file, boolean force) throws IOException, NumberFormatException {
        if (mObj == null || force) {
            mObj = new MyProperties(file);
            LOG.debug("instantiated: {}", mObj.toString());
        }
        return mObj;
    }

    private MyProperties(File file) throws IOException, NumberFormatException {
        this.aaiBase = DEFAULT_AAI;
        this.trustInsecure = false;
        if (!file.exists()) {
            this.writeDefaults(file);
        }
        this.load(new FileInputStream(file));
    }

    public void load(InputStream in) throws IOException, NumberFormatException {

        Properties defaultProps = new Properties();
        defaultProps.load(in);
        in.close();

        this.aaiBase = getProperty(defaultProps,"aai", DEFAULT_AAI);
        this.aaiHeaders = _parseHeadersMap(getProperty(defaultProps,"aaiHeaders", DEFAULT_AAI_HEADERS));
        this.esBase = getProperty(defaultProps,"database", DEFAULT_ESDATABASE);
        this.tilesBase = getProperty(defaultProps,"tiles", DEFAULT_TILES, DEFAULT_URL_OFF);
        this.topologyBase = getProperty(defaultProps,"topology", DEFAULT_TOPOLOGY, DEFAULT_URL_OFF);
        this.trustInsecure = Integer.parseInt(getProperty(defaultProps,"insecure", DEFAULT_TRUSTINSECURE)) == 1;
        this.corsEnabled = Integer.parseInt(getProperty(defaultProps,"cors", DEFAULT_CORSENABLED)) == 1;
    }
    private static String getProperty(Properties props,final String key, final String defValue) {
        return getProperty(props, key, defValue, null);
    }
    private static String getProperty(Properties props,final String key, final String defValue, final String valueIfEmpty) {

        LOG.debug("try to get property for {} with def {}", key, defValue);
        String value = props.getProperty(key,defValue);
        //try to read env var
        if (value != null && value.contains(ENVVARIABLE)) {

            LOG.debug("try to find env var(s) for {}", value);
            final Matcher matcher = ENV_PATTERN.matcher(value);
            String tmp = new String(value);
            while (matcher.find() && matcher.groupCount() > 0) {
                final String mkey = matcher.group(1);
                if (mkey != null) {
                    try {
                        LOG.debug("match found for v={} and env key={}", tmp, mkey);
                        //String env=System.getenv(mkey.substring(2,mkey.length()-1));
                        String env = System.getenv(mkey.substring(2, mkey.length() - 1));
                        tmp = tmp.replace(mkey, env == null ? "" : env);
                    } catch (SecurityException e) {
                        LOG.warn("unable to read env {}: {}", value, e);
                    }
                }
            }
            value = tmp;
        }
        if((value==null || value == "") && valueIfEmpty!=null) {
            value = valueIfEmpty;
        }
        return value;
    }
    private static Map<String, String> _parseHeadersMap(String s) {
        Map<String, String> r = new HashMap<>();
        try {
            JSONArray a = new JSONArray(s);
            if (a.length() > 0) {
                for (int i = 0; i < a.length(); i++) {
                    String item = a.getString(i);
                    String[] hlp = item.split(":");
                    if (hlp.length > 1) {
                        r.put(hlp[0], hlp[1]);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("problem loading headers map: {}", e.getMessage());
        }
        return r;
    }

    private String writeDefaults(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        final String LR = "\n";
        FileWriter fw = new FileWriter(f);
        sb.append("aai=" + DEFAULT_AAI + LR);
        sb.append("aaiHeaders=" + DEFAULT_AAI_HEADERS + LR);
        sb.append("database=" + DEFAULT_ESDATABASE + LR);
        sb.append("tiles=" + DEFAULT_TILES + LR);
        sb.append("topology=" + DEFAULT_TOPOLOGY + LR);
        sb.append("insecure=" + DEFAULT_TRUSTINSECURE + LR);
        sb.append("cors=" + DEFAULT_CORSENABLED);
        try {
            fw.write(sb.toString());
        } catch (Exception e) {
            LOG.warn("problem writing default values to propertyfile {} : {}", f.getAbsolutePath(), e.getMessage());
        } finally {
            fw.close();
        }
        return sb.toString();
    }

    public static MyProperties getInstance() {
        return mObj;
    }

    @Override
    public String toString() {
        return "MyProperties [aaiBase=" + aaiBase + ", aaiHeaders=" + aaiHeaders + ", esBase=" + esBase
                + ", trustInsecure=" + trustInsecure + ", corsEnabled=" + corsEnabled + "]";
    }

}
