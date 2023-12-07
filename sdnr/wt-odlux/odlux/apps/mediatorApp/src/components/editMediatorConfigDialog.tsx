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
import { Theme, Typography, FormControlLabel, Checkbox } from '@mui/material';

import { WithStyles } from '@mui/styles';
import createStyles from '@mui/styles/createStyles';
import withStyles from '@mui/styles/withStyles';

import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import Select from '@mui/material/Select';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';

import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';

import Fab from '@mui/material/Fab';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import IconButton from '@mui/material/IconButton';

import { addMediatorConfigAsyncActionCreator, updateMediatorConfigAsyncActionCreator, removeMediatorConfigAsyncActionCreator } from '../actions/mediatorConfigActions';
import { MediatorConfig, ODLConfig } from '../models/mediatorServer';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';

import { Panel } from '../../../../framework/src/components/material-ui/panel';

import { IDispatcher, connect, Connect } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';


const styles = (theme: Theme) => createStyles({
  root: {
    display: 'flex',
    flexDirection: 'column',
    flex: '1',
  },
  fab: {
    position: 'absolute',
    bottom: theme.spacing(1),
    right: theme.spacing(1),
  },
  title: {
    fontSize: 14,
  },
  center: {
    flex: "1",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
  },
  alignInOneLine: {
    display: 'flex',
    flexDirection: 'row'
  },
  left: {
    marginRight: theme.spacing(1),
  },
  right: {
    marginLeft: 0,
  }
});

const TabContainer: React.SFC = ({ children }) => {
  return (
    <div style={{ width: "430px", height: "530px", position: "relative", display: 'flex', flexDirection: 'column' }}>
      {children}
    </div>
  );
}

export enum EditMediatorConfigDialogMode {
  None = "none",
  AddMediatorConfig = "addMediatorConfig",
  EditMediatorConfig = "editMediatorConfig",
  RemoveMediatorConfig = "removeMediatorConfig",
}

const mapProps = (state: IApplicationStoreState) => ({
  supportedDevices: state.mediator.mediatorServerState.supportedDevices
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  addMediatorConfig: (config: MediatorConfig) => {
    dispatcher.dispatch(addMediatorConfigAsyncActionCreator(config));
  },
  updateMediatorConfig: (config: MediatorConfig) => {
    dispatcher.dispatch(updateMediatorConfigAsyncActionCreator(config));
  },
  removeMediatorConfig: (config: MediatorConfig) => {
    dispatcher.dispatch(removeMediatorConfigAsyncActionCreator(config));
  },
});

type DialogSettings = {
  dialogTitle: string;
  dialogDescription: string;
  applyButtonText: string;
  cancelButtonText: string;
  readonly: boolean;
  readonlyName: boolean;
};

const settings: { [key: string]: DialogSettings } = {
  [EditMediatorConfigDialogMode.None]: {
    dialogTitle: "",
    dialogDescription: "",
    applyButtonText: "",
    cancelButtonText: "",
    readonly: true,
    readonlyName: true,
  },
  [EditMediatorConfigDialogMode.AddMediatorConfig]: {
    dialogTitle: "Add Mediator Configuration",
    dialogDescription: "",
    applyButtonText: "Add",
    cancelButtonText: "Cancel",
    readonly: false,
    readonlyName: false,
  },
  [EditMediatorConfigDialogMode.EditMediatorConfig]: {
    dialogTitle: "Edit Mediator Configuration",
    dialogDescription: "",
    applyButtonText: "Update",
    cancelButtonText: "Cancel",
    readonly: false,
    readonlyName: true,
  },
  [EditMediatorConfigDialogMode.RemoveMediatorConfig]: {
    dialogTitle: "Remove Mediator Configuration",
    dialogDescription: "",
    applyButtonText: "Remove",
    cancelButtonText: "Cancel",
    readonly: true,
    readonlyName: true,
  },
};

type EditMediatorConfigDialogComponentProps = WithStyles<typeof styles> & Connect<typeof mapProps, typeof mapDispatch> & {
  mode: EditMediatorConfigDialogMode;
  mediatorConfig: MediatorConfig;
  onClose: () => void;
};

type EditMediatorConfigDialogComponentState = MediatorConfig & { activeTab: number; activeOdlConfig: string, forceAddOdlConfig: boolean, isOdlConfigHostnameEmpty: boolean };

class EditMediatorConfigDialogComponent extends React.Component<EditMediatorConfigDialogComponentProps, EditMediatorConfigDialogComponentState> {
  constructor(props: EditMediatorConfigDialogComponentProps) {
    super(props);

    this.state = {
      ...this.props.mediatorConfig,
      activeTab: 0,
      activeOdlConfig: "",
      forceAddOdlConfig: false,
      isOdlConfigHostnameEmpty: false
    };
  }

  private odlConfigValueChangeHandlerCreator = <THtmlElement extends HTMLElement, TKey extends keyof ODLConfig>(index: number, property: TKey, mapValue: (event: React.ChangeEvent<THtmlElement>) => any) => (event: React.ChangeEvent<THtmlElement>) => {
    event.stopPropagation();
    event.preventDefault();
    this.setState({
      ODLConfig: [
        ...this.state.ODLConfig.slice(0, index),
        { ...this.state.ODLConfig[index], [property]: mapValue(event) },
        ...this.state.ODLConfig.slice(index + 1)
      ]
    });
  }

  render(): JSX.Element {
    const setting = settings[this.props.mode];
    const { classes } = this.props;
    return (
      <Dialog open={this.props.mode !== EditMediatorConfigDialogMode.None}>
        <DialogTitle id="form-dialog-title">{setting.dialogTitle}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {setting.dialogDescription}
          </DialogContentText>
          <Tabs value={this.state.activeTab} indicatorColor="secondary" textColor="secondary" onChange={(event, value) => this.setState({ activeTab: value })} >
            <Tab label="Config" />
            <Tab label="ODL AutoConnect" />
          </Tabs>
          {this.state.activeTab === 0 ? <TabContainer >
            <TextField variant="standard" disabled={setting.readonly || setting.readonlyName} spellCheck={false} autoFocus margin="dense" id="name" label="Name" type="text" fullWidth value={this.state.Name} onChange={(event) => { this.setState({ Name: event.target.value }); }} />
            <FormControl variant="standard" fullWidth disabled={setting.readonly}>
              <InputLabel htmlFor="deviceType">Device</InputLabel>
              <Select variant="standard" value={this.state.DeviceType} onChange={(event, value) => {
                const device = this.props.supportedDevices.find(device => device.id === event.target.value);
                if (device) {
                  this.setState({
                    DeviceType: device.id,
                    NeXMLFile: device.xml
                  });
                } else {
                  this.setState({
                    DeviceType: -1,
                    NeXMLFile: ""
                  });
                }
              }} inputProps={{ name: 'deviceType', id: 'deviceType' }} fullWidth >
                <MenuItem value={-1}><em>None</em></MenuItem>
                {this.props.supportedDevices.map(device => (<MenuItem key={device.id} value={device.id} >{`${device.vendor} - ${device.device} (${device.version || '0.0.0'}) `}</MenuItem>))}
              </Select>
            </FormControl>
            <TextField variant="standard" disabled={setting.readonly} spellCheck={false} autoFocus margin="dense" id="ipAddress" label="Device IP" type="text" fullWidth value={this.state.DeviceIp} onChange={(event) => { this.setState({ DeviceIp: event.target.value }); }} />
            <TextField variant="standard" disabled={setting.readonly} spellCheck={false} autoFocus margin="dense" id="devicePort" label="Device SNMP Port" type="number" fullWidth value={this.state.DevicePort || ""} onChange={(event) => { this.setState({ DevicePort: +event.target.value }); }} />
            <TextField variant="standard" disabled={setting.readonly} spellCheck={false} autoFocus margin="dense" id="trapsPort" label="TrapsPort" type="number" fullWidth value={this.state.TrapPort || ""} onChange={(event) => { this.setState({ TrapPort: +event.target.value }); }} />
            <TextField variant="standard" disabled={setting.readonly} spellCheck={false} autoFocus margin="dense" id="ncUser" label="Netconf User" type="text" fullWidth value={this.state.NcUsername} onChange={(event) => { this.setState({ NcUsername: event.target.value }); }} />
            <TextField variant="standard" disabled={setting.readonly} spellCheck={false} autoFocus margin="dense" id="ncPassword" label="Netconf Password" type="password" fullWidth value={this.state.NcPassword} onChange={(event) => { this.setState({ NcPassword: event.target.value }); }} />
            <TextField variant="standard" disabled={setting.readonly} spellCheck={false} autoFocus margin="dense" id="ncPort" label="Netconf Port" type="number" fullWidth value={this.state.NcPort || ""} onChange={(event) => { this.setState({ NcPort: +event.target.value }); }} />
          </TabContainer> : null}
          {this.state.activeTab === 1 ? <TabContainer >
            {this.state.ODLConfig && this.state.ODLConfig.length > 0
              ? this.state.ODLConfig.map((cfg, ind) => {
                const panelId = `panel-${ind}`;
                const deleteButton = (<IconButton
                  onClick={() => {
                    this.setState({
                      ODLConfig: [
                        ...this.state.ODLConfig.slice(0, ind),
                        ...this.state.ODLConfig.slice(ind + 1)
                      ]
                    });
                  }}
                  size="large"><DeleteIcon /></IconButton>)
                return (
                  <Panel title={cfg.Server && `${cfg.User ? `${cfg.User}@` : ''}${cfg.Protocol}://${cfg.Server}:${cfg.Port}` || "new odl config"} key={panelId} panelId={panelId} activePanel={this.state.activeOdlConfig} customActionButtons={[deleteButton]}
                    onToggle={(id) => { this.setState({ activeOdlConfig: (this.state.activeOdlConfig === id) ? "" : (id || "") }); console.log("activeOdlConfig " + id); this.hideHostnameErrormessage(id) }} >
                    <div className={classes.alignInOneLine}>
                      <FormControl variant="standard" className={classes.left} margin={"dense"} >
                        <InputLabel htmlFor={`protocol-${ind}`}>Protocoll</InputLabel>
                        <Select variant="standard"  value={cfg.Protocol} onChange={(e, v) => this.odlConfigValueChangeHandlerCreator(ind, "Protocol", e => v)} inputProps={{ name: `protocol-${ind}`, id: `protocol-${ind}` }} fullWidth >
                          <MenuItem value={"http"}>http</MenuItem>
                          <MenuItem value={"https"}>https</MenuItem>
                        </Select>
                      </FormControl>
                      <TextField variant="standard" className={classes.left} spellCheck={false} margin="dense" id="hostname" label="Hostname" type="text" value={cfg.Server} onChange={this.odlConfigValueChangeHandlerCreator(ind, "Server", e => e.target.value)} />
                      <TextField variant="standard" className={classes.right} style={{ maxWidth: "65px" }} spellCheck={false} margin="dense" id="port" label="Port" type="number" value={cfg.Port || ""} onChange={this.odlConfigValueChangeHandlerCreator(ind, "Port", e => +e.target.value)} />
                    </div>
                    {
                      this.state.isOdlConfigHostnameEmpty &&
                      <Typography component={"div"} className={classes.left} color="error" gutterBottom>Please add a hostname.</Typography>
                    }
                    <div className={classes.alignInOneLine}>
                      <TextField variant="standard" className={classes.left} spellCheck={false} margin="dense" id="username" label="Username" type="text" value={cfg.User} onChange={this.odlConfigValueChangeHandlerCreator(ind, "User", e => e.target.value)} />
                      <TextField variant="standard" className={classes.right} spellCheck={false} margin="dense" id="password" label="Password" type="password" value={cfg.Password} onChange={this.odlConfigValueChangeHandlerCreator(ind, "Password", e => e.target.value)} />
                    </div>
                    <div className={classes.alignInOneLine}>
                      <FormControlLabel className={classes.right} control={<Checkbox checked={cfg.Trustall} onChange={this.odlConfigValueChangeHandlerCreator(ind, "Trustall", e => e.target.checked)} />} label="Trustall" />
                    </div>
                  </Panel>
                );
              })
              : (this.state.forceAddOdlConfig ?
                <div className={classes.center} >
                  <Typography component={"div"} className={classes.title} color="error" gutterBottom>Please add at least one ODL auto connect configuration.</Typography>
                </div>
                :
                <div className={classes.center} >
                  <Typography component={"div"} className={classes.title} color="textSecondary" gutterBottom>Please add an ODL auto connect configuration.</Typography>
                </div>
              )
            }
            <Fab className={classes.fab} color="primary" aria-label="Add" onClick={() => this.setState({
              ODLConfig: [...this.state.ODLConfig, { Server: '', Port: 8181, Protocol: 'https', User: 'admin', Password: 'admin', Trustall: false }]
            })} > <AddIcon /> </Fab>

          </TabContainer> : null}

        </DialogContent>
        <DialogActions>
          <Button color="inherit" onClick={(event) => { this.addConfig(event) }} > {setting.applyButtonText} </Button>
          <Button onClick={(event) => {
            this.onCancel();
            event.preventDefault();
            event.stopPropagation();
            this.resetPanel();
          }} color="secondary"> {setting.cancelButtonText} </Button>
        </DialogActions>
      </Dialog>
    );
  }

  private addConfig = (event: any) => {
    event.preventDefault();
    event.stopPropagation();

    if (this.state.ODLConfig.length === 0) {
      this.setState({ activeTab: 1, forceAddOdlConfig: true });
    }
    else
      if (this.state.ODLConfig.length > 0) {
        for (let i = 0; i <= this.state.ODLConfig.length; i++) {
          if (this.isHostnameEmpty(i)) {
            this.setState({ activeOdlConfig: 'panel-' + i })
            this.setState({ isOdlConfigHostnameEmpty: true })
            return;
          }
        }

        this.onApply(Object.keys(this.state).reduce<MediatorConfig & { [kex: string]: any }>((acc, key) => {
          // do not copy additional state properties
          if (key !== "activeTab" && key !== "activeOdlConfig" && key !== "isOdlConfigHostnameEmpty"
            && key !== "forceAddOdlConfig" && key !== "_initialMediatorConfig") acc[key] = (this.state as any)[key];
          return acc;
        }, {} as MediatorConfig));
        this.resetPanel();
      }
  }

  private resetPanel = () => {
    this.setState({ forceAddOdlConfig: false, isOdlConfigHostnameEmpty: false, activeTab: 0 });
  }


  private hideHostnameErrormessage = (panelId: string | null) => {

    if (panelId) {
      let id = Number(panelId.split('-')[1]);
      if (!this.isHostnameEmpty(id)) {
        this.setState({ isOdlConfigHostnameEmpty: false })
      }
    }
  }

  private isHostnameEmpty = (id: number) => {

    const element = this.state.ODLConfig[id];
    if (element) {
      if (!element.Server) {
        return true;
      }
      else {
        return false
      }

    } else {
      return null;
    }
  }

  private onApply = (config: MediatorConfig) => {
    this.props.onClose && this.props.onClose();
    switch (this.props.mode) {
      case EditMediatorConfigDialogMode.AddMediatorConfig:
        config && this.props.addMediatorConfig(config);
        break;
      case EditMediatorConfigDialogMode.EditMediatorConfig:
        config && this.props.updateMediatorConfig(config);
        break;
      case EditMediatorConfigDialogMode.RemoveMediatorConfig:
        config && this.props.removeMediatorConfig(config);
        break;
    }
  };

  private onCancel = () => {
    this.props.onClose && this.props.onClose();
  }

  static getDerivedStateFromProps(props: EditMediatorConfigDialogComponentProps, state: EditMediatorConfigDialogComponentState & { _initialMediatorConfig: MediatorConfig }): EditMediatorConfigDialogComponentState & { _initialMediatorConfig: MediatorConfig } {
    if (props.mediatorConfig !== state._initialMediatorConfig) {
      state = {
        ...state,
        ...props.mediatorConfig,
        _initialMediatorConfig: props.mediatorConfig,
      };
    }
    return state;
  }
}

export const EditMediatorConfigDialog = withStyles(styles)(connect(mapProps, mapDispatch)(EditMediatorConfigDialogComponent));
export default EditMediatorConfigDialog;