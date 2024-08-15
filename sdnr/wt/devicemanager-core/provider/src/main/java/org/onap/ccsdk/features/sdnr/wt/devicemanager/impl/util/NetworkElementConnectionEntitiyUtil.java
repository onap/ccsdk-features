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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util;

import java.util.HashSet;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.InternalConnectionStatus;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.credentials.Credentials;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.credentials.credentials.KeyAuth;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.credentials.credentials.LoginPw;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.credentials.credentials.LoginPwUnencrypted;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.credentials.credentials.login.pw.LoginPassword;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.network.element.connection.entity.NodeDetailsBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class NetworkElementConnectionEntitiyUtil {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkElementConnectionEntitiyUtil.class);

    /**
     * Update devicetype and let all other field emptys
     *
     * @param deviceType that should be updated
     * @return NetworkElementConnectionEntity with related parameter
     */
    public static NetworkElementConnectionEntity getNetworkConnectionDeviceTpe(NetworkElementDeviceType deviceType) {
        NetworkElementConnectionBuilder eb = new NetworkElementConnectionBuilder();
        eb.setDeviceType(deviceType);
        return eb.build();
    }

    /**
     * Provide device specific data
     *
     * @param nodeId mountpoint id
     * @param nNode data
     * @return NetworkElementConnectionEntity specific information
     */
    public static NetworkElementConnectionEntity getNetworkConnection(String nodeId, @Nonnull NetconfNode nNode) {
        return getNetworkConnection(nodeId, nNode, Optional.empty());
    }

    public static NetworkElementConnectionEntity getNetworkConnection(String nodeId, @Nonnull NetconfNode nNode,
            Optional<NetconfNode> configNetconfNode) {

        NetworkElementConnectionBuilder eb = new NetworkElementConnectionBuilder();
        // -- basics
        eb.setId(nodeId).setNodeId(nodeId).setDeviceType(NetworkElementDeviceType.Unknown).setIsRequired(false);

        // -- connection status
        ConnectionLogStatus status = InternalConnectionStatus.statusFromNodeStatus(nNode.getConnectionStatus());
        eb.setStatus(status);

        // -- capabilites
        Capabilities availableCapabilities = Capabilities.getAvailableCapabilities(nNode);
        Capabilities unAvailableCapabilities = Capabilities.getUnavailableCapabilities(nNode);

        NodeDetailsBuilder nodeDetails =
                new NodeDetailsBuilder().setAvailableCapabilities(new HashSet<>(availableCapabilities.getCapabilities()))
                        .setUnavailableCapabilities(new HashSet<>(unAvailableCapabilities.getCapabilities()));
        eb.setNodeDetails(nodeDetails.build());
        // -- host information
        Host host = nNode.getHost();
        PortNumber portNumber = nNode.getPort();
        if (host != null && portNumber != null) {
            eb.setHost(host.stringValue()).setPort(Uint32.valueOf(portNumber.getValue()));
        }

        Credentials credentials =
                configNetconfNode.isPresent() ? configNetconfNode.get().getCredentials() : nNode.getCredentials();
        if (credentials instanceof LoginPassword) {
            LoginPassword loginPassword = (LoginPassword) credentials;
            eb.setUsername(loginPassword.getUsername()).setPassword(loginPassword.getPassword())
                    .setMountMethod(LoginPassword.class.getSimpleName());
        } else if (credentials instanceof LoginPwUnencrypted) {
            LoginPwUnencrypted loginPassword = (LoginPwUnencrypted) credentials;
            eb.setUsername(loginPassword.getLoginPasswordUnencrypted().getUsername())
                    .setPassword(loginPassword.getLoginPasswordUnencrypted().getPassword())
                    .setMountMethod(LoginPwUnencrypted.class.getSimpleName());
        } else if (credentials instanceof LoginPw) {
            LoginPw loginPassword = (LoginPw) credentials;
            eb.setUsername(loginPassword.getLoginPassword().getUsername())
                    .setPassword(loginPassword.getLoginPassword().getPassword())
                    .setMountMethod(LoginPw.class.getSimpleName());
        } else if (credentials instanceof KeyAuth) {
            KeyAuth keyAuth = (KeyAuth) credentials;
            eb.setUsername(keyAuth.getKeyBased().getUsername()).setTlsKey(keyAuth.getKeyBased().getKeyId())
                    .setMountMethod(KeyAuth.class.getSimpleName());
        }
        LOG.debug("mount-method: {} ({})", eb.getMountMethod(),
                credentials == null ? null : credentials.getClass().getName());
        // Default value. Specific value (if any) is set in the specific devicemanagers
        eb.setCoreModelCapability("Unsupported");
        return eb.build();
    }
}
