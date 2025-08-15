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

import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.BaseIdentitySerializer;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.ContainerNodeSerializer;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.DateAndTimeSerializer;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.EnumSerializer;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.MapSerializer;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.TypeObjectSerializer;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.XMLNamespaceSerializer;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.TypeObject;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;

public class YangToolsModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    private final ContainerNodeSerializer containerNodeSerializer;

    public YangToolsModule() {
        super();
        this.containerNodeSerializer = new ContainerNodeSerializer();
        setDeserializerModifier(new YangToolsDeserializerModifier());

        addSerializer(DateAndTime.class, new DateAndTimeSerializer());
        addSerializer(TypeObject.class, new TypeObjectSerializer());
        addSerializer(ScalarTypeObject.class, new TypeObjectSerializer());
        addSerializer(Enum.class, new EnumSerializer());
        addSerializer(Map.class, new MapSerializer());
        addSerializer(BaseIdentity.class, new BaseIdentitySerializer());
        addSerializer(ContainerNode.class, this.containerNodeSerializer);
        addSerializer(XMLNamespace.class, new XMLNamespaceSerializer());
    }
}
