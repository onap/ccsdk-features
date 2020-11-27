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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeForwarderInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalDateAndTime;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.NetworkElementConnectionEntitiyUtil;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.AttributeValueChangedNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ObjectCreationNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ObjectDeletionNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ProblemNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.EventHandlingService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
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

@SuppressWarnings("deprecation")
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
            DataProvider databaseService, DcaeForwarderInternal dcaeForwarder) {
        super();

        this.ownKeyName = ownKeyName;
        this.webSocketService = webSocketService;

        this.databaseService = databaseService;
        this.aotsDcaeForwarder = dcaeForwarder;

        this.eventNumber = 0;
    }

    /*---------------------------------------------------------------
     * Handling of ODL Controller events
     */

    /**
     * (NonConnected) A registration after creation of a mountpoint occured
     * 
     * @param registrationName of device (mountpoint name)
     * @param nNode with mountpoint data
     */
    @Override
    public void registration(String registrationName, NetconfNode nNode) {

        ObjectCreationNotificationXml cNotificationXml = new ObjectCreationNotificationXml(ownKeyName, popEvntNumber(),
                InternalDateAndTime.valueOf(NETCONFTIME_CONVERTER.getTimeStamp()), registrationName);
        NetworkElementConnectionEntity e =
                NetworkElementConnectionEntitiyUtil.getNetworkConnection(registrationName, nNode);
        LOG.debug("registration networkelement-connection for {} with status {}", registrationName, e.getStatus());

        // Write first to prevent missing entries
        databaseService.updateNetworkConnection22(e, registrationName);
        databaseService.writeConnectionLog(cNotificationXml.getConnectionlogEntity());
        webSocketService.sendViaWebsockets(registrationName, cNotificationXml);
    }

    /**
     * (Connected) mountpoint state moves to connected
     * 
     * @param mountpointNodeName uuid that is nodeId or mountpointId
     * @param deviceType according to assessement
     */
    @Override
    public void connectIndication(String mountpointNodeName, NetworkElementDeviceType deviceType) {

        // Write first to prevent missing entries
        LOG.debug("updating networkelement-connection devicetype for {} with {}", mountpointNodeName, deviceType);
        NetworkElementConnectionEntity e =
                NetworkElementConnectionEntitiyUtil.getNetworkConnectionDeviceTpe(deviceType);
        //if updating db entry for ne connection fails retry later on (due elasticsearch max script executions error)
        if (!databaseService.updateNetworkConnectionDeviceType(e, mountpointNodeName)) {
            this.updateNeConnectionRetryWithDelay(e, mountpointNodeName);
        }

        AttributeValueChangedNotificationXml notificationXml = new AttributeValueChangedNotificationXml(ownKeyName,
                popEvntNumber(), InternalDateAndTime.valueOf(NETCONFTIME_CONVERTER.getTimeStamp()), mountpointNodeName,
                "deviceType", deviceType.name());
        webSocketService.sendViaWebsockets(mountpointNodeName, notificationXml);
    }

    /**
     * (NonConnected) mountpoint state changed.
     * 
     * @param mountpointNodeName nodeid
     * @param netconfNode node
     */
    public void onStateChangeIndication(String mountpointNodeName, NetconfNode netconfNode) {
        LOG.debug("mountpoint state changed indication for {}", mountpointNodeName);
        ConnectionStatus csts = netconfNode.getConnectionStatus();
        this.updateRegistration(mountpointNodeName, ConnectionStatus.class.getSimpleName(),
                csts != null ? csts.getName() : "null", netconfNode);

    }

    /**
     * (NonConnected) A deregistration after removal of a mountpoint occured.
     * 
     * @param registrationName Name of the event that is used as key in the database.
     */
    @SuppressWarnings("null")
    @Override
    public void deRegistration(String registrationName) {

        ObjectDeletionNotificationXml dNotificationXml = new ObjectDeletionNotificationXml(ownKeyName, popEvntNumber(),
                InternalDateAndTime.valueOf(NETCONFTIME_CONVERTER.getTimeStamp()), registrationName);

        // Write first to prevent missing entries
        databaseService.removeNetworkConnection(registrationName);
        databaseService.writeConnectionLog(dNotificationXml.getConnectionlogEntity());
        webSocketService.sendViaWebsockets(registrationName, dNotificationXml);

    }

    /**
     * Mountpoint state changed .. from connected -> connecting or unable-to-connect or vis-e-versa.
     * 
     * @param registrationName Name of the event that is used as key in the database.
     */
    @Override
    public void updateRegistration(String registrationName, String attribute, String attributeNewValue,
            NetconfNode nNode) {
        AttributeValueChangedNotificationXml notificationXml = new AttributeValueChangedNotificationXml(ownKeyName,
                popEvntNumber(), InternalDateAndTime.valueOf(NETCONFTIME_CONVERTER.getTimeStamp()), registrationName,
                attribute, attributeNewValue);
        NetworkElementConnectionEntity e =
                NetworkElementConnectionEntitiyUtil.getNetworkConnection(registrationName, nNode);
        LOG.debug("updating networkelement-connection for {} with status {}", registrationName, e.getStatus());

        //if updating db entry for ne connection fails retry later on (due elasticsearch max script executions error)
        if (!databaseService.updateNetworkConnection22(e, registrationName)) {
            this.updateNeConnectionRetryWithDelay(nNode, registrationName);
        }
        databaseService.writeConnectionLog(notificationXml.getConnectionlogEntity());
        webSocketService.sendViaWebsockets(registrationName, notificationXml);
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

        databaseService.writeFaultLog(notificationXml.getFaultlog(SourceType.Controller));
        databaseService.updateFaultCurrent(notificationXml.getFaultcurrent());

        aotsDcaeForwarder.sendProblemNotificationUsingMaintenanceFilter(ownKeyName, notificationXml);

        webSocketService.sendViaWebsockets(registrationName, notificationXml);
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
