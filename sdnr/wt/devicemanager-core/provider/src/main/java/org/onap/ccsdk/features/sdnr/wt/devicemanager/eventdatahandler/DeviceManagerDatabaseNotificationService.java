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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.HtAssert;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeForwarderInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ProblemNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.maintenance.impl.MaintenanceServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.EquipmentService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.toggleAlarmFilter.DevicemanagerNotificationDelayService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.toggleAlarmFilter.NotificationDelayFilter;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.toggleAlarmFilter.NotificationDelayedListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.EquipmentData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.EventlogNotificationBuilder;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Faultcurrent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.AttributeValueChangedNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.AttributeValueChangedNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ObjectCreationNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ObjectCreationNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ObjectDeletionNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ObjectDeletionNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ProblemNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ProblemNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceManagerDatabaseNotificationService implements NotificationService, EquipmentService, FaultService,
        NotificationDelayedListener<ProblemNotificationXml> {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerDatabaseNotificationService.class);

    private final @NonNull DataProvider databaseService;
    private final @NonNull WebSocketServiceClientInternal webSocketService;
    private @NonNull final DevicemanagerNotificationDelayService notificationDelayService;
    private @NonNull final DcaeForwarderInternal aotsDcaeForwarder;

    private final @NonNull MaintenanceServiceImpl maintenanceService;

    /**
     * @param databaseService to access database
     * @param maintenanceService
     * @param webSocketService to send notifications
     * @param notificationDelayService filter to prevent toggle alarms
     * @param aotsDcaeForwarder
     */
    public DeviceManagerDatabaseNotificationService(@NonNull DataProvider databaseService,
            @NonNull MaintenanceServiceImpl maintenanceService,
            @NonNull WebSocketServiceClientInternal webSocketService,
            @NonNull DevicemanagerNotificationDelayService notificationDelayService,
            @NonNull DcaeForwarderInternal aotsDcaeForwarder) {
        super();
        HtAssert.nonnull(databaseService, maintenanceService, webSocketService, notificationDelayService);

        this.databaseService = databaseService;
        this.maintenanceService = maintenanceService;
        this.webSocketService = webSocketService;
        this.notificationDelayService = notificationDelayService;
        this.aotsDcaeForwarder = aotsDcaeForwarder;
    }

    @Override
    public void eventNotification(@NonNull EventlogEntity eventNotification) {
        String nodeId = eventNotification.getNodeId();
        if (nodeId == null) {
            nodeId = "EmptyNodeId";
        }
        databaseService.writeEventLog(eventNotification);
        AttributeValueChangedNotification notification = new AttributeValueChangedNotificationBuilder()
                .setAttributeName(eventNotification.getAttributeName()).setCounter(eventNotification.getCounter())
                .setNewValue(eventNotification.getNewValue()).setObjectIdRef(eventNotification.getObjectId())
                .setTimeStamp(eventNotification.getTimestamp()).build();
        this.webSocketService.sendViaWebsockets(new NodeId(nodeId), notification, AttributeValueChangedNotification.QNAME,
                eventNotification.getTimestamp());
    }

    @Override
    public void changeNotification(NodeId nodeId, @Nullable Integer counter, @Nullable DateAndTime timeStamp,
            @Nullable String objectId, @Nullable String attributeName, @Nullable String newValue) {
        EventlogEntity eventlogEntity =
                new EventlogNotificationBuilder(nodeId, counter, timeStamp, objectId, attributeName, newValue).build();
        this.eventNotification(eventlogEntity);
    }

    @Override
    public void creationNotification(NodeId nodeId, @Nullable Integer counter, @Nullable DateAndTime timeStamp,
            @Nullable String objectId) {
        EventlogEntity eventlogEntity =
                new EventlogNotificationBuilder(nodeId, counter, timeStamp, objectId, "creation", null).build();
        databaseService.writeEventLog(eventlogEntity);
        ObjectCreationNotification notification = new ObjectCreationNotificationBuilder().setCounter(counter)
                .setObjectIdRef(objectId).setTimeStamp(eventlogEntity.getTimestamp()).build();
        this.webSocketService.sendViaWebsockets(nodeId, notification, ObjectCreationNotification.QNAME,
                eventlogEntity.getTimestamp());
    }


    @Override
    public void deletionNotification(NodeId nodeId, @Nullable Integer counter, @Nullable DateAndTime timeStamp,
            @Nullable String objectId) {
        EventlogEntity eventlogEntity =
                new EventlogNotificationBuilder(nodeId, counter, timeStamp, objectId, "deletion", null).build();
        databaseService.writeEventLog(eventlogEntity);
        ObjectDeletionNotification notification = new ObjectDeletionNotificationBuilder().setCounter(counter)
                .setObjectIdRef(objectId).setTimeStamp(eventlogEntity.getTimestamp()).build();
        this.webSocketService.sendViaWebsockets(nodeId, notification, ObjectDeletionNotification.QNAME,
                eventlogEntity.getTimestamp());
    }

    @Override
    public void writeEquipment(NodeId nodeId, @NonNull EquipmentData equipment) {
        HtAssert.nonnull(equipment);
        List<Inventory> list = equipment.getList();
        HtAssert.nonnull(list);
        databaseService.writeInventory(nodeId.getValue(), list);

    }

    @Override
    public void faultNotification(@NonNull FaultlogEntity faultNotification) {
        databaseService.writeFaultLog(faultNotification);
        databaseService.updateFaultCurrent(getFaultCurrent(faultNotification));

        ProblemNotificationXml notificationXml = new ProblemNotificationXml(faultNotification);
        String nodeName = faultNotification.getNodeId();
        if (NotificationDelayFilter.isEnabled()) {
            if (notificationXml.getSeverity() == InternalSeverity.NonAlarmed) {
                this.notificationDelayService.getInstance(nodeName, this).clearAlarmNotification(notificationXml);
            } else {
                this.notificationDelayService.getInstance(nodeName, this).pushAlarmNotification(notificationXml);
            }
        } else {
            this.pushAlarmIfNotInMaintenance(nodeName, notificationXml);
        }
        // Send
        ProblemNotification notification = new ProblemNotificationBuilder().setCounter(faultNotification.getCounter())
                .setObjectIdRef(faultNotification.getObjectId()).setTimeStamp(faultNotification.getTimestamp())
                .setProblem(faultNotification.getProblem())
                .setSeverity(InternalSeverity.toYang(faultNotification.getSeverity())).build();
        this.webSocketService.sendViaWebsockets(new NodeId(faultNotification.getNodeId()), notification,
                ProblemNotification.QNAME, faultNotification.getTimestamp());
    }

    private void pushAlarmIfNotInMaintenance(String nodeName, ProblemNotificationXml notificationXml) {
        if (!this.maintenanceService.isONFObjectInMaintenance(new NodeId(nodeName), notificationXml.getObjectId(),
                notificationXml.getProblem())) {
            this.aotsDcaeForwarder.sendProblemNotification(new NodeId(nodeName), notificationXml);
        } else {
            LOG.debug("Notification will not be sent to external services. Device " + nodeName
                    + " is in maintenance mode");
        }
    }

    @Override
    public int removeAllCurrentProblemsOfNode(@NonNull NodeId nodeId) {
        int deleted = databaseService.clearFaultsCurrentOfNode(nodeId.getValue());
        return deleted;
    }

    @Override
    public void initCurrentProblemStatus(@NonNull NodeId nodeId, FaultData resultList) {
        resultList.getProblemList().forEach(problem -> {
            FaultcurrentBuilder bFaultcurrent = new FaultcurrentBuilder();
            bFaultcurrent.fieldsFrom(problem);
            databaseService.updateFaultCurrent(bFaultcurrent.build());
        });
    }

    @Override
    public int removeObjectsCurrentProblemsOfNode(@NonNull NodeId nodeId, String objectId) {
        int deleted = databaseService.clearFaultsCurrentOfNodeWithObjectId(nodeId.getValue(), objectId);
        return deleted;
    }

    private Faultcurrent getFaultCurrent(@NonNull FaultlogEntity problem) {
        FaultcurrentBuilder bFaultcurrent = new FaultcurrentBuilder();
        bFaultcurrent.fieldsFrom(problem);
        return bFaultcurrent.build();

    }

    @Override
    public void onNotificationDelay(String nodeName, ProblemNotificationXml notification) {
        LOG.debug("Got delayed event of type :: {}", ProblemNotificationXml.class.getSimpleName());
        this.pushAlarmIfNotInMaintenance(nodeName, notification);
    }

}
