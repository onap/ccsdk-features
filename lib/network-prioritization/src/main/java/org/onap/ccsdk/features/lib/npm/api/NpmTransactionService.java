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

package org.onap.ccsdk.features.lib.npm.api;

import java.util.Map;
import java.util.NavigableSet;
import org.onap.ccsdk.features.lib.npm.models.NpmAck;
import org.onap.ccsdk.features.lib.npm.models.NpmTransaction;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The interface Npm Transaction Service provider.
 *
 * @author Kapil Singal
 */
public interface NpmTransactionService {

    /**
     * Form npm transaction npm transaction with all mandatory/non-null parameters.
     *
     * @param sbEndpoint     the sb endpoint (Host IP Address)
     * @param sbType         the sb type (eg. EMS_ERICSSON, EMS_NOKIA, D2_MSN etc)
     * @param serviceKey     the service key (the unique serviceKey specific to service reference)
     * @param serviceRequest the service request (actual request payload received from upstream)
     *
     * @return the npm transaction
     */
    NpmTransaction formNpmTransaction(String sbEndpoint, String sbType, String serviceKey, Object serviceRequest);

    /**
     * Form npm transaction npm transaction with all NPM header Information (nullable and non-nullable)
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
    NpmTransaction formNpmTransaction(UUID npmTransactionId, String sbEndpoint, String sbType, int priority, int connectionCount,
        Instant timestamp, Instant timeToLive, String serviceKey, Object serviceRequest);

    /**
     * Add Transactions to queue : called by all services to get Transactions added to respective priority queue
     *
     * @param npmTransactionList the NpmTransaction list
     *
     * @return the list of NpmAck with status and message
     */
    List<NpmAck> addTransactionsToQueue(List<NpmTransaction> npmTransactionList);

    /**
     * Add Transaction to queue : called by all services to get Transaction added to respective priority queue
     *
     * @param npmTransaction the NpmTransaction
     *
     * @return the NpmAck with status and message
     */
    NpmAck addTransactionsToQueue(NpmTransaction npmTransaction);

    /**
     * Remove Transactions from queue : called by all services to get Transactions removed from priority queue
     *
     * @param npmTransactionList the NpmTransaction list
     *
     * @return the list of NpmAck with status and message
     */
    List<NpmAck> removeTransactionsFromQueue(List<NpmTransaction> npmTransactionList);

    /**
     * Remove Transaction from queue : called by all services to get Transaction removed from priority queue
     *
     * @param npmTransaction the NpmTransaction
     *
     * @return the NpmAck with status and message
     */
    NpmAck removeTransactionsFromQueue(NpmTransaction npmTransaction);

    /**
     * Retrieve transaction from queue list.
     *
     * @param sbEndpoint the sb endpoint
     * @param sbType     the sb type
     *
     * @return the list
     */
    List<NpmTransaction> retrieveTransactionFromQueue(String sbEndpoint, String sbType);

    /**
     * Retrieve NPM priority queues map.
     *
     * @return the map
     * <pre>
     * npmPriorityQueues        : Map [String:sbEndpoint##sbType, Map:sbPriorityQueues]
     *     sbPriorityQueues     : Map [int:priority, TreeSet:priorityQueue]
     *         priorityQueue    : NavigableSet [Contains the NpmTransaction Object]
     * </pre>
     */
    Map<String, Map<Integer, NavigableSet<NpmTransaction>>> retrieveNpmPriorityQueues();

    /**
     * Register service : must be called by all services to register with Npm
     *
     * @param serviceKey            the unique serviceKey specific to service reference
     * @param npmServiceCallbackApi the instance of service class implementing NpmServiceCallbackApi
     */
    void registerService(String serviceKey, NpmServiceCallbackApi npmServiceCallbackApi);

    /**
     * Load npm config boolean.
     * <pre>
     * Default properties:
     *      <b>Priority_List=0,1,2</b>     Total possible priorities (lowest index is the highest priority)
     *      <b>Default_Priority=2</b>      Default Priority value to be set if missing in request
     *      <b>EMS_ERICSSON=2</b>          Maximum number of parallel connection that ERICSSON manufactured EMS can support
     *      <b>EMS_NOKIA=2</b>             Maximum number of parallel connection that NOKIA manufactured EMS can support
     *      <b>queue_capacity_0=10</b>     Total capacity of queue (maximum number of transactions a queue can hold) with priority 0
     *      <b>queue_capacity_1=7</b>      Total capacity of queue (maximum number of transactions a queue can hold) with priority 1
     *      <b>queue_capacity_2=5</b>      Total capacity of queue (maximum number of transactions a queue can hold) with priority 2
     *      <b>qsp_limit_0=5</b>           Maximum number of transactions that can be processed from a queue before jumping to lower priority queues
     *      <b>qsp_limit_1=3</b>           Maximum number of transactions that can be processed from a queue before jumping to lower priority queues
     *      <b>qsp_limit_2=2</b>           Maximum number of transactions that can be processed from a queue before jumping to lower priority queues
     * </pre>
     *
     * @param configFilePath the Config File Name
     *
     * @return the boolean
     */
    boolean loadNpmConfig(String configFilePath);

}
