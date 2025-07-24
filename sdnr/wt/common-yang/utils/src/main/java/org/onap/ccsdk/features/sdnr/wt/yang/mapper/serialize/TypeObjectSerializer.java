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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.opendaylight.yangtools.binding.TypeObject;

public class TypeObjectSerializer extends JsonSerializer<TypeObject> {

    /**
     * serialize typeobject values
     * prefer stringValue() method over getValue() method
     */
    @Override
    public void serialize(TypeObject value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Method[] methods = value.getClass().getDeclaredMethods();
        String name;
        Method getValueMethod = null;
        for (Method method : methods) {
            name = method.getName();
            if (method.getParameterCount() == 0) {
                if (name.equals("getValue")) {
                    getValueMethod = method;
                } else if (name.equals("stringValue")) {
                    try {
                        gen.writeString((String) method.invoke(value));
                        break;
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | IOException e) {
                        throw new IOException("No String getter method supported TypeObject for " + value.getClass(),
                                e);
                    }
                }
            }
        }
        if (getValueMethod != null) {
            try {
                if (String.class.equals(getValueMethod.getReturnType())) {
                    gen.writeString((String) getValueMethod.invoke(value));
                } else {
                    gen.writeObject(getValueMethod.invoke(value));
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException e) {
                throw new IOException("No String getter method supported TypeObject for " + value.getClass(), e);
            }

        }
    }
}
