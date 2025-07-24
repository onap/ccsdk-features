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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.vescollectorconnector.impl.config;

import java.util.UUID;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;

public class VESCollectorCfgImpl implements VESCollectorCfgService, Configuration {

    private static final String SECTION_MARKER = "VESCollector";

    /** set to true if VES Collector is installed and configured */
    private static final String PROPERTY_KEY_VES_COLLECTOR_ENABLED = "VES_COLLECTOR_ENABLED";
    private static final String DEFAULT_VALUE_VES_COLLECTOR_ENABLED = "${SDNR_VES_COLLECTOR_ENABLED}";

    private static final String PROPERTY_KEY_VES_COLLECTOR_IP = "VES_COLLECTOR_IP";
    private static final String DEFAULT_VALUE_VES_COLLECTOR_IP = "${SDNR_VES_COLLECTOR_IP}";

    private static final String PROPERTY_KEY_VES_COLLECTOR_PORT = "VES_COLLECTOR_PORT";
    private static final String DEFAULT_VALUE_VES_COLLECTOR_PORT = "${SDNR_VES_COLLECTOR_PORT}";

    private static final String PROPERTY_KEY_VES_COLLECTOR_TLS_ENABLED = "VES_COLLECTOR_TLS_ENABLED";
    private static final String DEFAULT_VALUE_VES_COLLECTOR_TLS_ENABLED = "${SDNR_VES_COLLECTOR_TLS_ENABLED}";

    private static final String PROPERTY_KEY_VES_COLLECTOR_TRUST_ALL_CERTS = "VES_COLLECTOR_TRUST_ALL_CERTS";
    private static final String DEFAULT_VALUE_VES_COLLECTOR_TRUST_ALL_CERTS = "${SDNR_VES_COLLECTOR_TRUST_ALL_CERTS}";

    private static final String PROPERTY_KEY_VES_COLLECTOR_USERNAME = "VES_COLLECTOR_USERNAME";
    private static final String DEFAULT_VALUE_VES_COLLECTOR_USERNAME = "${SDNR_VES_COLLECTOR_USERNAME}";

    private static final String PROPERTY_KEY_VES_COLLECTOR_PASSWORD = "VES_COLLECTOR_PASSWORD";
    private static final String DEFAULT_VALUE_VES_COLLECTOR_PASSWORD = "${SDNR_VES_COLLECTOR_PASSWORD}";

    private static final String PROPERTY_KEY_VES_COLLECTOR_VERSION = "VES_COLLECTOR_VERSION";
    private static final String DEFAULT_VALUE_VES_COLLECTOR_VERSION = "${SDNR_VES_COLLECTOR_VERSION}";
    private static final String DEFAULT_VALUE_VES_COLLECTOR_VERSION_NA = "v7";

    private static final String PROPERTY_KEY_REPORTING_ENTITY_NAME = "REPORTING_ENTITY_NAME";
    private static final String DEFAULT_VALUE_REPORTING_ENTITY_NAME = "${SDNR_VES_COLLECTOR_REPORTING_ENTITY_NAME}";
    private static final String DEFAULT_VALUE_REPORTING_ENTITY_NAME_NA = "ONAP SDN-R";
    private static final String PROPERTY_KEY_REPORTING_ENTITY_ID = "REPORTING_ENTITY_ID";
    private static final String DEFAULT_VALUE_REPORTING_ENTITY_ID = "${SDNR_VES_COLLECTOR_REPORTING_ENTITY_ID}";
    private static final String DEFAULT_VALUE_REPORTING_ENTITY_ID_NA = UUID.randomUUID().toString();

    private static final String PROPERTY_KEY_EVENTLOG_DETAIL = "EVENTLOG_MSG_DETAIL";
    private static final String DEFAULT_VALUE_EVENTLOG_DETAIL = "${SDNR_VES_COLLECTOR_EVENTLOG_MSG_DETAIL}";
    private static final String DEFAULT_VALUE_EVENTLOG_DETAIL_NA = "SHORT"; // "SHORT", "MEDIUM", "LONG"


    private static ConfigurationFileRepresentation configuration;

    public VESCollectorCfgImpl(ConfigurationFileRepresentation configuration) {
        VESCollectorCfgImpl.configuration = configuration;
        VESCollectorCfgImpl.configuration.addSection(SECTION_MARKER);
        defaults();
    }

    @Override
    public String getSectionName() {
        return SECTION_MARKER;
    }

    @Override
    public String getReportingEntityId() {
        String v = configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_REPORTING_ENTITY_ID);
        return (v == null || v.isBlank()) ? DEFAULT_VALUE_REPORTING_ENTITY_ID_NA : v;
    }

    @Override
    public String getReportingEntityName() {
        String v = configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_REPORTING_ENTITY_NAME);
        return (v == null || v.isBlank()) ? DEFAULT_VALUE_REPORTING_ENTITY_NAME_NA : v;
    }

    @Override
    public String getEventLogMsgDetail() {
        String v = configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_EVENTLOG_DETAIL);
        return (v == null || v.isBlank()) ? DEFAULT_VALUE_EVENTLOG_DETAIL_NA : v;
    }

    @Override
    public boolean isVESCollectorEnabled() {
        return configuration.getPropertyBoolean(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_ENABLED);
    }

    public boolean getTLSEnabled() {
        return configuration.getPropertyBoolean(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_TLS_ENABLED);
    }

    public String getUsername() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_USERNAME);
    }

    public String getPassword() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_PASSWORD);
    }

    public String getIP() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_IP);
    }

    public String getPort() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_PORT);
    }

    public String getVersion() {
        String v = configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_VERSION);
        return (v == null || v.isBlank()) ? DEFAULT_VALUE_VES_COLLECTOR_VERSION_NA : v;
    }

    public boolean isTrustAllCerts() {
        return configuration.getPropertyBoolean(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_TRUST_ALL_CERTS);
    }

    @Override
    public synchronized void defaults() {
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_ENABLED,
                DEFAULT_VALUE_VES_COLLECTOR_ENABLED);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_TLS_ENABLED,
                DEFAULT_VALUE_VES_COLLECTOR_TLS_ENABLED);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_TRUST_ALL_CERTS,
                DEFAULT_VALUE_VES_COLLECTOR_TRUST_ALL_CERTS);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_USERNAME,
                DEFAULT_VALUE_VES_COLLECTOR_USERNAME);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_PASSWORD,
                DEFAULT_VALUE_VES_COLLECTOR_PASSWORD);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_IP,
                DEFAULT_VALUE_VES_COLLECTOR_IP);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_PORT,
                DEFAULT_VALUE_VES_COLLECTOR_PORT);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_VES_COLLECTOR_VERSION,
                DEFAULT_VALUE_VES_COLLECTOR_VERSION);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_REPORTING_ENTITY_NAME,
                DEFAULT_VALUE_REPORTING_ENTITY_NAME);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_REPORTING_ENTITY_ID,
                DEFAULT_VALUE_REPORTING_ENTITY_ID);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_EVENTLOG_DETAIL,
                DEFAULT_VALUE_EVENTLOG_DETAIL);
    }

}
