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

import * as React from 'react';
import { RouteComponentProps, withRouter } from 'react-router-dom';

import connect, { IDispatcher, Connect } from "../../../../framework/src/flux/connect";
import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import { MaterialTable, MaterialTableCtorType, ColumnType } from "../../../../framework/src/components/material-table";
import { AppBar, Tabs, Tab, MenuItem, Typography } from "@material-ui/core";
import Refresh from '@material-ui/icons/Refresh';
import { PanelId } from "../models/panelId";
import { setPanelAction } from "../actions/panelActions";


import { createConnectedNetworkElementsProperties, createConnectedNetworkElementsActions } from "../handlers/connectedNetworkElementsHandler";

import { NetworkElementConnection } from "../models/networkElementConnection";

import { InventoryType } from '../models/inventory';

import { createInventoryElementsProperties, createInventoryElementsActions } from "../handlers/inventoryElementsHandler";
import { NavigateToApplication } from '../../../../framework/src/actions/navigationActions';
import { updateInventoryTreeAsyncAction } from '../actions/inventoryTreeActions';
import RefreshInventoryDialog, { RefreshInventoryDialogMode } from '../components/refreshInventoryDialog';

const InventoryTable = MaterialTable as MaterialTableCtorType<InventoryType & { _id: string }>;

const mapProps = (state: IApplicationStoreState) => ({
  connectedNetworkElementsProperties: createConnectedNetworkElementsProperties(state),
  panelId: state.inventory.currentOpenPanel,
  inventoryElementsProperties: createInventoryElementsProperties(state),
  inventoryElements: state.inventory.inventoryElements
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  connectedNetworkElementsActions: createConnectedNetworkElementsActions(dispatcher.dispatch),
  switchActivePanel: (panelId: PanelId) => {
    dispatcher.dispatch(setPanelAction(panelId));
  },
  inventoryElementsActions: createInventoryElementsActions(dispatcher.dispatch),
  navigateToApplication: (applicationName: string, path?: string) => dispatcher.dispatch(new NavigateToApplication(applicationName, path)),
  updateInventoryTree: (mountId: string, searchTerm?: string) => dispatcher.dispatch(updateInventoryTreeAsyncAction(mountId, searchTerm)),
});

let treeViewInitialSorted = false;
let inventoryInitialSorted = false;

const ConnectedElementTable = MaterialTable as MaterialTableCtorType<NetworkElementConnection>;

type DashboardComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDispatch>;
type DashboardComponentState = {
  refreshInventoryEditorMode: RefreshInventoryDialogMode
}

class DashboardSelectorComponent extends React.Component<DashboardComponentProps, DashboardComponentState> {
  constructor(props: DashboardComponentProps) {
    super(props);

    this.state = {
      refreshInventoryEditorMode: RefreshInventoryDialogMode.None
    };
  }

  private onHandleTabChange = (event: React.ChangeEvent<{}>, newValue: PanelId) => {
    this.onTogglePanel(newValue);
  }

  private onTogglePanel = (panelId: PanelId) => {
    const nextActivePanel = panelId;
    this.props.switchActivePanel(nextActivePanel);

    switch (nextActivePanel) {
      case 'InventoryElementsTable':

        if (!inventoryInitialSorted) {
          this.props.inventoryElementsActions.onHandleExplicitRequestSort("nodeId", "asc");
          inventoryInitialSorted = true;
        } else {
          this.props.inventoryElementsActions.onRefresh();

        }
        break;
      case 'TreeviewTable':
        if (!treeViewInitialSorted) {
          this.props.connectedNetworkElementsActions.onHandleExplicitRequestSort("nodeId", "asc");
          treeViewInitialSorted = true;
        } else {
          this.props.connectedNetworkElementsActions.onRefresh();
        }
        break;
      case null:
        // do nothing if all panels are closed
        break;
      default:
        console.warn("Unknown nextActivePanel [" + nextActivePanel + "] in connectView");
        break;
    }

  };

  getContextMenu = (rowData: InventoryType) => {
    return [
      <MenuItem aria-label={"inventory-button"} onClick={event => { this.props.updateInventoryTree(rowData.nodeId, rowData.uuid); this.props.navigateToApplication("inventory", rowData.nodeId) }}><Typography>View in Treeview</Typography></MenuItem>,
    ];

  }

  render() {

    const refreshInventoryAction = {
      icon: Refresh, tooltip: 'Refresh Inventory', onClick: () => {
        this.setState({
          refreshInventoryEditorMode: RefreshInventoryDialogMode.RefreshInventoryTable
        });
      }
    };
    const { panelId: activePanelId } = this.props;
    return (
      <>
        <AppBar position="static">
          <Tabs value={activePanelId} onChange={this.onHandleTabChange} aria-label="inventory-app-tabs">
            <Tab label="Table View" value="InventoryElementsTable" aria-label="table-tab" />
            <Tab label="Tree view" value="TreeviewTable" aria-label="treeview-tab" />
          </Tabs>
        </AppBar>

        {

          activePanelId === "InventoryElementsTable" &&
          <>
            <InventoryTable stickyHeader title="Inventory" idProperty="_id" tableId="inventory-table" customActionButtons={[refreshInventoryAction]} columns={[
              { property: "nodeId", title: "Node Name" },
              { property: "manufacturerIdentifier", title: "Manufacturer" },
              { property: "parentUuid", title: "Parent" },
              { property: "uuid", title: "Name" },
              { property: "serial", title: "Serial" },
              { property: "version", title: "Version" },
              { property: "date", title: "Date" },
              { property: "description", title: "Description" },
              { property: "partTypeId", title: "Part Type Id" },
              { property: "modelIdentifier", title: "Model Identifier" },
              { property: "typeName", title: "Type" },
              { property: "treeLevel", title: "Containment Level" },
            ]}  {...this.props.inventoryElementsActions} {...this.props.inventoryElementsProperties}
              createContextMenu={rowData => {

                return this.getContextMenu(rowData);
              }} >
            </InventoryTable>
            <RefreshInventoryDialog
              mode={this.state.refreshInventoryEditorMode}
              onClose={this.onCloseRefreshInventoryDialog}
            />
          </>

        }
        {
          activePanelId === "TreeviewTable" &&

          <ConnectedElementTable stickyHeader tableId="treeview-networkelement-selection-table"
            onHandleClick={(e, row) => {
              this.props.navigateToApplication("inventory", row.nodeId);
              this.props.updateInventoryTree(row.nodeId, '*');
            }}
            columns={[
              { property: "nodeId", title: "Node Name", type: ColumnType.text },
              { property: "isRequired", title: "Required", type: ColumnType.boolean },
              { property: "host", title: "Host", type: ColumnType.text },
              { property: "port", title: "Port", type: ColumnType.numeric },
              { property: "coreModelCapability", title: "Core Model", type: ColumnType.text },
              { property: "deviceType", title: "Type", type: ColumnType.text },
            ]} idProperty="id" {...this.props.connectedNetworkElementsActions} {...this.props.connectedNetworkElementsProperties} asynchronus >
          </ConnectedElementTable>
        }
      </>
    );
  }

  private onCloseRefreshInventoryDialog = () => {
    this.setState({
      refreshInventoryEditorMode: RefreshInventoryDialogMode.None
    });
  }
  componentDidMount() {

    if (this.props.panelId === null) { //set default tab if none is set
      this.onTogglePanel("InventoryElementsTable");
    }

  }
}

export const Dashboard = withRouter(connect(mapProps, mapDispatch)(DashboardSelectorComponent));
export default Dashboard;

