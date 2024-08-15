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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Map;

import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.GeneralConfig;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.StrimziKafkaVESMsgConsumerImpl;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.InvalidMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrimziKafkaCMVESMsgConsumer extends StrimziKafkaVESMsgConsumerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(StrimziKafkaCMVESMsgConsumer.class);

    public StrimziKafkaCMVESMsgConsumer(GeneralConfig generalConfig) {
        super(generalConfig);
        LOG.info("StrimziKafkaCMVESMsgConsumer started successfully");
    }

    @Override
    public void processMsg(String msg) throws InvalidMessageException, JsonProcessingException {
        LOG.debug("Processing CM message {}", msg);
        JsonNode rootNode = convertMessageToJsonNode(msg);
        try {

            String cmNodeId = rootNode.at("/event/commonEventHeader/reportingEntityName").textValue();
            String notificationType = rootNode.at("/event/stndDefinedFields/data/notificationType").textValue();

            if (notificationType.equalsIgnoreCase("notifyMOIChanges")) {
                LOG.info("Read CM message from Kafka topic that is moiChanges type with id {}", cmNodeId);
                processMoiChanges(rootNode);
            } else if (notificationType.equalsIgnoreCase("notifyMOICreation")) {
                LOG.info("Read CM message from Kafka topic that is moiCreation type with id {}", cmNodeId);
                sendCMNotification(preparePayloadMapFromMoi(rootNode, "/event/stndDefinedFields/data/attributeList"));
            } else if (notificationType.equalsIgnoreCase("notifyMOIDeletion")) {
                LOG.info("Read CM message from Kafka topic that is moiDeletion type with id {}", cmNodeId);
                sendCMNotification(preparePayloadMapFromMoi(rootNode,"/event/stndDefinedFields/data/attributeList"));
            } else if (notificationType.equalsIgnoreCase("notifyMOIAttributeValueChanges")) {
                LOG.info("Read CM message from Kafka topic that is moiAttributeValueChanges type with id {}", cmNodeId);
                sendCMNotification(preparePayloadMapFromMoi(rootNode,"/event/stndDefinedFields/data/attributeListValueChanges"));
            } else {
                LOG.warn("Message is invalid, sending aborted, wrong CM notification type {}", notificationType);
                throw new InvalidMessageException();
            }

        } catch (NullPointerException e) {
            LOG.warn("Message is invalid, sending aborted, processing stopped because one of fields is missing");
            throw new InvalidMessageException("Missing field");
        }
    }

    private CMBasicHeaderFieldsNotification prepareCMCommonHeaderFields(JsonNode rootNode) {
        return CMBasicHeaderFieldsNotification.builder()
            .withCMNodeId(rootNode.at("/event/commonEventHeader/reportingEntityName").textValue())
            .withCMSequence(rootNode.at("/event/commonEventHeader/sequence").toString())
            .withCMOccurrenceTime(Instant
                .ofEpochMilli(
                    rootNode.at("/event/commonEventHeader/startEpochMicrosec").longValue() / 1000)
                .atZone(ZoneId.of("Z")).toString())
            .withSourceId(rootNode.at("/event/commonEventHeader/sourceId").textValue())
            .withNotificationType(rootNode.at("/event/stndDefinedFields/data/notificationType").textValue())
            .build();
    }

    private void processMoiChanges(JsonNode rootNode) {
        Iterator<JsonNode> nodes = rootNode
            .at("/event/stndDefinedFields/data/moiChanges")
            .elements();
        while (nodes.hasNext()) {
            sendCMNotification(preparePayloadMapFromMoiChangesArray(rootNode, nodes));
        }
    }

    public Map<String, String> preparePayloadMapFromMoiChangesArray(JsonNode rootNode, Iterator<JsonNode> nodes) {
        JsonNode slaidNode = nodes.next();
        return CMNotificationClient.createCMNotificationPayloadMap(
            CMNotification.builder()
                .withCMBasicHeaderFieldsNotification(
                    prepareCMCommonHeaderFields(rootNode))
                .withCMNotificationId(slaidNode.get("notificationId").toString())
                .withCMSourceIndicator(slaidNode.get("sourceIndicator").textValue())
                .withCMPath(slaidNode.get("path").textValue())
                .withCMOperation(slaidNode.get("operation").textValue())
                .withCMValue(slaidNode.get("value").toString()
                    .replace("\"", ""))
                .build());
    }

    public Map<String, String> preparePayloadMapFromMoi(JsonNode rootNode, String cmValueKey){
        return CMNotificationClient.createCMNotificationPayloadMap(
            CMNotification.builder()
                .withCMBasicHeaderFieldsNotification(
                    prepareCMCommonHeaderFields(rootNode))
                .withCMSourceIndicator(rootNode.at("/event/stndDefinedFields/data/sourceIndicator").textValue())
                .withCMValue(rootNode.at(cmValueKey).toString()
                    .replace("\"", ""))
                .build());
    }

    private void sendCMNotification(Map<String, String> payloadMapMessage) {
        CMNotificationClient cmClient = setRESTConfAuthorization();
        String message = cmClient.prepareMessageFromPayloadMap(payloadMapMessage);
        cmClient.sendNotification(message);
    }


    private CMNotificationClient setRESTConfAuthorization() {
        String sdnrUser = getSDNRUser();
        String sdnrPasswd = getSDNRPasswd();

        CMNotificationClient cmClient = new CMNotificationClient(getBaseUrl());
        LOG.debug("Setting RESTConf Authorization values - {} : {}", sdnrUser, sdnrPasswd);
        cmClient.setAuthorization(sdnrUser, sdnrPasswd);
        return cmClient;
    }
}
