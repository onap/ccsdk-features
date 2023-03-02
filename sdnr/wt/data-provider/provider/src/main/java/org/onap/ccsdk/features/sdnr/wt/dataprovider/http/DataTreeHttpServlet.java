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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.InventoryTreeProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.DataTreeObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardServletPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Dürre
 *
 */

@HttpWhiteboardServletPattern("/tree/*")
@HttpWhiteboardServletName("DataTreeHttpServlet")
@Component(service = Servlet.class)
public class DataTreeHttpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private InventoryTreeProvider dataTreeProvider;
    private static final Logger LOG = LoggerFactory.getLogger(DataTreeHttpServlet.class);

    public DataTreeHttpServlet() {
        super();
    }

    public void setInventoryTreeProvider(InventoryTreeProvider provider) {
        this.dataTreeProvider = provider;

    }

    public static String readPayload(HttpServletRequest request) throws IOException {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        IOException toThrow = null;
        try (InputStream inputStream = request.getInputStream()) {

            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            toThrow = ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    LOG.debug("problem closing reader:", ex);
                    toThrow = ex;
                }
            }
        }
        if (toThrow != null) {
            throw toThrow;
        }
        body = stringBuilder.toString();
        return body;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getRequestURI();
        LOG.debug("GET request for {}", uri);
        final EntityWithTree e = getEntity(uri);
        if (e != null) {
            LOG.debug("GET request for {} to e={} with tree={}", uri, e.entity, e.tree);
            switch (e.entity) {
                case Inventoryequipment:
                    DataTreeObject o = this.dataTreeProvider.readInventoryTree(e.tree, null);
                    this.doJsonResponse(resp, o);
                    break;
                default:
                    this.notAvailble(resp);
                    break;
            }
        } else {
            LOG.debug("unable to find entity for uri {}", uri);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String uri = req.getRequestURI();
        String filter = null;
        try {
            final String body = readPayload(req);
            JSONObject data = new JSONObject(body);
            if (data.has("query")) {
                filter = data.getString("query");
            }

        } catch (Exception e) {
            LOG.warn("problem reading payload: {}", e);
        }
        LOG.debug("POST request for {}", uri);
        final EntityWithTree e = getEntity(uri);
        if (e != null) {
            switch (e.entity) {
                case Inventoryequipment:
                    DataTreeObject o = this.dataTreeProvider.readInventoryTree(e.tree, filter);
                    this.doJsonResponse(resp, o);
                    break;
                default:
                    this.notAvailble(resp);
                    break;
            }
        }
    }

    /**
     * @param resp
     */
    private void notAvailble(HttpServletResponse resp) {
        try {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException e) {

        }
    }

    public static EntityWithTree getEntity(String uri) {
        final String regex = "^\\/tree\\/read-(.*)-tree\\/?(.*)$";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(uri);
        if (matcher.find() && matcher.groupCount() > 0) {
            try {
                Optional<Entity> oe = Optional.ofNullable(Entity.forName(matcher.group(1)));
                if (oe.isPresent()) {
                    return new EntityWithTree(oe.get(), matcher.groupCount() > 1 ? matcher.group(2) : null);
                } else {
                    LOG.warn("unable to find entity for name {}", matcher.group(1));
                }
            } catch (Exception e2) {
                LOG.warn("unable to parse {} into entity: ", matcher.group(2), e2);
            }
        }
        return null;

    }

    private void doJsonResponse(HttpServletResponse resp, DataTreeObject data) {
        resp.setHeader("Content-Type", "application/json");
        try {
            resp.getWriter().write(data.toJSON());
        } catch (IOException e) {
            LOG.warn("problem sending response: ", e);
        }
    }

    public static class EntityWithTree {
        public final Entity entity;
        public final List<String> tree;

        @Override
        public String toString() {
            return "EntityWithTree [entity=" + entity + ", tree=" + tree + "]";
        }

        /**
        *
        * @param e database enttity to access
        * @param tree tree description
        *   e.g. nodeA           => tree entry for node-id=nodeA
        *        nodeA/key0      => tree entry for node-id=nodeA and uuid=key0 and tree-level=0
        *        nodeA/key0/key1 => tree entry for node-id=nodeA and uuid=key1 and tree-level=1
        *
        */
        public EntityWithTree(Entity e, String tree) {
            this.entity = e;
            if (tree != null) {
                if (tree.startsWith("/")) {
                    tree = tree.substring(1);
                }
                if (tree.endsWith("/")) {
                    tree = tree.substring(0, tree.length() - 1);
                }
                String[] tmp = tree.split("\\/");
                this.tree = new ArrayList<>();
                for (int i = 0; i < tmp.length; i++) {
                    try {
                        String s = URLDecoder.decode(tmp[i], "utf-8");
                        if (s != null && s.length() > 0) {
                            this.tree.add(s);
                        }
                    } catch (UnsupportedEncodingException e1) {
                        LOG.warn("problem urldecode {}: {}", tmp[i], e);
                    }
                }
            } else {
                this.tree = null;
            }
        }
    }
}
