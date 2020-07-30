/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.test;

import static org.mockito.Mockito.mock;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl.DeviceManagerOpenroadmImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NetconfNetworkElementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDeviceManagerOpenRoadmImpl {
    private static Path KARAF_ETC = Paths.get("etc");
    private static final Logger log = LoggerFactory.getLogger(TestDeviceManagerOpenRoadmImpl.class);
    DeviceManagerOpenroadmImpl devMgrOpenRdmImpl;
    NetconfNetworkElementService netcnfNtwrkElmntSrvc;

    @Before
    public void init() {
        netcnfNtwrkElmntSrvc = mock(NetconfNetworkElementService.class);
        devMgrOpenRdmImpl = new DeviceManagerOpenroadmImpl();
    }

    @Test
    public void test() throws Exception {

        devMgrOpenRdmImpl.setNetconfNetworkElementService(netcnfNtwrkElmntSrvc);

    }

    @After
    public void cleanUp() throws Exception {
        devMgrOpenRdmImpl.close();
    }

    private static void delete(Path etc) throws IOException {
        if (Files.exists(etc)) {
            System.out.println("Found, removing:" + etc.toString());
            delete(etc.toFile());
        }
    }

    private static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }

}
