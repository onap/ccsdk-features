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

import React from 'react';
import { RouteComponentProps, withRouter } from 'react-router-dom';

import Refresh from '@mui/icons-material/Refresh';
import { AppBar, MenuItem, Tab, Tabs, Typography } from '@mui/material';

import { NavigateToApplication } from '../../../../framework/src/actions/navigationActions';
import { ColumnType, MaterialTable, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { connect, Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { loadAllInventoryDeviceListAsync } from '../actions/inventoryDeviceListActions';
import { updateInventoryTreeAsyncAction } from '../actions/inventoryTreeActions';
import { setPanelAction } from '../actions/panelActions';
import RefreshInventoryDialog, { RefreshInventoryDialogMode } from '../components/refreshInventoryDialog';
import { createInventoryElementsActions, createInventoryElementsProperties } from '../handlers/inventoryElementsHandler';
import { InventoryType } from '../models/inventory';
import { InventoryDeviceListType } from '../models/inventoryDeviceListType';
import { PanelId } from '../models/panelId';

const InventoryTable = MaterialTable as MaterialTableCtorType<InventoryType & { _id: string }>;

const mapProps = (state: IApplicationStoreState) => ({
  panelId: state.inventory.currentOpenPanel,
  inventoryElementsProperties: createInventoryElementsProperties(state),
  inventoryElements: state.inventory.inventoryElements,
  inventoryDeviceList: state.inventory.inventoryDeviceList.inventoryDeviceList,
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  switchActivePanel: (panelId: PanelId) => {
    dispatcher.dispatch(setPanelAction(panelId));
  },
  inventoryElementsActions: createInventoryElementsActions(dispatcher.dispatch),
  navigateToApplication: (applicationName: string, path?: string) => dispatcher.dispatch(new NavigateToApplication(applicationName, path)),
  updateInventoryTree: (mountId: string, searchTerm?: string) => dispatcher.dispatch(updateInventoryTreeAsyncAction(mountId, searchTerm)),
  getAllInventoryDeviceList: async () => {
    await dispatcher.dispatch(loadAllInventoryDeviceListAsync);
  },
});

let treeViewInitialSorted = false;
let inventoryInitialSorted = false;

const InventoryDeviceListTable = MaterialTable as MaterialTableCtorType<InventoryDeviceListType>;

type DashboardComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDispatch>;
type DashboardComponentState = {
  refreshInventoryEditorMode: RefreshInventoryDialogMode;
};

class DashboardSelectorComponent extends React.Component<DashboardComponentProps, DashboardComponentState> {
  constructor(props: DashboardComponentProps) {
    super(props);

    this.state = {
      refreshInventoryEditorMode: RefreshInventoryDialogMode.None,
    };
  }

  private onHandleTabChange = (event: React.SyntheticEvent, newValue: PanelId) => {
    this.onTogglePanel(newValue);
  };

  private onTogglePanel = (panelId: PanelId) => {
    const nextActivePanel = panelId;
    this.props.switchActivePanel(nextActivePanel);

    switch (nextActivePanel) {
      case 'Equipment':

        if (!inventoryInitialSorted) {
          this.props.inventoryElementsActions.onHandleExplicitRequestSort('nodeId', 'asc');
          inventoryInitialSorted = true;
        } else {
          this.props.inventoryElementsActions.onRefresh();

        }
        break;
      case 'TreeView':
        this.props.getAllInventoryDeviceList();
        break;
      case null:
        // do nothing if all panels are closed
        break;
      default:
        console.warn('Unknown nextActivePanel [' + nextActivePanel + '] in connectView');
        break;
    }

  };

  getContextMenu = (rowData: InventoryType) => {
    return [
      <MenuItem aria-label={'inventory-button'} onClick={() => { this.props.updateInventoryTree(rowData.nodeId, rowData.uuid); this.props.navigateToApplication('inventory', rowData.nodeId); }}><Typography>View in Treeview</Typography></MenuItem>,
    ];

  };

  render() {

    const refreshInventoryAction = {
      icon: Refresh, tooltip: 'Refresh Inventory', ariaLabel: 'refresh', onClick: () => {
        this.setState({
          refreshInventoryEditorMode: RefreshInventoryDialogMode.RefreshInventoryTable,
        });
      },
    };
    const { panelId: activePanelId } = this.props;
    return (
      <>
        <AppBar enableColorOnDark position="static">
          <Tabs indicatorColor="secondary" textColor="inherit" value={activePanelId} onChange={this.onHandleTabChange} aria-label="inventory-app-tabs">
            <Tab label="Equipment" value="Equipment" aria-label="equipment-tab" />
            <Tab label="Tree View" value="TreeView" aria-label="treeview-tab" />
          </Tabs>
        </AppBar>

        {

          activePanelId === 'Equipment' &&
          <>
            <InventoryTable stickyHeader idProperty="_id" tableId="inventory-table" customActionButtons={[refreshInventoryAction]} columns={[
              { property: 'nodeId', title: 'Node Name' },
              { property: 'manufacturerIdentifier', title: 'Manufacturer' },
              { property: 'parentUuid', title: 'Parent' },
              { property: 'uuid', title: 'Name' },
              { property: 'serial', title: 'Serial' },
              { property: 'version', title: 'Version' },
              { property: 'date', title: 'Date' },
              { property: 'description', title: 'Description' },
              { property: 'partTypeId', title: 'Part Type Id' },
              { property: 'modelIdentifier', title: 'Model Identifier' },
              { property: 'typeName', title: 'Type' },
              { property: 'treeLevel', title: 'Containment Level' },
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
          activePanelId === 'TreeView' &&
          <>
            <InventoryDeviceListTable stickyHeader tableId="treeview-networkelement-selection-table"
              defaultSortColumn={'nodeId'} defaultSortOrder="asc"
              onHandleClick={(e, row) => {
                this.props.navigateToApplication('inventory', row.nodeId);
                this.props.updateInventoryTree(row.nodeId, '*');
              }}
              rows={this.props.inventoryDeviceList} asynchronus
              columns={[
                { property: 'nodeId', title: 'Node Name', type: ColumnType.text },
              ]} idProperty="nodeId" >
            </InventoryDeviceListTable>
          </>
        }
      </>
    );
  }

  private onCloseRefreshInventoryDialog = () => {
    this.setState({
      refreshInventoryEditorMode: RefreshInventoryDialogMode.None,
    });
  };

  componentDidMount() {
    if (this.props.panelId === null) { //set default tab if none is set
      this.onTogglePanel('Equipment');
    }
  }
}

export const Dashboard = withRouter(connect(mapProps, mapDispatch)(DashboardSelectorComponent));
export default Dashboard;

