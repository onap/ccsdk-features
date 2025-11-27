/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.pnfreg;

import static org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.MessageClient.MessageType.xml;
import static org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.MessageClient.SendMethod.PUT;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.MessageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PNFMountPointClient extends MessageClient {

    private static final Logger LOG = LoggerFactory.getLogger(PNFMountPointClient.class);

    private static final String MOUNTPOINT_URI =
            "rests/data/network-topology:network-topology/topology=topology-netconf/node=";
    public static final String DEVICE_NAME = "@device-name@", DEVICE_IP = "@device-ip@", DEVICE_PORT = "@device-port@",
            USERNAME = "@username@", PASSWORD = "@password@", KEY_ID = "@key-id@";
    private static final String PROTOCOL = "protocol_sec";
    public static List<String> REQUIRED_FIELDS_SSH =
            List.of(PROTOCOL, DEVICE_NAME, DEVICE_IP, DEVICE_PORT, USERNAME, PASSWORD);
    public static List<String> REQUIRED_FIELDS_TLS =
            List.of(PROTOCOL, DEVICE_NAME, DEVICE_IP, DEVICE_PORT, USERNAME, KEY_ID);

    private static final String SSH_PAYLOAD = " <node xmlns=\"urn:TBD:params:xml:ns:yang:network-topology\">\n"
            + "    <node-id>" + DEVICE_NAME + "</node-id>\n"
            + "    <netconf-node xmlns=\"urn:opendaylight:netconf-node-topology\">\n"
            + "        <host xmlns=\"urn:opendaylight:netconf-node-topology\">" + DEVICE_IP + "</host>\n"
            + "        <port xmlns=\"urn:opendaylight:netconf-node-topology\">" + DEVICE_PORT + "</port>\n"
            + "        <login-password-unencrypted xmlns=\"urn:opendaylight:netconf-node-topology\">\n"
            + "            <username xmlns=\"urn:opendaylight:netconf-node-topology\">" + USERNAME + "</username>\n"
            + "            <password xmlns=\"urn:opendaylight:netconf-node-topology\">" + PASSWORD + "</password>\n"
            + "         </login-password-unencrypted>\n"
            + "         <tcp-only xmlns=\"urn:opendaylight:netconf-node-topology\">false</tcp-only>\n"
            + "         <!-- non-mandatory fields with default values, you can safely remove these if you do not wish to override any of these values-->\n"
            + "         <reconnect-on-changed-schema xmlns=\"urn:opendaylight:netconf-node-topology\">false</reconnect-on-changed-schema>\n"
            + "         <connection-timeout-millis xmlns=\"urn:opendaylight:netconf-node-topology\">20000</connection-timeout-millis>\n"
            + "         <max-connection-attempts xmlns=\"urn:opendaylight:netconf-node-topology\">0</max-connection-attempts>\n"
            + "         <min-backoff-millis xmlns=\"urn:opendaylight:netconf-node-topology\">2000</min-backoff-millis>\n"
            + "         <max-backoff-millis xmlns=\"urn:opendaylight:netconf-node-topology\">1800000</max-backoff-millis>\n"
            + "         <backoff-multiplier xmlns=\"urn:opendaylight:netconf-node-topology\">1.5</backoff-multiplier>\n"
            + "         <!-- keepalive-delay set to 0 turns off keepalives-->\n"
            + "         <keepalive-delay xmlns=\"urn:opendaylight:netconf-node-topology\">120</keepalive-delay>\n"
            + "    </netconf-node>\n"
            + "    </node>";

    private static final String TLS_PAYLOAD = "<node xmlns=\"urn:TBD:params:xml:ns:yang:network-topology\">\n"
            + "    <node-id>" + DEVICE_NAME + "</node-id>\n"
            + "    <netconf-node xmlns=\"urn:opendaylight:netconf-node-topology\">\n"
            + "        <host xmlns=\"urn:opendaylight:netconf-node-topology\">" + DEVICE_IP + "</host>\n"
            + "        <port xmlns=\"urn:opendaylight:netconf-node-topology\">" + DEVICE_PORT + "</port>\n"
            + "        <key-based xmlns=\"urn:opendaylight:netconf-node-topology\">\n"
            + "            <username xmlns=\"urn:opendaylight:netconf-node-topology\">" + USERNAME + "</username>\n"
            + "            <key-id xmlns=\"urn:opendaylight:netconf-node-topology\">" + KEY_ID + "</key-id>\n"
            + "        </key-based>\n" + "        <protocol>\n" + "            <name>TLS</name>\n"
            + "        </protocol>\n"
            + "        <tcp-only xmlns=\"urn:opendaylight:netconf-node-topology\">false</tcp-only>\n"
            + "        <!-- non-mandatory fields with default values, you can safely remove these if you do not wish to override any of these values-->\n"
            + "        <reconnect-on-changed-schema xmlns=\"urn:opendaylight:netconf-node-topology\">false</reconnect-on-changed-schema>\n"
            + "        <connection-timeout-millis xmlns=\"urn:opendaylight:netconf-node-topology\">20000</connection-timeout-millis>\n"
            + "        <max-connection-attempts xmlns=\"urn:opendaylight:netconf-node-topology\">0</max-connection-attempts>\n"
            + "        <min-backoff-millis xmlns=\"urn:opendaylight:netconf-node-topology\">2000</min-backoff-millis>\n"
            + "        <max-backoff-millis xmlns=\"urn:opendaylight:netconf-node-topology\">1800000</max-backoff-millis>\n"
            + "        <backoff-multiplier xmlns=\"urn:opendaylight:netconf-node-topology\">1.5</backoff-multiplier>\n"
            + "        <!-- keepalive-delay set to 0 turns off keepalives-->\n"
            + "        <keepalive-delay xmlns=\"urn:opendaylight:netconf-node-topology\">120</keepalive-delay>\n"
            + "    </netconf-node>\n" + "</node>";

    public PNFMountPointClient(String baseUrl) {
        super(baseUrl, MOUNTPOINT_URI);
    }

    @Override
    public String prepareMessageFromPayloadMap(Map<String, String> notificationPayloadMap) {
        updateNotificationUriWithPnfName(notificationPayloadMap.get(DEVICE_NAME));
        String message = "";
        if (!notificationPayloadMap.containsKey(PROTOCOL)) {
            return message;
        }
        if (notificationPayloadMap.get(PROTOCOL).equals("SSH")) {
            message = super.prepareMessageFromPayloadMap(notificationPayloadMap, SSH_PAYLOAD, REQUIRED_FIELDS_SSH);
        } else if (notificationPayloadMap.get(PROTOCOL).equals("TLS")) {
            message = super.prepareMessageFromPayloadMap(notificationPayloadMap, TLS_PAYLOAD, REQUIRED_FIELDS_TLS);
        }
        return message;
    }

    private void updateNotificationUriWithPnfName(String pnfName) {
        setNotificationUri(MOUNTPOINT_URI + urlEncodeValue(pnfName));
    }

    public static String urlEncodeValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20");
        } catch (UnsupportedEncodingException ex) {
            LOG.warn("encoding problem: {}", ex.getMessage());
        }
        return value;
    }

    @Override
    public boolean sendNotification(String message) {
        return super.sendNotification(message, PUT, xml);
    }

    public static Map<String, String> createPNFNotificationPayloadMap(@NonNull String pnfName,
            @NonNull String ipAddress,
            @NonNull String commPort, @NonNull String protocol,
            String username, String password, String keyId) {
        HashMap<String, String> map = new HashMap<>();
        map.put(DEVICE_NAME, pnfName);
        map.put(DEVICE_IP, ipAddress);
        map.put(DEVICE_PORT, commPort);
        map.put(PROTOCOL, protocol);
        map.put(USERNAME, username);
        map.put(PASSWORD, password);
        map.put(KEY_ID, keyId);
        return map;
    }

}
