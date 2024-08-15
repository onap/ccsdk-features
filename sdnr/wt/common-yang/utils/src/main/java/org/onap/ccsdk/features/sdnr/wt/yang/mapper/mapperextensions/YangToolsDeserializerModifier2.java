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
package org.onap.ccsdk.features.sdnr.wt.yang.mapper.mapperextensions;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.type.MapType;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.BaseIdentityDeserializer;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.ClassDeserializer;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.IdentifierDeserializer;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.TypeObjectDeserializer;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.ScalarTypeObject;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YangToolsDeserializerModifier2 extends BeanDeserializerModifier {

    private static final Logger LOG = LoggerFactory.getLogger(YangToolsDeserializerModifier2.class);
    private static final String getEnumMethodName = "valueOf";
    private static final String getEnumMethodName2 = "forName";

    @SuppressWarnings("unchecked")
    public static Enum<?> parseEnum(String value, Class<?> clazz) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        try {
            Method method = clazz.getDeclaredMethod(getEnumMethodName, String.class);
            Enum<?> result = (Enum<?>) method.invoke(null, value);
            LOG.debug("Deserialize '{}' with class '{}' to '{}'", value, clazz.getName(), result);
            return result;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | NoSuchElementException | SecurityException e) {
            Method method = clazz.getDeclaredMethod(getEnumMethodName2, String.class);
            Optional<Enum<?>> result = (Optional<Enum<?>>) method.invoke(null, value);
            LOG.debug("Deserialize '{}' with class '{}' to '{}'", value, clazz.getName(), result);
            return result.orElseThrow();
        }
    }

    @Override
    public JsonDeserializer<Enum<?>> modifyEnumDeserializer(DeserializationConfig config, final JavaType type,
            BeanDescription beanDesc, final JsonDeserializer<?> deserializer) {
        return new JsonDeserializer<Enum<?>>() {

            @Override
            public Enum<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                Class<?> clazz = type.getRawClass();

                try {
                    return parseEnum(jp.getValueAsString(), clazz);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | NoSuchElementException | SecurityException e) {
                    LOG.warn("problem deserializing enum for {} with value {}: {}", clazz.getName(),
                            jp.getValueAsString(), e);
                }
                throw new IOException(
                        "unable to parse enum (" + type.getRawClass() + ")for value " + jp.getValueAsString());
            }
        };
    }

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
            JsonDeserializer<?> deserializer) {
        final JavaType type = beanDesc.getType();
        final Class<?> rawClass = type.getRawClass();

        JsonDeserializer<?> deser = super.modifyDeserializer(config, beanDesc, deserializer);

        if (YangToolsMapperHelper.implementsInterface(rawClass, TypeObject.class)) {
            deser = new TypeObjectDeserializer<TypeObject>(type, deser);
        } else if (YangToolsMapperHelper.implementsInterface(rawClass, ScalarTypeObject.class)) {
            deser = new TypeObjectDeserializer<ScalarTypeObject<?>>(type, deser);
        } else if (YangToolsMapperHelper.implementsInterface(rawClass, BaseIdentity.class)) {
            deser = new BaseIdentityDeserializer<BaseIdentity>(deser);
        } else if (rawClass.equals(Class.class)) {
            deser = new ClassDeserializer(rawClass);
        }

        LOG.debug("Deserialize '{}' with deserializer '{}'", rawClass.getName(), deser.getClass().getName());
        return deser;
    }

    @Override
    public JsonDeserializer<?> modifyMapDeserializer(DeserializationConfig config, MapType type,
            BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        final Class<?> rawClass = type.getBindings().getBoundType(1).getRawClass();
        return new YangtoolsMapDesirializer(rawClass);
    }

    @Override
    public KeyDeserializer modifyKeyDeserializer(DeserializationConfig config, JavaType type, KeyDeserializer deser) {
        KeyDeserializer res;
        if (YangToolsMapperHelper.implementsInterface(type.getRawClass(), Identifier.class)) {
            res = new IdentifierDeserializer();
        } else {
            res = super.modifyKeyDeserializer(config, type, deser);
        }
        LOG.debug("Keydeserialize '{}' with deserializer '{}'", type.getRawClass().getName(), res.getClass().getName());
        return res;
    }
}
