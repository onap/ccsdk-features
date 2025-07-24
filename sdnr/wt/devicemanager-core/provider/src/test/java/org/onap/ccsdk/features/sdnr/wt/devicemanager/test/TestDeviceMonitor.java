/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
 *  ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.test;

import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.DeviceMonitorImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler.ODLEventListenerHandler;
import org.opendaylight.mdsal.binding.api.DataBroker;

public class TestDeviceMonitor extends Mockito {

    private static final String CONFIGURATIONTESTFILE = "test.properties"; // for
    private static final String mountPointNodeName = "TestMountpoint";

    private static DeviceMonitorImpl deviceMonitor;

    @BeforeClass
    public static void before() {

        DataBroker dataBroker = mock(DataBroker.class);
        ODLEventListenerHandler odlEventListenerHandler = mock(ODLEventListenerHandler.class);
        ConfigurationFileRepresentation config = new ConfigurationFileRepresentation(CONFIGURATIONTESTFILE);

        deviceMonitor = new DeviceMonitorImpl(dataBroker, odlEventListenerHandler, config);

    }

    @Test
    public void testDeviceMonitor() {

        deviceMonitor.deviceConnectSlaveIndication(mountPointNodeName);
        deviceMonitor.refreshAlarmsInDb();
        deviceMonitor.taskTestRun();
        deviceMonitor.deviceDisconnectIndication(mountPointNodeName);
        deviceMonitor.removeMountpointIndication(mountPointNodeName);
    }

    @Test
    public void testDeviceMonitorTask() {


    }

    @AfterClass
    public static void after() throws Exception {
        deviceMonitor.close();
        File f = new File(CONFIGURATIONTESTFILE);
        if (f.exists())
            f.delete();
    }

}
