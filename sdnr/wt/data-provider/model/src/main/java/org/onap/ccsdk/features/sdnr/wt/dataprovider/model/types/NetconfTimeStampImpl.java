/*******************************************************************************
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 2019/06/17 Redesign to ZonedDateTime because of sync problems.
 *
 * Function is handling the NETCONF and the format used by database and restconf communication.
 *
 * Input supported for the formats used in NETCONF messages:
 *
 * Format1 ISO 8601 2017-01-18T11:44:49.482-05:00
 *
 * Format2 NETCONF - pattern from ietf-yang-types "2013-07-15" Pattern:
 * "\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d+)?(Z|[\+\-](\d{2}):(\d{2}))"
 *
 * Format3 NETCONF DateAndTime CoreModel-CoreFoundationModule-TypeDefinitions vom 2016-07-01 Example1: 20170118114449.1Z
 * Example2: 20170118114449.1-0500 Pattern: "\d{4}\d{2}\d{2}\d{2}\d{2}\d{2}.\d+?(Z|[\+\-](\d{2})(\d{2}))" typedef
 * DateAndTime { description "This primitive type defines the date and time according to the following structure:
 * 'yyyyMMddhhmmss.s[Z|{+|-}HHMm]' where: yyyy '0000'..'9999' year MM '01'..'12' month dd '01'..'31' day hh '00'..'23'
 * hour mm '00'..'59' minute ss '00'..'59' second s '.0'..'.9' tenth of second (set to '.0' if EMS or NE cannot support
 * this granularity) Z 'Z' indicates UTC (rather than local time) {+|-} '+' or '-' delta from UTC HH '00'..'23' time
 * zone difference in hours Mm '00'..'59' time zone difference in minutes."; type string; } Format4 E/// specific
 * Example1: 2017-01-23T13:32:38-05:00 Example2: 2017-01-23T13:32-05:00
 *
 * Input formats netconfTime as String according the formats given above
 *
 * Return format is String in ISO8601 Format for database and presentation.
 *
 * Example formats: 1) ISO8601. Example 2017-01-18T11:44:49.482-05:00 2) Microwave ONF. Examples 20170118114449.1Z,
 * 20170118114449.1-0500 3.1) Ericson. Example: 2017-01-23T13:32:38-05:00 3.2) Ericson. Example: 2017-01-23T13:32-05:00
 * Always 10 Groups, 1:Year 2:Month 3:day 4:Hour 5:minute 6:optional sec 7:optional ms 8:optional Z or 9:offset
 * signedhour 10:min
 *
 * Template: private static final NetconfTimeStamp NETCONFTIME_CONVERTER = NetconfTimeStamp.getConverter();
 */

public class NetconfTimeStampImpl implements NetconfTimeStamp {
    private static final Logger LOG = LoggerFactory.getLogger(NetconfTimeStamp.class);

    private static final NetconfTimeStamp CONVERTER = new NetconfTimeStampImpl();

    /**
     * Specify the input format expected from netconf, and from specific devices.
     */
    private static DateTimeFormatter formatterInput =
            DateTimeFormatter.ofPattern("" + "[yyyy-MM-dd'T'HH:mm[:ss][.SSS][.SS][.S][xxx][xx][X][Z]]"
                    + "[yyyyMMddHHmmss[.SSS][.SS][.S][xxx][xx][X][Z]]").withZone(ZoneOffset.UTC);

    /**
     * Specify output format that is used internally
     */
    private static DateTimeFormatter formatterOutput =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.S'Z'").withZone(ZoneOffset.UTC);

    /**
     * Use static access
     */
    private NetconfTimeStampImpl() {}

    /*
     * ------------------------------------ Public function
     */

    /**
     * Use this function to get the converter
     * 
     * @return global converter
     */
    public static NetconfTimeStamp getConverter() {
        return CONVERTER;
    }

    /**
     * Get actual timestamp as NETCONF specific type NETCONF/YANG 1.0 Format
     *
     * @return String with Date in NETCONF/YANG Format Version 1.0.
     */
    @Override
    public String getTimeStampAsNetconfString() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(formatterOutput);
    }

    /**
     * Get actual timestamp as NETCONF specific type NETCONF/YANG 1.0 Format
     * 
     * @return String with Date in NETCONF/YANG Format Version 1.0.
     */
    @Override
    public String getTimeStampAsNetconfString(Date date) {
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC).format(formatterOutput);
    }



    /**
     * Get actual timestamp as NETCONF specific type NETCONF/YANG 1.0 Format in GMT
     *
     * @return DateAndTime Type 1.0. Date in NETCONF/YANG Format Version 1.0.
     */
    @Override
    public DateAndTime getTimeStamp() {
        return DateAndTime.getDefaultInstance(getTimeStampAsNetconfString());
    }

    /**
     * Get time from date as NETCONF specific type NETCONF/YANG 1.0 Format in GMT
     * 
     * @param date specifying the date and time
     * @return DateAndTime Type 1.0. Date in NETCONF/YANG Format Version 1.0.
     */
    @Override
    public DateAndTime getTimeStamp(Date date) {
        return DateAndTime.getDefaultInstance(getTimeStampAsNetconfString(date));
    }

    /**
     * Get time from date as NETCONF specific type NETCONF/YANG 1.0 Format in GMT
     * 
     * @param date specifying the date and time
     * @return DateAndTime Type 1.0. Date in NETCONF/YANG Format Version 1.0.
     */
    @Override
    public DateAndTime getTimeStamp(String date) {
        return DateAndTime.getDefaultInstance(date);
    }

    /**
     * Return the String with a NETCONF time converted to long
     *
     * @param netconfTime as String according the formats given above
     * @return Epoch milliseconds
     * @throws IllegalArgumentException In case of no compliant time format definition for the string
     */
    @Override
    public long getTimeStampFromNetconfAsMilliseconds(String netconfTime) throws IllegalArgumentException {
        try {
            long utcMillis = doParse(netconfTime).toInstant().toEpochMilli();
            return utcMillis;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "No pattern for NETCONF data string: " + netconfTime + " Msg:" + e.getMessage());
        }
    }

    /**
     * Deliver String result.
     *
     * @param netconfTime as String according the formats given above
     * @return If successful: String in ISO8601 Format for database and presentation. If "wrong formed input" the Input
     *         string with the prefix "Maleformed date" is delivered back.
     */
    @Override
    public String getTimeStampFromNetconf(String netconfTime) {
        try {
            String inputUTC = doParse(netconfTime).format(formatterOutput);
            return inputUTC;
        } catch (Exception e) {
            LOG.info(e.getMessage());
        }
        LOG.debug("No pattern for NETCONF data string: {}", netconfTime);
        return "Malformed date: " + netconfTime; // Error handling
    }

    /*----------------------------------------------------
     * Private functions
     */

    private OffsetDateTime doParse(String netconfTime) {
        return OffsetDateTime.parse(netconfTime, formatterInput);
    }

    @Override
    public Date getDateFromNetconf(String netconfTime) {
        return Date.from(LocalDateTime.parse(netconfTime, formatterInput).atZone(ZoneOffset.UTC).toInstant());
    }

    @Override
    public String getTimeStampAsNetconfString(LocalDateTime dt) {
        return formatterOutput.format(dt);
    }

}
