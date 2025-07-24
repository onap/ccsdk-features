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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.BearerToken;
import org.apache.shiro.codec.Base64;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.Config;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.InvalidConfigurationException;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.NoDefinitionFoundException;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OAuthProviderConfig;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OAuthToken;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OdlPolicy;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OdlShiroConfiguration;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OdlShiroConfiguration.MainItem;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OdlShiroConfiguration.UrlItem;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OdlXmlMapper;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UnableToConfigureOAuthService;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UserTokenPayload;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.filters.CustomizedMDSALDynamicAuthorizationFilter;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.AuthService;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.AuthService.PublicOAuthProviderConfig;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.MdSalAuthorizationStore;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.OAuthProviderFactory;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.TokenCreator;
import org.opendaylight.aaa.api.AuthenticationException;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.PasswordCredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.aaa.tokenauthrealm.auth.PasswordCredentialBuilder;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthHttpServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(AuthHttpServlet.class.getName());
    private static final long serialVersionUID = 1L;
    private static final String BASEURI = "/oauth";
    private static final String LOGINURI = BASEURI + "/login";
    private static final String LOGOUTURI = BASEURI + "/logout";
    private static final String PROVIDERSURI = BASEURI + "/providers";
    public static final String REDIRECTURI = BASEURI + "/redirect";
    private static final String REDIRECTURI_FORMAT = REDIRECTURI + "/%s";
    private static final String POLICIESURI = BASEURI + "/policies";
    private static final String REDIRECTID_REGEX = "^\\" + BASEURI + "\\/redirect\\/([^\\/]+)$";
    private static final String LOGIN_REDIRECT_REGEX = "^\\" + LOGINURI + "\\/([^\\/]+)$";
    private static final Pattern REDIRECTID_PATTERN = Pattern.compile(REDIRECTID_REGEX);
    private static final Pattern LOGIN_REDIRECT_PATTERN = Pattern.compile(LOGIN_REDIRECT_REGEX);

    private static final String DEFAULT_DOMAIN = "sdn";
    private static final String HEAEDER_AUTHORIZATION = "Authorization";

    private static final String LOGOUT_REDIRECT_URL_PARAMETER = "redirect_uri";
    private static final String CLASSNAME_ODLBASICAUTH =
            "org.opendaylight.aaa.shiro.filters.ODLHttpAuthenticationFilter";
    private static final String CLASSNAME_ODLBEARERANDBASICAUTH =
            "org.opendaylight.aaa.shiro.filters.ODLHttpAuthenticationFilter2";
    private static final String CLASSNAME_ODLMDSALAUTH =
            "org.opendaylight.aaa.shiro.realm.MDSALDynamicAuthorizationFilter";
    public static final String LOGIN_REDIRECT_FORMAT = LOGINURI + "/%s";
    private static final String URI_PRE = BASEURI;

    private static final String CONFIGFILE ="/opt/opendaylight/etc/opendaylight/datastore/initial/config/aaa-app-config.xml";
    private final ObjectMapper mapper;
    /* state <=> AuthProviderService> */
    private final Map<String, AuthService> providerStore;
    private final TokenCreator tokenCreator;
    private final Config config;
    private static MdSalAuthorizationStore mdsalAuthStore;
    private static PasswordCredentialAuth passwordCredentialAuth;
    private static OdlShiroConfiguration shiroConfiguration;

    public AuthHttpServlet() throws IllegalArgumentException, IOException, InvalidConfigurationException,
            UnableToConfigureOAuthService {
        this(CONFIGFILE);
    }
    public AuthHttpServlet(String shiroconfigfile) throws IllegalArgumentException, IOException, InvalidConfigurationException,
            UnableToConfigureOAuthService {
        this.config = Config.getInstance();
        this.shiroConfiguration = loadShiroConfig(shiroconfigfile);
        this.tokenCreator = TokenCreator.getInstance(this.config);
        this.mapper = new ObjectMapper();
        this.providerStore = new HashMap<>();
        for (OAuthProviderConfig pc : config.getProviders()) {
            this.providerStore.put(pc.getId(), OAuthProviderFactory.create(pc.getType(), pc,
                    this.config.getRedirectUri(), TokenCreator.getInstance(this.config)));
        }
    }

    public void setDataBroker(DataBroker dataBroker) {
        CustomizedMDSALDynamicAuthorizationFilter.setDataBroker(dataBroker);
        mdsalAuthStore = new MdSalAuthorizationStore(dataBroker);
    }

    public void setPasswordCredentialAuth(PasswordCredentialAuth passwordCredentialAuth) {
        this.passwordCredentialAuth = passwordCredentialAuth;
    }


    public void onUnbindService(HttpService httpService) {
        httpService.unregister(AuthHttpServlet.URI_PRE);

    }

    public void onBindService(HttpService httpService)
            throws ServletException, NamespaceException {
        if (httpService == null) {
            LOG.warn("Unable to inject HttpService into loader.");
        } else {
            httpService.registerServlet(AuthHttpServlet.URI_PRE, this, null, null);
            LOG.info("oauth servlet registered.");
        }
    }
    private static OdlShiroConfiguration loadShiroConfig(String filename) throws  IOException {
        OdlXmlMapper mapper = new OdlXmlMapper();
        return mapper.readValue(new File(filename), OdlShiroConfiguration.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOG.debug("GET request for {}", req.getRequestURI());
        getHost(req);
        if (PROVIDERSURI.equals(req.getRequestURI())) {
            this.sendResponse(resp, HttpServletResponse.SC_OK, getConfigs(this.providerStore.values()));
        } else if (req.getRequestURI().startsWith(LOGINURI)) {
            this.handleLoginRedirect(req, resp);
        } else if (req.getRequestURI().equals(LOGOUTURI)) {
            this.handleLogout(req, resp);
        } else if (POLICIESURI.equals(req.getRequestURI())) {
            this.sendResponse(resp, HttpServletResponse.SC_OK, this.getPoliciesForUser(req));
        } else if (req.getRequestURI().startsWith(REDIRECTURI)) {
            this.handleRedirect(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String bearerToken = this.tokenCreator.getBearerToken(req, true);
        String redirectUrl = req.getParameter(LOGOUT_REDIRECT_URL_PARAMETER);
        if (redirectUrl == null) {
            redirectUrl = this.config.getPublicUrl();
        }
        if(redirectUrl == null) {
            redirectUrl = getHost(req);
        }
        UserTokenPayload userInfo = this.tokenCreator.decode(bearerToken);
        if (bearerToken != null && userInfo != null && !userInfo.isInternal()) {
            AuthService provider = this.providerStore.getOrDefault(userInfo.getProviderId(), null);

            if (provider != null) {
                provider.sendLogoutRedirectResponse(bearerToken, resp, redirectUrl);
                this.logout();
                return;
            }
        }
        this.logout();
        resp.sendRedirect(redirectUrl);

    }

    private void handleLoginRedirect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String uri = req.getRequestURI();
        final Matcher matcher = LOGIN_REDIRECT_PATTERN.matcher(uri);
        if (matcher.find()) {
            final String id = matcher.group(1);
            AuthService provider = this.providerStore.getOrDefault(id, null);
            if (provider != null) {
                String redirectUrl = getHost(req) + String.format(REDIRECTURI_FORMAT, id);
                provider.sendLoginRedirectResponse(resp, redirectUrl);
                return;
            }
        }
        this.sendResponse(resp, HttpServletResponse.SC_NOT_FOUND, "");
    }

    /**
     * find out what urls can be accessed by user and which are forbidden
     * <p>
     * urlEntries: "anon" -> any access allowed "authcXXX" -> no grouping rule -> any access for user allowed "authcXXX,
     * roles[abc] -> user needs to have role abc "authcXXX, roles["abc,def"] -> user needs to have roles abc AND def
     * "authcXXX, anyroles[abc] -> user needs to have role abc "authcXXX, anyroles["abc,def"] -> user needs to have
     * roles abc OR def
     *
     * @param req
     * @return
     */
    private List<OdlPolicy> getPoliciesForUser(HttpServletRequest req) {
        List<OdlPolicy> policies = new ArrayList<>();
        List<UrlItem> urlRules = this.shiroConfiguration.getUrls();
        UserTokenPayload data = this.getUserInfo(req);
        if (urlRules != null) {
            LOG.debug("try to find rules for user {} with roles {}",
                    data == null ? "null" : data.getPreferredUsername(), data == null ? "null" : data.getRoles());
            final String regex = "^([^,]+)[,]?[\\ ]?([anyroles]+)?(\\[\"?([a-zA-Z,]+)\"?\\])?";
            final Pattern pattern = Pattern.compile(regex);
            Matcher matcher;
            for (UrlItem urlRule : urlRules) {
                matcher = pattern.matcher(urlRule.getPairValue());
                if (matcher.find()) {
                    try {
                        final String authClass = getAuthClass(matcher.group(1));
                        Optional<OdlPolicy> policy = Optional.empty();
                        //anon access allowed
                        if (authClass == null) {
                            policy = Optional.of(OdlPolicy.allowAll(urlRule.getPairKey()));
                        } else if (authClass.equals(CLASSNAME_ODLBASICAUTH) || "authcBasic".equals(urlRule.getPairKey())) {
                            policy = isBasic(req) ? this.getTokenBasedPolicy(urlRule, matcher, data)
                                    : Optional.of(OdlPolicy.denyAll(urlRule.getPairKey()));
                        } else if (authClass.equals(CLASSNAME_ODLBEARERANDBASICAUTH)) {
                            policy = this.getTokenBasedPolicy(urlRule, matcher, data);
                        } else if (authClass.equals(CLASSNAME_ODLMDSALAUTH)) {
                            policy = this.getMdSalBasedPolicy(urlRule, data);
                        }
                        if (policy.isPresent()) {
                            policies.add(policy.get());
                        } else {
                            LOG.warn("unable to get policy for authClass {} for entry {}", authClass,
                                    urlRule.getPairValue());
                            policies.add(OdlPolicy.denyAll(urlRule.getPairKey()));
                        }
                    } catch (NoDefinitionFoundException e) {
                        LOG.warn("unknown authClass: ", e);
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

    /**
     * extract policy rule for user from MD-SAL not yet supported
     *
     * @param urlRule
     * @param data
     * @return
     */
    private Optional<OdlPolicy> getMdSalBasedPolicy(UrlItem urlRule, UserTokenPayload data) {
        if (mdsalAuthStore != null) {
            return data != null ? mdsalAuthStore.getPolicy(urlRule.getPairKey(), data.getRoles())
                    : Optional.of(OdlPolicy.denyAll(urlRule.getPairKey()));
        }
        return Optional.empty();
    }

    /**
     * extract policy rule for user from url rules of config
     *
     * @param urlRule
     * @param matcher
     * @param data
     * @return
     */
    private Optional<OdlPolicy> getTokenBasedPolicy(UrlItem urlRule, Matcher matcher,
                                                    UserTokenPayload data) {
        final String url = urlRule.getPairKey();
        final String rule = urlRule.getPairValue();
        if (!rule.contains(",")) {
            LOG.debug("found rule without roles for '{}'", matcher.group(1));
            //not important if anon or authcXXX
            if (data != null || "anon".equals(matcher.group(1))) {
                return Optional.of(OdlPolicy.allowAll(url));
            }
        }
        if (data != null) {
            LOG.debug("found rule with roles '{}'", matcher.group(4));
            if ("roles".equals(matcher.group(2))) {
                if (this.rolesMatch(data.getRoles(), Arrays.asList(matcher.group(4).split(",")), false)) {
                    return Optional.of(OdlPolicy.allowAll(url));
                } else {
                    return Optional.of(OdlPolicy.denyAll(url));
                }
            } else if ("anyroles".equals(matcher.group(2))) {
                if (this.rolesMatch(data.getRoles(), Arrays.asList(matcher.group(4).split(",")), true)) {
                    return Optional.of(OdlPolicy.allowAll(url));
                } else {
                    return Optional.of(OdlPolicy.denyAll(url));
                }
            } else {
                LOG.warn("unable to detect url role value: {}", urlRule.getPairValue());
            }
        } else {
            return Optional.of(OdlPolicy.denyAll(url));
        }
        return Optional.empty();
    }

    private String getAuthClass(String key) throws NoDefinitionFoundException {
        if ("anon".equals(key)) {
            return null;
        }
        if("authcBasic".equals(key)){
            return CLASSNAME_ODLBASICAUTH;
        }
        List<MainItem> list = shiroConfiguration.getMain();
        Optional<MainItem> main =
                list == null ? Optional.empty() : list.stream().filter(e -> e.getPairKey().equals(key)).findFirst();
        if (main.isPresent()) {
            return main.get().getPairValue();
        }
        throw new NoDefinitionFoundException("unable to find def for " + key);
    }

    private UserTokenPayload getUserInfo(HttpServletRequest req) {
        if (isBearer(req)) {
            UserTokenPayload data = this.tokenCreator.decode(req);
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
                List<String> roles = List.of();// odlIdentityService.listRoles(username, domain);
                return UserTokenPayload.createInternal(username, roles);
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
        return header != null && header.startsWith("Basic");
    }

    private static boolean isBearer(HttpServletRequest req) {
        final String header = req.getHeader(HEAEDER_AUTHORIZATION);
        return header != null && header.startsWith("Bearer");
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

    public String getHost(HttpServletRequest req) {
        String hostUrl = this.config.getPublicUrl();
        if (hostUrl == null) {
            final String tmp = req.getRequestURL().toString();
            final String regex = "^(http[s]{0,1}:\\/\\/[^\\/]+)";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            final Matcher matcher = pattern.matcher(tmp);
            if (matcher.find()) {
                hostUrl = matcher.group(1);
            }
        }
        LOG.info("host={}", hostUrl);
        return hostUrl;

    }

    private List<PublicOAuthProviderConfig> getConfigs(Collection<AuthService> values) {
        List<PublicOAuthProviderConfig> configs = new ArrayList<>();
        for (AuthService svc : values) {
            configs.add(svc.getConfig());
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
                //provider.setLocalHostUrl(getHost(req));
                provider.handleRedirect(req, resp, getHost(req));
                return;
            }
        }
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        LOG.debug("POST request for {}", req.getRequestURI());
        if (this.config.loginActive() && this.config.doSupportOdlUsers() && LOGINURI.equals(req.getRequestURI())) {
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
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    private BearerToken doLogin(String username, String password, String domain) {

        PasswordCredentials pc =
                (new PasswordCredentialBuilder()).setUserName(username).setPassword(password).setDomain(domain).build();
        Claim claim = null;
        try {
            claim = this.passwordCredentialAuth.authenticate(pc);
        } catch (AuthenticationException e) {
            LOG.warn("unable to authentication user {} for domain {}: ", username, domain, e);
        }
        if (claim != null) {
            List<String> roles = claim.roles().stream().toList();//odlIdentityService.listRoles(username, domain);
            UserTokenPayload data = new UserTokenPayload();
            data.setPreferredUsername(username);
            data.setFamilyName("");
            data.setGivenName(username);
            data.setIat(this.tokenCreator.getDefaultIat());
            data.setExp(this.tokenCreator.getDefaultExp());
            data.setRoles(roles);
            return this.tokenCreator.createNewJWT(data);
        } else {
            LOG.info("unable to read auth from authservice");
        }
        return null;
    }


/*    private void sendResponse(HttpServletResponse resp, int code) throws IOException {
        this.sendResponse(resp, code, null);
    }*/

    private void sendResponse(HttpServletResponse resp, int code, Object data) throws IOException {
        byte[] output = data != null ? mapper.writeValueAsString(data).getBytes() : new byte[0];
        // output
        resp.setStatus(code);
        resp.setContentLength(output.length);
        resp.setContentType("application/json");
        ServletOutputStream os = resp.getOutputStream();
        os.write(output);

    }

    private void logout() {
       /* final Subject subject = SecurityUtils.getSubject();
        try {
            subject.logout();
            Session session = subject.getSession(false);
            if (session != null) {
                session.stop();
            }
        } catch (ShiroException e) {
            LOG.debug("Couldn't log out {}", subject, e);
        }*/
    }
}
