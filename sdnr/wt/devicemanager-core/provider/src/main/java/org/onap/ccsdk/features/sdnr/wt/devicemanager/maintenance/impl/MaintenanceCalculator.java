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

package org.onap.ccsdk.features.sdnr.wt.devicemanager.maintenance.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.annotation.Nonnull;
import org.onap.ccsdk.features.sdnr.wt.common.HtAssert;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaintenanceCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(MaintenanceCalculator.class);

    private static ZoneId EsMaintenanceFilterTimeZone = ZoneId.of("UTC");
    //private static DateTimeFormatter FORMAT = DateTimeFormatter.ISO_DATE_TIME;       // "1986-04-08T12:30:00"
    private static DateTimeFormatter FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME; // 2011-12-03T10:15:30+01:00
    private static ZonedDateTime EMPTYDATETIME = ZonedDateTime.ofInstant(Instant.EPOCH, EsMaintenanceFilterTimeZone);

    /** Intended to be used static **/
    private MaintenanceCalculator() {}

    /**
     * Verify maintenance status
     * 
     * @param maintenance if null false, else according to settings
     * @param objectIdRef NETCONF object id
     * @param problem name that was provided
     * @param now time to verify with
     * @return true if in maintenance status
     */
    public static boolean isONFObjectInMaintenance(MaintenanceEntity maintenance, String objectIdRef, String problem,
            ZonedDateTime now) {

        if (maintenance != null) {
            Boolean isActive = maintenance.getActive();
            if (isActive != null && isActive && isInMaintenance(maintenance, objectIdRef, problem, now)) {
                return true;
            }

        }
        return false;
    }

    /** Shortcut **/
    public static boolean isONFObjectInMaintenance(MaintenanceEntity maintenance, String objectIdRef, String problem) {
        return isONFObjectInMaintenance(maintenance, objectIdRef, problem, getNow());
    }

    /*---------------------------------------------
     * private static helper functions to verify
     */

    /**
     * Get the actual time in the Filter time zone.
     * 
     * @return actual Time
     */
    private static ZonedDateTime getNow() {
        return ZonedDateTime.now(EsMaintenanceFilterTimeZone);
    }

    /**
     * Verify if the filter is active for an object
     *
     * @param now point of time to verify
     * @return if the object is covered by filter and now within point of time
     */
    private static boolean isInMaintenance(MaintenanceEntity maintenance, String objectIdRef, String problem,
            ZonedDateTime now) {
        return appliesToObjectReference(maintenance, objectIdRef, problem)
                && isInPeriod(maintenance.getStart(), maintenance.getEnd(), now);
    }

    /**
     * Compare the if probe is within the range of start and end.
     *
     * @param start of range
     * @param end of range
     * @param probe time to verify
     * @return boolean result true if (start <= probe <= end)
     */
    public static boolean isInPeriod(DateAndTime start, DateAndTime end, ZonedDateTime probe) {
        HtAssert.nonnull(start, end, probe);
        ZonedDateTime startZT = valueOf(start.getValue());
        ZonedDateTime endZT = valueOf(end.getValue());
        return startZT.compareTo(endZT) < 0 && startZT.compareTo(probe) <= 0 && endZT.compareTo(probe) >= 0;
    }

    /**
     * Verify if the definied object is matching to the referenced object
     * 
     * @param definition definition with parameters
     * @param pObjectIdRef object-id-ref of fault notification
     * @param pProblem problem of fault notification
     * @return true if if referenced
     */
    private static boolean appliesToObjectReference(@Nonnull MaintenanceEntity definition, @Nonnull String pObjectIdRef,
            @Nonnull String pProblem) {
        HtAssert.nonnull(definition, pObjectIdRef, pProblem);
        boolean res = (definition.getObjectIdRef() == null || pObjectIdRef.contains(definition.getObjectIdRef()))
                && (definition.getProblem() == null || pProblem.contains(definition.getProblem()));
        LOG.debug("Check result applies {}: {} {} against: {}", res, pObjectIdRef, pProblem, definition);
        return res;
    }

    /**
     * Convert String to time value
     * 
     * @param zoneTimeString with time
     * @return ZonedDateTime string
     */
    public static ZonedDateTime valueOf(String zoneTimeString) {
        if (zoneTimeString == null || zoneTimeString.isEmpty()) {
            LOG.warn("Null or empty zoneTimeString");
            return EMPTYDATETIME;
        }
        try {
            return ZonedDateTime.parse(zoneTimeString, FORMAT);
        } catch (DateTimeParseException e) {
            LOG.warn("Can not parse zoneTimeString '{}'", zoneTimeString);
            return EMPTYDATETIME;
        }
    }

}
