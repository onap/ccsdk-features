/*******************************************************************************
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.config.impl;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.IniConfigurationFile;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.IniConfigurationFile.ConfigurationException;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.BaseSubConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.ISubConfigHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiConfig extends BaseSubConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AaiConfig.class);

    private static final String SECTION_MARKER_AAI = "aai";

    private static final String PROPERTY_KEY_AAIPROP_FILE ="aaiPropertiesFile";
    private static final String PROPERTY_KEY_BASEURL = "aaiUrl";
    private static final String PROPERTY_KEY_USERCREDENTIALS = "aaiUserCredentials";
    private static final String PROPERTY_KEY_HEADERS = "aaiHeaders";
    private static final String PROPERTY_KEY_DELETEONMOUNTPOINTREMOVED = "aaiDeleteOnMountpointRemove";
    private static final String PROPERTY_KEY_TRUSTALLCERTS = "aaiTrustAllCerts";
    private static final String PROPERTY_KEY_APIVERSION = "aaiApiVersion";
    private static final String PROPERTY_KEY_PCKS12CERTFILENAME = "aaiPcks12ClientCertFile";
    private static final String PROPERTY_KEY_PCKS12PASSPHRASE = "aaiPcks12ClientCertPassphrase";
    private static final String PROPERTY_KEY_CONNECTIONTIMEOUT = "aaiClientConnectionTimeout";
    private static final String PROPERTY_KEY_APPLICATIONID = "aaiApplicationId";

    private static final String DEFAULT_VALUE_AAIPROP_FILE ="null";
    private static final String DEFAULT_VALUE_BASEURL = "off";
    private static final String DEFAULT_VALUE_APPLICATION = "SDNR";
    private static final String DEFAULT_VALUE_USERNAME = "";
    private static final String DEFAULT_VALUE_USERPASSWORD = "";
    private static final String DEFAULT_VALUE_USERCREDENTIALS = "";
    private static final String DEFAULT_VALUE_HEADERS = "[\"X-TransactionId: 9999\"]";
    private static final boolean DEFAULT_VALUE_DELETEONMOUNTPOINTREMOVED = false;
    private static final boolean DEFAULT_VALUE_TRUSTALLCERTS = false;
    private static final int DEFAULT_VALUE_CONNECTION_TIMEOUT = 30000;    //in ms
    private static final String DEFAULT_VALUE_APIVERSION = "aai/v13";
    private static final String DEFAULT_VALUE_PCKS12CERTFILENAME ="";
    private static final String DEFAULT_VALUE_PCKS12PASSPHRASE = "";
    private static final String DEFAULT_VALUE_APPLICATIONID = "SDNR";

    private static final String HEADER_KEY_APPLICATION = "X-FromAppId";


    private static AaiConfig aaiConfig;

    private final String aaiPropFile;
    private final String baseUrl;
    private String apiVersion;
    private String applicationIdentifier;
    private String username;
    private String password;
    private String pcks12CertificateFilename;
    private String pcks12CertificatePassphrase;
    private int connectionTimeout;
    private final boolean deleteOnMountPointRemoved;
    private final boolean trustAllCerts;

    public boolean doDeleteOnMountPointRemoved() {
        return this.deleteOnMountPointRemoved;
    }

    private Map<String, String> headers;


    private AaiConfig() {
        super();
        this.aaiPropFile = DEFAULT_VALUE_AAIPROP_FILE;
        this.apiVersion=DEFAULT_VALUE_APIVERSION;
        this.applicationIdentifier = DEFAULT_VALUE_APPLICATION;
        this.baseUrl = DEFAULT_VALUE_BASEURL;
        this.username = DEFAULT_VALUE_USERNAME;
        this.password = DEFAULT_VALUE_USERPASSWORD;
        this.deleteOnMountPointRemoved = DEFAULT_VALUE_DELETEONMOUNTPOINTREMOVED;
        this.trustAllCerts=DEFAULT_VALUE_TRUSTALLCERTS;
        this.applicationIdentifier=DEFAULT_VALUE_APPLICATIONID;
    }

    public AaiConfig(IniConfigurationFile config, ISubConfigHandler configHandler) throws ConfigurationException {
        this(config, configHandler, true);
    }

    public AaiConfig(IniConfigurationFile config, ISubConfigHandler configHandler, boolean save)
            throws ConfigurationException {
        super(config, configHandler, SECTION_MARKER_AAI);
        // load
        this.aaiPropFile=this.getString(PROPERTY_KEY_AAIPROP_FILE, "");
        AaiClientPropertiesFile aaiProperties = new AaiClientPropertiesFile(this.aaiPropFile);
        String defBaseUrl=DEFAULT_VALUE_BASEURL;
        String defPCKSCertFilename=DEFAULT_VALUE_PCKS12CERTFILENAME;
        String defPCKSPassphrase=DEFAULT_VALUE_PCKS12PASSPHRASE;
        String defApplicationId=DEFAULT_VALUE_APPLICATION;
        int defconnectionTimeout=DEFAULT_VALUE_CONNECTION_TIMEOUT;
        boolean loaded=false;
        if(aaiProperties.exists())
        {
            LOG.debug("found another aaiclient.properties file");
            try
            {
                aaiProperties.load();
                loaded=true;
                LOG.debug("loaded successfully");
            }
            catch(IOException|NumberFormatException e)
            {
                LOG.warn("problem loading external properties file "+aaiProperties.getFilename()+": "+e.getMessage());
            }
            if(loaded)    //preload new default values
            {
                String value;
                value = aaiProperties.getRemoteUrl();
                if (value != null) {
                    defBaseUrl = value;
                }
                value = aaiProperties.getPCKS12CertFilename();
                if (value != null) {
                    defPCKSCertFilename = value;
                }
                value = aaiProperties.getPCKS12Passphrase();
                if (value != null) {
                    defPCKSPassphrase = value;
                }
                value = aaiProperties.getApplicationIdentifier();
                if (value != null) {
                    defApplicationId = value;
                }
            }
        } else {
            LOG.debug("no aaiclient.properties file found");
        }


        this.baseUrl = this.getString(PROPERTY_KEY_BASEURL, defBaseUrl);
        this.apiVersion=this.getString(PROPERTY_KEY_APIVERSION,DEFAULT_VALUE_APIVERSION);
        String credentials = this.getString(PROPERTY_KEY_USERCREDENTIALS, DEFAULT_VALUE_USERCREDENTIALS);
        if (credentials.contains(":")) {
            try {
                this.username = credentials.split(":")[0];
                this.password = credentials.split(":")[1];
            } catch (Exception e) {
                this.username = DEFAULT_VALUE_USERNAME;
                this.password = DEFAULT_VALUE_USERPASSWORD;
            }
        } else {
            this.username = DEFAULT_VALUE_USERNAME;
            this.password = DEFAULT_VALUE_USERPASSWORD;
        }
        this.headers = _parseHeadersMap(this.getString(PROPERTY_KEY_HEADERS, DEFAULT_VALUE_HEADERS));
        this.applicationIdentifier = this.getString(PROPERTY_KEY_APPLICATIONID, defApplicationId);
        this.pcks12CertificateFilename=this.getString(PROPERTY_KEY_PCKS12CERTFILENAME, defPCKSCertFilename);
        this.pcks12CertificatePassphrase=this.getString(PROPERTY_KEY_PCKS12PASSPHRASE, defPCKSPassphrase);
        this.connectionTimeout = this.getInt(PROPERTY_KEY_CONNECTIONTIMEOUT, defconnectionTimeout);
        this.deleteOnMountPointRemoved = this.getBoolean(PROPERTY_KEY_DELETEONMOUNTPOINTREMOVED,
                DEFAULT_VALUE_DELETEONMOUNTPOINTREMOVED);
        this.trustAllCerts = this.getBoolean(PROPERTY_KEY_TRUSTALLCERTS, DEFAULT_VALUE_TRUSTALLCERTS);

        boolean missing=!this.hasKey(PROPERTY_KEY_APPLICATIONID)|| !this.hasKey(PROPERTY_KEY_CONNECTIONTIMEOUT)||
                !this.hasKey(PROPERTY_KEY_TRUSTALLCERTS) || !this.hasKey(PROPERTY_KEY_PCKS12CERTFILENAME) ||
                !this.hasKey(PROPERTY_KEY_PCKS12PASSPHRASE);
        if(missing) {
            LOG.debug("some params missing in config file");
        }
        //re-save if external aaiproperties file changed to show that params are submitted internally
        if(missing || aaiConfig!=null && aaiConfig!=this && (
                !propertyEquals(aaiConfig.aaiPropFile, this.aaiPropFile) ||
                !propertyEquals(aaiConfig.pcks12CertificateFilename, this.pcks12CertificateFilename) ||
                !propertyEquals(aaiConfig.pcks12CertificatePassphrase, this.pcks12CertificatePassphrase) ||
                !propertyEquals(aaiConfig.connectionTimeout, this.connectionTimeout)

                ))
        {
            LOG.debug("force saving because of reload changes from remote file");
            save=true;
        }
        if (save) {
            config.setProperty(SECTION_MARKER_AAI + "." + PROPERTY_KEY_BASEURL, this.baseUrl);
            config.setProperty(SECTION_MARKER_AAI + "." + PROPERTY_KEY_USERCREDENTIALS,
                    nullorempty(this.username) && nullorempty(this.password)?"":this.username + ":" + this.password);
            config.setProperty(SECTION_MARKER_AAI + "." + PROPERTY_KEY_HEADERS, _printHeadersMap(this.headers));
            config.setProperty(SECTION_MARKER_AAI + "." + PROPERTY_KEY_DELETEONMOUNTPOINTREMOVED,
                    this.deleteOnMountPointRemoved);
            config.setProperty(SECTION_MARKER_AAI + "." + PROPERTY_KEY_TRUSTALLCERTS, this.trustAllCerts);
            config.setProperty(SECTION_MARKER_AAI+"."+PROPERTY_KEY_AAIPROP_FILE, this.aaiPropFile);
            config.setProperty(SECTION_MARKER_AAI+"."+PROPERTY_KEY_APIVERSION,this.apiVersion);
            config.setProperty(SECTION_MARKER_AAI+"."+PROPERTY_KEY_APPLICATIONID, this.applicationIdentifier);
            config.setProperty(SECTION_MARKER_AAI+"."+PROPERTY_KEY_CONNECTIONTIMEOUT, this.connectionTimeout);
            /*if(this.pcks12CertificateFilename !=null && !this.pcks12CertificateFilename.isEmpty() &&
                    this.pcks12CertificatePassphrase!=null && !this.pcks12CertificatePassphrase.isEmpty())*/
            {
                LOG.debug("no client credentials to save");
                config.setProperty(SECTION_MARKER_AAI+"."+PROPERTY_KEY_PCKS12CERTFILENAME, this.pcks12CertificateFilename);
                config.setProperty(SECTION_MARKER_AAI+"."+PROPERTY_KEY_PCKS12PASSPHRASE, this.pcks12CertificatePassphrase);
            }
            LOG.debug("save");
            this.save();
        }
    }

    private boolean nullorempty(String s) {
        return s==null || s.isEmpty();
    }

    public boolean isOff() {
        return this.baseUrl == null || this.baseUrl.toLowerCase().equals("off");
    }

    private static boolean propertyEquals(final Object p1,final Object p2)
    {
        return p1==null && p2==null || p1 != null && p1.equals(p2);
    }
    private static boolean propertyEquals(final int p1,final int p2)
    {
        return p1==p2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (aaiPropFile == null ? 0 : aaiPropFile.hashCode());
        result = prime * result + (apiVersion == null ? 0 : apiVersion.hashCode());
        result = prime * result + (applicationIdentifier == null ? 0 : applicationIdentifier.hashCode());
        result = prime * result + (baseUrl == null ? 0 : baseUrl.hashCode());
        result = prime * result + connectionTimeout;
        result = prime * result + (deleteOnMountPointRemoved ? 1231 : 1237);
        result = prime * result + (headers == null ? 0 : headers.hashCode());
        result = prime * result + (password == null ? 0 : password.hashCode());
        result = prime * result + (pcks12CertificateFilename == null ? 0 : pcks12CertificateFilename.hashCode());
        result = prime * result + (pcks12CertificatePassphrase == null ? 0 : pcks12CertificatePassphrase.hashCode());
        result = prime * result + (trustAllCerts ? 1231 : 1237);
        result = prime * result + (username == null ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AaiConfig other = (AaiConfig) obj;
        if (aaiPropFile == null) {
            if (other.aaiPropFile != null) {
                return false;
            }
        } else if (!aaiPropFile.equals(other.aaiPropFile)) {
            return false;
        }
        if (apiVersion == null) {
            if (other.apiVersion != null) {
                return false;
            }
        } else if (!apiVersion.equals(other.apiVersion)) {
            return false;
        }
        if (applicationIdentifier == null) {
            if (other.applicationIdentifier != null) {
                return false;
            }
        } else if (!applicationIdentifier.equals(other.applicationIdentifier)) {
            return false;
        }
        if (baseUrl == null) {
            if (other.baseUrl != null) {
                return false;
            }
        } else if (!baseUrl.equals(other.baseUrl)) {
            return false;
        }
        if (connectionTimeout != other.connectionTimeout) {
            return false;
        }
        if (deleteOnMountPointRemoved != other.deleteOnMountPointRemoved) {
            return false;
        }
        if (headers == null) {
            if (other.headers != null) {
                return false;
            }
        } else if (!headers.equals(other.headers)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (pcks12CertificateFilename == null) {
            if (other.pcks12CertificateFilename != null) {
                return false;
            }
        } else if (!pcks12CertificateFilename.equals(other.pcks12CertificateFilename)) {
            return false;
        }
        if (pcks12CertificatePassphrase == null) {
            if (other.pcks12CertificatePassphrase != null) {
                return false;
            }
        } else if (!pcks12CertificatePassphrase.equals(other.pcks12CertificatePassphrase)) {
            return false;
        }
        if (trustAllCerts != other.trustAllCerts) {
            return false;
        }
        if (username == null) {
            if (other.username != null) {
                return false;
            }
        } else if (!username.equals(other.username)) {
            return false;
        }
        return true;
    }

    public String getBaseUrl() {
        String url=this.baseUrl;
        if(!url.endsWith("/")) {
            url+="/";
        }
        if(this.apiVersion.startsWith("/")) {
            this.apiVersion=this.apiVersion.substring(1);
        }
        return url+this.apiVersion;
    }

    public Map<String, String> getHeaders() {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.put(HEADER_KEY_APPLICATION, this.applicationIdentifier);
        String s = this.headers.getOrDefault("Authorization", null);
        if (nullorempty(s) && !nullorempty(this.username) && !nullorempty(this.password)) {
            this.headers.put("Authorization", "Basic "
                    + new String(Base64.getEncoder().encode((this.username + ":" + this.password).getBytes())));
        }
        return this.headers;
    }

    @Override
    public String toString() {
        return "AaiConfig [aaiPropFile=" + aaiPropFile + ", baseUrl=" + baseUrl + ", apiVersion=" + apiVersion
                + ", applicationIdentifier=" + applicationIdentifier + ", username=" + username + ", password="
                + password + ", pcks12CertificateFilename=" + pcks12CertificateFilename
                + ", pcks12CertificatePassphrase=" + pcks12CertificatePassphrase + ", connectionTimeout="
                + connectionTimeout + ", deleteOnMountPointRemoved=" + deleteOnMountPointRemoved + ", trustAllCerts="
                + trustAllCerts + ", headers=" + this.getHeaders() + "]";
    }

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

    private static Map<String, String> _parseHeadersMap(String s) throws JSONException {
        Map<String, String> r = new HashMap<>();
        JSONArray a = new JSONArray(s);
        if (a != null && a.length() > 0) {
            for (int i = 0; i < a.length(); i++) {
                String item = a.getString(i);
                String[] hlp = item.split(":");
                if (hlp.length > 1) {
                    r.put(hlp[0], hlp[1]);
                }
            }
        }
        return r;
    }

    public static boolean isInstantiated() {
        return aaiConfig != null;
    }

    public static AaiConfig getDefaultConfiguration() {
        return new AaiConfig();
    }

    public static AaiConfig getAai(IniConfigurationFile config, ISubConfigHandler configHandler) {
        if (aaiConfig == null) {
            try {
                aaiConfig = new AaiConfig(config, configHandler);
            } catch (ConfigurationException e) {
                aaiConfig = AaiConfig.getDefaultConfiguration();
            }
        }
        return aaiConfig;
    }

    public static @Nullable AaiConfig reload() {
        if (aaiConfig == null) {
            return null;
        }
        AaiConfig tmpConfig;
        try {
            tmpConfig = new AaiConfig(aaiConfig.getConfig(), aaiConfig.getConfigHandler(), false);
        } catch (ConfigurationException e) {
            tmpConfig = AaiConfig.getDefaultConfiguration();
            LOG.warn("problem loading config: "+e.getMessage());
        }
        aaiConfig = tmpConfig;
        return aaiConfig;
    }

    public boolean getTrustAll() {
        return this.trustAllCerts;
    }

    public String getPcks12CertificateFilename() {
        return this.pcks12CertificateFilename;
    }

    public String getPcks12CertificatePassphrase() {
        return this.pcks12CertificatePassphrase;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public static void clear() {
        aaiConfig=null;
    }

}
