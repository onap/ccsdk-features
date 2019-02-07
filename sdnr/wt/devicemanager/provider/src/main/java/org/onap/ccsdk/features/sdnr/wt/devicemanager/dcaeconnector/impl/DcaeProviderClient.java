/*******************************************************************************
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl;

import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.HtDevicemanagerConfiguration;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.impl.DcaeConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.DeviceManagerImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.ProviderClient;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ProblemNotificationXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DcaeProviderClient implements AutoCloseable, ProviderClient {

    private static final Logger LOG = LoggerFactory.getLogger(DcaeProviderClient.class);

    private final HtDevicemanagerConfiguration htConfig;
    private final IConfigChangedListener configChangedListener;

    private final Object lock = new Object();;
    private DcaeProviderWorker worker;

    public DcaeProviderClient(HtDevicemanagerConfiguration cfg, String entityName, DeviceManagerImpl deviceManager) {
        LOG.info("Create");
        this.htConfig=cfg;
        worker = new DcaeProviderWorker(this.htConfig.getDcae(), entityName, deviceManager);
        this.configChangedListener = () -> {
            LOG.info("Configuration change. Worker exchanged");
            synchronized(lock) {
                worker.close();
                worker = new DcaeProviderWorker(DcaeConfig.reload(), entityName, deviceManager);
            }
        };
        this.htConfig.registerConfigChangedListener(configChangedListener );

    }

    @Override
    public void sendProblemNotification(String mountPointName, ProblemNotificationXml notification) {
        synchronized(lock) {
            worker.sendProblemNotification(mountPointName, notification);
        }
    }

    @Override
    public void sendProblemNotification(String mountPointName, ProblemNotificationXml notification, boolean neDeviceAlarm) {
        sendProblemNotification(mountPointName, notification);
    }

    @Override
    public void close() {
        this.htConfig.unregisterConfigChangedListener(configChangedListener);
        synchronized(lock) {
            worker.close();
        }
    }

    /* ---------------------------------------------------------
     * Private
     */

}



