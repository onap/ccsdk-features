/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2023 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.yangspecs;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDeviceManagerQNames;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class OnapSystem extends YangModule {

    private static final Logger LOG = LoggerFactory.getLogger(OnapSystem.class);
    public static final String NAMESPACE = "urn:onap:system";
    public static final QNameModule ONAPSYSTEM_2020_10_26 =
            QNameModule.create(XMLNamespace.of(NAMESPACE), Revision.of("2020-10-26"));
    public static final QNameModule ONAPSYSTEM_2022_11_04 =
            QNameModule.create(XMLNamespace.of(NAMESPACE), Revision.of("2022-11-04"));
    private static final List<QNameModule> MODULES = Arrays.asList(ONAPSYSTEM_2020_10_26, ONAPSYSTEM_2022_11_04);

    private final QName NAME;
    private final QName WEB_UI;
    private final QName GEOGRAPHICAL_LOCATION;

    OnapSystem(NetconfDomAccessor netconfDomAccessor, QNameModule module) {
        super(netconfDomAccessor, module);

        NAME = QName.create(module, "name");
        WEB_UI = QName.create(module, "web-ui");
        GEOGRAPHICAL_LOCATION = QName.create(module, "geographical-location");
    }

    public QName getName() {
        return NAME;
    }

    public QName getWebUi() {
        return WEB_UI;
    }

    public QName getGeoLocation() {
        return GEOGRAPHICAL_LOCATION;
    }

    // Read from device
    /**
     * Read system data with GUI cut through information from device if ONAP_SYSTEM YANG is supported.
     *
     * @return NormalizedNode data with GUI cut through information or null if not available.
     */
    public @Nullable NormalizedNode getOnapSystemData() {
        LOG.debug("Get System1 for mountpoint {}", netconfDomAccessor.getNodeId().getValue());
        @NonNull
        InstanceIdentifierBuilder ietfSystemIID =
                YangInstanceIdentifier.builder().node(ORanDeviceManagerQNames.IETF_SYSTEM_CONTAINER);
        @NonNull
        AugmentationIdentifier onapSystemIID = null;
        if (netconfDomAccessor.getCapabilites().isSupportingNamespaceAndRevision(ONAPSYSTEM_2020_10_26))
            onapSystemIID = YangInstanceIdentifier.AugmentationIdentifier.create(Sets.newHashSet(NAME, WEB_UI));
        else if (netconfDomAccessor.getCapabilites().isSupportingNamespaceAndRevision(ONAPSYSTEM_2022_11_04))
            onapSystemIID = YangInstanceIdentifier.AugmentationIdentifier
                    .create(Sets.newHashSet(NAME, WEB_UI, GEOGRAPHICAL_LOCATION));

        InstanceIdentifierBuilder augmentedOnapSystem =
                YangInstanceIdentifier.builder(ietfSystemIID.build()).node(onapSystemIID);

        Optional<NormalizedNode> res =
                netconfDomAccessor.readDataNode(LogicalDatastoreType.OPERATIONAL, augmentedOnapSystem.build());
        LOG.debug("Result of System1 = {}", res);
        return res.isPresent() ? res.get() : null;

    }

    /**
     * Get specific instance, depending on capabilities
     *
     * @param capabilities
     * @return
     */
    public static Optional<OnapSystem> getModule(NetconfDomAccessor netconfDomAccessor) {
        Capabilities capabilities = netconfDomAccessor.getCapabilites();
        for (QNameModule module : MODULES) {
            if (capabilities.isSupportingNamespaceAndRevision(module)) {
                return Optional.of(new OnapSystem(netconfDomAccessor, module));
            }
        }
        return Optional.empty();
    }


}
