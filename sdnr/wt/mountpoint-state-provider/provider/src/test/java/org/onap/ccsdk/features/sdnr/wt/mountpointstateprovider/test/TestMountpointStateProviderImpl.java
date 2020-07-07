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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.MountpointStateProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMountpointStateProviderImpl {

    private static Path KARAF_ETC = Paths.get("etc");
    private static MountpointStateProviderImpl mountpointStateProvider;

    private static final Logger LOG = LoggerFactory.getLogger(TestMountpointStateProviderImpl.class);



    @BeforeClass
    public static void before() throws InterruptedException, IOException {

        System.out.println("Logger: " + LOG.getClass().getName() + " " + LOG.getName());
        // Call System property to get the classpath value
        Path etc = KARAF_ETC;
        delete(etc);

        System.out.println("Create empty:" + etc.toString());
        Files.createDirectories(etc);

        // Create mocks

        // start using blueprint interface
        try {
            mountpointStateProvider = new MountpointStateProviderImpl();

            //mountpointStateProvider.init(); // Can't be tested as this invokes a thread. Mockito doesn't help either
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Not initialized" + sw.toString());
        }
        System.out.println("Initialization status: " + mountpointStateProvider.isInitializationOk());
        System.out.println("Initialization done");
    }

    @AfterClass
    public static void after() throws InterruptedException, IOException {

        System.out.println("Start shutdown");
        // close using blueprint interface
        try {
            mountpointStateProvider.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        delete(KARAF_ETC);

    }

    @Test
    public void test1() {
        System.out.println("Test1: slave mountpoint");
        System.out.println("Initialization status: " + mountpointStateProvider.isInitializationOk());
        System.out.println("Test2: Done");
    }

    // ********************* Private

    private static void delete(Path etc) throws IOException {
        if (Files.exists(etc)) {
            System.out.println("Found and remove:" + etc.toString());
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
    /*	@Test
    	public void testInit() {
    
    	}
    
    	@Test
    	public void testOnConfigChanged() {
    		//fail("Not yet implemented");
    	}*/

}
