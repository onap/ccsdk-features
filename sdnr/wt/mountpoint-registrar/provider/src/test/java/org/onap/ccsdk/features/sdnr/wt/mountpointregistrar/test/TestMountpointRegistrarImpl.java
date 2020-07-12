/*
 * ============LICENSE_START======================================================= ONAP : ccsdk
 * feature sdnr wt ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * ================================================================================ Licensed under
 * the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License. ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.MountpointRegistrarImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMountpointRegistrarImpl {

    private static Path KARAF_ETC = Paths.get("etc");
    private static MountpointRegistrarImpl mountpointRegistrar;

    private static final Logger LOG = LoggerFactory.getLogger(TestMountpointRegistrarImpl.class);



    @Before
    public void before() throws InterruptedException, IOException {

        System.out.println("Logger: " + LOG.getClass().getName() + " " + LOG.getName());
        // Call System property to get the classpath value
        Path etc = KARAF_ETC;
        delete(etc);

        System.out.println("Create empty:" + etc.toString());
        Files.createDirectories(etc);

        // Create mocks

        // start using blueprint interface
        try {
            mountpointRegistrar = new MountpointRegistrarImpl();
            mountpointRegistrar.init();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            fail("Not initialized" + sw.toString());
        }
        System.out.println("Initialization status: " + mountpointRegistrar.isInitializationOk());
        System.out.println("Initialization done");
    }

    @After
    public void after() throws InterruptedException, IOException {

        System.out.println("Start shutdown");
        // close using blueprint interface
        try {
            mountpointRegistrar.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        delete(KARAF_ETC);

    }

    @Test
    public void test1() {
        System.out.println("Test1: slave mountpoint");
        assertNotNull(mountpointRegistrar);
        System.out.println("Initialization status: " + mountpointRegistrar.isInitializationOk());
        System.out.println("Test2: Done");
    }

    // ********************* Private

    @SuppressWarnings("unused")
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOG.warn(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

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
}
