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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.yangspecs;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Alarms10 extends YangModule {

    private static final Logger LOG = LoggerFactory.getLogger(Alarms10.class);

    private static final String NAMESPACE = "urn:onf:yang:alarms-1-0";
    private static final List<QNameModule> MODULES =
            Arrays.asList(QNameModule.create(XMLNamespace.of(NAMESPACE), Revision.of("2022-03-02")),
                    QNameModule.create(XMLNamespace.of(NAMESPACE), Revision.of("2022-07-29")));

    private final QName ALARM_PAC;
    private final QName CURRENT_ALARMS;
    private final QName CURRENT_ALARM_LIST;
    private final QName CURRENT_ALARM_IDENTIFIER;
    private final QName ALARM_TYPE_ID;
    private final QName ALARM_TYPE_QUALIFIER;
    private final QName RESOURCE;
    private final QName ALARM_SEVERITY;
    private final QName ALARM_TIMESTAMP;
    private final QName ALARM_EVENT_SEQUENCE_NUMBER;
    private final QName PROBLEM_SEVERITY;
    private final QName ALARM_AVC_NOTIFICATION;
    private final QName ALARM_EVENT_NOTIFICATION;

    private final CoreModel14 coreModel14;

    private Alarms10(NetconfDomAccessor netconfDomAccessor, QNameModule module, CoreModel14 coreModel14) {
        super(netconfDomAccessor, module);
        this.coreModel14 = coreModel14;

        ALARM_PAC = QName.create(module, "alarm-pac");
        CURRENT_ALARMS = QName.create(module, "current-alarms");
        CURRENT_ALARM_LIST = QName.create(module, "current-alarm-list");
        CURRENT_ALARM_IDENTIFIER = QName.create(module, "current-alarm-identifier");
        ALARM_TYPE_ID = QName.create(module, "alarm-type-id");
        ALARM_TYPE_QUALIFIER = QName.create(module, "alarm-type-qualifier");
        RESOURCE = QName.create(module, "resource");
        ALARM_SEVERITY = QName.create(module, "alarm-severity");
        ALARM_TIMESTAMP = QName.create(module, "timestamp");
        ALARM_EVENT_SEQUENCE_NUMBER = QName.create(module, "alarm-event-sequence-number");
        PROBLEM_SEVERITY = QName.create(module, "problem-severity");
        ALARM_AVC_NOTIFICATION = QName.create(module, "attribute-value-changed-notification");
        ALARM_EVENT_NOTIFICATION = QName.create(module, "alarm-event-notification");
    }

    public QNameModule getModule() {
        return module;
    }

    public FaultlogEntity getFaultlogEntity(ContainerNode cn) {
        return new FaultlogBuilder().setNodeId(getNodeId().getValue()).setSourceType(SourceType.Netconf)
                .setObjectId(Onf14DMDOMUtility.getLeafValueUuid(cn, RESOURCE))
                .setProblem(Onf14DMDOMUtility.getLeafValue(cn, ALARM_TYPE_QUALIFIER))
                .setTimestamp(Onf14DMDOMUtility.getLeafValueDateAndTime(cn, ALARM_TIMESTAMP))
                .setSeverity(Onf14DMDOMUtility.getLeafValueInternalSeverity(cn, PROBLEM_SEVERITY))
                .setCounter(Onf14DMDOMUtility.getLeafValueInt(cn, ALARM_EVENT_SEQUENCE_NUMBER)).build();
    }

    public boolean isAlarmEventNotification(DOMNotification domNotification) {
        return domNotification.getType().equals(Absolute.of(ALARM_EVENT_NOTIFICATION));
    }

    public void doRegisterNotificationListener(DOMNotificationListener alarmNotifListener) {
        QName[] alarmNotifications = {ALARM_AVC_NOTIFICATION, ALARM_EVENT_NOTIFICATION};
        netconfDomAccessor.doRegisterNotificationListener(alarmNotifListener, alarmNotifications);
    }

    public void sendNotification(@NonNull WebsocketManagerService websocketService, DOMNotification domNotification,
            ContainerNode cn) {
        websocketService.sendNotification(domNotification, getNodeId(), ALARM_TYPE_QUALIFIER,
                Onf14DMDOMUtility.getLeafValueDateAndTime(cn, ALARM_TIMESTAMP));
    }

    public FaultData getCurrentAlarms() {

        YangInstanceIdentifier alarmsPacIID =
                YangInstanceIdentifier.builder().node(coreModel14.getControlConstructQName()).build();
        InstanceIdentifierBuilder alarmsContainerIID = YangInstanceIdentifier.builder(alarmsPacIID).node(ALARM_PAC);

        //        @NonNull
        //        AugmentationIdentifier alarmsContainerIID =
        //                YangInstanceIdentifier.AugmentationIdentifier.create(Sets.newHashSet(ALARM_PAC));
        //
        //        InstanceIdentifierBuilder augmentedAlarmsIID =
        //                YangInstanceIdentifier.builder(alarmsPacIID).node(alarmsContainerIID);
        //
        //        // reading all the alarms
        Optional<NormalizedNode> alarms =
                this.getNetconfDomAccessor().readDataNode(LogicalDatastoreType.OPERATIONAL, alarmsContainerIID.build());

        FaultData resultList = new FaultData();
        if (alarms.isPresent()) {
            ContainerNode alarmsDataNode = (ContainerNode) alarms.get();
            ContainerNode alarmsContainer = (ContainerNode) alarmsDataNode.childByArg(new NodeIdentifier(ALARM_PAC));
            ContainerNode currentAlarmsContainer =
                    (ContainerNode) alarmsContainer.childByArg(new NodeIdentifier(CURRENT_ALARMS));
            MapNode currentAlarmsList =
                    (MapNode) currentAlarmsContainer.childByArg(new NodeIdentifier(CURRENT_ALARM_LIST));
            if (currentAlarmsList != null) {
                Collection<MapEntryNode> currentAlarmsCollection = currentAlarmsList.body();
                for (MapEntryNode currentAlarm : currentAlarmsCollection) {
                    resultList.add(getNodeId(),
                            Onf14DMDOMUtility.getLeafValueInt(currentAlarm, CURRENT_ALARM_IDENTIFIER),
                            Onf14DMDOMUtility.getLeafValueDateAndTime(currentAlarm, ALARM_TIMESTAMP),
                            Onf14DMDOMUtility.getLeafValueUuid(currentAlarm, RESOURCE),
                            Onf14DMDOMUtility.getLeafValue(currentAlarm, ALARM_TYPE_QUALIFIER),
                            Onf14DMDOMUtility.getLeafValueInternalSeverity(currentAlarm, ALARM_SEVERITY));
                }
            } else {
                LOG.debug("DBRead empty CurrentProblemList");
            }
        }
        return resultList;

    }

    public boolean isSupported(Capabilities capabilites) {
        return netconfDomAccessor.getCapabilites().isSupportingNamespace(NAMESPACE);
    }

    private NodeId getNodeId() {
        return netconfDomAccessor.getNodeId();
    }

    /**
     * Get specific instance, depending on capabilities
     *
     * @param capabilities
     * @return
     */
    public static Optional<Alarms10> getModule(NetconfDomAccessor netconfDomAccessor, CoreModel14 coreModel14) {
        Capabilities capabilities = netconfDomAccessor.getCapabilites();
        for (QNameModule module : MODULES) {
            if (capabilities.isSupportingNamespaceAndRevision(module)) {
                return Optional.of(new Alarms10(netconfDomAccessor, module, coreModel14));
            }
        }
        return Optional.empty();
    }
}
