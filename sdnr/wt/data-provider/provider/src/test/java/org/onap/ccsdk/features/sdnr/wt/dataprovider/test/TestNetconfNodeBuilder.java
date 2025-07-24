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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.credentials.Credentials;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.credentials.credentials.LoginPwUnencrypted;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.credentials.credentials.LoginPwUnencryptedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.credentials.credentials.login.pw.unencrypted.LoginPasswordUnencryptedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.netconf.node.augment.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.netconf.node.augment.NetconfNodeBuilder;


@SuppressWarnings("deprecation")
public class TestNetconfNodeBuilder {

    @Test
    public void test() {

        NetconfNodeBuilder netconfNodeBuilder = new NetconfNodeBuilder();

        netconfNodeBuilder.setCredentials(new LoginPwUnencryptedBuilder().setLoginPasswordUnencrypted(
                new LoginPasswordUnencryptedBuilder().setUsername("myTestUsername").setPassword("myTestPassword")
                        .build()).build());

        NetconfNode netconfNode = netconfNodeBuilder.build();
        System.out.println(netconfNode);

        Credentials credentials = netconfNode.getCredentials();
        System.out.println("Class: " + credentials.getClass() + "\nContent: " + credentials);

        if (credentials instanceof LoginPwUnencrypted loginPwUnencrypted) {
            assertEquals("myTestUsername", loginPwUnencrypted.getLoginPasswordUnencrypted().getUsername());
            assertEquals("myTestPassword", loginPwUnencrypted.getLoginPasswordUnencrypted().getPassword());
        } else {
            fail("Not expected class");
        }
    }

}
