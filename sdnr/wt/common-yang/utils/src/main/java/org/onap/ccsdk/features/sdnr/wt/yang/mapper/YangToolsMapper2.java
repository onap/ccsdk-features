/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.yang.mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Nullable;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.mapperextensions.YangToolsBuilderAnnotationIntrospector;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YangToolsMapper is a specific Jackson mapper configuration for opendaylight yangtools serialization or
 * deserialization of DataObject to/from JSON TODO ChoiceIn and Credentials deserialization only for
 * LoginPasswordBuilder
 */
public class YangToolsMapper2<T extends DataObject> extends YangToolsMapper {

    private static final Logger LOG = LoggerFactory.getLogger(YangToolsMapper2.class);
    private static final long serialVersionUID = 1L;

    private @Nullable final Class<?> builderClazz;

    /**
     * Generic Object creation of yangtools java class builder pattern.
     *
     * @param <X> Class of DataObject
     * @param <B> Builder for the class.
     * @param clazz specifies class to be mapped
     * @param builderClazz is the builder for class with name pattern "clazzBuilder".<br>
     *        If null the clazz is expected to support normal jackson build pattern.
     * @throws ClassNotFoundException if builderClazz not available in bundle
     */
    public <X extends T, B> YangToolsMapper2(@NonNull Class<T> clazz,
            @Nullable Class<B> builderClazz) throws ClassNotFoundException {
        super(new YangToolsBuilderAnnotationIntrospector(clazz, builderClazz));

        this.builderClazz =
                builderClazz != null ? builderClazz : getBuilderClass(YangToolsMapperHelper.getBuilderClassName(clazz));
    }

    /**
     * Get Builder object for yang tools interface.
     *
     * @param <T> yang-tools base datatype
     * @param clazz class with interface.
     * @return builder for interface or null if not existing
     */
    public @Nullable<B> B getBuilder(Class<T> clazz) {
        try {
            if (builderClazz != null)
                return (B) builderClazz.getDeclaredConstructor().newInstance();
            else
                return null;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOG.warn("Problem intantiating Builder", e);
            return null;
        }
    }

    // --- Private functions

    /**
     * Search builder in context
     *
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private <X extends T, B> Class<B> getBuilderClass(String name) throws ClassNotFoundException {
        return (Class<B>) YangToolsMapperHelper.getBuilderClass(name);
    }

   
}
