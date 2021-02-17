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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test;

import com.google.common.util.concurrent.FluentFuture;
import java.util.Optional;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.binding.GenericTransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.Netconf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.NetconfBuilder;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TestGenericTransactionUtils extends Mockito {

    @Test
    public void testRead() {
        GenericTransactionUtils transactionUtils = new GenericTransactionUtils();

        final Class<Netconf> netconfClazz = Netconf.class;
        InstanceIdentifier<Netconf> streamsIID = InstanceIdentifier.builder(netconfClazz).build();

        Netconf netconf = new NetconfBuilder().build();
        FluentFuture<Optional<Netconf>> readResult = FluentFutures.immediateFluentFuture(Optional.of(netconf));

        ReadTransaction readTransaction = mock(ReadTransaction.class);
        when(readTransaction.read(LogicalDatastoreType.OPERATIONAL, streamsIID)).thenReturn(readResult);

        DataBroker dataBroker = mock(DataBroker.class);
        when(dataBroker.newReadOnlyTransaction()).thenReturn(readTransaction);

        Netconf res = transactionUtils.readData(dataBroker, LogicalDatastoreType.OPERATIONAL, streamsIID);
        System.out.println("Res:"+res);
    }
}
