/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.adaptermanager.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.adaptermanager.impl.DeviceManagerAdapterManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDeviceManagerAdapterManagerImpl {
    private static Path KARAF_ETC = Paths.get("etc");
    private static final Logger LOG = LoggerFactory.getLogger(TestDeviceManagerAdapterManagerImpl.class);
    DeviceManagerAdapterManagerImpl devMgrAdapterManager;

    @Before
    public void init() throws InterruptedException, IOException {
        /*System.out.println("Logger: " + LOG.getClass().getName() + " " + LOG.getName());
        Path etc = KARAF_ETC;
        delete(etc);

        System.out.println("Create empty:" + etc.toString());
        Files.createDirectories(etc);*/
    }

    @Test
    public void test() throws Exception {
        devMgrAdapterManager = new DeviceManagerAdapterManagerImpl();
        /*DeviceManagerImpl devMgr = new DeviceManagerImpl();

        try {
        	devMgr.init();
        	devMgrOran.setNetconfNetworkElementService(devMgr);
        	devMgrOran.init();
        } catch (Exception e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }*/
        /*
         * devMgrOran.setNetconfNetworkElementService(null); devMgrOran.init();
         * NetconfNetworkElementService netConfNetworkElementService =
         * mock(NetconfNetworkElementService.class); devMgrOran =
         * mock(DeviceManagerORanImpl.class);
         * when(netConfNetworkElementService.registerNetworkElementFactory(new
         * ORanNetworkElementFactory())).thenReturn(null);
         */


    }

    @After
    public void cleanUp() throws Exception {
        devMgrAdapterManager.close();
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
