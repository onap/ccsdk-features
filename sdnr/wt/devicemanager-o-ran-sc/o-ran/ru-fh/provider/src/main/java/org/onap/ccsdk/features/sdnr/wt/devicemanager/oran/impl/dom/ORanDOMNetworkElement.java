/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.dom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESPNFRegistrationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.Stream;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.StreamKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Guicutthrough;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanDOMNetworkElement implements NetworkElement {

    private static final Logger LOG = LoggerFactory.getLogger(ORanDOMNetworkElement.class);

    private final @NonNull NetconfDomAccessor netconfDomAccessor;
    private final @NonNull DataProvider databaseService;
    private final @NonNull FaultService faultService;
    private final @NonNull NotificationService notificationService;
    private final @NonNull ORanDOMChangeNotificationListener oranDomChangeNotificationListener;
    private final @NonNull ORanDOMFaultNotificationListener oranDomFaultNotificationListener;
    private final @NonNull VESCollectorService vesCollectorService;
    private final @NonNull ORanRegistrationToVESpnfRegistrationMapper mapper;

    public ORanDOMNetworkElement(@NonNull NetconfDomAccessor netconfDomAccessor,
            @NonNull DeviceManagerServiceProvider serviceProvider) {
        LOG.debug("Create {}", ORanDOMNetworkElement.class.getSimpleName());
        this.netconfDomAccessor = Objects.requireNonNull(netconfDomAccessor);
        Objects.requireNonNull(serviceProvider);
        this.databaseService = serviceProvider.getDataProvider();
        this.vesCollectorService = serviceProvider.getVESCollectorService();
        this.faultService = serviceProvider.getFaultService();
        this.notificationService = serviceProvider.getNotificationService();

        this.oranDomChangeNotificationListener =
                new ORanDOMChangeNotificationListener(netconfDomAccessor, vesCollectorService, databaseService);

        this.oranDomFaultNotificationListener =
                new ORanDOMFaultNotificationListener(netconfDomAccessor, vesCollectorService,
                        serviceProvider.getFaultService(), serviceProvider.getWebsocketService(), databaseService);

        this.mapper = new ORanRegistrationToVESpnfRegistrationMapper(netconfDomAccessor, vesCollectorService);
    }

    @Override
    public void register() {
        Collection<MapEntryNode> componentList = initialReadFromNetworkElement();
        oranDomFaultNotificationListener.setComponentList(componentList);
        publishMountpointToVES(componentList);
        QName[] notifications = {ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_NETCONF_CONFIG_CHANGE,
                ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_NETCONF_CONFIRMED_COMMIT,
                ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_NETCONF_SESSION_START,
                ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_NETCONF_SESSION_END,
                ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_NETCONF_CAPABILITY_CHANGE};
        netconfDomAccessor.doRegisterNotificationListener(oranDomChangeNotificationListener, notifications);
        QName[] faultNotification = {ORanDeviceManagerQNames.ORAN_FM_ALARM_NOTIF};
        netconfDomAccessor.doRegisterNotificationListener(oranDomFaultNotificationListener, faultNotification);
        // Output notification streams to LOG
        @SuppressWarnings("unused")
        Map<StreamKey, Stream> streams = netconfDomAccessor.getNotificationStreamsAsMap();
        // Register to default stream
        netconfDomAccessor.invokeCreateSubscription();
    }

    public Collection<MapEntryNode> initialReadFromNetworkElement() {
        Collection<MapEntryNode> componentMapEntries = null;
        NormalizedNode hwData = readHardware();

        if (hwData != null) {
            ContainerNode hwContainer = (ContainerNode) hwData;
            MapNode componentMap = (MapNode) hwContainer
                    .childByArg(new NodeIdentifier(ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST));
            if (componentMap != null) {
                componentMapEntries = componentMap.body();
                List<Inventory> inventoryList =
                        ORanDOMToInternalDataModel.getInventoryList(netconfDomAccessor.getNodeId(), hwData);
                databaseService.writeInventory(netconfDomAccessor.getNodeId().getValue(), inventoryList);
            }
        } else {
            componentMapEntries = Collections.emptyList();
        }

        Optional<Guicutthrough> oGuicutthrough = ORanDOMToInternalDataModel.getGuicutthrough(getOnapSystemData());
        if (oGuicutthrough.isPresent()) {
            databaseService.writeGuiCutThroughData(oGuicutthrough.get(), netconfDomAccessor.getNodeId().getValue());
        }
        return componentMapEntries;
    }

    @Override
    public void deregister() {
        /*
         * if (oranDomChangeNotificationListener != null) {
         * this.oranDomChangeNotificationListener.close(); } if
         * (oRanFaultListenerRegistrationResult != null) {
         * this.oRanFaultListenerRegistrationResult.close(); } ;
         */
        databaseService.clearGuiCutThroughEntriesOfNode(getMountpointId());
    }

    @Override
    public NodeId getNodeId() {
        return netconfDomAccessor.getNodeId();
    }

    @Override
    public NetworkElementDeviceType getDeviceType() {
        return NetworkElementDeviceType.ORAN;
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

    // Private functions

    private String getMountpointId() {
        return getNodeId().getValue();
    }

    private NormalizedNode readHardware() {
        InstanceIdentifierBuilder hardwareIIDBuilder =
                YangInstanceIdentifier.builder().node(ORanDeviceManagerQNames.IETF_HW_CONTAINER);

        Optional<NormalizedNode> oData =
                netconfDomAccessor.readDataNode(LogicalDatastoreType.OPERATIONAL, hardwareIIDBuilder.build());
        if (oData.isPresent()) {
            return oData.get();
        }
        return null;
    }

    // Read from device
    /**
     * Read system data with GUI cut through information from device if ONAP_SYSTEM YANG is supported.
     *
     * @return NormalizedNode data with GUI cut through information or null if not available.
     */
    private @Nullable NormalizedNode getOnapSystemData() {
        LOG.debug("Get System1 for mountpoint {}", netconfDomAccessor.getNodeId().getValue());
        @NonNull
        InstanceIdentifierBuilder ietfSystemIID =
                YangInstanceIdentifier.builder().node(ORanDeviceManagerQNames.IETF_SYSTEM_CONTAINER);
        @NonNull
        AugmentationIdentifier onapSystemIID = YangInstanceIdentifier.AugmentationIdentifier.create(
                Sets.newHashSet(ORanDeviceManagerQNames.ONAP_SYSTEM_NAME, ORanDeviceManagerQNames.ONAP_SYSTEM_WEB_UI));
        InstanceIdentifierBuilder augmentedOnapSystem =
                YangInstanceIdentifier.builder(ietfSystemIID.build()).node(onapSystemIID);
        Capabilities x = netconfDomAccessor.getCapabilites();
        LOG.debug("Capabilites: {}", x);
        if (x.isSupportingNamespace(ORanDeviceManagerQNames.ONAP_SYSTEM_QNAME)) {
            Optional<NormalizedNode> res =
                    netconfDomAccessor.readDataNode(LogicalDatastoreType.OPERATIONAL, augmentedOnapSystem.build());
            LOG.debug("Result of System1 = {}", res);
            return res.isPresent() ? res.get() : null;
        } else {
            LOG.debug("No GUI cut through support");
            return null;
        }
    }

    // VES related
    private void publishMountpointToVES(Collection<MapEntryNode> componentList) {
        /*
         * 1. Check if this device is in the list of allowed-devices. 2. If device
         * exists in allowed-devices, then create VES pnfRegistration event and publish
         * to VES
         */
        if (vesCollectorService.getConfig().isVESCollectorEnabled() && inAllowedDevices(getMountpointId())) {
            for (MapEntryNode component : ORanDOMToInternalDataModel.getRootComponents(componentList)) {
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

    private boolean inAllowedDevices(String mountpointName) {
        InstanceIdentifierBuilder callhomeServerIID =
                YangInstanceIdentifier.builder().node(ORanDeviceManagerQNames.CALLHOME_SERVER_CONTAINER);
        final InstanceIdentifierBuilder allowedDevicesIID = YangInstanceIdentifier.builder(callhomeServerIID.build())
                .node(ORanDeviceManagerQNames.CALLHOME_SERVER_ALLOWED_DEVICE);

        Optional<NormalizedNode> allowedDevices = netconfDomAccessor
                .readControllerDataNode(LogicalDatastoreType.CONFIGURATION, allowedDevicesIID.build());

        if (allowedDevices.isPresent()) {
            ContainerNode allowedDevicesNode = (ContainerNode) allowedDevices.get();
            MapNode deviceList = (MapNode) allowedDevicesNode
                    .childByArg(new NodeIdentifier(ORanDeviceManagerQNames.CALLHOME_SERVER_ALLOWED_DEVICE_DEVICE_LIST));
            if (deviceList != null) {
                Collection<MapEntryNode> deviceListCollection = deviceList.body();
                for (MapEntryNode device : deviceListCollection) {
                    //					String deviceName = device.getIdentifier()
                    //							.getValue(ORanDeviceManagerQNames.CALLHOME_SERVER_ALLOWED_DEVICE_KEY).toString();
                    String deviceName = ORanDMDOMUtility.getLeafValue(device,
                            ORanDeviceManagerQNames.CALLHOME_SERVER_ALLOWED_DEVICE_KEY);
                    if (deviceName != null && deviceName.equals(mountpointName)) {
                        LOG.debug("Mountpoint {} is part of allowed-devices list", mountpointName);
                        return true;
                    }
                }
            }
        }

        LOG.debug("Mountpoint {} is not part of allowed-devices list", mountpointName);
        return false;
    }

}
