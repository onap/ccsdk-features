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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPResponse;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.CustomObjectMapper;

public class MappedBaseHttpResponse<T> {


    public static final int CODE404 = 404;
    public static final int CODE200 = 200;
    public static final MappedBaseHttpResponse<String> UNKNOWN = new MappedBaseHttpResponse<>(-1, null);
    private static final ObjectMapper mapper = new CustomObjectMapper();
    public final int code;
    public final T body;

    public MappedBaseHttpResponse(int code, String body, Class<T> clazz)
            throws JsonMappingException, JsonProcessingException {
        this(code, body != null ? mapper.readValue(body, clazz) : null);
    }

    private MappedBaseHttpResponse(int code, T body) {
        this.code = code;
        this.body = body;
    }

    public MappedBaseHttpResponse(BaseHTTPResponse response, Class<T> clazz)
            throws JsonMappingException, JsonProcessingException {
        this(response.code, response.body, clazz);
    }

    @Override
    public String toString() {
        return "BaseHTTPResponse [code=" + code + ", body=" + body + "]";
    }

    public boolean isSuccess() {
        return this.code == CODE200;
    }
}
