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
import java.io.IOException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NetconfNetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.MountpointStateProviderImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMountpointStateProviderImpl {
    private static final Logger LOG = LoggerFactory.getLogger(TestMountpointStateProviderImpl.class);
    private MountpointStateProviderImpl mountpointStateProvider;


    @Test
    public void test() throws InterruptedException, IOException {
        NetconfNodeStateService netconfNodeStateService = mock(NetconfNodeStateService.class);
        DeviceManagerServiceProvider serviceProvider = mock(DeviceManagerServiceProvider.class);
        VESCollectorService vesCollectorService = mock(VESCollectorService.class);
        VESCollectorCfgService vesCollectorCfgService = mock(VESCollectorCfgService.class);
        NetconfNetworkElementService netconfNetworkElementService = mock(NetconfNetworkElementService.class);

        when(netconfNetworkElementService.getServiceProvider()).thenReturn(serviceProvider);
        when(serviceProvider.getVESCollectorService()).thenReturn(vesCollectorService);
        when(vesCollectorService.getConfig()).thenReturn(vesCollectorCfgService);
        when(vesCollectorCfgService.isVESCollectorEnabled()).thenReturn(true);

        mountpointStateProvider = new MountpointStateProviderImpl();
        mountpointStateProvider.setNetconfNetworkElementService(netconfNetworkElementService);
        mountpointStateProvider.setNetconfNodeStateService(netconfNodeStateService);
        mountpointStateProvider.init();
    }

}
