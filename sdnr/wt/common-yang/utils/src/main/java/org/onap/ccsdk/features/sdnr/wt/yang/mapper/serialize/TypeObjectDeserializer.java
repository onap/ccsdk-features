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
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeObjectDeserializer<T> extends JsonDeserializer<T> {

    private static final Logger LOG = LoggerFactory.getLogger(TypeObjectDeserializer.class);
    private final JavaType type;
    private final JsonDeserializer<?> deser;


    public TypeObjectDeserializer(final JavaType type, final JsonDeserializer<?> deser) {
        this.type = type;
        this.deser = deser;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        Class<T> clazz = (Class<T>) type.getRawClass();
        final String arg = parser.getValueAsString();
        LOG.debug("Try to build arg:'{}' with class {}",arg, clazz);
        Optional<T> oRes = Optional.empty();
        try {
            //try get method for default instance
            if ((oRes = YangToolsMapperHelper.getDefaultInstance(clazz, arg)).isEmpty()) {
                //try to find builder with getDefaultInstance method
                Optional<Class<?>> oBuilderClazz = YangToolsMapperHelper.findBuilderClassOptional(ctxt, clazz);
                LOG.debug("Try builder class present:{}",oBuilderClazz.isPresent());
                if (oBuilderClazz.isEmpty()
                        || ((oRes = YangToolsMapperHelper.getDefaultInstance(oBuilderClazz.get(), arg)).isEmpty())) {
                    //try to find constructor with string
                    LOG.debug("Try constructor");
                    if ((oRes = YangToolsMapperHelper.getInstanceByConstructor(clazz, arg)).isEmpty()) {
                        //forward to standard deserializer or throw if not available
                        LOG.debug("Try default deserializer");
                        oRes = Optional.of((T) deser.deserialize(parser, ctxt));
                    }
                }
            }
            LOG.debug("Deserialize string value:{} for class:{} success:{}", arg, clazz, oRes.isPresent());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | NoSuchElementException | SecurityException | InstantiationException e) {
            LOG.warn("problem deserializing {} with value {}: {}", clazz.getName(), arg, e);
        }
        if (oRes.isPresent()) {
            return oRes.get();
        } else {
            throw new IllegalArgumentException("Could not find constructor for arg:'" + arg + "' and class: " + clazz);
        }
    }

}
