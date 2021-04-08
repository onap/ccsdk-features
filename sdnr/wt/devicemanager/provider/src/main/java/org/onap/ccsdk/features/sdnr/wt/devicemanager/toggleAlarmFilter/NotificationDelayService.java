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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.toggleAlarmFilter;

import java.util.HashMap;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.toggleAlarmFilter.conf.ToggleAlarmConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationDelayService<T extends ToggleAlarmFilterable>
        implements DeviceManagerService, AutoCloseable, IConfigChangedListener {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationDelayService.class);

    private final HashMap<String, NotificationDelayFilter<T>> filters;
    private final ToggleAlarmConfig config;

    public NotificationDelayService(ConfigurationFileRepresentation htconfig) {
        this.filters = new HashMap<>();
        htconfig.registerConfigChangedListener(this);
        config = new ToggleAlarmConfig(htconfig);
        onConfigChanged();
    }

    public NotificationDelayFilter<T> getInstance(String nodeName, NotificationDelayedListener<T> eventListener) {
        NotificationDelayFilter<T> filter = filters.getOrDefault(nodeName, null);

        LOG.trace("nodeName={}, filter!=null? {}", nodeName, filter != null);
        if (filter == null) {
            filter = new NotificationDelayFilter<>(nodeName, eventListener);
            this.filters.put(nodeName, filter);
        }
        return filter;
    }

    @Override
    public void onConfigChanged() {
        if (config != null) {
            NotificationDelayFilter.setDelay(config.getDelay());
            NotificationDelayFilter.setEnabled(config.isEnabled());
        } else {
            LOG.error("Can not process configuration change");
        }
    }

    @Override
    public void close() throws Exception {
        // close all filters
        for (NotificationDelayFilter<T> filter : this.filters.values()) {
            filter.close();
        }
    }

}
