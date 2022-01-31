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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
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
    public static final String TOKENALG_HS256 = "HS256";
    public static final String TOKENALG_RS256 = "RS256";
    public static final String TOKENALG_RS512 = "RS512";
    private static final String CLIENTALG_PRE = "Client";
    public static final String TOKENALG_CLIENT_RS256 = CLIENTALG_PRE + TOKENALG_RS256;
    public static final String TOKENALG_CLIENT_RS512 = CLIENTALG_PRE + TOKENALG_RS512;
    private static final String DEFAULT_TOKEN_ALGORITHM = TOKENALG_HS256;

    private static final long DEFAULT_TOKEN_LIFETIME = 30 * 60;
    private static final List<String> VALID_ALGORITHMS =
            Arrays.asList(TOKENALG_HS256, TOKENALG_RS256, TOKENALG_RS512, TOKENALG_CLIENT_RS256, TOKENALG_CLIENT_RS512);
    private static final List<String> VALID_ALGORITHMS_FOR_INTERNAL_LOGIN =
            Arrays.asList(TOKENALG_HS256, TOKENALG_RS256, TOKENALG_RS512);
    private static SecureRandom random;
    private static Config _instance;

    private List<OAuthProviderConfig> providers;
    private String redirectUri;
    private String supportOdlUsers;
    private String tokenSecret;
    private String tokenPubKey;
    private String algorithm;
    private String tokenIssuer;
    private String publicUrl;
    private long tokenLifetime;

    @Override
    public String toString() {
        return "Config [providers=" + providers + ", redirectUri=" + redirectUri + ", supportOdlUsers="
                + supportOdlUsers + ", tokenSecret=***, tokenPubKey=" + tokenPubKey + ", algorithm=" + algorithm
                + ", tokenIssuer=" + tokenIssuer + ", publicUrl=" + publicUrl + ", tokenLifetime=" + tokenLifetime
                + "]";
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

    public String getAlgorithm() {
        return this.algorithm;
    }

    public void setAlgorithm(String alg) {
        this.algorithm = alg;
    }

    @JsonGetter("tokenPubKey")
    public String getPublicKey() {
        return this.tokenPubKey;
    }

    @JsonSetter("tokenPubKey")
    public void setPublicKey(String pubKey) {
        this.tokenPubKey = pubKey;
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

    public long getTokenLifetime() {
        return this.tokenLifetime;
    }

    public void setTokenLifetime(long lifetime) {
        this.tokenLifetime = lifetime;
    }

    @JsonIgnore
    private void handleEnvironmentVars() {
        if (isEnvExpression(this.tokenIssuer)) {
            this.tokenIssuer = getProperty(this.tokenIssuer, null);
        }
        if (isEnvExpression(this.tokenSecret)) {
            this.tokenSecret = getProperty(this.tokenSecret, null);
        }
        if (isEnvExpression(this.tokenPubKey)) {
            this.tokenPubKey = getProperty(this.tokenPubKey, null);
        }
        if (isEnvExpression(this.algorithm)) {
            this.algorithm = getProperty(this.algorithm, null);
        }
        if (isEnvExpression(this.publicUrl)) {
            this.publicUrl = getProperty(this.publicUrl, null);
        }
        if (isEnvExpression(this.redirectUri)) {
            this.redirectUri = getProperty(this.redirectUri, null);
        }
        if (isEnvExpression(this.supportOdlUsers)) {
            this.supportOdlUsers = getProperty(this.supportOdlUsers, null);
        }
        if (this.providers != null && !this.providers.isEmpty()) {
            for (OAuthProviderConfig cfg : this.providers) {
                cfg.handleEnvironmentVars();
            }
        }
    }

    @JsonIgnore
    private void handleDefaultValues() {
        if (this.tokenIssuer == null || this.tokenIssuer.isEmpty()) {
            this.tokenIssuer = DEFAULT_TOKENISSUER;
        }
        if (this.algorithm == null || this.algorithm.isEmpty()) {
            this.algorithm = DEFAULT_TOKEN_ALGORITHM;
        }
        if (TOKENALG_HS256.equals(this.algorithm) && (this.tokenSecret == null || this.tokenSecret.isEmpty())) {
            this.tokenSecret = DEFAULT_TOKENSECRET;
        }
        if (this.redirectUri == null || this.redirectUri.isEmpty() || "null".equals(this.redirectUri)) {
            this.redirectUri = DEFAULT_REDIRECTURI;
        }
        if (this.publicUrl != null && (this.publicUrl.isEmpty() || "null".equals(this.publicUrl))) {
            this.publicUrl = null;
        }
        if (this.supportOdlUsers == null || this.supportOdlUsers.isEmpty()) {
            this.supportOdlUsers = DEFAULT_SUPPORTODLUSERS;
        }
        if (this.tokenLifetime <= 0) {
            this.tokenLifetime = DEFAULT_TOKEN_LIFETIME;
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
        if (random == null) {
            random = new SecureRandom();
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
                        if (env != null && !env.isEmpty()) {
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

    public static Config load(String filename) throws IOException, InvalidConfigurationException {
        CustomObjectMapper mapper = new CustomObjectMapper();
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        String content = String.join("", Files.readAllLines(file.toPath()));
        Config cfg = mapper.readValue(content, Config.class);
        cfg.handleEnvironmentVars();
        cfg.handleDefaultValues();
        cfg.validate();
        return cfg;
    }


    @JsonIgnore
    private void validate() throws InvalidConfigurationException {
        //verify that algorithm is supported
        if (!VALID_ALGORITHMS.contains(this.algorithm)) {
            throw new InvalidConfigurationException(String.format("Algorithm '%s' is not supported ", this.algorithm));
        }
        //verify that set values are matching the algorithm
        //if hs256 check if secret is set
        if (this.algorithm.startsWith("HS")) {
            if (this.tokenSecret == null || this.tokenSecret.isBlank()) {
                throw new InvalidConfigurationException(
                        String.format("There is no secret set for algorithm '%s'", this.algorithm));
            }
        }
        //if rs256 or rs512 check if secret(private key) and pubkey are set
        if (this.algorithm.startsWith("RS")) {
            if (this.tokenSecret == null || this.tokenSecret.isBlank()) {
                throw new InvalidConfigurationException(
                        String.format("There is no secret set for algorithm '%s'", this.algorithm));
            }
            if (this.tokenPubKey == null || this.tokenPubKey.isBlank()) {
                throw new InvalidConfigurationException(
                        String.format("There is no public key for algorithm '%s'", this.algorithm));
            }
        }
        //if client rs256 or client rs512 check if pubkey are set
        if (this.algorithm.startsWith("Client")) {
            if (this.tokenPubKey == null || this.tokenPubKey.isBlank()) {
                throw new InvalidConfigurationException(
                        String.format("There is no public key for algorithm '%s'", this.algorithm));
            }
        }
    }

    @JsonIgnore
    public boolean doSupportOdlUsers() {
        return "true".equals(this.supportOdlUsers);
    }


    public static Config getInstance() throws IOException, InvalidConfigurationException {
        return getInstance(DEFAULT_CONFIGFILENAME);
    }

    public static Config getInstance(String filename) throws IOException, InvalidConfigurationException {
        if (_instance == null) {
            _instance = load(filename);
        }
        return _instance;
    }

    public boolean loginActive() {
        return VALID_ALGORITHMS_FOR_INTERNAL_LOGIN.contains(this.algorithm);
    }


}
