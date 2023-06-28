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

import com.google.common.collect.ClassToInstanceMap;
import org.eclipse.jdt.annotation.NonNull;
import static org.junit.Assert.fail;
import java.util.Set;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section.EnvGetter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.DataTreeHttpServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.MsServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.UserdataHttpServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.about.AboutHttpServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.impl.DataProviderImpl;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.StatusChangedHandler.StatusKey;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcService;

/**
 * @author Michael Dürre
 *
 */
public class TestImplementation {

    static String XY = "http://localhost:"
            + (System.getProperty("databaseport") != null ? System.getProperty("databaseport") : "49200");

    @Test
    public void test() {
        //TestConfig.setSDNRDBURLEnv("http://localhost:"+(System.getProperty("databaseport") != null ? System.getProperty("databaseport") : "49200"));
        EnvGetter env = Section.getEnvGetter();
        Section.setEnvGetter((xy) -> {
            System.out.println("Search " + xy);
            return xy.equals("SDNRDBURL") ? XY : env.getenv(xy);
        });
        DataProviderImpl impl = new DataProviderImpl();
        impl.setRpcProviderService(new RpcProviderService() {

            @Override
            public <S extends RpcService, T extends S> ObjectRegistration<T> registerRpcImplementation(Class<S> type,
                    T implementation, Set<InstanceIdentifier<?>> paths) {
                return null;
            }

            @Override
            public @NonNull Registration registerRpcImplementation(Rpc<?, ?> implementation) {
                return null;
            }

            @Override
            public @NonNull Registration registerRpcImplementation(Rpc<?, ?> implementation,
                                                                   Set<InstanceIdentifier<?>> paths) {
                return null;
            }

            @Override
            public @NonNull Registration registerRpcImplementations(ClassToInstanceMap<Rpc<?, ?>> implementations) {
                return null;
            }

            @Override
            public @NonNull Registration registerRpcImplementations(ClassToInstanceMap<Rpc<?, ?>> implementations,
                                                                    Set<InstanceIdentifier<?>> paths) {
                return null;
            }

            @Override
            public <S extends RpcService, T extends S> ObjectRegistration<T> registerRpcImplementation(Class<S> type,
                    T implementation) {
                return null;
            }
        });
        impl.setMediatorServerServlet(new MsServlet());
        impl.setAboutServlet(new AboutHttpServlet());
        impl.setTreeServlet(new DataTreeHttpServlet());
        impl.setUserdataServlet(new UserdataHttpServlet());
        try {
            impl.init();
        } catch (Exception e) {
            e.printStackTrace();
            fail("failed to init impl: " + e.getMessage());
        }

        impl.setStatus(StatusKey.CLUSTER_SIZE, "3");
        try {
            impl.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("failed to close impl: " + e.getMessage());
        }
    }

}
