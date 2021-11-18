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
/**
 * Convert capabilities of netconfnode into internal format. Boron and Carbon are providing
 * different versions
 */
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.AvailableCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.UnavailableCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.available.capabilities.AvailableCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.unavailable.capabilities.UnavailableCapability;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper class for capabilites for Boron and later releases. Uses generics because yang model was changed from Boron
 * to later version. Interface class:
 * org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.available.capabilities.AvailableCapability
 */
public class Capabilities {

    private static final Logger LOG = LoggerFactory.getLogger(Capabilities.class);

    private static final String UNSUPPORTED = "Unsupported";
    private final List<String> capabilities = new ArrayList<>();
    private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private Capabilities() {}

    public static Capabilities getAvailableCapabilities(@Nullable NetconfNode nnode) {
        LOG.info("GetAvailableCapabilities for node");
        Capabilities capabilities = new Capabilities();
        if (nnode != null) {
            AvailableCapabilities availableCapabilites = nnode.getAvailableCapabilities();
            if (availableCapabilites != null) {
                capabilities.constructor(availableCapabilites.getAvailableCapability());
            } else {
                LOG.debug("empty capabilites");
            }
        } else {
            LOG.debug("No node provided");
        }
        return capabilities;
    }

    public static Capabilities getUnavailableCapabilities(NetconfNode nnode) {
        LOG.info("GetUnavailableCapabilities for node");
        Capabilities capabilities = new Capabilities();
        if (nnode != null) {
            UnavailableCapabilities availableCapabilites = nnode.getUnavailableCapabilities();
            if (availableCapabilites != null) {
                capabilities.constructor2(availableCapabilites.getUnavailableCapability());
            } else {
                LOG.debug("empty capabilites");
            }
        } else {
            LOG.debug("No node provided");
        }
        return capabilities;
    }


    /**
     * Does all construction steps
     *
     * @param pcapabilities with a list of capabilities.
     */
    private void constructor(List<AvailableCapability> pcapabilities) {
        if (pcapabilities != null) {
            for (AvailableCapability capability : pcapabilities) {
                this.capabilities.add(capability.getCapability());
            }
        }
    }

    private void constructor2(List<UnavailableCapability> pcapabilities) {
        if (pcapabilities != null) {
            for (UnavailableCapability capability : pcapabilities) {
                this.capabilities.add(capability.getCapability());
            }
        }
    }

    /**
     * Get Capabilites
     *
     * @return List<String> with capabilites
     */
    public List<String> getCapabilities() {
        return capabilities;
    }

    /**
     * Verify if the namespace is supported
     *
     * @param qCapability from model
     * @return true if namespace is supported
     */
    public boolean isSupportingNamespace(QName qCapability) {
        String namespace = qCapability.getNamespace().toString();
        return isSupportingNamespaceAndRevision(namespace, null);
    }

    /**
     * Verify if the namespace is supported
     *
     * @param namespace
     * @return
     */
    public boolean isSupportingNamespace(String namespace) {
        return isSupportingNamespaceAndRevision(namespace, null);
    }

    /**
     * check if the namespace and its revision are supported by the given capabilities
     *
     * @param qCapability capability from the model
     * @return true if supporting the model AND revision<br>
     *         false if revision not available or both not found.
     */
    public boolean isSupportingNamespaceAndRevision(QName qCapability) {
        String namespace = qCapability.getNamespace().toString();
        String revision = getRevisionString(qCapability);
        return revision == null ? false : isSupportingNamespaceAndRevision(namespace, revision);
    }

    /**
     *
     * @param namespace requested
     * @param revision request or null for any revision
     * @return true if existing
     */
    public boolean isSupportingNamespaceAndRevision(String namespace, @Nullable String revision) {
        LOG.trace("isSupportingNamespaceAndRevision: Model namespace {}?[revision {}]", namespace, revision);

        final String nsAndRev = String.format("%s?revision=%s", namespace, revision);
        for (String capability : capabilities) {
            //if (capability.contains(namespace) && (revision == null || capability.contains(revision))) {
            if (capability.contains(revision != null ? nsAndRev : namespace)) {
                LOG.trace("Verify true with: {}", capability);
                return true;
            } else {
                LOG.trace("Verify false with: {}", capability);
            }
        }
        return false;
    }

    /**
     * Provide revision as String from QName, considering older formats.
     *
     * @param qCapability that specifies the revision
     * @return String with revisiondate or null
     */
    private String getRevisionString(QName qCapability) {
        Object revisionObject = qCapability.getRevision();
        String revision = null;
        if (revisionObject instanceof Optional) {
            if (((Optional<?>) revisionObject).isPresent()) {
                revisionObject = ((Optional<?>) revisionObject).get();
                LOG.info("Unwrapp Optional: {}", revisionObject != null ? revisionObject.getClass() : null);
            }
        }
        if (revisionObject == null) {
            // Cover null case
        } else if (revisionObject instanceof String) {
            revision = (String) revisionObject;
        } else if (revisionObject instanceof Date) {
            revision = formatter.format((Date) revisionObject);
        } else {
            revision = revisionObject.toString();
            LOG.debug("Revision number type not supported. Use toString().String:{} Class:{} ", revisionObject,
                    revisionObject.getClass().getName());
        }
        return revision;
    }

    /**
     * Get revision of first entry of related capability
     *
     * @param qCapability that specifies the namespace
     * @return String with date or
     */
    public String getRevisionForNamespace(QName qCapability) {
        String namespace = qCapability.getNamespace().toString();
        for (String capability : capabilities) {
            if (capability.contains(namespace)) {
                Optional<Revision> revisionOpt = QName.create(capability).getRevision();
                if (revisionOpt.isPresent()) {
                    return revisionOpt.get().toString();
                }
            }
        }
        return UNSUPPORTED;
    }

    /**
     * Verify if QName namespace is supported by capabilities
     *
     * @param revision result of getRevisionForNamespace()
     * @return true if namespace is supported.
     */
    static public boolean isNamespaceSupported(String revision) {
        return !UNSUPPORTED.equals(revision);
    }

    @Override
    public String toString() {
        return "Capabilities [capabilities=" + capabilities + "]";
    }

}
