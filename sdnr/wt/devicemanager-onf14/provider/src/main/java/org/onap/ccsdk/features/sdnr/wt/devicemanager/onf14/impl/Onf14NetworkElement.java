/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.dataprovider.Onf14ToInternalDataModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.interfaces.Onf14AirInterface;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.interfaces.Onf14AirInterfaceNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.interfaces.Onf14EthernetContainer;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.interfaces.Onf14EthernetContainerNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.interfaces.Onf14WireInterface;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.interfaces.Onf14WireInterfaceNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNotifications;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.LAYERPROTOCOLNAMETYPEAIRLAYER;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.air._interface.lp.spec.AirInterfacePac;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.air._interface.pac.AirInterfaceCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.ControlConstruct;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.LAYERPROTOCOLNAMETYPE;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.Equipment;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.EquipmentKey;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.LogicalTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.LogicalTerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.equipment.ContainedHolder;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.logical.termination.point.LayerProtocol;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.logical.termination.point.LayerProtocolKey;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.LAYERPROTOCOLNAMETYPEETHERNETCONTAINERLAYER;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.ethernet.container.lp.spec.EthernetContainerPac;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.ethernet.container.pac.EthernetContainerCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.LAYERPROTOCOLNAMETYPEWIRELAYER;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.wire._interface.lp.spec.WireInterfacePac;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.wire._interface.pac.WireInterfaceCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repesentation of ONF Core model 1.4 device Top level element is "ControlConstruct" (replaces "NetworkElement" of
 * older ONF Version)
 */
public class Onf14NetworkElement implements NetworkElement {

    private static final Logger log = LoggerFactory.getLogger(Onf14NetworkElement.class);

    protected static final InstanceIdentifier<ControlConstruct> CONTROLCONSTRUCT_IID =
            InstanceIdentifier.builder(ControlConstruct.class).build();

    private static final int EQUIPMENTROOTLEVEL = 0;

    private final NetconfBindingAccessor netconfAccessor;
    private final DataProvider databaseService;
    private final Onf14ToInternalDataModel onf14Mapper;
    private final @NonNull FaultService faultService;

    // for storing the Equipment UUIDs that are inserted in the DB
    private final List<String> equipmentUuidList = new ArrayList<String>();

    // air interface related members
    private final List<TechnologySpecificPacKeys> airInterfaceList = new ArrayList<TechnologySpecificPacKeys>();
    @SuppressWarnings("unused")
    private ListenerRegistration<NotificationListener> airInterfaceNotificationListenerHandler;
    private @NonNull final Onf14AirInterfaceNotificationListener airInterfaceNotificationListener;

    // ethernet container related members
    private final List<TechnologySpecificPacKeys> ethernetContainerList = new ArrayList<TechnologySpecificPacKeys>();
    @SuppressWarnings("unused")
    private ListenerRegistration<NotificationListener> etherneContainerNotificationListenerHandler;
    private @NonNull final Onf14EthernetContainerNotificationListener ethernetContainerNotificationListener;

    // wire interface related members
    private final List<TechnologySpecificPacKeys> wireInterfaceList = new ArrayList<TechnologySpecificPacKeys>();
    @SuppressWarnings("unused")
    private ListenerRegistration<NotificationListener> wireInterfaceNotificationListenerHandler;
    private @NonNull final Onf14WireInterfaceNotificationListener wireInterfaceNotificationListener;

    Onf14NetworkElement(NetconfBindingAccessor netconfAccess, DeviceManagerServiceProvider serviceProvider) {
        log.info("Create {}", Onf14NetworkElement.class.getSimpleName());
        this.netconfAccessor = netconfAccess;
        this.databaseService = serviceProvider.getDataProvider();
        this.faultService = serviceProvider.getFaultService();
        this.onf14Mapper = new Onf14ToInternalDataModel();
        this.airInterfaceNotificationListenerHandler = null;
        this.airInterfaceNotificationListener = new Onf14AirInterfaceNotificationListener(netconfAccess, serviceProvider);
        this.etherneContainerNotificationListenerHandler = null;
        ethernetContainerNotificationListener =
                new Onf14EthernetContainerNotificationListener(netconfAccess, serviceProvider);
        this.wireInterfaceNotificationListenerHandler = null;
        wireInterfaceNotificationListener = new Onf14WireInterfaceNotificationListener(netconfAccess, serviceProvider);
    }

    public void initialReadFromNetworkElement() {

        // reading the inventory (CoreModel 1.4 Equipment Model) and adding it to the DB
        readEquipmentData();

        FaultData resultList = new FaultData();

        int problems = faultService.removeAllCurrentProblemsOfNode(netconfAccessor.getNodeId());
        log.debug("Removed all {} problems from database at registration", problems);

        readAllAirInterfaceCurrentProblems(resultList);
        problems = resultList.size();
        log.debug("NETCONF read air interface current problems completed. Got back {} problems.", problems);

        readAllEhernetContainerCurrentProblems(resultList);
        problems = resultList.size() - problems;
        log.debug("NETCONF read current problems completed. Got back {} problems.", resultList.size());

        readAllWireInterfaceCurrentProblems(resultList);
        problems = resultList.size();
        log.debug("NETCONF read wire interface current problems completed. Got back {} problems.", problems);

        faultService.initCurrentProblemStatus(netconfAccessor.getNodeId(), resultList);
        log.debug("DB write current problems completed");

    }

    public void readAllAirInterfaceCurrentProblems(FaultData resultList) {

        int idxStart; // Start index for debug messages

        for (TechnologySpecificPacKeys key : airInterfaceList) {
            idxStart = resultList.size();

            readAirInterfaceCurrentProblemForLtp(key.getLtpUuid(), key.getLocalId(), resultList);
            debugResultList(key.getLtpUuid().getValue(), resultList, idxStart);
        }
    }

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
    }

    /**
     * @param nNode
     * set core-model-capability
     */
    public void setCoreModel(@NonNull NetconfNode nNode) {
        NetworkElementConnectionBuilder eb = new NetworkElementConnectionBuilder();
        log.info("In setCoreModel for Onf14NetworkElement");
        String namespaceRevision;
        QName QNAME_COREMODEL14 = QName.create("urn:onf:yang:core-model-1-4", "2019-11-27", "core-model-1-4").intern();

        Capabilities availableCapabilities = Capabilities.getAvailableCapabilities(nNode);
        namespaceRevision = availableCapabilities.getRevisionForNamespace(QNAME_COREMODEL14);
        log.info("In setCoreModel for Onf14NetworkElement- namespaceRevision = "+namespaceRevision);
        if (Capabilities.isNamespaceSupported(namespaceRevision)) {
            eb.setCoreModelCapability(namespaceRevision);
        } else {
            eb.setCoreModelCapability("Unsupported");
        }
        databaseService.updateNetworkConnection22(eb.build(), netconfAccessor.getNodeId().getValue());
    }

    @Override
    public void register() {
        // Set core-model revision value in "core-model-capability" field
        setCoreModel(netconfAccessor.getNetconfNode());
        initialReadFromNetworkElement();

        // Register netconf stream
        airInterfaceNotificationListenerHandler =
                netconfAccessor.doRegisterNotificationListener(airInterfaceNotificationListener);
        etherneContainerNotificationListenerHandler =
                netconfAccessor.doRegisterNotificationListener(ethernetContainerNotificationListener);
        wireInterfaceNotificationListenerHandler =
                netconfAccessor.doRegisterNotificationListener(wireInterfaceNotificationListener);
        Optional<NetconfNotifications> notificationsSupport = netconfAccessor.getNotificationAccessor();
        if (notificationsSupport.isPresent()) {
            notificationsSupport.get().registerNotificationsStream(NetconfBindingAccessor.DefaultNotificationsStream);
        }
    }

    @Override
    public void deregister() {}


    @Override
    public NodeId getNodeId() {
        return netconfAccessor.getNodeId();
    }

    @Override
    public <L extends NetworkElementService> Optional<L> getService(Class<L> clazz) {
        return Optional.empty();
    }

    @Override
    public void warmstart() {}

    @Override
    public Optional<NetconfAccessor> getAcessor() {
        return Optional.of(netconfAccessor);
    }

    @Override
    public NetworkElementDeviceType getDeviceType() {
        return NetworkElementDeviceType.Wireless;
    }

    private void readEquipmentData() {

        Optional<ControlConstruct> controlConstruct = readControlConstruct(netconfAccessor);

        if (controlConstruct.isPresent()) {
            // the top-level-equipment list contains the root objects of the Equipment Model
            log.debug("Getting list of topLevelEquipment for mountpoint {}", netconfAccessor.getNodeId());
            @Nullable
            List<UniversalId> topLevelEquipment = controlConstruct.get().getTopLevelEquipment();

            if (topLevelEquipment != null) {
                for (UniversalId uuid : topLevelEquipment) {
                    log.debug("Got back topLevelEquipment with uuid {}", uuid.getValue());

                    // adding all root Equipment objects to the DB
                    @Nullable
                    Equipment equipmentInstance = readEquipmentInstance(netconfAccessor, uuid);
                    if (equipmentInstance != null) {
                        // recursively adding the root equipment and all its children into the DB
                        addEquipmentToDb(equipmentInstance, null, EQUIPMENTROOTLEVEL);
                    }
                }
            }
        }

        // storing all the LTP UUIDs internally, for later usage, for air-interface and ethernet-container
        readKeys(controlConstruct);
    }

    private void addEquipmentToDb(Equipment currentEq, Equipment parentEq, long treeLevel) {

        // if the Equipment UUID is already in the list, it was already processed
        // needed for solving possible circular dependencies
        if (equipmentUuidList.contains(currentEq.getUuid().getValue())) {
            log.debug("Not adding equipment with uuid {} because it was aleady added...",
                    currentEq.getUuid().getValue());
            return;
        }

        // we add this to our internal list, such that we avoid circular dependencies
        equipmentUuidList.add(currentEq.getUuid().getValue());
        log.debug("Adding equipment with uuid {} to the database...", currentEq.getUuid().getValue());

        // we add our current equipment to the database
        databaseService.writeInventory(
                onf14Mapper.getInternalEquipment(netconfAccessor.getNodeId(), currentEq, parentEq, treeLevel));

        // we iterate the kids of our current equipment and add them to the database recursively
        // the actual reference is here: /core-model:control-construct/equipment/contained-holder/occupying-fru
        @NonNull
        Collection<ContainedHolder> holderList = YangHelper.getCollection(currentEq.nonnullContainedHolder());

        for (ContainedHolder holder : holderList) {
            @Nullable
            UniversalId occupyingFru = holder.getOccupyingFru();
            if (occupyingFru != null) {
                @Nullable
                Equipment childEq = readEquipmentInstance(netconfAccessor, occupyingFru);

                if (childEq != null) {
                    // current becomes parent and tree level increases by 1
                    addEquipmentToDb(childEq, currentEq, treeLevel + 1);
                }
            }
        }
    }

    private void readKeys(Optional<ControlConstruct> controlConstruct) {

        if (controlConstruct.isPresent()) {
            @NonNull
            Collection<LogicalTerminationPoint> ltpList = YangHelper.getCollection(controlConstruct.get().nonnullLogicalTerminationPoint());
            log.debug("Iterating the LTP list for node {}", netconfAccessor.getNodeId().getValue());

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
                    if (layerProtocolName.getTypeName() == LAYERPROTOCOLNAMETYPEAIRLAYER.class.getName()) {
                        TechnologySpecificPacKeys airInterfaceKey =
                                new TechnologySpecificPacKeys(ltp.getUuid(), lp.getLocalId());
                        airInterfaceList.add(airInterfaceKey);
                        log.debug("Adding Ltp with uuid {} and local-id {} to the air-interface list",
                                ltp.getUuid().getValue(), lp.getLocalId());
                    }
                    // if the LTP has an ethernetContainier technology extension, the layer protocol name is ethernet-container-layer
                    else if (layerProtocolName.getTypeName() == LAYERPROTOCOLNAMETYPEETHERNETCONTAINERLAYER.class
                            .getName()) {
                        TechnologySpecificPacKeys ethernetContainerKey =
                                new TechnologySpecificPacKeys(ltp.getUuid(), lp.getLocalId());
                        ethernetContainerList.add(ethernetContainerKey);
                        log.debug("Adding Ltp with uuid {} and local-id {} to the ethernet-contatinier list",
                                ltp.getUuid().getValue(), lp.getLocalId());
                    } else if (layerProtocolName.getTypeName() == LAYERPROTOCOLNAMETYPEWIRELAYER.class.getName()) {
                        TechnologySpecificPacKeys wireInterfaceKey =
                                new TechnologySpecificPacKeys(ltp.getUuid(), lp.getLocalId());
                        wireInterfaceList.add(wireInterfaceKey);
                        log.debug("Adding Ltp with uuid {} and local-id {} to the wire-interface list",
                                ltp.getUuid().getValue(), lp.getLocalId());
                    }
                }
            }
        }
    }

    private void readAirInterfaceCurrentProblemForLtp(UniversalId ltpUuid, String localId, FaultData resultList) {

        final Class<AirInterfacePac> clazzPac = AirInterfacePac.class;

        log.info("DBRead Get current problems for class {} from mountpoint {} for LTP uuid {} and local-id {}",
                clazzPac.getSimpleName(), netconfAccessor.getNodeId().getValue(), ltpUuid.getValue(), localId);

        // constructing the IID needs the augmentation exposed byy the air-interface-2-0 model
        InstanceIdentifier<AirInterfaceCurrentProblems> airInterfaceCurrentProblem_IID = InstanceIdentifier
                .builder(ControlConstruct.class)
                .child(LogicalTerminationPoint.class, new LogicalTerminationPointKey(ltpUuid))
                .child(LayerProtocol.class, new LayerProtocolKey(localId))
                .augmentation(
                        org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.LayerProtocol1.class)
                .child(AirInterfacePac.class).child(AirInterfaceCurrentProblems.class).build();

        // reading all the current-problems list for this specific LTP and LP
        AirInterfaceCurrentProblems problems = netconfAccessor.getTransactionUtils().readData(
                netconfAccessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL, airInterfaceCurrentProblem_IID);

        if (problems == null) {
            log.debug("DBRead Id {} no AirInterfaceCurrentProblems", ltpUuid);
        } else if (problems.getCurrentProblemList() == null) {
            log.debug("DBRead Id {} empty CurrentProblemList", ltpUuid);
        } else {
            for (org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.air._interface.current.problems.CurrentProblemList problem : YangHelper.getCollection(problems
                    .nonnullCurrentProblemList())) {
                resultList.add(netconfAccessor.getNodeId(), (int) problem.getSequenceNumber(), problem.getTimestamp(),
                        ltpUuid.getValue(), problem.getProblemName(),
                        Onf14AirInterface.mapSeverity(problem.getProblemSeverity()));
            }
        }
    }

    private void readEthernetConainerCurrentProblemForLtp(UniversalId ltpUuid, String localId, FaultData resultList) {

        final Class<EthernetContainerPac> clazzPac = EthernetContainerPac.class;

        log.info("DBRead Get current problems for class {} from mountpoint {} for LTP uuid {} and local-id {}",
                clazzPac.getSimpleName(), netconfAccessor.getNodeId().getValue(), ltpUuid.getValue(), localId);

        // constructing the IID needs the augmentation exposed by the ethernet-container-2-0 model
        InstanceIdentifier<EthernetContainerCurrentProblems> etherneContainerCurrentProblem_IID = InstanceIdentifier
                .builder(ControlConstruct.class)
                .child(LogicalTerminationPoint.class, new LogicalTerminationPointKey(ltpUuid))
                .child(LayerProtocol.class, new LayerProtocolKey(localId))
                .augmentation(
                        org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.LayerProtocol1.class)
                .child(EthernetContainerPac.class).child(EthernetContainerCurrentProblems.class).build();

        // reading all the current-problems list for this specific LTP and LP
        EthernetContainerCurrentProblems problems = netconfAccessor.getTransactionUtils().readData(
                netconfAccessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL, etherneContainerCurrentProblem_IID);

        if (problems == null) {
            log.debug("DBRead Id {} no EthernetContainerCurrentProblems", ltpUuid);
        } else if (problems.getCurrentProblemList() == null) {
            log.debug("DBRead Id {} empty CurrentProblemList", ltpUuid);
        } else {
            for (org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.ethernet.container.current.problems.CurrentProblemList problem : YangHelper.getCollection(problems
                    .nonnullCurrentProblemList())) {
                resultList.add(netconfAccessor.getNodeId(), (int) problem.getSequenceNumber(), problem.getTimestamp(),
                        ltpUuid.getValue(), problem.getProblemName(),
                        Onf14EthernetContainer.mapSeverity(problem.getProblemSeverity()));
            }
        }
    }

    private void readWireInterfaceCurrentProblemForLtp(UniversalId ltpUuid, String localId, FaultData resultList) {

        final Class<WireInterfacePac> clazzPac = WireInterfacePac.class;

        log.info("DBRead Get current problems for class {} from mountpoint {} for LTP uuid {} and local-id {}",
                clazzPac.getSimpleName(), netconfAccessor.getNodeId().getValue(), ltpUuid.getValue(), localId);

        // constructing the IID needs the augmentation exposed by the wire-interface-2-0 model
        InstanceIdentifier<WireInterfaceCurrentProblems> wireInterfaceCurrentProblem_IID = InstanceIdentifier
                .builder(ControlConstruct.class)
                .child(LogicalTerminationPoint.class, new LogicalTerminationPointKey(ltpUuid))
                .child(LayerProtocol.class, new LayerProtocolKey(localId))
                .augmentation(
                        org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.LayerProtocol1.class)
                .child(WireInterfacePac.class).child(WireInterfaceCurrentProblems.class).build();

        // reading all the current-problems list for this specific LTP and LP
        WireInterfaceCurrentProblems problems = netconfAccessor.getTransactionUtils().readData(
                netconfAccessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL, wireInterfaceCurrentProblem_IID);

        if (problems == null) {
            log.debug("DBRead Id {} no WireInterfaceCurrentProblems", ltpUuid);
        } else if (problems.getCurrentProblemList() == null) {
            log.debug("DBRead Id {} empty CurrentProblemList", ltpUuid);
        } else {
            for (org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.wire._interface.current.problems.CurrentProblemList problem : YangHelper.getCollection(problems
                    .nonnullCurrentProblemList())) {
                resultList.add(netconfAccessor.getNodeId(), (int) problem.getSequenceNumber(), problem.getTimestamp(),
                        ltpUuid.getValue(), problem.getProblemName(),
                        Onf14WireInterface.mapSeverity(problem.getProblemSeverity()));
            }
        }
    }

    private Optional<ControlConstruct> readControlConstruct(NetconfBindingAccessor netconfAccessor) {
        return Optional.ofNullable(netconfAccessor.getTransactionUtils().readData(netconfAccessor.getDataBroker(),
                LogicalDatastoreType.CONFIGURATION, CONTROLCONSTRUCT_IID));
    }

    private @Nullable Equipment readEquipmentInstance(NetconfBindingAccessor accessData, UniversalId equipmentUuid) {

        final Class<?> clazzPac = Equipment.class;

        log.info("DBRead Get equipment for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                accessData.getNodeId().getValue(), equipmentUuid.getValue());

        InstanceIdentifier<Equipment> equipmentIID = InstanceIdentifier.builder(ControlConstruct.class)
                .child(Equipment.class, new EquipmentKey(equipmentUuid)).build();

        return accessData.getTransactionUtils().readData(accessData.getDataBroker(), LogicalDatastoreType.CONFIGURATION,
                equipmentIID);
    }

    // defining a structure that can map the LP local-id and its corresponding LTP uuid
    private class TechnologySpecificPacKeys {

        private UniversalId ltpUuid;
        private String localId;

        public TechnologySpecificPacKeys(UniversalId uuid, String lId) {
            this.ltpUuid = uuid;
            this.localId = lId;
        }

        public UniversalId getLtpUuid() {
            return ltpUuid;
        }

        public String getLocalId() {
            return localId;
        }

        @SuppressWarnings("unused")
        public void setLtpUuid(UniversalId uuid) {
            this.ltpUuid = uuid;
        }

        @SuppressWarnings("unused")
        public void setLocalId(String lId) {
            this.localId = lId;
        }
    }


    /**
     * LOG the newly added problems of the interface pac
     *
     * @param idxStart
     * @param uuid
     * @param resultList
     */
    private void debugResultList(String uuid, FaultData resultList, int idxStart) {
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (int t = idxStart; t < resultList.size(); t++) {
            sb.append(idx++);
            sb.append(":{");
            sb.append(resultList.get(t));
            sb.append('}');
        }
        log.debug("Found problems {} {}", uuid, sb);
    }

}
