/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
 * =================================================================================================
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

package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup;

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
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.istanbul.IstanbulReleaseInformation;

public class TestIstanbulReleaseInformation {

    @Test
    public void testReleaseInformation() {
        IstanbulReleaseInformation ri = new IstanbulReleaseInformation();
        Set<ComponentName> components = ri.getComponents();
        assertTrue(components.contains(ComponentName.USERDATA));
        assertTrue(components.contains(ComponentName.REQUIRED_NETWORKELEMENT));
        assertTrue(components.contains(ComponentName.CMLOG));
        assertEquals("userdata", ri.getAlias(ComponentName.USERDATA));
        assertEquals("networkelement-connection", ri.getAlias(ComponentName.REQUIRED_NETWORKELEMENT));
        assertEquals("cmlog", ri.getAlias(ComponentName.CMLOG));
        assertEquals("cmlog-v6", ri.getIndex(ComponentName.CMLOG));
    }

}
