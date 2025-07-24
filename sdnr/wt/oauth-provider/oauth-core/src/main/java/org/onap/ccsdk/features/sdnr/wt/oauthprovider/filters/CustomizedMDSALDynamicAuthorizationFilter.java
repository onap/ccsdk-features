package org.onap.ccsdk.features.sdnr.wt.oauthprovider.filters;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.HttpAuthorization;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization.policies.Policies;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.permission.Permissions;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class CustomizedMDSALDynamicAuthorizationFilter extends AuthorizationFilter
        implements DataTreeChangeListener<HttpAuthorization> {

    private static final Logger LOG = LoggerFactory.getLogger(CustomizedMDSALDynamicAuthorizationFilter.class);

    private static final DataTreeIdentifier<HttpAuthorization> AUTHZ_CONTAINER = DataTreeIdentifier.create(
            LogicalDatastoreType.CONFIGURATION, InstanceIdentifier.create(HttpAuthorization.class));

    private static DataBroker dataBroker;

    public static void setDataBroker(DataBroker dataBroker2){
        dataBroker = dataBroker2;
    }
    private Registration reg;
    private ListenableFuture<Optional<HttpAuthorization>> authContainer;

    public CustomizedMDSALDynamicAuthorizationFilter() {

    }

    @Override
    public Filter processPathConfig(final String path, final String config) {
        /*if (dataBroker == null){
            throw new RuntimeException("dataBroker is not initialized");
        }*/

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
    public void onDataTreeChanged(@NonNull List<DataTreeModification<HttpAuthorization>> changes) {
        final HttpAuthorization newVal = Iterables.getLast(changes).getRootNode().getDataAfter();
        LOG.debug("Updating authorization information to {}", newVal);
        authContainer = Futures.immediateFuture(Optional.ofNullable(newVal));
    }

    @Override
    public boolean isAccessAllowed(final ServletRequest request, final ServletResponse response,
                                   final Object mappedValue) {
        if (dataBroker == null){
            throw new RuntimeException("dataBroker is not initialized");
        }
        if(reg == null){
            try (ReadTransaction tx = dataBroker.newReadOnlyTransaction()) {
                authContainer = tx.read(AUTHZ_CONTAINER.getDatastoreType(), AUTHZ_CONTAINER.getRootIdentifier());
            }
            reg = dataBroker.registerTreeChangeListener(AUTHZ_CONTAINER, this);
        }
        checkArgument(request instanceof HttpServletRequest, "Expected HttpServletRequest, received {}", request);


        final boolean defaultReturnValue=false;
        final Subject subject = getSubject(request, response);
        final HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        final String requestURI = httpServletRequest.getRequestURI();
        LOG.debug("isAccessAllowed for user={} to requestURI={}", subject, requestURI);

        final Optional<HttpAuthorization> authorizationOptional;
        try {
            authorizationOptional = authContainer.get();
        } catch (ExecutionException e){
            // Something went completely wrong trying to read the authz container.  Deny access.
            LOG.warn("MDSAL attempt to read Http Authz Container failed, disallowing access", e);
            return false;
        }
        catch(InterruptedException e) {
            // Something went completely wrong trying to read the authz container.  Deny access.
            LOG.warn("MDSAL attempt to read Http Authz Container failed, disallowing access", e);
            Thread.currentThread().interrupt();
            return false;
        }

        if (!authorizationOptional.isPresent()) {
            // The authorization container does not exist-- hence no authz rules are present
            // Allow access.
            LOG.debug("Authorization Container does not exist");
            return defaultReturnValue;
        }

        final HttpAuthorization httpAuthorization = authorizationOptional.get();
        final var policies = httpAuthorization.getPolicies();
        List<Policies> policiesList = policies != null ? policies.getPolicies() : null;
        if (policiesList == null || policiesList.isEmpty()) {
            // The authorization container exists, but no rules are present.  Allow access.
            LOG.debug("Exiting early since no authorization rules exist");
            sendError(response, 403, "");
            return defaultReturnValue;
        }

        // Sort the Policies list based on index
        policiesList = new ArrayList<>(policiesList);
        policiesList.sort(Comparator.comparing(Policies::getIndex));

        for (Policies policy : policiesList) {
            final String resource = policy.getResource();
            final boolean pathsMatch = pathsMatch(resource, requestURI);
            if (pathsMatch) {
                LOG.debug("paths match for policy {} pattern={} and requestURI={}", policy.getIndex(), resource, requestURI);
                final String method = httpServletRequest.getMethod();
                LOG.trace("method={}", method);
                List<Permissions> permissions = policy.getPermissions();
                LOG.trace("perm={}", permissions);
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
                sendError(response, 403, "");
                return false;
            }
        }
        LOG.debug("no path found that matches {}", requestURI);
        sendError(response, 403, "");
        return defaultReturnValue;
    }

    private void sendError(ServletResponse response, int code, String message)  {
        if(response instanceof HttpServletResponse){
            try {
                ((HttpServletResponse)response).sendError(code, message);
            } catch (IOException e) {
                LOG.warn("unable to send {} {} response: ", code, message, e);
            }
        }
        else{
            LOG.warn("unable to send {} {} response", code, message);
        }
    }




}
