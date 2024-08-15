/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt mountpoint-registrar
 * =================================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.stnddefined;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config.GeneralConfig;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.InvalidMessageException;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.StrimziKafkaVESMsgConsumerImpl;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.fault.FaultNotificationClient;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrimziKafkaStndDefinedFaultVESMsgConsumer extends StrimziKafkaVESMsgConsumerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(StrimziKafkaStndDefinedFaultVESMsgConsumer.class);
    Map<String, String> payloadMapMessage = null;
    String faultNodeId;
    String notificationType;

    public StrimziKafkaStndDefinedFaultVESMsgConsumer(GeneralConfig generalConfig) {
        super(generalConfig);
        LOG.info("StrimziKafkaStndDefinedFaultVESMsgConsumer started successfully");
    }

    /*
     * Supports processing of notifyNewAlarm and notifyClearedAlarm messages ONLY
     */
    @Override
    public void processMsg(String msg) throws InvalidMessageException, JsonProcessingException {
        LOG.debug("Processing StndDefined Fault message {}", msg);
        JsonNode rootNode = convertMessageToJsonNode(msg);
        try {

            faultNodeId = rootNode.at("/event/commonEventHeader/sourceName").textValue();
            notificationType = rootNode.at("/event/stndDefinedFields/data/notificationType").textValue();

            if (notificationType.equalsIgnoreCase("notifyNewAlarm")) {
                LOG.info("Read stndDefined Fault message of type - {} with id {} from Kafka topic", notificationType,
                        faultNodeId);
                processNewAlarm(rootNode);
            } else if (notificationType.equalsIgnoreCase("notifyClearedAlarm")) {
                LOG.info("Read stdnDefined Fault message of type - {} with id {} from Kafka topic", notificationType,
                        faultNodeId);
                processClearedAlarm(rootNode);
            } else {
                LOG.warn(
                        "Read stdnDefined Fault message of type - {} with id {} from Kafka topic. No suitable implementation for processing this message",
                        notificationType, faultNodeId);
                throw new InvalidMessageException();
            }
            // Send Fault Notification
            String baseUrl = getBaseUrl();
            String sdnrUser = getSDNRUser();
            String sdnrPasswd = getSDNRPasswd();

            FaultNotificationClient faultClient = new FaultNotificationClient(baseUrl);
            LOG.debug("Setting RESTConf Authorization values - {} : {}", sdnrUser, sdnrPasswd);
            faultClient.setAuthorization(sdnrUser, sdnrPasswd);
            String message = faultClient.prepareMessageFromPayloadMap(payloadMapMessage);
            faultClient.sendNotification(message);
        } catch (NullPointerException e) {
            LOG.warn("Message is invalid, sending aborted, processing stopped because one of fields is missing");
            throw new InvalidMessageException("Missing field");
        }

    }

    private void processClearedAlarm(JsonNode rootNode) {
        String faultOccurrenceTime =
                Instant.ofEpochMilli(rootNode.at("/event/commonEventHeader/startEpochMicrosec").longValue() / 1000)
                        .atZone(ZoneId.of("Z")).toString();
        int faultSequence = rootNode.at("/event/commonEventHeader/sequence").intValue();
        String faultObjectId = rootNode.at("/event/stndDefinedFields/data/alarmId").textValue();
        String faultReason = rootNode.at("/event/stndDefinedFields/data/probableCause").textValue();
        String faultSeverity = SeverityType.NonAlarmed.toString();

        payloadMapMessage = FaultNotificationClient.createFaultNotificationPayloadMap(faultNodeId,
                Integer.toString(faultSequence), faultOccurrenceTime, faultObjectId, faultReason, faultSeverity);

    }

    private void processNewAlarm(JsonNode rootNode) {
        String faultOccurrenceTime =
                Instant.ofEpochMilli(rootNode.at("/event/commonEventHeader/startEpochMicrosec").longValue() / 1000)
                        .atZone(ZoneId.of("Z")).toString();
        int faultSequence = rootNode.at("/event/commonEventHeader/sequence").intValue();
        String faultObjectId = rootNode.at("/event/stndDefinedFields/data/alarmId").textValue();
        String faultReason = rootNode.at("/event/stndDefinedFields/data/probableCause").textValue();
        String faultSeverity =
                getSDNRSeverityType(rootNode.at("/event/stndDefinedFields/data/perceivedSeverity").textValue());

        payloadMapMessage = FaultNotificationClient.createFaultNotificationPayloadMap(faultNodeId,
                Integer.toString(faultSequence), faultOccurrenceTime, faultObjectId, faultReason, faultSeverity);

    }

    /*
     * 3GPP Definition PerceivedSeverity: type: string enum: - INDETERMINATE -
     * CRITICAL - MAJOR - MINOR - WARNING - CLEARED
     *
     */
    private String getSDNRSeverityType(String faultSeverity) {
        if (faultSeverity.equalsIgnoreCase("critical")) {
            faultSeverity = SeverityType.Critical.toString();
        } else if (faultSeverity.equalsIgnoreCase("major")) {
            faultSeverity = SeverityType.Major.toString();
        } else if (faultSeverity.equalsIgnoreCase("minor")) {
            faultSeverity = SeverityType.Minor.toString();
        } else if (faultSeverity.equalsIgnoreCase("warning") || faultSeverity.equalsIgnoreCase("indeterminate")) {
            faultSeverity = SeverityType.Warning.toString();
        } else if (faultSeverity.equalsIgnoreCase("cleared")) {
            faultSeverity = SeverityType.NonAlarmed.toString();
        } else {
            faultSeverity = SeverityType.NonAlarmed.toString();
        }
        return faultSeverity;
    }

}
