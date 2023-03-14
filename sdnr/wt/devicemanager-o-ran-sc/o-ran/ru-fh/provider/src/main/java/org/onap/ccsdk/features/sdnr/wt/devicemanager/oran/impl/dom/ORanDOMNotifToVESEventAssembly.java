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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.dom;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESNotificationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanDOMNotifToVESEventAssembly {

    private static final Logger log = LoggerFactory.getLogger(ORanDOMNotifToVESEventAssembly.class);
    private static final String VES_EVENT_DOMAIN = "notification";
    private static final String VES_EVENTTYPE = "ORAN_notification";
    private static final String VES_EVENT_PRIORITY = "Normal";
    private NetconfDomAccessor netconfDomAccessor;
    private VESCollectorService vesProvider;

    public ORanDOMNotifToVESEventAssembly(@NonNull NetconfDomAccessor netconfDomAccessor,
            @NonNull VESCollectorService vesCollectorService) {
        this.netconfDomAccessor = Objects.requireNonNull(netconfDomAccessor);
        this.vesProvider = Objects.requireNonNull(vesCollectorService);
    }

    // VES CommonEventHeader fields
    public VESCommonEventHeaderPOJO createVESCommonEventHeader(Instant time, String notificationTypeName,
            long sequenceNo) {
        VESCommonEventHeaderPOJO vesCEH = new VESCommonEventHeaderPOJO();
        vesCEH.setDomain(VES_EVENT_DOMAIN);
        vesCEH.setEventName(notificationTypeName);
        vesCEH.setEventType(VES_EVENTTYPE);
        vesCEH.setPriority(VES_EVENT_PRIORITY);

        String eventId;

        eventId = notificationTypeName + "-" + Long.toUnsignedString(sequenceNo);

        vesCEH.setEventId(eventId);
        vesCEH.setStartEpochMicrosec(time.toEpochMilli() * 1000);
        vesCEH.setLastEpochMicrosec(time.toEpochMilli() * 1000);
        vesCEH.setNfVendorName("ORAN");
        vesCEH.setReportingEntityName(vesProvider.getConfig().getReportingEntityName());
        vesCEH.setSequence(sequenceNo);
        vesCEH.setSourceId("ORAN");
        vesCEH.setSourceName(netconfDomAccessor.getNodeId().getValue());
        return vesCEH;
    }

    // Notification fields
    public VESNotificationFieldsPOJO createVESNotificationFields(HashMap<String, String> xPathFields,
            String notificationTypeName) {
        VESNotificationFieldsPOJO vesNotifFields = new VESNotificationFieldsPOJO();

        vesNotifFields.setChangeType(notificationTypeName);
        vesNotifFields.setChangeIdentifier(netconfDomAccessor.getNodeId().getValue());

        StringBuffer buf = new StringBuffer();
        Iterator<Entry<String, String>> it = xPathFields.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> pair = it.next();
            buf.append("\n" + pair.getKey() + " = " + pair.getValue());
        }
        log.debug("Resultlist({}):{}", xPathFields.size(), buf.toString());

        ArrayList<HashMap<String, Object>> arrayOfNamedHashMap = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> namedHashMap = new HashMap<String, Object>();
        namedHashMap.put("hashMap", xPathFields);
        namedHashMap.put("name", notificationTypeName);
        arrayOfNamedHashMap.add(namedHashMap);
        vesNotifFields.setArrayOfNamedHashMap(arrayOfNamedHashMap);
        return vesNotifFields;

    }
}
