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
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESFaultFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESMessage;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESNotificationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESPNFRegistrationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESStndDefinedFieldsPOJO;

/**
 * Interface used for publishing VES messages to the VES Collector
 *
 * @author ravi
 *
 */
public interface VESCollectorService extends DeviceManagerService {

    /**
     * Gets the VES Collector configuration from etc/devicemanager.properties configuration file
     */
    VESCollectorCfgService getConfig();

    /**
     * publishes a VES message to the VES Collector by sending a REST request
     * @param vesMsg
     * @return
     */
    boolean publishVESMessage(VESMessage vesMsg);

    /**
     *  clients interested in VES Collector configuration changes can call the registerForChanges method so as to be notified when configuration changes are made
     */
    void registerForChanges(VESCollectorConfigChangeListener o);

    /**
     *  de-registering clients as part of cleanup
     * @param o
     */
    void deregister(VESCollectorConfigChangeListener o);

    /**
     * Get a parser to parse {@link #org.opendaylight.yangtools.yang.binding.Notification } messages
     * @return NotificationProxyParser object
     */
    @NonNull
    NotificationProxyParser getNotificationProxyParser();

    /**
     * Generates VES Event JSON containing commonEventHeader and notificationFields fields
     *
     * @param commonEventHeader
     * @param notifFields
     * @return VESMessage - representing the VESEvent JSON
     * @throws JsonProcessingException
     */
    VESMessage generateVESEvent(VESCommonEventHeaderPOJO commonEventHeader, VESNotificationFieldsPOJO notifFields) throws JsonProcessingException;

    /**
     * Generates VES Event JSON containing commonEventHeader and faultFields fields
     *
     * @param commonEventHeader
     * @param faultFields
     * @return VESMessage - representing the VES Event JSON
     * @throws JsonProcessingException
     */
    VESMessage generateVESEvent(VESCommonEventHeaderPOJO commonEventHeader, VESFaultFieldsPOJO faultFields) throws JsonProcessingException;

    /**
     * Generates VES Event JSON containing commonEventHeader and pnfRegistration fields
     *
     * @param commonEventHeader
     * @param faultFields
     * @return VESMessage - representing the VES Event JSON
     * @throws JsonProcessingException
     */
    VESMessage generateVESEvent(VESCommonEventHeaderPOJO commonEventHeader, VESPNFRegistrationFieldsPOJO faultFields) throws JsonProcessingException;

    /**
     * Generates VES Event JSON containing commonEventHeader and stndDefined fields
     *
     * @param commonEventHeader
     * @param stndDefinedFields
     * @return VESMessage - representing the VES Event JSON
     * @throws JsonProcessingException
     */
    VESMessage generateVESEvent(VESCommonEventHeaderPOJO commonEventHeader, VESStndDefinedFieldsPOJO stndDefinedFields) throws JsonProcessingException;
    
}
