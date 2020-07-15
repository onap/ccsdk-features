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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.BoolQueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.SortOrder;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.Search7Request;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.SearchRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.AggregationEntries;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.SearchResponse;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.DataTreeChildObject;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.DataTreeHttpServlet.FilterMode;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.DataTreeObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Entity;

/**
 * @author Michael Dürre
 *
 */
public class DataTreeProviderImpl {

    private static final long MAXSIZE_PERSEARCH = 10;
    private HtDatabaseClient dbClient;
    final String INVENTORY_PROPERTY_TREELEVEL = "tree-level";
    final String INVENTORY_PROPERTY_NODEID = "node-id";
    final String INVENTORY_PROPERTY_UUID = "uuid";
    final String INVENTORY_PROPERTY_PARENTUUID = "parent-uuid";
    final String INVENTORY_PROPERTY_FOR_LABEL_CHILD = "uuid";
    final String INVENTORY_PROPERTY_FOR_LABEL = "uuid";

    private List<SearchHit> search(Entity e, String filter, String propTreeLevel) throws IOException {
        return this.search(e, filter, null, null, null, null, null, null, propTreeLevel);
    }

    private List<SearchHit> search(Entity e, String filter, String nodeKey, String nodeId, String parentKey,
            String parentValue, String childKey, String childValue, String propTreeLevel) throws IOException {
        QueryBuilder query =
                filter == null ? QueryBuilders.matchAllQuery() : QueryBuilders.searchAllFieldsQuery(filter);
        if ((nodeId != null && nodeKey != null) || (parentKey != null && parentValue != null)) {
            BoolQueryBuilder bquery = new BoolQueryBuilder();
            if (filter != null) {
                bquery.must(query);
            }
            if (nodeId != null) {
                bquery.must(QueryBuilders.matchQuery(nodeKey, nodeId));
            }
            //			if (parentKey != null && parentValue != null) {
            //				bquery.must(QueryBuilders.matchQuery(parentKey, parentValue));
            //			}
            //			if (childKey != null && childValue != null) {
            //				bquery.must(QueryBuilders.matchQuery(childKey, childValue));
            //			}
            query = bquery;

        }
        return this.search(e, query, propTreeLevel);
    }

    private List<SearchHit> search(Entity e, QueryBuilder query, String propTreeLevel) throws IOException {
        List<SearchHit> list = new ArrayList<SearchHit>();
        query.sort(propTreeLevel, SortOrder.ASCENDING);
        SearchRequest request = new Search7Request(Entity.Inventoryequipment.getName());
        query.size(MAXSIZE_PERSEARCH);
        request.setQuery(query);
        SearchResponse response = this.dbClient.search(request);
        SearchHit[] matches = response.getHits();
        for (SearchHit hit : matches) {
            list.add(hit);
        }
        if (response.getTotal() > MAXSIZE_PERSEARCH) {
            long todo = response.getTotal();
            long from = MAXSIZE_PERSEARCH;
            while (todo > from) {
                request.setQuery(query.from(from));
                from += MAXSIZE_PERSEARCH;
                //merge into list
                response = this.dbClient.search(request);
                matches = response.getHits();
                for (SearchHit hit : matches) {
                    list.add(hit);
                }
            }
        }
        return list;
    }

    /**
     * @param iNVENTORY_PROPERTY_NODEID2
     * @return
     * @throws IOException
     */
    private AggregationEntries searchAggregated(Entity e, String key) throws IOException {
        QueryBuilder query = QueryBuilders.matchAllQuery().aggregations(key).size(MAXSIZE_PERSEARCH);
        SearchRequest request = new Search7Request(e.getName());
        request.setQuery(query);
        SearchResponse response = this.dbClient.search(request);
        return response.getAggregations(key);
    }

    /**
     *
     * @param tree
     * @param filter
     * @param
     * @return
     * @throws IOException
     */
    public DataTreeObject readInventoryTree(List<String> tree, String filter, FilterMode mode) throws IOException {

        //root nodes will be node-information -> below inventory
        if (tree == null || tree.size() <= 0) {
            return this.readInventoryTreeWithNode(filter, mode);
        }
        //root node will be inventory on tree-level if sliced treePath
        else {
            return this.readInventoryTreeForNode(tree.get(0), tree.subList(0, tree.size() - 1), filter, mode);
        }

    }

    /**
     * @param string
     * @param slice
     * @param filter
     * @param mode
     * @return
     */
    private DataTreeObject readInventoryTreeForNode(String nodeId, List<String> list, String filter, FilterMode mode)
            throws IOException {
        DataTreeObject tree = new DataTreeObject(INVENTORY_PROPERTY_PARENTUUID, INVENTORY_PROPERTY_UUID);
        final String parentUuid = list.size() > 1 ? list.get(list.size() - 2) : null;
        final String uuid = list.size() > 0 ? list.get(list.size() - 1) : null;
        List<SearchHit> matches = this.search(Entity.Inventoryequipment, filter, INVENTORY_PROPERTY_NODEID, nodeId,
                INVENTORY_PROPERTY_PARENTUUID, parentUuid, INVENTORY_PROPERTY_UUID, uuid, INVENTORY_PROPERTY_TREELEVEL);

        //tree.a(subtreePath);
        List<SearchHit> others = this.search(Entity.Inventoryequipment, (String) null, INVENTORY_PROPERTY_TREELEVEL);
        if (matches.size() > 0) {
            int treeLevelToStart = (list == null || list.size() <= 0) ? 0 : list.size() - 1;
            //build tree
            JSONObject hitData;
            //fill root elems
            for (SearchHit hit : matches) {
                hitData = hit.getSource();
                if (hitData.getLong("tree-level") == treeLevelToStart) {
                    tree.put(hit.getId(),
                            new DataTreeChildObject(hitData.getString(INVENTORY_PROPERTY_FOR_LABEL), true)
                                    .setProperty(INVENTORY_PROPERTY_UUID, hitData.getString(INVENTORY_PROPERTY_UUID))
                                    .setProperty(INVENTORY_PROPERTY_PARENTUUID,
                                            hitData.getString(INVENTORY_PROPERTY_PARENTUUID)));
                }
            }
            for (SearchHit hit : others) {
                hitData = hit.getSource();
                if (hitData.getLong("tree-level") == treeLevelToStart) {
                    tree.putIfNotExists(hit.getId(),
                            new DataTreeChildObject(hitData.getString(INVENTORY_PROPERTY_FOR_LABEL), false)
                                    .setProperty(INVENTORY_PROPERTY_UUID, hitData.getString(INVENTORY_PROPERTY_UUID))
                                    .setProperty(INVENTORY_PROPERTY_PARENTUUID,
                                            hitData.getString(INVENTORY_PROPERTY_PARENTUUID)));
                }
            }
            //fill child elems
            for (SearchHit hit : matches) {
                hitData = hit.getSource();
                if (hitData.getLong("tree-level") > treeLevelToStart) {
                    tree.put(hitData.getLong("tree-level") - treeLevelToStart - 1, hit.getId(),
                            new DataTreeChildObject(hitData.getString(INVENTORY_PROPERTY_FOR_LABEL), true)
                                    .setProperty(INVENTORY_PROPERTY_UUID, hitData.getString(INVENTORY_PROPERTY_UUID))
                                    .setProperty(INVENTORY_PROPERTY_PARENTUUID,
                                            hitData.getString(INVENTORY_PROPERTY_PARENTUUID)));
                }
            }
            for (SearchHit hit : others) {
                hitData = hit.getSource();
                if (hitData.getLong("tree-level") > treeLevelToStart) {
                    tree.putIfNotExists(hitData.getLong("tree-level") - treeLevelToStart - 1, hit.getId(),
                            new DataTreeChildObject(hitData.getString(INVENTORY_PROPERTY_FOR_LABEL), false)
                                    .setProperty(INVENTORY_PROPERTY_UUID, hitData.getString(INVENTORY_PROPERTY_UUID))
                                    .setProperty(INVENTORY_PROPERTY_PARENTUUID,
                                            hitData.getString(INVENTORY_PROPERTY_PARENTUUID)));
                }
            }
        }
        return tree;
    }

    /**
     * node will be root elements inventory information below from level-1
     *
     * @param filter
     * @param mode
     * @return
     * @throws IOException
     */
    private DataTreeObject readInventoryTreeWithNode(String filter, FilterMode mode) throws IOException {
        DataTreeObject tree = new DataTreeObject(INVENTORY_PROPERTY_PARENTUUID, INVENTORY_PROPERTY_UUID);

        List<SearchHit> matches = this.search(Entity.Inventoryequipment, filter, INVENTORY_PROPERTY_TREELEVEL);
        List<SearchHit> others = null;
        if (matches.size() > 0) {
            if (filter != null) {
                //find all parents up to tree-level 0
                String nodeId = "";
                List<String> alreadyInList = new ArrayList<>();
                BoolQueryBuilder query2 = QueryBuilders.boolQuery();
                for (SearchHit hit : matches) {
                    nodeId = hit.getSource().getString(INVENTORY_PROPERTY_NODEID);
                    if (alreadyInList.contains(nodeId)) {
                        continue;
                    }
                    query2.should(QueryBuilders.matchQuery(INVENTORY_PROPERTY_NODEID, nodeId));
                    alreadyInList.add(nodeId);
                    tree.put(nodeId,
                            new DataTreeChildObject(nodeId, false).setProperty(INVENTORY_PROPERTY_UUID, nodeId));

                }
                others = this.search(Entity.Inventoryequipment, query2, INVENTORY_PROPERTY_TREELEVEL);
            } else {
                AggregationEntries nodes = this.searchAggregated(Entity.Inventoryequipment, INVENTORY_PROPERTY_NODEID);
                for (String node : nodes.keySet()) {
                    tree.put(node, new DataTreeChildObject(node, false).setProperty(INVENTORY_PROPERTY_UUID, node));
                }
            }

            //build tree
            JSONObject hitData;
            //fill root elems
            for (SearchHit hit : matches) {
                hitData = hit.getSource();
                if (hitData.getLong("tree-level") == 0) {
                    tree.put(0, hit.getId(),
                            new DataTreeChildObject(hitData.getString(INVENTORY_PROPERTY_FOR_LABEL), true)
                                    .setProperty(INVENTORY_PROPERTY_UUID, hitData.getString(INVENTORY_PROPERTY_UUID))
                                    .setProperty(INVENTORY_PROPERTY_PARENTUUID,
                                            hitData.getString(INVENTORY_PROPERTY_NODEID)));
                }
            }
            if (others != null) {
                for (SearchHit hit : others) {
                    hitData = hit.getSource();
                    if (hitData.getLong("tree-level") == 0) {
                        tree.putIfNotExists(0, hit.getId(),
                                new DataTreeChildObject(hitData.getString(INVENTORY_PROPERTY_FOR_LABEL), false)
                                        .setProperty(INVENTORY_PROPERTY_UUID,
                                                hitData.getString(INVENTORY_PROPERTY_UUID))
                                        .setProperty(INVENTORY_PROPERTY_PARENTUUID,
                                                hitData.getString(INVENTORY_PROPERTY_NODEID)));
                    }
                }
            }
            //fill child elements
            for (SearchHit hit : matches) {
                hitData = hit.getSource();
                long treeLevel = hitData.getLong("tree-level");
                if (treeLevel > 0) {
                    tree.put(treeLevel, hit.getId(),
                            new DataTreeChildObject(hitData.getString(INVENTORY_PROPERTY_FOR_LABEL_CHILD), true)
                                    .setProperty(INVENTORY_PROPERTY_UUID, hitData.getString(INVENTORY_PROPERTY_UUID))
                                    .setProperty(INVENTORY_PROPERTY_PARENTUUID,
                                            hitData.getString(INVENTORY_PROPERTY_PARENTUUID)));
                }
            }
            if (others != null) {
                for (SearchHit hit : others) {
                    hitData = hit.getSource();
                    long treeLevel = hitData.getLong("tree-level");
                    if (hitData.getLong("tree-level") > 0) {
                        tree.putIfNotExists(treeLevel, hit.getId(),
                                new DataTreeChildObject(hitData.getString(INVENTORY_PROPERTY_FOR_LABEL_CHILD), false)
                                        .setProperty(INVENTORY_PROPERTY_UUID,
                                                hitData.getString(INVENTORY_PROPERTY_UUID))
                                        .setProperty(INVENTORY_PROPERTY_PARENTUUID,
                                                hitData.getString(INVENTORY_PROPERTY_PARENTUUID)));
                    }
                }
            }
            if (mode == FilterMode.Strict) {
                tree.removeUnmatchedPaths();
            }
        }
        return tree;
    }



    /**
     * @param client
     */
    public void setDatabaseClient(HtDatabaseClient client) {
        this.dbClient = client;

    }
}
