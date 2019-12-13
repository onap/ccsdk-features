/*******************************************************************************
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.NetworkElementCoreData;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.INetconfAcessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.container.ExtendedEquipment;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.container.ValueNameList;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.wrapperc.OnfInterfacePac;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.wrapperc.WrapperEquipmentPacRev170402;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ProblemNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.legacy.InventoryInformation;
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
    private final OnfInterfacePac equipmentPac;
    private final INetconfAcessor acessor;

    private final ValueNameList extensionList;
    private final List<UniversalId> topLevelEqUuidList;
    private final List<ProblemNotificationXml> globalProblemList;
    private final List<ExtendedEquipment> globalEquipmentList;

    public ONFCoreNetworkElement12Equipment(INetconfAcessor acessor, NetworkElementCoreData coreData, Capabilities capabilities) {
        LOG.debug("Initialize " + ONFCoreNetworkElement12Equipment.class.getName());
        this.acessor = acessor;
        this.coreData = coreData;
        if (capabilities.isSupportingNamespaceAndRevision(WrapperEquipmentPacRev170402.QNAME)) {
            this.equipmentPac = new WrapperEquipmentPacRev170402(acessor, coreData);
            LOG.debug("Equipement pac supported {}", WrapperEquipmentPacRev170402.QNAME);
        } else {
            this.equipmentPac = null;
            LOG.debug("Equipement pac not supported {}", WrapperEquipmentPacRev170402.QNAME);
        }

        extensionList = new ValueNameList();
        topLevelEqUuidList = new ArrayList<>();
        globalProblemList = new ArrayList<>();
        globalEquipmentList = new ArrayList<>();

        initClassVars();
    }

    public void addProblemsofNode(List<ProblemNotificationXml> resultList) {
        resultList.addAll(globalProblemList);
    }

    public List<ProblemNotificationXml> addProblemsofNodeObject(String uuidString) {
        List<ProblemNotificationXml> res = new ArrayList<>();

        if (this.equipmentPac != null) {
            this.equipmentPac.readTheFaults(new UniversalId(uuidString), res);
        }
        return res;
    }

    public @NonNull InventoryInformation getInventoryInformation(List<String> uuids) {
        return getInventoryInformation(this.extensionList, uuids);
    }

    protected void readNetworkElementEquipment() {
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

    public List<ExtendedEquipment> getEquipmentList() {
        return globalEquipmentList;
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
        this.globalEquipmentList.clear();
        this.extensionList.clear();
        this.topLevelEqUuidList.clear();
    }

    private void doSyncNetworkElementEquipmentToClassVars() {

        NetworkElement optionalNe = coreData.getOptionalNetworkElement();
        initClassVars();

        if (optionalNe != null) {
            // extract Inventory
            extensionList.put(optionalNe.getExtension());

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
                        recurseReadEquipmentProblems(uuid, EQUIPMENTROOT, coreData.getMountpoint(), EQUIPMENTROOTLEVEL, globalProblemList,
                                globalEquipmentList);
                    }
                }
            } else {
                LOG.debug("extension list is null");
            }
        }
    }

    private void recurseReadEquipmentProblems(UniversalId uuid, UniversalId parentUuid, String path, int treeLevel,
            List<ProblemNotificationXml> problemList, List<ExtendedEquipment> equipmentList) {

        if (uuid != null) {

            Equipment equipment = this.readEquipment(uuid);

            if (equipment != null) {
                equipmentList.add(new ExtendedEquipment(this.getMountpoint(),parentUuid.getValue(), equipment, path, treeLevel));

                if (this.equipmentPac != null) {
                    this.equipmentPac.readTheFaults(uuid, problemList);

                    List<ContainedHolder> containedHolderListe = equipment.getContainedHolder();
                    if (containedHolderListe != null) {
                        for (ContainedHolder containedHolder : containedHolderListe) {
                            recurseReadEquipmentProblems(containedHolder.getOccupyingFru(), uuid, path+"/"+uuid.getValue(), treeLevel + 1,
                                    problemList, equipmentList);
                        }
                    }
                }
            }
        }
    }

    private @NonNull InventoryInformation getInventoryInformation(ValueNameList extensions, List<String> uuids) {

        InventoryInformation inventoryInformation = new InventoryInformation();

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

        LOG.info("DBRead Get equipment for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                coreData.getMountpoint(), interfacePacUuid.getValue());

        InstanceIdentifier<Equipment> equipmentIID =
                InstanceIdentifier.builder(Equipment.class, new EquipmentKey(interfacePacUuid)).build();

        Equipment res = getGenericTransactionUtils().readData(coreData.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                equipmentIID);

        return res;
    }

    /**
     * Read equipment information
     *
     * @param interfacePacUuid uuid as key for Equipment.
     * @return Equipment or null
     */
    private @Nullable Equipment readEquipmentAll() {

        final Class<?> clazzPac = Equipment.class;

        LOG.info("DBRead Get all equipment for class {} from mountpoint {}", clazzPac.getSimpleName(),
                coreData.getMountpoint());

        InstanceIdentifier<Equipment> equipmentIID = InstanceIdentifier.builder(Equipment.class).build();

        Equipment res = getGenericTransactionUtils().readData(coreData.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                equipmentIID);

        return res;
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
