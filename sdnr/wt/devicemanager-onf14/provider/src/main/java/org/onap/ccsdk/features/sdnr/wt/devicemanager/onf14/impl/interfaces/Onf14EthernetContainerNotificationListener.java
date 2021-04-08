/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.interfaces;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.AttributeValueChangedNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.EthernetContainer20Listener;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.ObjectCreationNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.ObjectDeletionNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.ProblemNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.SEVERITYTYPE;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.SEVERITYTYPECRITICAL;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.SEVERITYTYPEMAJOR;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.SEVERITYTYPEMINOR;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.SEVERITYTYPENONALARMED;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.SEVERITYTYPEWARNING;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Onf14EthernetContainerNotificationListener implements EthernetContainer20Listener {

    private static final Logger log = LoggerFactory.getLogger(Onf14EthernetContainerNotificationListener.class);

    private final NetconfAccessor netconfAccessor;
    private final DeviceManagerServiceProvider serviceProvider;

    private static final Map<Class<? extends SEVERITYTYPE>, SeverityType> severityMap = initSeverityMap();

    public Onf14EthernetContainerNotificationListener(NetconfAccessor netconfAccessor,
            DeviceManagerServiceProvider serviceProvider) {
        this.netconfAccessor = netconfAccessor;
        this.serviceProvider = serviceProvider;
    }

    private static Map<Class<? extends SEVERITYTYPE>, SeverityType> initSeverityMap() {
        Map<Class<? extends SEVERITYTYPE>, SeverityType> map = new HashMap<>();
        map.put(SEVERITYTYPECRITICAL.class, SeverityType.Critical);
        map.put(SEVERITYTYPEMAJOR.class, SeverityType.Major);
        map.put(SEVERITYTYPEMINOR.class, SeverityType.Minor);
        map.put(SEVERITYTYPEWARNING.class, SeverityType.Warning);
        map.put(SEVERITYTYPENONALARMED.class, SeverityType.NonAlarmed);
        return map;
    }

    @Override
    public void onObjectDeletionNotification(ObjectDeletionNotification notification) {
        log.debug("Got event of type :: {}", ObjectDeletionNotification.class.getSimpleName());

        EventlogBuilder eventlogBuilder = new EventlogBuilder();
        eventlogBuilder.setNodeId(netconfAccessor.getNodeId().getValue()).setAttributeName("")
                .setCounter(notification.getCounter().intValue()).setNewValue("deleted")
                .setObjectId(notification.getObjectIdRef().getValue()).setSourceType(SourceType.Netconf)
                .setTimestamp(notification.getTimestamp());
        serviceProvider.getDataProvider().writeEventLog(eventlogBuilder.build());
        serviceProvider.getNotificationService().deletionNotification(netconfAccessor.getNodeId(),
                notification.getCounter().intValue(), notification.getTimestamp(),
                notification.getObjectIdRef().getValue());

        log.debug("onObjectDeletionNotification log entry written");
    }

    @Override
    public void onProblemNotification(ProblemNotification notification) {
        log.debug("Got event of type :: {}", ProblemNotification.class.getSimpleName());
        FaultlogEntity faultAlarm = new FaultlogBuilder().setObjectId(notification.getObjectIdRef().getValue())
                .setProblem(notification.getProblem()).setTimestamp(notification.getTimestamp())
                .setNodeId(this.netconfAccessor.getNodeId().getValue()).setSourceType(SourceType.Netconf)
                .setSeverity(mapSeverity(notification.getSeverity())).setCounter(notification.getCounter().intValue())
                .build();
        serviceProvider.getFaultService().faultNotification(faultAlarm);
        serviceProvider.getWebsocketService().sendNotification(notification, netconfAccessor.getNodeId().getValue(),
                ProblemNotification.QNAME, notification.getTimestamp());

    }

    private SeverityType mapSeverity(@Nullable Class<? extends SEVERITYTYPE> severity) {
        return severityMap.getOrDefault(severity, SeverityType.NonAlarmed);
    }

    @Override
    public void onAttributeValueChangedNotification(AttributeValueChangedNotification notification) {
        log.debug("Got event of type :: {}", AttributeValueChangedNotification.class.getSimpleName());

        EventlogBuilder eventlogBuilder = new EventlogBuilder();
        eventlogBuilder.setNodeId(netconfAccessor.getNodeId().getValue())
                .setAttributeName(notification.getAttributeName()).setCounter(notification.getCounter().intValue())
                .setNewValue(notification.getNewValue()).setObjectId(notification.getObjectIdRef().getValue())
                .setSourceType(SourceType.Netconf).setTimestamp(notification.getTimestamp());
        serviceProvider.getDataProvider().writeEventLog(eventlogBuilder.build());
        serviceProvider.getWebsocketService().sendNotification(notification, netconfAccessor.getNodeId().getValue(),
                AttributeValueChangedNotification.QNAME, notification.getTimestamp());

        log.debug("onAttributeValueChangedNotification log entry written");
    }

    @Override
    public void onObjectCreationNotification(ObjectCreationNotification notification) {
        log.debug("Got event of type :: {}", ObjectCreationNotification.class.getSimpleName());

        EventlogBuilder eventlogBuilder = new EventlogBuilder();
        eventlogBuilder.setNodeId(netconfAccessor.getNodeId().getValue()).setAttributeName(notification.getObjectType())
                .setCounter(notification.getCounter().intValue()).setNewValue("created")
                .setObjectId(notification.getObjectIdRef().getValue()).setSourceType(SourceType.Netconf)
                .setTimestamp(notification.getTimestamp());
        serviceProvider.getDataProvider().writeEventLog(eventlogBuilder.build());
        serviceProvider.getWebsocketService().sendNotification(notification, netconfAccessor.getNodeId().getValue(),
                ObjectCreationNotification.QNAME, notification.getTimestamp());

        log.debug("onObjectCreationNotification log entry written");
    }


}
