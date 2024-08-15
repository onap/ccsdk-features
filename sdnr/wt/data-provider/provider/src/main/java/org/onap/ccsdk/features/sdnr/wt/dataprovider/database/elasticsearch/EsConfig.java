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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo.Protocol;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.IEsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class EsConfig implements Configuration, IEsConfig {

    private static final Logger LOG = LoggerFactory.getLogger(EsConfig.class);

    public static final String SECTION_MARKER_ES = "es";

    private static final String PROPERTY_KEY_DBHOSTS = "esHosts";
    private static final String PROPERTY_KEY_TRUSTALLCERTS = "esTrustAllCerts";
    private static final String PROPERTY_KEY_ARCHIVE_LIMIT = "esArchiveLifetimeSeconds";
    private static final String PROPERTY_KEY_CLUSTER = "esCluster";
    private static final String PROPERTY_KEY_ARCHIVE_INTERVAL = "esArchiveCheckIntervalSeconds";
    private static final String PROPERTY_KEY_NODE = "esNode";
    private static final String PROPERTY_KEY_AUTH_USERNAME = "esAuthUsername";
    private static final String PROPERTY_KEY_AUTH_PASSWORD = "esAuthPassword";
    private static final String PROPERTY_KEY_FULLSIZE = "esFullsize";


    private static String defaultHostinfo = "${SDNRDBURL}";
    private static final String DEFAULT_VALUE_CLUSTER = "";
    /** check db data in this interval [in seconds] 0 deactivated */
    private static final String DEFAULT_ARCHIVE_INTERVAL_SEC = "0";
    /** keep data for this time [in seconds] 30 days */
    private static final String DEFAULT_ARCHIVE_LIMIT_SEC = String.valueOf(60L * 60L * 24L * 30L);
    private static final String DEFAULT_VALUE_NODE = "elasticsearchnode";
    private static final String DEFAULT_VALUE_DBUSERNAME = "${SDNRDBUSERNAME}";
    private static final String DEFAULT_VALUE_DBPASSWORD = "${SDNRDBPASSWORD}";
    private static final String DEFAULT_VALUE_TRUSTALLCERTS = "${SDNRDBTRUSTALLCERTS}";
    private static final String DEFAULT_VALUE_FULLSIZE = "${SDNRDBFULLSIZEREQUESTS}";

    private final ConfigurationFileRepresentation configuration;

    public EsConfig(ConfigurationFileRepresentation configuration) {

        this.configuration = configuration;
        this.configuration.addSection(SECTION_MARKER_ES);
        defaults();
    }

    /*
     * Setter
     */

    public void setNode(String nodeName) {
        configuration.setProperty(SECTION_MARKER_ES, PROPERTY_KEY_NODE, nodeName);
    }

    /*
     * Getter
     */

    public String getNode() {
        return configuration.getProperty(SECTION_MARKER_ES, PROPERTY_KEY_NODE);
    }

    public HostInfo[] getHosts() {
        String dbHosts = configuration.getProperty(SECTION_MARKER_ES, PROPERTY_KEY_DBHOSTS);
        return parseHosts(dbHosts);
    }

    public void setHosts(HostInfo[] hosts) {
        this.configuration.setProperty(SECTION_MARKER_ES, PROPERTY_KEY_DBHOSTS, printHosts(hosts));
    }

    @Override
    public String getCluster() {
        return configuration.getProperty(SECTION_MARKER_ES, PROPERTY_KEY_ARCHIVE_INTERVAL);
    }

    public void setCluster(String cluster) {
        configuration.setProperty(SECTION_MARKER_ES, PROPERTY_KEY_CLUSTER, cluster);
    }

    public boolean hasBasicAuthCredentials() {
        return this.getBasicAuthUsername() != null && this.getBasicAuthPassword() != null
                && !this.getBasicAuthUsername().isEmpty() && !this.getBasicAuthPassword().isEmpty();
    }

    public String getBasicAuthUsername() {
        return this.configuration.getProperty(SECTION_MARKER_ES, PROPERTY_KEY_AUTH_USERNAME);
    }

    public String getBasicAuthPassword() {
        return this.configuration.getProperty(SECTION_MARKER_ES, PROPERTY_KEY_AUTH_PASSWORD);
    }

    @Override
    public long getArchiveCheckIntervalSeconds() {
        return configuration.getPropertyLong(SECTION_MARKER_ES, PROPERTY_KEY_ARCHIVE_INTERVAL).orElse(0L);
    }

    public boolean trustAllCerts() {
        return configuration.getPropertyBoolean(SECTION_MARKER_ES, PROPERTY_KEY_TRUSTALLCERTS);
    }

    public void setArchiveCheckIntervalSeconds(long seconds) {
        configuration.setProperty(SECTION_MARKER_ES, PROPERTY_KEY_ARCHIVE_INTERVAL, seconds);
    }

    @Override
    public long getArchiveLifetimeSeconds() {
        return configuration.getPropertyLong(SECTION_MARKER_ES, PROPERTY_KEY_ARCHIVE_LIMIT).orElse(0L);
    }

    public void setArchiveLimit(long seconds) {
        configuration.setProperty(SECTION_MARKER_ES, PROPERTY_KEY_ARCHIVE_LIMIT, seconds);
    }

    @Override
    public String getSectionName() {
        return SECTION_MARKER_ES;
    }

    @Override
    public synchronized void defaults() {
        // Add default if not available
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_ES, PROPERTY_KEY_DBHOSTS, defaultHostinfo);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_ES, PROPERTY_KEY_ARCHIVE_LIMIT,
                DEFAULT_ARCHIVE_LIMIT_SEC);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_ES, PROPERTY_KEY_CLUSTER, DEFAULT_VALUE_CLUSTER);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_ES, PROPERTY_KEY_ARCHIVE_INTERVAL,
                DEFAULT_ARCHIVE_INTERVAL_SEC);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_ES, PROPERTY_KEY_NODE, DEFAULT_VALUE_NODE);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_ES, PROPERTY_KEY_AUTH_USERNAME,
                DEFAULT_VALUE_DBUSERNAME);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_ES, PROPERTY_KEY_AUTH_PASSWORD,
                DEFAULT_VALUE_DBPASSWORD);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_ES, PROPERTY_KEY_TRUSTALLCERTS,
                DEFAULT_VALUE_TRUSTALLCERTS);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_ES, PROPERTY_KEY_FULLSIZE,
                DEFAULT_VALUE_FULLSIZE);

    }

    @Override
    public void unregisterConfigChangedListener(IConfigChangedListener archiveCleanService) {
        configuration.unregisterConfigChangedListener(archiveCleanService);
    }

    @Override
    public void registerConfigChangedListener(IConfigChangedListener archiveCleanService) {
        configuration.registerConfigChangedListener(archiveCleanService);
    }

    /** @TODO Shift to own class **/
    private static String printHosts(HostInfo[] h) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < h.length; i++) {
            sb.append(h[i].toUrl());
            if (i != h.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /** @TODO Shift to own class **/
    private static HostInfo[] parseHosts(String string) {
        List<HostInfo> infos = new ArrayList<>();
        String[] list = string.split(",");
        if (list.length > 0) {
            for (String item : list) {
                try {
                    URL url = new URL(item);
                    infos.add(new HostInfo(url.getHost(), url.getPort(), Protocol.getValueOf(url.getProtocol())));
                } catch (MalformedURLException e) {
                    LOG.warn("problem parsing url {} : {}", item, e.getMessage());
                }
            }
        }
        HostInfo[] a = new HostInfo[infos.size()];
        return infos.toArray(a);
    }

    @Override
    public String toString() {
        return "EsConfig [getNode()=" + getNode() + ", getHosts()=" + Arrays.toString(getHosts()) + ", getCluster()="
                + getCluster() + ", getArchiveCheckIntervalSeconds()=" + getArchiveCheckIntervalSeconds()
                + ", getArchiveLifetimeSeconds()=" + getArchiveLifetimeSeconds() + ", getSectionName()="
                + getSectionName() + "]";
    }

    @Override
    public boolean doFullsizeRequests() {
        return configuration.getPropertyBoolean(SECTION_MARKER_ES, PROPERTY_KEY_FULLSIZE);
    }

}
