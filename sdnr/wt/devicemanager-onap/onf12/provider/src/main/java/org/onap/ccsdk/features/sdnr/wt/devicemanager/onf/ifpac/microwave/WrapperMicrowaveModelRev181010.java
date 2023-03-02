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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave;



import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.pm.PerformanceDataAirInterface181010Builder;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.notifications.NotificationWorker;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.util.ONFLayerProtocolName;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.PerformanceDataLtp;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.util.InconsistentPMDataException;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.Lp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.AirInterfaceCurrentProblemTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.AirInterfaceDiversityCurrentProblemTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.AirInterfaceHistoricalPerformanceTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.AttributeValueChangedNotification;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.ContainerCurrentProblemTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.ContainerHistoricalPerformanceTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MicrowaveModelListener;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwAirInterfaceDiversityPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwAirInterfaceDiversityPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwAirInterfacePac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwAirInterfacePacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwEthernetContainerPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwEthernetContainerPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwHybridMwStructurePac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwHybridMwStructurePacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwPureEthernetStructurePac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwPureEthernetStructurePacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwTdmContainerPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwTdmContainerPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.ObjectCreationNotification;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.ObjectDeletionNotification;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.ProblemNotification;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.SeverityType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.StructureCurrentProblemTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.air._interface.diversity.pac.AirInterfaceDiversityCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.air._interface.pac.AirInterfaceConfiguration;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.air._interface.pac.AirInterfaceCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.air._interface.pac.AirInterfaceHistoricalPerformances;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.ethernet.container.pac.EthernetContainerCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.ethernet.container.pac.EthernetContainerHistoricalPerformances;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.hybrid.mw.structure.pac.HybridMwStructureCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.pure.ethernet.structure.pac.PureEthernetStructureCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.tdm.container.pac.TdmContainerCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WrapperMicrowaveModelRev181010 implements OnfMicrowaveModel, MicrowaveModelListener {

    private static final Logger LOG = LoggerFactory.getLogger(WrapperMicrowaveModelRev181010.class);

    public static final QName QNAME = MwAirInterfacePac.QNAME;


    private final NetconfBindingAccessor acessor;
    private final NotificationService microwaveModelListener;
    private final TransactionUtils genericTransactionUtils;
    private final String mountpointId;
    private final @NonNull FaultService faultService;
    private final WebsocketManagerService notificationService;

    private Optional<NotificationWorker<EventlogEntity>> notificationQueue;

    /**
     * Handle specific version of microwave model
     *
     * @param acessor to access device
     * @param serviceProvider for devicemanager services
     */
    public WrapperMicrowaveModelRev181010(@NonNull NetconfBindingAccessor acessor,
            @NonNull DeviceManagerServiceProvider serviceProvider) {
        this.acessor = acessor;
        this.genericTransactionUtils = acessor.getTransactionUtils();
        this.mountpointId = acessor.getNodeId().getValue();
        this.microwaveModelListener = serviceProvider.getNotificationService();
        this.faultService = serviceProvider.getFaultService();
        this.notificationService = serviceProvider.getWebsocketService();
        this.notificationQueue = Optional.empty();
    }

    /*-----------------------------------------------------------------------------
     * Setter/Getter
     */

    @SuppressWarnings("unchecked")
    @Override
    public <T extends NotificationListener> T getNotificationListener() {
        return (T) this;
    }

    @Override
    public void setNotificationQueue(NotificationWorker<EventlogEntity> notificationQueue) {
        this.notificationQueue = Optional.of(notificationQueue);
    }

    /*-----------------------------------------------------------------------------
     * Interfacefunctions
     */

    @Override
    public void readTheFaultsOfMicrowaveModel(ONFLayerProtocolName lpName, Class<?> lpClass, UniversalId uuid,
            FaultData resultList) {

        switch (lpName) {
            case MWAIRINTERFACE:
                readTheFaultsOfMwAirInterfacePac(uuid, resultList);
                break;

            case ETHERNETCONTAINER12:
                readTheFaultsOfMwEthernetContainerPac(uuid, resultList);
                break;

            case TDMCONTAINER:
                readTheFaultsOfMwTdmContainerPac(uuid, resultList);
                break;

            case STRUCTURE:
                if (lpClass == MwHybridMwStructurePac.class) {
                    readTheFaultsOfMwHybridMwStructurePac(uuid, resultList);

                } else if (lpClass == MwAirInterfaceDiversityPac.class) {
                    readTheFaultsOfMwAirInterfaceDiversityPac(uuid, resultList);

                } else if (lpClass == MwPureEthernetStructurePac.class) {
                    readTheFaultsOfMwPureEthernetStructurePac(uuid, resultList);

                } else {
                    LOG.warn("Unassigned lp model {} class {}", lpName, lpClass);
                }
                break;
            case ETHERNET:
                // No alarms supported
                break;
            case ETHERNETCONTAINER10:
            default:
                LOG.warn("Unassigned or not expected lp in model {}", lpName);
        }
    }

    @Override
    public @NonNull PerformanceDataLtp getLtpHistoricalPerformanceData(@NonNull ONFLayerProtocolName lpName,
            @NonNull Lp lp) throws InconsistentPMDataException {
        PerformanceDataLtp res = new PerformanceDataLtp();
        switch (lpName) {
            case MWAIRINTERFACE:
                return readAirInterfacePerformanceData(lp, res);

            case ETHERNETCONTAINER12:
                return readEthernetContainerPerformanceData(lp, res);

            case ETHERNETCONTAINER10:
            case ETHERNETPHYSICAL:
            case ETHERNET:
            case TDMCONTAINER:
            case STRUCTURE:
            case UNKNOWN:
                LOG.debug("Do not read HistoricalPM data for {} {}", lpName,
                        Helper.nnGetUniversalId(lp.getUuid()).getValue());
        }
        return res;
    }

    @Override
    public Class<?> getClassForLtpExtension(QName qName) {
        Class<?> res = null;
        if (qName.equals(MwAirInterfacePac.QNAME)) {
            res = MwAirInterfacePac.class;
        } else if (qName.equals(MwAirInterfaceDiversityPac.QNAME)) {
            res = MwAirInterfaceDiversityPac.class;
        } else if (qName.equals(MwPureEthernetStructurePac.QNAME)) {
            res = MwPureEthernetStructurePac.class;
        } else if (qName.equals(MwHybridMwStructurePac.QNAME)) {
            res = MwHybridMwStructurePac.class;
        } else if (qName.equals(MwEthernetContainerPac.QNAME)) {
            res = MwEthernetContainerPac.class;
        } else if (qName.equals(MwTdmContainerPac.QNAME)) {
            res = MwTdmContainerPac.class;
        }
        LOG.debug("Found QName {} mapped to {}", String.valueOf(qName), String.valueOf(res));
        return res;
    }

    @Override
    public void onObjectCreationNotification(ObjectCreationNotification notification) {
        LOG.debug("Got event of type :: {}", ObjectCreationNotification.class.getSimpleName());
        if (notification != null) {
            microwaveModelListener.creationNotification(acessor.getNodeId(), notification.getCounter(),
                    notification.getTimeStamp(), Helper.nnGetUniversalId(notification.getObjectIdRef()).getValue());
        }
    }

    @Override
    public void onObjectDeletionNotification(ObjectDeletionNotification notification) {
        LOG.debug("Got event of type :: {}", ObjectDeletionNotification.class.getSimpleName());
        if (notification != null) {
            microwaveModelListener.deletionNotification(acessor.getNodeId(), notification.getCounter(),
                    notification.getTimeStamp(), Helper.nnGetUniversalId(notification.getObjectIdRef()).getValue());
        }
    }

    @Override
    public void onAttributeValueChangedNotification(AttributeValueChangedNotification notification) {
        LOG.debug("Got event of type :: {}", AttributeValueChangedNotification.class.getSimpleName());
        EventlogEntity beventlogEntity = new EventlogBuilder().setNodeId(acessor.getNodeId().getValue())
                .setCounter(notification.getCounter()).setTimestamp(notification.getTimeStamp())
                .setObjectId(Helper.nnGetUniversalId(notification.getObjectIdRef()).getValue())
                .setAttributeName(notification.getAttributeName()).setNewValue(notification.getNewValue()).build();
        microwaveModelListener.eventNotification(beventlogEntity);
        if (notificationQueue.isPresent()) {
            notificationQueue.get().put(beventlogEntity);
        }
    }

    @Override
    public void onProblemNotification(ProblemNotification notification) {

        LOG.debug("Got event of type :: {}", ProblemNotification.class.getSimpleName());
        FaultlogEntity faultAlarm = new FaultlogBuilder().setObjectId(notification.getObjectIdRef().getValue())
                .setProblem(notification.getProblem()).setSourceType(SourceType.Netconf)
                .setTimestamp(notification.getTimeStamp()).setNodeId(this.acessor.getNodeId().getValue())
                .setSeverity(mapSeverity(notification.getSeverity())).setCounter(notification.getCounter()).build();
        // Send devicemanager specific notification for database and ODLUX
        faultService.faultNotification(faultAlarm);
        // Send model specific notification to WebSocketManager
        notificationService.sendNotification(notification, acessor.getNodeId(), ProblemNotification.QNAME,
                notification.getTimeStamp());
    }

    /*-----------------------------------------------------------------------------
     * Reading problems for specific interface pacs
     */

    /**
     * Read problems of specific interfaces
     *
     * @param uuId Universal Id String of the interface
     * @return number of alarms
     */
    private FaultData readTheFaultsOfMwAirInterfacePac(UniversalId interfacePacUuid, FaultData resultList) {

        final Class<MwAirInterfacePac> clazzPac = MwAirInterfacePac.class;

        LOG.debug("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                mountpointId, interfacePacUuid.getValue());

        // Step 2.2: construct data and the relative iid
        InstanceIdentifier<AirInterfaceCurrentProblems> mwAirInterfaceIID =
                InstanceIdentifier.builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(interfacePacUuid))
                        .child(AirInterfaceCurrentProblems.class).build();

        // Step 2.3: read to the config data store
        AirInterfaceCurrentProblems problems = genericTransactionUtils.readData(acessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, mwAirInterfaceIID);

        if (problems == null) {
            LOG.debug("DBRead Id {} no AirInterfaceCurrentProblems", interfacePacUuid);
        } else if (problems.getCurrentProblemList() == null) {
            LOG.debug("DBRead Id {} empty CurrentProblemList", interfacePacUuid);
        } else {
            for (AirInterfaceCurrentProblemTypeG problem : YangHelper
                    .getCollection(problems.nonnullCurrentProblemList())) {
                resultList.add(acessor.getNodeId(), problem.getSequenceNumber(), problem.getTimeStamp(),
                        interfacePacUuid.getValue(), problem.getProblemName(),
                        mapSeverity(problem.getProblemSeverity()));
            }
        }
        return resultList;
    }

    /**
     * Read problems of specific interfaces
     *
     * @param uuId Universal index of Interfacepac
     * @return number of alarms
     */
    private FaultData readTheFaultsOfMwEthernetContainerPac(UniversalId interfacePacUuid, FaultData resultList) {

        final Class<MwEthernetContainerPac> clazzPac = MwEthernetContainerPac.class;

        LOG.debug("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                mountpointId, interfacePacUuid.getValue());

        InstanceIdentifier<EthernetContainerCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(MwEthernetContainerPac.class, new MwEthernetContainerPacKey(interfacePacUuid))
                .child(EthernetContainerCurrentProblems.class).build();

        EthernetContainerCurrentProblems problems = genericTransactionUtils.readData(acessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
        if (problems == null) {
            LOG.debug("DBRead Id {} no EthernetContainerCurrentProblems", interfacePacUuid);
        } else if (problems.getCurrentProblemList() == null) {
            LOG.debug("DBRead Id {} empty CurrentProblemsList", interfacePacUuid);
        } else {
            for (ContainerCurrentProblemTypeG problem : YangHelper
                    .getCollection(problems.nonnullCurrentProblemList())) {
                resultList.add(acessor.getNodeId(), problem.getSequenceNumber(), problem.getTimeStamp(),
                        interfacePacUuid.getValue(), problem.getProblemName(),
                        mapSeverity(problem.getProblemSeverity()));
            }
        }
        return resultList;
    }

    /**
     * Read problems of specific interfaces
     *
     * @param uuId Universal index of Interfacepac
     * @return number of alarms
     */
    private FaultData readTheFaultsOfMwAirInterfaceDiversityPac(UniversalId interfacePacUuid, FaultData resultList) {

        final Class<MwAirInterfaceDiversityPac> clazzPac = MwAirInterfaceDiversityPac.class;
        final Class<AirInterfaceDiversityCurrentProblems> clazzProblems = AirInterfaceDiversityCurrentProblems.class;

        LOG.debug("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                mountpointId, interfacePacUuid.getValue());

        InstanceIdentifier<AirInterfaceDiversityCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(clazzPac, new MwAirInterfaceDiversityPacKey(interfacePacUuid)).child(clazzProblems).build();

        AirInterfaceDiversityCurrentProblems problems = genericTransactionUtils.readData(acessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
        if (problems == null) {
            LOG.debug("DBRead Id {} no AirInterfaceDiversityCurrentProblems", interfacePacUuid);
        } else if (problems.getCurrentProblemList() == null) {
            LOG.debug("DBRead Id {} empty CurrentProblemList", interfacePacUuid);
        } else {
            for (AirInterfaceDiversityCurrentProblemTypeG problem : YangHelper
                    .getCollection(problems.nonnullCurrentProblemList())) {
                resultList.add(acessor.getNodeId(), problem.getSequenceNumber(), problem.getTimeStamp(),
                        interfacePacUuid.getValue(), problem.getProblemName(),
                        mapSeverity(problem.getProblemSeverity()));
            }
        }
        return resultList;
    }

    /**
     * Read problems of specific interfaces
     *
     * @param uuId Universal index of Interfacepac
     * @return number of alarms
     */
    private FaultData readTheFaultsOfMwPureEthernetStructurePac(UniversalId interfacePacUuid, FaultData resultList) {

        final Class<MwPureEthernetStructurePac> clazzPac = MwPureEthernetStructurePac.class;
        final Class<PureEthernetStructureCurrentProblems> clazzProblems = PureEthernetStructureCurrentProblems.class;

        LOG.debug("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                mountpointId, interfacePacUuid.getValue());

        InstanceIdentifier<PureEthernetStructureCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(clazzPac, new MwPureEthernetStructurePacKey(interfacePacUuid)).child(clazzProblems).build();

        PureEthernetStructureCurrentProblems problems = genericTransactionUtils.readData(acessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
        if (problems == null) {
            LOG.debug("DBRead Id {} no PureEthernetStructureCurrentProblems", interfacePacUuid);
        } else if (problems.getCurrentProblemList() == null) {
            LOG.debug("DBRead Id {} empty CurrentProblemsList", interfacePacUuid);
        } else {
            for (StructureCurrentProblemTypeG problem : YangHelper
                    .getCollection(problems.nonnullCurrentProblemList())) {
                resultList.add(acessor.getNodeId(), problem.getSequenceNumber(), problem.getTimeStamp(),
                        interfacePacUuid.getValue(), problem.getProblemName(),
                        mapSeverity(problem.getProblemSeverity()));
            }
        }
        return resultList;
    }

    /**
     * Read problems of specific interfaces
     *
     * @param uuId Universal index of Interfacepac
     * @return number of alarms
     */
    private FaultData readTheFaultsOfMwHybridMwStructurePac(UniversalId interfacePacUuid, FaultData resultList) {

        final Class<MwHybridMwStructurePac> clazzPac = MwHybridMwStructurePac.class;
        final Class<HybridMwStructureCurrentProblems> clazzProblems = HybridMwStructureCurrentProblems.class;

        LOG.debug("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                mountpointId, interfacePacUuid.getValue());

        InstanceIdentifier<HybridMwStructureCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(clazzPac, new MwHybridMwStructurePacKey(interfacePacUuid)).child(clazzProblems).build();

        HybridMwStructureCurrentProblems problems = genericTransactionUtils.readData(acessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
        if (problems == null) {
            LOG.debug("DBRead Id {} no HybridMwStructureCurrentProblems", interfacePacUuid);
        } else if (problems.getCurrentProblemList() == null) {
            LOG.debug("DBRead Id {} empty CurrentProblemsList", interfacePacUuid);
        } else {
            for (StructureCurrentProblemTypeG problem : YangHelper
                    .getCollection(problems.nonnullCurrentProblemList())) {
                resultList.add(acessor.getNodeId(), problem.getSequenceNumber(), problem.getTimeStamp(),
                        interfacePacUuid.getValue(), problem.getProblemName(),
                        mapSeverity(problem.getProblemSeverity()));
            }
        }
        return resultList;
    }

    /**
     * Read problems of specific interfaces. TODO Goal for future implementation without usage of explicit new. Key is
     * generated by newInstance() function here to verify this approach.
     *
     * @param uuId Universal index of Interfacepac
     * @return number of alarms
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private FaultData readTheFaultsOfMwTdmContainerPac(UniversalId interfacePacUuid, FaultData resultList) {

        final Class<MwTdmContainerPac> clazzPac = MwTdmContainerPac.class;
        final Class<MwTdmContainerPacKey> clazzPacKey = MwTdmContainerPacKey.class;
        final Class<TdmContainerCurrentProblems> clazzProblems = TdmContainerCurrentProblems.class;

        LOG.debug("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                mountpointId, interfacePacUuid.getValue());

        try {
            // -- Specific part 1
            Constructor<MwTdmContainerPacKey> cons = clazzPacKey.getConstructor(UniversalId.class); // Avoid new()
            InstanceIdentifier<TdmContainerCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                    .builder(clazzPac, cons.newInstance(interfacePacUuid)).child(clazzProblems).build();

            // -- Specific part 2
            TdmContainerCurrentProblems problems = genericTransactionUtils.readData(acessor.getDataBroker(),
                    LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
            if (problems == null) {
                LOG.debug("DBRead Id {} no TdmContainerCurrentProblems", interfacePacUuid);
            } else {
                // -- Specific part 3
                for (ContainerCurrentProblemTypeG problem : YangHelper
                        .getCollection(problems.nonnullCurrentProblemList())) {
                    resultList.add(acessor.getNodeId(), problem.getSequenceNumber(), problem.getTimeStamp(),
                            interfacePacUuid.getValue(), problem.getProblemName(),
                            mapSeverity(problem.getProblemSeverity()));
                }
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            LOG.warn("Could not reade instance of MwTdmContainerPacKey: ", e);
        }
        return resultList;
    }

    /*-----------------------------------------------------------------------------
     * Performance related data
     */

    /**
     * Read and add performance data
     *
     * @param lp to read from
     * @param result Object to be filled with data
     * @return result
     * @throws InconsistentPMDataException
     */
    private @NonNull PerformanceDataLtp readAirInterfacePerformanceData(Lp lp, PerformanceDataLtp result)
            throws InconsistentPMDataException {

        LOG.debug("DBRead Get {} MWAirInterfacePac: {}", acessor.getNodeId(), lp.getUuid());
        // ----
        UniversalId mwAirInterfacePacuuId = lp.getUuid();
        // Step 2.1: construct data and the relative iid
        InstanceIdentifier<AirInterfaceConfiguration> mwAirInterfaceConfigurationIID =
                InstanceIdentifier.builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(mwAirInterfacePacuuId))
                        .child(AirInterfaceConfiguration.class).build();
        AirInterfaceConfiguration airConfiguration = acessor.getTransactionUtils().readData(acessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, mwAirInterfaceConfigurationIID);

        if (airConfiguration == null) {
            LOG.debug("DBRead MWAirInterfacePac Id {} no AirInterfaceConfiguration", mwAirInterfacePacuuId);

        } else {
            // Step 2.2: construct data and the relative iid
            InstanceIdentifier<AirInterfaceHistoricalPerformances> mwAirInterfaceHistoricalPerformanceIID =
                    InstanceIdentifier.builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(mwAirInterfacePacuuId))
                            .child(AirInterfaceHistoricalPerformances.class).build();

            // Step 2.3: read to the config data store
            AirInterfaceHistoricalPerformances airHistoricalPerformanceData = genericTransactionUtils.readData(
                    acessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL, mwAirInterfaceHistoricalPerformanceIID);

            if (airHistoricalPerformanceData == null) {
                LOG.debug("DBRead MWAirInterfacePac Id {} no AirInterfaceHistoricalPerformances",
                        mwAirInterfacePacuuId);
            } else {
                // org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170320.air._interface.historical.performances.g.HistoricalPerformanceDataList
                Collection<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.air._interface.historical.performances.g.HistoricalPerformanceDataList> airHistPMList =
                        YangHelper.getCollection(airHistoricalPerformanceData.nonnullHistoricalPerformanceDataList());
                LOG.debug("DBRead MWAirInterfacePac Id {} Records intermediate: {}", mwAirInterfacePacuuId,
                        airHistPMList.size());
                for (AirInterfaceHistoricalPerformanceTypeG pmRecord : airHistPMList) {
                    result.add(new PerformanceDataAirInterface181010Builder(acessor.getNodeId(), lp, pmRecord,
                            airConfiguration));
                }
            }
        }
        LOG.debug("DBRead MWAirInterfacePac Id {} Records result: {}", mwAirInterfacePacuuId, result.size());
        return result;
    }

    private @NonNull PerformanceDataLtp readEthernetContainerPerformanceData(Lp lp, PerformanceDataLtp result)
            throws InconsistentPMDataException {
        final String myName = "MWEthernetContainerPac";

        LOG.debug("DBRead Get {} : {}", mountpointId, myName, lp.getUuid());
        // ----
        UniversalId ethContainerPacuuId = lp.getUuid();
        // Step 2.2: construct data and the relative iid
        InstanceIdentifier<EthernetContainerHistoricalPerformances> ethContainerIID = InstanceIdentifier
                .builder(MwEthernetContainerPac.class, new MwEthernetContainerPacKey(ethContainerPacuuId))
                .child(EthernetContainerHistoricalPerformances.class).build();

        // Step 2.3: read to the config data store
        EthernetContainerHistoricalPerformances ethContainerHistoricalPerformanceData = acessor.getTransactionUtils()
                .readData(acessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL, ethContainerIID);

        if (ethContainerHistoricalPerformanceData == null) {
            LOG.debug("DBRead {} Id {} no HistoricalPerformances", myName, ethContainerPacuuId);
        } else {
            Collection<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.ethernet.container.historical.performances.g.HistoricalPerformanceDataList> airHistPMList =
                    YangHelper.getCollection(
                            ethContainerHistoricalPerformanceData.nonnullHistoricalPerformanceDataList());
            LOG.debug("DBRead {} Id {} Records intermediate: {}", myName, ethContainerPacuuId, airHistPMList.size());
            for (ContainerHistoricalPerformanceTypeG pmRecord : airHistPMList) {
                result.add(new PerformanceDataAirInterface181010Builder(acessor.getNodeId(), lp, pmRecord));
            }
        }
        LOG.debug("DBRead {} Id {} Records result: {}", myName, ethContainerPacuuId, result.size());
        return result;
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType mapSeverity(
            SeverityType severity) {

        Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType> res =
                Optional.ofNullable(org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType
                        .forName(severity.name()));
        return res.orElse(
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType.NonAlarmed);
    }



}
