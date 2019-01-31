/*******************************************************************************
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
 ******************************************************************************/
/**
 * Convert capabilities of netconfnode into internal format. Boron and Carbon are providing
 * different versions
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.container;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Capabilities {

    private static final Logger LOG = LoggerFactory.getLogger(Capabilities.class);
    private static final String INTERFACE_AVAILABLECAPABILITY =
            "org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.available.capabilities.AvailableCapability";

    private final List<String> capabilities = new ArrayList<>();
    private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public Capabilities() {

    }

    public Capabilities(NetconfNode nnode) {
        LOG.info("Create Capabilities constructor");

        if (nnode != null) {
            constructor(nnode.getAvailableCapabilities().getAvailableCapability());
        }
    }

    /**
     * Does all construction steps
     *
     * @param pcapabilities with a list of capabilities. <br>
     *        Type could be <br>
     *        - Boron: List<code><String></code> <br>
     *        - Carbon: List<AvailableCapability>
     */
    private void constructor(List<?> pcapabilities) {
        for (Object capability : pcapabilities) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("capability class: {} Interfaces: {}", capability.getClass().getName(),
                        Arrays.toString(capability.getClass().getInterfaces()));
            }
            if (capability instanceof String) { // ODL Boron specific
                this.capabilities.add((String) capability);
            } else if (hasInterface(capability, INTERFACE_AVAILABLECAPABILITY)) { // Carbon specific part .. handled via
                                                                                  // generic
                try {
                    Method method = capability.getClass().getDeclaredMethod("getCapability");
                    this.capabilities.add(method.invoke(capability).toString());
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    LOG.warn("Unknown capability class leads to a problem", e);
                }
            } else {
                LOG.warn("Unknown capability class: {}", capability.getClass(),
                        Arrays.toString(capability.getClass().getInterfaces()));
            }
        }
    }

    /**
     * check if namespace is supported by given capabilites
     *
     * @param theCapability Capability to search
     * @return true if available
     */
    @Deprecated
    public boolean isSupportingNamespace(QName theCapability) {
        String theNameSpace = theCapability.getNamespace().toString();
        for (String capability : capabilities) {
            if (capability.contains(theNameSpace)) {
                LOG.trace("Check {} against {}", capability, theNameSpace);
                return true;
            }
        }
        return false;
    }

    /**
     * check if the namespace and its revision are supported by the given capabilities
     *
     * @param qCapability capability from the model
     * @return true if supporting the model
     */
    public boolean isSupportingNamespaceAndRevision(QName qCapability) {
        String namespace = qCapability.getNamespace().toString();
        String revision;
        Object revisionObject = qCapability.getRevision();
        if (revisionObject instanceof Optional) {
            if (((Optional<?>) revisionObject).isPresent()) {
                revisionObject = ((Optional<?>) revisionObject).get();
                LOG.info("Unwrapp Optional: {}", revisionObject.getClass());
            }
        }
        if (revisionObject instanceof String) {
            revision = (String) revisionObject;
        } else if (revisionObject instanceof Date) {
            revision = formatter.format((Date) revisionObject);
        } else {
            revision = revisionObject.toString();
            LOG.warn("Revision number type not supported. Class:{} String:{}", revisionObject.getClass().getName(),
                    revisionObject);
        }
        for (String capability : capabilities) {
            if (capability.contains(namespace) && capability.contains(revision)) {
                LOG.trace("Model namespace {}?[revision {}]", namespace, revision);
                return true;
            }
        }
        return false;
    }


    public void add(String qname) {
        capabilities.add(qname);
    }

    @Override
    public String toString() {
        return "Capabilities [capabilities=" + capabilities + "]";
    }

    /**
     * Check if object is proxy and has specific interface
     *
     * @param object Name of the object to verify
     * @param interfaceName is the name of the interface
     * @return boolean accordingly
     */
    static boolean hasInterface(Object object, String interfaceName) {
        if (object instanceof Proxy) {
            Class<?>[] interfaces = object.getClass().getInterfaces();
            for (Class<?> i : interfaces) {
                if (i.getName().equals(interfaceName)) {
                    return true;
                }
            }
        }
        return false;
    }

}
