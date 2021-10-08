/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=======================================================
 *
 */

package org.onap.ccsdk.features.lib.npm.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.features.lib.npm.models.NpmTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * The type Npm utils.
 *
 * @author Kapil Singal
 */
public class NpmUtils {

    private static final Logger logger = LoggerFactory.getLogger(NpmUtils.class);

    private NpmUtils() {
    }

    /**
     * This is a getJson method
     *
     * @param instance the instance
     *
     * @return String json
     */
    public static String getJson(Object instance) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
        } catch (JsonProcessingException e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets list from json string.
     *
     * @param <T>       the type parameter
     * @param content   the content
     * @param valueType the value type
     *
     * @return the list of type parameter from json string
     */
    public static <T> List<T> getListFromJsonString(String content, Class<T> valueType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (mapper.readTree(content) instanceof ArrayNode) {
                return mapper.readValue(content, mapper.getTypeFactory().constructCollectionType(List.class, valueType));
            } else {
                return Collections.singletonList(mapper.readValue(content, valueType));
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Form npm transaction npm transaction.
     *
     * @param npmTransactionId      the npmTransactionId (instance of UUID :: defaults to Random UUID if null)
     * @param sbEndpoint            the sbEndpoint (Host IP Address :: used to create priority queues)
     * @param sbType                the sbType (eg. EMS_ERICSSON, EMS_NOKIA, D2_MSN etc :: used to determine parallel connections it can support )
     * @param priority              the priority (n :: lowest index is the highest priority, defaults to (least priority) if -1
     * @param connectionCount       the connectionCount (n :: total number of connection a transaction would occupy while processing defaults to (1) if -1
     * @param timestamp             the timestamp (instance of Instant :: defaults to Current Time if null)
     * @param timeToLive            the timeToLive (instance of Instant :: defaults to MAX Time if null)
     * @param serviceKey            the serviceKey (the unique serviceKey specific to service reference)
     * @param serviceRequest        the serviceRequest (actual request payload received from upstream)
     *
     * @return the npm transaction
     */
    public static NpmTransaction formNpmTransaction(UUID npmTransactionId, String sbEndpoint, String sbType, int priority, int connectionCount,
        Instant timestamp, Instant timeToLive, String serviceKey, Object serviceRequest) {

        NpmTransaction npmTransaction = new NpmTransaction();
        npmTransaction.setNpmTransactionId(npmTransactionId == null ? UUID.randomUUID() : npmTransactionId);
        npmTransaction.setSbEndpoint(sbEndpoint);
        npmTransaction.setSbType(sbType);
        if (priority > -1) {
            npmTransaction.setPriority(priority);
        }
        if (connectionCount > 0) {
            npmTransaction.setConnectionCount(connectionCount);
        }
        npmTransaction.setTimestamp(timestamp == null ? Instant.now() : timestamp);
        npmTransaction.setTimeToLive(timeToLive == null ? Instant.MAX : timeToLive);
        npmTransaction.setServiceKey(serviceKey);
        npmTransaction.setServiceRequest(serviceRequest);

        return npmTransaction;
    }

    /**
     * This is a isAllOkToProcess method
     *
     * @param qspLimit          the queue serve period limit
     * @param qspCounter        the queue serve period counter
     * @param connectionCounter the connection counter for sb_endpoint
     * @param sbConnectionLimit the sb_endpoint parallel connection limit
     *
     * @return  true if: queue serve period is not met and sbEndpoint is still having connection slot empty
     *          <p>
     *          false if: queue serve period is reached to max limit or particular SB (sbEndpoint) is already occupied to max connection limit
     */
    public static boolean isAllOkToProcess(int qspLimit, int qspCounter, int connectionCounter, int sbConnectionLimit) {
        return qspLimit > qspCounter && connectionCounter < sbConnectionLimit;
    }

    /**
     * Is expired boolean.
     *
     * @param npmTransaction the NpmTransaction instance
     *
     * @return true if timeToLive is passed than current UTC Time else false
     */
    public static boolean isExpired(NpmTransaction npmTransaction) {
        return npmTransaction != null && npmTransaction.getTimeToLive().compareTo(Instant.now()) <= 0;
    }

    /**
     * Get priority list.
     *
     * @param priorities defined Property_List from properties file
     *
     * @return the int [priorities]
     */
    public static int[] getPriorityList(String priorities) {
        return Stream.of(StringUtils.defaultIfBlank(priorities, "0,1,2").split(",")).mapToInt(Integer::parseInt).toArray();
    }

    /**
     * Gets npm priority queue key.
     *
     * @param sbEndpoint the sb endpoint
     * @param sbType     the sb type
     *
     * @return the npm priority queue key
     */
    public static String getNpmPriorityQueueKey(String sbEndpoint, String sbType) {
        return sbEndpoint.concat("##").concat(sbType);
    }

}
