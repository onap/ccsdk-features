/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.PerformanceDataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.dataprovider.Onf14DomToInternalDataModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.equipment.Onf14DomEquipmentManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.interfaces.Onf14DomInterfacePacManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.interfaces.TechnologySpecificPacKeys;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.yangspecs.CoreModel14;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.PerformanceManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.PerformanceDataLtp;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.util.InconsistentPMDataException;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.Stream;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.StreamKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of ONF Core model 1.4 device Top level element is "ControlConstruct" (replaces "NetworkElement" of
 * older ONF Version) NOTE:
 */
public class Onf14DomNetworkElement implements NetworkElement, PerformanceDataProvider {

    private static final Logger log = LoggerFactory.getLogger(Onf14DomNetworkElement.class);

    private final @NonNull Object pmLock = new Object();
    protected @Nullable TechnologySpecificPacKeys pmLp = null;
    protected @Nullable Iterator<TechnologySpecificPacKeys> interfaceListIterator = null;
    private final NetconfDomAccessor netconfDomAccessor;
    private final DataProvider databaseService;
    private final @NonNull FaultService faultService;
    private final @NonNull PerformanceManager performanceManager;
    private final @NonNull NotificationService notificationService;

    private final Onf14DomToInternalDataModel onf14Mapper;

    private final @NonNull Onf14DomEquipmentManager equipmentManager;
    private final @NonNull Onf14DomInterfacePacManager interfacePacManager;
    private final @NonNull CoreModel14 onf14CoreModelQNames;

    public Onf14DomNetworkElement(NetconfDomAccessor netconfDomAccessor, DeviceManagerServiceProvider serviceProvider,
            CoreModel14 onf14CoreModelQNames) {
        log.info("Create {}", Onf14DomNetworkElement.class.getSimpleName());
        this.netconfDomAccessor = netconfDomAccessor;
        this.onf14CoreModelQNames = onf14CoreModelQNames;
        this.databaseService = serviceProvider.getDataProvider();
        this.notificationService = serviceProvider.getNotificationService();
        this.faultService = serviceProvider.getFaultService();
        this.performanceManager = serviceProvider.getPerformanceManagerService();
        this.onf14Mapper = new Onf14DomToInternalDataModel();
        this.equipmentManager = new Onf14DomEquipmentManager(netconfDomAccessor, databaseService, onf14Mapper, onf14CoreModelQNames);
        this.interfacePacManager = new Onf14DomInterfacePacManager(netconfDomAccessor, serviceProvider, onf14CoreModelQNames);
    }

    /**
     * reading the inventory (CoreModel 1.4 Equipment Model) and adding it to the DB
     */
    public void initialReadFromNetworkElement() {
        log.debug("Calling read equipment");
        // Read complete device tree
        readEquipmentData();

        int problems = faultService.removeAllCurrentProblemsOfNode(netconfDomAccessor.getNodeId());
        log.debug("Removed all {} problems from database at registration", problems);

        // Read fault data and subscribe for notifications
        interfacePacManager.register();

    }

    public void setCoreModel() {
        NetworkElementConnectionBuilder eb = new NetworkElementConnectionBuilder();
        eb.setCoreModelCapability(onf14CoreModelQNames.getRevision());
        databaseService.updateNetworkConnection22(eb.build(), netconfDomAccessor.getNodeId().getValue());
    }

    @Override
    public void register() {
        // Set core-model revision value in "core-model-capability" field
        setCoreModel();
        initialReadFromNetworkElement();

        if (netconfDomAccessor.isNotificationsRFC5277Supported()) {
            // Output notification streams to LOG
            Map<StreamKey, Stream> streams = netconfDomAccessor.getNotificationStreamsAsMap();
            log.debug("Available notifications streams: {}", streams);
            // Register to default stream
            netconfDomAccessor.invokeCreateSubscription();
        }
        // -- Register NE to performance manager
        performanceManager.registration(netconfDomAccessor.getNodeId(), this);
    }

    @Override
    public void deregister() {
        faultService.removeAllCurrentProblemsOfNode(netconfDomAccessor.getNodeId());
        performanceManager.deRegistration(netconfDomAccessor.getNodeId());
    }

    @Override
    public NodeId getNodeId() {
        return netconfDomAccessor.getNodeId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <L extends NetworkElementService> Optional<L> getService(Class<L> clazz) {
        return clazz.isInstance(this) ? Optional.of((L) this) : Optional.empty();
    }

    @Override
    public void warmstart() {
        int problems = removeAllCurrentProblemsOfNode();
        log.debug("Removed all {} problems from database at deregistration for {}", problems, netconfDomAccessor.getNodeId().getValue());
    }

    /**
     * Remove all entries from list
     */
    public int removeAllCurrentProblemsOfNode() {
        return faultService.removeAllCurrentProblemsOfNode(netconfDomAccessor.getNodeId());
    }

    @Override
    public Optional<NetconfAccessor> getAcessor() {
        return Optional.of(netconfDomAccessor);
    }

    @Override
    public NetworkElementDeviceType getDeviceType() {
        return NetworkElementDeviceType.Wireless;
    }

    private void readEquipmentData() {
        Optional<NormalizedNode> topLevelEquipment = readTopLevelEquipment(netconfDomAccessor);
        log.debug("Top level equipment data is {}", topLevelEquipment.isPresent() ? topLevelEquipment.get() : null);
        if (topLevelEquipment.isPresent()) {
            equipmentManager.setEquipmentData(topLevelEquipment.get());
        }

    }

    private Optional<NormalizedNode> readTopLevelEquipment(NetconfDomAccessor netconfDomAccessor) {
        log.debug("Reading Top level equipment data");
        return netconfDomAccessor.readDataNode(LogicalDatastoreType.CONFIGURATION, onf14CoreModelQNames.getTopLevelEquipment_IId());
    }

    public Object getPmLock() {
        return pmLock;
    }

    @Override
    public void resetPMIterator() {
        synchronized (pmLock) {
            interfaceListIterator = interfacePacManager.getAirInterfaceList().iterator();
        }
        log.debug("PM reset iterator");
    }

    @Override
    public boolean hasNext() {
        boolean res;
        synchronized (pmLock) {
            res = interfaceListIterator != null ? interfaceListIterator.hasNext() : false;
        }
        log.debug("PM hasNext LTP {}", res);
        return res;
    }

    @Override
    public void next() {
        synchronized (pmLock) {
            if (interfaceListIterator == null) {
                pmLp = null;
                log.debug("PM next LTP null");
            } else {
                pmLp = interfaceListIterator.next();
            }
        }

    }

    @Override
    public Optional<PerformanceDataLtp> getLtpHistoricalPerformanceData() throws InconsistentPMDataException {
        synchronized (getPmLock()) {
            if (pmLp != null) {
                log.debug("Enter query PM");
                @NonNull
                TechnologySpecificPacKeys lp = pmLp;
                return Optional.of(interfacePacManager.getLtpHistoricalPerformanceData(lp));
            }
            return Optional.empty();
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
                res.append(pmLp.getLocalId());
            }
            res.append(" IFList=");
            int no = 0;
            for (TechnologySpecificPacKeys lp : interfacePacManager.getAirInterfaceList()) {
                res.append("[");
                res.append(no++);
                res.append("]=");
                res.append(lp.getLocalId());
                res.append(" ");
            }
        }
        return res.toString();
    }

}
