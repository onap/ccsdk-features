/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.binding;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FluentFuture;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.util.StackTrace;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GenericTransactionUtils implements TransactionUtils {

    static final Logger LOG = LoggerFactory.getLogger(GenericTransactionUtils.class);

    /**
     * Deliver the data back or null. Warning
     *
     * @param <T>           SubType of the DataObject to be handled
     * @param dataBroker    for accessing data
     * @param dataStoreType to address datastore
     * @param iid           id to access data
     * @return null or object
     */
    @Override
    @Nullable
    public <T extends DataObject> T readData(DataBroker dataBroker, LogicalDatastoreType dataStoreType,
            InstanceIdentifier<T> iid) {

        AtomicBoolean noErrorIndication = new AtomicBoolean();
        AtomicReference<String> statusText = new AtomicReference<>();

        @Nullable
        T obj = readDataOptionalWithStatus(dataBroker, dataStoreType, iid, noErrorIndication, statusText);

        if (!noErrorIndication.get()) {
            LOG.warn("Read transaction for identifier " + iid + " failed with status " + statusText.get());
        }

        return obj;
    }

    /**
     * Deliver the data back or null
     *
     * @param <T>               SubType of the DataObject to be handled
     * @param dataBroker        for accessing data
     * @param dataStoreType     to address datastore
     * @param iid               id to access data
     * @param noErrorIndication (Output) true if data could be read and are available and is not null
     * @param statusIndicator   (Output) String with status indications during the read.
     * @return null or object
     */
    @Override
    public @Nullable <T extends DataObject> T readDataOptionalWithStatus(DataBroker dataBroker,
            LogicalDatastoreType dataStoreType, InstanceIdentifier<T> iid, AtomicBoolean noErrorIndication,
            AtomicReference<String> statusIndicator) {

        @Nullable
        T data = null;
        noErrorIndication.set(false);

        statusIndicator.set("Preconditions");
        Preconditions.checkNotNull(dataBroker);

        statusIndicator.set("Create Read Transaction");
        try (ReadTransaction readTransaction = dataBroker.newReadOnlyTransaction()) {
            @NonNull
            FluentFuture<Optional<T>> od = readTransaction.read(dataStoreType, iid);
            statusIndicator.set("Read done");
            statusIndicator.set("Unwrap checkFuture done");
            Optional<T> optionalData = od.get();
            statusIndicator.set("Unwrap optional done");
            data = optionalData.orElse(null);
            statusIndicator.set("Read transaction done");
            noErrorIndication.set(true);
        } catch (CancellationException | ExecutionException | InterruptedException | NoSuchElementException e) {
            statusIndicator.set(StackTrace.toString(e));
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOG.debug("Exception during read", e);
        }

        LOG.debug("stage 2 noErrorIndication {} status text {}", noErrorIndication.get(), statusIndicator.get());

        return data;
    }
}
