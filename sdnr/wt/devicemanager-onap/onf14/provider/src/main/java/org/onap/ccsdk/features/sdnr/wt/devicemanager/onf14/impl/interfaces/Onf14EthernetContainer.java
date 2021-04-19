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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.interfaces;

import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.dataprovider.InternalDataModelSeverity;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.SEVERITYTYPE;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author alexs
 *
 */
public class Onf14EthernetContainer {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(Onf14EthernetContainer.class);

    public static SeverityType mapSeverity(
            Class<? extends SEVERITYTYPE> severity) {
        return InternalDataModelSeverity.mapSeverity(severity);
    }

}
