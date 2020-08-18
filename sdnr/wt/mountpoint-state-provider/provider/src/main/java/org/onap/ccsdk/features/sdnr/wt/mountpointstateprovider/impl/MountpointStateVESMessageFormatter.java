/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MountpointStateVESMessageFormatter {
    private static final Logger LOG = LoggerFactory.getLogger(MountpointStateVESMessageFormatter.class);
    private static final String VES_DOMAIN = "notification";
    private static final String VES_PRIORITY = "Normal";
    private static final String VES_CHANGETYPE = "ConnectionState";

    private VESCollectorCfgService vesCfg;
    static long sequenceNo = 0;

    public MountpointStateVESMessageFormatter(VESCollectorCfgService vesCfg) {
        this.vesCfg = vesCfg;
    }

    public String createVESMessage(JSONObject obj) {
        LOG.debug("JSON Object to format to VES is - {}", obj.toString());
        String vesMsg = "{}";
        sequenceNo++;

        VESCommonEventHeaderPOJO vesCommonEventHeader = createVESCommonEventHeader(obj);
        VESNotificationFieldsPOJO vesNotificationFields = createVESNotificationFields(obj);

        VESEvent vesEvent = new VESEvent();
        vesEvent.addEventObjects(vesCommonEventHeader);
        vesEvent.addEventObjects(vesNotificationFields);

        try {
            ObjectMapper objMapper = new ObjectMapper();
            vesMsg = objMapper.writeValueAsString(vesEvent);
            LOG.debug("VES message to be published - {}", vesMsg);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return vesMsg;

    }

    private VESNotificationFieldsPOJO createVESNotificationFields(JSONObject obj) {
        VESNotificationFieldsPOJO vesNotificationFields = new VESNotificationFieldsPOJO();

        vesNotificationFields.setChangeIdentifier(obj.getString("NodeId"));
        vesNotificationFields.setChangeType(VES_CHANGETYPE);
        vesNotificationFields.setNewState(obj.getString("NetConfNodeState"));

        return vesNotificationFields;
    }

    private VESCommonEventHeaderPOJO createVESCommonEventHeader(JSONObject obj) {
        VESCommonEventHeaderPOJO vesCommonEventHeader = new VESCommonEventHeaderPOJO();

        vesCommonEventHeader.setDomain(VES_DOMAIN);
        vesCommonEventHeader
                .setEventId(obj.getString("NodeId") + "_" + obj.getString("NetConfNodeState") + "_" + sequenceNo);
        vesCommonEventHeader
                .setEventName(obj.getString("NodeId") + "_" + obj.getString("NetConfNodeState") + "_" + sequenceNo);
        vesCommonEventHeader.setSourceName(obj.getString("NodeId"));
        vesCommonEventHeader.setPriority(VES_PRIORITY);
        vesCommonEventHeader.setReportingEntityName(this.vesCfg.getReportingEntityName());
        vesCommonEventHeader.setSequence(sequenceNo);

        Instant time = (Instant) obj.get("TimeStamp");
        vesCommonEventHeader.setLastEpochMicrosec(time.toEpochMilli() * 100);
        vesCommonEventHeader.setStartEpochMicrosec(time.toEpochMilli() * 100);

        return vesCommonEventHeader;
    }
}
