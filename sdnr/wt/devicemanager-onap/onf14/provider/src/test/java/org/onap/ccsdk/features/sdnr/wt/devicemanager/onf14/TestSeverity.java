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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.interfaces.Onf14AirInterface;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.interfaces.Onf14WireInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;

public class TestSeverity extends Mockito {

    @Test
    public void test1_1() {
        assertTrue(Onf14WireInterface.mapSeverity(
                org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPECRITICAL.class)
                .equals(SeverityType.Critical));
    }

    @Test
    public void test1_2() {
        assertTrue(Onf14WireInterface.mapSeverity(
                org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPEWARNING.class)
                .equals(SeverityType.Warning));
    }

    @Test
    public void test1_3() {
        assertTrue(Onf14WireInterface.mapSeverity(
                org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPEMINOR.class)
                .equals(SeverityType.Minor));
    }

    @Test
    public void test1_4() {
        assertTrue(Onf14WireInterface.mapSeverity(
                org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPEMAJOR.class)
                .equals(SeverityType.Major));
    }

    public void test2_1() {
        assertTrue(Onf14AirInterface.mapSeverity(
                org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.SEVERITYTYPECRITICAL.class)
                .equals(SeverityType.Critical));
    }

    @Test
    public void test2_2() {
        assertTrue(Onf14AirInterface.mapSeverity(
                org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.SEVERITYTYPEMINOR.class)
                .equals(SeverityType.Minor));
    }

    @Test
    public void test2_3() {
        assertTrue(Onf14AirInterface.mapSeverity(null).equals(SeverityType.NonAlarmed));
    }


}
