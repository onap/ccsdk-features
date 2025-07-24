/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test.util;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.WriteTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DataBrokerHelper<T extends DataObject> implements DataBroker {

    private final T data;

    public DataBrokerHelper(T data){
        this.data = data;
    }

    @Override
    public @NonNull ReadTransaction newReadOnlyTransaction() {
        return new ReadTransaction() {
            @Override
            public void close() {

            }

            @Override
            public @NonNull <T extends DataObject> FluentFuture<Optional<T>> read(@NonNull LogicalDatastoreType store,
                    @NonNull DataObjectIdentifier<T> path) {
                return FluentFuture.from(new ListenableFuture<Optional<T>>() {
                    @Override
                    public void addListener(Runnable runnable, Executor executor) {

                    }

                    @Override
                    public boolean cancel(boolean b) {
                        return false;
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }

                    @Override
                    public boolean isDone() {
                        return true;
                    }

                    @Override
                    public Optional<T> get() throws InterruptedException, ExecutionException {
                        return (Optional<T>) Optional.of(data);
                    }

                    @Override
                    public Optional<T> get(long l, TimeUnit timeUnit)
                            throws InterruptedException, ExecutionException, TimeoutException {
                        return (Optional<T>) Optional.of(data);
                    }
                });
            }

            @Override
            public @NonNull <T extends DataObject> FluentFuture<Optional<T>> read(@NonNull LogicalDatastoreType store,
                    @NonNull InstanceIdentifier<T> path) {
                return FluentFuture.from(new ListenableFuture<Optional<T>>() {
                    @Override
                    public void addListener(Runnable runnable, Executor executor) {

                    }

                    @Override
                    public boolean cancel(boolean b) {
                        return false;
                    }

                    @Override
                    public boolean isCancelled() {
                        return false;
                    }

                    @Override
                    public boolean isDone() {
                        return true;
                    }

                    @Override
                    public Optional<T> get() throws InterruptedException, ExecutionException {
                        return (Optional<T>) Optional.of(data);
                    }

                    @Override
                    public Optional<T> get(long l, TimeUnit timeUnit)
                            throws InterruptedException, ExecutionException, TimeoutException {
                        return (Optional<T>) Optional.of(data);
                    }
                });
            }

            @Override
            public @NonNull FluentFuture<Boolean> exists(@NonNull LogicalDatastoreType store,
                    @NonNull InstanceIdentifier<?> path) {
                return null;
            }

            @Override
            public @NonNull Object getIdentifier() {
                return null;
            }
        };
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
    public TransactionChain createTransactionChain() {
        return null;
    }

    @Override
    public TransactionChain createMergingTransactionChain() {
        return null;
    }


    @Override
    public @NonNull <T extends DataObject> Registration registerTreeChangeListener(
            @NonNull LogicalDatastoreType datastore, @NonNull DataObjectReference<T> subtrees,
            @NonNull DataTreeChangeListener<T> listener) {
        return null;
    }

    @Override
    public @NonNull <T extends DataObject> Registration registerLegacyTreeChangeListener(
            @NonNull LogicalDatastoreType datastore, @NonNull DataObjectReference<T> subtrees,
            @NonNull DataTreeChangeListener<T> listener) {
        return null;
    }
}
