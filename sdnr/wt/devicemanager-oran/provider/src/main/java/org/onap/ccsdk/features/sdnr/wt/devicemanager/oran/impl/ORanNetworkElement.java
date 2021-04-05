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

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationProxyParser;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESPNFRegistrationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNotifications;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.Stream;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.Hardware;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.Component;
import org.opendaylight.yang.gen.v1.urn.onap.system.rev201026.System1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Guicutthrough;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconf.callhome.server.rev161109.NetconfCallhomeServer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconf.callhome.server.rev161109.netconf.callhome.server.AllowedDevices;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconf.callhome.server.rev161109.netconf.callhome.server.allowed.devices.Device;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanNetworkElement implements NetworkElement {

    private static final Logger log = LoggerFactory.getLogger(ORanNetworkElement.class);

    private final NetconfBindingAccessor netconfAccessor;

    private final DataProvider databaseService;

    @SuppressWarnings("unused")
    private final VESCollectorService vesCollectorService;

    private ListenerRegistration<NotificationListener> oRanListenerRegistrationResult;
    private @NonNull final ORanChangeNotificationListener oRanListener;
    private ListenerRegistration<NotificationListener> oRanFaultListenerRegistrationResult;
    private @NonNull final ORanFaultNotificationListener oRanFaultListener;
    private final NotificationProxyParser notificationProxyParser;
    private @NonNull ORanRegistrationToVESpnfRegistrationMapper mapper;
    private Collection<Component> componentList;
    private static int sequenceNo = 0;

    ORanNetworkElement(NetconfBindingAccessor netconfAccess, DataProvider databaseService,
            VESCollectorService vesCollectorService) {
        log.info("Create {}", ORanNetworkElement.class.getSimpleName());
        this.netconfAccessor = netconfAccess;
        this.databaseService = databaseService;
        this.vesCollectorService = vesCollectorService;
        this.notificationProxyParser = vesCollectorService.getNotificationProxyParser();

        this.oRanListenerRegistrationResult = null;
        this.oRanListener = new ORanChangeNotificationListener(netconfAccessor, databaseService, vesCollectorService,
                notificationProxyParser);

        this.oRanFaultListenerRegistrationResult = null;
        this.oRanFaultListener =
                new ORanFaultNotificationListener(netconfAccessor, databaseService, vesCollectorService);
    }

    private void initialReadFromNetworkElement() {
        Hardware hardware = readHardware();
        if (hardware != null) {
            componentList = YangHelper.getCollection(hardware.nonnullComponent());
            List<Inventory> inventoryList =
                    ORanToInternalDataModel.getInventoryList(netconfAccessor.getNodeId(), componentList);
	    inventoryList.forEach(databaseService::writeInventory);
        }

        Optional<Guicutthrough> oGuicutthrough = ORanToInternalDataModel.getGuicutthrough(getOnapSystemData());
        if (oGuicutthrough.isPresent()) {
            databaseService.writeGuiCutThroughData(oGuicutthrough.get(), netconfAccessor.getNodeId().getValue());
        }
    }

    @Override
    public NetworkElementDeviceType getDeviceType() {
        return NetworkElementDeviceType.ORAN;
    }

    @Override
    public void register() {
        initialReadFromNetworkElement();
        // Publish the mountpoint to VES if enabled
        publishMountpointToVES();
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

    // Read from device
    private System1 getOnapSystemData() {
        log.info("Get System1 for class {} from mountpoint {}", netconfAccessor.getNodeId().getValue());

        InstanceIdentifier<System1> system1IID = InstanceIdentifier
                .builder(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.system.rev140806.System.class)
                .augmentation(System1.class).build();
        System1 res = netconfAccessor.getTransactionUtils().readData(netconfAccessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, system1IID);
        log.debug("Result of System1 = {}", res);
        return res;
    }

    private Hardware readHardware() {
        final Class<Hardware> clazzPac = Hardware.class;
        log.info("DBRead Get hardware for class {} from mountpoint {}", clazzPac.getSimpleName(),
                netconfAccessor.getNodeId().getValue());
        InstanceIdentifier<Hardware> hardwareIID = InstanceIdentifier.builder(clazzPac).build();
        Hardware res = netconfAccessor.getTransactionUtils().readData(netconfAccessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, hardwareIID);
        log.debug("Result of Hardware = {}", res);
        return res;
    }

    private void publishMountpointToVES() {
        log.debug("In publishMountpointToVES()");

        /**
         * 1. Check if this device is in the list of allowed-devices.
         * 2. If device exists in allowed-devices, then create VES pnfRegistration event and publish to VES
         */
        if (inAllowedDevices(netconfAccessor.getNodeId().getValue())) {
            if (vesCollectorService.getConfig().isVESCollectorEnabled()) {

                for (Component component : ORanToInternalDataModel.getRootComponents(componentList)) {
                    //Just get one component. At the moment we don't care which one. Also since there is only one management address, we assume there will be only one chassis.
                    //If the device supports subtended configuration then it is assumed that the Chassis containing the management interface will be the root component and there will be only one root.
                    this.mapper = new ORanRegistrationToVESpnfRegistrationMapper(netconfAccessor,
                            vesCollectorService, component);
                    VESCommonEventHeaderPOJO header =
                            mapper.mapCommonEventHeader(sequenceNo++);
                    VESPNFRegistrationFieldsPOJO body = mapper.mapPNFRegistrationFields();
                    try {
                        vesCollectorService.publishVESMessage(vesCollectorService.generateVESEvent(header, body));
                    } catch (JsonProcessingException e) {
                        log.warn("Error while serializing VES Event to String ", e);
                        e.printStackTrace();
                    }

                }
            }

        }

    }

    private boolean inAllowedDevices(String mountpointName) {
        final InstanceIdentifier<AllowedDevices> ALL_DEVICES =
                InstanceIdentifier.create(NetconfCallhomeServer.class).child(AllowedDevices.class);

        AllowedDevices allowedDevices;
        allowedDevices = netconfAccessor.getTransactionUtils().readData(
                netconfAccessor.getControllerBindingDataBroker(), LogicalDatastoreType.CONFIGURATION, ALL_DEVICES);

        if (allowedDevices != null) {
            Collection<Device> deviceList = YangHelper.getCollection(allowedDevices.nonnullDevice());
            for (Device device : deviceList) {
                log.info("Device in allowed-devices is - {}", device.getUniqueId());
                if (device.getUniqueId().equals(netconfAccessor.getNodeId().getValue())) {
                    log.info("Mountpoint is part of allowed-devices list");
                    return true;
                }
            }
        }

        log.info("Mountpoint {} is not part of allowed-devices list", mountpointName);
        return false;
    }

}
