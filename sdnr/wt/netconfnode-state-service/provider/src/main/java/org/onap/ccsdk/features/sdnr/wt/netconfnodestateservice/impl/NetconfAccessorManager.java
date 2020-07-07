package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfAccessorManager {

    private static final Logger log = LoggerFactory.getLogger(NetconfNodeStateServiceImpl.class);

    private static final TransactionUtils TRANSACTIONUTILS = new GenericTransactionUtils();
    private final ConcurrentHashMap<String, NetconfAccessorImpl> accessorList;

    public NetconfAccessorManager() {
        accessorList = new ConcurrentHashMap<>();
    }

    public NetconfAccessor getAccessor(NodeId nNodeId, NetconfNode netconfNode, DataBroker netconfNodeDataBroker,
            MountPoint mountPoint) {
        NetconfAccessorImpl res =
                new NetconfAccessorImpl(nNodeId, netconfNode, netconfNodeDataBroker, mountPoint, TRANSACTIONUTILS);
        NetconfAccessor previouse = accessorList.put(nNodeId.getValue(), res);
        if (Objects.nonNull(previouse)) {
            log.warn("Accessor with name already available. Replaced with new one.");
        }
        return res;
    }

    public boolean containes(NodeId nNodeId) {
        return accessorList.containsKey(nNodeId.getValue());
    }

    public void removeAccessor(NodeId nNodeId) {
        accessorList.remove(nNodeId.getValue());
    }
}
