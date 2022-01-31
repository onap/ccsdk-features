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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RSAKeyReader {

    private static final String PREFIX_FILEURL = "file://";

    public static RSAPrivateKey getPrivateKey(String filenameOrContent) throws IOException {
        if (filenameOrContent.startsWith(PREFIX_FILEURL)) {
            return (RSAPrivateKey) PemUtils.readPrivateKeyFromFile(filenameOrContent.substring(PREFIX_FILEURL.length()),
                    "RSA");
        }
        return (RSAPrivateKey) PemUtils.readPrivateKey(filenameOrContent, "RSA");
    }

    public static RSAPublicKey getPublicKey(String filenameOrContent) throws IOException {
        if (filenameOrContent.startsWith(PREFIX_FILEURL)) {
            return (RSAPublicKey) PemUtils.readPublicKeyFromFile(filenameOrContent.substring(PREFIX_FILEURL.length()),
                    "RSA");
        }
        return (RSAPublicKey) PemUtils.readPublicKey(filenameOrContent, "RSA");
    }
}
