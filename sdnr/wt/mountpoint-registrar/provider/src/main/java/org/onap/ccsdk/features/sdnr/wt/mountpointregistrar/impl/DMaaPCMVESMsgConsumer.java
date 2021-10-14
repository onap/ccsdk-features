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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMaaPCMVESMsgConsumer extends DMaaPVESMsgConsumerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DMaaPCMVESMsgConsumer.class);

    public DMaaPCMVESMsgConsumer(GeneralConfig generalConfig) {
        super(generalConfig);
        LOG.info("DMaaPCMVESMsgConsumer started successfully");
    }

    @Override
    public void processMsg(String msg) throws InvalidMessageException, JsonProcessingException {
        LOG.debug("Processing CM message {}", msg);
        JsonNode rootNode = convertMessageToJsonNode(msg);
        JsonNode dataNode;
        JsonNode notificationNode;
        try {
            dataNode = rootNode.get("event").get("stndDefinedFields").get("data").requireNonNull();
            if(dataNode.get("notificationType").textValue().equalsIgnoreCase("notifyMOIChanges")) {
                notificationNode = dataNode.get("moiChanges");
                LOG.info("Read CM message from DMaaP topic that is moiChanges type with id {}", dataNode.get("notificationId"));
            }
        } catch (NullPointerException e) {
            LOG.warn("Message is invalid, sending aborted, processing stopped because one of fields is missing");
            throw new InvalidMessageException("Missing field");
        }
        // take required data from notificationNode
    }

//    TODO add processing of moiChanges base on current processMsg implementation
//    @Override
//    public void processMsg(String msg) throws InvalidMessageException, JsonProcessingException {
//        String cmNodeId;
//        String cmSequence;
//        String cmOccurrenceTime;
//        String sourceId;
//        String notificationType;
//
//        JsonNode rootNode = convertMessageToJsonNode(msg);
//        try {
//            cmNodeId = rootNode.at("/event/commonEventHeader/reportingEntityName").textValue();
//            cmSequence = rootNode.at("/event/commonEventHeader/sequence").toString();
//            cmOccurrenceTime = Instant
//                                       .ofEpochMilli(
//                                               rootNode.at("/event/commonEventHeader/startEpochMicrosec").longValue() / 1000)
//                                       .atZone(ZoneId.of("Z")).toString();
//            sourceId = rootNode.at("/event/commonEventHeader/sourceId").textValue();
//
//            String baseUrl = getBaseUrl();
//            String sdnrUser = getSDNRUser();
//            String sdnrPasswd = getSDNRPasswd();
//
//            CMNotificationClient cmClient = new CMNotificationClient(baseUrl);
//            LOG.debug("Setting RESTConf Authorization values - {} : {}", sdnrUser, sdnrPasswd);
//            cmClient.setAuthorization(sdnrUser, sdnrPasswd);
//
//            notificationType = rootNode.at("/event/stndDefinedFields/data/notificationType").textValue();
//
//            if (notificationType.equalsIgnoreCase("notifyMOIChanges")) {
//                notificationType = CmNotificationType.NotifyMOIChanges.toString();
//                processMoiChanges(cmNodeId, cmSequence, cmOccurrenceTime, sourceId, notificationType, rootNode,
//                        cmClient);
//            } else if (notificationType.equalsIgnoreCase("notifyMOICreation")) {
//                notificationType = CmNotificationType.NotifyMOICreation.toString();
//                processMoiCreationDeletion(cmNodeId, cmSequence, cmOccurrenceTime, sourceId, notificationType, rootNode,
//                        cmClient);
//            } else if (notificationType.equalsIgnoreCase("notifyMOIDeletion")) {
//                notificationType = CmNotificationType.NotifyMOIDeletion.toString();
//                processMoiCreationDeletion(cmNodeId, cmSequence, cmOccurrenceTime, sourceId, notificationType, rootNode,
//                        cmClient);
//            } else if (notificationType.equalsIgnoreCase("notifyMOIAttributeValueChanges")) {
//                notificationType = CmNotificationType.NotifyMOIAttributeValueChanges.toString();
//                processMoiAttributeValueChanges(cmNodeId, cmSequence, cmOccurrenceTime, sourceId, notificationType,
//                        rootNode, cmClient);
//            } else {
//                LOG.warn("Message is invalid, sending aborted, wrong CM notification type {}", notificationType);
//                throw new InvalidMessageException();
//            }
//        } catch (NullPointerException e) {
//            LOG.warn("Message is invalid, sending aborted, processing stopped because of {}", e.getMessage());
//            throw new InvalidMessageException(e.getMessage());
//        }
//    }
//
//    private void processMoiChanges(String cmNodeId, String cmSequence, String cmOccurrenceTime,
//            String sourceId, String notificationType, JsonNode rootNode,
//            CMNotificationClient cmClient) {
//        String cmNotificationId;
//        String cmSourceIndicator;
//        String cmPath;
//        String cmOperation;
//        String cmValue;
//        JsonNode moiChanges = rootNode.at("/event/stndDefinedFields/data/moiChanges");
//        Iterator<JsonNode> nodes = moiChanges.elements();
//
//        while (nodes.hasNext()) {
//            JsonNode slaidNode = nodes.next();
//            cmNotificationId = slaidNode.get("notificationId").toString();
//            cmSourceIndicator = slaidNode.get("sourceIndicator").textValue();
//            cmPath = slaidNode.get("path").textValue();
//            cmOperation = slaidNode.get("operation").textValue();
//            cmValue = slaidNode.get("value").toString()
//                              .replace("\"", "");
//
//            Map<String, String> payloadMapMessage = CMNotificationClient.createCMNotificationPayloadMap(
//                    cmNodeId, cmSequence, cmOccurrenceTime, sourceId, notificationType, cmNotificationId,
//                    cmSourceIndicator, cmPath, cmOperation, cmValue);
//            String message = cmClient.prepareMessageFromPayloadMap(payloadMapMessage);
//            cmClient.sendNotification(message);
//        }
//    }
//
//    private void processMoiAttributeValueChanges(String cmNodeId, String cmSequence, String cmOccurrenceTime,
//            String sourceId, String notificationType, JsonNode rootNode,
//            CMNotificationClient cmClient) {
//        String sourceIndicator =
//                rootNode.at("/event/stndDefinedFields/data/sourceIndicator").textValue();
//        String value = rootNode.at("/event/stndDefinedFields/data/attributeListValueChanges").toString()
//                               .replace("\"", "");
//        Map<String, String> payloadMapMessage = CMNotificationClient.createCMNotificationPayloadMap(
//                cmNodeId, cmSequence, cmOccurrenceTime, sourceId, notificationType, null,
//                sourceIndicator, null, CmOperation.NULL.getName(), value);
//        String message = cmClient.prepareMessageFromPayloadMap(payloadMapMessage);
//        cmClient.sendNotification(message);
//    }
//
//    private void processMoiCreationDeletion(String cmNodeId, String cmSequence, String cmOccurrenceTime,
//            String sourceId, String notificationType, JsonNode rootNode,
//            CMNotificationClient cmClient) {
//        String sourceIndicator =
//                rootNode.at("/event/stndDefinedFields/data/sourceIndicator").textValue();
//        String value = rootNode.at("/event/stndDefinedFields/data/attributeList").toString()
//                               .replace("\"", "");
//        Map<String, String> payloadMapMessage = CMNotificationClient.createCMNotificationPayloadMap(
//                cmNodeId, cmSequence, cmOccurrenceTime, sourceId, notificationType, null,
//                sourceIndicator, null, CmOperation.NULL.getName(), value);
//        String message = cmClient.prepareMessageFromPayloadMap(payloadMapMessage);
//        cmClient.sendNotification(message);
//    }
}
