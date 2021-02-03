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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.FaultNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.FaultNotificationBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestDom {

    private static final Logger LOG = LoggerFactory.getLogger(TestDom.class);

    @Test
    public void test2() {
        FaultNotification faultNotification =
                new FaultNotificationBuilder().setCounter(1).setNodeId("Node1").setSeverity(SeverityType.Major).build();

//        final NormalizedNode<?, ?> data = DomContext.getBINDING_CONTEXT()
//                .toNormalizedNode(InstanceIdentifier.create(FaultNotification.class), faultNotification).getValue();

//        LOG.info("Normalized node: {}", data);
//        final String json = toJson(data, schemaContext);
//        LOG.info(json);

    }
    /**
     * Serialization of {@link NormalizedNode} into {@link String}.
     *
     * @param node          to be serialized data
     * @param schemaContext schema context
     * @return serialized data
     */
    private static String toJson(final NormalizedNode<?, ?> node, final EffectiveModelContext schemaContext) {
        final JSONCodecFactory codecFactory = JSONCodecFactorySupplier.RFC7951.createSimple(schemaContext);
        try (StringWriter stringWriter = new StringWriter();
             JsonWriter jsonWriter = new JsonWriter(stringWriter);
             NormalizedNodeWriter nodeStreamer = NormalizedNodeWriter.forStreamWriter(
                     JSONNormalizedNodeStreamWriter.createNestedWriter(codecFactory, SchemaPath.ROOT,
                             schemaContext.getQName().getNamespace(), jsonWriter))) {
            jsonWriter.beginObject();
            nodeStreamer.write(node);
            jsonWriter.endObject();
            return stringWriter.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to convert input node to JSON: " + node, e);
        }
    }
}
