/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt mountpoint-registrar
 * =================================================================================================
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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.cm;

import static org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.MessageClient.SendMethod.POST;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.MessageClient;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.MessageClient.MessageType;

public class CMNotificationClient extends MessageClient {

    private static final String CM_NOTIFICATION_URI = "rests/operations/devicemanager:push-cm-notification";
    public static final String NODE_ID = "@node-id@", COUNTER = "@counter@", TIMESTAMP = "@timestamp@",
        OBJECT_ID = "@object-id@", NOTIFICATION_TYPE = "@notification-type@", SOURCE_INDICATOR = "@source-indicator@",
        NOTIFICATION_ID = "@notification-id@", PATH = "@path@", OPERATION = "@operation@", VALUE = "@value@";
    public static final List<String> REQUIRED_FIELDS =
        List.of(NODE_ID, COUNTER, TIMESTAMP, OBJECT_ID, NOTIFICATION_TYPE, NOTIFICATION_ID, SOURCE_INDICATOR, PATH,
            OPERATION, VALUE);

    private static final String CM_PAYLOAD = "{\n"
        + "  \"input\": {\n"
        + "    \"node-id\": \"" + NODE_ID + "\",\n"
        + "    \"counter\": \"" + COUNTER + "\",\n"
        + "    \"timestamp\": \"" + TIMESTAMP + "\",\n"
        + "    \"object-id\": \"" + OBJECT_ID + "\",\n"
        + "    \"notification-type\": \"" + NOTIFICATION_TYPE + "\",\n"
        + "    \"notification-id\": \"" + NOTIFICATION_ID + "\",\n"
        + "    \"source-indicator\": \"" + SOURCE_INDICATOR + "\",\n"
        + "    \"path\": \"" + PATH + "\",\n"
        + "    \"operation\": \"" + OPERATION + "\",\n"
        + "    \"value\": \"" + VALUE + "\"\n"
        + "  }\n"
        + "}";

    public CMNotificationClient(String baseUrl) {
        super(baseUrl, CM_NOTIFICATION_URI);
    }

    @Override
    public String prepareMessageFromPayloadMap(Map<String, String> notificationPayloadMap) {
        return super.prepareMessageFromPayloadMap(notificationPayloadMap, CM_PAYLOAD, REQUIRED_FIELDS);
    }

    @Override
    public boolean sendNotification(String message) {
        return super.sendNotification(message, POST, MessageType.json);
    }


    public static Map<String, String> createCMNotificationPayloadMap(CMNotification cmNotification) {
        HashMap<String, String> map = new HashMap<>();
        map.put(NODE_ID, cmNotification.getBasicHeaderFields().getCmNodeId());
        map.put(COUNTER, cmNotification.getBasicHeaderFields().getCmSequence());
        map.put(TIMESTAMP, cmNotification.getBasicHeaderFields().getCmOccurrenceTime());
        map.put(OBJECT_ID, cmNotification.getBasicHeaderFields().getSourceId());
        map.put(NOTIFICATION_TYPE, cmNotification.getBasicHeaderFields().getNotificationType());
        map.put(NOTIFICATION_ID, cmNotification.getCmNotificationId());
        map.put(SOURCE_INDICATOR, cmNotification.getCmSourceIndicator());
        map.put(PATH, cmNotification.getCmPath());
        map.put(OPERATION, cmNotification.getCmOperation());
        map.put(VALUE, cmNotification.getCmValue());
        return map;
    }
}
