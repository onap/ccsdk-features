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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESPNFRegistrationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.Hardware;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.Component;
import org.opendaylight.yang.gen.v1.urn.onap.system.rev201026.System1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Guicutthrough;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconf.callhome.server.rev201015.NetconfCallhomeServer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconf.callhome.server.rev201015.netconf.callhome.server.AllowedDevices;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconf.callhome.server.rev201015.netconf.callhome.server.allowed.devices.Device;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanNetworkElement implements NetworkElement {

    private static final Logger LOG = LoggerFactory.getLogger(ORanNetworkElement.class);

    public static final QName ONAP_SYSTEM =
            org.opendaylight.yang.gen.v1.urn.onap.system.rev201026.$YangModuleInfoImpl.getInstance().getName();
    private static final InstanceIdentifier<System1> SYSTEM1_IID = InstanceIdentifier
            .builder(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.system.rev140806.System.class)
            .augmentation(System1.class).build();

    private final NetconfBindingAccessor netconfAccessor;
    private final DataProvider databaseService;
    private final ORanRegistrationToVESpnfRegistrationMapper mapper;
    private final VESCollectorService vesCollectorService;

    private ListenerRegistration<NotificationListener> oRanListenerRegistrationResult;
    private @NonNull final ORanChangeNotificationListener oRanListener;
    private ListenerRegistration<NotificationListener> oRanFaultListenerRegistrationResult;
    private @NonNull final ORanFaultNotificationListener oRanFaultListener;

    ORanNetworkElement(NetconfBindingAccessor netconfAccess, DeviceManagerServiceProvider serviceProvider) {
        LOG.info("Create {}", ORanNetworkElement.class.getSimpleName());
        // Read parameters
        this.netconfAccessor = netconfAccess;

        // Get services
        this.databaseService = serviceProvider.getDataProvider();
        this.vesCollectorService = serviceProvider.getVESCollectorService();

        this.mapper = new ORanRegistrationToVESpnfRegistrationMapper(netconfAccessor, vesCollectorService);

        // Register callbacks
        this.oRanListenerRegistrationResult = null;
        this.oRanListener = new ORanChangeNotificationListener(netconfAccessor, serviceProvider);

        this.oRanFaultListenerRegistrationResult = null;
        this.oRanFaultListener = new ORanFaultNotificationListener(netconfAccessor, vesCollectorService,
                serviceProvider.getFaultService(), serviceProvider.getWebsocketService(), databaseService);
    }

    private Collection<Component> initialReadFromNetworkElement() {
        Collection<Component> componentList;
        Hardware hardware = readHardware();
        if (hardware != null) {
            componentList = YangHelper.getCollection(hardware.nonnullComponent());
            List<Inventory> inventoryList =
                    ORanToInternalDataModel.getInventoryList(netconfAccessor.getNodeId(), componentList);
            databaseService.writeInventory(netconfAccessor.getNodeId().getValue(), inventoryList);
        } else {
            componentList = Collections.emptyList();
        }

        Optional<Guicutthrough> oGuicutthrough = ORanToInternalDataModel.getGuicutthrough(getOnapSystemData());
        if (oGuicutthrough.isPresent()) {
            databaseService.writeGuiCutThroughData(oGuicutthrough.get(), netconfAccessor.getNodeId().getValue());
        }
        return componentList;
    }

    @Override
    public NetworkElementDeviceType getDeviceType() {
        return NetworkElementDeviceType.ORAN;
    }

    @Override
    public void register() {
        // Read data from device
        Collection<Component> componentList = initialReadFromNetworkElement();
        oRanFaultListener.setComponentList(componentList);
        // Publish the mountpoint to VES if enabled
        publishMountpointToVES(componentList);
        // Register call back class for receiving notifications
        this.oRanListenerRegistrationResult = netconfAccessor.doRegisterNotificationListener(oRanListener);
        this.oRanFaultListenerRegistrationResult = netconfAccessor.doRegisterNotificationListener(oRanFaultListener);
        // Register notifications stream
        if (netconfAccessor.isNotificationsRFC5277Supported()) {
            netconfAccessor.registerNotificationsStream();
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
        databaseService.clearGuiCutThroughEntriesOfNode(getMountpointId());
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

    // Private functions

    private String getMountpointId() {
        return getNodeId().getValue();
    }

    // Read from device
    /**
     * Read system data with GUI cut through information from device if ONAP_SYSTEM YANG is supported.
     *
     * @return System1 data with GUI cut through information or null if not available.
     */
    private @Nullable System1 getOnapSystemData() {
        LOG.info("Get System1 for class {} from mountpoint {}", netconfAccessor.getNodeId().getValue());
        Capabilities x = netconfAccessor.getCapabilites();
        LOG.info("Capabilites: {}", x);
        if (x.isSupportingNamespace(ONAP_SYSTEM)) {
            @Nullable
            System1 res = netconfAccessor.getTransactionUtils().readData(netconfAccessor.getDataBroker(),
                    LogicalDatastoreType.OPERATIONAL, SYSTEM1_IID);
            LOG.debug("Result of System1 = {}", res);
            return res;
        } else {
            LOG.debug("No GUI cut through support");
            return null;
        }
    }

    private Hardware readHardware() {
        final Class<Hardware> clazzPac = Hardware.class;
        LOG.info("DBRead Get hardware for class {} from mountpoint {}", clazzPac.getSimpleName(),
                netconfAccessor.getNodeId().getValue());
        InstanceIdentifier<Hardware> hardwareIID = InstanceIdentifier.builder(clazzPac).build();
        Hardware res = netconfAccessor.getTransactionUtils().readData(netconfAccessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, hardwareIID);
        LOG.debug("Result of Hardware = {}", res);
        return res;
    }

    // VES related
    private void publishMountpointToVES(Collection<Component> componentList) {

        LOG.debug("In publishMountpointToVES()");

        /*
         * 1. Check if this device is in the list of allowed-devices. 2. If device
         * exists in allowed-devices, then create VES pnfRegistration event and publish
         * to VES
         */
        if (inAllowedDevices(getMountpointId())) {
            if (vesCollectorService.getConfig().isVESCollectorEnabled()) {
                for (Component component : ORanToInternalDataModel.getRootComponents(componentList)) {
                    // Just get one component. At the moment we don't care which one. Also since
                    // there is only one management address, we assume there will be only one
                    // chassis.
                    // If the device supports subtended configuration then it is assumed that the
                    // Chassis containing the management interface will be the root component and
                    // there will be only one root.
                    VESCommonEventHeaderPOJO header = mapper.mapCommonEventHeader(component);
                    VESPNFRegistrationFieldsPOJO body = mapper.mapPNFRegistrationFields(component);
                    try {
                        vesCollectorService.publishVESMessage(vesCollectorService.generateVESEvent(header, body));
                    } catch (JsonProcessingException e) {
                        LOG.warn("Error while serializing VES Event to String ", e);
                    }
                }
            }
        }
    }

    private boolean inAllowedDevices(String mountpointName) {
        final InstanceIdentifier<AllowedDevices> ALL_DEVICES =
                InstanceIdentifier.create(NetconfCallhomeServer.class).child(AllowedDevices.class);

        AllowedDevices allowedDevices = netconfAccessor.getTransactionUtils().readData(
                netconfAccessor.getControllerBindingDataBroker(), LogicalDatastoreType.CONFIGURATION, ALL_DEVICES);

        if (allowedDevices != null) {
            Collection<Device> deviceList = YangHelper.getCollection(allowedDevices.nonnullDevice());
            for (Device device : deviceList) {
                LOG.info("Device in allowed-devices is - {}", device.getUniqueId());
                if (device.getUniqueId().equals(netconfAccessor.getNodeId().getValue())) {
                    LOG.info("Mountpoint is part of allowed-devices list");
                    return true;
                }
            }
        }

        LOG.info("Mountpoint {} is not part of allowed-devices list", mountpointName);
        return false;
    }

}
