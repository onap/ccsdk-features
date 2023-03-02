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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.HistoricalPmList;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.group.HistoricalPm;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.list.HistoricalPmEntry;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.val.group.Measurement;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129.PmDataType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129.PmGranularity;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129.PmNamesEnum;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.g836.pm.types.rev200413.ErroredSecond;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.g836.pm.types.rev200413.SeverelyErroredSecond;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.openroadm.pm.types.rev200413.PerformanceMeasurementTypeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GranularityPeriodType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.entity.PerformanceDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.grp.MeasurementBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.grp.MeasurementKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.units.rev200413.Celsius;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.units.rev200413.DB;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.units.rev200413.DBm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.units.rev200413.Fahrenheit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.units.rev200413.KHz;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.units.rev200413.MW;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.units.rev200413.PerformanceMeasurementUnitId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shabnam
 * <p>
 * Reading Openroadm PM data and returning as PmDataEntitiy data
 */
public class PmDataBuilderOpenRoadm {
    // variables
    private static final Logger log = LoggerFactory.getLogger(PmDataBuilderOpenRoadm.class);
    private PmdataEntityBuilder pmDataBuilder;
    private Bundle b = FrameworkUtil.getBundle(this.getClass());

    // end of variables
    // constructors
    public PmDataBuilderOpenRoadm(NetconfBindingAccessor accessor) {
        this.pmDataBuilder = new PmdataEntityBuilder();
        this.pmDataBuilder.setNodeName(accessor.getNodeId().getValue());
    }

    // end of constructors
    // public methods
    // Read PM data
    public HistoricalPmList getPmData(NetconfBindingAccessor accessor) {
        final Class<HistoricalPmList> pmDataClass = HistoricalPmList.class;
        log.debug("Get PM data for element {}", accessor.getNodeId().getValue());
        InstanceIdentifier<HistoricalPmList> pmDataIid = InstanceIdentifier.builder(pmDataClass).build();
        return accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                pmDataIid);

    }

    // Build PM entity for writing into the database
    public List<PmdataEntity> buildPmDataEntity(HistoricalPmList historicalPmEnitityList) {
        List<PmdataEntity> pmEntitiyList = new ArrayList<>();
        if (historicalPmEnitityList == null) {
            return pmEntitiyList;
        }
        Collection<HistoricalPmEntry> pmDataEntryList =
                YangHelper.getCollection(historicalPmEnitityList.getHistoricalPmEntry());
        for (HistoricalPmEntry pmDataEntry : pmDataEntryList) {
            pmDataBuilder.setUuidInterface(pmDataEntry.getPmResourceType().getName());
            Collection<HistoricalPm> historicalPmList = YangHelper.getCollection(pmDataEntry.getHistoricalPm());
            for (HistoricalPm historicalPm : historicalPmList) {
                log.debug("PmName:{}", historicalPm.getType());
                //              pmDataBuilder.setPerformanceData(value)

                try {
                    writeperformanceData(historicalPm);
                } catch (ClassNotFoundException e) {
                    log.debug("No relevant data found");
                }
                pmEntitiyList.add(this.pmDataBuilder.build());

                log.debug("PmListSize before db writing: {}", pmEntitiyList.size());
            }
            log.debug("PmListSize before db writing: {}", pmEntitiyList.size());
        }
        return pmEntitiyList;
    }
    // end of public methods

    // private methods
    private void writeperformanceData(HistoricalPm historicalPm) throws ClassNotFoundException {
        Collection<Measurement> measurementList = YangHelper.getCollection(historicalPm.getMeasurement());
        Map<MeasurementKey, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.grp.Measurement>
                measurementMap = new HashMap<>();
        // Map Performance data of PmDataEntity with MeasurmentData-HistoricalPm
        PerformanceDataBuilder performanceDataBuilder = new PerformanceDataBuilder();
        for (Measurement measurementData : measurementList) {
            this.pmDataBuilder.setGranularityPeriod(mapGranularityPeriod(measurementData.getGranularity()))
                    .setTimeStamp(measurementData.getCompletionTime());
            if (measurementData.getValidity().getName().equals("suspect")) {
                this.pmDataBuilder.setSuspectIntervalFlag(true);
            }
            measurementMap.put(
                    new MeasurementKey(measurementBuilder(historicalPm.getType(), measurementData.getPmParameterUnit(),
                            measurementData.getPmParameterValue()).getPmKey()),
                    measurementBuilder(historicalPm.getType(), measurementData.getPmParameterUnit(),
                            measurementData.getPmParameterValue()));


        }

        pmDataBuilder.setPerformanceData(performanceDataBuilder.setMeasurement(measurementMap).build());
    }


    // Mapping Granularity period of PmDataEntity with PmGranularity of MeasurmentData-HistoricalPm
    private GranularityPeriodType mapGranularityPeriod(PmGranularity pmGranularity) {

        GranularityPeriodType granPeriod = null;
        switch (pmGranularity.getName()) {
            case ("notApplicable"):
                granPeriod = GranularityPeriodType.Unknown;
                break;
            case ("15min"):
                granPeriod = GranularityPeriodType.Period15Min;
                break;
            case ("24Hour"):
                granPeriod = GranularityPeriodType.Period24Hours;
                break;
            default:
                granPeriod = GranularityPeriodType.Period15Min;
                break;
        }
        return granPeriod;
    }

    private List<Class<? extends PerformanceMeasurementTypeId>> setMeasurementTypeId() {
        String packageName =
                "org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.openroadm.pm.types.rev200413";
        String packageName1 =
                "/org/opendaylight/yang/gen/v1/urn/opendaylight/params/xml/ns/yang/data/provider/openroadm/pm/types/rev200413/";
        List<Class<? extends PerformanceMeasurementTypeId>> measTypeObjList =
                new ArrayList<>();
        URL root = Thread.currentThread().getContextClassLoader().getResource(packageName1);

        log.debug("path for type package: {}", root);

        Enumeration<URL> results = getFileURL(b, packageName);
        log.debug("Found Packages {}", results);
        if (results != null) {
            while (results.hasMoreElements()) {
                URL path = results.nextElement();

                Class<?> cls1 = loadClass(b, path.getFile());

                if (PerformanceMeasurementTypeId.class.isAssignableFrom(cls1)) {
                    measTypeObjList.add((Class<? extends PerformanceMeasurementTypeId>) cls1);

                    log.debug("Class Added {}", cls1.getSimpleName());
                }
            }
        }

        return measTypeObjList;
    }

    private PerformanceMeasurementUnitId setMeasurementUnit(String unitName) {

        switch (unitName) {
            case ("celsius"):
                return Celsius.VALUE;
            case ("dB"):
                return DB.VALUE;
            case ("dBm"):
                return DBm.VALUE;
            case ("fahrenheit"):
                return Fahrenheit.VALUE;
            case ("kHz"):
                return KHz.VALUE;
            case ("mW"):
                return MW.VALUE;
            default:
                break;
        }
        return null;
    }

    private org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.pmdata.grp.Measurement measurementBuilder(
            PmNamesEnum pmType, String pmUnit, PmDataType pmDataType) {

        MeasurementBuilder measBuilder = new MeasurementBuilder();
        if (pmType.getName().equals("erroredSeconds")) {
            measBuilder.setPmKey(ErroredSecond.VALUE);
        } else if (pmType.getName().equals("severelyErroredSeconds")) {
            measBuilder.setPmKey(SeverelyErroredSecond.VALUE);
        } else {
            for (Class<? extends PerformanceMeasurementTypeId> obj : setMeasurementTypeId()) {
                if (obj.toString().contains(pmType.name())) {
                    measBuilder.setPmKey(YangToolsMapperHelper.getIdentityValueFromClass(obj) );
                }
            }
        }
        measBuilder.setPmUnit(setMeasurementUnit(pmUnit));
        measBuilder.setPmValue(pmDataType);
        return measBuilder.build();

    }

    private Class<?> loadClass(Bundle bundle, String classFilePath) {
        String className = classFilePath.replaceFirst("^/", "").replace('/', '.').replaceFirst(".class$", "");
        try {
            return bundle.loadClass(className);
        } catch (Exception e) {
            log.debug(String.format("Class [%s] could not be loaded. Message: [%s].", className, e.getMessage()));
        }
        return null;
    }

    private static Enumeration<URL> getFileURL(Bundle b, String classPath) {

        BundleContext context = b == null ? null : b.getBundleContext();
        if (context == null) {
            log.debug("no bundle context available");
            return null;
        }
        Bundle[] bundles = context.getBundles();
        if (bundles == null || bundles.length <= 0) {
            log.debug("no bundles found");
            return null;
        }
        log.debug("found {} bundles", bundles.length);
        Enumeration<URL> resultUrl = null;

        for (Bundle bundle : bundles) {
            resultUrl = bundle.findEntries("/" + classPath.replace(".", "/"), "*.class", false);
            if (resultUrl != null) {
                break;

            }

        }

        return resultUrl;
    }


    // end of private methods
}

