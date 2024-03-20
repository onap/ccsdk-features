<<<<<<< HEAD   (907af9 fix oauth code)
=======
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

import AddIcon from '@mui/icons-material/Add';
import ComputerIcon from '@mui/icons-material/Computer';
import EditIcon from '@mui/icons-material/Edit';
import Info from '@mui/icons-material/Info';
import LinkIcon from '@mui/icons-material/Link';
import LinkOffIcon from '@mui/icons-material/LinkOff';
import Refresh from '@mui/icons-material/Refresh';
import RemoveIcon from '@mui/icons-material/RemoveCircleOutline';
import { Divider, MenuItem, Typography } from '@mui/material';
import { Theme } from '@mui/material/styles';
import { WithStyles } from '@mui/styles';
import createStyles from '@mui/styles/createStyles';
import withStyles from '@mui/styles/withStyles';

import { NavigateToApplication } from '../../../../framework/src/actions/navigationActions';
import { ColumnType, MaterialTable, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { connect, Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { getAccessPolicyByUrl } from '../../../../framework/src/services/restService';

import { loadAllInfoElementAsync, loadAllInfoElementFeaturesAsync } from '../actions/infoNetworkElementActions';
import { createNetworkElementsActions, createNetworkElementsProperties } from '../handlers/networkElementsHandler';
import { NetworkElementConnection } from '../models/networkElementConnection';
import { ModuleSet, TopologyNode } from '../models/topologyNetconf';
import { connectService } from '../services/connectService';

import EditNetworkElementDialog, { EditNetworkElementDialogMode } from './editNetworkElementDialog';
import InfoNetworkElementDialog, { InfoNetworkElementDialogMode } from './infoNetworkElementDialog';
import RefreshNetworkElementsDialog, { RefreshNetworkElementsDialogMode } from './refreshNetworkElementsDialog';

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
    padding: '6px 6px',
    minWidth: 'unset',
  },
  spacer: {
    marginLeft: theme.spacing(1),
    marginRight: theme.spacing(1),
    display: 'inline',
  },
});

type GetStatelessComponentProps<T> = T extends (props: infer P & { children?: React.ReactNode }) => any ? P : any;
const MenuItemExt: React.FC<GetStatelessComponentProps<typeof MenuItem>> = (props) => {
  const [disabled, setDisabled] = React.useState(true);
  const onMouseDown = (ev: React.MouseEvent<HTMLElement>) => {
    if (ev.button === 1) {
      setDisabled(!disabled);
      ev.preventDefault();
    }
  };
  return (
    <div onMouseDown={onMouseDown} >
      <MenuItem {...{ ...props, disabled: props.disabled && disabled }} />
    </div>
  );
};

const mapProps = (state: IApplicationStoreState) => ({
  networkElementsProperties: createNetworkElementsProperties(state),
  applicationState: state,
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  networkElementsActions: createNetworkElementsActions(dispatcher.dispatch),
  navigateToApplication: (applicationName: string, path?: string) => dispatcher.dispatch(new NavigateToApplication(applicationName, path)),
  networkElementInfo: async (nodeId: string) => dispatcher.dispatch(loadAllInfoElementAsync(nodeId)),
  networkElementFeaturesInfo: async (nodeId: string) => dispatcher.dispatch(loadAllInfoElementFeaturesAsync(nodeId)),
});

type NetworkElementsListComponentProps = WithStyles<typeof styles> & Connect<typeof mapProps, typeof mapDispatch>;
type NetworkElementsListComponentState = {
  networkElementToEdit: NetworkElementConnection;
  networkElementEditorMode: EditNetworkElementDialogMode;
  refreshNetworkElementsEditorMode: RefreshNetworkElementsDialogMode;
  infoNetworkElementEditorMode: InfoNetworkElementDialogMode;
  elementInfo: TopologyNode | null;
  elementInfoFeature: ModuleSet | null;
};

const emptyRequireNetworkElement: NetworkElementConnection = { id: '', nodeId: '', host: '', port: 830, status: 'Disconnected', isRequired: true };
let initialSorted = false;
const NetworkElementTable = MaterialTable as MaterialTableCtorType<NetworkElementConnection>;

export class NetworkElementsListComponent extends React.Component<NetworkElementsListComponentProps, NetworkElementsListComponentState> {

  constructor(props: NetworkElementsListComponentProps) {
    super(props);

    this.state = {
      networkElementToEdit: emptyRequireNetworkElement,
      networkElementEditorMode: EditNetworkElementDialogMode.None,
      refreshNetworkElementsEditorMode: RefreshNetworkElementsDialogMode.None,
      elementInfo: null,
      elementInfoFeature: null,
      infoNetworkElementEditorMode: InfoNetworkElementDialogMode.None,
    };
  }

  getContextMenu(rowData: NetworkElementConnection): JSX.Element[] {
    const mountUri = rowData.id && connectService.getNetworkElementUri(rowData.id);
    const mountPolicy = mountUri && getAccessPolicyByUrl(mountUri);
    const canMount = mountPolicy && mountPolicy.POST || false;

    const { configuration } = this.props.applicationState as any;
    const buttonArray = [
      <MenuItemExt aria-label={'mount-button'} onClick={event => this.onOpenMountdNetworkElementsDialog(event, rowData)} disabled={!canMount} ><LinkIcon /><Typography>Mount</Typography></MenuItemExt>,
      <MenuItemExt aria-label={'unmount-button'} onClick={event => this.onOpenUnmountdNetworkElementsDialog(event, rowData)} disabled={!canMount} ><LinkOffIcon /><Typography>Unmount</Typography></MenuItemExt>,
      <Divider />,
      <MenuItem aria-label={'info-button'} onClick={event => this.onOpenInfoNetworkElementDialog(event, rowData)} disabled={rowData.status !== 'Connected'} ><Info /><Typography>Info</Typography></MenuItem>,
      <MenuItem aria-label={'edit-button'} onClick={event => this.onOpenEditNetworkElementDialog(event, rowData)}><EditIcon /><Typography>Edit</Typography></MenuItem>,
      <MenuItem aria-label={'remove-button'} onClick={event => this.onOpenRemoveNetworkElementDialog(event, rowData)} ><RemoveIcon /><Typography>Remove</Typography></MenuItem>,
      <Divider />,
      <MenuItem aria-label={'inventory-button'} onClick={() => this.props.navigateToApplication('inventory', rowData.nodeId)}><Typography>Inventory</Typography></MenuItem>,
      <Divider />,
      <MenuItem aria-label={'fault-button'} onClick={() => this.props.navigateToApplication('fault', rowData.nodeId)} ><Typography>Fault</Typography></MenuItem>,
      <MenuItem aria-label={'configure-button'} onClick={() => this.props.navigateToApplication('configuration', rowData.nodeId)} disabled={rowData.status === 'Connecting' || rowData.status === 'Disconnected' || !configuration}><Typography>Configure</Typography></MenuItem>,
      <MenuItem onClick={() => this.props.navigateToApplication('accounting', rowData.nodeId)} disabled={true}><Typography>Accounting</Typography></MenuItem>,
      <MenuItem aria-label={'performance-button'} onClick={() => this.props.navigateToApplication('performanceHistory', rowData.nodeId)}><Typography>Performance</Typography></MenuItem>,
      <MenuItem onClick={() => this.props.navigateToApplication('security', rowData.nodeId)} disabled={true} ><Typography>Security</Typography></MenuItem>,
    ];

    if (rowData.weburi) {
      // add an icon for gui cuttrough, if weburi is available
      return [<MenuItem aria-label={'web-client-button'} onClick={() => window.open(rowData.weburi, '_blank')} ><ComputerIcon /><Typography>Web Client</Typography></MenuItem>].concat(buttonArray);
    } else {
      return buttonArray;
    }
  }

  //  private navigationCreator

  render(): JSX.Element {
    //const { classes } = this.props;
    const { networkElementToEdit } = this.state;
    let savedRadio = 'password';
    if (this.state.networkElementToEdit.password && this.state.networkElementToEdit.password.length > 0) {
      savedRadio = 'password';
    } else if (this.state.networkElementToEdit.tlsKey && this.state.networkElementToEdit.tlsKey.length > 0) {
      savedRadio = 'tlsKey';
    }

    // const mountUri = rowData.id && connectService.getNetworkElementUri(rowData.id);
    // const mountPolicy = mountUri && getAccessPolicyByUrl(mountUri);
    // const canAdd =  mountPolicy && mountPolicy.POST || false;
    const canAdd = true;

    const addRequireNetworkElementAction = {
      icon: AddIcon, tooltip: 'Add node', ariaLabel: 'add-element', onClick: () => {
        this.setState({
          networkElementEditorMode: EditNetworkElementDialogMode.AddNewNetworkElement,
          networkElementToEdit: emptyRequireNetworkElement,
        });
      },
    };

    const refreshNetworkElementsAction = {
      icon: Refresh, tooltip: 'Refresh table', ariaLabel: 'refresh', onClick: () => {
        this.setState({
          refreshNetworkElementsEditorMode: RefreshNetworkElementsDialogMode.RefreshNetworkElementsTable,
        });
      },
    };

    return <>
      <NetworkElementTable stickyHeader tableId="network-element-table" customActionButtons={[refreshNetworkElementsAction, ...(canAdd ? [addRequireNetworkElementAction] : [])]} columns={[
        { property: 'nodeId', title: 'Node ID', type: ColumnType.text },
        { property: 'status', title: 'Connection Status', type: ColumnType.text, width:'15%' },
        { property: 'host', title: 'Host', type: ColumnType.text },
        { property: 'port', title: 'Port', type: ColumnType.numeric },
        { property: 'isRequired', title: 'Required', type: ColumnType.boolean },
        { property: 'deviceType', title: 'Type', type: ColumnType.text },
        { property: 'deviceFunction', title: 'Function', type: ColumnType.text, width: '25%' },
        { property: "coreModelCapability", title: "Core Model", type: ColumnType.text },
        { property: "mountMethod", title: "Mount Method", type: ColumnType.text },
      ]} idProperty="id" {...this.props.networkElementsActions} {...this.props.networkElementsProperties} asynchronus createContextMenu={rowData => {

        return this.getContextMenu(rowData);
      }} >
      </NetworkElementTable>
      <EditNetworkElementDialog 
        initialNetworkElement={networkElementToEdit}
        mode={this.state.networkElementEditorMode}
        onClose={this.onCloseEditNetworkElementDialog}
        radioChecked={savedRadio}
      />
      <RefreshNetworkElementsDialog
        mode={this.state.refreshNetworkElementsEditorMode}
        onClose={this.onCloseRefreshNetworkElementsDialog}
      />
      <InfoNetworkElementDialog
        initialNetworkElement={networkElementToEdit}
        mode={this.state.infoNetworkElementEditorMode}
        onClose={this.onCloseInfoNetworkElementDialog}
      />
    </>;
  }

  public componentDidMount() {
    if (!initialSorted) {
      initialSorted = true;
      this.props.networkElementsActions.onHandleRequestSort('node-id');
    } else {
      this.props.networkElementsActions.onRefresh();
    }
  }

  private onOpenAddNetworkElementDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.setState({
      networkElementToEdit: element,
      networkElementEditorMode: EditNetworkElementDialogMode.AddNewNetworkElement,
    });
  };

  private onOpenRemoveNetworkElementDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.setState({
      networkElementToEdit: element,
      networkElementEditorMode: EditNetworkElementDialogMode.RemoveNetworkElement,
    });
  };

  private onOpenEditNetworkElementDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    //let radioSaved;
    //if (element.password && element.password.length > 0)
    //  radioSaved = 'password';
    //else if (element.tlsKey && element.tlsKey.length > 0)
    //  radioSaved = 'tlsKey';
    this.setState({
      networkElementToEdit: {
        nodeId: element.nodeId,
        isRequired: element.isRequired,
        host: element.host,
        port: element.port,
        username: element.username,
        password: element.password,
        tlsKey: element.tlsKey,
      },
      networkElementEditorMode: EditNetworkElementDialogMode.EditNetworkElement,
    });
  };

  private onOpenUnmountdNetworkElementsDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.setState({
      networkElementToEdit: element,
      networkElementEditorMode: EditNetworkElementDialogMode.UnmountNetworkElement,
    });
  };

  private onOpenMountdNetworkElementsDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.setState({
      networkElementToEdit: element,
      networkElementEditorMode: EditNetworkElementDialogMode.MountNetworkElement,
    });
  };

  private onOpenInfoNetworkElementDialog = (event: React.MouseEvent<HTMLElement>, element: NetworkElementConnection) => {
    this.props.networkElementInfo(element.nodeId);
    this.props.networkElementFeaturesInfo(element.nodeId);
    this.setState({
      networkElementToEdit: element,
      infoNetworkElementEditorMode: InfoNetworkElementDialogMode.InfoNetworkElement,
    });
  };

  private onCloseEditNetworkElementDialog = () => {
    this.setState({
      networkElementEditorMode: EditNetworkElementDialogMode.None,
      networkElementToEdit: emptyRequireNetworkElement,
    });
  };

  private onCloseInfoNetworkElementDialog = () => {
    this.setState({
      infoNetworkElementEditorMode: InfoNetworkElementDialogMode.None,
      networkElementToEdit: emptyRequireNetworkElement,
    });
  };

  private onCloseRefreshNetworkElementsDialog = () => {
    this.setState({
      refreshNetworkElementsEditorMode: RefreshNetworkElementsDialogMode.None,
    });
  };
}

export const NetworkElementsList = withStyles(styles)(connect(mapProps, mapDispatch)(NetworkElementsListComponent));
>>>>>>> CHANGE (5418ff ODLUX Update)
