/*******************************************************************************
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave;



import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.pm.PerformanceDataAirInterface170324Builder;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.notifications.NotificationWorker;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.util.ONFLayerProtocolName;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.PerformanceDataLtp;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.Lp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.AirInterfaceCurrentProblemTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.AirInterfaceDiversityCurrentProblemTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.AirInterfaceHistoricalPerformanceTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.AttributeValueChangedNotification;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.ContainerCurrentProblemTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.ContainerHistoricalPerformanceTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MicrowaveModelListener;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwAirInterfaceDiversityPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwAirInterfaceDiversityPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwAirInterfacePac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwAirInterfacePacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwEthernetContainerPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwEthernetContainerPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwHybridMwStructurePac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwHybridMwStructurePacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwPureEthernetStructurePac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwPureEthernetStructurePacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwTdmContainerPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.MwTdmContainerPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.ObjectCreationNotification;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.ObjectDeletionNotification;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.ProblemNotification;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.SeverityType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.StructureCurrentProblemTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.air._interface.diversity.pac.AirInterfaceDiversityCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.air._interface.pac.AirInterfaceConfiguration;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.air._interface.pac.AirInterfaceCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.air._interface.pac.AirInterfaceHistoricalPerformances;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.ethernet.container.pac.EthernetContainerCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.ethernet.container.pac.EthernetContainerHistoricalPerformances;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.hybrid.mw.structure.pac.HybridMwStructureCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.pure.ethernet.structure.pac.PureEthernetStructureCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.tdm.container.pac.TdmContainerCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EventlogEntity;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WrapperMicrowaveModelRev170324 implements OnfMicrowaveModel, MicrowaveModelListener {

    private static final Logger LOG = LoggerFactory.getLogger(WrapperMicrowaveModelRev170324.class);

    public static final QName QNAME = MwAirInterfacePac.QNAME;


    //private NetworkElementCoreData coreData;
    private final NotificationService microwaveModelListener;
    private final NetconfAccessor acessor;
    private final TransactionUtils genericTransactionUtils;
    private final FaultService faultService;

    private Optional<NotificationWorker<EventlogEntity>> notificationQueue;

    /**
     * @param acessor to access device
     */
    public WrapperMicrowaveModelRev170324(@NonNull NetconfAccessor acessor, @NonNull DeviceManagerServiceProvider serviceProvider) {
        this.acessor = acessor;
        this.genericTransactionUtils = acessor.getTransactionUtils();
        this.microwaveModelListener = serviceProvider.getNotificationService();
        this.faultService = serviceProvider.getFaultService();
        this.notificationQueue = Optional.empty();
    }

    /*-----------------------------------------------------------------------------
     * Setter/Getter
     */

    @SuppressWarnings("unchecked")
    @Override
    public <T extends NotificationListener> T getNotificationListener() {
        return (T)this;
    }

    @Override
    public void setNotificationQueue(NotificationWorker<EventlogEntity> notificationQueue) {
        this.notificationQueue = Optional.of(notificationQueue);
    }

    /*-----------------------------------------------------------------------------
     * Interface functions
     */

    @Override
    public void readTheFaultsOfMicrowaveModel(ONFLayerProtocolName lpName, Class<?> lpClass, UniversalId uuid,
            FaultData resultList) {

        switch (lpName) {
        case MWAirInterface:
            readTheFaultsOfMwAirInterfacePac(uuid, resultList);
            break;

        case EthernetContainer12:
            readTheFaultsOfMwEthernetContainerPac(uuid, resultList);
            break;

        case TDMContainer:
            readTheFaultsOfMwTdmContainerPac(uuid, resultList);
            break;

        case Structure:
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
        case Ethernet:
            // No alarms supported
            break;
        case EthernetContainer10:
        default:
            LOG.warn("Unassigned or not expected lp in model {}", lpName);
        }
    }

    @Override
    public @NonNull PerformanceDataLtp getLtpHistoricalPerformanceData(@NonNull ONFLayerProtocolName lpName, @NonNull Lp lp) {
        PerformanceDataLtp res = new PerformanceDataLtp();
        res = readAirInterfacePerformanceData(lp, res);
        res = readEthernetContainerPerformanceData(lp, res);
        return res;
    }

//    @Override
//    public @NonNull List<? extends OtnHistoryDataG> readTheHistoricalPerformanceData(@NonNull ONFLayerProtocolName lpName, @NonNull Lp lp) {
//        switch (lpName) {
//        case MWAirInterface:
//            return readTheHistoricalPerformanceDataOfMwAirInterfacePac(lp);
//
//        case EthernetContainer12:
//            return readTheHistoricalPerformanceDataOfEthernetContainer(lp);
//
//        case EthernetContainer10:
//        case EthernetPhysical:
//        case Ethernet:
//        case TDMContainer:
//        case Structure:
//        case Unknown:
//            LOG.debug("Do not read HistoricalPM data for {} {}", lpName, getUuid(lp));
//            break;
//        }
//        return new ArrayList<>();
//    }
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
        LOG.info("Found QName {} mapped to {}", String.valueOf(qName), String.valueOf(res));
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
    public void onObjectDeletionNotification( ObjectDeletionNotification notification) {
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

        faultService.faultNotification(acessor.getNodeId(), notification.getCounter(),
                notification.getTimeStamp(), Helper.nnGetUniversalId(notification.getObjectIdRef()).getValue(),
                notification.getProblem(), mapSeverity(notification.getSeverity()));
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
        // final Class<MwAirInterfacePacKey> clazzPacKey = MwAirInterfacePacKey.class;
        // final Class<AirInterfaceCurrentProblems> clazzProblems =
        // AirInterfaceCurrentProblems.class;
        // final Class<AirInterfaceCurrentProblemTypeG> clazzProblem =
        // AirInterfaceCurrentProblemTypeG.class;

        String mountpointId = acessor.getNodeId().getValue();
        LOG.info("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                mountpointId, interfacePacUuid.getValue());

        // Step 2.2: construct data and the relative iid
        InstanceIdentifier<AirInterfaceCurrentProblems> mwAirInterfaceIID = InstanceIdentifier
                .builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(interfacePacUuid))
                .child(AirInterfaceCurrentProblems.class).build();

        // Step 2.3: read to the config data store
        AirInterfaceCurrentProblems problems = genericTransactionUtils.readData(acessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, mwAirInterfaceIID);

        if (problems == null) {
            LOG.debug("DBRead Id {} no AirInterfaceCurrentProblems", interfacePacUuid);
        } else {
            for (AirInterfaceCurrentProblemTypeG problem : problems.nonnullCurrentProblemList()) {
                resultList.add(acessor.getNodeId(), problem.getSequenceNumber(), problem.getTimeStamp(),
                        interfacePacUuid.getValue(), problem.getProblemName(), mapSeverity(problem.getProblemSeverity()));
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
        // final Class<MwEthernetContainerPacKey> clazzPacKey =
        // MwEthernetContainerPacKey.class;
        // final Class<EthernetContainerCurrentProblems> clazzProblems =
        // EthernetContainerCurrentProblems.class;
        // final Class<ContainerCurrentProblemTypeG> clazzProblem =
        // ContainerCurrentProblemTypeG.class;

        String mountpointId = acessor.getNodeId().getValue();
        LOG.info("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                mountpointId, interfacePacUuid.getValue());

        InstanceIdentifier<EthernetContainerCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(MwEthernetContainerPac.class, new MwEthernetContainerPacKey(interfacePacUuid))
                .child(EthernetContainerCurrentProblems.class).build();

        EthernetContainerCurrentProblems problems = genericTransactionUtils.readData(acessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
        if (problems == null) {
            LOG.debug("DBRead Id {} no EthernetContainerCurrentProblems", interfacePacUuid);
        } else {
            for (ContainerCurrentProblemTypeG problem : problems.nonnullCurrentProblemList()) {
                resultList.add(acessor.getNodeId(), problem.getSequenceNumber(), problem.getTimeStamp(),
                        interfacePacUuid.getValue(), problem.getProblemName(), mapSeverity(problem.getProblemSeverity()));
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
    private FaultData readTheFaultsOfMwAirInterfaceDiversityPac(UniversalId interfacePacUuid,
            FaultData resultList) {

        final Class<MwAirInterfaceDiversityPac> clazzPac = MwAirInterfaceDiversityPac.class;
        // final Class<MwAirInterfaceDiversityPacKey> clazzPacKey =
        // MwAirInterfaceDiversityPacKey.class;
        final Class<AirInterfaceDiversityCurrentProblems> clazzProblems = AirInterfaceDiversityCurrentProblems.class;
        // final Class<AirInterfaceDiversityCurrentProblemTypeG> clazzProblem =
        // AirInterfaceDiversityCurrentProblemTypeG.class;

        String mountpointId = acessor.getNodeId().getValue();
        LOG.info("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                mountpointId, interfacePacUuid.getValue());

        InstanceIdentifier<AirInterfaceDiversityCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(clazzPac, new MwAirInterfaceDiversityPacKey(interfacePacUuid)).child(clazzProblems).build();

        AirInterfaceDiversityCurrentProblems problems = genericTransactionUtils.readData(acessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
        if (problems == null) {
            LOG.debug("DBRead Id {} no AirInterfaceDiversityCurrentProblems", interfacePacUuid);
        } else {
            for (AirInterfaceDiversityCurrentProblemTypeG problem : problems.nonnullCurrentProblemList()) {
                resultList.add(acessor.getNodeId(), problem.getSequenceNumber(), problem.getTimeStamp(),
                        interfacePacUuid.getValue(), problem.getProblemName(), mapSeverity(problem.getProblemSeverity()));
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
    private FaultData readTheFaultsOfMwPureEthernetStructurePac(UniversalId interfacePacUuid,
            FaultData resultList) {

        final Class<MwPureEthernetStructurePac> clazzPac = MwPureEthernetStructurePac.class;
        // final Class<MwPureEthernetStructurePacKey> clazzPacKey =
        // MwPureEthernetStructurePacKey.class;
        final Class<PureEthernetStructureCurrentProblems> clazzProblems = PureEthernetStructureCurrentProblems.class;
        // final Class<StructureCurrentProblemTypeG> clazzProblem =
        // StructureCurrentProblemTypeG.class;

        String mountpointId = acessor.getNodeId().getValue();
        LOG.info("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                mountpointId, interfacePacUuid.getValue());

        InstanceIdentifier<PureEthernetStructureCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(clazzPac, new MwPureEthernetStructurePacKey(interfacePacUuid)).child(clazzProblems).build();

        PureEthernetStructureCurrentProblems problems = genericTransactionUtils.readData(acessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
        if (problems == null) {
            LOG.debug("DBRead Id {} no PureEthernetStructureCurrentProblems", interfacePacUuid);
        } else {
            for (StructureCurrentProblemTypeG problem : problems.nonnullCurrentProblemList()) {
                resultList.add(acessor.getNodeId(), problem.getSequenceNumber(), problem.getTimeStamp(),
                        interfacePacUuid.getValue(), problem.getProblemName(), mapSeverity(problem.getProblemSeverity()));
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
        // final Class<MwHybridMwStructurePacKey> clazzPacKey =
        // MwHybridMwStructurePacKey.class;
        final Class<HybridMwStructureCurrentProblems> clazzProblems = HybridMwStructureCurrentProblems.class;
        // final Class<HybridMwStructureCurrentProblemsG> clazzProblem =
        // HybridMwStructureCurrentProblemsG.class;

        String mountpointId = acessor.getNodeId().getValue();
        LOG.info("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                mountpointId, interfacePacUuid.getValue());

        InstanceIdentifier<HybridMwStructureCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(clazzPac, new MwHybridMwStructurePacKey(interfacePacUuid)).child(clazzProblems).build();

        HybridMwStructureCurrentProblems problems = genericTransactionUtils.readData(acessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, mwEthInterfaceIID);
        if (problems == null) {
            LOG.debug("DBRead Id {} no HybridMwStructureCurrentProblems", interfacePacUuid);
        } else {
            for (StructureCurrentProblemTypeG problem : problems.nonnullCurrentProblemList()) {
                resultList.add(acessor.getNodeId(), problem.getSequenceNumber(), problem.getTimeStamp(),
                        interfacePacUuid.getValue(), problem.getProblemName(), mapSeverity(problem.getProblemSeverity()));
            }
        }
        return resultList;
    }

    /**
     * Read problems of specific interfaces. TODO Goal for future implementation
     * without usage of explicit new. Key is generated by newInstance() function
     * here to verify this approach.
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
        // final Class<ContainerCurrentProblemTypeG> clazzProblem =
        // ContainerCurrentProblemTypeG.class;

        String mountpointId = acessor.getNodeId().getValue();
        LOG.info("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
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
                for (ContainerCurrentProblemTypeG problem : problems.nonnullCurrentProblemList()) {
                    resultList.add(acessor.getNodeId(), problem.getSequenceNumber(), problem.getTimeStamp(),
                            interfacePacUuid.getValue(), problem.getProblemName(), mapSeverity(problem.getProblemSeverity()));
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

//    /**
//     * PM MwAirInterfacePac
//     *
//     * @param lp
//     * @return
//     */
//    private @NonNull List<ExtendedAirInterfaceHistoricalPerformanceType12> readTheHistoricalPerformanceDataOfMwAirInterfacePac(
//            Lp lp) {
//
//        List<ExtendedAirInterfaceHistoricalPerformanceType12> resultList = new ArrayList<>();
//        LOG.debug("DBRead Get {} MWAirInterfacePac: {}", coreData.getMountpoint(), lp.getUuid());
//        // ----
//        UniversalId mwAirInterfacePacuuId = lp.getUuid();
//        // Step 2.1: construct data and the relative iid
//        InstanceIdentifier<AirInterfaceConfiguration> mwAirInterfaceConfigurationIID = InstanceIdentifier
//                .builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(mwAirInterfacePacuuId))
//                .child(AirInterfaceConfiguration.class).build();
//        AirInterfaceConfiguration airConfiguration = genericTransactionUtils.readData(coreData.getDataBroker(),
//                LogicalDatastoreType.OPERATIONAL, mwAirInterfaceConfigurationIID);
//
//        if (airConfiguration == null) {
//            LOG.debug("DBRead MWAirInterfacePac Id {} no AirInterfaceConfiguration", mwAirInterfacePacuuId);
//
//        } else {
//            // Step 2.2: construct data and the relative iid
//            InstanceIdentifier<AirInterfaceHistoricalPerformances> mwAirInterfaceHistoricalPerformanceIID = InstanceIdentifier
//                    .builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(mwAirInterfacePacuuId))
//                    .child(AirInterfaceHistoricalPerformances.class).build();
//
//            // Step 2.3: read to the config data store
//            AirInterfaceHistoricalPerformances airHistoricalPerformanceData = genericTransactionUtils.readData(
//                    coreData.getDataBroker(), LogicalDatastoreType.OPERATIONAL, mwAirInterfaceHistoricalPerformanceIID);
//
//            if (airHistoricalPerformanceData == null) {
//                LOG.debug("DBRead MWAirInterfacePac Id {} no AirInterfaceHistoricalPerformances",
//                        mwAirInterfacePacuuId);
//            } else {
//                // org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170320.air._interface.historical.performances.g.HistoricalPerformanceDataList
//                List<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.air._interface.historical.performances.g.HistoricalPerformanceDataList> airHistPMList = airHistoricalPerformanceData
//                        .nonnullHistoricalPerformanceDataList();
//                LOG.debug("DBRead MWAirInterfacePac Id {} Records intermediate: {}", mwAirInterfacePacuuId,
//                        airHistPMList.size());
//                for (AirInterfaceHistoricalPerformanceTypeG pmRecord : airHistoricalPerformanceData
//                        .nonnullHistoricalPerformanceDataList()) {
//                    resultList.add(new ExtendedAirInterfaceHistoricalPerformanceType12(pmRecord, airConfiguration));
//                }
//            }
//        }
//        LOG.debug("DBRead MWAirInterfacePac Id {} Records result: {}", mwAirInterfacePacuuId, resultList.size());
//        return resultList;
//    }

    /**
     * Read and add performance data
     * @param lp to read from
     * @param result Object to be filled with data
     * @return result
     */
    private @NonNull PerformanceDataLtp readAirInterfacePerformanceData(Lp lp, PerformanceDataLtp result) {

        LOG.debug("DBRead Get {} MWAirInterfacePac: {}", acessor.getNodeId(), lp.getUuid());
        // ----
        UniversalId mwAirInterfacePacuuId = lp.getUuid();
        // Step 2.1: construct data and the relative iid
        InstanceIdentifier<AirInterfaceConfiguration> mwAirInterfaceConfigurationIID = InstanceIdentifier
                .builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(mwAirInterfacePacuuId))
                .child(AirInterfaceConfiguration.class).build();
        AirInterfaceConfiguration airConfiguration = acessor.getTransactionUtils().readData(acessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, mwAirInterfaceConfigurationIID);

        if (airConfiguration == null) {
            LOG.debug("DBRead MWAirInterfacePac Id {} no AirInterfaceConfiguration", mwAirInterfacePacuuId);

        } else {
            // Step 2.2: construct data and the relative iid
            InstanceIdentifier<AirInterfaceHistoricalPerformances> mwAirInterfaceHistoricalPerformanceIID = InstanceIdentifier
                    .builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(mwAirInterfacePacuuId))
                    .child(AirInterfaceHistoricalPerformances.class).build();

            // Step 2.3: read to the config data store
            AirInterfaceHistoricalPerformances airHistoricalPerformanceData = genericTransactionUtils.readData(
                    acessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL, mwAirInterfaceHistoricalPerformanceIID);

            if (airHistoricalPerformanceData == null) {
                LOG.debug("DBRead MWAirInterfacePac Id {} no AirInterfaceHistoricalPerformances",
                        mwAirInterfacePacuuId);
            } else {
                // org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170320.air._interface.historical.performances.g.HistoricalPerformanceDataList
                List<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.air._interface.historical.performances.g.HistoricalPerformanceDataList> airHistPMList = airHistoricalPerformanceData
                        .nonnullHistoricalPerformanceDataList();
                LOG.debug("DBRead MWAirInterfacePac Id {} Records intermediate: {}", mwAirInterfacePacuuId,
                        airHistPMList.size());
                for (AirInterfaceHistoricalPerformanceTypeG pmRecord : airHistoricalPerformanceData
                        .nonnullHistoricalPerformanceDataList()) {
                    result.add(new PerformanceDataAirInterface170324Builder(acessor.getNodeId(), lp, pmRecord, airConfiguration));
                }
            }
        }
        LOG.debug("DBRead MWAirInterfacePac Id {} Records result: {}", mwAirInterfacePacuuId, result.size());
        return result;
    }

//    private @NonNull List<ContainerHistoricalPerformanceTypeG> readTheHistoricalPerformanceDataOfEthernetContainer(Lp lp) {
//
//        final String myName = "MWEthernetContainerPac";
//
//        List<ContainerHistoricalPerformanceTypeG> resultList = new ArrayList<>();
//        LOG.debug("DBRead Get {} : {}", coreData.getMountpoint(), myName, lp.getUuid());
//        // ----
//        UniversalId ethContainerPacuuId = lp.getUuid();
//        // Step 2.2: construct data and the relative iid
//        InstanceIdentifier<EthernetContainerHistoricalPerformances> ethContainerIID = InstanceIdentifier
//                .builder(MwEthernetContainerPac.class, new MwEthernetContainerPacKey(ethContainerPacuuId))
//                .child(EthernetContainerHistoricalPerformances.class).build();
//
//        // Step 2.3: read to the config data store
//        EthernetContainerHistoricalPerformances ethContainerHistoricalPerformanceData = genericTransactionUtils
//                .readData(coreData.getDataBroker(), LogicalDatastoreType.OPERATIONAL, ethContainerIID);
//
//        if (ethContainerHistoricalPerformanceData == null) {
//            LOG.debug("DBRead {} Id {} no HistoricalPerformances", myName, ethContainerPacuuId);
//        } else {
//            // import
//            // org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170320.ethernet.container.historical.performances.g.HistoricalPerformanceDataList
//            // org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.ethernet.container.historical.performances.g.HistoricalPerformanceDataList
//            List<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.ethernet.container.historical.performances.g.HistoricalPerformanceDataList> airHistPMList = ethContainerHistoricalPerformanceData
//                    .nonnullHistoricalPerformanceDataList();
//            LOG.debug("DBRead {} Id {} Records intermediate: {}", myName, ethContainerPacuuId, airHistPMList.size());
//            for (ContainerHistoricalPerformanceTypeG pmRecord : airHistPMList) {
//                resultList.add(pmRecord);
//            }
//        }
//        LOG.debug("DBRead {} Id {} Records result: {}", myName, ethContainerPacuuId, resultList.size());
//        return resultList;
//    }

    private @NonNull PerformanceDataLtp readEthernetContainerPerformanceData(Lp lp, PerformanceDataLtp result) {
        final String myName = "MWEthernetContainerPac";

        String mountpointId = acessor.getNodeId().getValue();

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
            List<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.ethernet.container.historical.performances.g.HistoricalPerformanceDataList> airHistPMList = ethContainerHistoricalPerformanceData
                    .nonnullHistoricalPerformanceDataList();
            LOG.debug("DBRead {} Id {} Records intermediate: {}", myName, ethContainerPacuuId, airHistPMList.size());
            for (ContainerHistoricalPerformanceTypeG pmRecord : airHistPMList) {
                result.add(new PerformanceDataAirInterface170324Builder(acessor.getNodeId(), lp, pmRecord));
            }
        }
        LOG.debug("DBRead {} Id {} Records result: {}", myName, ethContainerPacuuId, result.size());
        return result;
    }

//    private static String getUuid(Lp lp) {
//        UniversalId uuid = lp.getUuid();
//        return uuid != null ? uuid.getValue() : null;
//    }

    private org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType mapSeverity( SeverityType severity) {

        Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType> res =
                org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType.forName(severity.name());
        return res.orElse(org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SeverityType.NonAlarmed);
    }




}
