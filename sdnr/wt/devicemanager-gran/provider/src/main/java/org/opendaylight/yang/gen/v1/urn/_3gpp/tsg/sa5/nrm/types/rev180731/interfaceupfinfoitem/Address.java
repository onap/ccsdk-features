package org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.interfaceupfinfoitem;
import org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn._3gpp.tsg.sa5.nrm.types.rev180731.InterfaceUpfInfoItem;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.common.QName;

/**
 *
 * <p>
 * This class represents the following YANG schema fragment defined in module <b>_3gpp-common-yang-types</b>
 * <pre>
 * choice address {
 *   case ipv4Address {
 *     leaf ipv4Address {
 *       type inet:ipv4-address;
 *     }
 *   }
 *   case ipv6Address {
 *     leaf ipv6Address {
 *       type inet:ipv6-address;
 *     }
 *   }
 *   case ipv6Prefix {
 *     leaf ipv6Prefix {
 *       type inet:ipv6-prefix;
 *     }
 *   }
 *   case endpointFqdn {
 *     leaf endpointFqdn {
 *       type inet:domain-name;
 *     }
 *   }
 * }
 * </pre>The schema path to identify an instance is
 * <i>_3gpp-common-yang-types/InterfaceUpfInfoItem/address</i>
 *
 */
public interface Address
    extends
    ChoiceIn<InterfaceUpfInfoItem>
{



    public static final QName QNAME = $YangModuleInfoImpl.qnameOf("address");


}

