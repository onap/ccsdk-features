/*******************************************************************************
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
 *  ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property.
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.container;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.database.types.EsHistoricalPerformance15Minutes;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.database.types.EsHistoricalPerformance24Hours;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class AllPmTest {

    private AllPm allPm;

    @Before
    public void setUp(){
        allPm = AllPm.getEmpty();
    }

    @Test
    public void shouldCreateEmptyInstance() {
        assertEquals(0, allPm.size());
    }


    @Test
    public void shouldBePossibleToAdd15MinutesPerformanceMeasurements() {
        // given
        final EsHistoricalPerformance15Minutes esHistoricalPerformance15Minutes_1 = mock(EsHistoricalPerformance15Minutes.class);
        final EsHistoricalPerformance15Minutes esHistoricalPerformance15Minutes_2 = mock(EsHistoricalPerformance15Minutes.class);

        allPm.add(esHistoricalPerformance15Minutes_1);
        allPm.add(esHistoricalPerformance15Minutes_2);


        // when
        final List<EsHistoricalPerformance15Minutes> pm15size = allPm.getPm15();
        final List<EsHistoricalPerformance24Hours> pm24size = allPm.getPm24();

        // then
        assertEquals(2, pm15size.size());
        assertEquals(0, pm24size.size());
    }

    @Test
    public void shouldBePossibleToAdd24HoursPerformanceMeasurements() {
        // given
        final EsHistoricalPerformance24Hours esHistoricalPerformance24Hours_1 = mock(EsHistoricalPerformance24Hours.class);
        final EsHistoricalPerformance24Hours esHistoricalPerformance24Hours_2 = mock(EsHistoricalPerformance24Hours.class);

        allPm.add(esHistoricalPerformance24Hours_1);
        allPm.add(esHistoricalPerformance24Hours_2);


        // when
        final List<EsHistoricalPerformance15Minutes> pm15size = allPm.getPm15();
        final List<EsHistoricalPerformance24Hours> pm24size = allPm.getPm24();

        // then
        assertEquals(0, pm15size.size());
        assertEquals(2, pm24size.size());
    }

    @Test
    public void shouldBePossibleToAddPerformanceMeasurements() {
        // given
        final EsHistoricalPerformance15Minutes esHistoricalPerformance15Minutes = mock(EsHistoricalPerformance15Minutes.class);
        final EsHistoricalPerformance24Hours esHistoricalPerformance24Hours = mock(EsHistoricalPerformance24Hours.class);

        allPm.add(esHistoricalPerformance15Minutes);
        allPm.add(esHistoricalPerformance24Hours);

        // when
        final int size = allPm.size();

        // then
        assertEquals(2, size);
    }


}
