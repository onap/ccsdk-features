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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl;

import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPE;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPECRITICAL;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPEMAJOR;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPEMINOR;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPENONALARMED;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPEWARNING;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author alexs
 *
 */
public class Onf14WireInterface {

    private static final Logger log = LoggerFactory.getLogger(Onf14WireInterface.class);

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType mapSeverity(
            Class<? extends SEVERITYTYPE> severity) {

        if (severity != null) {
            if (severity.getTypeName() == SEVERITYTYPECRITICAL.class.getName()) {
                return org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType.Critical;
            } else if (severity.getTypeName() == SEVERITYTYPEMAJOR.class.getName()) {
                return org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType.Major;
            } else if (severity.getTypeName() == SEVERITYTYPEMINOR.class.getName()) {
                return org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType.Minor;
            } else if (severity.getTypeName() == SEVERITYTYPEWARNING.class.getName()) {
                return org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType.Warning;
            } else if (severity.getTypeName() == SEVERITYTYPENONALARMED.class.getName()) {
                return org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType.NonAlarmed;
            }
        }

        return org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType.NonAlarmed;
    }


}
