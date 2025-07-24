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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.equipment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.NetworkElementCoreData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.OnfInterfacePac;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.EquipmentData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.InventoryInformationDcae;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.Equipment;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.EquipmentKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.NetworkElement;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.equipment.g.ContainedHolder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.equipment.g.ManufacturedThing;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.manufactured.thing.g.EquipmentType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.manufactured.thing.g.ManufacturerProperties;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains equipment related information of ONFCore Network Element
 */
public class ONFCoreNetworkElement12Equipment {

    private static final Logger LOG = LoggerFactory.getLogger(ONFCoreNetworkElement12Equipment.class);

    private static final UniversalId EQUIPMENTROOT = new UniversalId("network-element");
    private static final int EQUIPMENTROOTLEVEL = 0;

    private final NetworkElementCoreData coreData;
    private final @Nullable OnfInterfacePac equipmentPac;
    private final NetconfBindingAccessor acessor;

    private final ValueNameList extensionList;
    private final @NonNull List<UniversalId> topLevelEqUuidList;
    private final @NonNull FaultData globalProblemList;
    private final @NonNull List<ExtendedEquipment> globalEquipmentList;

    public ONFCoreNetworkElement12Equipment(NetconfBindingAccessor acessor, NetworkElementCoreData coreData) {
        this(acessor, coreData, false);
    }

    public ONFCoreNetworkElement12Equipment(NetconfBindingAccessor acessor, NetworkElementCoreData coreData,
            Boolean disabled) {
        LOG.debug("Initialize class: {} " + ONFCoreNetworkElement12Equipment.class.getName());
        this.acessor = acessor;
        this.coreData = coreData;
        String reason;
        if (disabled) {
            this.equipmentPac = null;
            reason = "disabled";
        } else if (acessor.getCapabilites().isSupportingNamespaceAndRevision(WrapperEquipmentPacRev170402.QNAME)) {
            this.equipmentPac = new WrapperEquipmentPacRev170402(acessor);
            reason = "WrapperEquipmentPacRev170402.QNAME";
        } else {
            this.equipmentPac = null;
            reason = "unsupported";
        }
        LOG.debug("Equipement pac initialization '{}'", reason);

        globalEquipmentList = new ArrayList<>();

        extensionList = new ValueNameList();
        topLevelEqUuidList = new ArrayList<>();
        globalProblemList = new FaultData();
        initClassVars();
    }

    public void addProblemsofNode(FaultData resultList) {
        resultList.addAll(globalProblemList);
    }

    public FaultData addProblemsofNodeObject(String uuidString) {
        FaultData res = new FaultData();

        if (this.equipmentPac != null) {
            this.equipmentPac.readTheFaults(new UniversalId(uuidString), res);
        }
        return res;
    }

    public @NonNull InventoryInformationDcae getInventoryInformation(List<String> uuids) {
        return getInventoryInformationDcae(this.extensionList, uuids);
    }

    public void readNetworkElementEquipment() {
        doSyncNetworkElementEquipmentToClassVars();
    }

    public String getMountpoint() {
        return coreData.getMountpoint();
    }

    public OnfInterfacePac getEquipmentPac() {
        return equipmentPac;
    }

    public List<UniversalId> getTopLevelEqUuidList() {
        return topLevelEqUuidList;
    }

    public @NonNull EquipmentData getEquipmentData() {
        EquipmentData res = new EquipmentData();
        globalEquipmentList.forEach(extEquipment -> res.add(extEquipment.getCreateInventoryInput()));
        return res;
    }

    public List<Equipment> getEquipmentAll() {
        List<Equipment> equipmentListAll = new ArrayList<>();

        Equipment equipment = readEquipmentAll();
        equipmentListAll.add(equipment);

        return equipmentListAll;
    }

    TransactionUtils getGenericTransactionUtils() {
        return acessor.getTransactionUtils();
    }

    /*
     * --------------------------------------------------------------------------------- private
     * functions
     */

    private void initClassVars() {
        this.globalProblemList.clear();
        this.extensionList.clear();
        this.topLevelEqUuidList.clear();
    }

    private void doSyncNetworkElementEquipmentToClassVars() {

        Optional<NetworkElement> optionalNe = coreData.getOptionalNetworkElement();
        initClassVars();

        if (optionalNe.isPresent()) {

            // extract Inventory
            extensionList.put(YangHelper.getList(optionalNe.get().getExtension()));

            if (!extensionList.isEmpty()) {

                /*
                 * Loop through network element extension to get "top-level-equipment" <extension>
                 * <value-name>top-level-equipment</value-name> <value>1.0.BKP,1.0.WCS</value> </extension> "ipv4"
                 * address
                 */
                extensionList.getAsUniversalIdList("top-level-equipment", topLevelEqUuidList);

                // If top-level-equipment exists get further information
                if (topLevelEqUuidList.isEmpty()) {
                    LOG.debug("no top level equipment found");
                } else {
                    // Read equipment and problems
                    for (UniversalId uuid : topLevelEqUuidList) {
                        recurseReadEquipmentProblems(uuid, EQUIPMENTROOT, coreData.getMountpoint(), EQUIPMENTROOTLEVEL,
                                globalProblemList, globalEquipmentList);
                    }
                }
            } else {
                LOG.debug("extension list is null");
            }
        }
    }

    private void recurseReadEquipmentProblems(UniversalId uuid, UniversalId parentUuid, String path, int treeLevel,
            @NonNull FaultData problemList, @NonNull List<ExtendedEquipment> equipmentList) {

        if (uuid != null) {

            Equipment equipment = this.readEquipment(uuid);

            if (equipment != null) {
                equipmentList.add(
                        new ExtendedEquipment(this.getMountpoint(), parentUuid.getValue(), equipment, path, treeLevel));

                if (equipmentPac != null) {
                    equipmentPac.readTheFaults(uuid, problemList);

                    Collection<ContainedHolder> containedHolderListe = YangHelper.getCollection(equipment.getContainedHolder());
                    if (containedHolderListe != null) {
                        for (ContainedHolder containedHolder : containedHolderListe) {
                            recurseReadEquipmentProblems(containedHolder.getOccupyingFru(), uuid,
                                    path + "/" + uuid.getValue(), treeLevel + 1, problemList, equipmentList);
                        }
                    }
                }
            }
        }
    }

    private @NonNull InventoryInformationDcae getInventoryInformationDcae(ValueNameList extensions,
            List<String> uuids) {

        InventoryInformationDcae inventoryInformation = new InventoryInformationDcae();

        // uuids
        inventoryInformation.setInterfaceUuidList(uuids);

        if (!extensions.isEmpty()) {

            inventoryInformation.setDeviceIpv4(extensions.getOrNull("neIpAddress"));

            // If top-level-equipment exists get further information
            if (topLevelEqUuidList.isEmpty()) {
                LOG.debug("no top level equipment found");
            } else {
                if (!globalEquipmentList.isEmpty()) {
                    Equipment e = globalEquipmentList.get(0).getEquipment();
                    if (e != null) {
                        ManufacturedThing manufacturedThing = e.getManufacturedThing();
                        if (manufacturedThing != null) {
                            EquipmentType et;
                            if ((et = manufacturedThing.getEquipmentType()) != null) {
                                inventoryInformation.setType(et.getTypeName());
                                inventoryInformation.setModel(et.getModelIdentifier());
                            }
                            ManufacturerProperties em;
                            if ((em = manufacturedThing.getManufacturerProperties()) != null) {
                                inventoryInformation.setVendor(em.getManufacturerIdentifier());
                            }
                        }
                    }
                }
            }
        } else {
            LOG.debug("extension list is null");
        }

        LOG.debug("Inventory: {}", inventoryInformation);
        return inventoryInformation;

    }


    /**
     * Read equipment information
     *
     * @param interfacePacUuid uuid as key for Equipment.
     * @return Equipment or null
     */
    private @Nullable Equipment readEquipment(UniversalId interfacePacUuid) {

        final Class<?> clazzPac = Equipment.class;

        LOG.debug("DBRead Get equipment for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                coreData.getMountpoint(), interfacePacUuid.getValue());

        InstanceIdentifier<Equipment> equipmentIID =
                InstanceIdentifier.builder(Equipment.class, new EquipmentKey(interfacePacUuid)).build();

        return getGenericTransactionUtils().readData(coreData.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                equipmentIID);

    }

    /**
     * Read equipment information
     *
     * @return Equipment or null
     */
    private @Nullable Equipment readEquipmentAll() {

        final Class<?> clazzPac = Equipment.class;

        LOG.debug("DBRead Get all equipment for class {} from mountpoint {}", clazzPac.getSimpleName(),
                coreData.getMountpoint());

        InstanceIdentifier<Equipment> equipmentIID = InstanceIdentifier.builder(Equipment.class).build();

        return getGenericTransactionUtils().readData(coreData.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                equipmentIID);

    }

    /**
     * specific toString()
     */
    @Override
    public String toString() {
        return "ONFCoreNetworkElement12Equipment [coreData=" + coreData + ", equipmentPac=" + equipmentPac
                + ", extensions=" + extensionList + ", topLevelEqUuidList=" + topLevelEqUuidList + ", problemList="
                + globalProblemList + ", equipmentList=" + globalEquipmentList + "]";
    }

}
