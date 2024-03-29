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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.equipment;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.OnfInterfacePac;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.WrapperMicrowaveModelRev181010;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.CurrentProblemTypeG;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.EquipmentPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.EquipmentPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.equipment.pac.EquipmentCurrentProblems;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WrapperEquipmentPacRev170402 implements OnfInterfacePac {

    private static final Logger LOG = LoggerFactory.getLogger(WrapperEquipmentPacRev170402.class);
    public static final QName QNAME = EquipmentPac.QNAME;

    private final NetconfBindingAccessor acessor;


    public WrapperEquipmentPacRev170402(NetconfBindingAccessor acessor) {
        this.acessor = acessor;
    }

    private TransactionUtils getGenericTransactionUtils() {
        return acessor.getTransactionUtils();
    }

    /**
     * Read problems of specific interfaces. TODO Goal for future implementation without usage of explicit new. Key is
     * generated by newInstance() function here to verify this approach.
     *
     * @param interfacePacUuid Universal index of onf interface-pac
     * @param resultList list to add, or null for new list.
     * @return list of alarms
     */
    @Override
    public @NonNull FaultData readTheFaults(@NonNull UniversalId interfacePacUuid, @NonNull FaultData resultList) {

        final Class<EquipmentPac> clazzPac = EquipmentPac.class;
        final Class<EquipmentPacKey> clazzPacKey = EquipmentPacKey.class;
        final Class<EquipmentCurrentProblems> clazzProblems = EquipmentCurrentProblems.class;

        LOG.debug("DBRead Get problems for class {} from mountpoint {} for uuid {}", clazzPac.getSimpleName(),
                acessor.getNodeId(), interfacePacUuid.getValue());

        try {
            // -- Specific part 1
            Constructor<EquipmentPacKey> cons = clazzPacKey.getConstructor(UniversalId.class); // Avoid new()
            InstanceIdentifier<EquipmentCurrentProblems> interfaceIID = InstanceIdentifier
                    .builder(clazzPac, cons.newInstance(interfacePacUuid)).child(clazzProblems).build();

            // -- Specific part 2
            EquipmentCurrentProblems problems = getGenericTransactionUtils().readData(acessor.getDataBroker(),
                    LogicalDatastoreType.OPERATIONAL, interfaceIID);
            if (problems == null) {
                LOG.debug("DBRead Id {} no {} name {}", interfacePacUuid, clazzProblems, clazzProblems.getName());
            } else {
                // -- Specific part 3
                for (CurrentProblemTypeG problem : YangHelper.getCollection(problems.nonnullCurrentProblemList())) {
                    resultList.add(acessor.getNodeId(), problem.getSequenceNumber(), problem.getTimeStamp(),
                            interfacePacUuid.getValue(), problem.getProblemName(),
                            WrapperMicrowaveModelRev181010.mapSeverity(problem.getProblemSeverity()));
                }
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            LOG.warn("Could not reade instance of MwTdmContainerPacKey: ", e);
        }
        return resultList;
    }

}
