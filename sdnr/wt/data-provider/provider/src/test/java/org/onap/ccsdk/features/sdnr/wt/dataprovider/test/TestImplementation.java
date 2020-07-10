/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
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
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.AboutHttpServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.MsServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.impl.DataProviderImpl;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.StatusChangedHandler.StatusKey;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.DataProviderService;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;

import net.bytebuddy.implementation.bytecode.StackSize;

/**
 * @author Michael Dürre
 *
 */
public class TestImplementation {

    @Test
    public void test() {
        TestConfig.setSDNRDBURLEnv();
        DataProviderImpl impl = new DataProviderImpl();
        impl.setRpcProviderService(new RpcProviderService() {

            @Override
            public <S extends RpcService, T extends S> ObjectRegistration<T> registerRpcImplementation(Class<S> type,
                    T implementation, Set<InstanceIdentifier<?>> paths) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public <S extends RpcService, T extends S> ObjectRegistration<T> registerRpcImplementation(Class<S> type,
                    T implementation) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        impl.setMediatorServerServlet(new MsServlet());
        impl.setAboutServlet(new AboutHttpServlet());
        try {
            impl.init();
        } catch (Exception e) {
            e.printStackTrace();
            fail("failed to init impl: " + e.getMessage());
        }

        impl.setStatus(StatusKey.CLUSTER_SIZE, "3");
        impl.setReadyStatus(true);
        try {
            impl.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("failed to close impl: " + e.getMessage());
        }
    }

}
