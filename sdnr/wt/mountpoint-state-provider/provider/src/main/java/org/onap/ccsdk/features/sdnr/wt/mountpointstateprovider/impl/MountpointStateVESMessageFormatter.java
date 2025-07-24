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

    public MountpointStateVESMessageFormatter(VESCollectorService vesCollectorService) {
        this.vesCfg = vesCollectorService.getConfig();
        this.vesCollectorService = vesCollectorService;
    }

    private static void incrSequenceNo() {
        sequenceNo++;
    }

    private long getSequenceNo() {
        return sequenceNo;
    }

    public VESMessage createVESMessage(String nodeId, String connectionStatus,
            Instant timestamp) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("format to VES from {}, {}, {}", nodeId, connectionStatus, timestamp);
        }

        MountpointStateVESMessageFormatter.incrSequenceNo();

        VESCommonEventHeaderPOJO vesCommonEventHeader = createVESCommonEventHeader(nodeId, connectionStatus, timestamp);
        VESNotificationFieldsPOJO vesNotificationFields = createVESNotificationFields(nodeId, connectionStatus);

        VESMessage vesMsg = null;
        try {
            vesMsg = vesCollectorService.generateVESEvent(vesCommonEventHeader, vesNotificationFields);
            LOG.debug("VES Message is - {}", vesMsg.getMessage());
        } catch (JsonProcessingException e) {
            LOG.error("Exception while generating VES Event - ", e);
        }

        return vesMsg;

    }

    private VESNotificationFieldsPOJO createVESNotificationFields(String nodeId, String connectionStatus) {
        VESNotificationFieldsPOJO vesNotificationFields = new VESNotificationFieldsPOJO();

        vesNotificationFields.setChangeIdentifier(nodeId);
        vesNotificationFields.setChangeType(Constants.VES_CHANGETYPE);
        vesNotificationFields.setNewState(connectionStatus);

        return vesNotificationFields;
    }

    private VESCommonEventHeaderPOJO createVESCommonEventHeader(String nodeId, String connectionStatus,
            Instant timestamp) {
        VESCommonEventHeaderPOJO vesCommonEventHeader = new VESCommonEventHeaderPOJO();

        vesCommonEventHeader.setDomain(Constants.VES_DOMAIN);
        vesCommonEventHeader.setEventId(nodeId + "_" + connectionStatus + "_" + getSequenceNo());
        vesCommonEventHeader.setEventName(nodeId + "_" + connectionStatus + "_" + getSequenceNo());
        vesCommonEventHeader.setSourceName(nodeId);
        vesCommonEventHeader.setPriority(Constants.VES_PRIORITY);
        vesCommonEventHeader.setReportingEntityId(this.vesCfg.getReportingEntityId());
        vesCommonEventHeader.setReportingEntityName(this.vesCfg.getReportingEntityName());
        vesCommonEventHeader.setSequence(getSequenceNo());
        vesCommonEventHeader.setLastEpochMicrosec(timestamp.toEpochMilli() * 1000);
        vesCommonEventHeader.setStartEpochMicrosec(timestamp.toEpochMilli() * 1000);

        return vesCommonEventHeader;
    }
}
