/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
import * as React from "react";
import { withStyles, WithStyles, createStyles, Theme } from '@material-ui/core/styles';

import { renderObject } from '../../../../framework/src/components/objectDump';
import { Connect, connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { TreeView, TreeViewCtorType, SearchMode } from '../../../../framework/src/components/material-ui/treeView';

import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";

import { updateInventoryTreeAsyncAction, selectInventoryNodeAsyncAction, UpdateSelectedNodeAction, UpdateExpandedNodesAction, setSearchTermAction } from "../actions/inventoryTreeActions";
import { TreeDemoItem } from "../models/inventory";

import { RouteComponentProps } from "react-router-dom";

const styles = (theme: Theme) => createStyles({
  root: {
    flex: "1 0 0%",
    display: "flex",
    flexDirection: "row",
  },
  tree: {
    flex: "1 0 0%",
    minWidth: "250px",
    padding: `0px ${theme.spacing(1)}px`
  },
  details: {
    flex: "5 0 0%",
    padding: `0px ${theme.spacing(1)}px`
  }
});

const mapProps = (state: IApplicationStoreState) => ({
  isBusy: state.inventory.inventoryTree.isBusy,
  rootNodes: state.inventory.inventoryTree.rootNodes,
  searchTerm: state.inventory.inventoryTree.searchTerm,
  selectedNode: state.inventory.inventoryTree.selectedNode,
  expendedItems: state.inventory.inventoryTree.expandedItems,
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  updateExpendedNodes: (expendedNodes: TreeDemoItem[]) => dispatcher.dispatch(new UpdateExpandedNodesAction(expendedNodes)),
  updateInventoryTree: (mountId: string, seatchTerm?: string) => dispatcher.dispatch(updateInventoryTreeAsyncAction(mountId, seatchTerm)),
  selectTreeNode: (nodeId?: string) => nodeId ? dispatcher.dispatch(selectInventoryNodeAsyncAction(nodeId)) : dispatcher.dispatch(new UpdateSelectedNodeAction(undefined)),
  setSearchTerm: (searchTerm: string) => dispatcher.dispatch(setSearchTermAction(searchTerm)),
});

const propsChache = Symbol("PropsCache");
const InventoryTree = TreeView as any as TreeViewCtorType<string>;



type TreeviewComponentProps = RouteComponentProps<{ mountId: string }> & WithStyles<typeof styles> & Connect<typeof mapProps, typeof mapDispatch>

type TreeviewComponentState = {
  [propsChache]: {
    rootNodes?: TreeDemoItem[];
  };
  rootNodes: TreeDemoItem[];
}


class DashboardComponent extends React.Component<TreeviewComponentProps, TreeviewComponentState> {

  constructor(props: TreeviewComponentProps) {
    super(props);

    this.state = {
      [propsChache]: {},
      rootNodes: [],
    };
  }

  static getDerivedStateFromProps(props: TreeviewComponentProps, state: TreeviewComponentState) {
    if (state[propsChache].rootNodes != props.rootNodes) {
      state = { ...state, rootNodes: props.rootNodes }
    }
    return state;
  }

  render() {
    const { classes, updateInventoryTree, updateExpendedNodes, expendedItems, selectedNode, selectTreeNode, searchTerm, match: { params: { mountId } } } = this.props;
    const scrollbar = { overflow: "auto", paddingRight: "20px" }
    return (
      <div style={scrollbar} className={classes.root}>
        <InventoryTree className={classes.tree} items={this.state.rootNodes} enableSearchBar initialSearchTerm={searchTerm} searchMode={SearchMode.OnEnter} searchTerm={searchTerm}
          onSearch={(searchTerm) => updateInventoryTree(mountId, searchTerm)} expandedItems={expendedItems} onFolderClick={(item) => {
            const indexOfItemToToggle = expendedItems.indexOf(item);
            if (indexOfItemToToggle === -1) {
              updateExpendedNodes([...expendedItems, item]);
            } else {
              updateExpendedNodes([
                ...expendedItems.slice(0, indexOfItemToToggle),
                ...expendedItems.slice(indexOfItemToToggle + 1),
              ]);
            }
          }}
          onItemClick={(elm) => selectTreeNode(elm.value)} />
        <div className={classes.details}>{
          selectedNode && renderObject(selectedNode, "tree-view") || null
        }</div>
      </div>
    );
  }

  componentDidMount() {
    const { updateInventoryTree, searchTerm, match: { params: { mountId } } } = this.props;
    updateInventoryTree(mountId, searchTerm);
  }

  componentWillUnmount() {
    this.props.setSearchTerm("");
  }
}

export const InventoryTreeView = connect(mapProps, mapDispatch)(withStyles(styles)(DashboardComponent));
export default InventoryTreeView;