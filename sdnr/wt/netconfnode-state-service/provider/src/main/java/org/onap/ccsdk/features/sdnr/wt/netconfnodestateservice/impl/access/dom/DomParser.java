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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.dom;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationResult;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

public class DomParser {

    /**
     * Parsing JSON file into {@link NormalizedNode}.
     *
     * @param path          path to the file
     * @param schemaContext schema context
     * @return created {@link NormalizedNode}
     */
    public static NormalizationResult<?> parseJsonFile(final String path, final EffectiveModelContext schemaContext) {
        final JSONCodecFactory codecFactory = JSONCodecFactorySupplier.RFC7951.createSimple(schemaContext);
        final NormalizationResultHolder resultHolder = new NormalizationResultHolder();
        try (NormalizedNodeStreamWriter writer = ImmutableNormalizedNodeStreamWriter.from(resultHolder);
             JsonParserStream jsonParser = JsonParserStream.create(writer, codecFactory);
             InputStream inputStream = NetconfDomAccessorImpl.class.getResourceAsStream(path);
             JsonReader reader = new JsonReader(new InputStreamReader(inputStream))) {
            jsonParser.parse(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse input JSON file: " + path, e);
        }
        return resultHolder.getResult();
    }

}
