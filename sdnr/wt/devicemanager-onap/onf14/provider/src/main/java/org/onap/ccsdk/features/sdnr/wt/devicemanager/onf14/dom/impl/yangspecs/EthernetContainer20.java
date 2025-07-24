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
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.dataprovider.InternalDataModelSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.interfaces.TechnologySpecificPacKeys;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Debug;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthernetContainer20 extends YangModule {

    private static final Logger LOG = LoggerFactory.getLogger(EthernetContainer20.class);

    private static String NAMESPACE = "urn:onf:yang:ethernet-container-2-0";
    private static final List<QNameModule> MODULES =
            Arrays.asList(QNameModule.of(XMLNamespace.of(NAMESPACE), Revision.of("2020-01-21")));

    private final CoreModel14 coreModel14;

    public EthernetContainer20(NetconfDomAccessor netconfDomAccessor, QNameModule module, CoreModel14 coreModel14) {
        super(netconfDomAccessor, module);
        this.coreModel14 = coreModel14;
    }

    private FaultData readEthernetContainerCurrentProblemForLtp(String ltpUuid, String localId, FaultData resultList) {

        LOG.debug(
                "DBRead Get current problems for Ethernet Container from mountpoint {} for LTP uuid {} and local-id {}",
                netconfDomAccessor.getNodeId().getValue(), ltpUuid, localId);

        // constructing the IID needs the augmentation exposed by the
        // ethernet-container-2-0 model
        YangInstanceIdentifier layerProtocolIID = coreModel14.getLayerProtocolIId(ltpUuid, localId);

        InstanceIdentifierBuilder augmentedEthernetContainerConfigurationIID =
                YangInstanceIdentifier.builder(layerProtocolIID).node(getQName("ethernet-container-pac"));

        // reading all the current-problems list for this specific LTP and LP
        Optional<NormalizedNode> etherntContainerConfigurationOpt = netconfDomAccessor
                .readDataNode(LogicalDatastoreType.OPERATIONAL, augmentedEthernetContainerConfigurationIID.build());

        if (etherntContainerConfigurationOpt.isPresent()) {

            MapEntryNode ethernetContainerCurrentProblemsList = ((MapNode) etherntContainerConfigurationOpt.get())
                    .childByArg(NodeIdentifierWithPredicates.of(getQName("current-problem-list")));
            if (ethernetContainerCurrentProblemsList == null) {
                return null;
            }
            Collection<@NonNull DataContainerChild> ethernetContainerProblemsCollection =
                    ethernetContainerCurrentProblemsList.body();
            for (@NonNull DataContainerChild ethernetContainerProblem : ethernetContainerProblemsCollection) {
                if (ethernetContainerProblem instanceof DataContainerNode dataContainerNode) {
                    resultList.add(netconfDomAccessor.getNodeId(),
                            Integer.parseInt(Onf14DMDOMUtility.getLeafValue(dataContainerNode,
                                    getQName("sequence-number"))),
                            new DateAndTime(
                                    Onf14DMDOMUtility.getLeafValue(dataContainerNode, getQName("timestamp"))),
                            ltpUuid, Onf14DMDOMUtility.getLeafValue(dataContainerNode, getQName("problem-name")),
                            InternalDataModelSeverity.mapSeverity(Onf14DMDOMUtility
                                    .getLeafValue(dataContainerNode, getQName("problem-severity"))));
                } else {
                    LOG.warn("unable to cast ethernet container problem {} as container node",
                            ethernetContainerProblem);
                }
            }
        } else {
            LOG.debug("DBRead Id {} empty CurrentProblemList", ltpUuid);
        }

        return resultList;
    }

    public FaultData readAllCurrentProblems(FaultData resultList,
            List<TechnologySpecificPacKeys> ethernetContainerList) {

        if (resultList == null) {
            resultList = new FaultData();
        }
        int idxStart; // Start index for debug messages

        for (TechnologySpecificPacKeys key : ethernetContainerList) {
            if (resultList == null) {
                resultList = new FaultData();
            }
            idxStart = resultList.size();

            resultList = readEthernetContainerCurrentProblemForLtp(key.getLtpUuid(), key.getLocalId(), resultList);
            Debug.debugResultList(key.getLtpUuid(), resultList, idxStart);
        }
        return resultList;
    }


    /**
     * Get specific module for device, depending on capabilities
     */
    public static Optional<EthernetContainer20> getModule(NetconfDomAccessor netconfDomAccessor,
            CoreModel14 coreModel14) {

        Capabilities capabilities = netconfDomAccessor.getCapabilites();
        for (QNameModule module : MODULES) {

            if (capabilities.isSupportingNamespaceAndRevision(module)) {
                return Optional.of(new EthernetContainer20(netconfDomAccessor, module, coreModel14));
            }
        }
        return Optional.empty();
    }

}
