/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.interfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.notifications.Onf14DomAirInterfaceNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.notifications.Onf14DomAlarmsNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.notifications.Onf14DomEthernetContainerNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.notifications.Onf14DomWireInterfaceNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.qnames.Onf14DevicemanagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.yangspecs.AirInterface20;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.yangspecs.Alarms10;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.yangspecs.CoreModel14;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.yangspecs.EthernetContainer20;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.yangspecs.WireInterface20;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.PerformanceDataLtp;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
* Notifications streams provided by device NTSSim ONF14
* Stream{getName=StreamNameType{_value=nc-notifications}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=hybrid-mw-structure-2-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=vlan-interface-1-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=tdm-container-2-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=ethernet-container-2-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=ietf-yang-library}, isReplaySupport=false, augmentation=[]},
* Stream{getDescription=Default NETCONF stream containing all the Event Notifications., getName=StreamNameType{_value=NETCONF}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=vlan-fd-1-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=wire-interface-2-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=mac-fd-1-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=co-channel-profile-1-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=mac-interface-1-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=ietf-keystore}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=pure-ethernet-structure-2-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=ietf-netconf-notifications}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=mac-fc-1-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=wred-profile-1-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=air-interface-2-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=ip-interface-1-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=qos-profile-1-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=vlan-fc-1-0}, isReplaySupport=true, augmentation=[]},
* Stream{getName=StreamNameType{_value=l-3vpn-profile-1-0}, isReplaySupport=true, augmentation=[]}]
*/

public class Onf14DomInterfacePacManager {

    private static final Logger LOG = LoggerFactory.getLogger(Onf14DomInterfacePacManager.class);

    // private static final YangInstanceIdentifier LTP_IID =
    // YangInstanceIdentifier.builder().node(Onf14DevicemanagerQNames.CORE_MODEL_CONTROL_CONSTRUCT_CONTAINER)
    // .node(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP).build();

    private final NetconfDomAccessor netconfDomAccessor;
    private final @NonNull DeviceManagerServiceProvider serviceProvider;

    // air interface related members
    private final List<TechnologySpecificPacKeys> airInterfaceList = new ArrayList<>();
    @NonNull
    private final Onf14DomAirInterfaceNotificationListener airInterfaceNotificationListener;

    // ethernet container related members
    private final List<TechnologySpecificPacKeys> ethernetContainerList = new ArrayList<>();
    @NonNull
    private final Onf14DomEthernetContainerNotificationListener ethernetContainerNotificationListener;

    // wire interface related members
    private final List<TechnologySpecificPacKeys> wireInterfaceList = new ArrayList<>();
    @NonNull
    private final Onf14DomWireInterfaceNotificationListener wireInterfaceNotificationListener;

    // alarm-1-0 related alarms
    private final Onf14DomAlarmsNotificationListener alarmNotifListener;

    // Services and models
    private final @NonNull FaultService faultService;
    private final @NonNull CoreModel14 coreModel14;

    private final Optional<Alarms10> alarms10;
    private final Optional<AirInterface20> airInterface20;
    private final Optional<EthernetContainer20> ethernetInterface20;
    private final Optional<WireInterface20> wireInterface20;

    private final Onf14Interfaces interfaces;

    public Onf14DomInterfacePacManager(@NonNull NetconfDomAccessor netconfDomAccessor,
            @NonNull DeviceManagerServiceProvider serviceProvider, CoreModel14 coreModel14) {

        this.netconfDomAccessor = Objects.requireNonNull(netconfDomAccessor);
        this.serviceProvider = Objects.requireNonNull(serviceProvider);
        this.faultService = Objects.requireNonNull(serviceProvider.getFaultService());
        this.interfaces = new Onf14Interfaces();

        this.coreModel14 = coreModel14;
        this.alarms10 = Alarms10.getModule(netconfDomAccessor, coreModel14);
        this.airInterface20 = AirInterface20.getModule(netconfDomAccessor, coreModel14);
        this.ethernetInterface20 = EthernetContainer20.getModule(netconfDomAccessor, coreModel14);
        this.wireInterface20 = WireInterface20.getModule(netconfDomAccessor, coreModel14);

        if (alarms10.isPresent()) {
            this.alarmNotifListener =
                    new Onf14DomAlarmsNotificationListener(netconfDomAccessor, serviceProvider, alarms10.get());
        } else {
            this.alarmNotifListener = null;
        }
        this.airInterfaceNotificationListener =
                new Onf14DomAirInterfaceNotificationListener(netconfDomAccessor, serviceProvider);
        this.ethernetContainerNotificationListener =
                new Onf14DomEthernetContainerNotificationListener(netconfDomAccessor, serviceProvider);
        this.wireInterfaceNotificationListener =
                new Onf14DomWireInterfaceNotificationListener(netconfDomAccessor, serviceProvider);
    }

    public void register() {
        // storing all the LTP UUIDs internally, for later usage, for air-interface and
        // ethernet-container and wire-interface
        coreModel14.readKeys(interfaces);
        readAndWriteInterfaceCurrentProblems();
        readCurrentAlarms();
        registerForNotifications();
    }

    public List<TechnologySpecificPacKeys> getAirInterfaceList() {
        return airInterfaceList;
    }

    private void readCurrentAlarms() {
        if (alarms10.isPresent()) {
            FaultData resultList = alarms10.get().getCurrentAlarms();
            LOG.debug("ResultList = {}", resultList.toString());
            faultService.initCurrentProblemStatus(netconfDomAccessor.getNodeId(), resultList);
        }
    }

    private void readAndWriteInterfaceCurrentProblems() {
        // Read all fault data
        FaultData resultList = new FaultData();
        int problems = 0;

        if (airInterface20.isPresent()) {
            resultList = airInterface20.get().readAllCurrentProblems(resultList, airInterfaceList);

            problems = resultList.size();
            LOG.debug("NETCONF read airinterface current problems completed. Got back {} problems.", problems);
        }
        if (ethernetInterface20.isPresent()) {
            resultList = ethernetInterface20.get().readAllCurrentProblems(resultList, ethernetContainerList);

            problems = resultList.size() - problems;
            LOG.debug("NETCONF read ethernet interface current problems completed. Got back {} problems.", problems);
        }
        if (wireInterface20.isPresent()) {
            resultList = wireInterface20.get().readAllCurrentProblems(resultList, wireInterfaceList);

            problems = resultList.size() - problems;
            LOG.debug("NETCONF read wire interface current problems completed. Got back {} problems.", problems);
        }

        if (resultList.size() > 0) {
            faultService.initCurrentProblemStatus(netconfDomAccessor.getNodeId(), resultList);
            LOG.debug("DB write current problems completed");
        }

    }

    private void readKeys() {
        Optional<NormalizedNode> ltpData = readLtpData(netconfDomAccessor);
        LOG.debug("LTP Data is - {}", ltpData);
        if (ltpData.isPresent()) {
            LOG.debug("In readKeys - ltpData = {}", ltpData.get());

            MapNode ccLtp = (MapNode) ltpData.get();
            if (ccLtp != null) {
                LOG.debug("Iterating the LTP list for node {}", netconfDomAccessor.getNodeId().getValue());
                Collection<MapEntryNode> ltpList = ccLtp.body();

                // iterating all the Logical Termination Point list
                for (MapEntryNode ltp : ltpList) {
                    MapNode lpList =
                            (MapNode) ltp.childByArg(new NodeIdentifier(coreModel14.getQName("layer-protocol")));
                    // the Layer Protocol list should contain only one item, since we have an 1:1
                    // relationship between the LTP and the LP
                    if (lpList != null && lpList.size() != 1) {
                        LOG.debug("Layer protocol has no 1:1 relationship with the LTP.");
                        return;
                    }
                    // accessing the LP, which should be only 1
                    Collection<MapEntryNode> lp = lpList.body();
                    for (MapEntryNode lpEntry : lp) {
                        String layerProtocolName = Onf14DMDOMUtility.getLeafValue(lpEntry,

                                coreModel14.getQName("layer-protocol-name"));
                        if (layerProtocolName != null) {
                            // if the LTP has an airInterface technology extension, the layer protocol name
                            // is air-layer
                            if (layerProtocolName.contains("LAYER_PROTOCOL_NAME_TYPE_AIR_LAYER")) {
                                TechnologySpecificPacKeys airInterfaceKey = new TechnologySpecificPacKeys(
                                        Onf14DMDOMUtility.getLeafValue(ltp, coreModel14.getQName("uuid")),
                                        Onf14DMDOMUtility.getLeafValue(lpEntry, coreModel14.getQName("local-id")));
                                airInterfaceList.add(airInterfaceKey);
                                LOG.debug("Adding Ltp with uuid {} and local-id {} to the air-interface list",
                                        Onf14DMDOMUtility.getLeafValue(ltp, coreModel14.getQName("uuid")),
                                        Onf14DMDOMUtility.getLeafValue(lpEntry, coreModel14.getQName("local-id")));
                            }
                            // if the LTP has an ethernetContainier technology extension, the layer protocol
                            // name is ethernet-container-layer
                            else if (layerProtocolName.contains("LAYER_PROTOCOL_NAME_TYPE_ETHERNET_CONTAINER_LAYER")) {
                                TechnologySpecificPacKeys ethernetContainerKey = new TechnologySpecificPacKeys(
                                        Onf14DMDOMUtility.getLeafValue(ltp, coreModel14.getQName("uuid")),
                                        Onf14DMDOMUtility.getLeafValue(lpEntry, coreModel14.getQName("local-id")));
                                ethernetContainerList.add(ethernetContainerKey);
                                LOG.debug("Adding Ltp with uuid {} and local-id {} to the ethernet-container list",
                                        Onf14DMDOMUtility.getLeafValue(ltp, coreModel14.getQName("uuid")),
                                        Onf14DMDOMUtility.getLeafValue(lpEntry, coreModel14.getQName("local-id")));
                            } else if (layerProtocolName.contains("LAYER_PROTOCOL_NAME_TYPE_WIRE_LAYER")) {
                                TechnologySpecificPacKeys wireInterfaceKey = new TechnologySpecificPacKeys(
                                        Onf14DMDOMUtility.getLeafValue(ltp, coreModel14.getQName("uuid")),
                                        Onf14DMDOMUtility.getLeafValue(lpEntry, coreModel14.getQName("local-id")));
                                wireInterfaceList.add(wireInterfaceKey);
                                LOG.debug("Adding Ltp with uuid {} and local-id {} to the wire-interface list",
                                        Onf14DMDOMUtility.getLeafValue(ltp, coreModel14.getQName("uuid")),
                                        Onf14DMDOMUtility.getLeafValue(lpEntry, coreModel14.getQName("local-id")));
                            }
                        }
                    }
                }
            }
        }
    }

    private void registerForNotifications() {

        if (alarms10.isPresent()) {
            alarms10.get().doRegisterNotificationListener(alarmNotifListener);
        }

        QName[] airInterfaceNotifications = {Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_CREATE_NOTIFICATION,
                Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_AVC_NOTIFICATION,
                Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_DELETE_NOTIFICATION,
                Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_PROBLEM_NOTIFICATION};
        netconfDomAccessor.doRegisterNotificationListener(airInterfaceNotificationListener, airInterfaceNotifications);

        QName[] ethernetContainerNotifications =
                {Onf14DevicemanagerQNames.ETHERNET_CONTAINER_OBJECT_CREATE_NOTIFICATION,
                        Onf14DevicemanagerQNames.ETHERNET_CONTAINER_OBJECT_AVC_NOTIFICATION,
                        Onf14DevicemanagerQNames.ETHERNET_CONTAINER_OBJECT_DELETE_NOTIFICATION,
                        Onf14DevicemanagerQNames.ETHERNET_CONTAINER_OBJECT_PROBLEM_NOTIFICATION};
        netconfDomAccessor.doRegisterNotificationListener(ethernetContainerNotificationListener,
                ethernetContainerNotifications);

        QName[] wireInterfaceNotifications = {Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_CREATE_NOTIFICATION,
                Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_AVC_NOTIFICATION,
                Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_DELETE_NOTIFICATION,
                Onf14DevicemanagerQNames.WIRE_INTERFACE_OBJECT_PROBLEM_NOTIFICATION};
        netconfDomAccessor.doRegisterNotificationListener(wireInterfaceNotificationListener,
                wireInterfaceNotifications);
    }

    public Optional<NormalizedNode> readLtpData(NetconfDomAccessor netconfDomAccessor) {
        LOG.info("Reading Logical Termination Point data");
        return netconfDomAccessor.readDataNode(LogicalDatastoreType.CONFIGURATION, getLtp_IID());
    }

    public PerformanceDataLtp getLtpHistoricalPerformanceData(@NonNull TechnologySpecificPacKeys lp) {
        PerformanceDataLtp res = new PerformanceDataLtp();
        if (airInterface20.isPresent()) {
            airInterface20.get().readAirInterfaceHistoricalPerformanceData(lp.getLtpUuid(), lp.getLocalId(), res);
        } else {
            LOG.warn("Air Interface Module Unsupported. PM data not read");
        }
        return res;
    }

    private YangInstanceIdentifier getLtp_IID() {
        return YangInstanceIdentifier.builder().node(coreModel14.getQName("control-construct"))
                .node(coreModel14.getQName("logical-termination-point")).build();
    }
}
