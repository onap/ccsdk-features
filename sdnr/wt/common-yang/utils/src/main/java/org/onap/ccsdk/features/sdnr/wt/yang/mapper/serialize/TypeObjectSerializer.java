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
import org.opendaylight.yangtools.yang.binding.TypeObject;

public class TypeObjectSerializer extends JsonSerializer<TypeObject> {

    @Override
    public void serialize(TypeObject value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        //stringValue
        Method[] methods = value.getClass().getDeclaredMethods();
        String name;
        for (Method method : methods) {
            name = method.getName();
            if (method.getParameterCount()==0 && (name.equals("stringValue") || name.equals("getValue"))) {
                try {
                    gen.writeString((String)method.invoke(value));
                    break;
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | IOException e) {
                    throw new IOException("No String getter method supported TypeObject for "+value.getClass(),e);
                }
            }
        }
    }
}
