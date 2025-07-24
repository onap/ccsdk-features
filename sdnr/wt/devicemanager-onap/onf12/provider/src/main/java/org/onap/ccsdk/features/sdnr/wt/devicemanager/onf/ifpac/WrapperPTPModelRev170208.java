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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac;

import java.util.Collection;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.InstanceList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.InstanceListKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.instance.list.PortDsList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.port.ds.entry.PortIdentity;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reading PTP specific information from networkelement and creating log-trace output.
 *
 * @author herbert
 */
public class WrapperPTPModelRev170208 {

    private static final Logger LOG = LoggerFactory.getLogger(WrapperPTPModelRev170208.class);

    protected static final InstanceIdentifier<InstanceList> PTPINSTANCES_IID =
            InstanceIdentifier.builder(InstanceList.class, new InstanceListKey(Uint16.valueOf(1))).build();

    /**
     * Query synchronization information out of NE
     */

    public static void initSynchronizationExtension(NetconfBindingAccessor acessor) {

        String mountPointNodeName = acessor.getNodeId().getValue();
        Capabilities capabilities = acessor.getCapabilites();
        try {
            if (!capabilities.isSupportingNamespaceAndRevision(InstanceList.QNAME)) {
                LOG.debug("Mountpoint {} does not support PTP", mountPointNodeName);
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append("NE ");
                sb.append(mountPointNodeName);
                sb.append(" does support synchronisation.\n");
                InstanceList ptpInstance = readPTPClockInstances(acessor);
                if (ptpInstance != null) {
                    Collection<PortDsList> dsList = YangHelper.getCollection(ptpInstance.getPortDsList());
                    if (dsList != null) {
                        int t = 0;
                        for (PortDsList portDs : dsList) {
                            PortIdentity portId = portDs.getPortIdentity();
                            if (portId != null) {
                                sb.append("Port[");
                                sb.append(portId.getPortNumber());
                                sb.append("]{ ClockId: ");
                                sb.append(portId.getClockIdentity());
                                sb.append(", Portstate: ");
                                sb.append(portDs.getPortState());
                                sb.append("}, ");
                            } else {
                                sb.append("Incomplete port #" + t + ", ");
                            }
                            t++;
                        }
                    } else {
                        sb.append("dsList contains null");
                    }
                } else {
                    sb.append("ptpInstance equals null");
                }
                LOG.trace(sb.toString());
            }
        } catch (Exception e) {
            LOG.info("Inconsistent synchronisation structure: " + e.getMessage());
        }
    }

    @Nullable
    private static InstanceList readPTPClockInstances(NetconfBindingAccessor acessor) {
        return acessor.getTransactionUtils().readData(acessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                PTPINSTANCES_IID);
    }

}
