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
import java.time.Instant;
import org.eclipse.jdt.annotation.NonNull;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESMessage;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESNotificationFieldsPOJO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MountpointStateVESMessageFormatter {
    private static final Logger LOG = LoggerFactory.getLogger(MountpointStateVESMessageFormatter.class);

    private final @NonNull VESCollectorCfgService vesCfg;
    private final @NonNull VESCollectorService vesCollectorService;
    static long sequenceNo = 0;

    public MountpointStateVESMessageFormatter(VESCollectorCfgService vesCfg, VESCollectorService vesCollectorService) {
        this.vesCfg = vesCfg;
        this.vesCollectorService = vesCollectorService;
    }

    private static void incrSequenceNo() {
        sequenceNo++;
    }

    private long getSequenceNo() {
        return sequenceNo;
    }

    public VESMessage createVESMessage(JSONObject obj) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("JSON Object to format to VES is - {}", obj);
        }

        MountpointStateVESMessageFormatter.incrSequenceNo();

        VESCommonEventHeaderPOJO vesCommonEventHeader = createVESCommonEventHeader(obj);
        VESNotificationFieldsPOJO vesNotificationFields = createVESNotificationFields(obj);

        VESMessage vesMsg = null;
        try {
            vesMsg = vesCollectorService.generateVESEvent(vesCommonEventHeader, vesNotificationFields);
            LOG.debug("VES Message is - {}", vesMsg.getMessage());
        } catch (JsonProcessingException e) {
            LOG.error("Exception while generating VES Event - ", e);
        }

        return vesMsg;

    }

    private VESNotificationFieldsPOJO createVESNotificationFields(JSONObject obj) {
        VESNotificationFieldsPOJO vesNotificationFields = new VESNotificationFieldsPOJO();

        vesNotificationFields.setChangeIdentifier(obj.getString(Constants.NODEID));
        vesNotificationFields.setChangeType(Constants.VES_CHANGETYPE);
        vesNotificationFields.setNewState(obj.getString(Constants.NETCONFNODESTATE));

        return vesNotificationFields;
    }

    private VESCommonEventHeaderPOJO createVESCommonEventHeader(JSONObject obj) {
        VESCommonEventHeaderPOJO vesCommonEventHeader = new VESCommonEventHeaderPOJO();

        vesCommonEventHeader.setDomain(Constants.VES_DOMAIN);
        vesCommonEventHeader
                .setEventId(obj.getString(Constants.NODEID) + "_" + obj.getString(Constants.NETCONFNODESTATE) + "_" + getSequenceNo());
        vesCommonEventHeader
                .setEventName(obj.getString(Constants.NODEID) + "_" + obj.getString(Constants.NETCONFNODESTATE) + "_" + getSequenceNo());
        vesCommonEventHeader.setSourceName(obj.getString(Constants.NODEID));
        vesCommonEventHeader.setPriority(Constants.VES_PRIORITY);
        vesCommonEventHeader.setReportingEntityName(this.vesCfg.getReportingEntityName());
        vesCommonEventHeader.setSequence(getSequenceNo());

        Instant time = (Instant) obj.get(Constants.TIMESTAMP);
        vesCommonEventHeader.setLastEpochMicrosec(time.toEpochMilli() * 100);
        vesCommonEventHeader.setStartEpochMicrosec(time.toEpochMilli() * 100);

        return vesCommonEventHeader;
    }
}
