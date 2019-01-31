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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.database.types;

import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.Lp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.g._874._1.model.rev170320.OtnHistoryDataG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EsHistoricalPerformance15Minutes extends EsHistoricalPerformanceBase {

    public static final String ESDATATYPENAME = "historicalperformance15min";

    private static final Logger LOG = LoggerFactory.getLogger(EsHistoricalPerformance15Minutes.class);

    public EsHistoricalPerformance15Minutes(String nodeName, Lp lp) {
        super(nodeName, lp);
    }

    public <T extends OtnHistoryDataG> EsHistoricalPerformance15Minutes setHistoricalRecord15Minutes(OtnHistoryDataG record) {
        if (record.getGranularityPeriod() != org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.g._874._1.model.rev170320.GranularityPeriodType.Period15Min) {
            LOG.warn("Granularity mismatch for {} expected {} found {}", getNodeName(),getUuidInterface(), org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.g._874._1.model.rev170320.GranularityPeriodType.Period15Min, record.getGranularityPeriod());
        }
        set(record);
        return this;
    }

}
