/*******************************************************************************
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.frankfurt.FrankfurtReleaseInformation;

public class TestFrankfurtReleaseInformation {

    @Test
    public void test() {
        FrankfurtReleaseInformation ri = new FrankfurtReleaseInformation();
        Set<ComponentName> components = ri.getComponents();
        assertFalse(components.contains(ComponentName.INVENTORYTOPLEVEL));
        assertTrue(components.contains(ComponentName.EVENTLOG));
        assertEquals("faultlog",ri.getAlias(ComponentName.FAULTLOG));
        assertEquals("faultlog-v2",ri.getIndex(ComponentName.FAULTLOG));
        assertNull(ri.getConverter(Release.FRANKFURT_R1,ComponentName.INVENTORYTOPLEVEL));
        assertNotNull(ri.getConverter(Release.FRANKFURT_R1, ComponentName.FAULTCURRENT));
    }

}
