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
import { Theme, createStyles, withStyles, WithStyles } from '@material-ui/core/styles';

import AddIcon from '@material-ui/icons/Add';
import LinkIcon from '@material-ui/icons/Link';
import LinkOffIcon from '@material-ui/icons/LinkOff';
import RemoveIcon from '@material-ui/icons/RemoveCircleOutline';
import EditIcon from '@material-ui/icons/Edit';
import Info from '@material-ui/icons/Info';
import ComputerIcon from '@material-ui/icons/Computer';

import { MaterialTable, ColumnType, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { connect, Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { NavigateToApplication } from '../../../../framework/src/actions/navigationActions';

import { createNetworkElementsActions, createNetworkElementsProperties } from '../handlers/networkElementsHandler';

import { NetworkElementConnection } from '../models/networkElementConnection';
import EditNetworkElementDialog, { EditNetworkElementDialogMode } from './editNetworkElementDialog';

import InfoNetworkElementDialog, { InfoNetworkElementDialogMode } from './infoNetworkElementDialog';
import { loadAllInfoElementAsync } from '../actions/infoNetworkElementActions';
import { TopologyNode } from '../models/topologyNetconf';
import { MenuItem, Divider, Typography } from '@material-ui/core';

const styles = (theme: Theme) => createStyles({
  connectionStatusConnected: {
    color: 'darkgreen',
  },
  connectionStatusConnecting: {
    color: 'blue',
  },
  connectionStatusDisconnected: {
    color: 'red',
  },
  button: {
    margin: 0,
    padding: "6px 6px",
    minWidth: 'unset'
  },
  spacer: {
    marginLeft: theme.spacing(1),
    marginRight: theme.spacing(1),
    display: "inline"
  }
});

const mapProps = (state: IApplicationStoreState) => ({
  networkElementsProperties: createNetworkElementsProperties(state),
  applicationState: state,
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  networkElementsActions: createNetworkElementsActions(dispatcher.dispatch),
  navigateToApplication: (applicationName: string, path?: string) => dispatcher.dispatch(new NavigateToApplication(applicationName, path)),
  networkElementInfo: async (nodeId: string) => await dispatcher.dispatch(loadAllInfoElementAsync(nodeId)),
});

type NetworkElementsListComponentProps = WithStyles<typeof styles> & Connect<typeof mapProps, typeof mapDispatch>;
type NetworkElementsListComponentState = {
  networkElementToEdit: NetworkElementConnection,
  networkElementEditorMode: EditNetworkElementDialogMode,
  infoNetworkElementEditorMode: InfoNetworkElementDialogMode,
  elementInfo: TopologyNode | null
}

const emptyRequireNetworkElement: NetworkElementConnection = { id: "", nodeId: "", host: "", port: 0, status: "Disconnected", isRequired: false };

const NetworkElementTable = MaterialTable as MaterialTableCtorType<NetworkElementConnection>;

export class NetworkElementsListComponent extends React.Component<NetworkElementsListComponentProps, NetworkElementsListComponentState> {

  constructor(props: NetworkElementsListComponentProps) {
    super(props);

    this.state = {
      networkElementToEdit: emptyRequireNetworkElement,
      networkElementEditorMode: EditNetworkElementDialogMode.None,
      elementInfo: null,
      infoNetworkElementEditorMode: InfoNetworkElementDialogMode.None
    };
  }

  getContextMenu(rowData: NetworkElementConnection): JSX.Element[] {



    const { configuration, fault, inventory } = this.props.applicationState as any;
    let buttonArray = [
      <MenuItem aria-label={"mount-button"} onClick={event => this.onOpenMountdNetworkElementsDialog(event, rowData)} ><LinkIcon /><Typography>Mount</Typography></MenuItem>,
      <MenuItem aria-label={"unmount-button"} onClick={event => this.onOpenUnmountdNetworkElementsDialog(event, rowData)}><LinkOffIcon /><Typography>Unmount</Typography></MenuItem>,
      <Divider />,
      <MenuItem aria-label={"info-button"} onClick={event => this.onOpenInfoNetworkElementDialog(event, rowData)} disabled={rowData.status === "Connecting" || rowData.status === "Disconnected"} ><Info /><Typography>Info</Typography></MenuItem>,
      <MenuItem aria-label={"edit-button"} onClick={event => this.onOpenEditNetworkElementDialog(event, rowData)}><EditIcon /><Typography>Edit</Typography></MenuItem>,
      <MenuItem aria-label={"remove-button"} onClick={event => this.onOpenRemoveNetworkElementDialog(event, rowData)} ><RemoveIcon /><Typography>Remove</Typography></MenuItem>,
      <Divider />,
      <MenuItem aria-label={"inventory-button"} onClick={event => this.props.navigateToApplication("inventory", rowData.nodeId)}><Typography>Inventory</Typography></MenuItem>,
      <Divider />,
      <MenuItem aria-label={"fault-button"} onClick={event => this.props.navigateToApplication("fault", rowData.nodeId)} ><Typography>Fault</Typography></MenuItem>,
      <MenuItem aria-label={"configure-button"} onClick={event => this.props.navigateToApplication("configuration", rowData.nodeId)} disabled={rowData.status === "Connecting" || rowData.status === "Disconnected" || !configuration}><Typography>Configure</Typography></MenuItem>,
      <MenuItem onClick={event => this.props.navigateToApplication("accounting", rowData.nodeId)} disabled={true}><Typography>Accounting</Typography></MenuItem>,
      <MenuItem aria-label={"performance-button"} onClick={event => this.props.navigateToApplication("performanceHistory", rowData.nodeId)}><Typography>Performance</Typography></MenuItem>,
      <MenuItem onClick={event => this.props.navigateToApplication("security", rowData.nodeId)} disabled={true} ><Typography>Security</Typography></MenuItem>,
    ];

    if (rowData.webUri) {
      // add an icon for gui cuttrough, if weburi is available
      return [<MenuItem aria-label={"web-client-button"} onClick={event => window.open(rowData.webUri, "_blank")} ><ComputerIcon /><Typography>Web Client</Typography></MenuItem>].concat(buttonArray)
    } else {
      return buttonArray;
    }
  }

  //  private navigationCreator

  render(): JSX.Element {
    const { classes } = this.props;
    const { networkElementToEdit } = this.state;
    const addRequireNetworkElementAction = {
      icon: AddIcon, tooltip: 'Add', onClick: () => {
        this.setState({
          networkElementEditorMode: EditNetworkElementDialogMode.AddNewNetworkElement,
          networkElementToEdit: emptyRequireNetworkElement,
        });
      }
    };

    return (
      <>
        <NetworkElementTable stickyHeader tableId="network-element-table" customActionButtons={[addRequireNetworkElementAction]} columns={[
          { property: "nodeId", title: "Node Name", type: ColumnType.text },
          { property: "isRequired", title: "Required", type: ColumnType.boolean },
          { property: "status", title: "Connection Status", type: ColumnType.text },
          { property: "host", title: "Host", type: ColumnType.text },
          { property: "port", title: "Port", type: ColumnType.numeric },
          { property: "coreModelCapability", title: "Core Model", type: ColumnType.text },
          { property: "deviceType", title: "Type", type: ColumnType.text },
        ]} idProperty="id" {...this.props.networkElementsActions} {...this.props.networkElementsProperties} asynchronus createContextMenu={rowData => {

          return this.getContextMenu(rowData);
        }} >
        </NetworkElementTable>
        <EditNetworkElementDialog
          initialNetworkElement={networkElementToEdit}
          mode={this.state.networkElementEditorMode}
          onClose={this.onCloseEditNetworkElementDialog}
        />
        <InfoNetworkElementDialog
          initialNetworkElement={networkElementToEdit}
          mode={this.state.infoNetworkElementEditorMode}
          onClose={this.onCloseInfoNetworkElementDialog}
        />
      </>
    );
  };

  public componentDidMount() {
    this.props.networkElementsActions.onRefresh();
  }

  private onOpenAddNetworkElementDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.setState({
      networkElementToEdit: element,
      networkElementEditorMode: EditNetworkElementDialogMode.AddNewNetworkElement
    });
  }

  private onOpenRemoveNetworkElementDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.setState({
      networkElementToEdit: element,
      networkElementEditorMode: EditNetworkElementDialogMode.RemoveNetworkElement
    });
  }

  private onOpenEditNetworkElementDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.setState({
      networkElementToEdit: {
        nodeId: element.nodeId,
        isRequired: element.isRequired,
        host: element.host,
        port: element.port,
        username: element.username,
        password: element.password,
      },
      networkElementEditorMode: EditNetworkElementDialogMode.EditNetworkElement
    });
  }

  private onOpenUnmountdNetworkElementsDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.setState({
      networkElementToEdit: element,
      networkElementEditorMode: EditNetworkElementDialogMode.UnmountNetworkElement
    });
  }

  private onOpenMountdNetworkElementsDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.setState({
      networkElementToEdit: element,
      networkElementEditorMode: EditNetworkElementDialogMode.MountNetworkElement
    });
  }

  private onOpenInfoNetworkElementDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.props.networkElementInfo(element.nodeId);
    this.setState({
      networkElementToEdit: element,
      infoNetworkElementEditorMode: InfoNetworkElementDialogMode.InfoNetworkElement,
    });
  }

  private onCloseEditNetworkElementDialog = () => {
    this.setState({
      networkElementEditorMode: EditNetworkElementDialogMode.None,
      networkElementToEdit: emptyRequireNetworkElement,
    });
  }
  private onCloseInfoNetworkElementDialog = () => {
    this.setState({
      infoNetworkElementEditorMode: InfoNetworkElementDialogMode.None,
      networkElementToEdit: emptyRequireNetworkElement,
    });
  }
}

export const NetworkElementsList = withStyles(styles)(connect(mapProps, mapDispatch)(NetworkElementsListComponent));
export default NetworkElementsList;