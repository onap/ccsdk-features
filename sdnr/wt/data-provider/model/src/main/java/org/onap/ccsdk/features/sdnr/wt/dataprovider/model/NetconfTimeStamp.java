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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.model;

import java.time.LocalDateTime;
import java.util.Date;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;

/**
 * @author herbert
 *
 */
public interface NetconfTimeStamp {

    /**
     * Get actual timestamp as NETCONF specific type NETCONF/YANG 1.0 Format
     *
     * @return String with Date in NETCONF/YANG Format Version 1.0.
     */
    String getTimeStampAsNetconfString();

    /**
     * Get actual timestamp as NETCONF specific type NETCONF/YANG 1.0 Format
     * 
     * @return String with Date in NETCONF/YANG Format Version 1.0.
     */
    String getTimeStampAsNetconfString(Date date);

    /**
     * Get actual timestamp as NETCONF specific type NETCONF/YANG 1.0 Format in GMT
     *
     * @return DateAndTime Type 1.0. Date in NETCONF/YANG Format Version 1.0.
     */
    DateAndTime getTimeStamp();

    /**
     * Get time from date as NETCONF specific type NETCONF/YANG 1.0 Format in GMT
     * 
     * @param date specifying the date and time
     * @return DateAndTime Type 1.0. Date in NETCONF/YANG Format Version 1.0.
     */
    DateAndTime getTimeStamp(Date date);

    /**
     * Get time from date as NETCONF specific type NETCONF/YANG 1.0 Format in GMT
     * 
     * @param date specifying the date and time
     * @return DateAndTime Type 1.0. Date in NETCONF/YANG Format Version 1.0.
     */
    DateAndTime getTimeStamp(String date);

    /**
     * Return the String with a NETCONF time converted to long
     *
     * @param netconfTime as String according the formats given above
     * @return Epoch milliseconds
     * @throws IllegalArgumentException In case of no compliant time format definition for the string
     */
    long getTimeStampFromNetconfAsMilliseconds(String netconfTime) throws IllegalArgumentException;

    /**
     * Deliver String result.
     *
     * @param netconfTime as String according the formats given above
     * @return If successful: String in ISO8601 Format for database and presentation. If "wrong formed input" the Input
     *         string with the prefix "Maleformed date" is delivered back.
     */
    String getTimeStampFromNetconf(String netconfTime);

    Date getDateFromNetconf(String netconfTime);

    String getTimeStampAsNetconfString(LocalDateTime dt);

}
