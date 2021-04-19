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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.util;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ONFLayerProtocolName {

    MWAIRINTERFACE("MWPS"), //V1.0 + V1.2
    ETHERNETCONTAINER10("ETH-CTP"), //V1.0
    ETHERNETCONTAINER12("ETC"), //V1.2 + V1.2.1.1
    ETHERNETPHYSICAL("ETY"), //V1.2
    TDMCONTAINER("TDM"), //V1.2
    STRUCTURE("MWS"), //V1.0 + V1.2
    ETHERNET("ETH"), //V1.2
    UNKNOWN("");

    private static final Logger LOG = LoggerFactory.getLogger(ONFLayerProtocolName.class);

    private final String myLayerProtocolName;

    ONFLayerProtocolName(String myLayerProtocolName) {
        this.myLayerProtocolName = myLayerProtocolName;
    }

    public boolean is(
            org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.LayerProtocolName onfName) {
        return myLayerProtocolName.equals(onfName.getValue());
    }

    public static @NonNull ONFLayerProtocolName valueOf(
            org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.LayerProtocolName onfName) {
        for (ONFLayerProtocolName protocol : ONFLayerProtocolName.values()) {
            if (protocol.is(onfName)) {
                return protocol;
            }
        }
        LOG.info("Can not map {}. Use Unknown", onfName.getValue());
        return UNKNOWN;
    }

    public String getValue() {
        return myLayerProtocolName;
    }

}

