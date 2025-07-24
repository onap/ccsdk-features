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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.housekeeping;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.DeviceMonitor;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler.ODLEventListenerHandler;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.DeviceManagerImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResyncNetworkElementHouskeepingService implements ResyncNetworkElementsListener {

    private static final Logger LOG = LoggerFactory.getLogger(ResyncNetworkElementHouskeepingService.class);

    private static final InstanceIdentifier<Topology> NETCONF_TOPO_IID =
            InstanceIdentifier.create(NetworkTopology.class).child(Topology.class,
                    new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));

    // Services to use
    private final MountPointService mountPointService;
    private final ODLEventListenerHandler odlEventListenerHandler;
    private final DataProvider databaseClientEvents;
    private final DeviceMonitor deviceMonitor;
    private final DeviceManagerImpl deviceManager;

    /** Thread is started to du the clean up action **/
    private Thread threadDoClearCurrentFaultByNodename;
    /** Indicate number of refresh activities for log **/
    private int refreshCounter = 0;

    /**
     * @param deviceManager to provide devices information
     * @param mountPointService service
     * @param odlEventListenerHandler handler for events
     * @param databaseClientEvents database to clean
     * @param deviceMonitor devicemonitor
     */
    public ResyncNetworkElementHouskeepingService(DeviceManagerImpl deviceManager, MountPointService mountPointService,
            ODLEventListenerHandler odlEventListenerHandler, DataProvider databaseClientEvents,
            DeviceMonitor deviceMonitor) {
        super();
        this.deviceManager = deviceManager;
        this.mountPointService = mountPointService;
        this.odlEventListenerHandler = odlEventListenerHandler;
        this.databaseClientEvents = databaseClientEvents;
        this.deviceMonitor = deviceMonitor;
    }

    /**
     * Async RPC Interface implementation
     */
    @Override
    public @NonNull List<String> doClearCurrentFaultByNodename(@Nullable List<String> nodeNames)
            throws IllegalStateException {

        if (this.databaseClientEvents == null) {
            throw new IllegalStateException("dbEvents service not instantiated");
        }

        if (threadDoClearCurrentFaultByNodename != null && threadDoClearCurrentFaultByNodename.isAlive()) {
            throw new IllegalStateException("A clear task is already active");
        } else {
            List<String> nodeNamesInput;

            // Create list of mountpoints if input is empty, using the content in ES
            if (nodeNames == null || nodeNames.size() <= 0) {
                nodeNamesInput = this.databaseClientEvents.getAllNodesWithCurrentAlarms();
            } else {
                nodeNamesInput = nodeNames;
            }

            // Filter all mountpoints from input that were found and are known to this Cluster-node instance of
            // DeviceManager
            final List<String> nodeNamesHandled = new ArrayList<>();
            for (String mountpointName : nodeNamesInput) {
                LOG.info("Work with mountpoint {}", mountpointName);

                if (odlEventListenerHandler != null && mountpointName.equals(odlEventListenerHandler.getOwnKeyName())) {

                    // SDN Controller related alarms
                    // -- can not be recreated on all nodes in connected state
                    // -- would result in a DCAE/AAI Notification
                    // Conclusion for 1810 Delivery ... not covered by RPC function (See issue #43)
                    LOG.info("Ignore SDN Controller related alarms for {}", mountpointName);
                    // this.databaseClientEvents.clearFaultsCurrentOfNode(mountpointName);
                    // nodeNamesHandled.add(mountpointName);

                } else {

                    if (mountPointService != null) {
                        InstanceIdentifier<Node> instanceIdentifier =
                                NETCONF_TOPO_IID.child(Node.class, new NodeKey(new NodeId(mountpointName)));
                        Optional<MountPoint> optionalMountPoint = mountPointService.getMountPoint(instanceIdentifier);

                        if (!optionalMountPoint.isPresent()) {
                            LOG.info("Remove Alarms for unknown mountpoint {}", mountpointName);
                            this.databaseClientEvents.clearFaultsCurrentOfNode(mountpointName);
                            nodeNamesHandled.add(mountpointName);
                        } else {
                            if (deviceManager.getConnectedNeByMountpoint(mountpointName) != null) {
                                LOG.info("At node known mountpoint {}", mountpointName);
                                nodeNamesHandled.add(mountpointName);
                            } else {
                                LOG.info("At node unknown mountpoint {}", mountpointName);
                            }
                        }
                    }
                }
            }

            // Force a sync
            deviceMonitor.refreshAlarmsInDb();

            threadDoClearCurrentFaultByNodename = new Thread(() -> {
                refreshCounter++;
                LOG.info("Start refresh mountpoint task {}", refreshCounter);
                // for(String nodeName:nodeNamesOutput) {
                for (String nodeName : nodeNamesHandled) {
                    NetworkElement ne = deviceManager.getConnectedNeByMountpoint(nodeName);
                    if (ne != null) {
                        LOG.info("Refresh mountpoint {}", nodeName);
                        ne.warmstart();
                    } else {
                        LOG.info("Unhandled mountpoint {}", nodeName);
                    }
                }
                LOG.info("End refresh mountpoint task {}", refreshCounter);
            });
            threadDoClearCurrentFaultByNodename.start();
            return nodeNamesHandled;
        }
    };

}
