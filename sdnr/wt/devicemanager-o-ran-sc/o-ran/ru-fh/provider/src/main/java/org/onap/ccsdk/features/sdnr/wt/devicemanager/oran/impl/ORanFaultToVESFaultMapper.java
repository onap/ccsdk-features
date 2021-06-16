/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl;

import java.time.Instant;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESFaultFieldsPOJO;
import org.opendaylight.yang.gen.v1.urn.o.ran.fm._1._0.rev190204.AlarmNotif;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@formatter:off
/*
 * Maps ORAN Fault fields to VES fault domain fields and VES commonEventHeader fields
 *
 *
 *      VES Fields                  Mapping
 *      ----------                  -------
 *      domain                      "fault"
 *      eventId                     "nt:network-topology/nt:topology/nt:node/nt:node-id"
 *      eventName                   "nt:network-topology/nt:topology/nt:node/nt:node-id"
 *      eventType                   "O-RAN-RU-Fault"
 *      lastEpochMicrosec           TimeStamp represented by <eventTime> field in NetConf notification header in unix time format - as microseconds elapsed since 1 Jan 1970 not including leap seconds.
 *      nfcNamingCode               always ""
 *      nfNamingCode                always ""
 *      nfVendorName                /ietf-hardware:hardware/component[not(parent)][1]/mfg-name
 *      priority                    "Normal"
 *      reportingEntityId           The OAM-Controller identifier with in the SMO - e.g. the fully qualified domain name or IP-Address.
 *      reportingEntityName         as configured by helm charts for the OpenDaylight cluster name ??????
 *      sequence                    As per NetConf notification increasing sequence number as unsigned integer 32 bits. The value is reused in the eventId field.
 *      sourceId                    Value of ietf-hardware (RFC8348) /hardware/component[not(parent)][1]/uuid or 'nt:network-topology/nt:topology/nt:node/nt:node-id' if ietf component not found.
 *      sourceName                  "nt:network-topology/nt:topology/nt:node/nt:node-id"
 *      startEpochMicrosec          Current OAM-Controller Node timestamp in unix time format - as microseconds elapsed since 1 Jan 1970 not including leap seconds.
 *      timeZoneOffset              Static text: "+00:00"
 *      version                     "4.1"
 *      vesEventListenerVersion     "7.2.1"
 *
 *
 *      alarmAdditionalInformation
 *      alarmCondition              Value of "o-ran-fm:alarm-notif/fault-id"
 *      alarmInterfaceA             Value of "o-ran-fm:alarm-notif/fault-source"
 *      eventCategory               Static text "O-RU failure"
 *      eventSeverity               Value of "o-ran-fm:alarm-notif/fault-severity". But if "o-ran-fm:alarm-notif/is-cleared" then "NORMAL"
 *      eventSourceType             The value of ietf-hardware (RFC8348) /hardware/component[not(parent)][1]/model-name or "O-RU" if not found.
 *      faultFieldsVersion          "4.0"
 *      specificProblem             A mapping of the fault-id to its description according to O-RAN OpenFronthaul specification.
 *      vfStatus                    "Active"
 *
 */
//@formatter:on

public class ORanFaultToVESFaultMapper {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ORanFaultToVESFaultMapper.class);
    private static final String VES_EVENT_DOMAIN = "fault";
    private static final String VES_EVENTTYPE = "ORAN_Fault";
    private static final String VES_EVENT_PRIORITY = "Normal";
    private static final String VES_EVENT_CATEGORY = "O-RU Failure";
    private static final String VES_FAULT_FIELDS_VERSION = "4.0";
    private static final String VES_FAULT_FIELDS_VFSTATUS = "Active"; //virtual function status

    private final VESCollectorService vesProvider;
    private final String notifName;    // Name
    private final String nodeIdString; // Sourcename
    private String mfgName;
    private String uuid;
    private String modelName;

    public ORanFaultToVESFaultMapper(NodeId nodeId, VESCollectorService vesCollectorService,
            String notifName) {
        this.nodeIdString = nodeId.getValue();
        this.vesProvider = vesCollectorService;
        this.notifName = notifName;
    }

    public void setMfgName(String mfgName) {
        this.mfgName= mfgName;
    }

    private String getMfgName() {
        return mfgName;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    private String getUuid() {
        return uuid;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    private String getModelName() {
        return modelName;
    }

    public VESCommonEventHeaderPOJO mapCommonEventHeader(AlarmNotif notification, Instant eventTime, int sequenceNo) {
        VESCommonEventHeaderPOJO vesCEH = new VESCommonEventHeaderPOJO();
        vesCEH.setDomain(VES_EVENT_DOMAIN);
        vesCEH.setEventName(notifName);
        vesCEH.setEventType(VES_EVENTTYPE);
        vesCEH.setPriority(VES_EVENT_PRIORITY);

        String eventId;

        eventId = notifName + "-" + Long.toUnsignedString(sequenceNo);

        vesCEH.setEventId(eventId);
        vesCEH.setStartEpochMicrosec(eventTime.toEpochMilli() * 1000);
        vesCEH.setLastEpochMicrosec(eventTime.toEpochMilli() * 1000);
        vesCEH.setNfVendorName(getMfgName());
        vesCEH.setReportingEntityName(vesProvider.getConfig().getReportingEntityName());
        vesCEH.setSequence(sequenceNo);
        vesCEH.setSourceId(getUuid());
        vesCEH.setSourceName(nodeIdString);

        return vesCEH;
    }

    public VESFaultFieldsPOJO mapFaultFields(AlarmNotif alarmNotif) {
        VESFaultFieldsPOJO vesFaultFields = new VESFaultFieldsPOJO();

        vesFaultFields.setAlarmCondition(alarmNotif.getFaultId().toString());
        vesFaultFields.setAlarmInterfaceA(alarmNotif.getFaultSource());
        vesFaultFields.setEventCategory(VES_EVENT_CATEGORY);
        vesFaultFields.setEventSeverity(alarmNotif.getFaultSeverity().getName());
        vesFaultFields.setEventSourceType(getModelName());
        vesFaultFields.setFaultFieldsVersion(VES_FAULT_FIELDS_VERSION);
        vesFaultFields.setSpecificProblem(alarmNotif.getFaultText());
        vesFaultFields.setVfStatus(VES_FAULT_FIELDS_VFSTATUS);

        return vesFaultFields;
    }

}
