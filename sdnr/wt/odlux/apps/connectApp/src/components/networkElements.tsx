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

import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';

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
    let counter = 0;
    return (
      <>
        <NetworkElementTable tableId="network-element-table" customActionButtons={[addRequireNetworkElementAction]} columns={[
          { property: "nodeId", title: "Node Name", type: ColumnType.text },
          { property: "isRequired", title: "Required", type: ColumnType.boolean },
          { property: "status", title: "Connection Status", type: ColumnType.text },
          { property: "host", title: "Host", type: ColumnType.text },
          { property: "port", title: "Port", type: ColumnType.numeric },
          { property: "coreModelCapability", title: "Core Model", type: ColumnType.text },
          { property: "deviceType", title: "Type", type: ColumnType.text },
          {
            property: "actions", title: "Actions", type: ColumnType.custom, customControl: ({ rowData }) => {
              counter++;
              return (
                <>
                  <div className={classes.spacer}>
                    {
                      rowData.webUri && <Tooltip title={"Web Client"} ><IconButton aria-label={"web-client-button-" + counter} className={classes.button} onClick={event => { console.log(rowData); window.open(rowData.webUri, "_blank") }}><ComputerIcon /></IconButton></Tooltip>
                    }
                    <Tooltip title={"Mount"} >
                      <IconButton aria-label={"mount-button-" + counter} className={classes.button} onClick={event => this.onOpenMountdNetworkElementsDialog(event, rowData)} >
                        <LinkIcon /></IconButton>
                    </Tooltip>
                    <Tooltip title={"Unmount"} >
                      <IconButton aria-label={"unmount-button-" + counter} className={classes.button} onClick={event => this.onOpenUnmountdNetworkElementsDialog(event, rowData)} >
                        <LinkOffIcon /></IconButton>
                    </Tooltip>
                    <Tooltip title={"Info"} ><IconButton aria-label={"info-button-" + counter} className={classes.button} onClick={event => this.onOpenInfoNetworkElementDialog(event, rowData)} disabled={rowData.status === "Connecting" || rowData.status === "Disconnected"} >
                      <Info /></IconButton>
                    </Tooltip>
                    <Tooltip title={"Edit"} ><IconButton aria-label={"edit-button-" + counter} className={classes.button} onClick={event => this.onOpenEditNetworkElementDialog(event, rowData)} ><EditIcon /></IconButton></Tooltip>
                    <Tooltip title={"Remove"} ><IconButton aria-label={"remove-button-" + counter} className={classes.button} onClick={event => this.onOpenRemoveNetworkElementDialog(event, rowData)} ><RemoveIcon /></IconButton></Tooltip>
                  </div>
                  <div className={classes.spacer}>
                    <Tooltip title={"Inventory"} ><Button aria-label={"inventory-button-" + counter} className={classes.button} onClick={this.navigateToApplicationHandlerCreator("inventory", rowData)} >I</Button></Tooltip>
                  </div>
                  <div className={classes.spacer}>
                    <Tooltip title={"Fault"} ><Button aria-label={"fault-button-" + counter} className={classes.button} onClick={this.navigateToApplicationHandlerCreator("fault", rowData)} >F</Button></Tooltip>
                    <Tooltip title={"Configure"} ><Button aria-label={"configure-button-" + counter} className={classes.button} onClick={this.navigateToApplicationHandlerCreator("configuration", rowData)} >C</Button></Tooltip>
                    <Tooltip title={"Accounting "} ><Button className={classes.button} onClick={this.navigateToApplicationHandlerCreator("accounting", rowData)} disabled={true} >A</Button></Tooltip>
                    <Tooltip title={"Performance"} ><Button aria-label={"performance-button-" + counter} className={classes.button} onClick={this.navigateToApplicationHandlerCreator("performanceHistory", rowData)}>P</Button></Tooltip>
                    <Tooltip title={"Security"} ><Button className={classes.button} onClick={this.navigateToApplicationHandlerCreator("security", rowData)} disabled={true} >S</Button></Tooltip>
                  </div>
                </>
              )
            }
          },
        ]} idProperty="id" {...this.props.networkElementsActions} {...this.props.networkElementsProperties} asynchronus >
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

  private onOpenRemoveNetworkElementDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.setState({
      networkElementToEdit: element,
      networkElementEditorMode: EditNetworkElementDialogMode.RemoveNetworkElement
    });
    event.preventDefault();
    event.stopPropagation();
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
    event.preventDefault();
    event.stopPropagation();
  }

  private onOpenUnmountdNetworkElementsDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.setState({
      networkElementToEdit: element,
      networkElementEditorMode: EditNetworkElementDialogMode.UnmountNetworkElement
    });
    event.preventDefault();
    event.stopPropagation();
  }

  private onOpenMountdNetworkElementsDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.setState({
      networkElementToEdit: element,
      networkElementEditorMode: EditNetworkElementDialogMode.MountNetworkElement
    });
    event.preventDefault();
    event.stopPropagation();
  }

  private onOpenInfoNetworkElementDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.props.networkElementInfo(element.nodeId);
    this.setState({
      networkElementToEdit: element,
      infoNetworkElementEditorMode: InfoNetworkElementDialogMode.InfoNetworkElement,
    });
    event.preventDefault();
    event.stopPropagation();
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

  private navigateToApplicationHandlerCreator = (applicationName: string, element: NetworkElementConnection) => (event: React.MouseEvent<HTMLElement>) => {
    this.props.navigateToApplication(applicationName, element.nodeId);
    event.preventDefault();
    event.stopPropagation();
  }
}

export const NetworkElementsList = withStyles(styles)(connect(mapProps, mapDispatch)(NetworkElementsListComponent));
export default NetworkElementsList;