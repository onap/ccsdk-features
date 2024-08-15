/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.interfaces.Onf14Interfaces;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.interfaces.TechnologySpecificPacKeys;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreModel14 extends YangModule {

    private static final Logger LOG = LoggerFactory.getLogger(CoreModel14.class);

    private static final String NAMESPACE = "urn:onf:yang:core-model-1-4";
    private static final List<QNameModule> MODULES =
            Arrays.asList(QNameModule.create(XMLNamespace.of(NAMESPACE), Revision.of("2019-11-27")));

    private final QName CONTROL_CONSTRUCT;
    private final QName TOP_LEVEL_EQUIPMENT;

    private CoreModel14(NetconfDomAccessor netconfDomAccessor, QNameModule module) {
        super(netconfDomAccessor, module);

        CONTROL_CONSTRUCT = QName.create(module, "control-construct");
        TOP_LEVEL_EQUIPMENT = QName.create(module, "top-level-equipment");
    }

    public String getRevision() {
        return module.getRevision().get().toString();
    }

    public QName getControlConstructQName() {
        return CONTROL_CONSTRUCT;
    }

    public YangInstanceIdentifier getTopLevelEquipment_IId() {
        return YangInstanceIdentifier.builder().node(getControlConstructQName()).node(TOP_LEVEL_EQUIPMENT).build();
    }

    private YangInstanceIdentifier getLtp_IID() {
        return YangInstanceIdentifier.builder().node(getQName("control-construct"))
                .node(getQName("logical-termination-point")).build();
    }

    public YangInstanceIdentifier getLayerProtocolIId(String ltpUuid, String localId) {
        return YangInstanceIdentifier.builder().node(getQName("control-construct"))
                .node(getQName("logical-termination-point"))
                .nodeWithKey(getQName("logical-termination-point"),
                        QName.create(getQName("logical-termination-point"), "uuid").intern(), ltpUuid)
                .node(getQName("layer-protocol")).nodeWithKey(getQName("layer-protocol"),
                        QName.create(getQName("layer-protocol"), "local-id").intern(), localId)
                .build();
    }

    public Optional<NormalizedNode> readLtpData(NetconfDomAccessor netconfDomAccessor) {
        LOG.info("Reading Logical Termination Point data");
        return netconfDomAccessor.readDataNode(LogicalDatastoreType.CONFIGURATION, getLtp_IID());
    }

    /**
     * Get the LP list, which should contain only 1 entry. the Layer Protocol list should contain only one item, since
     * we have an 1:1 relationship between the LTP and the LP
     *
     * @param ltp
     * @return
     */
    private Collection<MapEntryNode> getInterfaceKeyList(DataContainerNode ltp) {
        MapNode lpList = (MapNode) ltp.childByArg(new NodeIdentifier(getQName("layer-protocol")));
        // the Layer Protocol list should contain only one item, since we have an 1:1
        // relationship between the LTP and the LP
        if (lpList != null && lpList.size() != 1) {
            LOG.debug("Layer protocol has no 1:1 relationship with the LTP.");
            return Collections.emptyList();
        }
        // accessing the LP, which should be only 1
        return lpList.body();
    }

    /**
     * Search through the LayerProtocol list for specific layerProtocolNamesValues
     * @param ltp
     * @param lp
     * @param layerProtocolNameValue
     * @return
     */
    private List<TechnologySpecificPacKeys> getTechnologySpecificPackKeys(DataContainerNode ltp, Collection<MapEntryNode> lp,
            String layerProtocolNameValue) {
        List<TechnologySpecificPacKeys> interfaceList = new ArrayList<>();
        for (MapEntryNode lpEntry : lp) {
            String layerProtocolName = Onf14DMDOMUtility.getLeafValue(lpEntry, getQName("layer-protocol-name"));
            if (layerProtocolName != null && layerProtocolName.contains(layerProtocolNameValue)) {
                TechnologySpecificPacKeys interfaceKey =
                        new TechnologySpecificPacKeys(Onf14DMDOMUtility.getLeafValue(ltp, getQName("uuid")),
                                Onf14DMDOMUtility.getLeafValue(lpEntry, getQName("local-id")));
                interfaceList.add(interfaceKey);
                LOG.debug("Adding Ltp with uuid {} and local-id {} to the {} list", interfaceKey.getLtpUuid(),
                        interfaceKey.getLocalId(), layerProtocolNameValue);
            }
        }
        return interfaceList;
    }

    public Onf14Interfaces readKeys(Onf14Interfaces interfaces) {

        Optional<NormalizedNode> ltpData = readLtpData(netconfDomAccessor);
        LOG.debug("LTP Data is - {}", ltpData);
        if (ltpData.isPresent()) {
            LOG.debug("In readKeys - ltpData = {}", ltpData.get());

            MapNode ccLtp = (MapNode) ltpData.get();
            if (ccLtp != null) {
                LOG.debug("Iterating the LTP list for node {}", netconfDomAccessor.getNodeId().getValue());
                Collection<MapEntryNode> ltpList = ccLtp.body();

                // iterating all the Logical Termination Point list
                for (MapEntryNode ltp : ltpList) {
                    Collection<MapEntryNode> lp = getInterfaceKeyList(ltp);

                    interfaces.add(Onf14Interfaces.Key.AIRINTERFACE,
                            getTechnologySpecificPackKeys(ltp, lp, "LAYER_PROTOCOL_NAME_TYPE_AIR_LAYER"));
                    interfaces.add(Onf14Interfaces.Key.ETHERNETCONTAINER,
                            getTechnologySpecificPackKeys(ltp, lp, "LAYER_PROTOCOL_NAME_TYPE_ETHERNET_CONTAINER_LAYER"));
                    interfaces.add(Onf14Interfaces.Key.WIREINTERFACE,
                            getTechnologySpecificPackKeys(ltp, lp, "LAYER_PROTOCOL_NAME_TYPE_WIRE_LAYER"));
                }
            }
        }
        return interfaces;
    }

    /**
     * Get specific module for device, depending on capabilities
     */
    public static Optional<CoreModel14> getModule(NetconfDomAccessor netconfDomAccessor) {

        Capabilities capabilities = netconfDomAccessor.getCapabilites();
        for (QNameModule module : MODULES) {

            if (capabilities.isSupportingNamespaceAndRevision(module)) {
                return Optional.of(new CoreModel14(netconfDomAccessor, module));
            }
        }
        return Optional.empty();
    }

}
