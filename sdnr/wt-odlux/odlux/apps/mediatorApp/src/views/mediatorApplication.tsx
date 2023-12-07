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
import { Theme, Tooltip } from '@mui/material';

import { WithStyles } from '@mui/styles';
import createStyles from '@mui/styles/createStyles';
import withStyles from '@mui/styles/withStyles';

import AddIcon from '@mui/icons-material/Add';
import IconButton from '@mui/material/IconButton';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import InfoIcon from '@mui/icons-material/Info';
import StartIcon from '@mui/icons-material/PlayArrow';
import StopIcon from '@mui/icons-material/Stop';

import CircularProgress from '@mui/material/CircularProgress'

import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { connect, Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import MaterialTable, { MaterialTableCtorType, ColumnType } from '../../../../framework/src/components/material-table';

import { MediatorConfig, BusySymbol, MediatorConfigResponse } from '../models/mediatorServer';
import EditMediatorConfigDialog, { EditMediatorConfigDialogMode } from '../components/editMediatorConfigDialog';
import { startMediatorByNameAsyncActionCreator, stopMediatorByNameAsyncActionCreator } from '../actions/mediatorConfigActions';
import mediatorService from '../services/mediatorService';
import { ShowMediatorInfoDialog, MediatorInfoDialogMode } from '../components/showMeditaorInfoDialog'

const styles = (theme: Theme) => createStyles({
  root: {
    display: 'flex',
    flexDirection: 'column',
    flex: '1',
  },
  formControl: {
    margin: theme.spacing(1),
    minWidth: 300,
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
  },
  progress: {
    flex: '1 1 100%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    pointerEvents: 'none'
  }
});

const mapProps = (state: IApplicationStoreState) => ({
  serverName: state.mediator.mediatorServerState.name,
  serverUrl: state.mediator.mediatorServerState.url,
  serverId: state.mediator.mediatorServerState.id,
  serverVersion: state.mediator.mediatorServerState.serverVersion,
  mediatorVersion: state.mediator.mediatorServerState.mediatorVersion,
  configurations: state.mediator.mediatorServerState.configurations,
  supportedDevices: state.mediator.mediatorServerState.supportedDevices,
  busy: state.mediator.mediatorServerState.busy,
  isReachable: state.mediator.mediatorServerState.isReachable
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  startMediator: (name: string) => dispatcher.dispatch(startMediatorByNameAsyncActionCreator(name)),
  stopMediator: (name: string) => dispatcher.dispatch(stopMediatorByNameAsyncActionCreator(name)),
});

const emptyMediatorConfig: MediatorConfig = {
  Name: "",
  DeviceIp: "127.0.0.1",
  DevicePort: 161,
  NcUsername: "admin",
  NcPassword: "admin",
  DeviceType: -1,
  NcPort: 4020,
  TrapPort: 10020,
  NeXMLFile: "",
  ODLConfig: []
};

const MediatorServerConfigurationsTable = MaterialTable as MaterialTableCtorType<MediatorConfigResponse>;
const MediatorServerUnreachableTable = MaterialTable as MaterialTableCtorType<{ Name: string, status: string, ipAdress: string, device: string, actions: string }>

type MediatorApplicationComponentProps = Connect<typeof mapProps, typeof mapDispatch> & WithStyles<typeof styles>;

type MediatorServerSelectionComponentState = {
  busy: boolean,
  mediatorConfigToEdit: MediatorConfig,
  mediatorConfigEditorMode: EditMediatorConfigDialogMode,
  mediatorShowInfoMode: MediatorInfoDialogMode,
  mediatorConfigToDisplay: MediatorConfigResponse | null
}

class MediatorApplicationComponent extends React.Component<MediatorApplicationComponentProps, MediatorServerSelectionComponentState> {

  constructor(props: MediatorApplicationComponentProps) {
    super(props);

    this.state = {
      busy: false,
      mediatorConfigToEdit: emptyMediatorConfig,
      mediatorConfigEditorMode: EditMediatorConfigDialogMode.None,
      mediatorShowInfoMode: MediatorInfoDialogMode.None,
      mediatorConfigToDisplay: null
    }
  }

  render() {
    const { classes } = this.props;

    const renderActions = (rowData: MediatorConfigResponse) => (
      <>
        <div className={classes.spacer}>
          <Tooltip disableInteractive title={"Start"} >
            <IconButton disabled={rowData[BusySymbol]} className={classes.button} size="large">
              <StartIcon onClick={(event) => { event.preventDefault(); event.stopPropagation(); this.props.startMediator(rowData.Name); }} />
            </IconButton>
          </Tooltip>
          <Tooltip disableInteractive title={"Stop"} >
            <IconButton disabled={rowData[BusySymbol]} className={classes.button} size="large">
              <StopIcon onClick={(event) => { event.preventDefault(); event.stopPropagation(); this.props.stopMediator(rowData.Name); }} />
            </IconButton>
          </Tooltip>
        </div>
        <div className={classes.spacer}>
          <Tooltip disableInteractive title={"Info"} ><IconButton
            className={classes.button}
            onClick={(event) => { this.onOpenInfoDialog(event, rowData) }}
            size="large"><InfoIcon /></IconButton></Tooltip>
        </div>
        <div className={classes.spacer}>
          {process.env.NODE_ENV === "development" ? <Tooltip disableInteractive title={"Edit"} ><IconButton
            disabled={rowData[BusySymbol]}
            className={classes.button}
            onClick={event => this.onOpenEditConfigurationDialog(event, rowData)}
            size="large"><EditIcon /></IconButton></Tooltip> : null}
          <Tooltip disableInteractive title={"Remove"} ><IconButton
            disabled={rowData[BusySymbol]}
            className={classes.button}
            onClick={event => this.onOpenRemoveConfigutationDialog(event, rowData)}
            size="large"><DeleteIcon /></IconButton></Tooltip>
        </div>
      </>
    );

    const addMediatorConfigAction = { icon: AddIcon, tooltip: 'Add', ariaLabel: 'add-element', onClick: this.onOpenAddConfigurationDialog };

    return (
      <div className={classes.root}>
        {this.props.busy || this.state.busy
          ? <div className={classes.progress}> <CircularProgress color={"secondary"} size={48} /> </div>
          :

          this.props.isReachable ?

            <MediatorServerConfigurationsTable defaultSortColumn={"Name"} tableId={null} defaultSortOrder="asc" stickyHeader title={this.props.serverName || ''} customActionButtons={[addMediatorConfigAction]} idProperty={"Name"} rows={this.props.configurations} asynchronus columns={[
              { property: "Name", title: "Mediator", type: ColumnType.text },
              { property: "Status", title: "Status", type: ColumnType.custom, customControl: ({ rowData }) => rowData.pid ? (<span>Running</span>) : (<span>Stopped</span>) },
              { property: "DeviceIp", title: "IP Adress", type: ColumnType.text },
              {
                property: "Device", title: "Device", type: ColumnType.custom, customControl: ({ rowData }) => {
                  const dev = this.props.supportedDevices && this.props.supportedDevices.find(dev => dev.id === rowData.DeviceType);
                  return (
                    <span> {dev && `${dev.vendor} - ${dev.device} (${dev.version || '0.0.0'})`} </span>
                  )
                }
              },
              { property: "actions", title: "Actions", type: ColumnType.custom, customControl: ({ rowData }) => renderActions(rowData) },
            ]} />
            :
            <MediatorServerUnreachableTable title={this.props.serverName || ''} tableId={null} idProperty={"Name"} disableFilter={true} disableSorting={true} enableSelection={false} rows={[{ Name: '', status: "Mediator server not found.", ipAdress: '', device: '', actions: '' }]} columns={[
              { property: "Name", title: "Mediator", type: ColumnType.text },
              { property: "status", title: "Status", type: ColumnType.text },
              { property: "ipAdress", title: "IP Adress", type: ColumnType.text },
              { property: "device", title: "Device", type: ColumnType.text },
              { property: "actions", title: "Actions", type: ColumnType.text },

            ]} />
        }

        <EditMediatorConfigDialog
          mediatorConfig={this.state.mediatorConfigToEdit}
          mode={this.state.mediatorConfigEditorMode}
          onClose={this.onCloseEditMediatorConfigDialog} />

        {

          this.state.mediatorShowInfoMode != MediatorInfoDialogMode.None &&
          <ShowMediatorInfoDialog
            config={this.state.mediatorConfigToDisplay as MediatorConfigResponse}
            mode={this.state.mediatorShowInfoMode}
            onClose={this.onCloseInfoDialog} />
        }
      </div>
    );
  }

  private onOpenInfoDialog = (event: React.MouseEvent<HTMLElement>, configEntry: MediatorConfigResponse) => {
    event.stopPropagation();
    event.preventDefault();
    this.setState({ mediatorShowInfoMode: MediatorInfoDialogMode.ShowDetails, mediatorConfigToDisplay: configEntry })
  }

  private onCloseInfoDialog = () => {
    this.setState({ mediatorShowInfoMode: MediatorInfoDialogMode.None, mediatorConfigToDisplay: null })
  }

  private onOpenAddConfigurationDialog = () => {
    // Tries to determine a free port for netconf listener and snpm listener
    // it it could not determine free ports the dialog will open any way
    // those ports should not be configured from the fontend, furthermore
    // the backend should auto configure them and tell the user the result
    // after the creation process.
    this.setState({
      busy: true,
    });
    this.props.serverId && Promise.all([
      mediatorService.getMediatorServerFreeNcPorts(this.props.serverId, 1),
      mediatorService.getMediatorServerFreeSnmpPorts(this.props.serverId, 1),
    ]).then(([freeNcPorts, freeSnmpPorts]) => {
      if (freeNcPorts && freeSnmpPorts && freeNcPorts.length > 0 && freeSnmpPorts.length > 0) {
        this.setState({
          busy: false,
          mediatorConfigEditorMode: EditMediatorConfigDialogMode.AddMediatorConfig,
          mediatorConfigToEdit: {
            ...emptyMediatorConfig,
            NcPort: freeNcPorts[0],
            TrapPort: freeSnmpPorts[0],
          },
        });
      } else {
        this.setState({
          busy: false,
          mediatorConfigEditorMode: EditMediatorConfigDialogMode.AddMediatorConfig,
          mediatorConfigToEdit: { ...emptyMediatorConfig },
        });
      }
    })

  }

  private onOpenEditConfigurationDialog = (event: React.MouseEvent<HTMLElement>, configEntry: MediatorConfig) => {
    event.preventDefault();
    event.stopPropagation();
    this.setState({
      mediatorConfigEditorMode: EditMediatorConfigDialogMode.EditMediatorConfig,
      mediatorConfigToEdit: configEntry,
    });
  }

  private onOpenRemoveConfigutationDialog = (event: React.MouseEvent<HTMLElement>, configEntry: MediatorConfig) => {
    event.preventDefault();
    event.stopPropagation();
    this.setState({
      mediatorConfigEditorMode: EditMediatorConfigDialogMode.RemoveMediatorConfig,
      mediatorConfigToEdit: configEntry,
    });
  }

  private onCloseEditMediatorConfigDialog = () => {
    this.setState({
      mediatorConfigEditorMode: EditMediatorConfigDialogMode.None,
      mediatorConfigToEdit: emptyMediatorConfig,
    });
  }
}

export const MediatorApplication = withStyles(styles)(connect(mapProps, mapDispatch)(MediatorApplicationComponent));
