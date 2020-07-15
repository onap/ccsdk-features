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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.impl.DataTreeProviderImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Dürre
 *
 */
public class DataTreeHttpServlet extends HttpServlet {

    public enum FilterMode {
        Strict, //show only filtered items and their parents
        Lazy //show root items (and all their children) which have matches inside
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final DataTreeProviderImpl dataTreeProvider;
    private static final Logger LOG = LoggerFactory.getLogger(DataTreeHttpServlet.class);

    public DataTreeHttpServlet() {
        super();
        this.dataTreeProvider = new DataTreeProviderImpl();
    }

    /**
     * @param client
     */
    public void setDatabaseClient(HtDatabaseClient client) {
        this.dataTreeProvider.setDatabaseClient(client);

    }

    public static String readPayload(HttpServletRequest request) throws IOException {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
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
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
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
            LOG.info("GET request for {} to e={} with tree={}", uri, e.entity, e.tree);
            switch (e.entity) {
                case Inventoryequipment:
                    DataTreeObject o = this.dataTreeProvider.readInventoryTree(e.tree, null, FilterMode.Lazy);
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
        FilterMode mode = FilterMode.Lazy;
        try {
            final String body = readPayload(req);
            JSONObject data = new JSONObject(body);
            if (data.has("query")) {
                filter = data.getString("query");
            }
            if (data.has("mode")) {
                mode = data.getString("mode").equals("lazy") ? FilterMode.Lazy : FilterMode.Strict;
            }


        } catch (Exception e) {
            LOG.warn("problem reading payload: {}", e);
        }
        LOG.debug("POST request for {}", uri);
        final EntityWithTree e = getEntity(uri);
        if (e != null) {
            switch (e.entity) {
                case Inventoryequipment:
                    DataTreeObject o = this.dataTreeProvider.readInventoryTree(e.tree, filter, mode);
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
        Entity e = null;
        if (matcher.find() && matcher.groupCount() > 0) {
            try {
                e = Entity.forName(matcher.group(1)).get();
                return new EntityWithTree(e, matcher.groupCount() > 1 ? matcher.group(2) : null);
            } catch (Exception e2) {
                LOG.warn("unable to parse {} into entity: {}", matcher.group(2), e2);
            }
        }
        return null;

    }

    private void doJsonResponse(HttpServletResponse resp, DataTreeObject data) {
        resp.setHeader("Content-Type", "application/json");
        try {
            resp.getWriter().write(data.toJSON());
        } catch (IOException e) {
            LOG.warn("problem sending response: {}", e);
        }
    }

    public static class EntityWithTree {
        public final Entity entity;
        public final List<String> tree;

        @Override
        public String toString() {
            return "EntityWithTree [entity=" + entity + ", tree=" + tree + "]";
        }

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
