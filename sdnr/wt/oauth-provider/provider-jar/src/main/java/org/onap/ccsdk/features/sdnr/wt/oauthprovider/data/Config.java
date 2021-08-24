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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

    private static final Logger LOG = LoggerFactory.getLogger(Config.class);
    private static final String DEFAULT_CONFIGFILENAME = "etc/oauth-provider.config.json";
    private static final String ENVVARIABLE = "${";
    private static final String REGEXENVVARIABLE = "(\\$\\{[A-Z0-9_-]+\\})";
    private static final Pattern pattern = Pattern.compile(REGEXENVVARIABLE);
    private static final String DEFAULT_TOKENISSUER = "Opendaylight";
    private static final String DEFAULT_TOKENSECRET = generateSecret();
    private static final String DEFAULT_REDIRECTURI = "/odlux/index.html#/oauth?token=";
    private static final String DEFAULT_SUPPORTODLUSERS = "true";
    private static Random random;
    private static Config _instance;

    private List<OAuthProviderConfig> providers;
    private String redirectUri;
    private String supportOdlUsers;
    private String tokenSecret;
    private String tokenIssuer;
    private String publicUrl;


    @Override
    public String toString() {
        return "Config [providers=" + providers + ", redirectUri=" + redirectUri + ", supportOdlUsers="
                + supportOdlUsers + ", tokenSecret=" + tokenSecret + ", tokenIssuer=" + tokenIssuer + "]";
    }



    public List<OAuthProviderConfig> getProviders() {
        return providers;
    }

    public void setProviders(List<OAuthProviderConfig> providers) {
        this.providers = providers;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getSupportOdlUsers() {
        return supportOdlUsers;
    }

    public void setSupportOdlUsers(String supportOdlUsers) {
        this.supportOdlUsers = supportOdlUsers;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public String getTokenIssuer() {
        return tokenIssuer;
    }

    public void setTokenIssuer(String tokenIssuer) {
        this.tokenIssuer = tokenIssuer;
    }


    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    @JsonIgnore
    private void handleEnvironmentVars() {
        if (isEnvExpression(tokenIssuer)) {
            this.tokenIssuer = getProperty(tokenIssuer, null);
        }
        if (isEnvExpression(tokenSecret)) {
            this.tokenSecret = getProperty(tokenSecret, null);
        }
        if (isEnvExpression(publicUrl)) {
            this.publicUrl = getProperty(publicUrl, null);
        }
        if (isEnvExpression(redirectUri)) {
            this.redirectUri = getProperty(redirectUri, null);
        }
        if (isEnvExpression(supportOdlUsers)) {
            this.supportOdlUsers = getProperty(supportOdlUsers, null);
        }
        if (this.providers != null && !this.providers.isEmpty()) {
            for(OAuthProviderConfig cfg : this.providers) {
                cfg.handleEnvironmentVars();
            }
        }
    }

    @JsonIgnore
    private void handleDefaultValues() {
        if (tokenIssuer == null || tokenIssuer.isEmpty()) {
            this.tokenIssuer = DEFAULT_TOKENISSUER;
        }
        if (tokenSecret == null || tokenSecret.isEmpty()) {
            this.tokenSecret = DEFAULT_TOKENSECRET;
        }
        if (redirectUri == null || redirectUri.isEmpty() || "null".equals(redirectUri)) {
            this.redirectUri = DEFAULT_REDIRECTURI;
        }
        if (publicUrl != null && (publicUrl.isEmpty() || "null".equals(publicUrl))) {
            this.publicUrl = null;
        }
        if (supportOdlUsers == null || supportOdlUsers.isEmpty()) {
            this.supportOdlUsers = DEFAULT_SUPPORTODLUSERS;
        }
    }

    static boolean isEnvExpression(String key) {
        return key != null && key.contains(ENVVARIABLE);
    }

    public static String generateSecret() {
        return generateSecret(30);
    }

    public static String generateSecret(int targetStringLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        if(random==null) {
            random = new Random();
        }
        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
        return generatedString;
    }

    /**
     *
     * @param key environment var
     * @param defValue default value if no env var found
     * @return
     */
    public static String getProperty(final String key, final String defValue) {
        String value = defValue;
        //try to read env var
        boolean found = false;
        if (isEnvExpression(key)) {

            LOG.info("try to find env var(s) for {}", key);
            final Matcher matcher = pattern.matcher(key);
            String tmp = new String(key);
            while (matcher.find() && matcher.groupCount() > 0) {
                final String mkey = matcher.group(1);
                if (mkey != null) {
                    try {
                        LOG.info("match found for v={} and env key={}", key, mkey);
                        String envvar = mkey.substring(2, mkey.length() - 1);
                        String env = System.getenv(envvar);
                        tmp = tmp.replace(mkey, env == null ? "" : env);
                        if (env != null && env.isEmpty()) {
                            found = true;
                        }
                    } catch (SecurityException e) {
                        LOG.warn("unable to read env {}: {}", key, e);
                    }
                }
            }
            if (found) {
                value = tmp;
            }
        }
        return value;
    }

    public static boolean getPropertyBoolean(String key, boolean defaultValue) {
        final String value = getProperty(key, String.valueOf(defaultValue));
        return value.equals("true");
    }

    public static Config load(String filename) throws IOException {
        CustomObjectMapper mapper = new CustomObjectMapper();
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        String content = String.join("", Files.readAllLines(file.toPath()));
        Config cfg = mapper.readValue(content, Config.class);
        cfg.handleEnvironmentVars();
        cfg.handleDefaultValues();
        return cfg;
    }


    @JsonIgnore
    public boolean doSupportOdlUsers() {
        return "true".equals(this.supportOdlUsers);
    }


    public static Config getInstance() throws IOException {
        return getInstance(DEFAULT_CONFIGFILENAME);
    }

    public static Config getInstance(String filename) throws IOException {
        if (_instance == null) {
            _instance = load(filename);
        }
        return _instance;
    }


}
