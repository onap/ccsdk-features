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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentifierDeserializer extends KeyDeserializer {

    private static final Logger LOG = LoggerFactory.getLogger(IdentifierDeserializer.class);

    public IdentifierDeserializer() {}

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        Class<?> clazz = ctxt.getClass();
        final String arg = key;
        LOG.debug("Deserialization for key:{}",key);
        // find constructor argument types
        List<Class<?>> ctypes = YangToolsMapperHelper.getConstructorParameterTypes(clazz, String.class);
        for (Class<?> ctype : ctypes) {
            try {
                if (ctype.equals(String.class)) {
                    return clazz.getConstructor(ctype).newInstance(arg);
                } else if (ctype.equals(Uint16.class)) {
                    return clazz.getConstructor(ctype).newInstance(Uint16.valueOf(arg));

                } else if (ctype.equals(Uint32.class)) {
                    return clazz.getConstructor(ctype).newInstance(Uint32.valueOf(arg));
                } else if (ctype.equals(Uint64.class)) {
                    return clazz.getConstructor(ctype).newInstance(Uint64.valueOf(arg));
                }
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LOG.warn("unable to instantiate class {} with arg {}: ", clazz, arg, e);
                throw new IllegalArgumentException(
                        "unable to instantiate class " + clazz.getName() + " with arg '" + arg + "' ", e);
            }
        }
        return null;
    }

}
