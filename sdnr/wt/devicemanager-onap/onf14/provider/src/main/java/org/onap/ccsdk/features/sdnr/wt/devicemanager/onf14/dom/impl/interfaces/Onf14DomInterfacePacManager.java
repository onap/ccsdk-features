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

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.dataprovider.InternalDataModelSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.pm.PerformanceDataAirInterface;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Debug;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DevicemanagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.notifications.Onf14DomAirInterfaceNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.notifications.Onf14DomEthernetContainerNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.notifications.Onf14DomWireInterfaceNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.PerformanceDataLtp;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
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

    private static final Logger log = LoggerFactory.getLogger(Onf14DomInterfacePacManager.class);

    private static final YangInstanceIdentifier LTP_IID =
            YangInstanceIdentifier.builder().node(Onf14DevicemanagerQNames.CORE_MODEL_CONTROL_CONSTRUCT_CONTAINER)
                    .node(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP).build();

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

    private final @NonNull FaultService faultService;

    public Onf14DomInterfacePacManager(@NonNull NetconfDomAccessor netconfDomAccessor,
            @NonNull DeviceManagerServiceProvider serviceProvider) {

        this.netconfDomAccessor = Objects.requireNonNull(netconfDomAccessor);
        this.serviceProvider = Objects.requireNonNull(serviceProvider);
        this.faultService = Objects.requireNonNull(serviceProvider.getFaultService());

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
        readKeys();
        readAndWriteInterfaceCurrentProblems();
        registerForNotifications();
    }

    public List<TechnologySpecificPacKeys> getAirInterfaceList() {
        return airInterfaceList;
    }

    public PerformanceDataLtp readAirInterfaceHistoricalPerformanceData(String ltpUuid, String localId,
            PerformanceDataLtp res) {
        log.debug("Get historical performance data for class {} from mountpoint {} for LTP uuid {} and local-id {}",
                Onf14DevicemanagerQNames.AIR_INTERFACE_2_0_MODULE, netconfDomAccessor.getNodeId().getValue(), ltpUuid,
                localId);

        // constructing the IID needs the augmentation exposed by the air-interface-2-0
        // model

        InstanceIdentifierBuilder layerProtocolIID =
                YangInstanceIdentifier.builder().node(Onf14DevicemanagerQNames.CORE_MODEL_CONTROL_CONSTRUCT_CONTAINER)
                        .node(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP)
                        .nodeWithKey(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP,
                                QName.create(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP, "uuid").intern(), ltpUuid)
                        .node(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL)
                        .nodeWithKey(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL, QName
                                .create(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL, "local-id").intern(),
                                localId);

        @NonNull
        AugmentationIdentifier airInterfacePacIID = YangInstanceIdentifier.AugmentationIdentifier
                .create(Sets.newHashSet(Onf14DevicemanagerQNames.AIR_INTERFACE_PAC));

        InstanceIdentifierBuilder augmentedAirInterfacePacIID =
                YangInstanceIdentifier.builder(layerProtocolIID.build()).node(airInterfacePacIID);

        // reading historical performance list for this specific LTP and LP
        Optional<NormalizedNode> airInterfacePacDataOpt =
                netconfDomAccessor.readDataNode(LogicalDatastoreType.OPERATIONAL, augmentedAirInterfacePacIID.build());
        log.debug("Performance Data = {}", airInterfacePacDataOpt.get().body());
        if (airInterfacePacDataOpt.isPresent()) {
            AugmentationNode airInterfacePacData = (AugmentationNode) airInterfacePacDataOpt.get();
            ContainerNode cn = (ContainerNode) airInterfacePacData
                    .childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.AIR_INTERFACE_PAC));
            if (cn != null) {
                ContainerNode airIntfHistPerf = (ContainerNode) cn
                        .childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCES));
                if (airIntfHistPerf != null) {
                    MapNode airInterfaceHistoricalPerformanceList = (MapNode) airIntfHistPerf.childByArg(
                            new NodeIdentifier(Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCES_LIST));
                    if (airInterfaceHistoricalPerformanceList != null) {
                        Collection<MapEntryNode> airInterfaceHistoricalPerfCollection =
                                airInterfaceHistoricalPerformanceList.body();
                        for (MapEntryNode airInterfaceHistPerf : airInterfaceHistoricalPerfCollection) {
                            res.add(new PerformanceDataAirInterface(netconfDomAccessor.getNodeId(), ltpUuid, localId,
                                    airInterfaceHistPerf));
                        }
                        return res;
                    } else {
                        log.debug("DBRead Id {} empty CurrentProblemList", ltpUuid);
                    }
                }
            }
        }
        return null;
    }

    private void readAndWriteInterfaceCurrentProblems() {
        // Read all fault data
        FaultData resultList = new FaultData();
        int problems = 0;
        readAllAirInterfaceCurrentProblems(resultList);
        problems = resultList.size();
        log.debug("NETCONF read air interface current problems completed. Got back {} problems.", problems);


        readAllEthernetContainerCurrentProblems(resultList);
        problems = resultList.size() - problems;
        log.debug("NETCONF read current problems completed. Got back {} problems.", problems);

        readAllWireInterfaceCurrentProblems(resultList);
        problems = resultList.size();
        log.debug("NETCONF read wire interface current problems completed. Got back {} problems.", problems);


        if (resultList.size() > 0) {
            faultService.initCurrentProblemStatus(netconfDomAccessor.getNodeId(), resultList);
            log.debug("DB write current problems completed");
        }

    }

    private void readKeys() {
        Optional<NormalizedNode> ltpData = readLtpData(netconfDomAccessor);
        log.debug("LTP Data is - {}", ltpData);
        if (ltpData.isPresent()) {

            MapNode ccLtp = (MapNode) ltpData.get();
            if (ccLtp != null) {
                log.debug("Iterating the LTP list for node {}", netconfDomAccessor.getNodeId().getValue());
                Collection<MapEntryNode> ltpList = ccLtp.body();

                // iterating all the Logical Termination Point list
                for (MapEntryNode ltp : ltpList) {
                    MapNode lpList = (MapNode) ltp
                            .childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL));
                    // the Layer Protocol list should contain only one item, since we have an 1:1
                    // relationship between the LTP and the LP
                    if (lpList != null && lpList.size() != 1) {
                        log.debug("Layer protocol has no 1:1 relationship with the LTP.");
                        return;
                    }
                    // accessing the LP, which should be only 1
                    Collection<MapEntryNode> lp = lpList.body();
                    for (MapEntryNode lpEntry : lp) {
                        String layerProtocolName = Onf14DMDOMUtility.getLeafValue(lpEntry,

                                Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL_NAME);
                        if (layerProtocolName != null) {
                            // if the LTP has an airInterface technology extension, the layer protocol name
                            // is air-layer
                            if (layerProtocolName.contains("LAYER_PROTOCOL_NAME_TYPE_AIR_LAYER")) {
                                TechnologySpecificPacKeys airInterfaceKey = new TechnologySpecificPacKeys(
                                        Onf14DMDOMUtility.getLeafValue(ltp,
                                                Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_UUID),
                                        Onf14DMDOMUtility.getLeafValue(lpEntry,
                                                Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL_LOCAL_ID));
                                airInterfaceList.add(airInterfaceKey);
                                log.debug("Adding Ltp with uuid {} and local-id {} to the air-interface list",
                                        Onf14DMDOMUtility.getLeafValue(ltp,
                                                Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_UUID),
                                        Onf14DMDOMUtility.getLeafValue(lpEntry,
                                                Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL_LOCAL_ID));
                            }
                            // if the LTP has an ethernetContainer technology extension, the layer protocol
                            // name is ethernet-container-layer
                            else if (layerProtocolName.contains("LAYER_PROTOCOL_NAME_TYPE_ETHERNET_CONTAINER_LAYER")) {
                                TechnologySpecificPacKeys ethernetContainerKey = new TechnologySpecificPacKeys(
                                        Onf14DMDOMUtility.getLeafValue(ltp,
                                                Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_UUID),
                                        Onf14DMDOMUtility.getLeafValue(lpEntry,
                                                Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL_LOCAL_ID));
                                ethernetContainerList.add(ethernetContainerKey);
                                log.debug("Adding Ltp with uuid {} and local-id {} to the ethernet-container list",
                                        Onf14DMDOMUtility.getLeafValue(ltp,
                                                Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_UUID),
                                        Onf14DMDOMUtility.getLeafValue(lpEntry,
                                                Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL_LOCAL_ID));
                            } else if (layerProtocolName.contains("LAYER_PROTOCOL_NAME_TYPE_WIRE_LAYER")) {
                                TechnologySpecificPacKeys wireInterfaceKey = new TechnologySpecificPacKeys(
                                        Onf14DMDOMUtility.getLeafValue(ltp,
                                                Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_UUID),
                                        Onf14DMDOMUtility.getLeafValue(lpEntry,
                                                Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL_LOCAL_ID));
                                wireInterfaceList.add(wireInterfaceKey);
                                log.debug("Adding Ltp with uuid {} and local-id {} to the wire-interface list",
                                        Onf14DMDOMUtility.getLeafValue(ltp,
                                                Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_UUID),
                                        Onf14DMDOMUtility.getLeafValue(lpEntry,
                                                Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL_LOCAL_ID));
                            }
                        }
                    }
                }
            }
        }
    }

    private void readAllAirInterfaceCurrentProblems(FaultData resultList) {

        int idxStart; // Start index for debug messages

        for (TechnologySpecificPacKeys key : airInterfaceList) {
            idxStart = resultList.size();

            readAirInterfaceCurrentProblemForLtp(key.getLtpUuid(), key.getLocalId(), resultList);
            Debug.debugResultList(key.getLtpUuid(), resultList, idxStart);
        }
    }

    private void readAllEthernetContainerCurrentProblems(FaultData resultList) {

        int idxStart; // Start index for debug messages

        for (TechnologySpecificPacKeys key : ethernetContainerList) {
            idxStart = resultList.size();

            readEthernetContainerCurrentProblemForLtp(key.getLtpUuid(), key.getLocalId(), resultList);
            Debug.debugResultList(key.getLtpUuid(), resultList, idxStart);
        }
    }

    private void readAllWireInterfaceCurrentProblems(FaultData resultList) {

        int idxStart; // Start index for debug messages

        for (TechnologySpecificPacKeys key : wireInterfaceList) {
            idxStart = resultList.size();

            readWireInterfaceCurrentProblemForLtp(key.getLtpUuid(), key.getLocalId(), resultList);
            Debug.debugResultList(key.getLtpUuid(), resultList, idxStart);
        }
    }

    private void readAirInterfaceCurrentProblemForLtp(String ltpUuid, String localId, FaultData resultList) {

        log.debug("DBRead Get current problems for class {} from mountpoint {} for LTP uuid {} and local-id {}",
                Onf14DevicemanagerQNames.AIR_INTERFACE_2_0_MODULE, netconfDomAccessor.getNodeId().getValue(), ltpUuid,
                localId);

        // constructing the IID needs the augmentation exposed by the air-interface-2-0
        // model

        InstanceIdentifierBuilder layerProtocolIID =
                YangInstanceIdentifier.builder().node(Onf14DevicemanagerQNames.CORE_MODEL_CONTROL_CONSTRUCT_CONTAINER)
                        .node(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP)
                        .nodeWithKey(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP,
                                QName.create(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP, "uuid").intern(), ltpUuid)
                        .node(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL)
                        .nodeWithKey(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL, QName
                                .create(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL, "local-id").intern(),
                                localId);

        @NonNull
        AugmentationIdentifier airInterfacePacIID = YangInstanceIdentifier.AugmentationIdentifier
                .create(Sets.newHashSet(Onf14DevicemanagerQNames.AIR_INTERFACE_PAC));

        InstanceIdentifierBuilder augmentedAirInterfacePacIID =
                YangInstanceIdentifier.builder(layerProtocolIID.build()).node(airInterfacePacIID);

        // reading all the current-problems list for this specific LTP and LP
        Optional<NormalizedNode> airInterfacePacDataOpt =
                netconfDomAccessor.readDataNode(LogicalDatastoreType.OPERATIONAL, augmentedAirInterfacePacIID.build());

        if (airInterfacePacDataOpt.isPresent()) {
            AugmentationNode airInterfacePacData = (AugmentationNode) airInterfacePacDataOpt.get();
            MapNode airInterfaceCurrentProblemsList = (MapNode) airInterfacePacData
                    .childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.AIR_INTERFACE_CURRENT_PROBLEMS_LIST));
            if (airInterfaceCurrentProblemsList != null) {
                Collection<MapEntryNode> airInterfaceProblemsCollection = airInterfaceCurrentProblemsList.body();
                for (MapEntryNode airInterfaceProblem : airInterfaceProblemsCollection) {
                    resultList.add(netconfDomAccessor.getNodeId(),
                            Integer.parseInt(Onf14DMDOMUtility.getLeafValue(airInterfaceProblem,
                                    Onf14DevicemanagerQNames.AIR_INTERFACE_CURRENT_PROBLEMS_SEQ_NO)),
                            new DateAndTime(Onf14DMDOMUtility.getLeafValue(airInterfaceProblem,
                                    Onf14DevicemanagerQNames.AIR_INTERFACE_CURRENT_PROBLEMS_TIMESTAMP)),
                            ltpUuid,
                            Onf14DMDOMUtility.getLeafValue(airInterfaceProblem,
                                    Onf14DevicemanagerQNames.AIR_INTERFACE_CURRENT_PROBLEMS_PROBLEM_NAME),
                            InternalDataModelSeverity.mapSeverity(Onf14DMDOMUtility.getLeafValue(airInterfaceProblem,
                                    Onf14DevicemanagerQNames.AIR_INTERFACE_CURRENT_PROBLEMS_PROBLEM_SEVERITY)));
                }
            } else {
                log.debug("DBRead Id {} empty CurrentProblemList", ltpUuid);
            }
        }
    }

    private void readEthernetContainerCurrentProblemForLtp(String ltpUuid, String localId, FaultData resultList) {

        log.debug(
                "DBRead Get current problems for Ethernet Container from mountpoint {} for LTP uuid {} and local-id {}",
                netconfDomAccessor.getNodeId().getValue(), ltpUuid, localId);

        // constructing the IID needs the augmentation exposed by the
        // ethernet-container-2-0 model
        InstanceIdentifierBuilder layerProtocolIID =
                YangInstanceIdentifier.builder().node(Onf14DevicemanagerQNames.CORE_MODEL_CONTROL_CONSTRUCT_CONTAINER)
                        .node(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP)
                        .nodeWithKey(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP,
                                QName.create(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP, "uuid").intern(), ltpUuid)
                        .node(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL)
                        .nodeWithKey(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL, QName
                                .create(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL, "local-id").intern(),
                                localId);

        @NonNull
        AugmentationIdentifier ethernetContainerIID = YangInstanceIdentifier.AugmentationIdentifier
                .create(Sets.newHashSet(Onf14DevicemanagerQNames.ETHERNET_CONTAINER_PAC));

        InstanceIdentifierBuilder augmentedEthernetContainerConfigurationIID =
                YangInstanceIdentifier.builder(layerProtocolIID.build()).node(ethernetContainerIID);

        // reading all the current-problems list for this specific LTP and LP
        Optional<NormalizedNode> etherntContainerConfigurationOpt = netconfDomAccessor
                .readDataNode(LogicalDatastoreType.OPERATIONAL, augmentedEthernetContainerConfigurationIID.build());

        if (etherntContainerConfigurationOpt.isPresent()) {
            AugmentationNode etherntContainerConfiguration = (AugmentationNode) etherntContainerConfigurationOpt.get();
            MapNode ethernetContainerCurrentProblemsList = (MapNode) etherntContainerConfiguration
                    .childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.ETHERNET_CONTAINER_CURRENT_PROBLEMS_LIST));
            if (ethernetContainerCurrentProblemsList != null) {
                Collection<MapEntryNode> ethernetContainerProblemsCollection =
                        ethernetContainerCurrentProblemsList.body();
                for (MapEntryNode ethernetContainerProblem : ethernetContainerProblemsCollection) {
                    resultList.add(netconfDomAccessor.getNodeId(),
                            Integer.parseInt(Onf14DMDOMUtility.getLeafValue(ethernetContainerProblem,
                                    Onf14DevicemanagerQNames.ETHERNET_CONTAINER_CURRENT_PROBLEMS_SEQ_NO)),
                            new DateAndTime(Onf14DMDOMUtility.getLeafValue(ethernetContainerProblem,
                                    Onf14DevicemanagerQNames.ETHERNET_CONTAINER_CURRENT_PROBLEMS_TIMESTAMP)),
                            ltpUuid,
                            Onf14DMDOMUtility.getLeafValue(ethernetContainerProblem,
                                    Onf14DevicemanagerQNames.ETHERNET_CONTAINER_CURRENT_PROBLEMS_PROBLEM_NAME),
                            InternalDataModelSeverity.mapSeverity(Onf14DMDOMUtility.getLeafValue(
                                    ethernetContainerProblem,
                                    Onf14DevicemanagerQNames.ETHERNET_CONTAINER_CURRENT_PROBLEMS_PROBLEM_SEVERITY)));
                }
            } else {
                log.debug("DBRead Id {} empty CurrentProblemList", ltpUuid);
            }
        }

    }

    private void readWireInterfaceCurrentProblemForLtp(String ltpUuid, String localId, FaultData resultList) {

        log.debug("DBRead Get current problems for Wire Interface from mountpoint {} for LTP uuid {} and local-id {}",
                netconfDomAccessor.getNodeId().getValue(), ltpUuid, localId);

        // constructing the IID needs the augmentation exposed by the wire-interface-2-0
        // model
        InstanceIdentifierBuilder layerProtocolIID =
                YangInstanceIdentifier.builder().node(Onf14DevicemanagerQNames.CORE_MODEL_CONTROL_CONSTRUCT_CONTAINER)
                        .node(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP)
                        .nodeWithKey(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP,
                                QName.create(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP, "uuid").intern(), ltpUuid)
                        .node(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL)
                        .nodeWithKey(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL, QName
                                .create(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP_LAYER_PROTOCOL, "local-id").intern(),
                                localId);

        @NonNull
        AugmentationIdentifier wireInterfacePacIID = YangInstanceIdentifier.AugmentationIdentifier
                .create(Sets.newHashSet(Onf14DevicemanagerQNames.WIRE_INTERFACE_PAC));

        InstanceIdentifierBuilder augmentedWireInterfaceConfigurationIID =
                YangInstanceIdentifier.builder(layerProtocolIID.build()).node(wireInterfacePacIID);

        // reading all the current-problems list for this specific LTP and LP
        Optional<NormalizedNode> wireInterfaceConfigurationOpt = netconfDomAccessor
                .readDataNode(LogicalDatastoreType.OPERATIONAL, augmentedWireInterfaceConfigurationIID.build());

        if (wireInterfaceConfigurationOpt.isPresent()) {
            AugmentationNode wireInterfaceConfiguration = (AugmentationNode) wireInterfaceConfigurationOpt.get();
            MapNode wireInterfaceCurrentProblemsList = (MapNode) wireInterfaceConfiguration
                    .childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.WIRE_INTERFACE_CURRENT_PROBLEMS_LIST));
            if (wireInterfaceCurrentProblemsList != null) {
                Collection<MapEntryNode> wireInterfaceProblemsCollection = wireInterfaceCurrentProblemsList.body();
                for (MapEntryNode wireInterfaceProblem : wireInterfaceProblemsCollection) {
                    resultList.add(netconfDomAccessor.getNodeId(),
                            Integer.parseInt(Onf14DMDOMUtility.getLeafValue(wireInterfaceProblem,
                                    Onf14DevicemanagerQNames.WIRE_INTERFACE_CURRENT_PROBLEMS_SEQ_NO)),
                            new DateAndTime(Onf14DMDOMUtility.getLeafValue(wireInterfaceProblem,
                                    Onf14DevicemanagerQNames.WIRE_INTERFACE_CURRENT_PROBLEMS_TIMESTAMP)),
                            ltpUuid,
                            Onf14DMDOMUtility.getLeafValue(wireInterfaceProblem,
                                    Onf14DevicemanagerQNames.WIRE_INTERFACE_CURRENT_PROBLEMS_PROBLEM_NAME),
                            InternalDataModelSeverity.mapSeverity(Onf14DMDOMUtility.getLeafValue(wireInterfaceProblem,
                                    Onf14DevicemanagerQNames.WIRE_INTERFACE_CURRENT_PROBLEMS_PROBLEM_SEVERITY)));
                }
            } else {
                log.debug("DBRead Id {} empty CurrentProblemList", ltpUuid);
            }
        }

    }

    private void registerForNotifications() {

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
        log.info("Reading Logical Termination Point data");
        return netconfDomAccessor.readDataNode(LogicalDatastoreType.CONFIGURATION, LTP_IID);
    }

    public PerformanceDataLtp getLtpHistoricalPerformanceData(@NonNull TechnologySpecificPacKeys lp) {
        PerformanceDataLtp res = new PerformanceDataLtp();
        readAirInterfaceHistoricalPerformanceData(lp.getLtpUuid(), lp.getLocalId(), res);
        return res;
    }
}
