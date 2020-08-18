/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Update Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=======================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NetconfNetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.MountpointStatePublisher;

public class TestMountpointStatePublisher {

    MountpointStatePublisher mountpointStatePublisher;
    VESCollectorService vesCollectorService;
    VESCollectorCfgService vesCfg;
    String vesMsg = "{}";
    JSONObject testJsonData;

    @Before
    public void testMountpointStatePublisherData() {
        testJsonData = new JSONObject();
        testJsonData.put("NodeId", "69322972e178_50001");
        testJsonData.put("NetConfNodeState", "Connecting");
        testJsonData.put("TimeStamp", java.time.Clock.systemUTC().instant());


        DeviceManagerServiceProvider serviceProvider = mock(DeviceManagerServiceProvider.class);
        vesCollectorService = mock(VESCollectorService.class);
        vesCfg = mock(VESCollectorCfgService.class);
        NetconfNetworkElementService netconfNetworkElementService = mock(NetconfNetworkElementService.class);
        when(netconfNetworkElementService.getServiceProvider()).thenReturn(serviceProvider);
        when(serviceProvider.getVESCollectorService()).thenReturn(vesCollectorService);
        when(vesCollectorService.getConfig()).thenReturn(vesCfg);
        when(vesCfg.getReportingEntityName()).thenReturn("ONAP SDN-R");
        when(vesCollectorService.publishVESMessage(vesMsg)).thenReturn(true);

        mountpointStatePublisher = new MountpointStatePublisher(vesCollectorService);
        mountpointStatePublisher.addToPublish(testJsonData);
        //mountpointStatePublisher.getStateObjects().add(testJsonData);
    }

    @Test
    public void testMountpointStatePublisherConfiguration() throws InterruptedException {
        Thread t = new Thread(mountpointStatePublisher);
        t.start();
        Thread.sleep(7000);
    }

    @After
    public void close() {
        mountpointStatePublisher.stop();
    }
}
