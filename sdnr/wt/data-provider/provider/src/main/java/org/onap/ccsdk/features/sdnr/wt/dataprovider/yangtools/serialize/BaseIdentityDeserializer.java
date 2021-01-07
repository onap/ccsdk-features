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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.YangToolsMapperHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseIdentityDeserializer<T> extends JsonDeserializer<T> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseIdentityDeserializer.class);
    private final JsonDeserializer<?> deser;

    public BaseIdentityDeserializer(final JsonDeserializer<?> deser) {
        this.deser = deser;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        LOG.debug("BaseIdentityDeserializer class for '{}'",parser.getValueAsString());
        String clazzToSearch = parser.getValueAsString();
        // clazz from Elasticsearch is full qualified
        int lastDot = clazzToSearch.lastIndexOf(".");
        if (lastDot > -1) {
            clazzToSearch = clazzToSearch.substring(lastDot+1);
        } else {
            clazzToSearch = clazzToSearch.substring(0, 1).toUpperCase() + clazzToSearch.substring(1);
        }
        Class<?> clazz;
        try {
            clazz = YangToolsMapperHelper.findClass(clazzToSearch);
            if (clazz != null)
                return (T)clazz;
        } catch (ClassNotFoundException e) {
            LOG.warn("BaseIdentityDeserializer class not found for '"+parser.getValueAsString()+"'",e);
        }
        return (T) deser.deserialize(parser, ctxt);
    }
}
