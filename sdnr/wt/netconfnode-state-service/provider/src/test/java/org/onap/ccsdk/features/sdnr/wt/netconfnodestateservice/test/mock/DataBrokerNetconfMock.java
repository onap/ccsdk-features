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

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

public class DataBrokerNetconfMock implements DataBroker {

    private @NonNull DataTreeChangeListener<Node> listener;
    private @NonNull ClusteredDataTreeChangeListener<Node> listenerClustered;

    @Override
    public @NonNull ReadTransaction newReadOnlyTransaction() {
        return null;
    }

    @Override
    public org.opendaylight.mdsal.binding.api.@NonNull ReadWriteTransaction newReadWriteTransaction() {
        return null;
    }

    @Override
    public org.opendaylight.mdsal.binding.api.@NonNull WriteTransaction newWriteOnlyTransaction() {
        return null;
    }


    @Override
    public @NonNull TransactionChain createTransactionChain(
            org.opendaylight.mdsal.binding.api.@NonNull TransactionChainListener listener) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataObject, L extends DataTreeChangeListener<T>> @NonNull ListenerRegistration<L> registerDataTreeChangeListener(
            @NonNull DataTreeIdentifier<T> treeId, @NonNull L pListener) {
        System.out.println("Register " + pListener.getClass().getName());
        if (pListener instanceof ClusteredDataTreeChangeListener) {
            System.out.println("Clustered listener");
            this.listenerClustered = (ClusteredDataTreeChangeListener<Node>) pListener;
        } else if (pListener instanceof DataTreeChangeListener) {
            System.out.println("Listener");
            this.listener = (DataTreeChangeListener<Node>) pListener;
        }
        return new ListenerRegistration<L>() {

            @Override
            public @NonNull L getInstance() {
                return pListener;
            }

            @Override
            public void close() {}

        };
    }

    public void sendChanges(Collection<DataTreeModification<Node>> changes) {
        this.listener.onDataTreeChanged(changes);
    }

    public void sendClusteredChanges(Collection<DataTreeModification<Node>> changes) {
        this.listenerClustered.onDataTreeChanged(changes);
    }

}
