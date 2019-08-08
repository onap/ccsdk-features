package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.wrapperc;

import java.util.List;

import javax.annotation.Nullable;

import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.container.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.util.GenericTransactionUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.InstanceList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.InstanceListKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.instance.list.PortDsList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.port.ds.entry.PortIdentity;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reading PTP specific information from networkelement and creating log-trace output.
 *
 * @author herbert
 */
@SuppressWarnings("deprecation")
public class WrapperPTPModelRev170208 {

    private static final Logger LOG = LoggerFactory.getLogger(WrapperPTPModelRev170208.class);

    protected static final InstanceIdentifier<InstanceList> PTPINSTANCES_IID = InstanceIdentifier
            .builder(InstanceList.class, new InstanceListKey(1)).build();

    /**
     * Query synchronization information out of NE
     */

    public static void initSynchronizationExtension(String mountPointNodeName, DataBroker netconfNodeDataBroker,
            Capabilities capabilities) {
        try {
            if (!capabilities.isSupportingNamespaceAndRevision(InstanceList.QNAME)) {
                LOG.debug("Mountpoint {} does not support PTP", mountPointNodeName);
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append("NE ");
                sb.append(mountPointNodeName);
                sb.append(" does support synchronisation.\n");
                InstanceList ptpInstance = readPTPClockInstances(netconfNodeDataBroker);
                if (ptpInstance != null) {
                    List<PortDsList> dsList = ptpInstance.getPortDsList();
                    if (dsList != null) {
                        int t = 0;
                        for (PortDsList portDs : ptpInstance.getPortDsList()) {
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
    private static InstanceList readPTPClockInstances(DataBroker netconfNodeDataBroker) {
        return GenericTransactionUtils.readData(netconfNodeDataBroker, LogicalDatastoreType.OPERATIONAL,
                PTPINSTANCES_IID);
    }

}
