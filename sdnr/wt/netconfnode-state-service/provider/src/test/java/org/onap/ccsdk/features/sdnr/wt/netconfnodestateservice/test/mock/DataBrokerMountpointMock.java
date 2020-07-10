/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
 *  ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
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
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.mock;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.TransactionChainListener;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

public class DataBrokerMountpointMock implements DataBroker, BindingService {

    ReadTransaction readTransaction;

    public void setReadOnlyTransaction(ReadTransaction readTransaction) {
        this.readTransaction = readTransaction;
    }

    @Override
    public @NonNull ReadTransaction newReadOnlyTransaction() {
        return null;
    }

    @Override
    public @NonNull ReadWriteTransaction newReadWriteTransaction() {
        return null;
    }

    @Override
    public @NonNull WriteTransaction newWriteOnlyTransaction() {
        return null;
    }

    @Override
    public <T extends DataObject, L extends DataTreeChangeListener<T>> @NonNull ListenerRegistration<L> registerDataTreeChangeListener(
            @NonNull DataTreeIdentifier<T> treeId, @NonNull L listener) {
        return null;
    }

    @Override
    public @NonNull TransactionChain createTransactionChain(@NonNull TransactionChainListener listener) {
        return null;
    }


}
