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

package org.onap.ccsdk.features.sdnr.northbound.a1Adapter;

import java.util.Properties;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.MdsalHelper;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyInstanceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyTypeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyInstanceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyTypeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetHealthCheckOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetNearRTRICsOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstanceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstancesOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypesOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.NotifyPolicyEnforcementUpdateOutputBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class A1AdapterClient {

    private static final Logger LOG = LoggerFactory.getLogger(A1AdapterClient.class);

    private SvcLogicService svcLogicService = null;

    public A1AdapterClient(final SvcLogicService svcLogicService) {
        this.svcLogicService = svcLogicService;
    }

    public boolean hasGraph(String module, String rpc, String version, String mode) throws SvcLogicException {
        return svcLogicService.hasGraph(module, rpc, version, mode);
    }

    // Client for GetNearRTRICs

    public Properties execute(String module, String rpc, String version, String mode,
            GetNearRTRICsOutputBuilder serviceData) throws SvcLogicException {

        Properties parms = new Properties();

        return execute(module, rpc, version, mode, serviceData, parms);
    }

    public Properties execute(String module, String rpc, String version, String mode,
            GetNearRTRICsOutputBuilder serviceData, Properties parms) throws SvcLogicException {
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

    // Client for GetHealthCheck

    public Properties execute(String module, String rpc, String version, String mode,
            GetHealthCheckOutputBuilder serviceData) throws SvcLogicException {

        Properties parms = new Properties();

        return execute(module, rpc, version, mode, serviceData, parms);
    }

    public Properties execute(String module, String rpc, String version, String mode,
            GetHealthCheckOutputBuilder serviceData, Properties parms) throws SvcLogicException {
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


    // Client for getPolicyTypes

    public Properties execute(String module, String rpc, String version, String mode,
            GetPolicyTypesOutputBuilder serviceData) throws SvcLogicException {

        Properties parms = new Properties();

        return execute(module, rpc, version, mode, serviceData, parms);
    }

    public Properties execute(String module, String rpc, String version, String mode,
            GetPolicyTypesOutputBuilder serviceData, Properties parms) throws SvcLogicException {
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


    // Client for createPolicyType


    public Properties execute(String module, String rpc, String version, String mode,
            CreatePolicyTypeOutputBuilder serviceData) throws SvcLogicException {

        Properties parms = new Properties();

        return execute(module, rpc, version, mode, serviceData, parms);
    }

    public Properties execute(String module, String rpc, String version, String mode,
            CreatePolicyTypeOutputBuilder serviceData, Properties parms) throws SvcLogicException {
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

    // Client for getPolicyType

    public Properties execute(String module, String rpc, String version, String mode,
            GetPolicyTypeOutputBuilder serviceData) throws SvcLogicException {

        Properties parms = new Properties();

        return execute(module, rpc, version, mode, serviceData, parms);
    }

    public Properties execute(String module, String rpc, String version, String mode,
            GetPolicyTypeOutputBuilder serviceData, Properties parms) throws SvcLogicException {
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


    // Client for deletePolicyType

    public Properties execute(String module, String rpc, String version, String mode,
            DeletePolicyTypeOutputBuilder serviceData) throws SvcLogicException {

        Properties parms = new Properties();

        return execute(module, rpc, version, mode, serviceData, parms);
    }

    public Properties execute(String module, String rpc, String version, String mode,
            DeletePolicyTypeOutputBuilder serviceData, Properties parms) throws SvcLogicException {
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


    // Client for getPolicyInstances

    public Properties execute(String module, String rpc, String version, String mode,
            GetPolicyInstancesOutputBuilder serviceData) throws SvcLogicException {

        Properties parms = new Properties();

        return execute(module, rpc, version, mode, serviceData, parms);
    }

    public Properties execute(String module, String rpc, String version, String mode,
            GetPolicyInstancesOutputBuilder serviceData, Properties parms) throws SvcLogicException {
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



    // Client for createPolicyInstance

    public Properties execute(String module, String rpc, String version, String mode,
            CreatePolicyInstanceOutputBuilder serviceData) throws SvcLogicException {

        Properties parms = new Properties();

        return execute(module, rpc, version, mode, serviceData, parms);
    }

    public Properties execute(String module, String rpc, String version, String mode,
            CreatePolicyInstanceOutputBuilder serviceData, Properties parms) throws SvcLogicException {
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


    // Client for getPolicyInstance

    public Properties execute(String module, String rpc, String version, String mode,
            GetPolicyInstanceOutputBuilder serviceData) throws SvcLogicException {

        Properties parms = new Properties();

        return execute(module, rpc, version, mode, serviceData, parms);
    }

    public Properties execute(String module, String rpc, String version, String mode,
            GetPolicyInstanceOutputBuilder serviceData, Properties parms) throws SvcLogicException {
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

    // Client for deletePolicyInstance

    public Properties execute(String module, String rpc, String version, String mode,
            DeletePolicyInstanceOutputBuilder serviceData) throws SvcLogicException {

        Properties parms = new Properties();

        return execute(module, rpc, version, mode, serviceData, parms);
    }

    public Properties execute(String module, String rpc, String version, String mode,
            DeletePolicyInstanceOutputBuilder serviceData, Properties parms) throws SvcLogicException {
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

    // Client for getStatus


    public Properties execute(String module, String rpc, String version, String mode,
            GetStatusOutputBuilder serviceData) throws SvcLogicException {

        Properties parms = new Properties();

        return execute(module, rpc, version, mode, serviceData, parms);
    }

    public Properties execute(String module, String rpc, String version, String mode,
            GetStatusOutputBuilder serviceData, Properties parms) throws SvcLogicException {
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


    // Client for notifyPolicyEnforcementUpdate


    public Properties execute(String module, String rpc, String version, String mode,
            NotifyPolicyEnforcementUpdateOutputBuilder serviceData) throws SvcLogicException {

        Properties parms = new Properties();

        return execute(module, rpc, version, mode, serviceData, parms);
    }

    public Properties execute(String module, String rpc, String version, String mode,
            NotifyPolicyEnforcementUpdateOutputBuilder serviceData, Properties parms) throws SvcLogicException {
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
