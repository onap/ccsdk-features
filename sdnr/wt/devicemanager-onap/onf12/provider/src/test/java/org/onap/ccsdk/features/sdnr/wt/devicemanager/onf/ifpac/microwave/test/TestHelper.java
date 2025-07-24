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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.Helper;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.LayerProtocolName;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.g._874._1.model.rev170320.GranularityPeriodType;

public class TestHelper {

    @Test
    public void test() {

        assertEquals(GranularityPeriodType.Period24Hours,
                Helper.nnGetGranularityPeriodType(GranularityPeriodType.Period24Hours));
        assertEquals(GranularityPeriodType.Unknown, Helper.nnGetGranularityPeriodType(null));

        assertEquals(new LayerProtocolName("TDM"),
                Helper.nnGetLayerProtocolName(LayerProtocolName.getDefaultInstance("TDM")));
        assertEquals(new LayerProtocolName("default"), Helper.nnGetLayerProtocolName(null));

        assertEquals("TEST", Helper.nnGetString("TEST"));
        assertEquals("", Helper.nnGetString(null));

        assertEquals(new UniversalId("ABC"), Helper.nnGetUniversalId(new UniversalId("ABC")));
        assertEquals(new UniversalId("Default"), Helper.nnGetUniversalId(null));

    }

}
