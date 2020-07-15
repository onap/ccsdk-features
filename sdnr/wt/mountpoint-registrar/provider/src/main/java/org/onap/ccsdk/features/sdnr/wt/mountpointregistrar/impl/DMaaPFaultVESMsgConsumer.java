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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMaaPFaultVESMsgConsumer extends DMaaPVESMsgConsumerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(DMaaPFaultVESMsgConsumer.class);

    //private static int faultCounter = 0;
    private static final String DEFAULT_SDNRUSER = "admin";
    private static final String DEFAULT_SDNRPASSWD = "admin";

    private final GeneralConfig generalConfig;

    public DMaaPFaultVESMsgConsumer(GeneralConfig generalConfig) {
        this.generalConfig = generalConfig;
    }

    @Override
    public void processMsg(String msg) throws Exception {
        String faultNodeId;
        String faultOccurrenceTime;
        String faultObjectId;
        String faultReason;
        String faultSeverity;
        int faultSequence;
        ObjectMapper oMapper = new ObjectMapper();
        JsonNode dmaapMessageRootNode;

        LOG.info("Fault VES Message is - {}", msg);
        try {
            dmaapMessageRootNode = oMapper.readTree(msg);
            faultNodeId = dmaapMessageRootNode.at("/event/commonEventHeader/sourceName").textValue();
            faultOccurrenceTime =
                    dmaapMessageRootNode.at("/event/faultFields/alarmAdditionalInformation/eventTime").textValue();
            faultObjectId = dmaapMessageRootNode.at("/event/faultFields/alarmInterfaceA").textValue();
            faultReason = dmaapMessageRootNode.at("/event/faultFields/specificProblem").textValue();
            faultSeverity = dmaapMessageRootNode.at("/event/faultFields/eventSeverity").textValue();
            faultSequence = dmaapMessageRootNode.at("/event/commonEventHeader/sequence").intValue();
            //faultCounter++;

            if (faultSeverity.equalsIgnoreCase("critical")) {
                faultSeverity = SeverityType.Critical.toString();
            } else if (faultSeverity.equalsIgnoreCase("major")) {
                faultSeverity = SeverityType.Major.toString();
            } else if (faultSeverity.equalsIgnoreCase("minor")) {
                faultSeverity = SeverityType.Minor.toString();
            } else if (faultSeverity.equalsIgnoreCase("warning")) {
                faultSeverity = SeverityType.Warning.toString();
            } else if (faultSeverity.equalsIgnoreCase("nonalarmed")) {
                faultSeverity = SeverityType.NonAlarmed.toString();
            } else {
                faultSeverity = SeverityType.NonAlarmed.toString();
            }

            String baseUrl = getBaseUrl();
            String sdnrUser = getSDNRUser();
            String sdnrPasswd = getSDNRPasswd();

            FaultNotificationClient faultClient = getFaultNotificationClient(baseUrl);
            faultClient.setAuthorization(sdnrUser, sdnrPasswd);
            faultClient.sendFaultNotification(faultNodeId, Integer.toString(faultSequence), faultOccurrenceTime,
                    faultObjectId, faultReason, faultSeverity);

        } catch (IOException e) {
            LOG.info("Cannot parse json object ");
            throw new Exception("Cannot parse json object", e);
        }
    }

    public String getBaseUrl() {
        return generalConfig.getBaseUrl();
    }

    public String getSDNRUser() {
        return generalConfig.getSDNRUser() != null ? generalConfig.getSDNRUser() : DEFAULT_SDNRUSER;
    }

    public String getSDNRPasswd() {
        return generalConfig.getSDNRPasswd() != null ? generalConfig.getSDNRPasswd() : DEFAULT_SDNRPASSWD;
    }

    public FaultNotificationClient getFaultNotificationClient(String baseUrl) {
        return new FaultNotificationClient(baseUrl);
    }
}
