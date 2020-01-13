/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.features.sdnr.northbound.a1Adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyInstanceInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyInstanceInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyInstanceOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyInstanceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyTypeInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyTypeInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyTypeOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyTypeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyInstanceInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyInstanceInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyInstanceOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyInstanceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyTypeInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyTypeInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyTypeOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyTypeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetHealthCheckInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetHealthCheckInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetHealthCheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetHealthCheckOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetNearRTRICsInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetNearRTRICsInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetNearRTRICsOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetNearRTRICsOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstanceInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstanceInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstanceOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstanceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstancesInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstancesInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstancesOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstancesOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypeInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypeInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypeOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypesInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypesInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypesOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypesOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetStatusInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetStatusOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.NotifyPolicyEnforcementUpdateInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.NotifyPolicyEnforcementUpdateOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.NotifyPolicyEnforcementUpdateOutputBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class Tests all the methods in A1AdapterProvider
 *
 * @author lathishbabu.ganesan@est.tech
 *
 */

@RunWith(MockitoJUnitRunner.Silent.class)
public class A1AdapterProviderTest {

    protected static final Logger LOG = LoggerFactory.getLogger(A1AdapterProviderTest.class);

    class A1AdapterProviderMock extends A1AdapterProvider {

        A1AdapterProviderMock(final DataBroker dataBroker, final NotificationPublishService notificationPublishService,
                final RpcProviderRegistry rpcProviderRegistry, final A1AdapterClient A1AdapterClient) {
            super(dataBroker, mockNotificationPublishService, mockRpcProviderRegistry, a1AdapterClient);
        }

        @Override
        public Builder<?> getServiceData(String svcOperation) {
            return null;
        }
    }

    private A1AdapterProviderMock a1AdapterProviderMock = null;
    @Mock
    private DataBroker dataBroker;
    @Mock
    private NotificationPublishService mockNotificationPublishService;
    @Mock
    private RpcProviderRegistry mockRpcProviderRegistry;
    @Mock
    private A1AdapterClient a1AdapterClient;
    private static String module = "A1-ADAPTER-API";
    private static String mode = "sync";

    @Before
    public void setUp() throws Exception {

        a1AdapterProviderMock = new A1AdapterProviderMock(dataBroker, mockNotificationPublishService,
                mockRpcProviderRegistry, a1AdapterClient);
        a1AdapterProviderMock = Mockito.spy(a1AdapterProviderMock);

    }

    @Test
    public void test_GetNearRT_RICs() throws SvcLogicException, InterruptedException, ExecutionException {
        GetNearRTRICsOutputBuilder nearRTRICsOutputBuilder = new GetNearRTRICsOutputBuilder();
        nearRTRICsOutputBuilder.setResponseCode("200");
        String rpc = "getNearRT-RICs";
        Properties respProps = new Properties();
        GetNearRTRICsInputBuilder inputBuilder = new GetNearRTRICsInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetNearRTRICsOutputBuilder.class),
                any(Properties.class))).thenReturn(respProps);
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) nearRTRICsOutputBuilder);
        ListenableFuture<RpcResult<GetNearRTRICsOutput>> result =
                a1AdapterProviderMock.getNearRTRICs(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_GetNearRT_RICs_With_No_Input() throws SvcLogicException, InterruptedException, ExecutionException {
        GetNearRTRICsInput getNearRTRICsInput = null;
        String rpc = "getNearRT-RICs";
        GetNearRTRICsOutputBuilder nearRTRICsOutputBuilder = new GetNearRTRICsOutputBuilder();
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) nearRTRICsOutputBuilder);
        ListenableFuture<RpcResult<GetNearRTRICsOutput>> result =
                a1AdapterProviderMock.getNearRTRICs(getNearRTRICsInput);
        assertEquals("Input is null", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_GetNearRT_RICs_With_No_DG() throws SvcLogicException, InterruptedException, ExecutionException {
        GetNearRTRICsOutputBuilder nearRTRICsOutputBuilder = new GetNearRTRICsOutputBuilder();
        String rpc = "getNearRT-RICs";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) nearRTRICsOutputBuilder);
        GetNearRTRICsInputBuilder inputBuilder = new GetNearRTRICsInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(Boolean.FALSE);
        ListenableFuture<RpcResult<GetNearRTRICsOutput>> result =
                a1AdapterProviderMock.getNearRTRICs(inputBuilder.build());
        assertEquals("503", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_GetNearRT_RICs_With_DG_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetNearRTRICsOutputBuilder nearRTRICsOutputBuilder = new GetNearRTRICsOutputBuilder();
        String rpc = "getNearRT-RICs";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) nearRTRICsOutputBuilder);
        GetNearRTRICsInputBuilder inputBuilder = new GetNearRTRICsInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<GetNearRTRICsOutput>> result =
                a1AdapterProviderMock.getNearRTRICs(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_GetNearRT_RICs_With_DG_Execute_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetNearRTRICsOutputBuilder nearRTRICsOutputBuilder = new GetNearRTRICsOutputBuilder();
        String rpc = "getNearRT-RICs";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) nearRTRICsOutputBuilder);
        GetNearRTRICsInputBuilder inputBuilder = new GetNearRTRICsInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetNearRTRICsOutputBuilder.class),
                any(Properties.class))).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<GetNearRTRICsOutput>> result =
                a1AdapterProviderMock.getNearRTRICs(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getHealthCheck() throws SvcLogicException, InterruptedException, ExecutionException {
        String rpc = "getHealthCheck";
        Properties respProps = new Properties();
        GetHealthCheckOutputBuilder healthCheckOutputBuilder = new GetHealthCheckOutputBuilder();
        healthCheckOutputBuilder.setResponseCode("200");
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) healthCheckOutputBuilder);
        GetHealthCheckInputBuilder inputBuilder = new GetHealthCheckInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetHealthCheckOutputBuilder.class),
                any(Properties.class))).thenReturn(respProps);
        ListenableFuture<RpcResult<GetHealthCheckOutput>> result =
                a1AdapterProviderMock.getHealthCheck(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getHealthCheck_With_No_Input() throws SvcLogicException, InterruptedException, ExecutionException {
        GetHealthCheckInput getHealthCheckInput = null;
        String rpc = "getHealthCheck";
        GetHealthCheckOutputBuilder healthCheckOutputBuilder = new GetHealthCheckOutputBuilder();
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) healthCheckOutputBuilder);
        ListenableFuture<RpcResult<GetHealthCheckOutput>> result =
                a1AdapterProviderMock.getHealthCheck(getHealthCheckInput);
        assertEquals("Input is null", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getHealthCheck_With_No_DG() throws SvcLogicException, InterruptedException, ExecutionException {
        GetHealthCheckOutputBuilder healthCheckOutputBuilder = new GetHealthCheckOutputBuilder();
        String rpc = "getHealthCheck";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) healthCheckOutputBuilder);
        GetHealthCheckInputBuilder inputBuilder = new GetHealthCheckInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(Boolean.FALSE);
        ListenableFuture<RpcResult<GetHealthCheckOutput>> result =
                a1AdapterProviderMock.getHealthCheck(inputBuilder.build());
        assertEquals("503", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getHealthCheck_With_DG_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetHealthCheckOutputBuilder healthCheckOutputBuilder = new GetHealthCheckOutputBuilder();
        String rpc = "getHealthCheck";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) healthCheckOutputBuilder);
        GetHealthCheckInputBuilder inputBuilder = new GetHealthCheckInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<GetHealthCheckOutput>> result =
                a1AdapterProviderMock.getHealthCheck(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getHealthCheck_With_DG_Execute_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetHealthCheckOutputBuilder healthCheckOutputBuilder = new GetHealthCheckOutputBuilder();
        String rpc = "getHealthCheck";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) healthCheckOutputBuilder);
        GetHealthCheckInputBuilder inputBuilder = new GetHealthCheckInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetHealthCheckOutputBuilder.class),
                any(Properties.class))).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<GetHealthCheckOutput>> result =
                a1AdapterProviderMock.getHealthCheck(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyTypes() throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyTypesOutputBuilder policyTypesOutputBuilder = new GetPolicyTypesOutputBuilder();
        policyTypesOutputBuilder.setResponseCode("200");
        String rpc = "getPolicyTypes";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyTypesOutputBuilder);
        Properties respProps = new Properties();
        GetPolicyTypesInputBuilder inputBuilder = new GetPolicyTypesInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypesOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<GetPolicyTypesOutput>> result =
                a1AdapterProviderMock.getPolicyTypes(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyTypes_With_No_Input() throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyTypesInput getHealthCheckInput = null;
        String rpc = "getPolicyTypes";
        GetPolicyTypesOutputBuilder policyTypesOutputBuilder = new GetPolicyTypesOutputBuilder();
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyTypesOutputBuilder);
        ListenableFuture<RpcResult<GetPolicyTypesOutput>> result =
                a1AdapterProviderMock.getPolicyTypes(getHealthCheckInput);
        assertEquals("Input is null", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyTypes_With_No_DG() throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyTypesOutputBuilder policyTypesOutputBuilder = new GetPolicyTypesOutputBuilder();
        String rpc = "getPolicyTypes";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyTypesOutputBuilder);
        GetPolicyTypesInputBuilder inputBuilder = new GetPolicyTypesInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(Boolean.FALSE);
        ListenableFuture<RpcResult<GetPolicyTypesOutput>> result =
                a1AdapterProviderMock.getPolicyTypes(inputBuilder.build());
        assertEquals("503", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyTypes_With_DG_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyTypesOutputBuilder policyTypesOutputBuilder = new GetPolicyTypesOutputBuilder();
        String rpc = "getPolicyTypes";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyTypesOutputBuilder);
        GetPolicyTypesInputBuilder inputBuilder = new GetPolicyTypesInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<GetPolicyTypesOutput>> result =
                a1AdapterProviderMock.getPolicyTypes(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyTypes_With_DG_Execute_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyTypesOutputBuilder policyTypesOutputBuilder = new GetPolicyTypesOutputBuilder();
        String rpc = "getPolicyTypes";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyTypesOutputBuilder);
        GetPolicyTypesInputBuilder inputBuilder = new GetPolicyTypesInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypesOutputBuilder.class),
                any(Properties.class))).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<GetPolicyTypesOutput>> result =
                a1AdapterProviderMock.getPolicyTypes(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_createPolicyType() throws SvcLogicException, InterruptedException, ExecutionException {
        CreatePolicyTypeOutputBuilder createPolicyTypeOutputBuilder = new CreatePolicyTypeOutputBuilder();
        createPolicyTypeOutputBuilder.setResponseCode("200");
        String rpc = "createPolicyType";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) createPolicyTypeOutputBuilder);
        Properties respProps = new Properties();
        CreatePolicyTypeInputBuilder inputBuilder = new CreatePolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(CreatePolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<CreatePolicyTypeOutput>> result =
                a1AdapterProviderMock.createPolicyType(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_createPolicyType_With_No_Input()
            throws SvcLogicException, InterruptedException, ExecutionException {
        CreatePolicyTypeInput createPolicyTypeInput = null;
        String rpc = "createPolicyType";
        CreatePolicyTypeOutputBuilder createPolicyTypeOutputBuilder = new CreatePolicyTypeOutputBuilder();
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) createPolicyTypeOutputBuilder);
        ListenableFuture<RpcResult<CreatePolicyTypeOutput>> result =
                a1AdapterProviderMock.createPolicyType(createPolicyTypeInput);
        assertEquals("Input is null", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_createPolicyType_With_No_DG() throws SvcLogicException, InterruptedException, ExecutionException {
        CreatePolicyTypeOutputBuilder createPolicyTypeOutputBuilder = new CreatePolicyTypeOutputBuilder();
        String rpc = "createPolicyType";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) createPolicyTypeOutputBuilder);
        CreatePolicyTypeInputBuilder inputBuilder = new CreatePolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(Boolean.FALSE);
        ListenableFuture<RpcResult<CreatePolicyTypeOutput>> result =
                a1AdapterProviderMock.createPolicyType(inputBuilder.build());
        assertEquals("503", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_createPolicyType_With_DG_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        CreatePolicyTypeOutputBuilder createPolicyTypeOutputBuilder = new CreatePolicyTypeOutputBuilder();
        String rpc = "createPolicyType";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) createPolicyTypeOutputBuilder);
        CreatePolicyTypeInputBuilder inputBuilder = new CreatePolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<CreatePolicyTypeOutput>> result =
                a1AdapterProviderMock.createPolicyType(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_createPolicyType_With_DG_Execute_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        CreatePolicyTypeOutputBuilder createPolicyTypeOutputBuilder = new CreatePolicyTypeOutputBuilder();
        String rpc = "createPolicyType";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) createPolicyTypeOutputBuilder);
        CreatePolicyTypeInputBuilder inputBuilder = new CreatePolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(CreatePolicyTypeOutputBuilder.class),
                any(Properties.class))).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<CreatePolicyTypeOutput>> result =
                a1AdapterProviderMock.createPolicyType(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyType() throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyTypeOutputBuilder policyTypeOutputBuilder = new GetPolicyTypeOutputBuilder();
        policyTypeOutputBuilder.setResponseCode("200");
        String rpc = "getPolicyType";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyTypeOutputBuilder);
        Properties respProps = new Properties();
        GetPolicyTypeInputBuilder inputBuilder = new GetPolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<GetPolicyTypeOutput>> result =
                a1AdapterProviderMock.getPolicyType(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyType_With_No_Input() throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyTypeInput getPolicyTypeInput = null;
        String rpc = "getPolicyType";
        GetPolicyTypeOutputBuilder policyTypeOutputBuilder = new GetPolicyTypeOutputBuilder();
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyTypeOutputBuilder);
        ListenableFuture<RpcResult<GetPolicyTypeOutput>> result =
                a1AdapterProviderMock.getPolicyType(getPolicyTypeInput);
        assertEquals("Input is null", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyType_With_No_DG() throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyTypeOutputBuilder policyTypeOutputBuilder = new GetPolicyTypeOutputBuilder();
        String rpc = "getPolicyType";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyTypeOutputBuilder);
        GetPolicyTypeInputBuilder inputBuilder = new GetPolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(Boolean.FALSE);
        ListenableFuture<RpcResult<GetPolicyTypeOutput>> result =
                a1AdapterProviderMock.getPolicyType(inputBuilder.build());
        assertEquals("503", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyType_With_DG_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyTypeOutputBuilder policyTypeOutputBuilder = new GetPolicyTypeOutputBuilder();
        String rpc = "getPolicyType";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyTypeOutputBuilder);
        GetPolicyTypeInputBuilder inputBuilder = new GetPolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<GetPolicyTypeOutput>> result =
                a1AdapterProviderMock.getPolicyType(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyType_With_DG_Execute_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyTypeOutputBuilder policyTypeOutputBuilder = new GetPolicyTypeOutputBuilder();
        String rpc = "getPolicyType";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyTypeOutputBuilder);
        GetPolicyTypeInputBuilder inputBuilder = new GetPolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                any(Properties.class))).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<GetPolicyTypeOutput>> result =
                a1AdapterProviderMock.getPolicyType(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_deletePolicyType() throws SvcLogicException, InterruptedException, ExecutionException {
        DeletePolicyTypeOutputBuilder deletePolicyTypeOutputBuilder = new DeletePolicyTypeOutputBuilder();
        deletePolicyTypeOutputBuilder.setResponseCode("200");
        String rpc = "deletePolicyType";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) deletePolicyTypeOutputBuilder);
        Properties respProps = new Properties();
        DeletePolicyTypeInputBuilder inputBuilder = new DeletePolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<DeletePolicyTypeOutput>> result =
                a1AdapterProviderMock.deletePolicyType(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_deletePolicyType_With_No_Input()
            throws SvcLogicException, InterruptedException, ExecutionException {
        DeletePolicyTypeInput deletePolicyTypeInput = null;
        String rpc = "deletePolicyType";
        DeletePolicyTypeOutputBuilder deletePolicyTypeOutputBuilder = new DeletePolicyTypeOutputBuilder();
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) deletePolicyTypeOutputBuilder);
        ListenableFuture<RpcResult<DeletePolicyTypeOutput>> result =
                a1AdapterProviderMock.deletePolicyType(deletePolicyTypeInput);
        assertEquals("Input is null", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_deletePolicyType_With_No_DG() throws SvcLogicException, InterruptedException, ExecutionException {
        DeletePolicyTypeOutputBuilder deletePolicyTypeOutputBuilder = new DeletePolicyTypeOutputBuilder();
        String rpc = "deletePolicyType";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) deletePolicyTypeOutputBuilder);
        DeletePolicyTypeInputBuilder inputBuilder = new DeletePolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(Boolean.FALSE);
        ListenableFuture<RpcResult<DeletePolicyTypeOutput>> result =
                a1AdapterProviderMock.deletePolicyType(inputBuilder.build());
        assertEquals("503", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_deletePolicyType_With_DG_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        DeletePolicyTypeOutputBuilder deletePolicyTypeOutputBuilder = new DeletePolicyTypeOutputBuilder();
        String rpc = "deletePolicyType";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) deletePolicyTypeOutputBuilder);
        DeletePolicyTypeInputBuilder inputBuilder = new DeletePolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<DeletePolicyTypeOutput>> result =
                a1AdapterProviderMock.deletePolicyType(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_deletePolicyType_With_DG_Execute_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        DeletePolicyTypeOutputBuilder deletePolicyTypeOutputBuilder = new DeletePolicyTypeOutputBuilder();
        String rpc = "deletePolicyType";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) deletePolicyTypeOutputBuilder);
        DeletePolicyTypeInputBuilder inputBuilder = new DeletePolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(DeletePolicyTypeOutputBuilder.class),
                any(Properties.class))).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<DeletePolicyTypeOutput>> result =
                a1AdapterProviderMock.deletePolicyType(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyInstances() throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyInstancesOutputBuilder policyInstancesOutputBuilder = new GetPolicyInstancesOutputBuilder();
        policyInstancesOutputBuilder.setResponseCode("200");
        String rpc = "getPolicyInstances";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyInstancesOutputBuilder);
        Properties respProps = new Properties();
        GetPolicyInstancesInputBuilder inputBuilder = new GetPolicyInstancesInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<GetPolicyInstancesOutput>> result =
                a1AdapterProviderMock.getPolicyInstances(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyInstances_With_No_Input()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyInstancesInput getPolicyInstancesInput = null;
        String rpc = "getPolicyInstances";
        GetPolicyInstancesOutputBuilder policyInstancesOutputBuilder = new GetPolicyInstancesOutputBuilder();
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyInstancesOutputBuilder);
        ListenableFuture<RpcResult<GetPolicyInstancesOutput>> result =
                a1AdapterProviderMock.getPolicyInstances(getPolicyInstancesInput);
        assertEquals("Input is null", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyInstances_With_No_DG()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyInstancesOutputBuilder policyInstancesOutputBuilder = new GetPolicyInstancesOutputBuilder();
        String rpc = "getPolicyInstances";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyInstancesOutputBuilder);
        GetPolicyInstancesInputBuilder inputBuilder = new GetPolicyInstancesInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(Boolean.FALSE);
        ListenableFuture<RpcResult<GetPolicyInstancesOutput>> result =
                a1AdapterProviderMock.getPolicyInstances(inputBuilder.build());
        assertEquals("503", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyInstances_With_DG_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyInstancesOutputBuilder policyInstancesOutputBuilder = new GetPolicyInstancesOutputBuilder();
        String rpc = "getPolicyInstances";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyInstancesOutputBuilder);
        GetPolicyInstancesInputBuilder inputBuilder = new GetPolicyInstancesInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<GetPolicyInstancesOutput>> result =
                a1AdapterProviderMock.getPolicyInstances(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyInstances_With_DG_Execute_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyInstancesOutputBuilder policyInstancesOutputBuilder = new GetPolicyInstancesOutputBuilder();
        String rpc = "getPolicyInstances";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyInstancesOutputBuilder);
        GetPolicyInstancesInputBuilder inputBuilder = new GetPolicyInstancesInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode),
                any(GetPolicyInstancesOutputBuilder.class), any(Properties.class))).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<GetPolicyInstancesOutput>> result =
                a1AdapterProviderMock.getPolicyInstances(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_createPolicyInstance() throws SvcLogicException, InterruptedException, ExecutionException {
        CreatePolicyInstanceOutputBuilder createPolicyInstanceOutputBuilder = new CreatePolicyInstanceOutputBuilder();
        createPolicyInstanceOutputBuilder.setResponseCode("200");
        String rpc = "createPolicyInstance";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) createPolicyInstanceOutputBuilder);
        Properties respProps = new Properties();
        CreatePolicyInstanceInputBuilder inputBuilder = new CreatePolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode),
                any(CreatePolicyInstanceOutputBuilder.class), eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<CreatePolicyInstanceOutput>> result =
                a1AdapterProviderMock.createPolicyInstance(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_createPolicyInstance_With_No_Input()
            throws SvcLogicException, InterruptedException, ExecutionException {
        CreatePolicyInstanceInput createPolicyInstanceInput = null;
        String rpc = "createPolicyInstance";
        CreatePolicyInstanceOutputBuilder createPolicyInstanceOutputBuilder = new CreatePolicyInstanceOutputBuilder();
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) createPolicyInstanceOutputBuilder);
        ListenableFuture<RpcResult<CreatePolicyInstanceOutput>> result =
                a1AdapterProviderMock.createPolicyInstance(createPolicyInstanceInput);
        assertEquals("Input is null", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_createPolicyInstance_With_No_DG()
            throws SvcLogicException, InterruptedException, ExecutionException {
        CreatePolicyInstanceOutputBuilder createPolicyInstanceOutputBuilder = new CreatePolicyInstanceOutputBuilder();
        String rpc = "createPolicyInstance";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) createPolicyInstanceOutputBuilder);
        CreatePolicyInstanceInputBuilder inputBuilder = new CreatePolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(Boolean.FALSE);
        ListenableFuture<RpcResult<CreatePolicyInstanceOutput>> result =
                a1AdapterProviderMock.createPolicyInstance(inputBuilder.build());
        assertEquals("503", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_createPolicyInstance_With_DG_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        CreatePolicyInstanceOutputBuilder createPolicyInstanceOutputBuilder = new CreatePolicyInstanceOutputBuilder();
        String rpc = "createPolicyInstance";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) createPolicyInstanceOutputBuilder);
        CreatePolicyInstanceInputBuilder inputBuilder = new CreatePolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<CreatePolicyInstanceOutput>> result =
                a1AdapterProviderMock.createPolicyInstance(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_createPolicyInstance_With_DG_Execute_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        CreatePolicyInstanceOutputBuilder createPolicyInstanceOutputBuilder = new CreatePolicyInstanceOutputBuilder();
        String rpc = "createPolicyInstance";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) createPolicyInstanceOutputBuilder);
        CreatePolicyInstanceInputBuilder inputBuilder = new CreatePolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode),
                any(CreatePolicyInstanceOutputBuilder.class), any(Properties.class)))
                        .thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<CreatePolicyInstanceOutput>> result =
                a1AdapterProviderMock.createPolicyInstance(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyInstance() throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyInstanceOutputBuilder policyInstanceOutputBuilder = new GetPolicyInstanceOutputBuilder();
        policyInstanceOutputBuilder.setResponseCode("200");
        String rpc = "getPolicyInstance";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyInstanceOutputBuilder);
        Properties respProps = new Properties();
        GetPolicyInstanceInputBuilder inputBuilder = new GetPolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<GetPolicyInstanceOutput>> result =
                a1AdapterProviderMock.getPolicyInstance(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyInstance_With_No_Input()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyInstanceInput getPolicyInstanceInput = null;
        String rpc = "getPolicyInstance";
        GetPolicyInstanceOutputBuilder policyInstanceOutputBuilder = new GetPolicyInstanceOutputBuilder();
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyInstanceOutputBuilder);
        ListenableFuture<RpcResult<GetPolicyInstanceOutput>> result =
                a1AdapterProviderMock.getPolicyInstance(getPolicyInstanceInput);
        assertEquals("Input is null", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyInstance_With_No_DG() throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyInstanceOutputBuilder policyInstanceOutputBuilder = new GetPolicyInstanceOutputBuilder();
        String rpc = "getPolicyInstance";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyInstanceOutputBuilder);
        GetPolicyInstanceInputBuilder inputBuilder = new GetPolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(Boolean.FALSE);
        ListenableFuture<RpcResult<GetPolicyInstanceOutput>> result =
                a1AdapterProviderMock.getPolicyInstance(inputBuilder.build());
        assertEquals("503", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyInstance_With_DG_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyInstanceOutputBuilder policyInstanceOutputBuilder = new GetPolicyInstanceOutputBuilder();
        String rpc = "getPolicyInstance";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyInstanceOutputBuilder);
        GetPolicyInstanceInputBuilder inputBuilder = new GetPolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<GetPolicyInstanceOutput>> result =
                a1AdapterProviderMock.getPolicyInstance(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyInstance_With_DG_Execute_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyInstanceOutputBuilder policyInstanceOutputBuilder = new GetPolicyInstanceOutputBuilder();
        String rpc = "getPolicyInstance";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) policyInstanceOutputBuilder);
        GetPolicyInstanceInputBuilder inputBuilder = new GetPolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyInstanceOutputBuilder.class),
                any(Properties.class))).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<GetPolicyInstanceOutput>> result =
                a1AdapterProviderMock.getPolicyInstance(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_deletePolicyInstance() throws SvcLogicException, InterruptedException, ExecutionException {
        DeletePolicyInstanceOutputBuilder deletePolicyInstanceOutputBuilder = new DeletePolicyInstanceOutputBuilder();
        deletePolicyInstanceOutputBuilder.setResponseCode("200");
        String rpc = "deletePolicyInstance";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) deletePolicyInstanceOutputBuilder);
        Properties respProps = new Properties();
        DeletePolicyInstanceInputBuilder inputBuilder = new DeletePolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<DeletePolicyInstanceOutput>> result =
                a1AdapterProviderMock.deletePolicyInstance(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_deletePolicyInstance_With_No_Input()
            throws SvcLogicException, InterruptedException, ExecutionException {
        DeletePolicyInstanceInput deletePolicyInstanceInput = null;
        String rpc = "deletePolicyInstance";
        DeletePolicyInstanceOutputBuilder deletePolicyInstanceOutputBuilder = new DeletePolicyInstanceOutputBuilder();
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) deletePolicyInstanceOutputBuilder);
        ListenableFuture<RpcResult<DeletePolicyInstanceOutput>> result =
                a1AdapterProviderMock.deletePolicyInstance(deletePolicyInstanceInput);
        assertEquals("Input is null", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_deletePolicyInstance_With_No_DG() throws SvcLogicException, InterruptedException, ExecutionException {
        DeletePolicyInstanceOutputBuilder deletePolicyInstanceOutputBuilder = new DeletePolicyInstanceOutputBuilder();
        String rpc = "deletePolicyInstance";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) deletePolicyInstanceOutputBuilder);
        DeletePolicyInstanceInputBuilder inputBuilder = new DeletePolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(Boolean.FALSE);
        ListenableFuture<RpcResult<DeletePolicyInstanceOutput>> result =
                a1AdapterProviderMock.deletePolicyInstance(inputBuilder.build());
        assertEquals("503", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_deletePolicyInstance_With_DG_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        DeletePolicyInstanceOutputBuilder deletePolicyInstanceOutputBuilder = new DeletePolicyInstanceOutputBuilder();
        String rpc = "deletePolicyInstance";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) deletePolicyInstanceOutputBuilder);
        DeletePolicyInstanceInputBuilder inputBuilder = new DeletePolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<DeletePolicyInstanceOutput>> result =
                a1AdapterProviderMock.deletePolicyInstance(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_deletePolicyInstance_With_DG_Execute_Exception()
            throws SvcLogicException, InterruptedException, ExecutionException {
        DeletePolicyInstanceOutputBuilder deletePolicyInstanceOutputBuilder = new DeletePolicyInstanceOutputBuilder();
        String rpc = "deletePolicyInstance";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) deletePolicyInstanceOutputBuilder);
        DeletePolicyInstanceInputBuilder inputBuilder = new DeletePolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(DeletePolicyInstanceOutputBuilder.class),
                any(Properties.class))).thenThrow(new SvcLogicException());
        ListenableFuture<RpcResult<DeletePolicyInstanceOutput>> result =
                a1AdapterProviderMock.deletePolicyInstance(inputBuilder.build());
        assertEquals("500", result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getStatus() throws SvcLogicException, InterruptedException, ExecutionException {
        GetStatusOutputBuilder statusOutputBuilder = new GetStatusOutputBuilder();
        statusOutputBuilder.setResponseCode("200");
        String rpc = "getStatus";
        when(a1AdapterProviderMock.getServiceData(rpc)).thenReturn((Builder) statusOutputBuilder);
        Properties respProps = new Properties();
        GetStatusInputBuilder inputBuilder = new GetStatusInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<GetStatusOutput>> result = a1AdapterProviderMock.getStatus(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_notifyPolicyEnforcementUpdate()
            throws SvcLogicException, InterruptedException, ExecutionException {
        NotifyPolicyEnforcementUpdateOutputBuilder notifyPolicyEnforcementUpdateOutputBuilder =
                new NotifyPolicyEnforcementUpdateOutputBuilder();
        notifyPolicyEnforcementUpdateOutputBuilder.setResponseCode("200");
        String rpc = "notifyPolicyEnforcementUpdate";
        when(a1AdapterProviderMock.getServiceData(rpc))
                .thenReturn((Builder) notifyPolicyEnforcementUpdateOutputBuilder);
        Properties respProps = new Properties();
        NotifyPolicyEnforcementUpdateInputBuilder inputBuilder = new NotifyPolicyEnforcementUpdateInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<NotifyPolicyEnforcementUpdateOutput>> result =
                a1AdapterProviderMock.notifyPolicyEnforcementUpdate(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }


}
