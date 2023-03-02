/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2023 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.SortOrder;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.DataTreeChildObject;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.DataTreeObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryEntity;
import org.opendaylight.yangtools.yang.common.Uint32;

public abstract class BaseInventoryTreeProvider implements InventoryTreeProvider {

    private static final String INVENTORY_PROPERTY_TREELEVEL = "tree-level";
    protected static final String INVENTORY_PROPERTY_NODEID = "node-id";
    protected static final String INVENTORY_PROPERTY_UUID = "uuid";
    protected static final String INVENTORY_PROPERTY_PARENTUUID = "parent-uuid";

    protected abstract List<String> getAllNodeIds();

    protected abstract List<InventoryEntity> search(String filter, String sortOrderProperty, SortOrder sortOrder);

    protected abstract List<InventoryEntity> search(String filter, String nodeId, String parentUuid, String uuid,
            String sortOrderProperty, SortOrder sortOrder);

    protected abstract List<InventoryEntity> getItemsForNodes(List<String> nodeIds, String sortOrderProperty,
            SortOrder sortOrder);


    @Override
    public DataTreeObject readInventoryTree(List<String> tree, String filter) throws IOException {

        //root nodes will be node-information -> below inventory
        if (tree == null || tree.size() <= 0) {
            return this.readInventoryTreeWithNode(filter);
        }
        //root node will be inventory on tree-level if sliced treePath
        else {
            return this.readInventoryTreeForNode(tree.get(0), tree.subList(0, tree.size() - 1), filter);
        }

    }

    /**
     * Provide inventory list for a node, starting from element described by path
     * @param nodeId node
     * @param path describing element
     * @param filter
     * @return Inventory tree
     */
    private DataTreeObject readInventoryTreeForNode(String nodeId, List<String> path, String filter)
            throws IOException {
        DataTreeObject tree = new DataTreeObject(INVENTORY_PROPERTY_PARENTUUID, INVENTORY_PROPERTY_UUID);
        //get parent uuid of path
        final String parentUuid = path.size() > 1 ? path.get(path.size() - 2) : null;
        //get uuid of path
        final String uuid = path.size() > 0 ? path.get(path.size() - 1) : null;
        List<InventoryEntity> matches =
                this.search(filter, nodeId, parentUuid, uuid, INVENTORY_PROPERTY_TREELEVEL, SortOrder.ASCENDING);
        //tree.a(subtreePath);
        List<InventoryEntity> others =
                this.search((String) null, nodeId, null, null, INVENTORY_PROPERTY_TREELEVEL, SortOrder.ASCENDING);
        if (matches.size() > 0) {
            int treeLevelToStart = (path == null || path.size() <= 0) ? 0 : path.size() - 1;
            //build tree
            //fill root elems
            for (InventoryEntity hit : matches) {
                if (hit.getTreeLevel().longValue() == treeLevelToStart) {
                    tree.put(hit.getId(),
                            new DataTreeChildObject(hit.getUuid(), true)
                                    .setProperty(INVENTORY_PROPERTY_UUID, hit.getUuid())
                                    .setProperty(INVENTORY_PROPERTY_PARENTUUID, hit.getParentUuid()));
                }
            }
            for (InventoryEntity hit : others) {
                if (hit.getTreeLevel().longValue() == treeLevelToStart) {
                    tree.putIfNotExists(hit.getId(),
                            new DataTreeChildObject(hit.getUuid(), false)
                                    .setProperty(INVENTORY_PROPERTY_UUID, hit.getUuid())
                                    .setProperty(INVENTORY_PROPERTY_PARENTUUID, hit.getParentUuid()));
                }
            }
            //fill child elems
            for (InventoryEntity hit : matches) {
                if (hit.getTreeLevel().longValue() > treeLevelToStart) {
                    tree.put(hit.getTreeLevel().longValue() - treeLevelToStart - 1, hit.getId(),
                            new DataTreeChildObject(hit.getUuid(), true)
                                    .setProperty(INVENTORY_PROPERTY_UUID, hit.getUuid())
                                    .setProperty(INVENTORY_PROPERTY_PARENTUUID, hit.getParentUuid()));
                }
            }
            for (InventoryEntity hit : others) {
                if (hit.getTreeLevel().longValue() > treeLevelToStart) {
                    tree.putIfNotExists(hit.getTreeLevel().longValue() - treeLevelToStart - 1, hit.getId(),
                            new DataTreeChildObject(hit.getUuid(), false)
                                    .setProperty(INVENTORY_PROPERTY_UUID, hit.getUuid())
                                    .setProperty(INVENTORY_PROPERTY_PARENTUUID, hit.getParentUuid()));
                }
            }
            tree.removeUnmatchedPaths();
        }
        return tree;
    }

    /**
     * node will be root elements inventory information below from level-1
     *
     * @param filter
     * @return
     * @throws IOException
     */
    private DataTreeObject readInventoryTreeWithNode(String filter) throws IOException {
        DataTreeObject tree = new DataTreeObject(INVENTORY_PROPERTY_PARENTUUID, INVENTORY_PROPERTY_UUID);

        List<InventoryEntity> matches = this.search(filter, INVENTORY_PROPERTY_TREELEVEL, SortOrder.ASCENDING);
        List<InventoryEntity> others = null;
        if (matches.size() > 0) {
            if (filter != null) {
                //find all parents up to tree-level 0
                String nodeId = "";
                List<String> alreadyInList = new ArrayList<>();
                for (InventoryEntity hit : matches) {
                    nodeId = hit.getNodeId();
                    if (alreadyInList.contains(nodeId)) {
                        continue;
                    }
                    alreadyInList.add(nodeId);
                    tree.put(nodeId,
                            new DataTreeChildObject(nodeId, false).setProperty(INVENTORY_PROPERTY_UUID, nodeId));

                }
                others = this.getItemsForNodes(alreadyInList, INVENTORY_PROPERTY_TREELEVEL, SortOrder.ASCENDING);
            } else {
                List<String> nodeIds = this.getAllNodeIds();
                for (String node : nodeIds) {
                    tree.put(node, new DataTreeChildObject(node, false).setProperty(INVENTORY_PROPERTY_UUID, node));
                }
            }

            //build tree
            //fill root elems
            for (InventoryEntity hit : matches) {
                if (hit.getTreeLevel() == Uint32.ZERO) {
                    tree.put(0, hit.getId(),
                            new DataTreeChildObject(hit.getUuid(), true)
                                    .setProperty(INVENTORY_PROPERTY_UUID, hit.getUuid())
                                    .setProperty(INVENTORY_PROPERTY_PARENTUUID, hit.getNodeId()));
                }
            }
            if (others != null) {
                for (InventoryEntity hit : others) {
                    if (hit.getTreeLevel() == Uint32.ZERO) {
                        tree.putIfNotExists(0, hit.getId(),
                                new DataTreeChildObject(hit.getUuid(), false)
                                        .setProperty(INVENTORY_PROPERTY_UUID, hit.getUuid())
                                        .setProperty(INVENTORY_PROPERTY_PARENTUUID, hit.getNodeId()));
                    }
                }
            }
            //fill child elements
            for (InventoryEntity hit : matches) {
                long treeLevel = hit.getTreeLevel().longValue();
                if (treeLevel > 0) {
                    tree.put(treeLevel, hit.getId(),
                            new DataTreeChildObject(hit.getUuid(), true)
                                    .setProperty(INVENTORY_PROPERTY_UUID, hit.getUuid())
                                    .setProperty(INVENTORY_PROPERTY_PARENTUUID, hit.getParentUuid()));
                }
            }
            if (others != null) {
                for (InventoryEntity hit : others) {
                    long treeLevel = hit.getTreeLevel().longValue();
                    if (treeLevel > 0) {
                        tree.putIfNotExists(treeLevel, hit.getId(),
                                new DataTreeChildObject(hit.getUuid(), false)
                                        .setProperty(INVENTORY_PROPERTY_UUID, hit.getUuid())
                                        .setProperty(INVENTORY_PROPERTY_PARENTUUID, hit.getParentUuid()));
                    }
                }
            }
            tree.removeUnmatchedPaths();
        }
        return tree;
    }
}
