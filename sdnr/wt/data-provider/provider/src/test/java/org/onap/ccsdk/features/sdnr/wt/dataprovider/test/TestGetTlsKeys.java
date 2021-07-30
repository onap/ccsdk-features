/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.google.common.util.concurrent.FluentFuture;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.MsServlet;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.impl.DataProviderServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.test.util.HostInfoForTest;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.keystore.rev171017.Keystore;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadTlsKeyEntryInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadTlsKeyEntryInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadTlsKeyEntryOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Pagination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.PaginationBuilder;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class TestGetTlsKeys {

    private static final int DBPORT = HostInfoForTest.get()[0].port;
    private static RpcProviderService rpcProviderService = mock(RpcProviderService.class);
    private static MsServlet msServlet = mock(MsServlet.class);
    private static DataBroker dataBroker = mock(DataBroker.class);
    private static DataProviderServiceImpl dataProvider = null;

    @BeforeClass
    public static void init() {
        String configContent = "[es]\n" + "esHosts=" + String.format("http://localhost:%d", DBPORT) + "\n"
                + "esArchiveLifetimeSeconds=2592000\n" + "esCluster=\n" + "esArchiveCheckIntervalSeconds=0\n"
                + "esNode=elasticsearchnode\n" + "esAuthUsername=${SDNRDBUSERNAME}\n"
                + "esAuthPassword=${SDNRDBPASSWORD}\n" + "esTrustAllCerts=${SDNRDBTRUSTALLCERTS}\n" + "";
        try {
            finish();
            Files.createDirectories(new File("etc").toPath());
            Files.write(new File(DataProviderServiceImpl.CONFIGURATIONFILE).toPath(), configContent.getBytes(),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        when(dataBroker.newReadOnlyTransaction())
                .thenReturn(new ReadHelperTransaction("src/test/resources/tlskeys/keys1.json", Keystore.class));
        try {
            dataProvider = new DataProviderServiceImpl(rpcProviderService, msServlet, dataBroker);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @AfterClass
    public static void finish() {
        File f = new File(DataProviderServiceImpl.CONFIGURATIONFILE);
        if (f.exists()) {
            f.delete();
        }
        File folder = new File("etc");
        if (folder.exists()) {
            folder.delete();
        }
    }

    @Test
    public void test() {
         Pagination pagination = new PaginationBuilder().setPage(Uint64.valueOf(1)).setSize(Uint32.valueOf(20)).build();
        ReadTlsKeyEntryInput firstPageInput = new ReadTlsKeyEntryInputBuilder().setPagination(pagination).build();
        RpcResult<ReadTlsKeyEntryOutput> output = null;
        try {
            output = dataProvider.readTlsKeyEntry(firstPageInput).get();
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
        ReadTlsKeyEntryOutput result = output.getResult();
        assertEquals(4,result.getPagination().getTotal().longValue());
        assertEquals(4, result.getData().size());
        assertEquals(1, result.getPagination().getPage().longValue());
        assertEquals(4, result.getPagination().getSize().longValue());
    }
    @Test
    public void test2() {
         Pagination pagination = new PaginationBuilder().setPage(Uint64.valueOf(1)).setSize(Uint32.valueOf(2)).build();
        ReadTlsKeyEntryInput firstPageInput = new ReadTlsKeyEntryInputBuilder().setPagination(pagination).build();
        RpcResult<ReadTlsKeyEntryOutput> output = null;
        try {
            output = dataProvider.readTlsKeyEntry(firstPageInput).get();
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
        ReadTlsKeyEntryOutput result = output.getResult();
        assertEquals(4,result.getPagination().getTotal().longValue());
        assertEquals(2, result.getData().size());
        assertEquals(1, result.getPagination().getPage().longValue());
        assertEquals(2, result.getPagination().getSize().longValue());
    }

    @Test
    public void test3() {
         Pagination pagination = new PaginationBuilder().setPage(Uint64.valueOf(2)).setSize(Uint32.valueOf(3)).build();
        ReadTlsKeyEntryInput firstPageInput = new ReadTlsKeyEntryInputBuilder().setPagination(pagination).build();
        RpcResult<ReadTlsKeyEntryOutput> output = null;
        try {
            output = dataProvider.readTlsKeyEntry(firstPageInput).get();
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
        ReadTlsKeyEntryOutput result = output.getResult();
        assertEquals(4,result.getPagination().getTotal().longValue());
        assertEquals(1, result.getData().size());
        assertEquals(2, result.getPagination().getPage().longValue());
        assertEquals(1, result.getPagination().getSize().longValue());
    }

    private static class ReadHelperTransaction implements ReadTransaction {
        private final String filename;
        private final YangToolsMapper mapper;
        private final Class<?> clazz;

        public ReadHelperTransaction(String filename, Class<?> clazz) {
            this.filename = filename;
            this.clazz = clazz;
            this.mapper = new YangToolsMapper();
        }

        @Override
        public @NonNull Object getIdentifier() {
            // TODO Auto-generated method stub
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends DataObject> @NonNull FluentFuture<Optional<T>> read(@NonNull LogicalDatastoreType store,
                @NonNull InstanceIdentifier<T> path) {

            T result = null;
            try {
                result = (T) this.mapper.readValue(new File(filename), this.clazz);
            } catch (IOException e) {
                return FluentFutures.immediateFluentFuture(Optional.empty());
            }
            return FluentFutures.immediateFluentFuture(Optional.of(result));
        }

        @Override
        public @NonNull FluentFuture<Boolean> exists(@NonNull LogicalDatastoreType store,
                @NonNull InstanceIdentifier<?> path) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void close() {
            // TODO Auto-generated method stub

        }
    }
}
