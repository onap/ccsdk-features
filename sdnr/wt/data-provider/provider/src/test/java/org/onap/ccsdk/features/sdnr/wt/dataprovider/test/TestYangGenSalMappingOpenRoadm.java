/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.apache.sshd.common.util.io.IoUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.jline.utils.Log;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.YangHelper2;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.DataProviderYangToolsMapper;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper2;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129.PmDataType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129.PmDataTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.openroadm.pm.types.rev200413.BIPErrorCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.openroadm.pm.types.rev200413.DelayTCM2Up;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.openroadm.pm.types.rev200413.OpticalPowerInputOSCMin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GranularityPeriodType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.entity.PerformanceData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.entity.PerformanceDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.grp.Measurement;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.grp.MeasurementBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.grp.MeasurementKey;
import org.opendaylight.yangtools.yang.common.Uint64;

public class TestYangGenSalMappingOpenRoadm extends Mockito {

    private static String resourceDirectoryPath = "/" + TestYangGenSalMappingOpenRoadm.class.getSimpleName() + "/";

    @Test
    public void testOpenroadmPMBuilder() throws IOException, ClassNotFoundException {
        out("Test: " + method());

        MeasurementBuilder measurementBuilder1 = new MeasurementBuilder();
        measurementBuilder1.setPmKey(OpticalPowerInputOSCMin.VALUE);
        measurementBuilder1.setPmValue(new PmDataType(Uint64.valueOf(64L)));
        Measurement measurement1 = measurementBuilder1.build();

        MeasurementBuilder measurementBuilder2 = new MeasurementBuilder();
        measurementBuilder2.setPmKey(BIPErrorCounter.VALUE);
        measurementBuilder2.setPmValue(new PmDataType(Uint64.valueOf(65L)));
        Measurement measurement2 = measurementBuilder2.build();

        PerformanceDataBuilder performanceDataBuilder = new PerformanceDataBuilder();
        performanceDataBuilder.setMeasurement(
                YangHelper2.getListOrMap(MeasurementKey.class, Arrays.asList(measurement1, measurement2)));

        PmdataEntityBuilder pmDataEntitybuilder = new PmdataEntityBuilder();
        pmDataEntitybuilder.setPerformanceData(performanceDataBuilder.build());

        PmdataEntity pmDataType = pmDataEntitybuilder.build();

        DataProviderYangToolsMapper mapper2 = new DataProviderYangToolsMapper();
        String jsonString = mapper2.writeValueAsString(pmDataType);
        out("Result json after mapping: " + jsonString);

        PmdataEntity generatepmdNode = mapper2.readValue(jsonString, PmdataEntity.class);
        out("Original: " + pmDataType.toString());
        out("Mapped  : " + generatepmdNode.toString());
        assertTrue("Can mapping not working", generatepmdNode.equals(pmDataType));
    }

    @Test
    public void testOpenroadmPMString1() throws IOException, ClassNotFoundException {
        out("Test: " + method());
        String jsonString2 = getFileContent("pmdata1.json");
        DataProviderYangToolsMapper mapper2 = new DataProviderYangToolsMapper();
        PmdataEntity generatepmdNode = mapper2.readValue(jsonString2.getBytes(), PmdataEntity.class);
        out("String1:"+generatepmdNode.toString()); // Print it with specified indentation
        assertTrue("GranularityPeriod", generatepmdNode.getGranularityPeriod().equals(GranularityPeriodType.Period15Min));
        assertTrue("NodeName", generatepmdNode.getNodeName().equals("NTS_RDM2"));
        @Nullable PerformanceData performanceData = generatepmdNode.getPerformanceData();
        assertNotNull("PerformanceData", performanceData);
        @Nullable Map<MeasurementKey, Measurement> measurement = performanceData.getMeasurement();
        assertNotNull("Measurement", measurement);
        Measurement measurement1 = measurement.get(new MeasurementKey(OpticalPowerInputOSCMin.VALUE));
        assertTrue("Measurement=64", measurement1.getPmValue().stringValue().equals("64"));
    }

    @Test
    public void testOpenroadmPMString2() throws IOException, ClassNotFoundException {
        out("Test: " + method());
        PmDataTypeBuilder.getDefaultInstance("11298624220985537708");
        String jsonString2 = getFileContent("pmdata2.json");
        DataProviderYangToolsMapper mapper2 = new DataProviderYangToolsMapper();
        PmdataEntity generatepmdNode = mapper2.readValue(jsonString2, PmdataEntity.class);
        out(generatepmdNode.toString()); // Print it with specified indentation
    }

    @Test
    public void testOpenroadmPMString3() throws IOException, ClassNotFoundException {
        out("Test: " + method());
        String jsonString2 = getFileContent("pmdata3.json");
        YangToolsMapper2<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data> mapper2 =
                new YangToolsMapper2<>(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data.class,
                        null);
        PmdataEntity generatepmdNode = mapper2.readValue(jsonString2.getBytes(), PmdataEntity.class);
        out(generatepmdNode.toString()); // Print it with specified indentation
    }


    @Test
    public void testOpenroadmPMString4() throws IOException, ClassNotFoundException {
        out("Test: " + method());
        String jsonString = getFileContent("pmdata3.json");
        YangToolsMapper2<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data> mapper =
                new YangToolsMapper2<>(
                        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data.class,
                        null);
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data data =
                mapper.readValue(jsonString.getBytes(),
                        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data.class);

        assertTrue("GranularityPeriod", data.getGranularityPeriod().equals(GranularityPeriodType.Period15Min));
        assertTrue("NodeName", data.getNodeName().equals("openroadm1"));
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata15m.entity.@Nullable PerformanceData performanceData =
                data.getPerformanceData();
        assertNotNull("PerformanceData", performanceData);
        @Nullable Map<MeasurementKey, Measurement> measurement = performanceData.getMeasurement();
        assertNotNull("Measurement", measurement);
        Measurement measurement1 = measurement.get(new MeasurementKey(DelayTCM2Up.VALUE));
        assertTrue("Measurement=11298624220985537708", measurement1.getPmValue().stringValue().equals("11298624220985537708"));
    }
    /*
     * --------------------------------- Private
     */

    private static String method() {
        String nameofCurrMethod = new Throwable().getStackTrace()[1].getMethodName();
        return nameofCurrMethod;
    }

    private static void out(String text) {
        Log.info("Log: " + text);
    }

    private static String getFileContent(String filename) throws IOException {
        return String.join("\n",
                IoUtils.readAllLines(TestTree.class.getResourceAsStream(resourceDirectoryPath + filename)));
    }

}
