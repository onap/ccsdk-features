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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.interfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.equipment.Onf14DomEquipmentManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.util.Debug;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.AttributeValueChangedNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.LAYERPROTOCOLNAMETYPEAIRLAYER;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.LayerProtocol1;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.ObjectCreationNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.ObjectDeletionNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.ProblemNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.air._interface.lp.spec.AirInterfacePac;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.air._interface.pac.AirInterfaceCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.ControlConstruct;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.LAYERPROTOCOLNAMETYPE;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.LogicalTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.LogicalTerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.logical.termination.point.LayerProtocol;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.logical.termination.point.LayerProtocolKey;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.LAYERPROTOCOLNAMETYPEETHERNETCONTAINERLAYER;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.LAYERPROTOCOLNAMETYPEWIRELAYER;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.QName;
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

public class Onf14DomInterfacePacManager implements DOMNotificationListener {

    // constants
    private static final Logger log = LoggerFactory.getLogger(Onf14DomEquipmentManager.class);
    // end of constants

    // variables
    private final NetconfDomAccessor netconfDomAccessor;
    private final @NonNull DeviceManagerServiceProvider serviceProvider;

    // air interface related members
    private final List<TechnologySpecificPacKeys> airInterfaceList = new ArrayList<TechnologySpecificPacKeys>();
    private @NonNull final Onf14AirInterfaceNotificationListener airInterfaceNotificationListener;

    // ethernet container related members
    private final List<TechnologySpecificPacKeys> ethernetContainerList = new ArrayList<TechnologySpecificPacKeys>();
    private @NonNull final Onf14EthernetContainerNotificationListener ethernetContainerNotificationListener;

    // wire interface related members
    private final List<TechnologySpecificPacKeys> wireInterfaceList = new ArrayList<TechnologySpecificPacKeys>();
    private @NonNull final Onf14WireInterfaceNotificationListener wireInterfaceNotificationListener;
    private @NonNull final BindingNormalizedNodeSerializer serializer;
    // end of variables


    // constructors
    public Onf14DomInterfacePacManager(@NonNull NetconfDomAccessor netconfDomAccessor,
            @NonNull DeviceManagerServiceProvider serviceProvider) {

        this.netconfDomAccessor = Objects.requireNonNull(netconfDomAccessor);
        this.serviceProvider = Objects.requireNonNull(serviceProvider);
        this.serializer = Objects.requireNonNull(netconfDomAccessor.getBindingNormalizedNodeSerializer());

        this.airInterfaceNotificationListener =
                new Onf14AirInterfaceNotificationListener(netconfDomAccessor, serviceProvider);
        this.ethernetContainerNotificationListener =
                new Onf14EthernetContainerNotificationListener(netconfDomAccessor, serviceProvider);
        this.wireInterfaceNotificationListener =
                new Onf14WireInterfaceNotificationListener(netconfDomAccessor, serviceProvider);
    }
    // end of constructors

    // getters and setters
    // end of getters and setters

    // private methods
    // end of private methods

    // public methods
    public void readAllAirInterfaceCurrentProblems(NetconfDomAccessor netconfDomAccessor,
            ControlConstruct controlConstruct, FaultData resultList) {

        int idxStart; // Start index for debug messages

        for (TechnologySpecificPacKeys key : airInterfaceList) {
            idxStart = resultList.size();

            readAirInterfaceCurrentProblemForLtp(netconfDomAccessor, controlConstruct, key.getLtpUuid(),
                    key.getLocalId(), resultList);
            Debug.debugResultList(key.getLtpUuid().getValue(), resultList, idxStart);
        }
    }

    /*
    public void readAllEhernetContainerCurrentProblems(FaultData resultList) {

        int idxStart; // Start index for debug messages

        for (TechnologySpecificPacKeys key : ethernetContainerList) {
            idxStart = resultList.size();

            readEthernetConainerCurrentProblemForLtp(key.getLtpUuid(), key.getLocalId(), resultList);
            debugResultList(key.getLtpUuid().getValue(), resultList, idxStart);
        }
    }

    public void readAllWireInterfaceCurrentProblems(FaultData resultList) {

        int idxStart; // Start index for debug messages

        for (TechnologySpecificPacKeys key : wireInterfaceList) {
            idxStart = resultList.size();

            readWireInterfaceCurrentProblemForLtp(key.getLtpUuid(), key.getLocalId(), resultList);
            debugResultList(key.getLtpUuid().getValue(), resultList, idxStart);
        }
    }*/
    public void readKeys(ControlConstruct controlConstruct) {

        @NonNull
        Collection<LogicalTerminationPoint> ltpList =
                YangHelper.getCollection(controlConstruct.nonnullLogicalTerminationPoint());
        log.debug("Iterating the LTP list for node {}", netconfDomAccessor.getNodeId().getValue());

        // iterating all the Logical Termination Point list
        for (LogicalTerminationPoint ltp : ltpList) {
            @NonNull
            List<LayerProtocol> lpList = YangHelper.getList(ltp.nonnullLayerProtocol());
            // the Layer Protocol list should contain only one item, since we have an 1:1 relationship between the LTP and the LP
            if (lpList.size() != 1) {
                log.debug("Layer protocol has no 1:1 relationship with the LTP.");
                return;
            }
            // accessing the LP, which should be only 1
            LayerProtocol lp = lpList.get(0);
            @Nullable
            Class<? extends LAYERPROTOCOLNAMETYPE> layerProtocolName = lp.getLayerProtocolName();
            if (layerProtocolName != null) {
                // if the LTP has an airInterface technology extension, the layer protocol name is air-layer
                if (layerProtocolName.getTypeName().equals(LAYERPROTOCOLNAMETYPEAIRLAYER.class.getName())) {
                    TechnologySpecificPacKeys airInterfaceKey =
                            new TechnologySpecificPacKeys(ltp.getUuid(), lp.getLocalId());
                    airInterfaceList.add(airInterfaceKey);
                    log.debug("Adding Ltp with uuid {} and local-id {} to the air-interface list",
                            ltp.getUuid().getValue(), lp.getLocalId());
                }
                // if the LTP has an ethernetContainier technology extension, the layer protocol name is ethernet-container-layer
                else if (layerProtocolName.getTypeName().equals(LAYERPROTOCOLNAMETYPEETHERNETCONTAINERLAYER.class
                        .getName())) {
                    TechnologySpecificPacKeys ethernetContainerKey =
                            new TechnologySpecificPacKeys(ltp.getUuid(), lp.getLocalId());
                    ethernetContainerList.add(ethernetContainerKey);
                    log.debug("Adding Ltp with uuid {} and local-id {} to the ethernet-contatinier list",
                            ltp.getUuid().getValue(), lp.getLocalId());
                } else if (layerProtocolName.getTypeName().equals(LAYERPROTOCOLNAMETYPEWIRELAYER.class.getName())) {
                    TechnologySpecificPacKeys wireInterfaceKey =
                            new TechnologySpecificPacKeys(ltp.getUuid(), lp.getLocalId());
                    wireInterfaceList.add(wireInterfaceKey);
                    log.debug("Adding Ltp with uuid {} and local-id {} to the wire-interface list",
                            ltp.getUuid().getValue(), lp.getLocalId());
                }
            }
        }
    }

    private static void readAirInterfaceCurrentProblemForLtp(NetconfDomAccessor netconfDomAccessor,
            ControlConstruct controlConstruct, UniversalId ltpUuid, String localId, FaultData resultList) {

        final Class<AirInterfacePac> clazzPac = AirInterfacePac.class;

        log.info("DBRead Get current problems for class {} from mountpoint {} for LTP uuid {} and local-id {}",
                clazzPac.getSimpleName(), netconfDomAccessor.getNodeId().getValue(), ltpUuid.getValue(), localId);

        // constructing the IID needs the augmentation exposed byy the air-interface-2-0 model
        //        InstanceIdentifier<AirInterfaceCurrentProblems> airInterfaceCurrentProblem_IID = InstanceIdentifier
        //                .builder(ControlConstruct.class)
        //                .child(LogicalTerminationPoint.class, new LogicalTerminationPointKey(ltpUuid))
        //                .child(LayerProtocol.class, new LayerProtocolKey(localId))
        //                .augmentation(
        //                        org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.LayerProtocol1.class)
        //                .child(AirInterfacePac.class)
        //                .child(AirInterfaceCurrentProblems.class).build();
        /*
        final YangInstanceIdentifier airInterfaceCurrentProblem_IID =
                YangInstanceIdentifier.builder().node(ControlConstruct.QNAME)
                        .nodeWithKey(LogicalTerminationPoint.QNAME,
                                QName.create(LogicalTerminationPoint.QNAME, "logical-termination-point-key").intern(),
                                ltpUuid.getValue())
                        .nodeWithKey(LayerProtocol.QNAME,
                                QName.create(LayerProtocol.QNAME, "layer-protocol-key").intern(), localId)
                        //.node(org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.LayerProtocol1.QNAME)
                        .node(AirInterfacePac.QNAME).node(AirInterfaceCurrentProblems.QNAME).build();

        // reading all the current-problems list for this specific LTP and LP
        AirInterfaceCurrentProblems problems =
                 netconfDomAccessor.readData(LogicalDatastoreType.OPERATIONAL, airInterfaceCurrentProblem_IID);
         */

        @NonNull
        Map<LogicalTerminationPointKey, LogicalTerminationPoint> ltpMap =
                controlConstruct.nonnullLogicalTerminationPoint();
        LogicalTerminationPoint ltp = ltpMap.get(new LogicalTerminationPointKey(ltpUuid));
        if (ltp != null) {
            @NonNull
            Map<LayerProtocolKey, LayerProtocol> lpMap = ltp.nonnullLayerProtocol();
            LayerProtocol lp = lpMap.get(new LayerProtocolKey(localId));
            @Nullable
            LayerProtocol1 lp1 = lp.augmentation(
                    org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.LayerProtocol1.class);
            if (lp1 != null) {
                @Nullable
                AirInterfacePac airInterfacePack = lp1.getAirInterfacePac();
                if (airInterfacePack != null) {
                    @Nullable
                    AirInterfaceCurrentProblems cp = airInterfacePack.getAirInterfaceCurrentProblems();
                    if (cp == null) {
                        log.debug("DBRead Id {} no AirInterfaceCurrentProblems", ltpUuid);
                    } else {
                        for (org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.air._interface.current.problems.CurrentProblemList problem : YangHelper
                                .getCollection(cp.nonnullCurrentProblemList())) {
                            resultList.add(netconfDomAccessor.getNodeId(), (int) problem.getSequenceNumber(),
                                    problem.getTimestamp(), ltpUuid.getValue(), problem.getProblemName(),
                                    Onf14AirInterface.mapSeverity(problem.getProblemSeverity()));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onNotification(@NonNull DOMNotification domNotification) {
        @Nullable
        Notification notification =
                serializer.fromNormalizedNodeNotification(domNotification.getType(), domNotification.getBody());
        if (notification instanceof ProblemNotification) {
            ProblemNotification problemNotification = (ProblemNotification) notification;
            log.debug("DOM ProblemNotification: {}", problemNotification);
            airInterfaceNotificationListener.onProblemNotification(problemNotification);
        } else if (notification instanceof AttributeValueChangedNotification) {
            AttributeValueChangedNotification attributeValueChangeNotification =
                    (AttributeValueChangedNotification) notification;
            log.debug("DOM AttributeValueChangedNotification: {}", attributeValueChangeNotification);
            airInterfaceNotificationListener.onAttributeValueChangedNotification(attributeValueChangeNotification);
        } else if (notification instanceof ObjectDeletionNotification) {
            ObjectDeletionNotification objectDeletionNotification = (ObjectDeletionNotification) notification;
            log.debug("DOM ObjectDeletionNotification: {}", objectDeletionNotification);
            airInterfaceNotificationListener.onObjectDeletionNotification(objectDeletionNotification);
        } else if (notification instanceof ObjectCreationNotification) {
            ObjectCreationNotification objectCreationNotification = (ObjectCreationNotification) notification;
            log.debug("DOM ObjectDeletionNotification: {}", objectCreationNotification);
            airInterfaceNotificationListener.onObjectCreationNotification(objectCreationNotification);
        } else {
            log.warn("DOM Notification ignored: {}", domNotification);
        }
    }

    /**
     * Register notifications to handle
     */
    public void subscribeNotifications() {
        QName[] notifications = { ObjectCreationNotification.QNAME, ObjectDeletionNotification.QNAME,
                AttributeValueChangedNotification.QNAME, ProblemNotification.QNAME };
        netconfDomAccessor.doRegisterNotificationListener(this, notifications);
    }

    /*
    private void readEthernetConainerCurrentProblemForLtp(UniversalId ltpUuid, String localId, FaultData resultList) {

        final Class<EthernetContainerPac> clazzPac = EthernetContainerPac.class;

        log.info("DBRead Get current problems for class {} from mountpoint {} for LTP uuid {} and local-id {}",
                clazzPac.getSimpleName(), netconfDomAccessor.getNodeId().getValue(), ltpUuid.getValue(), localId);

        // constructing the IID needs the augmentation exposed by the ethernet-container-2-0 model
        //        InstanceIdentifier<EthernetContainerCurrentProblems> etherneContainerCurrentProblem_IID = InstanceIdentifier
        //                .builder(ControlConstruct.class)
        //                .child(LogicalTerminationPoint.class, new LogicalTerminationPointKey(ltpUuid))
        //                .child(LayerProtocol.class, new LayerProtocolKey(localId))
        //                .augmentation(
        //                        org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.LayerProtocol1.class)
        //                .child(EthernetContainerPac.class).child(EthernetContainerCurrentProblems.class).build();
        final YangInstanceIdentifier etherneContainerCurrentProblem_IID =
                YangInstanceIdentifier.builder().node(ControlConstruct.QNAME)
                        .nodeWithKey(LogicalTerminationPoint.QNAME,
                                QName.create(LogicalTerminationPoint.QNAME, "logical-termination-point-key").intern(),
                                ltpUuid.getValue())
                        .nodeWithKey(LayerProtocol.QNAME,
                                QName.create(LayerProtocol.QNAME, "layer-protocol-key").intern(), localId)
                        //.node(org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.LayerProtocol1.QNAME)
                        .node(EthernetContainerPac.QNAME).node(EthernetContainerCurrentProblems.QNAME).build();

        // reading all the current-problems list for this specific LTP and LP
        EthernetContainerCurrentProblems problems =
                netconfDomAccessor.readData(LogicalDatastoreType.OPERATIONAL, etherneContainerCurrentProblem_IID);

        if (problems == null) {
            log.debug("DBRead Id {} no EthernetContainerCurrentProblems", ltpUuid);
        } else if (problems.getCurrentProblemList() == null) {
            log.debug("DBRead Id {} empty CurrentProblemList", ltpUuid);
        } else {
            for (org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.ethernet.container.current.problems.CurrentProblemList problem : YangHelper
                    .getCollection(problems.nonnullCurrentProblemList())) {
                resultList.add(netconfDomAccessor.getNodeId(), (int) problem.getSequenceNumber(),
                        problem.getTimestamp(), ltpUuid.getValue(), problem.getProblemName(),
                        Onf14EthernetContainer.mapSeverity(problem.getProblemSeverity()));
            }
        }
    }

    private void readWireInterfaceCurrentProblemForLtp(UniversalId ltpUuid, String localId, FaultData resultList) {

        final Class<WireInterfacePac> clazzPac = WireInterfacePac.class;

        log.info("DBRead Get current problems for class {} from mountpoint {} for LTP uuid {} and local-id {}",
                clazzPac.getSimpleName(), netconfDomAccessor.getNodeId().getValue(), ltpUuid.getValue(), localId);

        // constructing the IID needs the augmentation exposed by the wire-interface-2-0 model
        //        InstanceIdentifier<WireInterfaceCurrentProblems> wireInterfaceCurrentProblem_IID = InstanceIdentifier
        //                .builder(ControlConstruct.class)
        //                .child(LogicalTerminationPoint.class, new LogicalTerminationPointKey(ltpUuid))
        //                .child(LayerProtocol.class, new LayerProtocolKey(localId))
        //                .augmentation(
        //                        org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.LayerProtocol1.class)
        //                .child(WireInterfacePac.class).child(WireInterfaceCurrentProblems.class).build();
        final YangInstanceIdentifier wireInterfaceCurrentProblem_IID =
                YangInstanceIdentifier.builder().node(ControlConstruct.QNAME)
                        .nodeWithKey(LogicalTerminationPoint.QNAME,
                                QName.create(LogicalTerminationPoint.QNAME, "logical-termination-point-key").intern(),
                                ltpUuid.getValue())
                        .nodeWithKey(LayerProtocol.QNAME,
                                QName.create(LayerProtocol.QNAME, "layer-protocol-key").intern(), localId)
                        //.node(org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.LayerProtocol1.QNAME)
                        .node(WireInterfacePac.QNAME).node(WireInterfaceCurrentProblems.QNAME).build();

        // reading all the current-problems list for this specific LTP and LP
        WireInterfaceCurrentProblems problems =
                netconfDomAccessor.readData(LogicalDatastoreType.OPERATIONAL, wireInterfaceCurrentProblem_IID);

        if (problems == null) {
            log.debug("DBRead Id {} no WireInterfaceCurrentProblems", ltpUuid);
        } else if (problems.getCurrentProblemList() == null) {
            log.debug("DBRead Id {} empty CurrentProblemList", ltpUuid);
        } else {
            for (org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.wire._interface.current.problems.CurrentProblemList problem : YangHelper
                    .getCollection(problems.nonnullCurrentProblemList())) {
                resultList.add(netconfDomAccessor.getNodeId(), (int) problem.getSequenceNumber(),
                        problem.getTimestamp(), ltpUuid.getValue(), problem.getProblemName(),
                        Onf14WireInterface.mapSeverity(problem.getProblemSeverity()));
            }
        }
    }
    */

    // end of public methods

    // static methods
    // end of static methods

    // private classes
    // end of private classes
}
