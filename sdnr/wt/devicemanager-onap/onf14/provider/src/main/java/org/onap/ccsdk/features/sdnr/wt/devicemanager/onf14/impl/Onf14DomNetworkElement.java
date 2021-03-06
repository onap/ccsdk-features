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

import java.util.List;
import java.util.Map;
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
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.Stream;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.StreamKey;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.ControlConstruct;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.Equipment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of ONF Core model 1.4 device Top level element is "ControlConstruct" (replaces "NetworkElement" of
 * older ONF Version) NOTE: This class is still under development due to unmet dependencies (especially the ones related
 * to DOM notifications) in ODL. Once the dependencies are complete, this class will replace the ONF14NetworkElement
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
    private final @NonNull String namespaceRevision;

    private boolean experimental;


    public Onf14DomNetworkElement(NetconfDomAccessor netconfDomAccessor, DeviceManagerServiceProvider serviceProvider,
            String namespaceRevision) {
        log.info("Create {}", Onf14DomNetworkElement.class.getSimpleName());
        this.netconfDomAccessor = netconfDomAccessor;
        this.databaseService = serviceProvider.getDataProvider();
        this.notificationService = serviceProvider.getNotificationService();
        this.faultService = serviceProvider.getFaultService();
        this.namespaceRevision = namespaceRevision;
        this.onf14Mapper = new Onf14ToInternalDataModel();
        this.equipmentManager = new Onf14DomEquipmentManager(netconfDomAccessor, databaseService, onf14Mapper);

        this.interfacePacManager = new Onf14DomInterfacePacManager(netconfDomAccessor, serviceProvider);
        this.experimental = false;
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

            //-- Start for experimental purpose
            if (experimental) {
                log.warn("Experimental code activated");
                for (UniversalId uuid : equipmentManager.getEquipmentUuidList()) {
                    log.info("Read data with id {}", uuid);
                    Optional<Equipment> res1 = equipmentManager.readEquipmentInstance(netconfDomAccessor, uuid);
                    log.info("Res1: {}", res1.isPresent() ? res1.get() : "No data1");

                    /*List<DataObject> res2 = equipmentManager.readEquipmentList(netconfDomAccessor);
                    log.info("Res2: {}", res2.isPresent() ? res2.get() : "No data2");*/

                    equipmentManager.readTopLevelEquipment(netconfDomAccessor);
                    //Do it only once for test purpose and break
                    break;
                }
                List<DataObject> res2 = equipmentManager.readEquipmentList(netconfDomAccessor);
                //log.info("Res2: {}", res2.isPresent() ? res2.get() : "No data2");
                for (DataObject dobj : res2) {
                    Equipment eqpt = (Equipment) dobj;
                    log.info("Equipment local ID is : {}", eqpt.getLocalId());
                }

                equipmentManager.readTopLevelEquipment(netconfDomAccessor);
            }
            //-- End for experimental purpose

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
    public void setCoreModel() {
        NetworkElementConnectionBuilder eb = new NetworkElementConnectionBuilder();
        eb.setCoreModelCapability(namespaceRevision);
        databaseService.updateNetworkConnection22(eb.build(), netconfDomAccessor.getNodeId().getValue());
    }

    @Override
    public void register() {
        // Set core-model revision value in "core-model-capability" field
        setCoreModel();
        initialReadFromNetworkElement();

        if (netconfDomAccessor.isNotificationsRFC5277Supported()) {
            // register listener
            interfacePacManager.subscribeNotifications();
            // Output notification streams to LOG
            Map<StreamKey, Stream> streams = netconfDomAccessor.getNotificationStreamsAsMap();
            log.info("Available notifications streams: {}", streams);
            // Register to default stream
            netconfDomAccessor.invokeCreateSubscription();
        }
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
        return netconfDomAccessor.readData(LogicalDatastoreType.CONFIGURATION, CONTROLCONSTRUCT_IID,
                ControlConstruct.class);
    }



}
