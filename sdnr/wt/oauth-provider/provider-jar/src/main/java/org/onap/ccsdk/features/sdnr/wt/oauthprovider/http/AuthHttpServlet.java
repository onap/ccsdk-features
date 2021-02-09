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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.codec.Base64;
import org.jolokia.osgi.security.Authenticator;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPClient;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.Config;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OAuthProviderConfig;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OAuthToken;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OdlPolicy;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UserTokenPayload;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.AuthService;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.AuthService.PublicOAuthProviderConfig;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.OAuthProviderFactory;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.TokenCreator;
import org.opendaylight.aaa.api.IDMStoreException;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.shiro.filters.backport.BearerToken;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.shiro.configuration.Urls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthHttpServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(AuthHttpServlet.class.getName());
    private static final long serialVersionUID = 1L;
    private static final String BASEURI = "/oauth";
    private static final String LOGINURI = BASEURI + "/login";
    //private static final String LOGOUTURI = BASEURI + "/logout";
    private static final String PROVIDERSURI = BASEURI + "/providers";
    public static final String REDIRECTURI = BASEURI + "/redirect";
    private static final String POLICIESURI = BASEURI + "/policies";
    //private static final String PROVIDERID_REGEX = "^\\" + BASEURI + "\\/providers\\/([^\\/]+)$";
    private static final String REDIRECTID_REGEX = "^\\" + BASEURI + "\\/redirect\\/([^\\/]+)$";
    //private static final Pattern PROVIDERID_PATTERN = Pattern.compile(PROVIDERID_REGEX);
    private static final Pattern REDIRECTID_PATTERN = Pattern.compile(REDIRECTID_REGEX);

    private static final String DEFAULT_DOMAIN = "sdn";
    private static final String HEAEDER_AUTHORIZATION = "Authorization";

    private final ObjectMapper mapper;
    /* state <=> AuthProviderService> */
    private final Map<String, AuthService> providerStore;
    private Authenticator odlAuthenticator;
    private IdMService odlIdentityService;
    private final TokenCreator tokenCreator;
    private final Config config;
    private ShiroConfiguration shiroConfiguration;

    public AuthHttpServlet() throws IOException {
        this.tokenCreator = TokenCreator.getInstance();
        this.config = Config.getInstance();
        this.mapper = new ObjectMapper();
        this.providerStore = new HashMap<>();
        for (OAuthProviderConfig pc : config.getProviders()) {
            this.providerStore.put(pc.getId(), OAuthProviderFactory.create(pc.getType(), pc,
                    this.config.getRedirectUri(), TokenCreator.getInstance()));
        }

    }

    public void setOdlAuthenticator(Authenticator odlAuthenticator) {
        this.odlAuthenticator = odlAuthenticator;
    }

    public void setOdlIdentityService(IdMService odlIdentityService) {
        this.odlIdentityService = odlIdentityService;
    }

    public void setShiroConfiguration(ShiroConfiguration shiroConfiguration) {
        this.shiroConfiguration = shiroConfiguration;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOG.debug("GET request for {}", req.getRequestURI());
        fillHost(req);
        if (PROVIDERSURI.equals(req.getRequestURI())) {
            this.sendResponse(resp, HttpServletResponse.SC_OK, getConfigs(this.providerStore.values()));
        } else if (POLICIESURI.equals(req.getRequestURI())) {
            this.sendResponse(resp, HttpServletResponse.SC_OK, this.getPoliciesForUser(req));
        } else if (req.getRequestURI().startsWith(REDIRECTURI)) {
            this.handleRedirect(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

    }

    /**
     * find out what urls can be accessed by user and which are forebidden
     *
     * urlEntries: "anon" -> any access allowed "authcXXX" -> no grouping rule -> any access for user allowed "authcXXX,
     * roles[abc] -> user needs to have role abc "authcXXX, roles["abc,def"] -> user needs to have roles abc AND def
     * "authcXXX, anyroles[abc] -> user needs to have role abc "authcXXX, anyroles["abc,def"] -> user needs to have
     * roles abc OR def
     *
     *
     * @param req
     * @return
     */
    private List<OdlPolicy> getPoliciesForUser(HttpServletRequest req) {
        List<Urls> urlRules = this.shiroConfiguration.getUrls();
        UserTokenPayload data = this.getUserInfo(req);
        List<OdlPolicy> policies = new ArrayList<>();
        if (urlRules != null) {
            LOG.debug("try to find rules for user {} with roles {}",
                    data == null ? "null" : data.getPreferredUsername(), data == null ? "null" : data.getRoles());
            final String regex = "^([^,]+)[,]?[\\ ]?([anyroles]+)?(\\[\"?([a-zA-Z,]+)\"?\\])?";
            final Pattern pattern = Pattern.compile(regex);
            Matcher matcher;
            for (Urls urlRule : urlRules) {
                final String url = urlRule.getPairKey();
                matcher = pattern.matcher(urlRule.getPairValue());
                if (matcher.find()) {
                    if (!urlRule.getPairValue().contains(",")) {
                        LOG.debug("found rule without roles for '{}'", matcher.group(1));
                        //not important if anon or authcXXX
                        if (data != null || "anon".equals(matcher.group(1))) {
                            policies.add(OdlPolicy.allowAll(url));
                        }
                    } else if (data != null) {
                        LOG.debug("found rule with roles '{}'", matcher.group(4));
                        if ("roles".equals(matcher.group(2))) {
                            if (this.rolesMatch(data.getRoles(), Arrays.asList(matcher.group(4).split(",")), false)) {
                                policies.add(OdlPolicy.allowAll(url));
                            } else {
                                policies.add(OdlPolicy.denyAll(url));
                            }
                        } else if ("anyroles".equals(matcher.group(2))) {
                            if (this.rolesMatch(data.getRoles(), Arrays.asList(matcher.group(4).split(",")), true)) {
                                policies.add(OdlPolicy.allowAll(url));
                            } else {
                                policies.add(OdlPolicy.denyAll(url));
                            }
                        } else {
                            LOG.warn("unable to detect url role value: {}", urlRule.getPairValue());
                        }
                    } else {
                        policies.add(OdlPolicy.denyAll(url));
                    }
                } else {
                    LOG.warn("unable to detect url role value: {}", urlRule.getPairValue());
                }
            }
        } else {
            LOG.debug("no url rules found");
        }
        return policies;
    }

    private UserTokenPayload getUserInfo(HttpServletRequest req) {
        if (isBearer(req)) {
            UserTokenPayload data = TokenCreator.getInstance().decode(req);
            if (data != null) {
                return data;
            }
        } else if (isBasic(req)) {
            String username = getBasicAuthUsername(req);
            if (username != null) {
                final String domain = getBasicAuthDomain(username);
                if (!username.contains("@")) {
                    username = String.format("%s@%s", username, domain);
                }
                List<String> roles = this.odlIdentityService.listRoles(username, domain);
                return UserTokenPayload.create(username, roles);
            }
        }
        return null;
    }

    private static String getBasicAuthDomain(String username) {
        if (username.contains("@")) {
            return username.split("@")[1];
        }
        return DEFAULT_DOMAIN;
    }

    private static String getBasicAuthUsername(HttpServletRequest req) {
        final String header = req.getHeader(HEAEDER_AUTHORIZATION);
        final String decoded = Base64.decodeToString(header.substring(6));
        // attempt to decode username/password; otherwise decode as token
        if (decoded.contains(":")) {
            return decoded.split(":")[0];
        }
        LOG.warn("unable to detect username from basicauth header {}", header);
        return null;
    }

    private static boolean isBasic(HttpServletRequest req) {
        final String header = req.getHeader(HEAEDER_AUTHORIZATION);
        return header == null ? false : header.startsWith("Basic");
    }

    private static boolean isBearer(HttpServletRequest req) {
        final String header = req.getHeader(HEAEDER_AUTHORIZATION);
        return header == null ? false : header.startsWith("Bearer");
    }

    private boolean rolesMatch(List<String> userRoles, List<String> policyRoles, boolean any) {
        if (any) {
            for (String policyRole : policyRoles) {
                if (userRoles.contains(policyRole)) {
                    return true;
                }
            }
            return false;
        } else {
            for (String policyRole : policyRoles) {
                if (!userRoles.contains(policyRole)) {
                    return false;
                }
            }
            return true;
        }

    }

    private void fillHost(HttpServletRequest req) {
        String hostUrl = this.config.getHost();
        if (hostUrl == null) {
            final String tmp = req.getRequestURL().toString();
            final String regex = "^(http[s]{0,1}:\\/\\/[^\\/]+)";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(tmp);
            if (matcher.find()) {
                hostUrl = matcher.group(1);
                this.config.setHost(hostUrl);
            }
        }

    }

    private List<PublicOAuthProviderConfig> getConfigs(Collection<AuthService> values) {
        List<PublicOAuthProviderConfig> configs = new ArrayList<>();
        for (AuthService svc : values) {
            configs.add(svc.getConfig(this.config.getHost()));
        }
        return configs;
    }

    /**
     * GET /oauth/redirect/{providerID}
     *
     * @param req
     * @param resp
     * @throws IOException
     */
    private void handleRedirect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String uri = req.getRequestURI();
        final Matcher matcher = REDIRECTID_PATTERN.matcher(uri);
        if (matcher.find()) {
            AuthService provider = this.providerStore.getOrDefault(matcher.group(1), null);
            if (provider != null) {
                provider.setLocalHostUrl(this.config.getHost());
                provider.handleRedirect(req, resp);
                return;
            }
        }
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        LOG.debug("POST request for {}", req.getRequestURI());
        if (this.config.doSupportOdlUsers() && LOGINURI.equals(req.getRequestURI())) {
            final String username = req.getParameter("username");
            final String domain = req.getParameter("domain");
            BearerToken token =
                    this.doLogin(username, req.getParameter("password"), domain != null ? domain : DEFAULT_DOMAIN);
            if (token != null) {
                sendResponse(resp, HttpServletResponse.SC_OK, new OAuthToken(token));
                LOG.debug("login for odluser {} succeeded", username);
                return;
            } else {
                LOG.debug("login failed");
            }

        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //        final String uri = req.getRequestURI();
        //        final Matcher matcher = PROVIDERID_PATTERN.matcher(uri);
        //        if (matcher.find()) {
        //            final String id = matcher.group(1);
        //            final OAuthProviderConfig config = this.mapper.readValue(req.getInputStream(), OAuthProviderConfig.class);
        //            //this.providerStore.put(id, config);
        //            sendResponse(resp, HttpServletResponse.SC_OK, "");
        //            return;
        //        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //        final String uri = req.getRequestURI();
        //        final Matcher matcher = PROVIDERID_PATTERN.matcher(uri);
        //        if (matcher.find()) {
        //            final String id = matcher.group(1);
        //            this.providerStore.remove(id);
        //            sendResponse(resp, HttpServletResponse.SC_OK, "");
        //            return;
        //        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private BearerToken doLogin(String username, String password, String domain) {
        if (!username.contains("@")) {
            username = String.format("%s@%s", username, domain);
        }
        HttpServletRequest req = new HeadersOnlyHttpServletRequest(
                Map.of("Authorization", BaseHTTPClient.getAuthorizationHeaderValue(username, password)));
        if (this.odlAuthenticator.authenticate(req)) {
            try {
                LOG.info("userids={}", this.odlIdentityService.listUserIDs());
                LOG.info("domains={}", this.odlIdentityService.listDomains(username));
            } catch (IDMStoreException e) {

            }
            List<String> roles = this.odlIdentityService.listRoles(username, domain);
            UserTokenPayload data = new UserTokenPayload();
            data.setPreferredUsername(username);
            data.setFamilyName("");
            data.setGivenName(username);
            data.setExp(this.tokenCreator.getDefaultExp());
            data.setRoles(roles);
            return this.tokenCreator.createNewJWT(data);

        }
        return null;
    }



    private void sendResponse(HttpServletResponse resp, int code, Object data) throws IOException {
        byte[] output = data != null ? mapper.writeValueAsString(data).getBytes() : new byte[0];
        // output
        resp.setStatus(code);
        resp.setContentLength(output.length);
        resp.setContentType("application/json");
        ServletOutputStream os = null;
        os = resp.getOutputStream();
        os.write(output);

    }

}
