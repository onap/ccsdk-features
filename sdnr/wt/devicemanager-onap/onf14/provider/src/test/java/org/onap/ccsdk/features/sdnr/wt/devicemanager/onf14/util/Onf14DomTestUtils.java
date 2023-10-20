/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.util;

import com.google.gson.stream.JsonReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.codec.xml.XmlParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
//import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference; //Yangtool 8.0
import org.xml.sax.SAXException;

public class Onf14DomTestUtils {

    private static final QNameModule CORE_MODEL_1_4_MODULE =
            QNameModule.create(XMLNamespace.of("urn:onf:yang:core-model-1-4"), Revision.of("2019-11-27"));
    private static final QName CORE_MODEL_CONTROL_CONSTRUCT_CONTAINER =
            QName.create(CORE_MODEL_1_4_MODULE, "control-construct");

    private static EffectiveModelContext schemaContext;
    private static Inference hwContainerSchema;
    static JSONCodecFactory lhotkaCodecFactory;
    private static String streamAsString;
    private static NormalizedNode transformedInput;

    public static void cleanup() {
        lhotkaCodecFactory = null;
        schemaContext = null;
        hwContainerSchema = null;
    }

    static String loadTextFile(final File file) throws IOException {
        final StringBuilder result = new StringBuilder();
        try (BufferedReader bufReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line = null;
            while ((line = bufReader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }


    public static NormalizedNode getNormalizedNodeFromJson() throws IOException, URISyntaxException {
        schemaContext = TestYangParserUtil.parseYangResourceDirectory("/");
        lhotkaCodecFactory = JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02.getShared(schemaContext);
        streamAsString =
                loadTextFile(new File(Onf14DomTestUtils.class.getResource("/ControlConstruct-data-test.json").toURI()));
        final JsonReader reader = new JsonReader(new StringReader(streamAsString));

        NormalizedNodeResult result = new NormalizedNodeResult();

        // StreamWriter which attaches NormalizedNode under parent
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        // JSON -> StreamWriter parser
        try (JsonParserStream handler = JsonParserStream.create(streamWriter, lhotkaCodecFactory)) {
            handler.parse(new JsonReader(new StringReader(streamAsString)));
        }

        // Finally build the node
        transformedInput = result.getResult();
        return transformedInput;
    }

    public static NormalizedNode getNormalizedNodeFromXML()
            throws XMLStreamException, URISyntaxException, IOException, SAXException {
        schemaContext = TestYangParserUtil.parseYangResourceDirectory("/previousRevision");
        hwContainerSchema = Inference.ofDataTreePath(schemaContext, CORE_MODEL_CONTROL_CONSTRUCT_CONTAINER);
        final InputStream resourceAsStream =
                Onf14DomTestUtils.class.getResourceAsStream("/previousRevision/ControlConstruct-data-test.xml");

        /*
         * final XMLInputFactory factory = XMLInputFactory.newInstance();
         * XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);
         */
        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, hwContainerSchema);
        xmlParser.parse(reader);

        xmlParser.flush();
        xmlParser.close();

        transformedInput = result.getResult();
        return transformedInput;
    }
    
    public static NormalizedNode getNormalizedNodeFromXML(String revision)
            throws XMLStreamException, URISyntaxException, IOException, SAXException {
        schemaContext = TestYangParserUtil.parseYangResourceDirectory("/currentRevision");
        hwContainerSchema = Inference.ofDataTreePath(schemaContext, CORE_MODEL_CONTROL_CONSTRUCT_CONTAINER);
        final InputStream resourceAsStream =
                Onf14DomTestUtils.class.getResourceAsStream("/currentRevision/Ceragon-Control-Construct.xml");

        /*
         * final XMLInputFactory factory = XMLInputFactory.newInstance();
         * XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);
         */
        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, hwContainerSchema);
        xmlParser.parse(reader);

        xmlParser.flush();
        xmlParser.close();

        transformedInput = result.getResult();
        return transformedInput;
    }

}
