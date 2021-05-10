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

import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import org.onap.ccsdk.features.lib.npm.NpmException;
import org.onap.ccsdk.features.lib.npm.models.NpmTransaction;

/**
 * The interface Npm Service Manager being used internally by Npm
 * <p>
 * Placeholder for Priority Queues holding Npm Transactions:
 * Create: During Npm Startup if un-processed Transaction are available in NPM_TRANSACTION Table
 * Manage: If a tx is expired sitting in queue, invoke service callback api
 * Remove: If gets notified by a service once done with processing Transaction
 *
 * @author Kapil Singal
 */
public interface NpmServiceManager {

    /**
     * Add Transaction to queue.
     *
     * @param npmTransaction the NpmTransaction instance which contain serviceRequest with header information
     *
     * @throws NpmException the NpmException if Npm Transaction:
     *                      if missing required header information
     *                      couldn't be queued if priority queue is already full to it's max capacity
     */
    void addTransactionToQueue(NpmTransaction npmTransaction) throws NpmException;

    /**
     * Remove Transaction from queue and update connection counter
     *
     * @param npmTransaction          the NpmTransaction instance which contain serviceRequest with header information
     * @param updateConnectionCounter the update connection counter only if it's true
     */
    void removeTransactionFromQueue(NpmTransaction npmTransaction, boolean updateConnectionCounter);

    /**
     * Retrieve transaction from queue list.
     *
     * @param sbEndpoint the sb endpoint
     * @param sbType     the sb type
     *
     * @return the list of NpmTransaction
     */
    List<NpmTransaction> retrieveTransactionFromQueue(String sbEndpoint, String sbType);

    /**
     * Retrieve all priority queues map.
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
     * @throws NpmException the npm exception
     */
    void loadNpmConfig(String configFilePath) throws NpmException;

    /**
     * Gets npm config.
     *
     * @param referenceKey the reference key
     *
     * @return the npmConfig Value
     */
    String getNpmConfig(String referenceKey);

    /**
     * Register service : must be called to register with Npm
     *
     * @param serviceKey            the unique serviceKey specific to service reference
     * @param npmServiceCallbackApi the instance of service class implementing NpmServiceCallbackApi
     */
    void registerService(String serviceKey, NpmServiceCallbackApi npmServiceCallbackApi);

}
