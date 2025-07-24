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

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder.Value;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.builder.DateAndTimeBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YangToolsBuilderAnnotationIntrospector extends JacksonAnnotationIntrospector {

    private static final Logger LOG = LoggerFactory.getLogger(YangToolsBuilderAnnotationIntrospector.class);
    private static final long serialVersionUID = 1L;

    private final Map<Class<?>, String> customDeserializer;

    public YangToolsBuilderAnnotationIntrospector() {
        this(null, null);
    }

    public YangToolsBuilderAnnotationIntrospector(Class<?> cls, Class<?> builderClass) {
        this.customDeserializer = new HashMap<>();
        if (cls != null && builderClass != null) {
            this.customDeserializer.put(cls, builderClass.getName());
        }
        this.customDeserializer.put(DateAndTime.class, DateAndTimeBuilder.class.getName());
        //this.customDeserializer.put(Credentials.class, LoginPasswordBuilder.class.getName());
    }

    @Override
    public Class<?> findPOJOBuilder(AnnotatedClass ac) {
        try {
            String builder = null;
            if (this.customDeserializer.containsKey(ac.getRawType())) {
                builder = this.customDeserializer.get(ac.getRawType());
            } else {
                if (ac.getRawType().isInterface()) {
                    builder = ac.getName() + "Builder";
                }
            }
            if (builder != null) {
                LOG.trace("map {} with builder {}", ac.getName(), builder);
                Class<?> innerBuilder = YangToolsMapperHelper.findClass(builder);
                return innerBuilder;
            }
        } catch (ClassNotFoundException e) {
            LOG.trace("builder class not found for {}", ac.getName());
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

    @Override
    public AnnotatedMethod resolveSetterConflict(MapperConfig<?> config, AnnotatedMethod setter1,
            AnnotatedMethod setter2) {
        Class<?> p1 = setter1.getRawParameterType(0);
        Class<?> p2 = setter2.getRawParameterType(0);
        AnnotatedMethod res = null;

        if (isAssignable(p1, p2, Map.class, List.class)) {
			res = p1.isAssignableFrom(List.class) ? setter1 : setter2; //prefer List setter
        } else if (isAssignable(p1, p2, Uint64.class, BigInteger.class)) {
            res = setter1;
        } else if (isAssignable(p1, p2, Uint32.class, Long.class)) {
            res = setter1;
        } else if (isAssignable(p1, p2, Uint16.class, Integer.class)) {
            res = setter1;
        } else if (isAssignable(p1, p2, Uint8.class, Short.class)) {
            res = setter1;
        }
        if (res == null) {
            res = super.resolveSetterConflict(config, setter1, setter2);
        }
        if(res ==null) {
            LOG.warn("unable to resolve setter conflict for {}", setter1.getName());
        }
        else {
            LOG.debug("{} (m1={} <=> m2={} => result:{})", setter1.getName(), p1.getSimpleName(), p2.getSimpleName(),
                res.getRawParameterType(0)==null?"null":res.getRawParameterType(0).getSimpleName());
        }
        return res;
    }

    public static boolean isAssignable(Class<?> p1, Class<?> p2, Class<?> c1, Class<?> c2) {
        return ((p1.isAssignableFrom(c1) && p2.isAssignableFrom(c2))
                || (p2.isAssignableFrom(c1) && p1.isAssignableFrom(c2)));

    }

    public void addDeserializer(Class<?> clazz, String builder) {
        this.customDeserializer.put(clazz, builder);
    }

}
