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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.mapperextensions;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.ScalarTypeObject;
//import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.ScalarTypeObject;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.YangHelper2;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.YangToolsMapperHelper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.serialize.BaseIdentityDeserializer;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.serialize.ClassDeserializer;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.serialize.IdentifierDeserializer;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.serialize.TypeObjectDeserializer;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YangToolsDeserializerModifier extends BeanDeserializerModifier {

    private static final Logger LOG = LoggerFactory.getLogger(YangToolsDeserializerModifier.class);
    private static final String getEnumMethodName="valueOf";

    @Override
    public JsonDeserializer<Enum<?>> modifyEnumDeserializer(DeserializationConfig config, final JavaType type,
            BeanDescription beanDesc, final JsonDeserializer<?> deserializer) {
        return new JsonDeserializer<Enum<?>>() {

            @Override
            public Enum<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
                Class<?> clazz = type.getRawClass();

                try {
                    Method method = clazz.getDeclaredMethod(getEnumMethodName, String.class);
                    Enum<?> result = (Enum<?>) method.invoke(null, jp.getValueAsString());
                    LOG.debug("Deserialize '{}' with class '{}' to '{}'", jp.getValueAsString(), clazz.getName(), result);
                    return result;
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
        } else if (YangToolsMapperHelper.implementsInterface(rawClass, YangHelper2.getScalarTypeObjectClass())) {
            deser = new TypeObjectDeserializer<ScalarTypeObject>(type, deser);
        } else if (YangToolsMapperHelper.implementsInterface(rawClass, BaseIdentity.class)) {
            deser = new BaseIdentityDeserializer<BaseIdentity>(deser);
        } else if (rawClass.equals(Class.class)) {
            deser = new ClassDeserializer(rawClass);
        }

        LOG.debug("Deserialize '{}' with deserializer '{}'", rawClass.getName(), deser.getClass().getName());
        return deser;
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

    void test() {
        com.fasterxml.jackson.databind.util.ClassUtil xy;
    }
}
