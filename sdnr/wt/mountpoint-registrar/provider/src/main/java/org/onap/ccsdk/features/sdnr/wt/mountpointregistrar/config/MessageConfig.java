/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt mountpoint-registrar
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;


public abstract class MessageConfig implements Configuration {
    protected String sectionMarker;

    public static final String PROPERTY_KEY_CONSUMER_TOPIC = "topic";

    public static final String PROPERTY_KEY_CONSUMER_GROUP = "consumerGroup";
    private static final String DEFAULT_VALUE_CONSUMER_GROUP = "myG";

    public static final String PROPERTY_KEY_CONSUMER_ID = "consumerID";
    private static final String DEFAULT_VALUE_CONSUMER_ID = "C1";

    public static final String PROPERTY_KEY_CONSUMER_TIMEOUT = "timeout";
    private static final String DEFAULT_VALUE_CONSUMER_TIMEOUT = "2000";

    public static final String PROPERTY_KEY_CONSUMER_LIMIT = "limit";
    private static final String DEFAULT_VALUE_CONSUMER_LIMIT = "1000";

    public static final String PROPERTY_KEY_CONSUMER_FETCHPAUSE = "fetchPause";
    private static final String DEFAULT_VALUE_CONSUMER_FETCHPAUSE = "5000";

    protected ConfigurationFileRepresentation configuration;

    public MessageConfig(ConfigurationFileRepresentation configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getSectionName() {
        return sectionMarker;
    }

    @Override
    public void defaults() {
        configuration.setPropertyIfNotAvailable(sectionMarker, PROPERTY_KEY_CONSUMER_GROUP,
                DEFAULT_VALUE_CONSUMER_GROUP);
        configuration.setPropertyIfNotAvailable(sectionMarker, PROPERTY_KEY_CONSUMER_ID, DEFAULT_VALUE_CONSUMER_ID);
        configuration.setPropertyIfNotAvailable(sectionMarker, PROPERTY_KEY_CONSUMER_TIMEOUT,
                DEFAULT_VALUE_CONSUMER_TIMEOUT);
        configuration.setPropertyIfNotAvailable(sectionMarker, PROPERTY_KEY_CONSUMER_LIMIT,
                DEFAULT_VALUE_CONSUMER_LIMIT);
        configuration.setPropertyIfNotAvailable(sectionMarker, PROPERTY_KEY_CONSUMER_FETCHPAUSE,
                DEFAULT_VALUE_CONSUMER_FETCHPAUSE);
    }

    public String getTopic() {
        return configuration.getProperty(sectionMarker, PROPERTY_KEY_CONSUMER_TOPIC);
    }

    public String getConsumerGroup() {
        return configuration.getProperty(sectionMarker, PROPERTY_KEY_CONSUMER_GROUP);
    }

    public String getConsumerId() {
        return configuration.getProperty(sectionMarker, PROPERTY_KEY_CONSUMER_ID);
    }

    public String getTimeout() {
        return configuration.getProperty(sectionMarker, PROPERTY_KEY_CONSUMER_TIMEOUT);
    }

    public String getLimit() {
        return configuration.getProperty(sectionMarker, PROPERTY_KEY_CONSUMER_LIMIT);
    }

    public String getFetchPause() {
        return configuration.getProperty(sectionMarker, PROPERTY_KEY_CONSUMER_FETCHPAUSE);
    }

}
