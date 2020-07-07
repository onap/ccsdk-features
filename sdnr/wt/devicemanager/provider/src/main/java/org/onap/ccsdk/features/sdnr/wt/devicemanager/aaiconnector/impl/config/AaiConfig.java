/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.aaiconnector.impl.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import org.json.JSONArray;
import org.onap.ccsdk.features.sdnr.wt.common.HtAssert;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiConfig implements Configuration {

    private static Logger LOG = LoggerFactory.getLogger(AaiConfig.class);

    private static final String SECTION_MARKER_AAI = "aai";

    private enum Config {
        AAIPROP_FILE("aaiPropertiesFile", "null"), BASEURL("aaiUrl", "off",
                "org.onap.ccsdk.sli.adaptors.aai.uri"), USERCREDENTIALS("aaiUserCredentials", ""), HEADERS("aaiHeaders",
                        "[\"X-TransactionId: 9999\"]"), DELETEONMOUNTPOINTREMOVED("aaiDeleteOnMountpointRemove",
                                false), TRUSTALLCERTS("aaiTrustAllCerts", false,
                                        "org.onap.ccsdk.sli.adaptors.aai.host.certificate.ignore"), APIVERSION(
                                                "aaiApiVersion",
                                                "aai/v13"), PCKS12CERTFILENAME("aaiPcks12ClientCertFile", "",
                                                        "org.onap.ccsdk.sli.adaptors.aai.ssl.key"), PCKS12PASSPHRASE(
                                                                "aaiPcks12ClientCertPassphrase", "",
                                                                "org.onap.ccsdk.sli.adaptors.aai.ssl.key.psswd"), CONNECTIONTIMEOUT(
                                                                        "aaiClientConnectionTimeout",
                                                                        String.valueOf(
                                                                                DEFAULT_VALUE_CONNECTION_TIMEOUT),
                                                                        "connection.timeout"), //in ms;
        APPLICATIONID("aaiApplicationId", "SDNR", "org.onap.ccsdk.sli.adaptors.aai.application"), HTTPREADTIMEOUT(
                "aaiReadTimeout", "60000", "read.timeout");

        private String propertyKey;
        private String propertyValue;
        private Optional<String> propertyKeySecondFile;

        Config(String propertyKey, Object propertyValue) {
            this.propertyKey = propertyKey;
            this.propertyValue = propertyValue.toString();
            this.propertyKeySecondFile = Optional.empty();
        }

        Config(String propertyKey, Object propertyValue, String propertyKeySecondFile) {
            this(propertyKey, propertyValue);
            this.propertyKeySecondFile = Optional.of(propertyKeySecondFile);
        }
    }

    private static final long DEFAULT_VALUE_CONNECTION_TIMEOUT = 30000; //in ms
    private static final String HEADER_KEY_APPLICATION = "X-FromAppId";

    private final ConfigurationFileRepresentation configuration;

    public AaiConfig(ConfigurationFileRepresentation configuration) {
        HtAssert.nonnull(configuration);
        this.configuration = configuration;
        this.configuration.addSection(SECTION_MARKER_AAI);
        defaults();
    }

    /*
     * Getter
     */

    public boolean doDeleteOnMountPointRemoved() {
        return configuration.getPropertyBoolean(SECTION_MARKER_AAI, Config.DELETEONMOUNTPOINTREMOVED.propertyKey);
    }

    public boolean getTrustAll() {
        return configuration.getPropertyBoolean(SECTION_MARKER_AAI, Config.TRUSTALLCERTS.propertyKey);
    }

    public String getPcks12CertificateFilename() {
        return configuration.getProperty(SECTION_MARKER_AAI, Config.PCKS12CERTFILENAME.propertyKey);
    }

    public String getPcks12CertificatePassphrase() {
        return configuration.getProperty(SECTION_MARKER_AAI, Config.PCKS12PASSPHRASE.propertyKey);
    }

    public int getConnectionTimeout() {
        long res = configuration.getPropertyLong(SECTION_MARKER_AAI, Config.CONNECTIONTIMEOUT.propertyKey)
                .orElse(DEFAULT_VALUE_CONNECTION_TIMEOUT);
        return (int) res;
    }

    public boolean isOff() {
        return configuration.getProperty(SECTION_MARKER_AAI, Config.BASEURL.propertyKey).equalsIgnoreCase("off");
    }

    public String getBaseUri() {
        String res = configuration.getProperty(SECTION_MARKER_AAI, Config.APIVERSION.propertyKey);
        if (!res.startsWith("/")) {
            res = "/" + res;
        }
        return res;
    }

    public String getBaseUrl() {
        if (isOff()) {
            return "";
        }

        String url = configuration.getProperty(SECTION_MARKER_AAI, Config.BASEURL.propertyKey);
        if (!url.endsWith("/")) {
            url += "/";
        }
        String apiVersion = configuration.getProperty(SECTION_MARKER_AAI, Config.APIVERSION.propertyKey);
        if (apiVersion.startsWith("/")) {
            apiVersion = apiVersion.substring(1);
        }
        return url + apiVersion;

    }

    public Map<String, String> getHeaders() {

        Map<String, String> headers =
                _parseHeadersMap(configuration.getProperty(SECTION_MARKER_AAI, Config.HEADERS.propertyKey));
        headers.put(HEADER_KEY_APPLICATION,
                configuration.getProperty(SECTION_MARKER_AAI, Config.APPLICATIONID.propertyKey));

        String credentials = configuration.getProperty(SECTION_MARKER_AAI, Config.USERCREDENTIALS.propertyKey);
        if (!nullorempty(credentials)) {
            String credentialParts[] = credentials.split(":");
            if (credentialParts.length == 2) {
                // 0:username 1:password
                String s = headers.getOrDefault("Authorization", null);
                if (nullorempty(s) && !nullorempty(credentialParts[0]) && !nullorempty(credentialParts[1])) {
                    headers.put("Authorization",
                            "Basic " + new String(Base64.getEncoder().encode(credentials.getBytes())));
                }
            }
        }
        return headers;
    }

    @Override
    public String getSectionName() {
        return SECTION_MARKER_AAI;
    }

    @Override
    public void defaults() {
        for (Config conf : Config.values()) {
            configuration.setPropertyIfNotAvailable(SECTION_MARKER_AAI, conf.propertyKey, conf.propertyValue);
        }
        // If file is available, the content is assigned to related parameters.
        getAaiPropertiesFile();
    }

    @Override
    public String toString() {
        return "AaiConfig [doDeleteOnMountPointRemoved()=" + doDeleteOnMountPointRemoved() + ", getTrustAll()="
                + getTrustAll() + ", getPcks12CertificateFilename()=" + getPcks12CertificateFilename()
                + ", getPcks12CertificatePassphrase()=" + getPcks12CertificatePassphrase() + ", getConnectionTimeout()="
                + getConnectionTimeout() + ", isOff()=" + isOff() + ", getBaseUri()=" + getBaseUri() + ", getBaseUrl()="
                + getBaseUrl() + ", getHeaders()=" + getHeaders() + ", getSectionName()=" + getSectionName() + "]";
    }

    /*
     * Private
     */

    private boolean nullorempty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Convert headers to configuration string.
     * 
     * @param headers
     * @return
     */
    @SuppressWarnings("unused")
    private static String _printHeadersMap(Map<String, String> headers) {
        String r = "[";
        if (headers != null) {
            int i = 0;
            for (Entry<String, String> entry : headers.entrySet()) {
                if (i > 0) {
                    r += ",";
                }
                r += "\"" + entry.getKey() + ":" + entry.getValue() + "\"";
                i++;
            }
        }
        r += "]";
        return r;
    }

    private static Map<String, String> _parseHeadersMap(String s) {

        LOG.info("Parse: '{}'", s);
        Map<String, String> r = new HashMap<>();
        if (s != null) {
            s = s.trim();
            if (!s.isEmpty()) {
                JSONArray a;
                try {
                    a = new JSONArray(s);
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
                    LOG.debug("Unparsable '{}'", s);
                }
            }
        }
        return r;
    }

    /**
     * Read file if available and assign to configuration
     */
    private void getAaiPropertiesFile() {
        String aaiPropertiesFileName = configuration.getProperty(SECTION_MARKER_AAI, Config.AAIPROP_FILE.propertyKey);
        File f = new File(aaiPropertiesFileName);
        if (f.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(f);
                Properties defaultProps = new Properties();
                defaultProps.load(in);

                for (Config conf : Config.values()) {
                    if (conf.propertyKeySecondFile.isPresent()) {
                        String config = defaultProps.getProperty(conf.propertyKeySecondFile.get(), conf.propertyValue);
                        LOG.debug("Property file assign  {} = {} ", conf.propertyKey, config);
                        configuration.setProperty(SECTION_MARKER_AAI, conf.propertyKey, config);
                    }
                }

            } catch (IOException e) {
                LOG.warn("Problem during file read {} {}", f.getAbsoluteFile(), e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        LOG.warn("problem closing file string for {}: {}", f.getAbsoluteFile(), e);
                    }
                }
            }
        }
    }

}
