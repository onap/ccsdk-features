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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.handler;

import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.ONFCoreNetworkElement12Equipment;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.ONFCoreNetworkElementCallback;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.wrapperc.OnfMicrowaveModelNotification;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeForwarderInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.AttributeValueChangedNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ObjectCreationNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ObjectDeletionNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ProblemNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.toggleAlarmFilter.NotificationDelayFilter;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.toggleAlarmFilter.NotificationDelayService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.toggleAlarmFilter.NotificationDelayedListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Important: Websocket notification must be the last action.
 * At the beginning intended to handle notifications of type <code>OnfMicrowaveModelNotification</code>.
 * Today an abstract class for processing notifications independent of model.
 *
 * @author herbert
 */
public class NetconfEventListenerHandler12 implements OnfMicrowaveModelNotification, NotificationDelayedListener<ProblemNotificationXml> {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfEventListenerHandler12.class);

    private final String nodeName;
    private final WebSocketServiceClientInternal webSocketService;
    //private final WebsocketmanagerService websocketmanagerService;
    //private final XmlMapper xmlMapper;
    private final DataProvider databaseService;
    private final DcaeForwarderInternal dcaeForwarder;

    private final NotificationDelayFilter<ProblemNotificationXml> delayFilter;
    private final ONFCoreNetworkElementCallback ne;

    public NetconfEventListenerHandler12(String nodeName, WebSocketServiceClientInternal webSocketService,
            DataProvider databaseService, DcaeForwarderInternal aotsDcaeForwarder,
            NotificationDelayService<ProblemNotificationXml> notificationDelayService,
            ONFCoreNetworkElementCallback ne) {
        super();
        this.nodeName = nodeName;
        //this.websocketmanagerService = websocketmanagerService;
        //this.xmlMapper = xmlMapper;
        this.webSocketService = webSocketService;
        this.databaseService = databaseService;
        this.dcaeForwarder = aotsDcaeForwarder;
        this.delayFilter=notificationDelayService.getInstance(nodeName, this);//12(nodeName,this);
        this.ne = ne;
    }


    @Override
    public void onAttributeValueChangedNotification(AttributeValueChangedNotificationXml notificationXml) {

        ne.notificationFromNeListener(notificationXml);

        databaseService.writeEventLog(notificationXml.getEventlogEntity());
        webSocketService.sendViaWebsockets(nodeName, notificationXml);
    }


    @Override
    public void onObjectCreationNotification(ObjectCreationNotificationXml notificationXml) {

        databaseService.writeEventLog(notificationXml.getEventlogEntity());
        webSocketService.sendViaWebsockets(nodeName, notificationXml);

    }

    @Override
    public void onObjectDeletionNotification(ObjectDeletionNotificationXml notificationXml) {

        databaseService.writeEventLog(notificationXml.getEventlogEntity());
        webSocketService.sendViaWebsockets(nodeName, notificationXml);
    }

    @Override
    public void onProblemNotification(ProblemNotificationXml notificationXml) {

        databaseService.writeFaultLog(notificationXml.getFaultlog(SourceType.Netconf));
        databaseService.updateFaultCurrent(notificationXml.getFaultcurrent());

        // ToggleAlarmFilter functionality
        if (delayFilter.processNotification(notificationXml.getSeverity() == InternalSeverity.NonAlarmed, notificationXml.getProblem(), notificationXml))
         {
            dcaeForwarder.sendProblemNotificationUsingMaintenanceFilter(this.nodeName, notificationXml);
        // end of ToggleAlarmFilter
        }

        this.webSocketService.sendViaWebsockets(nodeName, notificationXml);

    }

    @Override
    public void onNotificationDelay(ProblemNotificationXml notificationXml) {

        LOG.debug("Got delayed event of type :: {}", ProblemNotificationXml.class.getSimpleName());
        dcaeForwarder.sendProblemNotificationUsingMaintenanceFilter(this.nodeName, notificationXml);

    }
    private void initCurrentProblem(ProblemNotificationXml notificationXml) {
        databaseService.updateFaultCurrent(notificationXml.getFaultcurrent());
        dcaeForwarder.sendProblemNotification(this.nodeName, notificationXml);
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
//        List<ExtendedEquipment> equipmentList = equipment.getEquipmentList();
//        for (ExtendedEquipment card : equipmentList) {
//            databaseService.writeInventory(card.getCreateInventoryInput());
//        }
        equipment.getEquipmentList().forEach(card -> databaseService.writeInventory(card.getCreateInventoryInput()) );

    }

 }
