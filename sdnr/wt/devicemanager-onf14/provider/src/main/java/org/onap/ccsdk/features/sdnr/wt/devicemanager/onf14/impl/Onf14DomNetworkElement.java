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

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.dataprovider.Onf14ToInternalDataModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.equipment.Onf14DomEquipmentManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.interfaces.Onf14DomInterfacePacManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.ControlConstruct;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.Equipment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of ONF Core model 1.4 device Top level element is "ControlConstruct" (replaces "NetworkElement" of
 * older ONF Version)
 * NOTE: This class is still under development due to unmet dependencies (especially the ones related to DOM notifications) in ODL. Once the dependencies are complete, this class will replace the ONF14NetworkElement   
 */
public class Onf14DomNetworkElement implements NetworkElement {

    private static final Logger log = LoggerFactory.getLogger(Onf14DomNetworkElement.class);

    //    protected static final InstanceIdentifier<ControlConstruct> CONTROLCONSTRUCT_IID =
    //            InstanceIdentifier.builder(ControlConstruct.class).build();
    protected static final YangInstanceIdentifier CONTROLCONSTRUCT_IID =
            YangInstanceIdentifier.builder().node(ControlConstruct.QNAME).build();

    private final NetconfDomAccessor netconfDomAccessor;
    private final DataProvider databaseService;
    private final @NonNull FaultService faultService;
    private final @NonNull NotificationService notificationService;

    private final Onf14ToInternalDataModel onf14Mapper;

    private final @NonNull Onf14DomEquipmentManager equipmentManager;
    private final @NonNull Onf14DomInterfacePacManager interfacePacManager;


    public Onf14DomNetworkElement(NetconfDomAccessor netconfDomAccessor, DeviceManagerServiceProvider serviceProvider) {
        log.info("Create {}", Onf14DomNetworkElement.class.getSimpleName());
        this.netconfDomAccessor = netconfDomAccessor;
        this.databaseService = serviceProvider.getDataProvider();
        this.notificationService = serviceProvider.getNotificationService();
        this.faultService = serviceProvider.getFaultService();
        this.onf14Mapper = new Onf14ToInternalDataModel();
        this.equipmentManager = new Onf14DomEquipmentManager(netconfDomAccessor, databaseService, onf14Mapper);
        this.interfacePacManager = new Onf14DomInterfacePacManager(netconfDomAccessor, serviceProvider);
    }

    /**
     * reading the inventory (CoreModel 1.4 Equipment Model) and adding it to the DB
     */
    public void initialReadFromNetworkElement() {

        //Read complete device tree
        Optional<ControlConstruct> oControlConstruct = readControlConstruct(netconfDomAccessor);

        if (oControlConstruct.isPresent()) {
            ControlConstruct controlConstruct = oControlConstruct.get();

            equipmentManager.setEquipmentData(controlConstruct);

            //-- Start For test purpose
            for (UniversalId uuid : equipmentManager.getEquipmentUuidList()) {
                log.info("Read data with id {}", uuid);
                Optional<Equipment> res1 = equipmentManager.readEquipmentInstance(netconfDomAccessor, uuid);
                log.info("Res1: {}", res1.isPresent() ? res1.get() : "No data1");

                Optional<ControlConstruct> res2 = equipmentManager.readEquipmentList(netconfDomAccessor, uuid);
                log.info("Res2: {}", res2.isPresent() ? res2.get() : "No data2");

                equipmentManager.readTopLevelEquipment(netconfDomAccessor);
                //Do it only once for test purpose
                break;
            }
            //-- End For test purpose

            // storing all the LTP UUIDs internally, for later usage, for air-interface and ethernet-container
            interfacePacManager.readKeys(controlConstruct);

            // Read all fault data
            FaultData resultList = new FaultData();

            int problems = faultService.removeAllCurrentProblemsOfNode(netconfDomAccessor.getNodeId());
            log.debug("Removed all {} problems from database at registration", problems);

            interfacePacManager.readAllAirInterfaceCurrentProblems(netconfDomAccessor, controlConstruct, resultList);
            problems = resultList.size();
            log.debug("NETCONF read air interface current problems completed. Got back {} problems.", problems);
            /*
            readAllEhernetContainerCurrentProblems(resultList);
            problems = resultList.size() - problems;
            log.debug("NETCONF read current problems completed. Got back {} problems.", resultList.size());

            readAllWireInterfaceCurrentProblems(resultList);
            problems = resultList.size();
            log.debug("NETCONF read wire interface current problems completed. Got back {} problems.", problems);
            */
            faultService.initCurrentProblemStatus(netconfDomAccessor.getNodeId(), resultList);
            log.debug("DB write current problems completed");
        }
    }

    /**
     * @param nNode set core-model-capability
     */
    public void setCoreModel(@NonNull NetconfNode nNode) {
        NetworkElementConnectionBuilder eb = new NetworkElementConnectionBuilder();
        String namespaceRevision;
        QName QNAME_COREMODEL14 = QName.create("urn:onf:yang:core-model-1-4", "2019-11-27", "core-model-1-4").intern();

        Capabilities availableCapabilities = Capabilities.getAvailableCapabilities(nNode);
        namespaceRevision = availableCapabilities.getRevisionForNamespace(QNAME_COREMODEL14);

        if (Capabilities.isNamespaceSupported(namespaceRevision)) {
            eb.setCoreModelCapability(namespaceRevision);
        } else {
            eb.setCoreModelCapability("Unsupported");
        }
        databaseService.updateNetworkConnection22(eb.build(), netconfDomAccessor.getNodeId().getValue());
    }

    @Override
    public void register() {
        // Set core-model revision value in "core-model-capability" field
        setCoreModel(netconfDomAccessor.getNetconfNode());
        initialReadFromNetworkElement();

        // Register netconf stream
        //        airInterfaceNotificationListenerHandler =
        //                netconfDomAccessor.doRegisterNotificationListener(airInterfaceNotificationListener);
        //        etherneContainerNotificationListenerHandler =
        //                netconfDomAccessor.doRegisterNotificationListener(ethernetContainerNotificationListener);
        //        wireInterfaceNotificationListenerHandler =
        //                netconfDomAccessor.doRegisterNotificationListener(wireInterfaceNotificationListener);
        //        Optional<NetconfNotifications> notificationsSupport = netconfDomAccessor.getNotificationAccessor();
        //        if (notificationsSupport.isPresent()) {
        //            notificationsSupport.get().registerNotificationsStream(NetconfBindingAccessor.DefaultNotificationsStream);
        //        }
    }

    @Override
    public void deregister() {}


    @Override
    public NodeId getNodeId() {
        return netconfDomAccessor.getNodeId();
    }

    @Override
    public <L extends NetworkElementService> Optional<L> getService(Class<L> clazz) {
        return Optional.empty();
    }

    @Override
    public void warmstart() {}

    @Override
    public Optional<NetconfAccessor> getAcessor() {
        return Optional.of(netconfDomAccessor);
    }

    @Override
    public NetworkElementDeviceType getDeviceType() {
        return NetworkElementDeviceType.Wireless;
    }

    private static Optional<ControlConstruct> readControlConstruct(NetconfDomAccessor netconfDomAccessor) {
        return netconfDomAccessor.readData(LogicalDatastoreType.CONFIGURATION, CONTROLCONSTRUCT_IID, ControlConstruct.class);
    }



}
