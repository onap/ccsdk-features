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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESFaultFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESNotificationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESPNFRegistrationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.yangtools.yang.binding.Notification;

/*
 * Interface for mapping ODL notification to VES event fields grouped by event type.
 * Also includes the commonEventHeader which is applicable for all events.
 * Ex: fault event, notification event.
 *
 * No base implementation exists for this interface. Clients that need to map their data into VES formats must implement this interface.
 */

public abstract class VESEventMapper<N extends Notification,F extends Notification,T extends Notification> {

    /**
     * Creates VESEvent mapping
     */
    public abstract String createMapping(NetconfBindingAccessor netconfAccessor,
            VESCollectorService vesCollectorService, Notification notification, String notifName, int sequenceNo,
            Instant eventTime);

    /**
     * Returns VES commonEventHeader fields
     */
    public abstract VESCommonEventHeaderPOJO mapCommonEventHeader(N notification);

    /**
     * Returns VES faultFields
     */
    public abstract VESFaultFieldsPOJO mapFaultFields(F notification);

    /**
     * Returns VES Notification Fields
     */
    public abstract VESNotificationFieldsPOJO mapNotificationFields(T notification);

    /**
     * Returns VES pnfRegistration domain fields
     *
     * @return
     */
    public abstract VESPNFRegistrationFieldsPOJO mapPNFRegistrationFields();

    /**
     * Generates VES Event JSON containing commonEventHeader and notificationFields fields
     *
     * @param commonEventHeader
     * @param notifFields
     * @return String - representing the VESEvent JSON
     */
    String generateVESEvent(VESCommonEventHeaderPOJO commonEventHeader, VESNotificationFieldsPOJO notifFields) {
        Map<String, Object> innerEvent = new HashMap<String, Object>();
        innerEvent.put("commonEventHeader", commonEventHeader);
        innerEvent.put("notificationFields", notifFields);

        Map<String, Object> outerEvent = new HashMap<String, Object>();
        outerEvent.put("event", innerEvent);
        try {
            ObjectMapper objMapper = new ObjectMapper();
            return objMapper.writeValueAsString(outerEvent);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generates VES Event JSON containing commonEventHeader and faultFields fields
     *
     * @param commonEventHeader
     * @param faultFields
     * @return String - representing the VES Event JSON
     */
    String generateVESEvent(VESCommonEventHeaderPOJO commonEventHeader, VESFaultFieldsPOJO faultFields) {
        Map<String, Object> innerEvent = new HashMap<String, Object>();
        innerEvent.put("commonEventHeader", commonEventHeader);
        innerEvent.put("faultFields", faultFields);

        Map<String, Object> outerEvent = new HashMap<String, Object>();
        outerEvent.put("event", innerEvent);
        try {
            ObjectMapper objMapper = new ObjectMapper();
            return objMapper.writeValueAsString(outerEvent);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
