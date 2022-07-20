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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OdlPolicy;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OdlPolicy.PolicyMethods;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.HttpAuthorization;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization.Policies;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.permission.Permissions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.permission.Permissions.Actions;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MdSalAuthorizationStore {

    private static final Logger LOG = LoggerFactory.getLogger(MdSalAuthorizationStore.class.getName());

    private final DataBroker dataBroker;

    public MdSalAuthorizationStore(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public Optional<OdlPolicy> getPolicy(String path, List<String> userRoles) {
        InstanceIdentifier<Policies> iif = InstanceIdentifier.create(HttpAuthorization.class).child(Policies.class);
        Optional<Policies> odata = Optional.empty();
        // The implicite close is not handled correctly by underlaying opendaylight netconf service
        ReadTransaction transaction = this.dataBroker.newReadOnlyTransaction();
        try {
            odata = transaction.read(LogicalDatastoreType.CONFIGURATION, iif).get();
        } catch (ExecutionException e) {
            LOG.warn("unable to read policies from mdsal: ", e);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        if (odata.isEmpty()) {
            return Optional.empty();
        }
        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization.policies.Policies> data =
                odata.get().getPolicies();
        if (data == null) {
            return Optional.empty();
        }
        Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization.policies.Policies> entry =
                data.stream().filter(e -> path.equals(e.getResource())).findFirst();
        if (entry.isEmpty()) {
            return Optional.empty();
        }
        List<Permissions> permissions = entry.get().getPermissions();
        if (permissions == null) {
            return Optional.empty();
        }
        Optional<Permissions> rolePm = permissions.stream().filter((e) -> userRoles.contains(e.getRole())).findFirst();
        if (rolePm.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapPolicy(path, rolePm.get().getActions()));
    }

    private OdlPolicy mapPolicy(String path, Set<Actions> actions) {
        PolicyMethods methods = new PolicyMethods();
        String action;
        for (Actions a : actions) {
            action = a.getName().toLowerCase();
            switch (action) {
                case "get":
                    methods.setGet(true);
                    break;
                case "post":
                    methods.setPost(true);
                    break;
                case "put":
                    methods.setPut(true);
                    break;
                case "delete":
                    methods.setDelete(true);
                    break;
                case "patch":
                    methods.setPatch(true);
                    break;
                default:
                    LOG.warn("unknown http method {}", action);
                    break;
            }
        }
        return new OdlPolicy(path, methods);
    }

}
