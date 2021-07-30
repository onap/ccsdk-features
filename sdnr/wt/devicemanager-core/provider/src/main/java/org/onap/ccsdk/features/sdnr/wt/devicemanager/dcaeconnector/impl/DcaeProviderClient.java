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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.config.DcaeConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.DeviceManagerImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.ProviderClient;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ProblemNotificationXml;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DcaeProviderClient implements AutoCloseable, ProviderClient {

    private static final Logger LOG = LoggerFactory.getLogger(DcaeProviderClient.class);

    private final ConfigurationFileRepresentation htConfig;
    private final IConfigChangedListener configChangedListener;

    private final Object lock = new Object();
    private DcaeProviderWorker worker;
    private DcaeConfig config;

    public DcaeProviderClient(ConfigurationFileRepresentation cfg, String entityName, DeviceManagerImpl deviceManager) {
        LOG.info("Create");
        this.htConfig = cfg;
        this.config = new DcaeConfig(cfg);
        worker = new DcaeProviderWorker(config, entityName, deviceManager);
        this.configChangedListener = () -> {
            LOG.info("Configuration change. Worker exchanged");
            synchronized (lock) {
                worker.close();
                worker = new DcaeProviderWorker(this.config, entityName, deviceManager);
            }
        };
        this.htConfig.registerConfigChangedListener(configChangedListener);

    }

    @Override
    public void sendProblemNotification(NodeId nodeId, ProblemNotificationXml notification) {
        synchronized (lock) {
            worker.sendProblemNotification(nodeId, notification);
        }
    }

    @Override
    public void sendProblemNotification(NodeId nodeId, ProblemNotificationXml notification,
            boolean neDeviceAlarm) {
        sendProblemNotification(nodeId, notification);
    }

    @Override
    public void close() {
        this.htConfig.unregisterConfigChangedListener(configChangedListener);
        synchronized (lock) {
            worker.close();
        }
    }

    /* ---------------------------------------------------------
     * Private
     */

}


