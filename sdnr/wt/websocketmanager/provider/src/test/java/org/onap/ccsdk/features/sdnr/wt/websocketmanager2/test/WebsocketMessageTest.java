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
package org.onap.ccsdk.features.sdnr.wt.websocketmanager2.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.WebSocketManagerSocket;

public class WebsocketMessageTest {

    private static final String MSG1 = "{\"data\":\"scopes\",\"scopes\":[{\"node-id\":\"scope1\"}]}";
    private static final String MSG1_RESPONSE = "{\"status\":\"success\",\"scopes\":[{\"node-id\":\"scope1\"}]}";
    private static final String MSG2 = "{}";
    private static final String MSG3 = "{\n"
            + "  \"event-time\": \"2021-03-12T05:08:55.3Z\",\n"
            + "  \"type\": \"urn:opendaylight:params:xml:ns:yang:devicemanager@2019-01-09:object-creation-notification\",\n"
            + "  \"node-id\": \"SDN-Controller-0\",\n"
            + "  \"data\": {\n"
            + "    \"object-id-ref\": \"sim1\",\n"
            + "    \"counter\": 7,\n"
            + "    \"time-stamp\": \"2021-03-12T05:08:55.2Z\"\n"
            + "  }\n"
            + "}";
    private static final String MSG4 = "{ Not correct messga}";

    @Test
    public void test() {
        MyWebSocketManagerSocket socketToTest = new MyWebSocketManagerSocket();
        Session sess = mock(Session.class);
        InetSocketAddress remoteAdr = new InetSocketAddress("127.0.0.1", 4444);
        when(sess.getRemoteAddress()).thenReturn(remoteAdr);
        socketToTest.onWebSocketConnect(sess);
        // message from client
        socketToTest.setExpected(MSG1_RESPONSE);
        socketToTest.onWebSocketText(MSG1);
        socketToTest.setExpected(MSG2);
        socketToTest.onWebSocketText(MSG2);
        socketToTest.setExpected(MSG3);
        socketToTest.onWebSocketText(MSG3);
        socketToTest.setExpected(MSG4);
        socketToTest.onWebSocketText(MSG4);
        socketToTest.onWebSocketClose(0, "by default");
        sess.close();

    }

    private static class MyWebSocketManagerSocket extends WebSocketManagerSocket {

        private String expected;

        public MyWebSocketManagerSocket() {}

        void setExpected(String expected) {
            this.expected = expected;
        }

        @Override
        public void send(String msg) {
            System.out.println(msg);
            assertTrue("Expected '" + expected + "' answer '" + msg + "'", msg.contains(expected));
        }

    }
}
