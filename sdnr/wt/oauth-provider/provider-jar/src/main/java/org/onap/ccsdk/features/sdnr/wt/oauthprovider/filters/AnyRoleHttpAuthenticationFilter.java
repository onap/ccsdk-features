/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.filters;

import java.util.Arrays;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.RolesAuthorizationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Requires the requesting user to be {@link org.apache.shiro.subject.Subject#isAuthenticated() authenticated} for the
 * request to continue, and if they're not, requires the user to login via the HTTP Bearer protocol-specific challenge.
 * Upon successful login, they're allowed to continue on to the requested resource/url.
 * <p/>
 * The {@link #onAccessDenied(ServletRequest, ServletResponse)} method will only be called if the subject making the
 * request is not {@link org.apache.shiro.subject.Subject#isAuthenticated() authenticated}
 *
 * @see <a href="https://tools.ietf.org/html/rfc2617">RFC 2617</a>
 * @see <a href="https://tools.ietf.org/html/rfc6750#section-2.1">OAuth2 Authorization Request Header Field</a>
 * @since 1.5
 */

public class AnyRoleHttpAuthenticationFilter extends RolesAuthorizationFilter {

    /**
     * This class's private logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AnyRoleHttpAuthenticationFilter.class);

    @Override
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        final Subject subject = getSubject(request, response);
        final String[] rolesArray = (String[]) mappedValue;
        LOG.debug("isAccessAllowed {}", Arrays.asList(rolesArray));

        if (rolesArray == null || rolesArray.length == 0) {
            //no roles specified, so nothing to check - allow access.
            LOG.debug("no role specified: access allowed");
            return true;
        }

        for (String roleName : rolesArray) {
            LOG.debug("checking role {}", roleName);
            if (subject.hasRole(roleName)) {
                LOG.debug("role matched to {}: access allowed", roleName);
                return true;
            }
        }
        LOG.debug("no role matched: access denied");
        return false;
    }
}