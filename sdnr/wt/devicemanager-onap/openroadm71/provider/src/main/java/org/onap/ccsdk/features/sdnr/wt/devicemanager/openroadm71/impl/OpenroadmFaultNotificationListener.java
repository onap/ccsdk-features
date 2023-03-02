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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm71.impl;


import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev200529.AlarmNotification;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev200529.OrgOpenroadmAlarmListener;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev200529.alarm.ProbableCause;
import org.opendaylight.yang.gen.v1.http.org.openroadm.probablecause.rev200529.ProbableCauseEnum;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.Resource;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.CircuitPack;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.Connection;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.Degree;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.Device;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.Interface;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.InternalLink;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.LineAmplifier;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.OduSncpPg;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.Other;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.PhysicalLink;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.Port;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.Service;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.Shelf;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.Srg;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.TempService;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.VersionedService;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.resource.resource.Xponder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shabnam Sultana
 *
 *         Listener for Open roadm device specific alarm notifications
 **/
public class OpenroadmFaultNotificationListener implements OrgOpenroadmAlarmListener {
    private static final Logger log = LoggerFactory.getLogger(OpenroadmFaultNotificationListener.class);

    private final @NonNull FaultService faultEventListener;
    private @NonNull WebsocketManagerService notificationService;
    private Integer count = 1;

    private NetconfBindingAccessor netconfAccessor;


    public OpenroadmFaultNotificationListener(NetconfBindingAccessor accessor, DeviceManagerServiceProvider serviceProvider) {
        this.netconfAccessor = accessor;
        this.faultEventListener = serviceProvider.getFaultService();
        this.notificationService = serviceProvider.getWebsocketService();

    }

    @Override
    public void onAlarmNotification(AlarmNotification notification) {
        log.debug("AlarmNotification is {} \t {}", notification.getId(), notification.getAdditionalDetail());
        String affectedResourceName = getAffectedResourceName(notification.getResource().getResource().getResource());
        String probableCauseName = getProbableCauseName(notification.getProbableCause());

        if (notification.getId() == null) {
            log.warn("Alarm ID is null. Not logging alarm information to the DB. Alarm ID should not be null. Please fix the same in the Device");
            return;
        }
        FaultlogEntity faultAlarm = new FaultlogBuilder().setObjectId(affectedResourceName)
                .setProblem(probableCauseName).setSourceType(SourceType.Netconf)
                .setTimestamp(notification.getRaiseTime()).setId(notification.getId()).setNodeId(netconfAccessor.getNodeId().getValue())
                .setSeverity(InitialDeviceAlarmReader.checkSeverityValue(notification.getSeverity())).setCounter(count)
                .build();

        this.faultEventListener.faultNotification(faultAlarm);
        this.notificationService.sendNotification(notification,new NodeId(netconfAccessor.getNodeId().getValue()), AlarmNotification.QNAME,
                notification.getRaiseTime());
        count++;
        log.debug("Notification is written into the database {}", faultAlarm.getObjectId());

    }

    public String getAffectedResourceName(Resource affectedResource) {
        if (affectedResource instanceof CircuitPack) {
            return ((CircuitPack)affectedResource).getCircuitPackName();
        } else if (affectedResource instanceof Port) {
            return ((Port)affectedResource).getPort().getPortName();
        } else if (affectedResource instanceof Connection) {
            return ((Connection)affectedResource).getConnectionName();
        } else if (affectedResource instanceof PhysicalLink) {
            return ((PhysicalLink)affectedResource).getPhysicalLinkName();
        } else if (affectedResource instanceof InternalLink) {
            return ((InternalLink)affectedResource).getInternalLinkName();
        } else if (affectedResource instanceof Shelf) {
            return ((Shelf)affectedResource).getShelfName();
        } else if (affectedResource instanceof Srg) {
            return "SRG #- " + ((Srg)affectedResource).getSrgNumber().toString();
        } else if (affectedResource instanceof Degree) {
            return "Degree - " + ((Degree)affectedResource).getDegreeNumber().toString();
        } else if (affectedResource instanceof Service) {
            return ((Service)affectedResource).getServiceName();
        } else if (affectedResource instanceof Interface) {
            return ((Interface)affectedResource).getInterfaceName();
        } else if (affectedResource instanceof OduSncpPg) {
            return ((OduSncpPg)affectedResource).getOduSncpPgName();
        } else if (affectedResource instanceof Device) {
            return ((Device)affectedResource).getNodeId().getValue();
        } else if (affectedResource instanceof LineAmplifier) {
            return "LineAmplifier # - " + ((LineAmplifier)affectedResource).getAmpNumber().toString();
        } else if (affectedResource instanceof Xponder) {
            return "Xponder # - "+ ((Xponder)affectedResource).getXpdrNumber().toString();
        } else if (affectedResource instanceof Other) {
            return ((Other)affectedResource).getOtherResourceId();
        } else if (affectedResource instanceof VersionedService) {
            return ((VersionedService)affectedResource).getVersionedServiceName();
        } else if (affectedResource instanceof TempService) {
            return ((TempService)affectedResource).getCommonId();
        }

        log.warn("Unknown Resource {} received from Notification", affectedResource.getClass().getSimpleName());
        return "Unknown Resource";
    }

    public String getProbableCauseName(ProbableCause probableCause) {
        if (probableCause != null) {
            ProbableCauseEnum pce = probableCause.getCause();
            if (pce != null) {
                return pce.getName();
            }
            log.warn("ProbableCauseEnum is NULL");
            return "Unknown Cause";
        }
        log.warn("ProbableCause is NULL");
        return "Unknown Cause";
    }

}
