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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.config.ORanDMConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.dataprovider.ORanDOMToInternalDataModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.notification.ORanDOMChangeNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.notification.ORanDOMFaultNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.notification.ORanDOMSupervisionNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.notification.ORanNotificationObserverImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.rpc.ORanSupervisionRPCImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDeviceManagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.vesmapper.ORanRegistrationToVESpnfRegistrationMapper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.yangspecs.ORANFM;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.yangspecs.OnapSystem;
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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanDOMNetworkElement implements NetworkElement, IConfigChangedListener {

    private static final Logger LOG = LoggerFactory.getLogger(ORanDOMNetworkElement.class);

    private final @NonNull NetconfDomAccessor netconfDomAccessor;
    private final @NonNull DataProvider databaseService;
    private final @NonNull FaultService faultService;
    private final @NonNull NotificationService notificationService;
    private final @NonNull ORanDOMChangeNotificationListener oranDomChangeNotificationListener;
    private final @NonNull ORanDOMFaultNotificationListener oranDomFaultNotificationListener;
    private final @NonNull ORanDOMSupervisionNotificationListener oranDomSupervisionNotificationListener;
    private final @NonNull VESCollectorService vesCollectorService;
    private final @NonNull ORanRegistrationToVESpnfRegistrationMapper mapper;
    private final Optional<OnapSystem> onapSystem;
    private final Optional<ORANFM> oranfm;
    private ORanDMConfig oranSupervisionConfig;

    public ORanDOMNetworkElement(@NonNull NetconfDomAccessor netconfDomAccessor,
            @NonNull DeviceManagerServiceProvider serviceProvider, ORanDMConfig oranSupervisionConfig,
            ConfigurationFileRepresentation configFileRepresentation) {
        LOG.debug("Create {}", ORanDOMNetworkElement.class.getSimpleName());
        this.netconfDomAccessor = Objects.requireNonNull(netconfDomAccessor);
        Objects.requireNonNull(serviceProvider);
        this.databaseService = serviceProvider.getDataProvider();
        this.vesCollectorService = serviceProvider.getVESCollectorService();
        this.faultService = serviceProvider.getFaultService();
        this.notificationService = serviceProvider.getNotificationService();
        this.onapSystem = OnapSystem.getModule(netconfDomAccessor);
        this.oranfm = ORANFM.getModule(netconfDomAccessor);
        this.oranSupervisionConfig = oranSupervisionConfig;

        configFileRepresentation.registerConfigChangedListener(this);
        this.oranDomChangeNotificationListener =
                new ORanDOMChangeNotificationListener(netconfDomAccessor, vesCollectorService, databaseService);

        this.oranDomFaultNotificationListener =
                new ORanDOMFaultNotificationListener(netconfDomAccessor, this.oranfm, vesCollectorService,
                        serviceProvider.getFaultService(), serviceProvider.getWebsocketService(), databaseService);

        this.oranDomSupervisionNotificationListener = new ORanDOMSupervisionNotificationListener(netconfDomAccessor,
                vesCollectorService, databaseService, oranSupervisionConfig);

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

        QName[] faultNotification = {oranfm.get().getAlarmNotifQName()};
        netconfDomAccessor.doRegisterNotificationListener(oranDomFaultNotificationListener, faultNotification);

        Capabilities x = netconfDomAccessor.getCapabilites();
        if (x.isSupportingNamespaceAndRevision(ORanDeviceManagerQNames.ORAN_SUPERVISION_MODULE)) {
            LOG.debug("Device {} supports oran-supervision", netconfDomAccessor.getNodeId().getValue());
            oranDomSupervisionNotificationListener.setComponentList(componentList);
            QName[] supervisionNotification = {ORanDeviceManagerQNames.ORAN_SUPERVISION_NOTIFICATION};
            netconfDomAccessor.doRegisterNotificationListener(oranDomSupervisionNotificationListener,
                    supervisionNotification);
        }
        // Output notification streams to LOG
        @SuppressWarnings("unused")
        Map<StreamKey, Stream> streams = netconfDomAccessor.getNotificationStreamsAsMap();
        // Register to default stream
        netconfDomAccessor.invokeCreateSubscription();
        if (x.isSupportingNamespaceAndRevision(ORanDeviceManagerQNames.ORAN_SUPERVISION_MODULE)) {
            ORanSupervisionRPCImpl.invokeWatchdogReset(netconfDomAccessor, oranSupervisionConfig);
            oranDomSupervisionNotificationListener.registerForNotificationReceivedEvent(
                    new ORanNotificationObserverImpl(netconfDomAccessor, oranSupervisionConfig));
        }
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

        if (oranfm.isPresent()) {
            getActiveAlarms();
        }
        if (onapSystem.isPresent()) {
            AugmentationNode gcData = (AugmentationNode) onapSystem.get().getOnapSystemData();
            Optional<Guicutthrough> oGuicutthrough =
                    ORanDOMToInternalDataModel.getGuicutthrough(gcData, onapSystem.get());
            if (oGuicutthrough.isPresent()) {
                databaseService.writeGuiCutThroughData(oGuicutthrough.get(), netconfDomAccessor.getNodeId().getValue());
            }
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
        faultService.removeAllCurrentProblemsOfNode(getNodeId());
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
    public void warmstart() {
        faultService.removeAllCurrentProblemsOfNode(getNodeId());
    }

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

    private void getActiveAlarms() {
        InstanceIdentifierBuilder activeAlarmListBuilder =
                YangInstanceIdentifier.builder().node(oranfm.get().getFaultActiveAlarmListQName());
        Optional<NormalizedNode> oData =
                netconfDomAccessor.readDataNode(LogicalDatastoreType.OPERATIONAL, activeAlarmListBuilder.build());
        if (oData.isPresent()) {
            ContainerNode cn = (ContainerNode) oData.get();
            UnkeyedListNode activeAlarmsList =
                    (UnkeyedListNode) cn.childByArg(new NodeIdentifier(oranfm.get().getFaultActiveAlarmsQName()));
            for (UnkeyedListEntryNode activeAlarmEntry : activeAlarmsList.body())
                faultService.faultNotification(ORanDOMToInternalDataModel.getFaultLog(activeAlarmEntry, oranfm.get(),
                        netconfDomAccessor.getNodeId()));
        }
    }

    @Override
    public void onConfigChanged() {
        LOG.info("O-RU Supervision Watchdog timers changed, resetting in O-RU via RPC");
        ORanSupervisionRPCImpl.invokeWatchdogReset(netconfDomAccessor, oranSupervisionConfig);
    }

}
