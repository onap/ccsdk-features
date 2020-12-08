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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.VESEvent;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.VESNotificationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanNotifToVESEventAssembly {

    private static final Logger log = LoggerFactory.getLogger(ORanNotifToVESEventAssembly.class);
    private static final String VES_EVENT_DOMAIN = "notification";
    private static final String VES_EVENTTYPE = "ORAN_notification";
    private static final String VES_EVENT_PRIORITY = "Normal";
    private NetconfAccessor netconfAccessor;
    private VESCollectorService vesProvider;

    public ORanNotifToVESEventAssembly(NetconfAccessor netconfAccessor, VESCollectorService vesProvider) {
        this.netconfAccessor = netconfAccessor;
        this.vesProvider = vesProvider;
    }

    public String performAssembly(HashMap<String, String> xPathFieldsMap, Instant instant, String notificationTypeName,
            long sequenceNo) {
        VESEvent data = assembleVESEventMsg(xPathFieldsMap, instant, notificationTypeName, sequenceNo);
        return createVESEventJSON(data);
    }

    public VESEvent assembleVESEventMsg(HashMap<String, String> xPathFieldsMap, Instant instant,
            String notificationTypeName, long sequenceNo) {
        VESCommonEventHeaderPOJO vesCEH = createVESCommonEventHeader(instant, notificationTypeName, sequenceNo);
        VESNotificationFieldsPOJO vesNotifFields = createVESNotificationFields(xPathFieldsMap, notificationTypeName);

        VESEvent vesEvent = new VESEvent();
        vesEvent.addEventObjects(vesCEH);
        vesEvent.addEventObjects(vesNotifFields);

        return vesEvent;
    }

    public String createVESEventJSON(VESEvent vesEvent) {
        String oranVESMsg = "";
        try {
            ObjectMapper objMapper = new ObjectMapper();
            oranVESMsg = objMapper.writeValueAsString(vesEvent);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        log.debug("VES Message generated from ORAN Netconf Notification is - {}", oranVESMsg);
        return oranVESMsg;
    }

    // VES CommonEventHeader fields
    private VESCommonEventHeaderPOJO createVESCommonEventHeader(Instant time, String notificationTypeName,
            long sequenceNo) {
        VESCommonEventHeaderPOJO vesCEH = new VESCommonEventHeaderPOJO();
        vesCEH.setDomain(VES_EVENT_DOMAIN);
        vesCEH.setEventName(notificationTypeName);
        vesCEH.setEventType(VES_EVENTTYPE);
        vesCEH.setPriority(VES_EVENT_PRIORITY);

        String eventId;

        eventId = notificationTypeName + "-" + Long.toUnsignedString(sequenceNo);

        vesCEH.setEventId(eventId);
        vesCEH.setStartEpochMicrosec(time.toEpochMilli() * 100);
        vesCEH.setLastEpochMicrosec(time.toEpochMilli() * 100);
        vesCEH.setNfVendorName("ORAN");
        vesCEH.setReportingEntityName(vesProvider.getConfig().getReportingEntityName());
        vesCEH.setSequence(sequenceNo);
        vesCEH.setSourceId("ORAN");
        vesCEH.setSourceName(netconfAccessor.getNodeId().getValue());
        return vesCEH;
    }

    // Notification fields
    private VESNotificationFieldsPOJO createVESNotificationFields(HashMap<String, String> xPathFields,
            String notificationTypeName) {
        VESNotificationFieldsPOJO vesNotifFields = new VESNotificationFieldsPOJO();

        vesNotifFields.setChangeType(notificationTypeName);
        vesNotifFields.setChangeIdentifier(netconfAccessor.getNodeId().getValue());

        StringBuffer buf = new StringBuffer();
        Iterator<Entry<String, String>> it = xPathFields.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> pair = it.next();
            buf.append("\n" + pair.getKey() + " = " + pair.getValue());
        }
        log.info("Resultlist({}):{}", xPathFields.size(), buf.toString());

        ArrayList<HashMap<String, Object>> arrayOfNamedHashMap = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> namedHashMap = new HashMap<String, Object>();
        namedHashMap.put("hashMap", xPathFields);
        namedHashMap.put("name", notificationTypeName);
        arrayOfNamedHashMap.add(namedHashMap);
        vesNotifFields.setArrayOfNamedHashMap(arrayOfNamedHashMap);
        return vesNotifFields;

    }
}
