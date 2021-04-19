/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util;

import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;

/**
 * Converts time stamps into internal format according to ONF1.2 and ISO 8601. To be replaced by NetconfTimeStampImpl
 */
@Deprecated
public class InternalDateAndTime {

    private static final String TESTPATTERNSTRING = "2017-01-01T00:00:00.0Z";
    private static final String INITIALPATTERN = "0000-00-00T00:00:00.0Z";

    private static final InternalDateAndTime TESTPATTERN = new InternalDateAndTime(TESTPATTERNSTRING);
    private static final DateAndTime TESTPATTERN2 = new DateAndTime(TESTPATTERNSTRING);

    private static final NetconfTimeStamp NETCONFTIME_CONVERTER = NetconfTimeStampImpl.getConverter();

    /** Internal variable to hold the value, that is converted **/
    private final String internalDateAndTime;

    /**
     * Static builder ONF1.0
     * 
     * @param time in ONF1.0 yang format
     * @return InternalDateAndTime
     */

    public static InternalDateAndTime valueOf(
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime time) {
        return new InternalDateAndTime(time);
    }

    /**
     * @return Getter with String representation
     */
    public String getValue() {
        return internalDateAndTime;
    }

    /**
     * Get a testpattern
     * 
     * @return testpattern
     */
    public static InternalDateAndTime getTestpattern() {
        return TESTPATTERN;
    }

    /**
     * @return DateAndTime testpattern
     */
    public static @Nullable DateAndTime getTestpatternDateAndTime() {
        return TESTPATTERN2;
    }

    /*----------------------------------------------------------------
     * Private constructors
     */

    /**
     * Convert ONF 1.2 DateAndTime to String
     * 
     * @param time as input
     */
    private InternalDateAndTime(
            org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime time) {
        if (time != null) {
            internalDateAndTime = NETCONFTIME_CONVERTER.getTimeStampFromNetconf(time.getValue());
        } else {
            internalDateAndTime = INITIALPATTERN;
        }
    }

    /**
     * Setup static TEST
     * 
     * @param internalDateAndTime
     */
    private InternalDateAndTime(String internalDateAndTime) {
        this.internalDateAndTime = internalDateAndTime;
    }

}
