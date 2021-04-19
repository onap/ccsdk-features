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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.config.PmConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NetconfNetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.PerformanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceManagerImpl implements PerformanceManager, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(PerformanceManagerImpl.class);

    private PerformanceManagerTask task;

    public PerformanceManagerImpl(long seconds, NetconfNetworkElementService netconfNetworkElementService,
            DataProvider microwaveHistoricalPerformanceWriterService, ConfigurationFileRepresentation config) {

        LOG.info("Construct {}", PerformanceManagerImpl.class.getSimpleName());

        this.task = null;
        PmConfig configurationPM = new PmConfig(config);
        LOG.info("Performance manager configuration: {}", configurationPM);

        if (!configurationPM.isPerformanceManagerEnabled()) {
            LOG.info("Don't start performance manager");

        } else {
            LOG.info("{} Seconds", seconds);
            LOG.info("Start of PM task");
            task = new PerformanceManagerTask(seconds, microwaveHistoricalPerformanceWriterService,
                    netconfNetworkElementService);
            task.start();
            LOG.info("PM task scheduled");
        }

        LOG.info("Construct end {}", PerformanceManagerImpl.class.getSimpleName());
    }

    @Override
    public void close() {
        LOG.info("Close {}", PerformanceManagerImpl.class.getSimpleName());
        if (task != null) {
            task.stop();
        }
    }

    @Override
    public void registration(String mountPointNodeName, NetworkElement ne) {
        LOG.debug("Register {}", mountPointNodeName);
        if (task != null) {
            task.registration(mountPointNodeName, ne);
        }
    }

    @Override
    public void deRegistration(String mountPointNodeName) {
        LOG.debug("Deregister {}", mountPointNodeName);
        if (task != null) {
            task.deRegistration(mountPointNodeName);
        }
    }

}
