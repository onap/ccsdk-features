/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Update Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=======================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler;

import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeForwarderInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.PushNotifications;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ProblemNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientInternal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.AttributeValueChangedNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.AttributeValueChangedNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ProblemNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ProblemNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushAttributeChangeNotificationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushFaultNotificationInput;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcPushNotificationsHandler implements PushNotifications {

    private static final Logger LOG = LoggerFactory.getLogger(RpcPushNotificationsHandler.class);

    private static final String OWNKEYNAME = "VES";
    private final WebSocketServiceClientInternal webSocketService;
    private final DataProvider databaseService;
    private final DcaeForwarderInternal aotsDcaeForwarder;

    public RpcPushNotificationsHandler(WebSocketServiceClientInternal webSocketService, DataProvider databaseService,
            DcaeForwarderInternal aotsDcaeForwarder) {
        super();
        this.webSocketService = webSocketService;
        this.databaseService = databaseService;
        this.aotsDcaeForwarder = aotsDcaeForwarder;
    }

    @Override
    public void pushAttributeChangeNotification(PushAttributeChangeNotificationInput input) {

        LOG.debug("Got attribute change event {}", input);

        EventlogBuilder enventlogBuilder = new EventlogBuilder();
        enventlogBuilder.setSourceType(SourceType.Ves);
        enventlogBuilder.fieldsFrom(input);
        EventlogEntity eventlogEntity = enventlogBuilder.build();
        databaseService.writeEventLog(eventlogEntity);
        AttributeValueChangedNotification notification =
                new AttributeValueChangedNotificationBuilder().setAttributeName(input.getAttributeName())
                        .setCounter(input.getCounter()).setNewValue(input.getNewValue())
                        .setObjectIdRef(input.getObjectId()).setTimeStamp(input.getTimestamp()).build();
        webSocketService.sendViaWebsockets(new NodeId(input.getNodeId()!=null?input.getNodeId():OWNKEYNAME), notification, AttributeValueChangedNotification.QNAME,
                input.getTimestamp());

    }

    @Override
    public void pushFaultNotification(PushFaultNotificationInput input) {

        LOG.debug("Got fault event {}", input);

        FaultlogBuilder faultlogBuilder = new FaultlogBuilder();
        faultlogBuilder.setSourceType(SourceType.Ves);
        faultlogBuilder.fieldsFrom(input);
        FaultlogEntity faultlogEntity = faultlogBuilder.build();
        databaseService.writeFaultLog(faultlogEntity);

        FaultcurrentBuilder faultcurrentBuilder = new FaultcurrentBuilder();
        faultcurrentBuilder.fieldsFrom(input);
        FaultcurrentEntity faultcurrentEntity = faultcurrentBuilder.build();
        databaseService.updateFaultCurrent(faultcurrentEntity);

        ProblemNotificationXml notificationXml = new ProblemNotificationXml(faultlogEntity);
        ProblemNotification notification = new ProblemNotificationBuilder().setProblem(input.getProblem())
                .setCounter(input.getCounter()).setObjectIdRef(input.getObjectId())
                .setSeverity(InternalSeverity.toYang(input.getSeverity())).setTimeStamp(input.getTimestamp()).build();
        aotsDcaeForwarder.sendProblemNotificationUsingMaintenanceFilter(new NodeId(OWNKEYNAME), notificationXml);
        webSocketService.sendViaWebsockets(new NodeId(input.getNodeId()!=null?input.getNodeId():OWNKEYNAME), notification, ProblemNotification.QNAME, input.getTimestamp());
    }

}
