/*******************************************************************************
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt sdnr-wt-devicemanager-provider
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
 ******************************************************************************/

package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.mock;

import java.util.Optional;
import org.mockito.Mockito;
import org.opendaylight.mdsal.binding.api.BindingService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * @author herbert
 *
 */
public class MountPointMock extends Mockito implements MountPoint {

    private boolean databrokerAbsent = true;
    private final RpcConsumerRegistryMock rpcConsumerRegistry = new RpcConsumerRegistryMock();
    private NotificationService setReadTransaction;
    private DataBroker dataBroker;

    private static final InstanceIdentifier<Topology> NETCONF_TOPO_IID =
            InstanceIdentifier.create(NetworkTopology.class).child(Topology.class,
                    new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));

    public MountPointMock() {
        this.dataBroker = mock(DataBroker.class);
    }

    @Override
    public InstanceIdentifier<?> getIdentifier() {
        return NETCONF_TOPO_IID;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BindingService> Optional<T> getService(Class<T> service) {

        System.out.println("Requested mountpoint service: " + service.getSimpleName() + " databrokerAbsent state: "
                + databrokerAbsent);

        Optional<? extends BindingService> res;
        if (service.isInstance(dataBroker)) {
            System.out.println("Delivering databroker");
            res = databrokerAbsent ? Optional.empty() : Optional.of(dataBroker);
        } else if (service.isInstance(rpcConsumerRegistry)) {
            System.out.println("Delivering RpcConsumerRegistryMock");
            res = Optional.of(rpcConsumerRegistry);
        } else if (service.isInstance(setReadTransaction)) {
            System.out.println("Delivering notificationService");
            res = Optional.of(setReadTransaction);
        } else {
            System.out.println("Delivering no service");
            res = Optional.empty();
        }
        return (Optional<T>) res;
    }

    public void setDatabrokerAbsent(boolean state) {
        this.databrokerAbsent = state;
    }

    public DataBroker getDataBroker() {
        return dataBroker;
    }

}
