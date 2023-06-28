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
import org.opendaylight.yangtools.yang.binding.EnumTypeObject;


@SuppressWarnings("rawtypes")
public class EnumSerializer extends JsonSerializer<Enum> {

    @Override
    public void serialize(Enum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        //sadly a seperate serializer for class Enumeration doesn't work, so we have to catch it here
        if(value instanceof EnumTypeObject) {
            gen.writeString(((EnumTypeObject)value).getName());
        }
        else {
            gen.writeString(value.name());
        }
    }
}
