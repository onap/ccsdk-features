/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2024 highstreet technologies GmbH Intellectual Property.
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
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;

public class ContainerNodeSerializer extends JsonSerializer<ContainerNode> {

    private Map<String, String> yangModuleLut;
    private static final String IDENTITY_STR_REGEX = "^\\([^\\?]{1,255}:([a-zA-Z0-9\\-]{1,50})\\?revision=\\d{4}-\\d{2}-\\d{2}\\)(.{1,255})$";
    private static final Pattern IDENTITY_STR_PATTERN = Pattern.compile(IDENTITY_STR_REGEX);


    public ContainerNodeSerializer() {
        this.yangModuleLut = null;
    }

    @Override
    public void serialize(ContainerNode value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        for (DataContainerChild child : value.body()) {
            if (child instanceof LeafNode<?>) {
                var v = child.body().toString();
                //(correct identites from (<namespace?revision=<revision>)<value> to <module-name>:<value>
                // use LookUpTable
                if (this.yangModuleLut != null) {
                    for(var lutEntry: yangModuleLut.entrySet()){
                        if(v.startsWith(lutEntry.getKey())){
                            v = v.replace(lutEntry.getKey(),lutEntry.getValue()+":");
                            break;
                        }
                    }
                }
                //if no LookUpTable available use regex replacement
                else {
                    final Matcher matcher = IDENTITY_STR_PATTERN.matcher(v);
                    if (matcher.find()) {
                        v = String.format("%s:%s", matcher.group(1), matcher.group(2));
                    }
                }
                gen.writePOJOField(child.name().getNodeType().getLocalName(), v);
            } else if (child instanceof UnkeyedListNode) {
                gen.writeArrayFieldStart(child.name().getNodeType().getLocalName());
                @SuppressWarnings("unchecked")
                Collection<UnkeyedListEntryNode> nn = (Collection<UnkeyedListEntryNode>) child.body();
                Iterator<?> childEntryItr = nn.iterator();
                while (childEntryItr.hasNext()) {
                    gen.writeStartObject();
                    UnkeyedListEntryNode childEntryNode = (UnkeyedListEntryNode) childEntryItr.next();
                    for (DataContainerChild child1 : childEntryNode.body()) {
                        if (child1 instanceof LeafNode<?>) {
                            gen.writePOJOField(child1.name().getNodeType().getLocalName(),
                                    child1.body().toString());
                        }
                    }
                    gen.writeEndObject();
                }
                gen.writeEndArray();
            }
        }
        gen.writeEndObject();
    }

    public void setContainerNodeYangModuleLut(Map<String, String> yangModuleLut) {
        this.yangModuleLut = yangModuleLut;
    }
}
