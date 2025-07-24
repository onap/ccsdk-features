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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.dataprovider.ORanDOMToInternalDataModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDeviceManagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.vesmapper.ORanDOMFaultToVESFaultMapper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.yangspecs.ORANFM;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESFaultFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESMessage;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanDOMFaultNotificationListener implements DOMNotificationListener {

    private static final Logger LOG = LoggerFactory.getLogger(ORanDOMFaultNotificationListener.class);

    private final @NonNull NetconfDomAccessor netconfDomAccessor;
    private final @NonNull VESCollectorService vesCollectorService;
    private final @NonNull ORanDOMFaultToVESFaultMapper mapper;
    private final @NonNull FaultService faultService;
    private final @NonNull WebsocketManagerService websocketManagerService;
    private final @NonNull DataProvider databaseService;
    private final @NonNull ORANFM oranfm;

    private Integer counter; //Local counter is assigned to Events in EventLog

    public ORanDOMFaultNotificationListener(@NonNull NetconfDomAccessor netconfDomAccessor, ORANFM oranfm,
            @NonNull VESCollectorService vesCollectorService, @NonNull FaultService faultService,
            @NonNull WebsocketManagerService websocketManagerService, @NonNull DataProvider databaseService) {
        this.netconfDomAccessor = Objects.requireNonNull(netconfDomAccessor);
        this.vesCollectorService = Objects.requireNonNull(vesCollectorService);
        this.faultService = Objects.requireNonNull(faultService);
        this.websocketManagerService = Objects.requireNonNull(websocketManagerService);
        this.databaseService = Objects.requireNonNull(databaseService);
        this.oranfm = oranfm;
        this.mapper = new ORanDOMFaultToVESFaultMapper(netconfDomAccessor.getNodeId(), this.vesCollectorService,
                this.oranfm, "AlarmNotif");
        this.counter = 0;
    }

    @Override
    public void onNotification(@NonNull DOMNotification notification) {
        onAlarmNotif(notification);
    }

    /**
     * Gets the mfg name, mode-name and Uuid of the root component (Ex: Chassis.) In cases where there are multiple root
     * components i.e., components with no parent, the Uuid of the last occurred component from the componentList will
     * be considered. Till now we haven't seen Uuid set for root components, so not an issue for now.
     *
     * @param componentList
     */
    public void setComponentList(Collection<MapEntryNode> componentList) {
        for (MapEntryNode component : ORanDOMToInternalDataModel.getRootComponents(componentList)) {
            mapper.setMfgName(
                    ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_MFG_NAME));
            mapper.setUuid(ORanDMDOMUtility.getLeafValue(component,
                    ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_UUID) != null
                            ? ORanDMDOMUtility.getLeafValue(component,
                                    ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_UUID)
                            : netconfDomAccessor.getNodeId().getValue());
            mapper.setModelName(ORanDMDOMUtility.getLeafValue(component,
                    ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_MODEL_NAME));
        }
    }

    public void onAlarmNotif(DOMNotification notification) {

        LOG.debug("onAlarmNotif {}", notification.getClass().getSimpleName());
        counter++;
        // Send devicemanager specific notification for database and ODLUX
        Instant eventTimeInstant = ORanDMDOMUtility.getNotificationInstant(notification);
        faultService.faultNotification(
                ORanDOMToInternalDataModel.getFaultLog(notification, oranfm, netconfDomAccessor.getNodeId()));
        // Send model specific notification to WebSocketManager
        websocketManagerService.sendNotification(notification, netconfDomAccessor.getNodeId(),
                oranfm.getAlarmNotifQName());

        try {
            if (vesCollectorService.getConfig().isVESCollectorEnabled()) {
                VESCommonEventHeaderPOJO header = mapper.mapCommonEventHeader(notification, eventTimeInstant);
                VESFaultFieldsPOJO body = mapper.mapFaultFields(notification);
                VESMessage vesMsg = vesCollectorService.generateVESEvent(header, body);
                vesCollectorService.publishVESMessage(vesMsg);
                LOG.debug("VES Message is  {}", vesMsg.getMessage());
                writeToEventLog(vesMsg.getMessage(), eventTimeInstant, oranfm.getAlarmNotifQName().getLocalName(),
                        counter);
            }
        } catch (JsonProcessingException | DateTimeParseException e) {
            LOG.debug("Can not convert event into VES message {}", notification, e);
        }
    }

    private void writeToEventLog(String data, Instant eventTimeInstant, String notificationName, int sequenceNo) {
        EventlogBuilder eventlogBuilder = new EventlogBuilder();

        eventlogBuilder.setObjectId("Device");
        eventlogBuilder.setCounter(sequenceNo);
        eventlogBuilder.setAttributeName(notificationName);
        eventlogBuilder.setNodeId(netconfDomAccessor.getNodeId().getValue());
        String eventLogMsgLvl = vesCollectorService.getConfig().getEventLogMsgDetail();
        if (eventLogMsgLvl.equalsIgnoreCase("SHORT")) {
            data = getShortEventLogMessage(data);
        } else if (eventLogMsgLvl.equalsIgnoreCase("MEDIUM")) {
            data = getMediumEventLogMessage(data);
        } else if (eventLogMsgLvl.equalsIgnoreCase("LONG")) {
            // do nothing, data already contains long message
        } else { // Unknown value, default to "SHORT"
            data = getShortEventLogMessage(data);
        }
        eventlogBuilder.setNewValue(data);
        eventlogBuilder.setSourceType(SourceType.Netconf);
        eventlogBuilder.setTimestamp(ORanDMDOMUtility.getDateAndTimeOfInstant(eventTimeInstant));

        databaseService.writeEventLog(eventlogBuilder.build());
    }

    private String getShortEventLogMessage(String data) {
        try {
            JSONObject jsonObj = new JSONObject(data);
            String domain = jsonObj.getJSONObject("event").getJSONObject("commonEventHeader").getString("domain");
            String eventId = jsonObj.getJSONObject("event").getJSONObject("commonEventHeader").getString("eventId");
            return "domain:" + domain + " eventId:" + eventId;
        } catch (JSONException e) {
            LOG.debug("{}", e);
            return "Invalid message received";
        }
    }

    private String getMediumEventLogMessage(String data) {
        try {
            JSONObject jsonObj = new JSONObject(data);
            return jsonObj.getJSONObject("event").getJSONObject("commonEventHeader").toString();
        } catch (JSONException e) {
            LOG.debug("{}", e);
            return "Invalid message received";
        }
    }
}
