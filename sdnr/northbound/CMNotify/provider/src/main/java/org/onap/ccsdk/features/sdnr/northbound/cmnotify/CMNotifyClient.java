/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * Modifications Copyright (C) 2020 Nordix Foundation.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.features.sdnr.northbound.cmnotify;

import java.util.Properties;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.MdsalHelper;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;

import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200224.NbrlistChangeNotificationOutputBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMNotifyClient {

    private static final Logger LOG = LoggerFactory.getLogger(CMNotifyClient.class);

    private SvcLogicService svcLogicService = null;

    public CMNotifyClient(final SvcLogicService svcLogicService) {
        this.svcLogicService = svcLogicService;
    }

    public boolean hasGraph(String module, String rpc, String version, String mode) throws SvcLogicException {
        return svcLogicService.hasGraph(module, rpc, version, mode);
    }

      // Client for NbrlistChangeNotification

    public Properties execute(String module, String rpc, String version, String mode,
            NbrlistChangeNotificationOutputBuilder serviceData) throws SvcLogicException {

        Properties parms = new Properties();

        return execute(module, rpc, version, mode, serviceData, parms);
    }

    public Properties execute(String module, String rpc, String version, String mode,
            NbrlistChangeNotificationOutputBuilder serviceData, Properties parms) throws SvcLogicException {
        Properties localProp;
        localProp = MdsalHelper.toProperties(parms, serviceData);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Parameters passed to SLI");

            for (Object key : localProp.keySet()) {
                String parmName = (String) key;
                String parmValue = localProp.getProperty(parmName);

                LOG.debug(parmName + " = " + parmValue);

            }
        }

        Properties respProps = svcLogicService.execute(module, rpc, version, mode, localProp);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Parameters returned by SLI");

            for (Object key : respProps.keySet()) {
                String parmName = (String) key;
                String parmValue = respProps.getProperty(parmName);

                LOG.debug(parmName + " = " + parmValue);

            }
        }
        if ("failure".equalsIgnoreCase(respProps.getProperty("SvcLogic.status"))) {
            return respProps;
        }

        MdsalHelper.toBuilder(respProps, serviceData);

        return respProps;
    }



}
