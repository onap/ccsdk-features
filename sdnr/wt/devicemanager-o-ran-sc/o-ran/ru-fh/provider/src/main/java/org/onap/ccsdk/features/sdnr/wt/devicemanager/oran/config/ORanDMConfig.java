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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.config;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;

public class ORanDMConfig implements Configuration {

    private static final String SECTION_MARKER_ORAN_SUPERVISION = "ORAN-SUPERVISION";
    private static final String PROPERTY_KEY_SUPERVISION_NOTIFICATION_INTERVAL = "supervision-notification-interval";
    private static final String PROPERTY_KEY_GUARD_TIMER_OVERHEAD = "guard-timer-overhead";

    private static final String DEFAULT_SUPERVISION_NOTIFICATION_INTERVAL = "${O_RU_SUPERVISION_NOTIFICATION_INTERVAL}";
    private static final String DEFAULT_GUARD_TIMER_OVERHEAD = "${O_RU_GUARD_TIMER_OVERHEAD}";

    private static final int DEFAULT_SUPERVISION_NOTIFICATION_INTERVAL_VAL = 60;
    private static final int DEFAULT_GUARD_TIMER_OVERHEAD_VAL = 10;

    private ConfigurationFileRepresentation configuration;

    public ORanDMConfig(ConfigurationFileRepresentation configuration) {
        this.configuration = configuration;
        this.configuration.addSection(SECTION_MARKER_ORAN_SUPERVISION);
        defaults();
    }

    @Override
    public String getSectionName() {
        return SECTION_MARKER_ORAN_SUPERVISION;
    }

    @Override
    public void defaults() {
        configuration.setPropertyIfNotAvailable(this.getSectionName(), PROPERTY_KEY_SUPERVISION_NOTIFICATION_INTERVAL,
                DEFAULT_SUPERVISION_NOTIFICATION_INTERVAL);
        configuration.setPropertyIfNotAvailable(this.getSectionName(), PROPERTY_KEY_GUARD_TIMER_OVERHEAD,
                DEFAULT_GUARD_TIMER_OVERHEAD);
    }

    public int getNotificationInterval() {
        String v = this.configuration.getProperty(SECTION_MARKER_ORAN_SUPERVISION,
                PROPERTY_KEY_SUPERVISION_NOTIFICATION_INTERVAL);
        return (v == null || v.equals("null") || v.isEmpty() || !isNumeric(v))
                ? DEFAULT_SUPERVISION_NOTIFICATION_INTERVAL_VAL
                : Integer.parseInt(v);
    }

    public int getWatchdogTimer() {
        String v = this.configuration.getProperty(SECTION_MARKER_ORAN_SUPERVISION, PROPERTY_KEY_GUARD_TIMER_OVERHEAD);
        return (v == null || v.equals("null") || v.isEmpty() || !isNumeric(v)) ? DEFAULT_GUARD_TIMER_OVERHEAD_VAL
                : Integer.parseInt(v);
    }

    private boolean isNumeric(String v) {
        try {
            int i = Integer.parseInt(v);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
