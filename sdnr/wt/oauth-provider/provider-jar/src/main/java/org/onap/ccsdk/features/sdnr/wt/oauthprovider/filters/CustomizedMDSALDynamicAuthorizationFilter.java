package org.onap.ccsdk.features.sdnr.wt.oauthprovider.filters;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.opendaylight.aaa.shiro.web.env.ThreadLocals;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.HttpAuthorization;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization.policies.Policies;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.permission.Permissions;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class CustomizedMDSALDynamicAuthorizationFilter extends AuthorizationFilter
        implements ClusteredDataTreeChangeListener<HttpAuthorization> {

    private static final Logger LOG = LoggerFactory.getLogger(CustomizedMDSALDynamicAuthorizationFilter.class);

    private static final DataTreeIdentifier<HttpAuthorization> AUTHZ_CONTAINER = DataTreeIdentifier.create(
            LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(HttpAuthorization.class));

    private final DataBroker dataBroker;

    private ListenerRegistration<?> reg;
    private volatile ListenableFuture<Optional<HttpAuthorization>> authContainer;

    public CustomizedMDSALDynamicAuthorizationFilter() {
        dataBroker = requireNonNull(ThreadLocals.DATABROKER_TL.get());
    }

    @Override
    public Filter processPathConfig(final String path, final String config) {
        try (ReadTransaction tx = dataBroker.newReadOnlyTransaction()) {
            authContainer = tx.read(AUTHZ_CONTAINER.getDatastoreType(), AUTHZ_CONTAINER.getRootIdentifier());
        }
        this.reg = dataBroker.registerDataTreeChangeListener(AUTHZ_CONTAINER, this);
        return super.processPathConfig(path, config);
    }

    @Override
    public void destroy() {
        if (reg != null) {
            reg.close();
            reg = null;
        }
        super.destroy();
    }

    @Override
    public void onDataTreeChanged(final Collection<DataTreeModification<HttpAuthorization>> changes) {
        final HttpAuthorization newVal = Iterables.getLast(changes).getRootNode().getDataAfter();
        LOG.debug("Updating authorization information to {}", newVal);
        authContainer = Futures.immediateFuture(Optional.ofNullable(newVal));
    }

    @Override
    public boolean isAccessAllowed(final ServletRequest request, final ServletResponse response,
                                   final Object mappedValue) {
        checkArgument(request instanceof HttpServletRequest, "Expected HttpServletRequest, received {}", request);

        final Subject subject = getSubject(request, response);
        final HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        final String requestURI = httpServletRequest.getRequestURI();
        LOG.debug("isAccessAllowed for user={} to requestURI={}", subject, requestURI);

        final Optional<HttpAuthorization> authorizationOptional;
        try {
            authorizationOptional = authContainer.get();
        } catch (ExecutionException | InterruptedException e) {
            // Something went completely wrong trying to read the authz container.  Deny access.
            LOG.warn("MDSAL attempt to read Http Authz Container failed, disallowing access", e);
            return false;
        }

        if (!authorizationOptional.isPresent()) {
            // The authorization container does not exist-- hence no authz rules are present
            // Allow access.
            LOG.debug("Authorization Container does not exist");
            return true;
        }

        final HttpAuthorization httpAuthorization = authorizationOptional.get();
        final var policies = httpAuthorization.getPolicies();
        List<Policies> policiesList = policies != null ? policies.getPolicies() : null;
        if (policiesList == null || policiesList.isEmpty()) {
            // The authorization container exists, but no rules are present.  Allow access.
            LOG.debug("Exiting successfully early since no authorization rules exist");
            return true;
        }

        // Sort the Policies list based on index
        policiesList = new ArrayList<>(policiesList);
        policiesList.sort(Comparator.comparing(Policies::getIndex));

        for (Policies policy : policiesList) {
            final String resource = policy.getResource();
            final boolean pathsMatch = pathsMatch(resource, requestURI);
            if (pathsMatch) {
                LOG.debug("paths match for pattern={} and requestURI={}", resource, requestURI);
                final String method = httpServletRequest.getMethod();
                LOG.trace("method={}", method);
                List<Permissions> permissions = policy.getPermissions();
                if(permissions !=null) {
                    for (Permissions permission : permissions) {
                        final String role = permission.getRole();
                        LOG.trace("role={}", role);
                        Set<Permissions.Actions> actions = permission.getActions();
                        if (actions != null) {
                            for (Permissions.Actions action : actions) {
                                LOG.trace("action={}", action.getName());
                                if (action.getName().equalsIgnoreCase(method)) {
                                    final boolean hasRole = subject.hasRole(role);
                                    LOG.trace("hasRole({})={}", role, hasRole);
                                    if (hasRole) {
                                        return true;
                                    }
                                }
                            }
                        }
                        else{
                            LOG.trace("no actions found");
                        }
                    }
                }
                else {
                    LOG.trace("no permissions found");
                }
                LOG.debug("couldn't authorize the user for access");
                return false;
            }
        }
        LOG.debug("successfully authorized the user for access");
        return true;
    }
}
