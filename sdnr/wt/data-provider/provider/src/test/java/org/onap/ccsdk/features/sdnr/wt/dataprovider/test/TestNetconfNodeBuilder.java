/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.credentials.Credentials;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.credentials.credentials.LoginPassword;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.credentials.credentials.LoginPasswordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNodeBuilder;

@SuppressWarnings("deprecation")
public class TestNetconfNodeBuilder {

    @Test
    public void test() {

        NetconfNodeBuilder netconfNodeBuilder = new NetconfNodeBuilder();

        LoginPasswordBuilder loginPasswordBuilder = new LoginPasswordBuilder();
        loginPasswordBuilder.setUsername("myTestUsername");
        loginPasswordBuilder.setPassword("myTestPassword");
        netconfNodeBuilder.setCredentials(loginPasswordBuilder.build());

        NetconfNode netconfNode = netconfNodeBuilder.build();
        System.out.println(netconfNode);

        Credentials credentials = netconfNode.getCredentials();
        System.out.println("Class: " + credentials.getClass() + "\nContent: " + credentials);

        if (credentials instanceof LoginPassword) {
            LoginPassword loginPassword = (LoginPassword) credentials;
            System.out.println("User: " + loginPassword.getUsername() + " Password" + loginPassword.getPassword());
        } else {
            System.out.println("Not expected class");
        }
    }

}
