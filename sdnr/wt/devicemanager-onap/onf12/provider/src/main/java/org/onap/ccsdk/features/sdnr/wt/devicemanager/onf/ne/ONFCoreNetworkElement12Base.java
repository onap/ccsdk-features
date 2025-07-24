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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.NetworkElementCoreData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.WrapperPTPModelRev170208;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.equipment.ONFCoreNetworkElement12Equipment;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.Helper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.WrapperMicrowaveModelRev181010;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.InventoryInformationDcae;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.PerformanceDataLtp;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.util.InconsistentPMDataException;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.NetworkElement;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.extension.g.Extension;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.extension.g.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.Lp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.network.element.Ltp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.NetworkElementPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.network.element.pac.NetworkElementCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Guicutthrough;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GuicutthroughBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class contains the ONF Core model Version 1.2 related functions.<br>
 * Provides the basic ONF Core Model function.<br>
 * - initialReadFromNetworkElement is not implemented in child classes.
 */
public abstract class ONFCoreNetworkElement12Base extends ONFCoreNetworkElementBase implements NetworkElementCoreData {

    private static final Logger LOG = LoggerFactory.getLogger(ONFCoreNetworkElement12Base.class);

    protected static final @NonNull List<Extension> EMPTYLTPEXTENSIONLIST = new ArrayList<>();

    protected static final InstanceIdentifier<NetworkElement> NETWORKELEMENT_IID =
            InstanceIdentifier.builder(NetworkElement.class).build();


    /*-----------------------------------------------------------------------------
     * Class members
     */

    // Non specific part. Used by all functions.
    /** interfaceList is used by PM task and should be synchronized */
    @SuppressWarnings("null")
    private final @NonNull List<Lp> interfaceList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    private Optional<NetworkElement> optionalNe;
    private final DataProvider databaseService;
    // Performance monitoring specific part
    /** Lock for the PM access specific elements that could be null */
    private final @NonNull Object pmLock = new Object();
    protected @Nullable Iterator<Lp> interfaceListIterator = null;
    /** Actual pmLp used during iteration over interfaces */
    protected @Nullable Lp pmLp = null;

    // Device monitoring specific part
    /** Lock for the DM access specific elements that could be null */
    protected final @NonNull Object dmLock = new Object();

    protected final boolean isNetworkElementCurrentProblemsSupporting12;

    protected final ONFCoreNetworkElement12Equipment equipment;

    protected final NodeId nodeId;

    /*---------------------------------------------------------------
     * Constructor
     */

    protected ONFCoreNetworkElement12Base(@NonNull NetconfBindingAccessor acessor,
            @NonNull DeviceManagerServiceProvider serviceProvider) {
        super(acessor);
        this.optionalNe = Optional.empty();
        this.nodeId = acessor.getNodeId();
        this.isNetworkElementCurrentProblemsSupporting12 =
                acessor.getCapabilites().isSupportingNamespaceAndRevision(NetworkElementPac.QNAME);
        this.equipment = new ONFCoreNetworkElement12Equipment(acessor, this);
        this.databaseService = serviceProvider.getDataProvider();
        WrapperPTPModelRev170208.initSynchronizationExtension(acessor);
        LOG.debug("support necurrent-problem-list={}", this.isNetworkElementCurrentProblemsSupporting12);
    }

    /*---------------------------------------------------------------
     * Getter/ Setter
     */

    @Override
    public Optional<NetworkElement> getOptionalNetworkElement() {
        return optionalNe;
    }

    List<Lp> getInterfaceList() {
        return interfaceList;
    }

    public Object getPmLock() {
        return pmLock;
    }

    /*---------------------------------------------------------------
     * Core model related function
     */

    /**
     * Get uuid of Optional NE.
     *
     * @return Uuid or EMPTY String if optionNE is not available
     */
    protected String getUuId() {
        return optionalNe.isPresent() ? Helper.nnGetUniversalId(optionalNe.get().getUuid()).getValue() : EMPTY;
    }

    /**
     * Read from NetworkElement and verify LTPs have changed. If the NE has changed, update to the new structure. From
     * initial state it changes also.
     */
    protected boolean readNetworkElementAndInterfaces() {

        LOG.debug("Update mountpoint if changed {}", getMountpoint());

        optionalNe = Optional.ofNullable(getGenericTransactionUtils().readData(getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, NETWORKELEMENT_IID));
        synchronized (pmLock) {
            boolean change = false;

            if (optionalNe.isEmpty()) {
                LOG.debug("Unable to read NE data for mountpoint {}", getMountpoint());
                if (!interfaceList.isEmpty()) {
                    interfaceList.clear();
                    interfaceListIterator = null;
                    change = true;
                }

            } else {
                NetworkElement ne = optionalNe.get();
                Optional<Guicutthrough> oGuicutthrough = getGuicutthrough(ne);
                Optional<NetconfAccessor> netconfAccessorOpt = getAcessor();
                if (oGuicutthrough.isPresent() && netconfAccessorOpt.isPresent()) {
                    databaseService.writeGuiCutThroughData(oGuicutthrough.get(),
                            netconfAccessorOpt.get().getNodeId().getValue());
                }
                LOG.debug("Mountpoint '{}' NE-Name '{}'", getMountpoint(), ne.getName());
                List<Lp> actualInterfaceList = getLtpList(ne);
                if (!interfaceList.equals(actualInterfaceList)) {
                    LOG.debug("Mountpoint '{}' Update LTP List. Elements {}", getMountpoint(),
                            actualInterfaceList.size());
                    interfaceList.clear();
                    interfaceList.addAll(actualInterfaceList);
                    interfaceListIterator = null;
                    change = true;
                }
            }
            return change;
        }
    }

    /**
     * Get List of UUIDs for conditional packages from Networkelement<br>
     * Possible interfaces are:<br>
     * MWPS, LTP(MWPS-TTP), MWAirInterfacePac, MicrowaveModel-ObjectClasses-AirInterface<br>
     * ETH-CTP,LTP(Client), MW_EthernetContainer_Pac<br>
     * MWS, LTP(MWS-CTP-xD), MWAirInterfaceDiversityPac, MicrowaveModel-ObjectClasses-AirInterfaceDiversity<br>
     * MWS, LTP(MWS-TTP), ,MicrowaveModel-ObjectClasses-HybridMwStructure<br>
     * MWS, LTP(MWS-TTP), ,MicrowaveModel-ObjectClasses-PureEthernetStructure<br>
     *
     * @param ne NetworkElement
     * @return Id List, never null.
     */

    private static List<Lp> getLtpList(@Nullable NetworkElement ne) {

        List<Lp> res = Collections.synchronizedList(new ArrayList<Lp>());

        if (ne != null) {
            Collection<Ltp> ltpRefList = YangHelper.getCollection(ne.getLtp());
            if (ltpRefList == null) {
                LOG.debug("DBRead NE-Interfaces: null");
            } else {
                for (Ltp ltRefListE : ltpRefList) {
                    Collection<Lp> lpList = YangHelper.getCollection(ltRefListE.getLp());
                    if (lpList == null) {
                        LOG.debug("DBRead NE-Interfaces Reference List: null");
                    } else {
                        for (Lp ltp : lpList) {
                            res.add(ltp);
                        }
                    }
                }
            }
        } else {
            LOG.debug("DBRead NE: null");
        }

        // ---- Debug
        if (LOG.isDebugEnabled()) {
            StringBuilder strBuild = new StringBuilder();
            for (Lp ltp : res) {
                if (strBuild.length() > 0) {
                    strBuild.append(", ");
                }
                strBuild.append(Helper.nnGetLayerProtocolName(ltp.getLayerProtocolName()).getValue());
                strBuild.append(':');
                strBuild.append(Helper.nnGetUniversalId(ltp.getUuid()).getValue());
            }
            LOG.debug("DBRead NE-Interfaces: {}", strBuild.toString());
        }
        // ---- Debug end

        return res;
    }

    /**
     * Read current problems of AirInterfaces and EthernetContainer according to NE status into DB
     *
     * @return List with all problems
     */
    protected FaultData readAllCurrentProblemsOfNode() {

        // Step 2.3: read the existing faults and add to DB
        FaultData resultList = new FaultData();
        int idxStart; // Start index for debug messages
        UniversalId uuid;

        synchronized (pmLock) {
            for (Lp lp : interfaceList) {

                idxStart = resultList.size();
                uuid = lp.getUuid();
                FaultData.debugResultList(LOG, uuid.getValue(), resultList, idxStart);

            }
        }

        // Step 2.4: Read other problems from mountpoint
        if (isNetworkElementCurrentProblemsSupporting12) {
            idxStart = resultList.size();
            readNetworkElementCurrentProblems12(resultList);
            FaultData.debugResultList(LOG, "CurrentProblems12", resultList, idxStart);
        }

        return resultList;

    }

    /**
     * Reading problems for the networkElement V1.2
     *
     * @param resultList to collect the problems
     * @return resultList with additonal problems
     */
    protected FaultData readNetworkElementCurrentProblems12(FaultData resultList) {

        LOG.debug("DBRead Get {} NetworkElementCurrentProblems12", getMountpoint());

        InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.NetworkElementPac> networkElementCurrentProblemsIID =
                InstanceIdentifier.builder(
                        org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.NetworkElementPac.class)
                        .build();

        // Step 2.3: read to the config data store
        NetworkElementPac problemPac;
        NetworkElementCurrentProblems problems = null;
        try {
            problemPac = getGenericTransactionUtils().readData(getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                    networkElementCurrentProblemsIID);
            if (problemPac != null) {
                problems = problemPac.getNetworkElementCurrentProblems();
            }
            if (problems == null) {
                LOG.debug("DBRead no NetworkElementCurrentProblems12");
            } else {
                for (org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.network.element.current.problems.g.CurrentProblemList problem : YangHelper
                        .getCollection(problems.nonnullCurrentProblemList())) {
                    resultList.add(nodeId, problem.getSequenceNumber(), problem.getTimeStamp(),
                            problem.getObjectReference(), problem.getProblemName(),
                            WrapperMicrowaveModelRev181010.mapSeverity(problem.getProblemSeverity()));
                }
            }
        } catch (Exception e) {
            LOG.warn("DBRead {} NetworkElementCurrentProblems12 not supported. Message '{}' ", getMountpoint(),
                    e.getMessage());
        }
        return resultList;
    }

    /*---------------------------------------------------------------
     * Device Monitor
     */

    @Override
    public boolean checkIfConnectionToMediatorIsOk() {
        synchronized (dmLock) {
            return optionalNe.isPresent();
        }
    }

    /*
     * New implementation to interpret status with empty LTP List as notConnected => return false
     * 30.10.2018 Since this behavior is very specific and implicit for specific NE Types
     *     it needs to be activated by extension or configuration. Change to be disabled at the moment
     */
    @Override
    public boolean checkIfConnectionToNeIsOk() {
        return true;
    }

    /*---------------------------------------------------------------
     * Synchronization
     */


    /*---------------------------------------------------------------
     * Equipment related functions
     */

    @Override
    public @NonNull InventoryInformationDcae getInventoryInformation(String layerProtocolFilter) {
        LOG.debug("request inventory information. filter: {}" + layerProtocolFilter);
        return this.equipment.getInventoryInformation(getFilteredInterfaceUuidsAsStringList(layerProtocolFilter));
    }

    @Override
    public InventoryInformationDcae getInventoryInformation() {
        return getInventoryInformation(null);
    }

    protected List<String> getFilteredInterfaceUuidsAsStringList(String layerProtocolFilter) {
        List<String> uuids = new ArrayList<>();

        LOG.debug("request inventory information. filter: {}" + layerProtocolFilter);
        // uuids
        for (Lp lp : this.interfaceList) {
            if (layerProtocolFilter == null || layerProtocolFilter.isEmpty() || layerProtocolFilter
                    .equals(Helper.nnGetLayerProtocolName(lp.getLayerProtocolName()).getValue())) {
                uuids.add(Helper.nnGetUniversalId(lp.getUuid()).getValue());
            }
        }
        LOG.debug("uuids found: {}", uuids);
        return uuids;
    }


    /*---------------------------------------------------------------
     * Performancemanagement specific interface
     */

    @Override
    public void resetPMIterator() {
        synchronized (pmLock) {
            interfaceListIterator = interfaceList.iterator();
        }
        LOG.debug("PM reset iterator");
    }

    @Override
    public boolean hasNext() {
        boolean res;
        synchronized (pmLock) {
            res = interfaceListIterator != null ? interfaceListIterator.hasNext() : false;
        }
        LOG.debug("PM hasNext LTP {}", res);
        return res;
    }

    @Override
    public void next() {
        synchronized (pmLock) {
            if (interfaceListIterator == null) {
                pmLp = null;
                LOG.debug("PM next LTP null");
            } else {
                pmLp = interfaceListIterator.next();
                LOG.debug("PM next LTP {}", Helper.nnGetLayerProtocolName(pmLp.getLayerProtocolName()).getValue());
            }
        }
    }

    @Override
    public String pmStatusToString() {
        StringBuilder res = new StringBuilder();
        synchronized (pmLock) {
        	if (pmLp == null) {
                res.append("no interface");	
        	} else {
        		res.append("ActualLP=");
        		res.append(Helper.nnGetLayerProtocolName(pmLp.getLayerProtocolName()).getValue());
        	}
            res.append(" IFList=");
            int no=0;
            for (Lp lp : getInterfaceList()) {
            	res.append("[");
            	res.append(no++);
            	res.append("]=");
                res.append(Helper.nnGetLayerProtocolName(lp.getLayerProtocolName()).getValue());
                res.append(" ");
            }
        }
        return res.toString();
    }

    @Override
    public void doRegisterEventListener(MountPoint mountPoint) {
        //Do nothing
    }

    @SuppressWarnings("unchecked")
    @Override
    public <L extends NetworkElementService> Optional<L> getService(Class<L> clazz) {
        return clazz.isInstance(this) ? Optional.of((L) this) : Optional.empty();
    }

    @Override
    public Optional<PerformanceDataLtp> getLtpHistoricalPerformanceData() throws InconsistentPMDataException {
        return Optional.empty();
    }

    //Guicutthrough
    private Optional<Guicutthrough> getGuicutthrough(NetworkElement ne) {
        Extension extension = ne.nonnullExtension().get(new ExtensionKey("webUri"));
        Optional<NetconfAccessor> netconfAccessorOpt = getAcessor();
        if (extension != null && netconfAccessorOpt.isPresent()) {
            GuicutthroughBuilder gcBuilder = new GuicutthroughBuilder();
            gcBuilder.setName(netconfAccessorOpt.get().getNodeId().getValue());
            gcBuilder.setWeburi(extension.getValue());
            return Optional.of(gcBuilder.build());
        } else {
            return Optional.empty();
        }
    }

}
