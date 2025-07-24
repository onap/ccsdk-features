/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
 *  ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.ZonedDateTime;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.maintenance.impl.MaintenanceCalculator;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceBuilder;

public class TestMaintenanceTimeFilter {

    private static String DEFAULT1 =
            "EsMaintenanceFilter [start=1970-01-01T00:00Z[UTC], end=1970-01-01T00:00Z[UTC], definition=EsMaintenanceFilterDefinition [objectIdRef=, problem=], description=]";
    private static String DEFAULT2 =
            "EsMaintenanceFilter [start=1970-01-01T00:00Z[UTC], end=2018-01-01T10:00+05:00, definition=EsMaintenanceFilterDefinition [objectIdRef=, problem=], description=]";

    @Test
    public void testBasic() {

        boolean res;

        DateAndTime start = new DateAndTime("2018-01-01T10:00:00+05:00");
        DateAndTime end = new DateAndTime("2019-01-01T10:00:00+05:00");
        ZonedDateTime now;

        now = ZonedDateTime.parse("2017-05-01T10:00:00+05:00");
        res = MaintenanceCalculator.isInPeriod(start, end, now);
        System.out.println("Before: " + res);
        assertFalse("before period", res);

        now = ZonedDateTime.parse("2018-05-01T10:00:00+05:00");
        res = MaintenanceCalculator.isInPeriod(start, end, now);
        System.out.println("Within: " + res);
        assertTrue("within period", res);

        now = ZonedDateTime.parse("2019-05-01T10:00:00+05:00");
        res = MaintenanceCalculator.isInPeriod(start, end, now);
        System.out.println("After: " + res);
        assertFalse("after period", res);

    }

    @Test
    public void testBasic2() {

        MaintenanceBuilder mb = new MaintenanceBuilder();

        mb.setActive(true);
        mb.setStart(new DateAndTime("1999-01-01T00:00:00Z"));
        mb.setEnd(new DateAndTime("2001-01-01T00:00:00Z"));
        mb.setId("id1");
        mb.setObjectIdRef("Interface1");
        mb.setProblem("Problem1");

        boolean res;
        ZonedDateTime now;

        now = MaintenanceCalculator.valueOf("2000-01-01T00:00Z");
        res = MaintenanceCalculator.isONFObjectInMaintenance(mb.build(), "Interface1", "Problem1", now);
        assertTrue("within period", res);

        now = MaintenanceCalculator.valueOf("2002-01-01T00:00Z");
        res = MaintenanceCalculator.isONFObjectInMaintenance(mb.build(), "", "", now);
        assertFalse("outside period", res);

    }

}
