/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.common.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PomFile {

    private final Document xmlDoc;

    public PomFile(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        //		documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        //		documentBuilderFactory.setFeature(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        //		documentBuilderFactory.setFeature(XMLInputFactory.SUPPORT_DTD, false);
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        this.xmlDoc = documentBuilder.parse(is);
    }

    public String getProperty(String key) {
        Node props = findChild("properties", this.xmlDoc.getDocumentElement());
        if (props != null) {
            Node prop = findChild(key, props);
            if (prop != null) {
                return getTextValue(prop);
            }
        }
        return null;
    }

    public String getParentVersion() {
        Node parent = findChild("parent", this.xmlDoc.getDocumentElement());
        if (parent != null) {
            Node version = findChild("version", parent);
            if (version != null) {
                return getTextValue(version);
            }
        }
        return null;
    }

    private static String getTextValue(Node node) {
        var textValue = new StringBuilder();
        for (int i = 0, length = node.getChildNodes().getLength(); i < length; i++) {
            Node c = node.getChildNodes().item(i);
            if (c.getNodeType() == Node.TEXT_NODE) {
                textValue.append(c.getNodeValue());
            }
        }
        return textValue.toString().trim();
    }

    private Node findChild(String name, Node root) {
        List<Element> childs = getChildElementNodes(root);
        for (Element n : childs) {
            if (name.equals(n.getNodeName())) {
                return n;
            }
        }
        return null;
    }

    /**
     * get all child nodes with type ELEMENT_NODE back in a list
     * 
     * @param node parent node
     * @return List with child nodes
     */
    private static List<Element> getChildElementNodes(Node node) {
        List<Element> res = new ArrayList<>();
        NodeList childs = node.getChildNodes();
        Node item;
        //System.out.println("Query node "+node.getNodeName());
        for (int n = 0; n < childs.getLength(); n++) {
            item = childs.item(n);
            //System.out.println(node.getNodeName()+"-"+item.getNodeName()+" "+item.getNodeType());
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                res.add((Element) childs.item(n));
            }
        }
        return res;
    }

}
