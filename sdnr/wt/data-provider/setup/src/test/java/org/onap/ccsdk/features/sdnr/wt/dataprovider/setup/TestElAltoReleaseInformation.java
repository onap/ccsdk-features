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
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.elalto.ElAltoReleaseInformation;

public class TestElAltoReleaseInformation {

    @Test
    public void test() {
        ElAltoReleaseInformation ri = new ElAltoReleaseInformation();
        Set<ComponentName> components = ri.getComponents();
        assertFalse(components.contains(ComponentName.CONNECTIONLOG));
        assertTrue(components.contains(ComponentName.EVENTLOG));
        assertEquals("sdnevents", ri.getAlias(ComponentName.FAULTLOG));
        assertEquals("sdnevents_v1", ri.getIndex(ComponentName.FAULTLOG));
        assertNull(ri.getConverter(Release.EL_ALTO, ComponentName.CONNECTIONLOG));
        assertNotNull(ri.getConverter(Release.EL_ALTO, ComponentName.FAULTCURRENT));
    }

}
