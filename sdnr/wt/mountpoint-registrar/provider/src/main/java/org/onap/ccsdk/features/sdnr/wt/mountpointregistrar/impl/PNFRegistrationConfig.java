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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;

public class PNFRegistrationConfig implements Configuration {
    private static final String SECTION_MARKER = "pnfRegistration";

    public static final String PROPERTY_KEY_CONSUMER_TRANSPORTTYPE = "TransportType";
    private static final String DEFAULT_VALUE_CONSUMER_TRANSPORTTYPE = "HTTPNOAUTH";

    public static final String PROPERTY_KEY_CONSUMER_PROTOCOL = "Protocol";
    private static final String DEFAULT_VALUE_CONSUMER_PROTOCOL = "http";

    public static final String PROPERTY_KEY_CONSUMER_USERNAME = "username";
    private static final String DEFAULT_VALUE_CONSUMER_USERNAME = "${DMAAP_PNFREG_TOPIC_USERNAME}";

    public static final String PROPERTY_KEY_CONSUMER_PASSWORD = "password";
    private static final String DEFAULT_VALUE_CONSUMER_PASSWORD = "${DMAAP_PNFREG_TOPIC_PASSWORD}";

    public static final String PROPERTY_KEY_CONSUMER_HOST_PORT = "host";
    private static final String DEFAULT_VALUE_CONSUMER_HOST_PORT = "onap-dmaap:3904";

    public static final String PROPERTY_KEY_CONSUMER_TOPIC = "topic";
    private static final String DEFAULT_VALUE_CONSUMER_TOPIC = "unauthenticated.VES_PNFREG_OUTPUT";

    public static final String PROPERTY_KEY_CONSUMER_CONTENTTYPE = "contenttype";
    private static final String DEFAULT_VALUE_CONSUMER_CONTENTTYPE = "application/json";

    public static final String PROPERTY_KEY_CONSUMER_GROUP = "group";
    private static final String DEFAULT_VALUE_CONSUMER_GROUP = "myG";

    public static final String PROPERTY_KEY_CONSUMER_ID = "id";
    private static final String DEFAULT_VALUE_CONSUMER_ID = "C1";

    public static final String PROPERTY_KEY_CONSUMER_TIMEOUT = "timeout";
    private static final String DEFAULT_VALUE_CONSUMER_TIMEOUT = "20000";

    public static final String PROPERTY_KEY_CONSUMER_LIMIT = "limit";
    private static final String DEFAULT_VALUE_CONSUMER_LIMIT = "10000";

    public static final String PROPERTY_KEY_CONSUMER_FETCHPAUSE = "fetchPause";
    private static final String DEFAULT_VALUE_CONSUMER_FETCHPAUSE = "5000";

    public static final String PROPERTY_KEY_CONSUMER_CLIENT_READTIMEOUT = "jersey.config.client.readTimeout";
    private static final String DEFAULT_VALUE_CONSUMER_CLIENT_READTIMEOUT = "25000";
    
    public static final String PROPERTY_KEY_CONSUMER_CLIENT_CONNECTTIMEOUT = "jersey.config.client.connectTimeout";
    private static final String DEFAULT_VALUE_CONSUMER_CLIENT_CONNECTTIMEOUT = "25000";
    
    public static final String PROPERTY_KEY_CONSUMER_CLIENT_HTTPPROXY_AUTH_USER = "jersey.config.client.proxy.username";
    private static final String DEFAULT_VALUE_CONSUMER_CLIENT_HTTPPROXY_AUTH_USER = "${HTTP_PROXY_USERNAME}";
    
    public static final String PROPERTY_KEY_CONSUMER_CLIENT_HTTPPROXY_AUTH_PASSWORD = "jersey.config.client.proxy.password";
    private static final String DEFAULT_VALUE_CONSUMER_CLIENT_HTTPPROXY_AUTH_PASSWORD = "${HTTP_PROXY_PASSWORD}";
    
    public static final String PROPERTY_KEY_CONSUMER_CLIENT_HTTPPROXY_URI = "jersey.config.client.proxy.uri";
    private static final String DEFAULT_VALUE_CONSUMER_CLIENT_HTTPPROXY_URI = "${HTTP_PROXY_URI}";
    
    private final ConfigurationFileRepresentation configuration;

    public PNFRegistrationConfig(ConfigurationFileRepresentation configuration) {
        this.configuration = configuration;
        this.configuration.addSection(SECTION_MARKER);
        defaults();
    }

    @Override
    public String getSectionName() {
        return SECTION_MARKER;
    }

    @Override
    public void defaults() {

        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_TRANSPORTTYPE,
                DEFAULT_VALUE_CONSUMER_TRANSPORTTYPE);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_PROTOCOL,
                DEFAULT_VALUE_CONSUMER_PROTOCOL);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_USERNAME,
                DEFAULT_VALUE_CONSUMER_USERNAME);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_PASSWORD,
                DEFAULT_VALUE_CONSUMER_PASSWORD);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_HOST_PORT,
                DEFAULT_VALUE_CONSUMER_HOST_PORT);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_TOPIC,
                DEFAULT_VALUE_CONSUMER_TOPIC);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_CONTENTTYPE,
                DEFAULT_VALUE_CONSUMER_CONTENTTYPE);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_GROUP,
                DEFAULT_VALUE_CONSUMER_GROUP);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_ID, DEFAULT_VALUE_CONSUMER_ID);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_TIMEOUT,
                DEFAULT_VALUE_CONSUMER_TIMEOUT);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_LIMIT,
                DEFAULT_VALUE_CONSUMER_LIMIT);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_FETCHPAUSE,
                DEFAULT_VALUE_CONSUMER_FETCHPAUSE);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_CLIENT_READTIMEOUT,
                DEFAULT_VALUE_CONSUMER_CLIENT_READTIMEOUT);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_CLIENT_CONNECTTIMEOUT,
                DEFAULT_VALUE_CONSUMER_CLIENT_CONNECTTIMEOUT);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_CLIENT_HTTPPROXY_AUTH_USER,
        		DEFAULT_VALUE_CONSUMER_CLIENT_HTTPPROXY_AUTH_USER);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_CLIENT_HTTPPROXY_AUTH_PASSWORD,
        		DEFAULT_VALUE_CONSUMER_CLIENT_HTTPPROXY_AUTH_PASSWORD);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_CLIENT_HTTPPROXY_URI,
        		DEFAULT_VALUE_CONSUMER_CLIENT_HTTPPROXY_URI);
    }

    public String getHostPort() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_HOST_PORT);
    }

    public String getTransportType() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_TRANSPORTTYPE);
    }

    public String getProtocol() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_PROTOCOL);
    }

    public String getUsername() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_USERNAME);
    }

    public String getPassword() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_PASSWORD);
    }

    public String getTopic() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_TOPIC);
    }

    public String getConsumerGroup() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_GROUP);
    }

    public String getConsumerId() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_ID);
    }

    public String getTimeout() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_TIMEOUT);
    }

    public String getLimit() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_LIMIT);
    }

    public String getFetchPause() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_FETCHPAUSE);
    }

    public String getContenttype() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_CONTENTTYPE);
    }

    public String getClientReadTimeout() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_CLIENT_READTIMEOUT);
    }

    public String getClientConnectTimeout() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_CLIENT_CONNECTTIMEOUT);
    }
    
    public String getHTTPProxyURI() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_CLIENT_HTTPPROXY_URI);
    }
    
    public String getHTTPProxyUsername() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_CLIENT_HTTPPROXY_AUTH_USER);
    }
    
    public String getHTTPProxyPassword() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_CONSUMER_CLIENT_HTTPPROXY_AUTH_PASSWORD);
    }
    
}
