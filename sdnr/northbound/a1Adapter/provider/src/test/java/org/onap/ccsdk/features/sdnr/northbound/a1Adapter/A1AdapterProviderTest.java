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

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyInstanceInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyInstanceOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyInstanceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyTypeInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyTypeOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyTypeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyInstanceInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyInstanceOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyInstanceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyTypeInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyTypeOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyTypeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetHealthCheckInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetHealthCheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetHealthCheckOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetNearRTRICsInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetNearRTRICsOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetNearRTRICsOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstanceInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstanceOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstanceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstancesInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstancesOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstancesOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypeInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypeOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypeOutputBuilder;
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
import org.powermock.reflect.Whitebox;
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
    private A1AdapterProvider a1AdapterProvider;
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
        a1AdapterProvider = new A1AdapterProvider(dataBroker, mockNotificationPublishService, mockRpcProviderRegistry,
                a1AdapterClient);
    }

    @Test
    public void test_GetNearRT_RICs() throws SvcLogicException, InterruptedException, ExecutionException {
        GetNearRTRICsOutputBuilder nearRTRICsOutputBuilder = new GetNearRTRICsOutputBuilder();
        nearRTRICsOutputBuilder.setResponseCode("200");
        Map<String, Builder<?>> serviceData =
                ImmutableMap.<String, Builder<?>>builder().put("getNearRT-RICs", nearRTRICsOutputBuilder).build();
        String rpc = "getNearRT-RICs";
        Whitebox.setInternalState(a1AdapterProvider, "serviceData", serviceData);
        Properties respProps = new Properties();
        GetNearRTRICsInputBuilder inputBuilder = new GetNearRTRICsInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetNearRTRICsOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<GetNearRTRICsOutput>> result = a1AdapterProvider.getNearRTRICs(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getHealthCheck() throws SvcLogicException, InterruptedException, ExecutionException {
        GetHealthCheckOutputBuilder healthCheckInputBuilder = new GetHealthCheckOutputBuilder();
        Map<String, Builder<?>> serviceData =
                ImmutableMap.<String, Builder<?>>builder().put("getHealthCheck", healthCheckInputBuilder).build();
        Whitebox.setInternalState(a1AdapterProvider, "serviceData", serviceData);
        GetHealthCheckInputBuilder inputBuilder = new GetHealthCheckInputBuilder();
        ListenableFuture<RpcResult<GetHealthCheckOutput>> result =
                a1AdapterProvider.getHealthCheck(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyTypes() throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyTypesOutputBuilder policyTypesOutputBuilder = new GetPolicyTypesOutputBuilder();
        policyTypesOutputBuilder.setResponseCode("200");
        Map<String, Builder<?>> serviceData =
                ImmutableMap.<String, Builder<?>>builder().put("getPolicyTypes", policyTypesOutputBuilder).build();
        String rpc = "getPolicyTypes";
        Whitebox.setInternalState(a1AdapterProvider, "serviceData", serviceData);
        Properties respProps = new Properties();
        GetPolicyTypesInputBuilder inputBuilder = new GetPolicyTypesInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypesOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<GetPolicyTypesOutput>> result =
                a1AdapterProvider.getPolicyTypes(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_createPolicyType() throws SvcLogicException, InterruptedException, ExecutionException {
        CreatePolicyTypeOutputBuilder createPolicyTypeOutputBuilder = new CreatePolicyTypeOutputBuilder();
        createPolicyTypeOutputBuilder.setResponseCode("200");
        Map<String, Builder<?>> serviceData = ImmutableMap.<String, Builder<?>>builder()
                .put("createPolicyType", createPolicyTypeOutputBuilder).build();
        String rpc = "createPolicyType";
        Whitebox.setInternalState(a1AdapterProvider, "serviceData", serviceData);
        Properties respProps = new Properties();
        CreatePolicyTypeInputBuilder inputBuilder = new CreatePolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(CreatePolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<CreatePolicyTypeOutput>> result =
                a1AdapterProvider.createPolicyType(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyType() throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyTypeOutputBuilder policyTypeOutputBuilder = new GetPolicyTypeOutputBuilder();
        policyTypeOutputBuilder.setResponseCode("200");
        Map<String, Builder<?>> serviceData =
                ImmutableMap.<String, Builder<?>>builder().put("getPolicyType", policyTypeOutputBuilder).build();
        String rpc = "getPolicyType";
        Whitebox.setInternalState(a1AdapterProvider, "serviceData", serviceData);
        Properties respProps = new Properties();
        GetPolicyTypeInputBuilder inputBuilder = new GetPolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<GetPolicyTypeOutput>> result = a1AdapterProvider.getPolicyType(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_deletePolicyType() throws SvcLogicException, InterruptedException, ExecutionException {
        DeletePolicyTypeOutputBuilder deletePolicyTypeOutputBuilder = new DeletePolicyTypeOutputBuilder();
        deletePolicyTypeOutputBuilder.setResponseCode("200");
        Map<String, Builder<?>> serviceData = ImmutableMap.<String, Builder<?>>builder()
                .put("deletePolicyType", deletePolicyTypeOutputBuilder).build();
        String rpc = "deletePolicyType";
        Whitebox.setInternalState(a1AdapterProvider, "serviceData", serviceData);
        Properties respProps = new Properties();
        DeletePolicyTypeInputBuilder inputBuilder = new DeletePolicyTypeInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<DeletePolicyTypeOutput>> result =
                a1AdapterProvider.deletePolicyType(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyInstances() throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyInstancesOutputBuilder policyInstancesOutputBuilder = new GetPolicyInstancesOutputBuilder();
        policyInstancesOutputBuilder.setResponseCode("200");
        Map<String, Builder<?>> serviceData = ImmutableMap.<String, Builder<?>>builder()
                .put("getPolicyInstances", policyInstancesOutputBuilder).build();
        String rpc = "getPolicyInstances";
        Whitebox.setInternalState(a1AdapterProvider, "serviceData", serviceData);
        Properties respProps = new Properties();
        GetPolicyInstancesInputBuilder inputBuilder = new GetPolicyInstancesInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<GetPolicyInstancesOutput>> result =
                a1AdapterProvider.getPolicyInstances(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_createPolicyInstance() throws SvcLogicException, InterruptedException, ExecutionException {
        CreatePolicyInstanceOutputBuilder createPolicyInstanceOutputBuilder = new CreatePolicyInstanceOutputBuilder();
        createPolicyInstanceOutputBuilder.setResponseCode("200");
        Map<String, Builder<?>> serviceData = ImmutableMap.<String, Builder<?>>builder()
                .put("createPolicyInstance", createPolicyInstanceOutputBuilder).build();
        String rpc = "createPolicyInstance";
        Whitebox.setInternalState(a1AdapterProvider, "serviceData", serviceData);
        Properties respProps = new Properties();
        CreatePolicyInstanceInputBuilder inputBuilder = new CreatePolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<CreatePolicyInstanceOutput>> result =
                a1AdapterProvider.createPolicyInstance(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getPolicyInstance() throws SvcLogicException, InterruptedException, ExecutionException {
        GetPolicyInstanceOutputBuilder policyInstanceOutputBuilder = new GetPolicyInstanceOutputBuilder();
        policyInstanceOutputBuilder.setResponseCode("200");
        Map<String, Builder<?>> serviceData = ImmutableMap.<String, Builder<?>>builder()
                .put("getPolicyInstance", policyInstanceOutputBuilder).build();
        String rpc = "getPolicyInstance";
        Whitebox.setInternalState(a1AdapterProvider, "serviceData", serviceData);
        Properties respProps = new Properties();
        GetPolicyInstanceInputBuilder inputBuilder = new GetPolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<GetPolicyInstanceOutput>> result =
                a1AdapterProvider.getPolicyInstance(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_deletePolicyInstance() throws SvcLogicException, InterruptedException, ExecutionException {
        DeletePolicyInstanceOutputBuilder deletePolicyInstanceOutputBuilder = new DeletePolicyInstanceOutputBuilder();
        deletePolicyInstanceOutputBuilder.setResponseCode("200");
        Map<String, Builder<?>> serviceData = ImmutableMap.<String, Builder<?>>builder()
                .put("deletePolicyInstance", deletePolicyInstanceOutputBuilder).build();
        String rpc = "deletePolicyInstance";
        Whitebox.setInternalState(a1AdapterProvider, "serviceData", serviceData);
        Properties respProps = new Properties();
        DeletePolicyInstanceInputBuilder inputBuilder = new DeletePolicyInstanceInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<DeletePolicyInstanceOutput>> result =
                a1AdapterProvider.deletePolicyInstance(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_getStatus() throws SvcLogicException, InterruptedException, ExecutionException {
        GetStatusOutputBuilder statusOutputBuilder = new GetStatusOutputBuilder();
        statusOutputBuilder.setResponseCode("200");
        Map<String, Builder<?>> serviceData =
                ImmutableMap.<String, Builder<?>>builder().put("getStatus", statusOutputBuilder).build();
        String rpc = "getStatus";
        Whitebox.setInternalState(a1AdapterProvider, "serviceData", serviceData);
        Properties respProps = new Properties();
        GetStatusInputBuilder inputBuilder = new GetStatusInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<GetStatusOutput>> result = a1AdapterProvider.getStatus(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

    @Test
    public void test_notifyPolicyEnforcementUpdate() throws SvcLogicException, InterruptedException, ExecutionException {
        NotifyPolicyEnforcementUpdateOutputBuilder notifyPolicyEnforcementUpdateOutputBuilder = new NotifyPolicyEnforcementUpdateOutputBuilder();
        notifyPolicyEnforcementUpdateOutputBuilder.setResponseCode("200");
        Map<String, Builder<?>> serviceData =
                ImmutableMap.<String, Builder<?>>builder().put("notifyPolicyEnforcementUpdate", notifyPolicyEnforcementUpdateOutputBuilder).build();
        String rpc = "notifyPolicyEnforcementUpdate";
        Whitebox.setInternalState(a1AdapterProvider, "serviceData", serviceData);
        Properties respProps = new Properties();
        NotifyPolicyEnforcementUpdateInputBuilder inputBuilder = new NotifyPolicyEnforcementUpdateInputBuilder();
        when(a1AdapterClient.hasGraph(module, rpc, null, mode)).thenReturn(true);
        when(a1AdapterClient.execute(eq(module), eq(rpc), eq(null), eq(mode), any(GetPolicyTypeOutputBuilder.class),
                eq(null))).thenReturn(respProps);
        ListenableFuture<RpcResult<NotifyPolicyEnforcementUpdateOutput>> result = a1AdapterProvider.notifyPolicyEnforcementUpdate(inputBuilder.build());
        assertNotNull(result.get().getResult().getResponseCode());
    }

}
