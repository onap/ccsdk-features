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
package org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test.mock.odlapi;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.controller.md.sal.binding.api.BindingService;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * @author herbert
 *
 */
@SuppressWarnings("deprecation")
public class DataBrokerMountpointMock implements DataBroker, BindingService {

    ReadOnlyTransaction readOnlyTransaction;

    public void setReadOnlyTransaction(ReadOnlyTransaction readOnlyTransaction) {
        this.readOnlyTransaction = readOnlyTransaction;
    }

    public <T extends DataObject, L extends DataTreeChangeListener<T>> ListenerRegistration<L> registerDataTreeChangeListener(
            DataTreeIdentifier<T> arg0, L arg1) {
        return null;
    }

    public BindingTransactionChain createTransactionChain(TransactionChainListener listener) {
        return null;
    }

    public ReadOnlyTransaction newReadOnlyTransaction1() {
        return readOnlyTransaction;
    }

    public ReadWriteTransaction newReadWriteTransaction1() {
        return null;
    }

    public org.opendaylight.mdsal.binding.api.@NonNull WriteTransaction newWriteOnlyTransaction1() {
        return null;
    }

    @Override
    public @NonNull ReadTransaction newReadOnlyTransaction() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public org.opendaylight.mdsal.binding.api.@NonNull ReadWriteTransaction newReadWriteTransaction() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public org.opendaylight.mdsal.binding.api.@NonNull WriteTransaction newWriteOnlyTransaction() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends DataObject, L extends org.opendaylight.mdsal.binding.api.DataTreeChangeListener<T>> @NonNull ListenerRegistration<L> registerDataTreeChangeListener(
            org.opendaylight.mdsal.binding.api.@NonNull DataTreeIdentifier<T> treeId, @NonNull L listener) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NonNull TransactionChain createTransactionChain(
            org.opendaylight.mdsal.binding.api.@NonNull TransactionChainListener listener) {
        // TODO Auto-generated method stub
        return null;
    }



}
