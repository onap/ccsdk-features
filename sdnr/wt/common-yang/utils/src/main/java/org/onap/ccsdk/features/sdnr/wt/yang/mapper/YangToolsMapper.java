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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.mapperextensions.YangToolsBuilderAnnotationIntrospector;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.mapperextensions.YangToolsModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YangToolsMapper is a specific Jackson mapper configuration for opendaylight yangtools serialization or
 * deserialization of DataObject to/from JSON TODO ChoiceIn and Credentials deserialization only for
 * LoginPasswordBuilder
 */
public class YangToolsMapper extends ObjectMapper {

    @SuppressWarnings("unused")
    private final Logger LOG = LoggerFactory.getLogger(YangToolsMapper.class);
    private final YangToolsBuilderAnnotationIntrospector annotationIntrospector;
    private final YangToolsModule module;
    private static final long serialVersionUID = 1L;
    private boolean isModuleRegistered = false;

    public YangToolsMapper() {
        this(new YangToolsBuilderAnnotationIntrospector());
    }

    protected YangToolsMapper(YangToolsBuilderAnnotationIntrospector yangToolsBuilderAnnotationIntrospector) {
        super();

        this.annotationIntrospector = yangToolsBuilderAnnotationIntrospector;
        this.module = new YangToolsModule();
        this.registerModule(this.module);
        this.registerModule(new JavaTimeModule());
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
        setSerializationInclusion(Include.NON_NULL);
        enable(MapperFeature.USE_GETTERS_AS_SETTERS);
        setAnnotationIntrospector(yangToolsBuilderAnnotationIntrospector);
    }

    public void addDeserializer(Class<?> clsToDeserialize, String builderClassName) {
        this.annotationIntrospector.addDeserializer(clsToDeserialize, builderClassName);
    }

    public void addKeyDeserializer(Class<?> type, KeyDeserializer deserializer) {
        this.module.addKeyDeserializer(type, deserializer);
    }

    @Override
    public <T> T readValue(String content, Class<T> valueType) throws JsonProcessingException, JsonMappingException {
        if (!this.isModuleRegistered) {
            this.registerModule(this.module);
            this.isModuleRegistered = true;
        }
        return super.readValue(content, valueType);
    }

    @Override
    public String writeValueAsString(Object value) throws JsonProcessingException {
        if (!this.isModuleRegistered) {
            this.registerModule(this.module);
            this.isModuleRegistered = true;
        }
        return super.writeValueAsString(value);
    }

}
