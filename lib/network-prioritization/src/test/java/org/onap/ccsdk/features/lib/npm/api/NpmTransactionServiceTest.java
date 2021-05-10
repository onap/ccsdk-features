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

import org.junit.BeforeClass;
import org.onap.ccsdk.features.lib.npm.NpmConstants;
import org.onap.ccsdk.features.lib.npm.models.NpmAck;
import org.onap.ccsdk.features.lib.npm.models.NpmStatusEnum;
import org.onap.ccsdk.features.lib.npm.models.NpmTransaction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class NpmTransactionServiceTest {

    private static NpmTransactionServiceImpl npmTransactionService;

    private List<NpmTransaction> npmTransactionList;

    @BeforeClass
    public static void once() throws Exception {
        System.setProperty(NpmConstants.SDNC_CONFIG_DIR, "src/test/resources/properties");
        npmTransactionService = new NpmTransactionServiceImpl(new NpmServiceManagerImpl());

        npmTransactionService.registerService("npmServiceCallbackHandler", new NpmServiceCallbackHandler(npmTransactionService));
    }

    @Before
    public void before() throws Exception {

        npmTransactionList = new ArrayList<>();
        npmTransactionList.add(npmTransactionService.formNpmTransaction(null,
                "1.1.1.1",
                "EMS_ERICSSON",
                0,
                1,
                Instant.now(),
                Instant.MAX,
                "npmServiceCallbackHandler",
                new ObjectMapper().readTree("{\"attr\": \"EMS_ERICSSON_1\"}")));
        npmTransactionList.add(npmTransactionService.formNpmTransaction(null,
                "1.1.1.1",
                "EMS_ERICSSON",
                0,
                1,
                Instant.now(),
                Instant.MAX,
                "npmServiceCallbackHandler",
                new ObjectMapper().readTree("{\"attr\": \"EMS_ERICSSON_2\"}")));

        npmTransactionList.add(npmTransactionService.formNpmTransaction(null,
                "2.2.2.2",
                "EMS_NOKIA",
                0,
                1,
                Instant.now(),
                Instant.MAX,
                "npmServiceCallbackHandler",
                new ObjectMapper().readTree("{\"attr\": \"EMS_NOKIA_1\"}")));
        npmTransactionList.add(npmTransactionService.formNpmTransaction(null,
                "2.2.2.2",
                "EMS_NOKIA",
                0,
                1,
                Instant.now(),
                Instant.MAX,
                "npmServiceCallbackHandler",
                new ObjectMapper().readTree("{\"attr\": \"EMS_NOKIA_2\"}")));
    }

    @Test
    public void addTransactionsToQueue_validTransaction() {
        List<NpmAck> npmAckList = npmTransactionService.addTransactionsToQueue(npmTransactionList);
        assertEquals(npmTransactionList.size(), npmAckList.size());
        assertEquals(NpmStatusEnum.QUEUED, npmAckList.get(0).getStatus());
        assertEquals(NpmStatusEnum.QUEUED, npmAckList.get(1).getStatus());
    }

    @Test
    public void addTransactionsToQueue_invalidTransaction() {
        npmTransactionList.get(0).setServiceRequest(null);
        NpmAck npmAck = npmTransactionService.addTransactionsToQueue(npmTransactionList.get(0));
        assertEquals(NpmStatusEnum.FAILED, npmAck.getStatus());
    }

}