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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.dataprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDeviceManagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.yangspecs.ORANFM;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.yangspecs.OnapSystem;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Guicutthrough;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GuicutthroughBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanDOMToInternalDataModel {

    private static final Logger LOG = LoggerFactory.getLogger(ORanDOMToInternalDataModel.class);

    public static List<Inventory> getInventoryList(NodeId nodeId, NormalizedNode hwData) {

        List<Inventory> inventoryResultList = new ArrayList<Inventory>();
        ContainerNode hwContainer = (ContainerNode) hwData;
        MapNode componentMap =
                (MapNode) hwContainer.getChildByArg(new NodeIdentifier(ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST));
        Collection<MapEntryNode> componentMapEntries = componentMap.body();

        for (MapEntryNode componentMapEntryNode : getRootComponents(componentMapEntries)) {
            inventoryResultList =
                    recurseGetInventory(nodeId, componentMapEntryNode, componentMapEntries, 0, inventoryResultList);
        }
        // Verify if result is complete
        if (componentMapEntries.size() != inventoryResultList.size()) {
            LOG.warn(
                    "Not all data were written to the Inventory. Potential entries with missing "
                            + "contained-child. Node Id = {}, Components Found = {}, Entries written to Database = {}",
                    nodeId.getValue(), componentMapEntries.size(), inventoryResultList.size());
        }
        return inventoryResultList;
    }

    private static List<Inventory> recurseGetInventory(NodeId nodeId, MapEntryNode component,
            Collection<MapEntryNode> componentList, int treeLevel, List<Inventory> inventoryResultList) {
        //Add element to list, if conversion successfull
        Optional<Inventory> oInventory = getInternalEquipment(nodeId, component, treeLevel);
        if (oInventory.isPresent()) {
            inventoryResultList.add(oInventory.get());
        }
        //Walk through list of child keys and add to list
        for (String childUuid : CodeHelpers.nonnull(new ArrayList<>(ORanDMDOMUtility.getLeafListValue(component,
                ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_CONTAINS_CHILD)))) {
            for (MapEntryNode c : getComponentsByName(childUuid, componentList)) {
                inventoryResultList = recurseGetInventory(nodeId, c, componentList, treeLevel + 1, inventoryResultList);
            }
        }
        return inventoryResultList;
    }

    public static List<MapEntryNode> getRootComponents(Collection<MapEntryNode> componentMapEntries) {
        List<MapEntryNode> resultList = new ArrayList<>();
        for (MapEntryNode componentMapEntryNode : componentMapEntries) {
            if (ORanDMDOMUtility.getLeafValue(componentMapEntryNode,
                    ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_PARENT) == null) { // Root elements do not have a parent
                resultList.add(componentMapEntryNode);
            }
        }
        return resultList;
    }

    private static List<MapEntryNode> getComponentsByName(String name, Collection<MapEntryNode> componentList) {
        List<MapEntryNode> resultList = new ArrayList<>();
        for (MapEntryNode c : componentList) {
            if (name.equals(ORanDMDOMUtility.getKeyValue(c))) { // <-- Component list is flat search for child's of name
                resultList.add(c);
            }
        }
        return resultList;
    }

    public static Optional<Inventory> getInternalEquipment(NodeId nodeId, MapEntryNode component, int treeLevel) {

        // Make sure that expected data are not null
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(component);

        // Read mandatory data

        @Nullable
        String nodeIdString = nodeId.getValue();
        @Nullable
        String uuid = ORanDMDOMUtility.getKeyValue(component);
        @Nullable
        String idParent =
                ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_PARENT);
        @Nullable
        String uuidParent = idParent != null ? idParent : uuid; //<- Passt nicht

        // do consistency check if all mandatory parameters are there
        if (treeLevel >= 0 && nodeIdString != null && uuid != null && uuidParent != null) {
            // Build output data

            InventoryBuilder inventoryBuilder = new InventoryBuilder();

            // General assumed as mandatory
            inventoryBuilder.setNodeId(nodeIdString);
            inventoryBuilder.setUuid(uuid);
            inventoryBuilder.setParentUuid(uuidParent);
            inventoryBuilder.setTreeLevel(Uint32.valueOf(treeLevel));

            // -- String list with ids of holders (optional)
            inventoryBuilder.setContainedHolder(ORanDMDOMUtility.getLeafListValue(component,
                    ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_CONTAINS_CHILD));

            // -- Manufacturer related things (optional)
            @Nullable
            String mfgName =
                    ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_MFG_NAME);
            inventoryBuilder.setManufacturerName(mfgName);
            inventoryBuilder.setManufacturerIdentifier(mfgName);

            // Equipment type (optional)
            inventoryBuilder.setDescription(
                    ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_DESC));
            inventoryBuilder.setModelIdentifier(ORanDMDOMUtility.getLeafValue(component,
                    ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_MODEL_NAME));

            inventoryBuilder.setPartTypeId(
                    ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_CLASS));

            inventoryBuilder.setTypeName(ORanDMDOMUtility.getKeyValue(component));
            inventoryBuilder.setVersion(
                    ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_HW_REV));

            // Equipment instance (optional)
            @Nullable
            String mfgDate =
                    ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_MFG_DATE);
            if (mfgDate != null) {
                inventoryBuilder.setDate(mfgDate);
            }
            inventoryBuilder.setSerial(
                    ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_SER_NUM));

            return Optional.of(inventoryBuilder.build());
        }
        return Optional.empty();
    }

    /**
     * If system data is available convert
     *
     * @param sys
     * @return
     */
    public static Optional<Guicutthrough> getGuicutthrough(@Nullable AugmentationNode onapSysAugData,
            @NonNull OnapSystem onapSys) {

        if (onapSysAugData != null) {
            String name = ORanDMDOMUtility.getLeafValue(onapSysAugData, onapSys.getName());
            @Nullable
            Uri uri = new Uri(ORanDMDOMUtility.getLeafValue(onapSysAugData, onapSys.getWebUi()));
            if (uri.getValue() != null) {
                GuicutthroughBuilder gcBuilder = new GuicutthroughBuilder();
                if (name != null) {
                    gcBuilder.setName(name);
                }
                gcBuilder.setWeburi(uri.getValue());
                return Optional.of(gcBuilder.build());
            }
            LOG.warn("Uri not set to invoke a Gui cut through session to the device. Please set the Uri in the device");
        }
        LOG.warn("Retrieving augmented System details failed. Gui cut through information not available");
        return Optional.empty();
    }

    /**
     * Convert fault notification into data-provider FaultLogEntity
     *
     * @param notification with O-RAN notification
     * @param oranfm
     * @param nodeId of node to handle
     * @param counter to be integrated into data
     * @return FaultlogEntity with data
     */
    public static FaultlogEntity getFaultLog(DOMNotification notification, @NonNull ORANFM oranfm, NodeId nodeId) {
        ContainerNode cn = notification.getBody();
        FaultlogBuilder faultAlarm = new FaultlogBuilder();
        faultAlarm.setNodeId(nodeId.getValue());
        faultAlarm.setObjectId(ORanDMDOMUtility.getLeafValue(cn, oranfm.getFaultSourceQName()));
        faultAlarm.setProblem(ORanDMDOMUtility.getLeafValue(cn, oranfm.getFaultTextQName()));
        faultAlarm.setSeverity(getSeverityType(ORanDMDOMUtility.getLeafValue(cn, oranfm.getFaultSeverityQName()),
                ORanDMDOMUtility.getLeafValue(cn, oranfm.getFaultIsClearedQName()).equals("true")));
        faultAlarm.setCounter(Integer.parseInt(ORanDMDOMUtility.getLeafValue(cn, oranfm.getFaultIdQName())));
        faultAlarm.setId(ORanDMDOMUtility.getLeafValue(cn, oranfm.getFaultIdQName()));
        faultAlarm.setSourceType(SourceType.Netconf);
        faultAlarm.setTimestamp(NetconfTimeStampImpl.getConverter()
<<<<<<< PATCH SET (9e7f6c Align initial alarms and notifications parsing)
                .getTimeStamp(ORanDMDOMUtility.getNotificationInstant(notification).toString()));
=======
                .getTimeStamp(ORanDMDOMUtility.getLeafValue(cn, oranfm.getFaultEventTimeQName())));

>>>>>>> BASE      (762f33 Merge "Web Client context menu item display" into montreal)
        return faultAlarm.build();
    }

    public static FaultlogEntity getFaultLog(UnkeyedListEntryNode activeAlarmEntry, ORANFM oranfm, NodeId nodeId) {
        FaultlogBuilder faultAlarm = new FaultlogBuilder();
        faultAlarm.setNodeId(nodeId.getValue());
        faultAlarm.setObjectId(ORanDMDOMUtility.getLeafValue(activeAlarmEntry, oranfm.getFaultSourceQName()));
        faultAlarm.setProblem(ORanDMDOMUtility.getLeafValue(activeAlarmEntry, oranfm.getFaultTextQName()));
        faultAlarm.setSeverity(getSeverityType(
                ORanDMDOMUtility.getLeafValue(activeAlarmEntry, oranfm.getFaultSeverityQName()),
                ORanDMDOMUtility.getLeafValue(activeAlarmEntry, oranfm.getFaultIsClearedQName()).equals("true")));
<<<<<<< PATCH SET (9e7f6c Align initial alarms and notifications parsing)
        faultAlarm.setCounter(Integer.parseInt(ORanDMDOMUtility.getLeafValue(activeAlarmEntry, oranfm.getFaultIdQName())));
=======
        faultAlarm.setCounter(
                Integer.parseInt(ORanDMDOMUtility.getLeafValue(activeAlarmEntry, oranfm.getFaultIdQName())));
>>>>>>> BASE      (762f33 Merge "Web Client context menu item display" into montreal)
        faultAlarm.setId(ORanDMDOMUtility.getLeafValue(activeAlarmEntry, oranfm.getFaultIdQName()));
        faultAlarm.setSourceType(SourceType.Netconf);
        faultAlarm.setTimestamp(NetconfTimeStampImpl.getConverter()
                .getTimeStamp(ORanDMDOMUtility.getLeafValue(activeAlarmEntry, oranfm.getFaultEventTimeQName())));
        return faultAlarm.build();
    }

    /**
     * Convert O-RAN specific severity into data-provider severity
     *
     * @param faultSeverity O-RAN severity
     * @param isCleared clear indicator
     * @return data-provider severity type
     * @throws IllegalArgumentException if conversion not possible.
     */
    private static SeverityType getSeverityType(@Nullable String faultSeverity, @Nullable Boolean isCleared)
            throws IllegalArgumentException {
        if (isCleared != null && isCleared) {
            return SeverityType.NonAlarmed;
        }
        if (faultSeverity != null) {
            switch (faultSeverity) {
                case "CRITICAL":
                    return SeverityType.Critical;
                case "MAJOR":
                    return SeverityType.Major;
                case "MINOR":
                    return SeverityType.Minor;
                case "WARNING":
                    return SeverityType.Warning;
            }
        }
        throw new IllegalArgumentException("Unknown Alarm state represent as Critical. isCleared=" + isCleared
                + " faultSeverity=" + faultSeverity);
    }

}
