/*******************************************************************************
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.listener;

import javax.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.InternalDateAndTime;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.InternalSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.util.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.ProviderClient;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.database.service.HtDatabaseEventsService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.AttributeValueChangedNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ObjectCreationNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ObjectDeletionNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ProblemNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClient;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.maintenance.MaintenanceService;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.ProblemNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible class for documenting changes in the ODL itself. The occurence of such an event is
 * documented in the database and to clients. Specific example here is the registration or
 * deregistration of a netconf device. This service has an own eventcounter to apply to the ONF
 * Coremodel netconf behaviour.
 *
 * Important: Websocket notification must be the last action.
 *
 * @author herbert
 */

public class ODLEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(ODLEventListener.class);

    private static final NetconfTimeStamp NETCONFTIME_CONVERTER = NetconfTimeStamp.getConverter();

    private final String ownKeyName;
    private final WebSocketServiceClient webSocketService;
    private final HtDatabaseEventsService databaseService;
    private final ProviderClient dcaeProvider;
    private final ProviderClient aotsMProvider;
    private final MaintenanceService maintenanceService;

    private int eventNumber;

    /*---------------------------------------------------------------
     * Construct
     */

    /**
     * Create a Service to document events to clients and within a database
     *
     * @param ownKeyName The name of this service, that is used in the database as identification key.
     * @param webSocketService service to direct messages to clients
     * @param databaseService service to write to the database
     * @param dcaeProvider to deliver problems to
     * @param maintenanceService2
     */
    @SuppressWarnings("javadoc")
    public ODLEventListener(String ownKeyName, WebSocketServiceClient webSocketService,
            HtDatabaseEventsService databaseService, ProviderClient dcaeProvider,
            @Nullable ProviderClient aotsMProvider, MaintenanceService maintenanceService) {
        super();

        this.ownKeyName = ownKeyName;
        this.webSocketService = webSocketService;

        this.databaseService = databaseService;
        this.dcaeProvider = dcaeProvider;
        this.aotsMProvider = aotsMProvider;

        this.eventNumber = 0;
        this.maintenanceService = maintenanceService;

    }

    /*---------------------------------------------------------------
     * Handling of ODL Controller events
     */

    /**
     * A registration of a mountpoint occured, that is in connect state
     *
     * @param registrationName Name of the event that is used as key in the database.
     */

    public void registration(String registrationName) {

        ObjectCreationNotificationXml cNotificationXml =
                new ObjectCreationNotificationXml(ownKeyName, getEventNumberAsString(),
                        InternalDateAndTime.valueOf(NETCONFTIME_CONVERTER.getTimeStamp()), registrationName);

        // Write first to prevent missing entries
        databaseService.writeEventLog(cNotificationXml);
        webSocketService.sendViaWebsockets(registrationName, cNotificationXml);
    }


    /**
     * A deregistration of a mountpoint occured.
     *
     * @param registrationName Name of the event that is used as key in the database.
     */

    public void deRegistration(String registrationName) {

        ObjectDeletionNotificationXml dNotificationXml =
                new ObjectDeletionNotificationXml(ownKeyName, getEventNumberAsString(),
                        InternalDateAndTime.valueOf(NETCONFTIME_CONVERTER.getTimeStamp()), registrationName);

        // Write first to prevent missing entries
        databaseService.writeEventLog(dNotificationXml);
        webSocketService.sendViaWebsockets(registrationName, dNotificationXml);

    }

    /**
     * Mountpoint state changed .. from connected -> connecting or unable-to-connect or vis-e-versa.
     *
     * @param registrationName Name of the event that is used as key in the database.
     */
    public void updateRegistration(String registrationName, String attribute, String attributeNewValue) {
        AttributeValueChangedNotificationXml notificationXml = new AttributeValueChangedNotificationXml(ownKeyName,
                getEventNumberAsString(), InternalDateAndTime.valueOf(NETCONFTIME_CONVERTER.getTimeStamp()),
                registrationName, attribute, attributeNewValue);
        databaseService.writeEventLog(notificationXml);
        webSocketService.sendViaWebsockets(registrationName, notificationXml);

    }

    /**
     * At a mountpoint a problem situation is indicated
     *
     * @param registrationName indicating object within SDN controller, normally the mountpointName
     * @param problemName that changed
     * @param problemSeverity of the problem according to NETCONF/YANG
     */

    public void onProblemNotification(String registrationName, String problemName, InternalSeverity problemSeverity) {
        LOG.debug("Got event of type :: {} or {} or {}", ProblemNotification.class.getSimpleName(),
                org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev180907.ProblemNotification.class
                        .getSimpleName(),
                org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.ProblemNotification.class
                        .getSimpleName());
        // notification

        ProblemNotificationXml notificationXml =
                new ProblemNotificationXml(ownKeyName, registrationName, problemName, problemSeverity,
                        // popEvntNumberAsString(), InternalDateAndTime.TESTPATTERN );
                        getEventNumberAsString(), InternalDateAndTime.valueOf(NETCONFTIME_CONVERTER.getTimeStamp()));

        databaseService.writeFaultLog(notificationXml);
        databaseService.updateFaultCurrent(notificationXml);

        if (!maintenanceService.isONFObjectInMaintenance(registrationName, notificationXml.getObjectId(),
                notificationXml.getProblem())) {
            dcaeProvider.sendProblemNotification(ownKeyName, notificationXml);
            if (aotsMProvider != null) {
                aotsMProvider.sendProblemNotification(ownKeyName, notificationXml, false);// not a nealarm, its a
            }
        } // sdncontroller alarm
        else {
            LOG.debug("Notification will not be sent to external services. Device " + registrationName
                    + " is in maintenance mode");
        }

        webSocketService.sendViaWebsockets(registrationName, notificationXml);
    }


    /*---------------------------------------------
     * Handling of ODL Controller events
     */

    /**
     * Called on exit to remove everything for a node from the current list.
     *
     * @param nodeName to remove all problems for
     * @return Number of deleted objects
     */
    public int removeAllCurrentProblemsOfNode(String nodeName) {
        return databaseService.clearFaultsCurrentOfNodeWithObjectId(ownKeyName, nodeName);
    }

    /*---------------------------------------------------------------
     * Get/Set
     */

    /**
     * @return the ownKeyName
     */
    public String getOwnKeyName() {
        return ownKeyName;
    }

    /*---------------------------------------------------------------
     * Private
     */

    private String getEventNumberAsString() {
        return String.valueOf(popEvntNumber());
    }

    private int popEvntNumber() {
        return eventNumber++;
    }
}
