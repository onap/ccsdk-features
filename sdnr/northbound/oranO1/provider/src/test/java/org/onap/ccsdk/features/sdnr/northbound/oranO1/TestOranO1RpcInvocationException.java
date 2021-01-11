/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */


package org.onap.ccsdk.features.sdnr.northbound.oranO1;

import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200806.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200806.status.Status;


public class TestOranO1RpcInvocationException {

    @Test(expected = SvcLogicException.class)
    public void TestOranO1RpcInvocationException() throws SvcLogicException{
        Status status = null;
        CommonHeader commonHeader = null;
        OranO1RpcInvocationException exception = new OranO1RpcInvocationException(status, commonHeader);
        assert(exception.getStatus() == status);
        assert(exception.getCommonHeader() == commonHeader);
        throw exception;
    }
}
