/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.http.client;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingBaseHttpClient extends BaseHTTPClient {

    private static Logger LOG = LoggerFactory.getLogger(MappingBaseHttpClient.class);

    public MappingBaseHttpClient(String base, boolean trustAllCerts) {
        super(base, trustAllCerts);
    }

    public MappingBaseHttpClient(String host) {
        super(host);
    }

    public <T> Optional<MappedBaseHttpResponse<String>> sendMappedRequest(String uri, String method, String body,
            Map<String, String> headers) {
        return this.sendMappedRequest(uri, method, body != null ? body.getBytes(CHARSET) : null, headers, String.class);
    }

    public <T> Optional<MappedBaseHttpResponse<T>> sendMappedRequest(String uri, String method, String body,
            Map<String, String> headers, Class<T> clazz) {
        return this.sendMappedRequest(uri, method, body != null ? body.getBytes(CHARSET) : null, headers, clazz);
    }

    protected <T> Optional<MappedBaseHttpResponse<T>> sendMappedRequest(String uri, String method, byte[] body,
            Map<String, String> headers, Class<T> clazz) {
        try {
            return Optional.of(new MappedBaseHttpResponse<T>(this.sendRequest(uri, method, body, headers), clazz));
        } catch (IOException e) {
            LOG.warn("problem during request for {}: ", uri, e);
        }
        return Optional.empty();
    }

}
