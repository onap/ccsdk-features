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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools;

import java.io.IOException;
import javax.annotation.Nullable;

import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.credentials.Credentials;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder.Value;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;

/**
 * YangToolsMapper is a specific Jackson mapper configuration for opendaylight yangtools serialization or
 * deserialization of DataObject to/from JSON TODO ChoiceIn and Credentials deserialization only for
 * LoginPasswordBuilder
 */
public class YangToolsMapper2<T extends DataObject> extends ObjectMapper {

    private final Logger LOG = LoggerFactory.getLogger(YangToolsMapper2.class);
    private static final long serialVersionUID = 1L;
    private static String BUILDER = "Builder";

    private @Nullable final Class<T> clazz;
    private @Nullable final Class<? extends Builder<? extends T>> builderClazz;

    private BundleContext context;

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
    public <X extends T, B extends Builder<X>> YangToolsMapper2(@NonNull Class<T> clazz,
            @Nullable Class<B> builderClazz) throws ClassNotFoundException {
        super();
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        setSerializationInclusion(Include.NON_NULL);
        setAnnotationIntrospector(new YangToolsBuilderAnnotationIntrospector());
        SimpleModule dateAndTimeSerializerModule = new SimpleModule();
        dateAndTimeSerializerModule.addSerializer(DateAndTime.class, new CustomDateAndTimeSerializer());
        registerModule(dateAndTimeSerializerModule);
        Bundle bundle = FrameworkUtil.getBundle(YangToolsMapper2.class);

        this.clazz = clazz;
        this.builderClazz = builderClazz != null ? builderClazz : getBuilderClass(getBuilderClassName(clazz));
        context = bundle != null ? bundle.getBundleContext() : null;
    }

    @Override
    public String writeValueAsString(Object value) throws JsonProcessingException {
        return super.writeValueAsString(value);
    }

    /**
     * Get Builder object for yang tools interface.
     * 
     * @param <T> yang-tools base datatype
     * @param clazz class with interface.
     * @return builder for interface or null if not existing
     */
    public @Nullable Builder<? extends T> getBuilder(Class<T> clazz) {
        try {
            if (builderClazz != null)
                return (Builder<? extends T>) builderClazz.newInstance();
            else
                return null;
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.debug("Problem ", e);
            return null;
        }
    }

    /**
     * Callback for handling mapping failures.
     * 
     * @return
     */
    public int getMappingFailures() {
        return 0;
    }

    /**
     * Provide mapping of string to attribute names, generated by yang-tools. "netconf-id" converted to "_netconfId"
     * 
     * @param name with attribute name, not null or empty
     * @return converted string or null if name was empty or null
     */
    public @Nullable static String toCamelCaseAttributeName(final String name) {
        if (name == null || name.isEmpty())
            return null;

        final StringBuilder ret = new StringBuilder(name.length());
        if (!name.startsWith("_"))
            ret.append('_');
        int start = 0;
        for (final String word : name.split("-")) {
            if (!word.isEmpty()) {
                if (start++ == 0) {
                    ret.append(Character.toLowerCase(word.charAt(0)));
                } else {
                    ret.append(Character.toUpperCase(word.charAt(0)));
                }
                ret.append(word.substring(1));
            }
        }
        return ret.toString();
    }

    /**
     * Verify if builder is available
     * 
     * @throws ClassNotFoundException
     **/
    public Class<?> assertBuilderClass(Class<?> clazz) throws ClassNotFoundException {
        return getBuilderClass(getBuilderClassName(clazz));
    }

    // --- Private functions

    /**
     * Create name of builder class
     * 
     * @param <T>
     * @param clazz
     * @return builders class name
     * @throws ClassNotFoundException
     */
    private static String getBuilderClassName(Class<?> clazz) {
        return clazz.getName() + BUILDER;
    }

    /**
     * Search builder in context
     * 
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private <X extends T, B extends Builder<X>> Class<B> getBuilderClass(String name) throws ClassNotFoundException {
        // Try to find in other bundles
        if (context != null) {
            //OSGi environment
            for (Bundle b : context.getBundles()) {
                try {
                    return (Class<B>) b.loadClass(name);
                } catch (ClassNotFoundException e) {
                    // No problem, this bundle doesn't have the class
                }
            }
            throw new ClassNotFoundException("Can not find Class in OSGi context.");
        } else {
            return (Class<B>) Class.forName(name);
        }
        // not found in any bundle
    }

    // --- Classes

    /**
     * Adapted Builder callbacks
     */
    private class YangToolsBuilderAnnotationIntrospector extends JacksonAnnotationIntrospector {
        private static final long serialVersionUID = 1L;

        @Override
        public Class<?> findPOJOBuilder(AnnotatedClass ac) {

            if (ac.getRawType().equals(Credentials.class)) {
                return org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.credentials.credentials.LoginPasswordBuilder.class;

            } else if (ac.getRawType().equals(DateAndTime.class)) {
                return DateAndTimeBuilder.class;

            } else if (ac.getRawType().equals(clazz)) {
                return builderClazz;
            }

            if (ac.getRawType().isInterface()) {
                String builder = getBuilderClassName(ac.getRawType());
                try {
                    Class<?> innerBuilder = getBuilderClass(builder);
                    return innerBuilder;
                } catch (ClassNotFoundException e) {
                    // No problem .. try next
                }
            }
            return super.findPOJOBuilder(ac);
        }

        @Override
        public Value findPOJOBuilderConfig(AnnotatedClass ac) {
            if (ac.hasAnnotation(JsonPOJOBuilder.class)) {
                return super.findPOJOBuilderConfig(ac);
            }
            return new JsonPOJOBuilder.Value("build", "set");
        }
    }

    public static class DateAndTimeBuilder {

        private final String _value;

        public DateAndTimeBuilder(String v) {
            this._value = v;
        }

        public DateAndTime build() {
            return new DateAndTime(_value);
        }

    }
    public static class CustomDateAndTimeSerializer extends StdSerializer<@NonNull DateAndTime> {

        private static final long serialVersionUID = 1L;

        public CustomDateAndTimeSerializer() {
            this(null);
        }

        protected CustomDateAndTimeSerializer(Class<DateAndTime> t) {
            super(t);
        }

        @Override
        public void serialize(DateAndTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getValue());
        }

    }
}
