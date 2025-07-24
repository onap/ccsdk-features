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
package org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseIdentityDeserializer<T> extends JsonDeserializer<T> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseIdentityDeserializer.class);
    private final JsonDeserializer<?> deser;

    public BaseIdentityDeserializer(final JsonDeserializer<?> deser) {
        this.deser = deser;
    }

    private static List<Class<? extends BaseIdentity>> getTypesInNamespace(String packageName) throws IOException {
        return ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClasses(packageName).
                stream().map(e -> (Class<? extends BaseIdentity>)e.load()).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        LOG.debug("BaseIdentityDeserializer class for '{}'", parser.getValueAsString());
        String clazzToSearch = parser.getValueAsString();
        String simpleName;
        Class<? extends BaseIdentity> clazz;
        // clazz from Elasticsearch is full qualified
        int lastDot = clazzToSearch.lastIndexOf(".");
        if (lastDot > -1) {
            simpleName = clazzToSearch.substring(lastDot + 1);
            String finalSimpleName;
            if(simpleName.endsWith("Identity")){
                finalSimpleName = simpleName.substring(0, simpleName.length()-8);
            }
            else{
                finalSimpleName=simpleName;
            }
            clazz = getTypesInNamespace(clazzToSearch.substring(0, lastDot)).stream()
                    .filter(e -> e.getSimpleName().equals(finalSimpleName)).findFirst().orElse(null);
            if (clazz != null)
                return (T) YangToolsMapperHelper.getIdentityValueFromClass(clazz);
        } else {
            simpleName = clazzToSearch.substring(0, 1).toUpperCase() + clazzToSearch.substring(1);
            if(simpleName.endsWith("Identity")){
                simpleName = simpleName.substring(0, simpleName.length()-8);
            }
        }

        try {
            clazz = (Class<? extends BaseIdentity>) YangToolsMapperHelper.findClass(simpleName);
            if (clazz != null)
                return (T) YangToolsMapperHelper.getIdentityValueFromClass(clazz);
        } catch (ClassNotFoundException e) {
            LOG.warn("BaseIdentityDeserializer class not found for '" + parser.getValueAsString() + "'", e);
        }
        return (T) deser.deserialize(parser, ctxt);
    }
}
