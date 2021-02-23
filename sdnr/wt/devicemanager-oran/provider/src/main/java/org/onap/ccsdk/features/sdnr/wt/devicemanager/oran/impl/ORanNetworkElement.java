/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNotifications;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.Stream;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.Hardware;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.Component;
import org.opendaylight.yang.gen.v1.urn.onap.system.rev201026.System1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GuicutthroughBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class ORanNetworkElement implements NetworkElement {

    private static final Logger log = LoggerFactory.getLogger(ORanNetworkElement.class);

    private final NetconfBindingAccessor netconfAccessor;

    private final DataProvider databaseService;

    @SuppressWarnings("unused")
    private final VESCollectorService vesCollectorService;

    private final ORanToInternalDataModel oRanMapper;

    private ListenerRegistration<NotificationListener> oRanListenerRegistrationResult;
    private @NonNull final ORanChangeNotificationListener oRanListener;
    private ListenerRegistration<NotificationListener> oRanFaultListenerRegistrationResult;
    private @NonNull final ORanFaultNotificationListener oRanFaultListener;

    ORanNetworkElement(NetconfBindingAccessor netconfAccess, DataProvider databaseService,
            VESCollectorService vesCollectorService) {
        log.info("Create {}", ORanNetworkElement.class.getSimpleName());
        this.netconfAccessor = netconfAccess;
        this.databaseService = databaseService;
        this.vesCollectorService = vesCollectorService;

        this.oRanListenerRegistrationResult = null;
        this.oRanListener = new ORanChangeNotificationListener(netconfAccessor, databaseService, vesCollectorService);

        this.oRanFaultListenerRegistrationResult = null;
        this.oRanFaultListener = new ORanFaultNotificationListener();

        this.oRanMapper = new ORanToInternalDataModel();

    }

    public void initialReadFromNetworkElement() {
        Hardware hardware = readHardware(netconfAccessor);
        if (hardware != null) {
            Collection<Component> componentList = YangHelper.getCollection(hardware.getComponent());
            if (componentList != null) {
                int componentListSize = componentList.size();
                int writeCount = 0;

                for (Component component : componentList) {
                    if (component.getParent() == null) {
                        writeCount += writeInventory(component, componentList, 0);
                    }
                }
                if (componentListSize != writeCount) {
                    log.warn("Not all data were written to the Inventory. Potential entries with missing "
                            + "contained-child. Node Id = {}, Components Found = {}, Entries written to Database = {}",
                            netconfAccessor.getNodeId().getValue(), componentListSize, writeCount);
                }
            }
        }

        System1 sys = getOnapSystemData(netconfAccessor);
        if (sys != null) {
            GuicutthroughBuilder gcBuilder = new GuicutthroughBuilder();
            gcBuilder.setId(sys.getName()).setName(sys.getName()).setWeburi(sys.getWebUi().getValue());
            databaseService.writeGuiCutThroughData(gcBuilder.build());
        }
    }

    private int writeInventory(Component component, Collection<Component> componentList, int treeLevel) {
        databaseService
                .writeInventory(oRanMapper.getInternalEquipment(netconfAccessor.getNodeId(), component, treeLevel));
        int count = 1;
        if (component.getContainsChild() != null) {
            List<String> containerHolderList = component.getContainsChild();
            for (String containerHolder : containerHolderList) {
                for (Component c : componentList) {
                    if (containerHolder.equals(c.getName())) {
                        count += writeInventory(c, componentList, treeLevel + 1);
                    }
                }
            }
        }
        return count;
    }

    @Override
    public NetworkElementDeviceType getDeviceType() {
        return NetworkElementDeviceType.ORAN;
    }

    private System1 getOnapSystemData(NetconfBindingAccessor accessData) {
        InstanceIdentifier<System1> system1IID = InstanceIdentifier
                .builder(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.system.rev140806.System.class)
                .augmentation(System1.class).build();

        System1 res = accessData.getTransactionUtils().readData(accessData.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, system1IID);
        log.debug("Result of getOnapSystemData = {}", res);
        return res;
    }

    private Hardware readHardware(NetconfBindingAccessor accessData) {

        final Class<Hardware> clazzPac = Hardware.class;

        log.info("DBRead Get equipment for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                accessData.getNodeId().getValue());

        InstanceIdentifier<Hardware> hardwareIID = InstanceIdentifier.builder(clazzPac).build();

        Hardware res = accessData.getTransactionUtils().readData(accessData.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, hardwareIID);

        return res;
    }

    @Override
    public void register() {

        initialReadFromNetworkElement();
        // Register call back class for receiving notifications
        Optional<NetconfNotifications> oNotifications = netconfAccessor.getNotificationAccessor();
        if (oNotifications.isPresent()) {
            NetconfNotifications notifications = oNotifications.get();
            this.oRanListenerRegistrationResult = netconfAccessor.doRegisterNotificationListener(oRanListener);
            this.oRanFaultListenerRegistrationResult =
                    netconfAccessor.doRegisterNotificationListener(oRanFaultListener);
            // Register notifications stream
            if (notifications.isNCNotificationsSupported()) {
                List<Stream> streamList = notifications.getNotificationStreams();
                notifications.registerNotificationsStream(NetconfBindingAccessor.DefaultNotificationsStream); // Always register first to default stream
                notifications.registerNotificationsStream(streamList);
            } else {
                notifications.registerNotificationsStream(NetconfBindingAccessor.DefaultNotificationsStream);
            }
        }
    }

    @Override
    public void deregister() {
        if (oRanListenerRegistrationResult != null) {
            this.oRanListenerRegistrationResult.close();
        }
        if (oRanFaultListenerRegistrationResult != null) {
            this.oRanFaultListenerRegistrationResult.close();
        } ;
    }


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

}
