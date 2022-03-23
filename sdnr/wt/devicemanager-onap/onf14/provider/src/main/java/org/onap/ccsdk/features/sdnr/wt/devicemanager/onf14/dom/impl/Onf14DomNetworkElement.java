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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl;

import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.dataprovider.Onf14DomToInternalDataModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.equipment.Onf14DomEquipmentManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.interfaces.Onf14DomInterfacePacManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DevicemanagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.Stream;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.StreamKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of ONF Core model 1.4 device Top level element is "ControlConstruct" (replaces "NetworkElement" of
 * older ONF Version) NOTE:
 */
public class Onf14DomNetworkElement implements NetworkElement {

    private static final Logger log = LoggerFactory.getLogger(Onf14DomNetworkElement.class);

    protected static final YangInstanceIdentifier TOPLEVELEQUIPMENT_IID =
            YangInstanceIdentifier.builder().node(Onf14DevicemanagerQNames.CORE_MODEL_CONTROL_CONSTRUCT_CONTAINER)
                    .node(Onf14DevicemanagerQNames.CORE_MODEL_CC_TOP_LEVEL_EQPT).build();

    private final NetconfDomAccessor netconfDomAccessor;
    private final DataProvider databaseService;
    private final @NonNull FaultService faultService;
    private final @NonNull NotificationService notificationService;

    private final Onf14DomToInternalDataModel onf14Mapper;

    private final @NonNull Onf14DomEquipmentManager equipmentManager;
    private final @NonNull Onf14DomInterfacePacManager interfacePacManager;
    private final @NonNull String namespaceRevision;

    public Onf14DomNetworkElement(NetconfDomAccessor netconfDomAccessor, DeviceManagerServiceProvider serviceProvider,
            String namespaceRevision) {
        log.info("Create {}", Onf14DomNetworkElement.class.getSimpleName());
        this.netconfDomAccessor = netconfDomAccessor;
        this.databaseService = serviceProvider.getDataProvider();
        this.notificationService = serviceProvider.getNotificationService();
        this.faultService = serviceProvider.getFaultService();
        this.namespaceRevision = namespaceRevision;
        this.onf14Mapper = new Onf14DomToInternalDataModel();
        this.equipmentManager = new Onf14DomEquipmentManager(netconfDomAccessor, databaseService, onf14Mapper);
        this.interfacePacManager = new Onf14DomInterfacePacManager(netconfDomAccessor, serviceProvider);

    }

    /**
     * reading the inventory (CoreModel 1.4 Equipment Model) and adding it to the DB
     */
    public void initialReadFromNetworkElement() {
        log.info("Calling read equipment");
        // Read complete device tree
        readEquipmentData();

        // Read fault data and subscribe for notifications
        interfacePacManager.register();

        int problems = faultService.removeAllCurrentProblemsOfNode(netconfDomAccessor.getNodeId());
        log.debug("Removed all {} problems from database at registration", problems);

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

    private void readEquipmentData() {
        Optional<NormalizedNode> topLevelEquipment = readTopLevelEquipment(netconfDomAccessor);
        log.info("Top level equipment data is {}", topLevelEquipment.isPresent() ? topLevelEquipment.get() : null);
        if (topLevelEquipment.isPresent()) {
            equipmentManager.setEquipmentData(topLevelEquipment.get());
        }

    }

    private Optional<NormalizedNode> readTopLevelEquipment(NetconfDomAccessor netconfDomAccessor) {
        log.info("Reading Top level equipment data");
        return netconfDomAccessor.readDataNode(LogicalDatastoreType.CONFIGURATION, TOPLEVELEQUIPMENT_IID);
    }

}
