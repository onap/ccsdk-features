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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.test;

import static org.junit.Assert.fail;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.io.IOException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.RSAKeyReader;

/**
 *
 * @author jack
 *
 */
public class TestRSAAlgorithms {

    private static final String ISSUER = "jwttest";
    private static final String SUBJECT = "meandmymonkey";

    @BeforeClass
    public static void init() {
        Security.addProvider(
                new BouncyCastleProvider()
       );
    }

    /**
     * private and public key were generated in ubuntu 20.04 with
     * $ ssh-keygen -t rsa -b 4096 -m PEM -P "" -f jwtRS512.key
     * $ openssl rsa -in jwtRS512.key -pubout -outform PEM -out jwtRS512.key.pub
     */
    @Test
    public void testRSA512() {
        RSAPrivateKey privKey = null;
        RSAPublicKey pubKey = null;
        try {
            privKey = RSAKeyReader.getPrivateKey("file://src/test/resources/jwtRS512.key");
            pubKey = RSAKeyReader.getPublicKey("file://src/test/resources/jwtRS512.key.pub");
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        verifyAlg(Algorithm.RSA512(pubKey, privKey));
    }

    /**
     * private and public key were generated in ubuntu 20.04 with
     * $ openssl genrsa 2048 -out rsa-2048bit-jwtRS256.key
     * $ openssl rsa -in jwtRS256.key -pubout > jwtRS256.key.pub
     */
    @Test
    public void testRSA256() {
        RSAPrivateKey privKey = null;
        RSAPublicKey pubKey = null;
        try {
            privKey = RSAKeyReader.getPrivateKey("file://src/test/resources/jwtRS256.key");
            pubKey = RSAKeyReader.getPublicKey("file://src/test/resources/jwtRS256.key.pub");
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        verifyAlg(Algorithm.RSA512(pubKey, privKey));
    }

    private static void verifyAlg(Algorithm a) {
        long now = new Date().getTime();
        final String token = JWT.create().withIssuer(ISSUER).withExpiresAt(new Date(now+10000))
                .withIssuedAt(new Date(now))
                .withSubject(SUBJECT)
                .sign(a);
        try {
            JWTVerifier verifier = JWT.require(a).withIssuer(ISSUER).build();
            verifier.verify(token);

        } catch (JWTVerificationException e) {
            fail(e.getMessage());
        }
    }
}
