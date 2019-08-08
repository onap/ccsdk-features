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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.InternalDateAndTime;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.InternalSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.InventoryInformation;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.container.AllPm;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.container.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.util.GenericTransactionUtils;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.wrapperc.WrapperPTPModelRev170208;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ProblemNotificationXml;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPoint;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.InstanceList;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.NetworkElement;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.extension.g.Extension;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.Lp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.network.element.Ltp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.NetworkElementPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.network.element.pac.NetworkElementCurrentProblems;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class contains the ONF Core model Version 1.2 related functions.<br>
 * Provides the basic ONF Core Model function.<br>
 * - initialReadFromNetworkElement is not implemented in child classes.
 */
@SuppressWarnings("deprecation")
public abstract class ONFCoreNetworkElement12Base extends ONFCoreNetworkElementBase implements ONFCoreNetworkElementCoreData {

    private static final Logger LOG = LoggerFactory.getLogger(ONFCoreNetworkElement12Base.class);

    protected static final List<Extension> EMPTYLTPEXTENSIONLIST = new ArrayList<>();
    // private static final List<Ltp> EMPTYLTPLIST = new ArrayList<>();

    protected static final InstanceIdentifier<NetworkElement> NETWORKELEMENT_IID = InstanceIdentifier
            .builder(NetworkElement.class).build();


    /*-----------------------------------------------------------------------------
     * Class members
     */

    // Non specific part. Used by all functions.
    /** interfaceList is used by PM task and should be synchronized */
    private final @Nonnull List<Lp> interfaceList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    private @Nullable NetworkElement optionalNe = null;

    // Performance monitoring specific part
    /** Lock for the PM access specific elements that could be null */
    private final @Nonnull Object pmLock = new Object();
    protected @Nullable Iterator<Lp> interfaceListIterator = null;
    /** Actual pmLp used during iteration over interfaces */
    protected @Nullable Lp pmLp = null;

    // Device monitoring specific part
    /** Lock for the DM access specific elements that could be null */
    protected final @Nonnull Object dmLock = new Object();

    protected final boolean isNetworkElementCurrentProblemsSupporting12;

    private final ONFCoreNetworkElement12Equipment equipment;

    private @Nonnull InventoryInformation inventoryInformation = new InventoryInformation();

    /*
     * Constructor
     */

    protected ONFCoreNetworkElement12Base(String mountPointNodeName, DataBroker netconfNodeDataBroker,
            Capabilities capabilities) {
        super(mountPointNodeName, netconfNodeDataBroker, capabilities);
        // TODO Auto-generated constructor stub
        this.isNetworkElementCurrentProblemsSupporting12 = capabilities.isSupportingNamespaceAndRevision(NetworkElementPac.QNAME);
        this.equipment = new ONFCoreNetworkElement12Equipment(this, capabilities);
        WrapperPTPModelRev170208.initSynchronizationExtension(mountPointNodeName, netconfNodeDataBroker, capabilities);
        LOG.debug("support necurrent-problem-list=" + this.isNetworkElementCurrentProblemsSupporting12);
        LOG.info("Create NE instance {}", InstanceList.QNAME.getLocalName());
    }

    /*---------------------------------------------------------------
     * Getter/ Setter
     */

    @Override
    public NetworkElement getOptionalNetworkElement() {
        return optionalNe;
    }

	List<Lp> getInterfaceList() {
		return interfaceList;
	}

    public Object getPmLock() {
		return pmLock;
	}

    public ONFCoreNetworkElement12Equipment getEquipment() {
    	return equipment;
    }

    /*---------------------------------------------------------------
     * Core model related function
     */

	/**
     * Read the NetworkElement part from database.
     *
     * @return Optional with NetworkElement or empty
     */
    @Nullable
    private NetworkElement readNetworkElement() {
        // Step 2.2: construct data and the relative iid
        // The schema path to identify an instance is
        // <i>CoreModel-CoreNetworkModule-ObjectClasses/NetworkElement</i>
        // Read to the config data store
        return GenericTransactionUtils.readData(getNetconfNodeDataBroker(), LogicalDatastoreType.OPERATIONAL,
                NETWORKELEMENT_IID);
    }

    /**
     * Get uuid of Optional NE.
     *
     * @return Uuid or EMPTY String if optionNE is not available
     */
    protected String getUuId() {
        String uuid = EMPTY;

        try {
            uuid = optionalNe != null ? optionalNe.getUuid() != null ? optionalNe.getUuid().getValue() : EMPTY : EMPTY;
        } catch (NullPointerException e) {
            // Unfortunately throws null pointer if not definied
        }
        return uuid;
    }

    /**
     * Read from NetworkElement and verify LTPs have changed. If the NE has changed, update to the new
     * structure. From initial state it changes also.
     */
    protected synchronized boolean readNetworkElementAndInterfaces() {

        LOG.debug("Update mountpoint if changed {}", getMountPointNodeName());

        optionalNe = GenericTransactionUtils.readData(getNetconfNodeDataBroker(), LogicalDatastoreType.OPERATIONAL,
                NETWORKELEMENT_IID);;
        synchronized (pmLock) {
            boolean change = false;

            if (optionalNe == null) {
                LOG.debug("Unable to read NE data for mountpoint {}", getMountPointNodeName());
                if (!interfaceList.isEmpty()) {
                    interfaceList.clear();
                    interfaceListIterator = null;
                    change = true;
                }

            } else {
                LOG.debug("Mountpoint '{}' NE-Name '{}'", getMountPointNodeName(), optionalNe.getName());
                List<Lp> actualInterfaceList = getLtpList(optionalNe);
                if (!interfaceList.equals(actualInterfaceList)) {
                    LOG.debug("Mountpoint '{}' Update LTP List. Elements {}", getMountPointNodeName(),
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
     * MWS, LTP(MWS-CTP-xD), MWAirInterfaceDiversityPac,
     * MicrowaveModel-ObjectClasses-AirInterfaceDiversity<br>
     * MWS, LTP(MWS-TTP), ,MicrowaveModel-ObjectClasses-HybridMwStructure<br>
     * MWS, LTP(MWS-TTP), ,MicrowaveModel-ObjectClasses-PureEthernetStructure<br>
     *
     * @param ne NetworkElement
     * @return Id List, never null.
     */

    private static List<Lp> getLtpList(@Nullable NetworkElement ne) {

        List<Lp> res = Collections.synchronizedList(new ArrayList<Lp>());

        if (ne != null) {
            List<Ltp> ltpRefList = ne.getLtp();
            if (ltpRefList == null) {
                LOG.debug("DBRead NE-Interfaces: null");
            } else {
                for (Ltp ltRefListE : ltpRefList) {
                    List<Lp> lpList = ltRefListE.getLp();
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
            StringBuffer strBuf = new StringBuffer();
            for (Lp ltp : res) {
                if (strBuf.length() > 0) {
                    strBuf.append(", ");
                }
                strBuf.append(ltp.getLayerProtocolName().getValue());
                strBuf.append(':');
                strBuf.append(ltp.getUuid().getValue());
            }
            LOG.debug("DBRead NE-Interfaces: {}", strBuf.toString());
        }
        // ---- Debug end

        return res;
    }

    /**
     * Read current problems of AirInterfaces and EthernetContainer according to NE status into DB
     *
     * @return List with all problems
     */
    protected List<ProblemNotificationXml> readAllCurrentProblemsOfNode() {

        // Step 2.3: read the existing faults and add to DB
        List<ProblemNotificationXml> resultList = new ArrayList<>();
        int idxStart; // Start index for debug messages
        UniversalId uuid;

        synchronized (pmLock) {
            for (Lp lp : interfaceList) {

                idxStart = resultList.size();
                uuid = lp.getUuid();
                ProblemNotificationXml.debugResultList(LOG, uuid.getValue(), resultList, idxStart);

            }
        }

        // Step 2.4: Read other problems from mountpoint
        if (isNetworkElementCurrentProblemsSupporting12) {
            idxStart = resultList.size();
            readNetworkElementCurrentProblems12(resultList);
            ProblemNotificationXml.debugResultList(LOG, "CurrentProblems12", resultList, idxStart);
        }

        return resultList;

    }

	/**
	 * Reading problems for the networkElement V1.2
	 * @param resultList
	 * @return
	 */
    private List<ProblemNotificationXml> readNetworkElementCurrentProblems12(List<ProblemNotificationXml> resultList) {

        LOG.info("DBRead Get {} NetworkElementCurrentProblems12", getMountPointNodeName());

        InstanceIdentifier<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.NetworkElementPac> networkElementCurrentProblemsIID =
                InstanceIdentifier.builder(
                        org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.NetworkElementPac.class)
                        .build();

        // Step 2.3: read to the config data store
        NetworkElementPac problemPac;
        NetworkElementCurrentProblems problems;
        try {
            problemPac = GenericTransactionUtils.readData(getNetconfNodeDataBroker(), LogicalDatastoreType.OPERATIONAL,
                    networkElementCurrentProblemsIID);
            problems = problemPac.getNetworkElementCurrentProblems();
            if (problems == null) {
                LOG.debug("DBRead no NetworkElementCurrentProblems12");
            } else if (problems.getCurrentProblemList() == null) {
                LOG.debug("DBRead empty CurrentProblemList12");
            } else {
                for (org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.network.element.current.problems.g.CurrentProblemList problem : problems
                        .getCurrentProblemList()) {
                    resultList.add(new ProblemNotificationXml(getMountPointNodeName(), problem.getObjectReference(),
                            problem.getProblemName(), InternalSeverity.valueOf(problem.getProblemSeverity()),
                            problem.getSequenceNumber().toString(),
                            InternalDateAndTime.valueOf(problem.getTimeStamp())));
                }
            }
        } catch (Exception e) {
            LOG.warn("DBRead {} NetworkElementCurrentProblems12 not supported. Message '{}' ", getMountPointNodeName(),
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
            return optionalNe != null;
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
    public @Nonnull InventoryInformation getInventoryInformation(String layerProtocolFilter) {
        LOG.debug("request inventory information. filter:" + layerProtocolFilter);
        return this.equipment.getInventoryInformation(getFilteredInterfaceUuidsAsStringList(layerProtocolFilter));
    }

	@Override
	public InventoryInformation getInventoryInformation() {
		return getInventoryInformation(null);
	}

    protected List<String> getFilteredInterfaceUuidsAsStringList(String layerProtocolFilter) {
        List<String> uuids = new ArrayList<>();

        LOG.debug("request inventory information. filter:" + layerProtocolFilter);
        if (optionalNe != null) {
            // uuids
            for (Lp lp : this.interfaceList) {
                if (layerProtocolFilter == null || layerProtocolFilter.isEmpty()) {
                    uuids.add(lp.getUuid().getValue());
                } else if (lp.getLayerProtocolName() != null && lp.getLayerProtocolName().getValue() != null
                        && lp.getLayerProtocolName().getValue().equals(layerProtocolFilter)) {
                    uuids.add(lp.getUuid().getValue());
                }
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
                LOG.debug("PM next LTP {}", pmLp.getLayerProtocolName().getValue());
            }
        }
    }

    @Override
    public String pmStatusToString() {
        StringBuffer res = new StringBuffer();
        synchronized (pmLock) {
            res.append(pmLp == null ? "no interface" : pmLp.getLayerProtocolName().getValue());
            for (Lp lp : getInterfaceList()) {
                res.append("IF:");
                res.append(lp.getLayerProtocolName().getValue());
                res.append(" ");
            }
        }
        return res.toString();
    }

	@Override
	public AllPm getHistoricalPM() {
        return AllPm.getEmpty();
	}


	@Override
	public void doRegisterMicrowaveEventListener(MountPoint mountPoint) {
        //Do nothing
	}


}
