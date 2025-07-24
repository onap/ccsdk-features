/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.yangspecs;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.dataprovider.InternalDataModelSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.interfaces.TechnologySpecificPacKeys;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.pm.PerformanceDataAirInterface;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.qnames.Onf14DevicemanagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Debug;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.PerformanceDataLtp;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirInterface20 extends YangModule {

    private static final Logger LOG = LoggerFactory.getLogger(AirInterface20.class);

    private static String NAMESPACE = "urn:onf:yang:air-interface-2-0";
    private static final List<QNameModule> MODULES =
            Arrays.asList(QNameModule.of(XMLNamespace.of(NAMESPACE), Revision.of("2020-01-21")),
                    QNameModule.of(XMLNamespace.of(NAMESPACE), Revision.of("2022-07-29")));

    private final CoreModel14 coreModel14;

    private AirInterface20(NetconfDomAccessor netconfDomAccessor, QNameModule module, CoreModel14 coreModel14) {
        super(netconfDomAccessor, module);
        this.coreModel14 = coreModel14;
    }

    public FaultData readAllCurrentProblems(FaultData resultList,
            List<TechnologySpecificPacKeys> airInterfaceList) {

        int idxStart; // Start index for debug messages

        for (TechnologySpecificPacKeys key : airInterfaceList) {
            idxStart = resultList.size();
            readAirInterfaceCurrentProblemForLtp(key.getLtpUuid(), key.getLocalId(), resultList);
            Debug.debugResultList(key.getLtpUuid(), resultList, idxStart);
        }
        return resultList;
    }

    /**
     * Get specific module for device, depending on capabilities
     */
    public static Optional<AirInterface20> getModule(NetconfDomAccessor netconfDomAccessor, CoreModel14 coreModel14) {

        Capabilities capabilities = netconfDomAccessor.getCapabilites();
        for (QNameModule module : MODULES) {

            if (capabilities.isSupportingNamespaceAndRevision(module)) {
                return Optional.of(new AirInterface20(netconfDomAccessor, module, coreModel14));
            }
        }
        return Optional.empty();
    }

    private FaultData readAirInterfaceCurrentProblemForLtp(String ltpUuid, String localId, FaultData resultList) {

        LOG.debug("DBRead Get current problems for class {} from mountpoint {} for LTP uuid {} and local-id {}",
                getQNameModule(), netconfDomAccessor.getNodeId().getValue(), ltpUuid, localId);

        // constructing the IID needs the augmentation exposed by the air-interface-2-0
        // model

        YangInstanceIdentifier layerProtocolIID = coreModel14.getLayerProtocolIId(ltpUuid, localId);

        InstanceIdentifierBuilder augmentedAirInterfacePacIID =
                YangInstanceIdentifier.builder(layerProtocolIID).node(getQName("air-interface-pac"));

        // reading all the current-problems list for this specific LTP and LP
        Optional<NormalizedNode> airInterfacePacDataOpt =
                netconfDomAccessor.readDataNode(LogicalDatastoreType.OPERATIONAL, augmentedAirInterfacePacIID.build());

        if (airInterfacePacDataOpt.isPresent()) {
            @Nullable MapEntryNode airInterfaceCurrentProblemsList =
                    ((MapNode) airInterfacePacDataOpt.get()).childByArg(
                            NodeIdentifierWithPredicates.of(getQName("current-problem-list")));
            if (airInterfaceCurrentProblemsList != null) {
                Collection<@NonNull DataContainerChild> airInterfaceProblemsCollection = airInterfaceCurrentProblemsList.body();
                for (DataContainerChild airInterfaceProblem : airInterfaceProblemsCollection) {
                    if (airInterfaceProblem instanceof DataContainerNode dataContainerNode) {
                        resultList.add(netconfDomAccessor.getNodeId(),
                                Integer.parseInt(
                                        Onf14DMDOMUtility.getLeafValue(dataContainerNode, getQName("sequence-number"))),
                                new DateAndTime(
                                        Onf14DMDOMUtility.getLeafValue(dataContainerNode, getQName("timestamp"))),
                                ltpUuid, Onf14DMDOMUtility.getLeafValue(dataContainerNode, getQName("problem-name")),
                                InternalDataModelSeverity.mapSeverity(Onf14DMDOMUtility.getLeafValue(dataContainerNode,
                                        getQName("problem-severity"))));
                    } else {
                        LOG.warn("unable to cast if problem {} as container node", airInterfaceProblem);
                    }
                }
            } else {
                LOG.debug("DBRead Id {} empty CurrentProblemList", ltpUuid);
            }
        }
        return resultList;
    }

    public PerformanceDataLtp readAirInterfaceHistoricalPerformanceData(String ltpUuid, String localId,
            PerformanceDataLtp res) {
        LOG.debug("Get historical performance data for class {} from mountpoint {} for LTP uuid {} and local-id {}",
                Onf14DevicemanagerQNames.AIR_INTERFACE_2_0_MODULE, netconfDomAccessor.getNodeId().getValue(), ltpUuid,
                localId);

        // constructing the IID needs the augmentation exposed by the air-interface-2-0
        // model

        YangInstanceIdentifier layerProtocolIID = coreModel14.getLayerProtocolIId(ltpUuid, localId);

        InstanceIdentifierBuilder augmentedAirInterfacePacIID =
                YangInstanceIdentifier.builder(layerProtocolIID).node(Onf14DevicemanagerQNames.AIR_INTERFACE_PAC);

        // reading historical performance list for this specific LTP and LP
        Optional<NormalizedNode> airInterfacePacDataOpt =
                netconfDomAccessor.readDataNode(LogicalDatastoreType.OPERATIONAL, augmentedAirInterfacePacIID.build());
        if (airInterfacePacDataOpt.isPresent()) {
            LOG.debug("Performance Data = {}", airInterfacePacDataOpt.get().body());

            var cn = ((ContainerNode) airInterfacePacDataOpt.get())
                    .childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.AIR_INTERFACE_PAC));
            if (cn == null) {
                return null;
            }
            var airIntfHistPerf = ((ContainerNode) cn)
                    .childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCES));
            if (airIntfHistPerf == null) {
                return null;
            }
            MapEntryNode airInterfaceHistoricalPerformanceList = ((MapNode) airIntfHistPerf).childByArg(
                    NodeIdentifierWithPredicates.of(
                            Onf14DevicemanagerQNames.AIR_INTERFACE_HISTORICAL_PERFORMANCES_LIST));
            if (airInterfaceHistoricalPerformanceList == null) {
                return null;
            }
            Collection<@NonNull DataContainerChild> airInterfaceHistoricalPerfCollection =
                    airInterfaceHistoricalPerformanceList.body();
            for (@NonNull DataContainerChild airInterfaceHistPerf : airInterfaceHistoricalPerfCollection) {
                if (airInterfaceHistPerf instanceof DataContainerNode dataContainerNode) {
                    res.add(new PerformanceDataAirInterface(netconfDomAccessor.getNodeId(), ltpUuid, localId,
                            dataContainerNode));
                } else {
                    LOG.warn("unable to cast airinterfacehistper {} as container node", airInterfaceHistPerf);
                }
            }
            return res;
        } else {
            LOG.debug("DBRead Id {} empty CurrentProblemList", ltpUuid);
        }
        return null;

    }
}
