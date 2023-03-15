/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2023 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.websocketmanager.config;

import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;

public class WebSocketManagerConfig implements Configuration {

    private static final String SECTION_MARKER = "websocket";
    private static final String PROPERTY_KEY_WEBSOCKET_PORT = "port";
    private static final String PROPERTY_VALUE_WEBSOCKET_PORT = "${SDNR_WEBSOCKET_PORT}";

    private ConfigurationFileRepresentation configuration;

    public WebSocketManagerConfig(ConfigurationFileRepresentation configuration) {
        this.configuration = configuration;
        configuration.addSection(SECTION_MARKER);
        defaults();
    }

    public Optional<Long> getWebsocketPort() {
        return configuration.getPropertyLong(SECTION_MARKER, PROPERTY_KEY_WEBSOCKET_PORT);
    }

    @Override
    public String getSectionName() {
        return SECTION_MARKER;
    }

    @Override
    public void defaults() {
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_WEBSOCKET_PORT, PROPERTY_VALUE_WEBSOCKET_PORT);
    }

}
