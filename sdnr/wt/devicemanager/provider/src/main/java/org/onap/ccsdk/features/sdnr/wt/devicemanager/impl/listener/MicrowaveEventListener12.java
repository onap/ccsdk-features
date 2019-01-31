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

import java.util.List;

import javax.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.InternalSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.ONFCoreNetworkElement12Equipment;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.ONFCoreNetworkElementCallback;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.wrapperc.OnfMicrowaveModelNotification;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.toggleAlarmFilter.NotificationDelayFilter;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.toggleAlarmFilter.NotificationDelayService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.toggleAlarmFilter.NotificationDelayedListener;
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
 * Important: Websocket notification must be the last action.
 * @author herbert
 *
 */ //OnfMicrowaveModelNotification  //
public class MicrowaveEventListener12 implements OnfMicrowaveModelNotification, NotificationDelayedListener<ProblemNotificationXml> {

    private static final Logger LOG = LoggerFactory.getLogger(MicrowaveEventListener12.class);

    private final String nodeName;
    private final WebSocketServiceClient webSocketService;
    //private final WebsocketmanagerService websocketmanagerService;
    //private final XmlMapper xmlMapper;
    private final HtDatabaseEventsService databaseService;
    private final ProviderClient dcaeProvider;
    private final @Nullable ProviderClient aotsmClient;

    private final MaintenanceService maintenanceService;

    private final NotificationDelayFilter<ProblemNotificationXml> delayFilter;
    private final ONFCoreNetworkElementCallback ne;

    public MicrowaveEventListener12(String nodeName, WebSocketServiceClient webSocketService,
            HtDatabaseEventsService databaseService, ProviderClient dcaeProvider,@Nullable ProviderClient aotsmClient,
            MaintenanceService maintenanceService2,NotificationDelayService<ProblemNotificationXml> notificationDelayService,
            ONFCoreNetworkElementCallback ne) {
        super();
        this.nodeName = nodeName;
        //this.websocketmanagerService = websocketmanagerService;
        //this.xmlMapper = xmlMapper;
        this.webSocketService = webSocketService;
        this.databaseService = databaseService;
        this.dcaeProvider = dcaeProvider;
        this.aotsmClient = aotsmClient;
        this.maintenanceService=maintenanceService2;
        this.delayFilter=notificationDelayService.getInstance(nodeName, this);//12(nodeName,this);
        this.ne = ne;
    }


    @Override
    public void onAttributeValueChangedNotification(AttributeValueChangedNotificationXml notificationXml) {

        ne.notificationFromNeListener(notificationXml);

        databaseService.writeEventLog(notificationXml);

        webSocketService.sendViaWebsockets(nodeName, notificationXml);
    }


    @Override
    public void onObjectCreationNotification(ObjectCreationNotificationXml notificationXml) {

        databaseService.writeEventLog(notificationXml);

        webSocketService.sendViaWebsockets(nodeName, notificationXml);

    }

    @Override
    public void onObjectDeletionNotification(ObjectDeletionNotificationXml notificationXml) {

        databaseService.writeEventLog(notificationXml);

        webSocketService.sendViaWebsockets(nodeName, notificationXml);
    }

    @Override
    public void onProblemNotification(ProblemNotificationXml notificationXml) {

        databaseService.writeFaultLog(notificationXml);
        databaseService.updateFaultCurrent(notificationXml);

        //ToggleAlarmFilter functionality
        if(NotificationDelayFilter.isEnabled())
        {
            if(notificationXml.getSeverity() == InternalSeverity.NonAlarmed) {
                delayFilter.clearAlarmNotification(notificationXml.getProblem(), notificationXml);
            } else {
                delayFilter.pushAlarmNotification(notificationXml.getProblem(), notificationXml);
            }
        }
        else
        {
             this.pushAlarmIfNotInMaintenance(notificationXml);
        }
        //end of ToggleAlarmFilter

        this.webSocketService.sendViaWebsockets(nodeName, notificationXml);

    }

    @Override
    public void onNotificationDelay(ProblemNotificationXml notificationXml) {

        LOG.debug("Got delayed event of type :: {}", ProblemNotification.class.getSimpleName());
        this.pushAlarmIfNotInMaintenance(notificationXml);

    }
    private void pushAlarmIfNotInMaintenance(ProblemNotificationXml notificationXml)
    {
         if(!this.maintenanceService.isONFObjectInMaintenance(nodeName, notificationXml.getObjectId(), notificationXml.getProblem()))
         {
             this.dcaeProvider.sendProblemNotification(nodeName, notificationXml);
             if(this.aotsmClient!=null) {
                this.aotsmClient.sendProblemNotification(nodeName, notificationXml);
            }
         }
         else
         {
             LOG.debug("Notification will not be sent to external services. Device "+this.nodeName+" is in maintenance mode");
         }
    }
    private void initCurrentProblem(ProblemNotificationXml notificationXml) {
        databaseService.updateFaultCurrent(notificationXml);
        //to prevent push alarms on reconnect
        //=> only pushed alarms are forwared to dcae
        //dcaeProvider.sendProblemNotification(nodeName, notificationXml);
        if(aotsmClient!=null) {
            aotsmClient.sendProblemNotification(this.nodeName, notificationXml);
        }
    }

    /**
     * Called to initialize with the current status and notify the clients
     * @param notificationXmlList List with problems
     */
    public void initCurrentProblemStatus(List<ProblemNotificationXml> notificationXmlList) {

        for (ProblemNotificationXml notificationXml : notificationXmlList) {
            initCurrentProblem(notificationXml);
        }

    }

    /**
     * Called on exit to remove everything from the current list.
     * @return Number of deleted objects
     */
    public int removeAllCurrentProblemsOfNode() {
        int deleted = databaseService.clearFaultsCurrentOfNode(nodeName);
        return deleted;
    }

    /**
     * Called on exit to remove for one Object-Pac from the current list.
     * @param objectId uuid of the interface-pac or equipment-pac
     * @return Number of deleted objects
     */
    public int removeObjectsCurrentProblemsOfNode(String objectId) {
        int deleted = databaseService.clearFaultsCurrentOfNodeWithObjectId(nodeName, objectId);
        return deleted;
    }

    /**
     * Write equipment data to database
     * @param equipment to write
     */
    public void writeEquipment(ONFCoreNetworkElement12Equipment equipment) {
        databaseService.writeInventory(equipment);
    }

 }
