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
import org.onap.ccsdk.features.lib.npm.NpmException;
import org.onap.ccsdk.features.lib.npm.models.NpmAck;
import org.onap.ccsdk.features.lib.npm.models.NpmStatusEnum;
import org.onap.ccsdk.features.lib.npm.models.NpmTransaction;
import org.onap.ccsdk.features.lib.npm.utils.NpmUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * The interface Npm Transaction Service provider.
 *
 * @author Kapil Singal
 */
public class NpmTransactionServiceImpl implements NpmTransactionService {
    private static final Logger logger = LoggerFactory.getLogger(NpmTransactionServiceImpl.class);
    private final NpmServiceManager npmServiceManager;

    public NpmTransactionServiceImpl(NpmServiceManager npmServiceManager) {
        this.npmServiceManager = npmServiceManager;
    }

    @Override
    public List<NpmAck> addTransactionsToQueue(List<NpmTransaction> npmTransactionList) {
        logger.debug("------------- Inside NPM TS addTransactionsToQueue (List<NpmAck>) -------------");
        logger.trace("Received addTransactionsToQueue(List) for Npm Transactions:({})", npmTransactionList);

        List<NpmAck> npmAckList = new ArrayList<>();
        for (NpmTransaction npmTransaction : npmTransactionList) {
            logger.trace("Npm Transaction with npmTransactionId:({}) is being queued.", npmTransaction.getNpmTransactionId());
            npmAckList.add(addTransactionsToQueue(npmTransaction));
        }
        logger.trace("Responding with npmAckList:({})", npmAckList);
        return npmAckList;
    }

    @Override
    public NpmAck addTransactionsToQueue(NpmTransaction npmTransaction) {
        logger.debug("------------- Inside NPM TS addTransactionsToQueue -------------");
        logger.trace("Received addTransactionsToQueue for Npm Transaction:({})", npmTransaction);

        NpmAck npmAck = new NpmAck();
        npmAck.setNpmTransactionId(npmTransaction.getNpmTransactionId());
        npmAck.setRequestId(npmTransaction.getRequestId());
        try {
            //Validate Npm Transaction before creating entry to NPM_TRANSACTION Table.
            npmTransaction.validate();
            if (Arrays.stream(NpmUtils.getPriorityList(npmServiceManager.getNpmConfig("Priority_List"))).noneMatch(i -> i == npmTransaction.getPriority())) {
                // Setting up the configured default priority value it it's missing in request
                npmTransaction.setPriority(NumberUtils.toInt(npmServiceManager.getNpmConfig("Default_Priority"), 2));
                logger.trace("Default priority value:({}) has been set, as it's missing in request.", npmTransaction.getPriority());
            }
            logger.trace("Trying to queue Npm Transaction");
            npmServiceManager.addTransactionToQueue(npmTransaction);
            npmAck.setStatus(NpmStatusEnum.QUEUED);
            npmAck.setMessage("Added to Priority Queue.");
        } catch (NpmException e) {
            logger.error("Failed to queue Npm Transaction.\nErrorMessage: {}", e.getMessage(), e);
            npmAck.setStatus(NpmStatusEnum.FAILED);
            npmAck.setMessage(e.getMessage());
        }
        return npmAck;
    }

    @Override
    public List<NpmAck> removeTransactionsFromQueue(List<NpmTransaction> npmTransactionList) {
        logger.debug("------------- Inside NPM TS removeTransactionsFromQueue (List<NpmAck>) -------------");
        logger.trace("Received removeTransactionsFromQueue for Npm Transactions:({})", npmTransactionList);

        List<NpmAck> npmAckList = new ArrayList<>();
        for (NpmTransaction npmTransaction : npmTransactionList) {
            npmAckList.add(removeTransactionsFromQueue(npmTransaction));
        }
        logger.trace("Responding with npmAckList:({})", npmAckList);
        return npmAckList;
    }

    @Override
    public NpmAck removeTransactionsFromQueue(NpmTransaction npmTransaction) {
        logger.debug("------------- Inside NPM TS removeTransactionsFromQueue -------------");
        logger.trace("Received removeTransactionsFromQueue for Npm Transactions:({})", npmTransaction);

        npmServiceManager.removeTransactionFromQueue(npmTransaction, true);

        NpmAck npmAck = new NpmAck();
        npmAck.setNpmTransactionId(npmTransaction.getNpmTransactionId());
        npmAck.setRequestId(npmTransaction.getRequestId());
        npmAck.setStatus(NpmStatusEnum.PROCESSED);
        npmAck.setMessage("Removed from Priority Queue");
        logger.trace("Responding with npmAck:({})", npmAck);
        return npmAck;
    }

    @Override
    public List<NpmTransaction> retrieveTransactionFromQueue(String sbEndpoint, String sbType) {
        logger.debug("------------- Inside NPM TS retrieveQueueStatus (List<NpmTransaction>) -------------");
        logger.trace("Received retrieveTransactionFromQueue for sbEndpoint:({}) and sbType:({})", sbEndpoint, sbType);
        return npmServiceManager.retrieveTransactionFromQueue(sbEndpoint, sbType);
    }

    @Override
    public Map<String, Map<Integer, NavigableSet<NpmTransaction>>> retrieveNpmPriorityQueues() {
        logger.debug("------------- Inside NPM TS retrieveAllPriorityQueues (Map<sbEndpoint, Map<priority, NavigableSet<NpmTransaction>>>) -------------");
        return npmServiceManager.retrieveNpmPriorityQueues();
    }

    @Override
    public void registerService(String serviceKey, NpmServiceCallbackApi npmServiceCallbackApi) {
        logger.trace("Registering NpmServiceCallbackApi with serviceKey:({})", serviceKey);
        npmServiceManager.registerService(serviceKey, npmServiceCallbackApi);
    }

    @Override
    public boolean loadNpmConfig(String configFilePath) {
        try {
            npmServiceManager.loadNpmConfig(configFilePath);
            return true;
        } catch (NpmException e) {
            logger.trace("Loading configurations from file:({}), failed with:({}): ", configFilePath, e.getMessage(), e);
        }
        return false;
    }

    @Override
    public NpmTransaction formNpmTransaction(String sbEndpoint, String sbType, String serviceKey, Object serviceRequest) {
        return NpmUtils.formNpmTransaction(null, sbEndpoint, sbType, -1, -1,
            null, null, serviceKey, serviceRequest);
    }

    @Override
    public NpmTransaction formNpmTransaction(UUID npmTransactionId, String sbEndpoint, String sbType, int priority, int connectionCount,
        Instant timestamp, Instant timeToLive, String serviceKey, Object serviceRequest) {
        return NpmUtils.formNpmTransaction(npmTransactionId, sbEndpoint, sbType, priority, connectionCount,
            timestamp, timeToLive, serviceKey, serviceRequest);
    }

}
