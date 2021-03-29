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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.test;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.hardware.rev180313.Fan;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.hardware.rev180313.HardwareClass;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.hardware.rev180313.Port;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana.hardware.rev180313.Sensor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.AdminState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.OperState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.SensorStatus;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.SensorValue;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.SensorValueType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.Component;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.ComponentBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.component.SensorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.component.SensorDataBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.component.State;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.component.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;

public class ComponentHelper {

    public static Component get(String name, String dateTimeString) {
        DateAndTime dateTime = new DateAndTime(dateTimeString);
        Uuid uuid = new Uuid("0Aabcdef-0abc-0cfD-0abC-0123456789AB");

        ComponentBuilder componentBuilder = new ComponentBuilder();
        componentBuilder.setParent("Shelf").setName("Slot-0").setParentRelPos(0);
        componentBuilder.setUuid(uuid);
        componentBuilder.setContainsChild(Arrays.asList("Card-01A", "Card-01B"));
        componentBuilder.setDescription("ORAN Network Element NO-456");
        componentBuilder.setXmlClass(TestHardwareClass.class);
        componentBuilder.setMfgName("Nokia");
        componentBuilder.setMfgDate(dateTime);
        return componentBuilder.build();
    }

    public static List<Component> getComponentList(String resourceName) {
        try (Scanner scanner = new Scanner(ComponentHelper.class.getResourceAsStream(resourceName), "UTF-8")) {
            String jsonString = scanner.useDelimiter("\\A").next();
            JSONObject jsonHardware = new JSONObject(jsonString).getJSONObject("hardware");
            JSONArray jsonComponentArray = jsonHardware.getJSONArray("component");
            return IntStream.range(0, jsonComponentArray.length())
                    .mapToObj(idx -> ComponentHelper.get(jsonComponentArray.getJSONObject(idx)))
                    .collect(Collectors.toList());
        }
    }

    public static Component get(JSONObject jsonComponent) {
        ComponentBuilder componentBuilder = new ComponentBuilder();
        componentBuilder.setName(getString(jsonComponent, "name"));
        componentBuilder.setParent(getString(jsonComponent, "parent"));
        componentBuilder.setParentRelPos(getInteger(jsonComponent, "parent-rel-pos"));
        componentBuilder.setAlias(getString(jsonComponent, "alias"));
        componentBuilder.setXmlClass(getXmlClass(jsonComponent, "class"));
        componentBuilder.setState(getState(jsonComponent, "state"));
        componentBuilder.setDescription(getString(jsonComponent, "description"));
        componentBuilder.setContainsChild(getStringArray(jsonComponent, "contains-child"));
        componentBuilder.setSensorData(getSensorData(jsonComponent, "sensor-data"));
        componentBuilder.setFirmwareRev(getString(jsonComponent, "firmware-rev"));
        componentBuilder.setSerialNum(getString(jsonComponent, "serial-num"));
        componentBuilder.setSoftwareRev(getString(jsonComponent, "software-rev"));
        return componentBuilder.build();
    }

    // Private

    private static State getState(JSONObject jsonComponent, String key) {
        if (jsonComponent.has(key)) {
            JSONObject jsonState = jsonComponent.getJSONObject(key);
            StateBuilder stateBuilder = new StateBuilder();
            stateBuilder.setOperState(getString(jsonState, "oper-state", value -> OperState.forName(value)).get());
            stateBuilder.setAdminState(getString(jsonState, "admin-state", value -> AdminState.forName(value)).get());
            return stateBuilder.build();
        }
        return null;
    }

    private static SensorData getSensorData(JSONObject jsonComponent, String key) {
        if (jsonComponent.has(key)) {
            JSONObject jsonSonsor = jsonComponent.getJSONObject(key);
            SensorDataBuilder sensorBuilder = new SensorDataBuilder();
            sensorBuilder.setValueTimestamp(getString(jsonSonsor, "value-timestamp", value -> new DateAndTime(value)));
            sensorBuilder.setValue(getInteger(jsonSonsor, "value", value -> new SensorValue(value)));
            sensorBuilder
                    .setValueType(getString(jsonSonsor, "value-type", value -> SensorValueType.forName(value).get()));
            sensorBuilder
                    .setOperStatus(getString(jsonSonsor, "oper-status", value -> SensorStatus.forName(value).get()));
            return sensorBuilder.build();
        }
        return null;
    }

    // Get data types
    private static Class<? extends HardwareClass> getXmlClass(JSONObject jsonComponent, String key) {
        return getString(jsonComponent, key, value -> {
            switch (value) {
                case "iana-hardware:sensor":
                    return Sensor.class;
                case "iana-hardware:port":
                    return Port.class;
                case "iana-hardware:fan":
                    return Fan.class;
                default:
                    return HardwareClass.class;
            }
        });
    }

    private static String getString(JSONObject jsonObject, String key) {
        return getString(jsonObject, key, value -> value);
    }

    private static Integer getInteger(JSONObject jsonObject, String key) {
        return getInteger(jsonObject, key, value -> value);
    }

    private interface ConvertString<T> {
        T convert(String value);
    }

    private static <T> T getString(JSONObject jsonObject, String key, ConvertString<T> convert) {
        if (jsonObject.has(key)) {
            String value = jsonObject.getString(key);
            return convert.convert(value);
        }
        return null;
    }

    private interface ConvertInteger<T> {
        T convert(int value);
    }

    private static <T> T getInteger(JSONObject jsonObject, String key, ConvertInteger<T> convert) {
        if (jsonObject.has(key)) {
            int value = jsonObject.getInt(key);
            return convert.convert(value);
        }
        return null;
    }

    private static List<String> getStringArray(JSONObject jsonComponent, String key) {
        if (jsonComponent.has(key)) {
            JSONArray stringArray = jsonComponent.getJSONArray(key);
            return IntStream.range(0, stringArray.length()).mapToObj(idx -> stringArray.getString(idx))
                    .collect(Collectors.toList());
        }
        return null;
    }

}
