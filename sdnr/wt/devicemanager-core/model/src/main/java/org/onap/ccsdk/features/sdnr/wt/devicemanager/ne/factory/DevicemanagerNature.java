/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2023 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory;

public class DevicemanagerNature implements Comparable<DevicemanagerNature> {

    /** Specific devicemanager */
    public static final DevicemanagerNature SPECIFIC = new DevicemanagerNature(10);
    /** Priority of mid specfic types */
    public static final DevicemanagerNature NORMAL = new DevicemanagerNature(100);
    /** Common devicemanager with basic functionality for standard */
    public static final DevicemanagerNature COMMON = new DevicemanagerNature(250);
    /** Priority of "NetworkElementFactory" types */
    public static final DevicemanagerNature NETWORKELEMENT_FACTORY_DEFAULT = new DevicemanagerNature(Integer.MAX_VALUE);

    //Low numbers have the highest priority for devicemanager selection
    private final int nature;

    private DevicemanagerNature(int nature) {
        super();
        this.nature = nature;
    }

    @Override
    public int compareTo(DevicemanagerNature o) {
        return Integer.compare(nature, o.nature);
    }

    public static DevicemanagerNature getDefaultDevicemanagerNature(NetworkElementFactory a) {
        return (a instanceof NetworkElementFactory2) ? ((NetworkElementFactory2) a).getDevicemanagerNature()
                : NETWORKELEMENT_FACTORY_DEFAULT;
    }

    public static int compareTo(NetworkElementFactory a, NetworkElementFactory b) {
        return getDefaultDevicemanagerNature(a).compareTo(getDefaultDevicemanagerNature(b));
    }

}
