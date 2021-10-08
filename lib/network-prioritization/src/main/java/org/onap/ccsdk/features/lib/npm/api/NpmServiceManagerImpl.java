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

import java.util.ArrayList;
import java.util.List;
import org.onap.ccsdk.features.lib.npm.NpmConstants;
import org.onap.ccsdk.features.lib.npm.NpmException;
import org.onap.ccsdk.features.lib.npm.models.NpmStatusEnum;
import org.onap.ccsdk.features.lib.npm.models.NpmTransaction;
import org.onap.ccsdk.features.lib.npm.utils.NpmUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.MDC;

import static org.onap.ccsdk.features.lib.npm.NpmConstants.MDC_REQUEST_ID;

/**
 * The type Npm Service Manager.
 *
 * @author Kapil Singal
 */
public class NpmServiceManagerImpl implements NpmServiceManager {
    private static final Logger logger = LoggerFactory.getLogger(NpmServiceManagerImpl.class);
    /**
     * Npm `Priority Queues`.
     * <p>
     * npmPriorityQueues : Map <String:sbEndpoint##sbType, Map:sbPriorityQueues>
     * sbPriorityQueues  : Map <int:priority, TreeSet:priorityQueue>
     * priorityQueue     : NavigableSet [Contains the NpmTransaction Object]
     * </p>
     *
     * <p>
     * Npm will maintain multiple Priority queues per sb_endpoint (an EMS is an example of sb_endpoint)
     * Priority Queues will be maintained per priority, having Npm Transactions, sorted based on timestamp, to accomplish a FIFO queue.
     * </p>
     *
     * <p>
     * Why NavigableSet or ConcurrentSkipListSet is being used:
     * Need to maintain priority queue Sorted based om Transaction timestamp.!!
     * Hence using ConcurrentSkipListSet -> which implements NavigableSet -> which extends SortedSet
     * </p>
     *
     * <p>
     * The ConcurrentSkipListSet class allows safe execution of
     * Insertion, removal, and access operations on set concurrently by multiple threads.
     * </p>
     *
     * <p>
     * It should be preferred over other implementations of the Set interface
     * when concurrent modification of set by multiple threads is required.
     * </p>
     */
    private final Map<String, Map<Integer, NavigableSet<NpmTransaction>>> npmPriorityQueues = new ConcurrentHashMap<>();
    private final Map<String, Integer> connectionCounter = new ConcurrentHashMap<>();
    private final Map<String, NpmServiceCallbackApi> serviceRegistry = new ConcurrentHashMap<>();
    private final Map<String, Integer> priorityExecState = new ConcurrentHashMap<>();
    private final Map<String, Integer> qspExecState = new ConcurrentHashMap<>();

    private final Properties npmConfigurations = new Properties();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private boolean isProcessIngNpmPriorityQueues = false;

    public NpmServiceManagerImpl() throws NpmException {
        loadProperties();
        Runnable processNpmPriorityQueuesTask = () -> {
            try {
                if (!isProcessIngNpmPriorityQueues) {
                    isProcessIngNpmPriorityQueues = true;
                    // Cleaning up MDC to make sure logging doesn't have old requestID being used for further processing
                    MDC.clear();
                    processNpmPriorityQueues();
                    isProcessIngNpmPriorityQueues = false;
                }
            } catch (StackOverflowError | Exception e) {
                //Setting isProcessIngNpmPriorityQueues to false because next time when periodic task runs it should re-run processNpmPriorityQueues
                isProcessIngNpmPriorityQueues = false;
                // Catching both as there may not be any npm transaction at time of boot or eventual
                logger.warn("----------- Task to processNpmPriorityQueues failed ----------- \nErrorMessage:({})", e.getMessage(), e);
            }
        };
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(processNpmPriorityQueuesTask, 30, 5, TimeUnit.SECONDS);
    }

    @Override
    public String getNpmConfig(String referenceKey) {
        return npmConfigurations.getProperty(referenceKey);
    }

    @Override
    public void registerService(String serviceKey, NpmServiceCallbackApi npmServiceCallbackApi) {
        logger.trace("------------- Registering NpmServiceCallbackApi with serviceKey:({}) -------------", serviceKey);
        serviceRegistry.put(serviceKey, npmServiceCallbackApi);
    }

    @Override
    public void addTransactionToQueue(final NpmTransaction npmTransaction) throws NpmException {
        logger.trace("------------- Inside NPM SM addTransactionToQueue -------------");
        logger.trace("Queuing Npm Transaction with npmTransactionId:({})", npmTransaction.getNpmTransactionId());

        //Sorted Queue based on timestamp, if timestamp same for multiple Transaction it sorts those with NpmTransactionId.
        //Using computeIfAbsent to make sure it creates priority_queue for particular sb_endpoint if not already present
        final String npmPriorityQueueKey = NpmUtils.getNpmPriorityQueueKey(npmTransaction.getSbEndpoint(), npmTransaction.getSbType());

        logger.trace("Locating npmPriorityQueues with key sbEndpoint##sbType :: ({})", npmPriorityQueueKey);
        NavigableSet<NpmTransaction> priorityQueue = npmPriorityQueues.computeIfAbsent(npmPriorityQueueKey,
            sbPriorityQueues -> new TreeMap<>()).computeIfAbsent(npmTransaction.getPriority(),
                priorityQueueSet -> new ConcurrentSkipListSet<>
                        (Comparator.comparing(NpmTransaction :: getTimestamp).thenComparing(NpmTransaction :: getNpmTransactionId)));
        logger.trace("Current queue length for sbEndpoint:({}) with priority:({}) is:({})",
                npmTransaction.getSbEndpoint(), npmTransaction.getPriority(), priorityQueue.size());

        if (priorityQueue.contains(npmTransaction)) {
            logger.trace("Npm Transaction with npmTransactionId:({}) is already present in queued, returning without altering the queue...",
                    npmTransaction.getNpmTransactionId());
            return;
        }

        // Compare if queue_capacity_$priority available from Configurations, else by default it will be comparing against default value = 10
        final int queueCapacity = NumberUtils.toInt(getNpmConfig("queue_capacity_" + npmTransaction.getPriority()), 10);
        if (priorityQueue.size() >= queueCapacity) {
            npmTransaction.setStatus(NpmStatusEnum.OUT_OF_CAPACITY);
            String message = String.format("Queue %s Error. Npm Queue for sb_endpoint:(%s) with Priority:(%s) is maxed out to it's capacity limit:(%s)",
                    NpmStatusEnum.OUT_OF_CAPACITY, npmTransaction.getSbEndpoint(), npmTransaction.getPriority(), queueCapacity);
            logger.trace("Returning Error message:({})", message);
            throw new NpmException(message);
        }
        npmTransaction.setStatus(NpmStatusEnum.QUEUED);
        priorityQueue.add(npmTransaction);
        logger.trace("Successfully queued Npm Transaction with npmTransactionId:({})", npmTransaction.getNpmTransactionId());
        logger.trace("Updated queue length for sbEndpoint:({}) with priority:({}) is:({})",
                npmTransaction.getSbEndpoint(), npmTransaction.getPriority(), priorityQueue.size());
    }

    @Override
    public List<NpmTransaction> retrieveTransactionFromQueue(String sbEndpoint, String sbType) {
        logger.trace("------------- Inside NPM SM retrieveTransactionFromQueue -------------");
        logger.trace("Retrieving all Npm Transactions for sbEndpoint:sbType ({}:{}) from priorityQueues", sbEndpoint, sbType);

        final String npmPriorityQueueKey = NpmUtils.getNpmPriorityQueueKey(sbEndpoint, sbType);
        final List<NpmTransaction> npmTransactionList = new ArrayList<>();

        //Using computeIfPresent as npmTransactionQueueMap doesn't need any alteration if Npm Transaction is not found.
        npmPriorityQueues.computeIfPresent(npmPriorityQueueKey, (sb, sbPriorityQueues) -> {
            sbPriorityQueues.forEach((priority, npmTransactionNavigableSet) -> {
                npmTransactionList.addAll(npmTransactionNavigableSet);
            });
            return sbPriorityQueues;
        });

        logger.trace("Retrieved total {} Npm Transactions from priorityQueues", npmTransactionList.size());
        return npmTransactionList;
    }

    @Override
    public Map<String, Map<Integer, NavigableSet<NpmTransaction>>> retrieveNpmPriorityQueues() {
        // TODO: Check if it should return the actual queue map instance or a clone !!
        return npmPriorityQueues;
    }

    @Override
    public void removeTransactionFromQueue(NpmTransaction npmTransaction, boolean updateConnectionCounter) {
        logger.trace("------------- Inside NPM SM removeTransactionFromQueue -------------");
        logger.trace("Removing Npm Transaction from priority queue with npmTransactionId:({})", npmTransaction.getNpmTransactionId());
        if (updateConnectionCounter) {
            // Updating connection counter so that next transaction can be processed from queue for same sbEndpoint
            updateConnectionCounter(npmTransaction.getSbEndpoint(), Math.negateExact(npmTransaction.getConnectionCount()));
        }
        final String npmPriorityQueueKey = NpmUtils.getNpmPriorityQueueKey(npmTransaction.getSbEndpoint(), npmTransaction.getSbType());

        //Using computeIfPresent as npmTransactionQueueMap doesn't need any alteration if Npm Transaction is not found.
        npmPriorityQueues.computeIfPresent(npmPriorityQueueKey, (sb, sbPriorityQueues) -> {
            NavigableSet<NpmTransaction> priorityQueue = sbPriorityQueues.get(npmTransaction.getPriority());
            if (priorityQueue != null) {
                logger.trace("Current queue length for sbEndpoint:({}) with priority:({}) is:({})",
                        npmTransaction.getSbEndpoint(), npmTransaction.getPriority(), priorityQueue.size());

                priorityQueue.remove(npmTransaction);
                logger.trace("Successfully removed Npm Transaction with npmTransactionId:({})", npmTransaction.getNpmTransactionId());
                logger.trace("Updated queue length for sbEndpoint:({}) with priority:({}) is:({})",
                        npmTransaction.getSbEndpoint(), npmTransaction.getPriority(), priorityQueue.size());

                // Cleaning up Priority Queue if empty
                if (priorityQueue.isEmpty()) {
                    logger.trace("As priorityQueue for sbEndpoint:({}) with priority:({}) is empty, removing the priority queue.",
                            npmTransaction.getSbEndpoint(), npmTransaction.getPriority());
                    sbPriorityQueues.remove(npmTransaction.getPriority());
                }
            }
            return sbPriorityQueues;
        });
    }

    private void processExpiredNpmTransaction() {
        logger.trace("------------- Inside NPM SM processExpiredNpmTransaction -------------");
        if (npmPriorityQueues.isEmpty()) {
            logger.trace("------------- No Priority Queue is present, nothing to cleanup, hence returning -------------");
            // Returning here itself to avoid StackOverFlow or other runtime error as there may not be any npm transaction to process
            return;
        }
        // Converting to entrySet so that it can parallel stream :)
        npmPriorityQueues.entrySet().parallelStream().forEach(sbEndpointMapEntry -> {
            sbEndpointMapEntry.getValue().entrySet().parallelStream().forEach(prioritySetEntry -> {

                // TODO: Need to test, Periodic Printing of Current Queue Length is expected by Rajesh and Team
                logger.trace("Current queue length with key sbEndpoint##sbType :: ({}) with priority:({}) is:({})",
                        sbEndpointMapEntry.getKey(), prioritySetEntry.getKey(), prioritySetEntry.getValue().size());

                prioritySetEntry.getValue().parallelStream().forEach(npmTransaction -> {
                    // Checking if npmTransaction is already expired
                    if (NpmUtils.isExpired(npmTransaction)) {
                        logger.trace("Npm Transaction with npmTransactionId:({}) is Expired and will be removed from priority queue, as timeToLive has passed.",
                                npmTransaction.getNpmTransactionId());
                        npmTransaction.setStatus(NpmStatusEnum.EXPIRED);
                        npmTransaction.setMessage("Npm Transaction is Expired and will be removed from priority queue, as timeToLive has passed.");
                        Runnable notifyServiceTask = () -> invokeServiceCallbackApi(npmTransaction, false);
                        executorService.execute(notifyServiceTask);
                        removeTransactionFromQueue(npmTransaction, false);
                    }
                });
            });
        });
        logger.trace("------------- Done with checking all priority queues for any expired Npm Transaction -------------");
    }

    private void processNpmPriorityQueues() {
        logger.trace("------------- Inside NPM SM processNpmPriorityQueues -------------");
        if (npmPriorityQueues.isEmpty()) {
            // Returning here itself to avoid StackOverFlow or other runtime error as there may not be any npm transaction to process
            return;
        }
        logger.trace("Calling processExpiredNpmTransaction to cleanup expired Npm Transactions before processing any queue.");
        processExpiredNpmTransaction();

        // Converting to entrySet so that it can parallel stream :)
        npmPriorityQueues.entrySet().parallelStream().forEach(sbQueueEntry -> {
            final String sbEndpoint = sbQueueEntry.getKey().split("##")[0];
            final String sbType = sbQueueEntry.getKey().split("##")[1];
            final int sbConnectionLimit = NumberUtils.toInt(getNpmConfig(sbType), 1);

            if (sbConnectionLimit <= connectionCounter.getOrDefault(sbEndpoint, 0)) {
                logger.trace("Not processing any Npm Transaction for sbEndpoint:({}) as it is already occupied to it's maximum connection limit:({}).",
                        sbEndpoint, sbConnectionLimit);
                //returning when a particular SB (sbEndpoint) is already occupied to it's max limit
                return;
            }
            logger.trace("Trying to process priority queue for sbEndpoint({}) with connectionLimit:({})", sbEndpoint, sbConnectionLimit);
            processSbPriorityQueues(sbEndpoint, sbType, sbConnectionLimit, sbQueueEntry.getValue());
        });
    }

    private void processSbPriorityQueues(String sbEndpoint, String sbType, int sbConnectionLimit, final Map<Integer, NavigableSet<NpmTransaction>> priorityQueues) {
        logger.trace("------------- Inside NPM SM processSbPriorityQueues -------------");
        if (priorityQueues == null || priorityQueues.isEmpty()) {
            // Returning here itself to avoid StackOverFlow or other runtime error as there may not be any npm transaction to process
            return;
        }

        Iterator<Map.Entry<Integer,NavigableSet<NpmTransaction>>> priorityQueuesIterator = priorityQueues.entrySet().iterator();
        while (priorityQueuesIterator.hasNext()) {
            Map.Entry<Integer,NavigableSet<NpmTransaction>> entry = priorityQueuesIterator.next();
            final NavigableSet<NpmTransaction> npmTransactions = entry.getValue();
            if (npmTransactions.isEmpty()) {
                priorityQueuesIterator.remove();
                continue;
            }

            final Integer priorityIndex = entry.getKey();
            final String qspLimitKey = "qsp_limit_" + priorityIndex;
            final String qspStateKey = sbEndpoint + "_" + priorityIndex;

            AtomicInteger qspLimit = new AtomicInteger(NumberUtils.toInt(getNpmConfig(qspLimitKey), 5));
            AtomicInteger qspCounter = new AtomicInteger(qspExecState.getOrDefault(qspStateKey, 0));
            logger.trace("For sbEndpoint:({}) with priority:({}) qspLimit is:({}) and current qspCounter is:({})",
                sbEndpoint, priorityIndex, qspLimit.get(), qspCounter.get());

            // On re-iteration it should be processing same priority queue which was processed last only if qsp hasn't met
            if (NpmUtils.isAllOkToProcess(qspLimit.get(), qspCounter.get(), connectionCounter.getOrDefault(sbEndpoint, 0), sbConnectionLimit)
                && priorityExecState.containsKey(sbEndpoint) && priorityQueues.containsKey(priorityExecState.get(sbEndpoint))
                && !priorityIndex.equals(priorityExecState.get(sbEndpoint))) {
                logger.trace("Last execution state for sbEndpoint:({}) was for priority:({})", sbEndpoint, priorityExecState.get(sbEndpoint));
                return;
            }

            logger.trace("------------- Iterating npmTransactions from priorityQueue -------------");
            for (final NpmTransaction npmTransaction : npmTransactions) {
                // Setting RequestID in MDC same as NPM Transaction RequestId
                MDC.put(MDC_REQUEST_ID, npmTransaction.getRequestId());
                // Should pick npmTransactions which are in QUEUED state and are not Expired
                if (NpmStatusEnum.QUEUED.equals(npmTransaction.getStatus()) && !NpmUtils.isExpired(npmTransaction)
                    && NpmUtils.isAllOkToProcess(qspLimit.get(), qspCounter.get(), connectionCounter.getOrDefault(sbEndpoint, 0), sbConnectionLimit)
                    && invokeServiceCallbackApi(npmTransaction, true)) {

                    logger.trace("------------- Updating priorityExecState and qspExecState -------------");
                    priorityExecState.put(sbEndpoint, priorityIndex);
                    qspExecState.put(qspStateKey, qspCounter.incrementAndGet());
                    logger.trace("Updated priorityExecState for sbEndpoint:({}) with priority:({})", sbEndpoint, priorityIndex);
                    logger.trace("Updated qspExecState for qspStateKey:({}) with qspCounter value:({})", qspStateKey, qspExecState.get(qspStateKey));
                }
            }
            resetExecStates(sbEndpoint, sbType);
        }
    }

    private boolean invokeServiceCallbackApi(NpmTransaction npmTransaction, boolean updateConnectionCounter) {
        logger.trace("------------- Inside NPM SM invokeServiceCallbackApi -------------");
        try {
            logger.trace("Notifying Registered Service with serviceKey:({}) to process Npm Transaction with npmTransactionId:({})",
                npmTransaction.getServiceKey(), npmTransaction.getNpmTransactionId());
            //Setting the status as PROCESSING so that same won't be picked up again in processNpmPriorityQueues
            npmTransaction.setStatus(NpmStatusEnum.PROCESSING);
            serviceRegistry.get(npmTransaction.getServiceKey()).process(npmTransaction);
            logger.trace("Notified Registered Service to process Npm Transaction with npmTransactionId:({})", npmTransaction.getNpmTransactionId());

            if (updateConnectionCounter) {
                updateConnectionCounter(npmTransaction.getSbEndpoint(), npmTransaction.getConnectionCount());
            }
        } catch (NpmException e) {
            logger.error("Notifying Registered Service with serviceKey:({}) for npmTransactionId:({}) failed with ErrorMessage:({})",
                npmTransaction.getServiceKey(), npmTransaction.getNpmTransactionId(), e.getMessage(), e);
            removeTransactionFromQueue(npmTransaction, true);
            return false;
        }
        return true;
    }

    /**
     * Resetting Execution States only if QSP met for all priorities so that Npm can reiterate in Round Robin fashion.
     */
    private void resetExecStates(String sbEndpoint, String sbType) {
        logger.trace("------------- Inside NPM SM resetExecStates -------------");
        boolean temp = true;
        final String npmPriorityQueueKey = NpmUtils.getNpmPriorityQueueKey(sbEndpoint, sbType);
        for (int priority : NpmUtils.getPriorityList(getNpmConfig("Priority_List"))) {
            if (npmPriorityQueues.containsKey(npmPriorityQueueKey) && npmPriorityQueues.get(npmPriorityQueueKey).containsKey(priority)
                && !priorityExecState.containsValue(priority)) {
                //Setting temp to false so that it won't cleanup priorityExecState and qspExecState as all priorityQueues hasn't been processed yet
                logger.trace("Execution States won't be resetting for sbEndpoint:({}) as all priorityQueues hasn't been processed yet.", sbEndpoint);
                temp = false;
                break;
            }
        }
        if (temp) {
            for (int priority : NpmUtils.getPriorityList(getNpmConfig("Priority_List"))) {
                logger.trace("Resetting Execution States for sbEndpoint:({}) as all priorityQueues processed and those needs to reiterate.", sbEndpoint);
                priorityExecState.remove(sbEndpoint);
                qspExecState.remove(sbEndpoint + "_" + priority);
            }
        }
    }

    private void updateConnectionCounter(String sbEndpoint, int connectionCounterValue) {
        logger.trace("------------- Inside NPM SM updateConnectionCounter -------------");
        //Updating connectionCounter value to be 0 or +ve integer whichever is larger
        connectionCounter.computeIfPresent(sbEndpoint, (key, value) -> Math.max((value + connectionCounterValue), 0));
        connectionCounter.computeIfAbsent(sbEndpoint, s -> Math.max(connectionCounterValue, 0));
        logger.trace("For sbEndpoint:({}) updated connectionCounter value is:({}) ", sbEndpoint, connectionCounter.get(sbEndpoint));
    }

    private void loadProperties() throws NpmException {
        logger.trace("------------- Inside NPM SM loadProperties -------------");
        String propDir = System.getProperty(NpmConstants.SDNC_CONFIG_DIR);
        if (StringUtils.isBlank(propDir)) {
            propDir = System.getenv(NpmConstants.SDNC_CONFIG_DIR);
        }
        if (StringUtils.isBlank(propDir)) {
            logger.warn("Environment variable:({}) is not set, defaulting properties directory to:({})",
                    NpmConstants.SDNC_CONFIG_DIR, NpmConstants.DEFAULT_SDNC_CONFIG_DIR);
            propDir = NpmConstants.DEFAULT_SDNC_CONFIG_DIR;
        }
        loadNpmConfig(propDir + File.separator + NpmConstants.NPM_CONFIG_PROPERTIES_FILE_NAME);
    }

    @Override
    public void loadNpmConfig(String configFilePath) throws NpmException {
        logger.trace("------------- Inside NPM SM loadNpmConfig -------------");
        try {
            logger.trace("Initializing NPM Configurations from:({})", configFilePath);
            if (new File(configFilePath).exists()) {
                try (FileInputStream configInputStream = new FileInputStream(configFilePath)) {
                    npmConfigurations.load(configInputStream);
                }
            } else {
                logger.warn("Config File:({}) not found, Initializing NPM with default configurations.", configFilePath);
                configFilePath = "properties" + File.separator + NpmConstants.NPM_CONFIG_PROPERTIES_FILE_NAME;
                npmConfigurations.load(getClass().getClassLoader().getResourceAsStream(configFilePath));
            }
            logger.trace("Initialized NPM with Configurations:({}) from configFilePath:({})", npmConfigurations, configFilePath);
        } catch (IOException e) {
            throw new NpmException(String.format("SDN-R Internal Error: Failed to load NPM Configurations form:(%s)", configFilePath), e);
        }
    }

}
