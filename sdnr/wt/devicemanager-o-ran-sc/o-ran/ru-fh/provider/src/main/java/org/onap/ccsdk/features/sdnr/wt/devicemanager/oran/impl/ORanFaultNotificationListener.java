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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESFaultFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESMessage;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.Component;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.o.ran.fm._1._0.rev190204.AlarmNotif;
import org.opendaylight.yang.gen.v1.urn.o.ran.fm._1._0.rev190204.ORanFmListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanFaultNotificationListener implements ORanFmListener {

    private static final Logger LOG = LoggerFactory.getLogger(ORanFaultNotificationListener.class);

    private final @NonNull NetconfBindingAccessor netconfAccessor;
    private final @NonNull VESCollectorService vesCollectorService;
    private final @NonNull ORanFaultToVESFaultMapper mapper;
    private final @NonNull FaultService faultService;
    private final @NonNull WebsocketManagerService websocketManagerService;
    private final @NonNull DataProvider databaseService;
    private static final NetconfTimeStamp NETCONFTIME_CONVERTER = NetconfTimeStampImpl.getConverter();

    private Integer counter; //Local counter is assigned to Notifications

    public ORanFaultNotificationListener(@NonNull NetconfBindingAccessor netconfAccessor,
            @NonNull VESCollectorService vesCollectorService, @NonNull FaultService faultService,
            @NonNull WebsocketManagerService websocketManagerService, @NonNull DataProvider databaseService) {
        this.netconfAccessor = Objects.requireNonNull(netconfAccessor);
        this.vesCollectorService = Objects.requireNonNull(vesCollectorService);
        this.faultService = Objects.requireNonNull(faultService);
        this.websocketManagerService = Objects.requireNonNull(websocketManagerService);
        this.databaseService = Objects.requireNonNull(databaseService);

        this.mapper = new ORanFaultToVESFaultMapper(netconfAccessor.getNodeId(), vesCollectorService,
                AlarmNotif.class.getSimpleName());
        this.counter = 0;
    }

    /**
     * Gets the mfg name, mode-name and Uuid of the root component (Ex: Chassis.)
     * In cases where there are multiple root components i.e., components with no parent,
     * the Uuid of the last occurred component from the componentList will be considered.
     * Till now we haven't seen Uuid set for root components, so not an issue for now.
     * @param componentList
     */
    public void setComponentList(Collection<Component> componentList) {
        for (Component component : ORanToInternalDataModel.getRootComponents(componentList)) {
            mapper.setMfgName(component.getMfgName());
            mapper.setUuid(component.getUuid()!=null?component.getUuid().getValue():netconfAccessor.getNodeId().getValue());
            mapper.setModelName(component.getModelName());
        }
    }

    @Override
    public void onAlarmNotif(AlarmNotif notification) {

        LOG.debug("onAlarmNotif {}", notification.getClass().getSimpleName());
        counter++;

        // Send devicemanager specific notification for database and ODLUX
        Instant eventTimeInstant = ORanToInternalDataModel.getInstantTime(notification.getEventTime());
        faultService.faultNotification(
                ORanToInternalDataModel.getFaultLog(notification, netconfAccessor.getNodeId(), counter));
        // Send model specific notification to WebSocketManager
        websocketManagerService.sendNotification(notification, netconfAccessor.getNodeId(), AlarmNotif.QNAME);

        try {
            if (vesCollectorService.getConfig().isVESCollectorEnabled()) {
                VESCommonEventHeaderPOJO header = mapper.mapCommonEventHeader(notification, eventTimeInstant, counter);
                VESFaultFieldsPOJO body = mapper.mapFaultFields(notification);
                VESMessage vesMsg = vesCollectorService.generateVESEvent(header, body);
                vesCollectorService.publishVESMessage(vesMsg);
                LOG.info("VES Message is  {}",vesMsg.getMessage());
                writeToEventLog(vesMsg.getMessage(), eventTimeInstant, AlarmNotif.QNAME.toString(), counter);
            }
        } catch (JsonProcessingException | DateTimeParseException e) {
            LOG.debug("Can not convert event into VES message {}", notification, e);
        }
    }

    private void writeToEventLog(String data, Instant instant, String notificationName,
            int sequenceNo) {
        EventlogBuilder eventlogBuilder = new EventlogBuilder();

        eventlogBuilder.setObjectId("Device");
        eventlogBuilder.setCounter(sequenceNo);
        eventlogBuilder.setAttributeName(notificationName);
        eventlogBuilder.setNodeId(netconfAccessor.getNodeId().getValue());
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

        Date eventDate = Date.from(instant);
        eventlogBuilder.setTimestamp(new DateAndTime(NETCONFTIME_CONVERTER.getTimeStamp(eventDate)));

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
