/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

/*
 * [strimzi-kafka]
 * bootstrapServers=abc:9092,def:9092
 * securityProtocol=PLAINTEXT #OTHER POSSIBLE VALUES - SSL, SASL_PLAINTEXT, SASL_SSL
 * saslMechanism=PLAIN #Need to understand more
 * saslJaasConfig=
 * consumerGroup=
 * consumerID=
 */
public class StrimziKafkaConfig implements Configuration {

    private static final String SECTION_MARKER = "strimzi-kafka";

    private static final String PROPERTY_KEY_ENABLED = "strimziEnabled";

    private static final String PROPERTY_KEY_BOOTSTRAPSERVERS = "bootstrapServers";
    private static final String DEFAULT_VALUE_BOOTSTRAPSERVERS = "onap-strimzi-kafka-0:9094,onap-strimzi-kafka-1:9094";

    private static final String PROPERTY_KEY_SECURITYPROTOCOL = "securityProtocol";
    private static final String DEFAULT_VALUE_SECURITYPROTOCOL = "PLAINTEXT";

    private static final String PROPERTY_KEY_SASLMECHANISM = "saslMechanism";
    private static final String DEFAULT_VALUE_SASLMECHANISM = "PLAIN";

    private static final String PROPERTY_KEY_SASLJAASCONFIG = "saslJaasConfig";
    private static final String DEFAULT_VALUE_SASLJAASCONFIG = "PLAIN"; // TBD

    private ConfigurationFileRepresentation configuration;

    public StrimziKafkaConfig(ConfigurationFileRepresentation configuration) {
        this.configuration = configuration;
        configuration.addSection(SECTION_MARKER);
        defaults();
    }

    public Boolean getEnabled() {
        return configuration.getPropertyBoolean(SECTION_MARKER, PROPERTY_KEY_ENABLED);
    }

    public String getBootstrapServers() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_BOOTSTRAPSERVERS);
    }

    public String getSecurityProtocol() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_SECURITYPROTOCOL);
    }

    public String getSaslMechanism() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_SASLMECHANISM);
    }

    public String getSaslJaasConfig() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_SASLJAASCONFIG);
    }

    @Override
    public String getSectionName() {
        return SECTION_MARKER;
    }

    @Override
    public void defaults() {
        // The default value should be "false" given that SDNR can be run in
        // environments where Strimzi is not used
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_ENABLED, Boolean.FALSE);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_BOOTSTRAPSERVERS,
                DEFAULT_VALUE_BOOTSTRAPSERVERS);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_SECURITYPROTOCOL,
                DEFAULT_VALUE_SECURITYPROTOCOL);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_SASLMECHANISM,
                DEFAULT_VALUE_SASLMECHANISM);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_SASLJAASCONFIG,
                DEFAULT_VALUE_SASLJAASCONFIG);
    }

}
