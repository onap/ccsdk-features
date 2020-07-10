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
package org.onap.ccsdk.features.sdnr.wt.common.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.time.Instant;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.common.database.Portstatus;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;

public class TestPortstatus extends Mockito {


    @Test
    public void testIsAvailable() throws IOException {
        assertFalse("Port status should be false", Portstatus.isAvailable("localhost", 4567));
        ServerSocket serverSocket = new ServerSocket(4567);
        assertTrue("Port status should be true", Portstatus.isAvailable("localhost", 4567));
        serverSocket.close();
    }

    @Test
    public void testWaitIsAvailable() throws IOException {
        ServerSocket serverSocket = new ServerSocket(4567);
        assertTrue("Port status should be true", Portstatus.waitSecondsTillAvailable(5, "localhost", 4567));
        serverSocket.close();
    }

    @Test
    public void testWaitIsAvailableHostInfo() throws IOException {
        ServerSocket serverSocket = new ServerSocket(4567);
        assertTrue("Port status should be true",
                Portstatus.waitSecondsTillAvailable(5, new HostInfo("localhost", 4567)));
        serverSocket.close();
    }

    @Test
    public void testWaitTillAvailable() throws IOException {
        Instant start = Instant.now();
        assertFalse("Port status should be false", Portstatus.waitSecondsTillAvailable(5, "localhost", 4567));
        Instant end = Instant.now();
        long seconds = Duration.between(start, end).getSeconds();
        assertTrue("Port status timeout 5 expected and not between 3 .. 7 seconds", 3 < seconds && seconds < 7);
    }

    @Test
    public void testWaitTillAvailableHostinfo() throws IOException {
        Instant start = Instant.now();
        assertFalse("Port status should be false",
                Portstatus.waitSecondsTillAvailable(5, new HostInfo("localhost", 4567)));
        Instant end = Instant.now();
        long seconds = Duration.between(start, end).getSeconds();
        assertTrue("Port status timeout 5 expected and not between 3 .. 7 seconds", 3 < seconds && seconds < 7);
    }


}
