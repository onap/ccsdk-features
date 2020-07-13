/*******************************************************************************
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) Apache Camel project
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

package org.onap.ccsdk.features.sdnr.wt.common.database;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;

public class Portstatus {

    public static final int MIN_PORT_NUMBER = 0;
    public static final int MAX_PORT_NUMBER = 65535;

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     */
    public static boolean isAvailable(String dnsName, int port) {
        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        SocketChannel channel = null;
        SocketAddress socketAddress = new InetSocketAddress(dnsName, port);
        try {
            channel = SocketChannel.open(socketAddress);
            return true;
        } catch (IOException e) {
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

    public static boolean isAvailable(HostInfo... hosts) {
        for (HostInfo host : hosts) {
            if (!isAvailable(host.hostname, host.port)) {
                return false;
            }
        }
        return true;
    }

    public static boolean waitSecondsTillAvailable(long timeoutSeconds, HostInfo... hosts) {

        if (timeoutSeconds < 0) {
            throw new IllegalArgumentException("Invalid timeout: " + timeoutSeconds);
        }
        long waitSeconds = 0;
        boolean res = false;
        while ((timeoutSeconds == 0 || ++waitSeconds < timeoutSeconds) && !(res = isAvailable(hosts))) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return res;
    }

    public static boolean waitSecondsTillAvailable(long timeoutSeconds, String dnsName, int port) {
        return waitSecondsTillAvailable(timeoutSeconds, new HostInfo(dnsName, port));
    }

}
