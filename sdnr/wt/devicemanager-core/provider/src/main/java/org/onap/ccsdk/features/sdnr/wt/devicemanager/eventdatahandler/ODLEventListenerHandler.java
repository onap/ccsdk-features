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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeForwarderInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalDateAndTime;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.NetworkElementConnectionEntitiyUtil;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ProblemNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.EventHandlingService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.ConnectionOper.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Connectionlog;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.AttributeValueChangedNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.AttributeValueChangedNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ObjectCreationNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ObjectCreationNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ObjectDeletionNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ObjectDeletionNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ProblemNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ProblemNotificationBuilder;
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

/**
 * Responsible class for documenting changes in the ODL itself. The occurence of such an event is documented in the
 * database and to clients. Specific example here is the registration or deregistration of a netconf device. This
 * service has an own eventcounter to apply to the ONF Coremodel netconf behaviour.
 *
 * Important: Websocket notification must be the last action.
 *
 * @author herbert
 */

public class ODLEventListenerHandler implements EventHandlingService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ODLEventListenerHandler.class);

    private static final NetconfTimeStamp NETCONFTIME_CONVERTER = NetconfTimeStampImpl.getConverter();

    /**
     * if update NE failed delay before retrying to write data into database
     */
    private static final long DBWRITE_RETRY_DELAY_MS = 3000;

    private final String ownKeyName;
    private final WebSocketServiceClientInternal webSocketService;
    private final DataProvider databaseService;
    private final DcaeForwarderInternal aotsDcaeForwarder;
    private final DataBroker dataBroker;
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private int eventNumber;


    /*---------------------------------------------------------------
     * Construct
     */

    /**
     * Create a Service to document events to clients and within a database
     *
     * @param ownKeyName The name of this service, that is used in the database as identification key.
     * @param webSocketService service to direct messages to clients
     * @param databaseService service to write to the database
     * @param dcaeForwarder to deliver problems to external service
     */
    public ODLEventListenerHandler(String ownKeyName, WebSocketServiceClientInternal webSocketService,
            DataProvider databaseService, DcaeForwarderInternal dcaeForwarder, DataBroker dataBroker) {
        super();

        this.ownKeyName = ownKeyName;
        this.webSocketService = webSocketService;

        this.databaseService = databaseService;
        this.aotsDcaeForwarder = dcaeForwarder;
        this.dataBroker = dataBroker;

        this.eventNumber = 0;
    }

    /*---------------------------------------------------------------
     * Handling of ODL Controller events
     */

    /**
     * (NonConnected) A registration after creation of a mountpoint occured
     *
     * @param nodeId of device (mountpoint name)
     * @param nNode with mountpoint data
     */
    @Override
    public void registration(NodeId nodeId, NetconfNode nNode) {

        DateAndTime ts = NETCONFTIME_CONVERTER.getTimeStamp();
        ObjectCreationNotification notification = new ObjectCreationNotificationBuilder()
                .setObjectIdRef(nodeId.getValue()).setCounter(popEvntNumber()).setTimeStamp(ts).build();
        Connectionlog log = new ConnectionlogBuilder().setNodeId(nodeId.getValue())
                .setStatus(ConnectionLogStatus.Mounted).setTimestamp(ts).build();

        NetworkElementConnectionEntity e = NetworkElementConnectionEntitiyUtil.getNetworkConnection(nodeId.getValue(),
                nNode, getNnodeConfig(nodeId));
        LOG.debug("registration networkelement-connection for {} with status {}", nodeId.getValue(), e.getStatus());

        // Write first to prevent missing entries
        databaseService.updateNetworkConnection22(e, nodeId.getValue());
        databaseService.writeConnectionLog(log);
        webSocketService.sendViaWebsockets(new NodeId(ownKeyName), notification, ObjectCreationNotification.QNAME,
                NetconfTimeStampImpl.getConverter().getTimeStamp());
    }

    private Optional<NetconfNode> getNnodeConfig(NodeId nodeId) {
        if (this.dataBroker != null) {

            InstanceIdentifier<NetconfNode> iif = InstanceIdentifier.create(NetworkTopology.class)
                    .child(Topology.class, new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())))
                    .child(Node.class, new NodeKey(nodeId)).augmentation(NetconfNode.class);

            //Implicit close of try with resource is not handled correctly by underlying opendaylight NETCONF service
            @NonNull
            ReadTransaction readTransaction = this.dataBroker.newReadOnlyTransaction();
            try {
                return readTransaction.read(LogicalDatastoreType.CONFIGURATION, iif).get();
            } catch (InterruptedException e) {
                LOG.warn("InterruptedException occurred - problem requesting netconfnode again:", e);
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                LOG.warn("ExecutionException occurred - problem requesting netconfnode again:", e);
            }
        }
        return Optional.empty();
    }

    /**
     * (Connected) mountpoint state moves to connected
     *
     * @param nNodeId uuid that is nodeId or mountpointId
     * @param deviceType according to assessement
     */
    @Override
    public void connectIndication(NodeId nNodeId, NetworkElementDeviceType deviceType) {

        // Write first to prevent missing entries
        LOG.debug("updating networkelement-connection devicetype for {} with {}", nNodeId.getValue(), deviceType);
        NetworkElementConnectionEntity e =
                NetworkElementConnectionEntitiyUtil.getNetworkConnectionDeviceTpe(deviceType);
        //if updating db entry for ne connection fails retry later on (due elasticsearch max script executions error)
        if (!databaseService.updateNetworkConnectionDeviceType(e, nNodeId.getValue())) {
            this.updateNeConnectionRetryWithDelay(e, nNodeId.getValue());
        }
        DateAndTime ts = NETCONFTIME_CONVERTER.getTimeStamp();
        AttributeValueChangedNotification notification = new AttributeValueChangedNotificationBuilder()
                .setCounter(popEvntNumber()).setTimeStamp(ts).setObjectIdRef(nNodeId.getValue())
                .setAttributeName("deviceType").setNewValue(deviceType.getName()).build();
        webSocketService.sendViaWebsockets(new NodeId(ownKeyName), notification,
                AttributeValueChangedNotification.QNAME, ts);
    }

    /**
     * (NonConnected) mountpoint state changed.
     *
     * @param nodeId nodeid
     * @param netconfNode node
     */
    public void onStateChangeIndication(NodeId nodeId, NetconfNode netconfNode) {
        LOG.debug("mountpoint state changed indication for {}", nodeId.getValue());
        ConnectionStatus csts = netconfNode.getConnectionStatus();
        this.updateRegistration(nodeId, ConnectionStatus.class.getSimpleName(), csts != null ? csts.getName() : "null",
                netconfNode);

    }

    /**
     * (NonConnected) A deregistration after removal of a mountpoint occured.
     *
     * @param nodeId Name of the event that is used as key in the database.
     */
    @SuppressWarnings("null")
    @Override
    public void deRegistration(NodeId nodeId) {

        DateAndTime ts = NETCONFTIME_CONVERTER.getTimeStamp();
        ObjectDeletionNotification notification = new ObjectDeletionNotificationBuilder().setCounter(popEvntNumber())
                .setTimeStamp(ts).setObjectIdRef(nodeId.getValue()).build();
        Connectionlog log = new ConnectionlogBuilder().setNodeId(nodeId.getValue())
                .setStatus(ConnectionLogStatus.Unmounted).setTimestamp(ts).build();
        // Write first to prevent missing entries
        databaseService.removeNetworkConnection(nodeId.getValue());
        databaseService.writeConnectionLog(log);
        webSocketService.sendViaWebsockets(new NodeId(ownKeyName), notification,
                ObjectDeletionNotification.QNAME, ts);

    }

    /**
     * Mountpoint state changed .. from connected -> connecting or unable-to-connect or vis-e-versa.
     *
     * @param nodeId Name of the event that is used as key in the database.
     */
    @Override
    public void updateRegistration(NodeId nodeId, String attribute, String attributeNewValue, NetconfNode nNode) {
        DateAndTime ts = NETCONFTIME_CONVERTER.getTimeStamp();
        AttributeValueChangedNotification notification = new AttributeValueChangedNotificationBuilder()
                .setCounter(popEvntNumber()).setTimeStamp(ts).setObjectIdRef(nodeId.getValue())
                .setAttributeName(attribute).setNewValue(attributeNewValue).build();
        Connectionlog log = new ConnectionlogBuilder().setNodeId(nodeId.getValue())
                .setStatus(getStatus(attributeNewValue)).setTimestamp(ts).build();
        NetworkElementConnectionEntity e = NetworkElementConnectionEntitiyUtil.getNetworkConnection(nodeId.getValue(),
                nNode, getNnodeConfig(nodeId));
        LOG.debug("updating networkelement-connection for {} with status {}", nodeId.getValue(), e.getStatus());

        //if updating db entry for ne connection fails retry later on (due elasticsearch max script executions error)
        if (!databaseService.updateNetworkConnection22(e, nodeId.getValue())) {
            this.updateNeConnectionRetryWithDelay(nNode, nodeId.getValue());
        }
        databaseService.writeConnectionLog(log);
        webSocketService.sendViaWebsockets(new NodeId(ownKeyName), notification,
                AttributeValueChangedNotification.QNAME, ts);
    }


    private void updateNeConnectionRetryWithDelay(NetconfNode nNode, String registrationName) {
        LOG.debug("try to rewrite networkelement-connection in {} for node {}", DBWRITE_RETRY_DELAY_MS,
                registrationName);
        executor.execute(new DelayedThread(DBWRITE_RETRY_DELAY_MS) {
            @Override
            public void run() {
                super.run();
                databaseService.updateNetworkConnection22(
                        NetworkElementConnectionEntitiyUtil.getNetworkConnection(registrationName, nNode),
                        registrationName);
            }
        });
    }

    private void updateNeConnectionRetryWithDelay(NetworkElementConnectionEntity e, String registrationName) {
        LOG.debug("try to rewrite networkelement-connection in {} for node {}", DBWRITE_RETRY_DELAY_MS,
                registrationName);
        executor.execute(new DelayedThread(DBWRITE_RETRY_DELAY_MS) {
            @Override
            public void run() {
                super.run();
                databaseService.updateNetworkConnection22(e, registrationName);
            }
        });
    }

    /**
     * At a mountpoint a problem situation is indicated
     *
     * @param registrationName indicating object within SDN controller, normally the mountpointName
     * @param problemName that changed
     * @param problemSeverity of the problem according to NETCONF/YANG
     */

    public void onProblemNotification(String registrationName, String problemName, InternalSeverity problemSeverity) {
        LOG.debug("Got event of {} {} {}", registrationName, problemName, problemSeverity);
        // notification

        ProblemNotificationXml notificationXml =
                new ProblemNotificationXml(ownKeyName, registrationName, problemName, problemSeverity,
                        // popEvntNumberAsString(), InternalDateAndTime.TESTPATTERN );
                        popEvntNumber(), InternalDateAndTime.valueOf(NETCONFTIME_CONVERTER.getTimeStamp()));
        DateAndTime ts = NETCONFTIME_CONVERTER.getTimeStamp();
        ProblemNotification notification =
                new ProblemNotificationBuilder().setObjectIdRef(registrationName).setCounter(popEvntNumber())
                        .setProblem(problemName).setSeverity(InternalSeverity.toYang(problemSeverity)).build();
        databaseService.writeFaultLog(notificationXml.getFaultlog(SourceType.Controller));
        databaseService.updateFaultCurrent(notificationXml.getFaultcurrent());

        aotsDcaeForwarder.sendProblemNotificationUsingMaintenanceFilter(new NodeId(ownKeyName), notificationXml);

        webSocketService.sendViaWebsockets(new NodeId(ownKeyName), notification, ProblemNotification.QNAME, ts);
    }

    @Override
    public void writeEventLog(String objectId, String msg, String value) {

        LOG.debug("Got startComplete");
        EventlogBuilder eventlogBuilder = new EventlogBuilder();
        eventlogBuilder.setNodeId(ownKeyName).setTimestamp(new DateAndTime(NETCONFTIME_CONVERTER.getTimeStamp()))
                .setObjectId(objectId).setAttributeName(msg).setNewValue(value).setCounter(popEvntNumber())
                .setSourceType(SourceType.Controller);
        databaseService.writeEventLog(eventlogBuilder.build());

    }

    /*---------------------------------------------
     * Handling of ODL Controller events
     */

    /**
     * Called on exit to remove everything for a node from the current list.
     *
     * @param nodeName to remove all problems for
     * @return Number of deleted objects
     */
    public int removeAllCurrentProblemsOfNode(String nodeName) {
        return databaseService.clearFaultsCurrentOfNodeWithObjectId(ownKeyName, nodeName);
    }

    /*---------------------------------------------------------------
     * Get/Set
     */

    /**
     * @return the ownKeyName
     */
    public String getOwnKeyName() {
        return ownKeyName;
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
        executor.awaitTermination(DBWRITE_RETRY_DELAY_MS * 3, TimeUnit.SECONDS);
    }

    /*---------------------------------------------------------------
     * Private
     */
    private Integer popEvntNumber() {
        return eventNumber++;
    }

    private static ConnectionLogStatus getStatus(String newValue) {

        if (newValue.equals(ConnectionStatus.Connected.getName())) {
            return ConnectionLogStatus.Connected;

        } else if (newValue.equals(ConnectionStatus.Connecting.getName())) {
            return ConnectionLogStatus.Connecting;

        } else if (newValue.equals(ConnectionStatus.UnableToConnect.getName())) {
            return ConnectionLogStatus.UnableToConnect;

        }
        return ConnectionLogStatus.Undefined;
    }

    private class DelayedThread extends Thread {
        private final long delay;

        public DelayedThread(long delayms) {
            this.delay = delayms;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(this.delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
