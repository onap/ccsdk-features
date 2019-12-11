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

import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import { FormControl, InputLabel, Select, MenuItem } from '@material-ui/core';

import { IDispatcher, connect, Connect } from '../../../../framework/src/flux/connect';

import {
  editNetworkElementAsyncActionCreator,
  addNewNetworkElementAsyncActionCreator,
  removeNetworkElementAsyncActionCreator
} from '../actions/networkElementsActions';

import { unmountNetworkElementAsyncActionCreator, mountNetworkElementAsyncActionCreator } from '../actions/mountedNetworkElementsActions';
import { NetworkElementConnection, UpdateNetworkElement } from '../models/networkElementConnection';

export enum EditNetworkElementDialogMode {
  None = "none",
  EditNetworkElement = "editNetworkElement",
  RemoveNetworkElement = "removeNetworkElement",
  AddNewNetworkElement = "addNewNetworkElement",
  MountNetworkElement = "mountNetworkElement",
  UnmountNetworkElement = "unmountNetworkElement",
}

const mapDispatch = (dispatcher: IDispatcher) => ({
  addNewNetworkElement: async (element: NetworkElementConnection) => {
    await dispatcher.dispatch(addNewNetworkElementAsyncActionCreator(element));
    await dispatcher.dispatch(mountNetworkElementAsyncActionCreator(element));
  },
  mountNetworkElement: (element: NetworkElementConnection) => dispatcher.dispatch(mountNetworkElementAsyncActionCreator(element)),
  unmountNetworkElement: (element: NetworkElementConnection) => {
    dispatcher.dispatch(unmountNetworkElementAsyncActionCreator(element && element.nodeId));
  },
  editNetworkElement: async (element: UpdateNetworkElement, mountElement: NetworkElementConnection) => {
    await dispatcher.dispatch(editNetworkElementAsyncActionCreator(element));
    await dispatcher.dispatch(mountNetworkElementAsyncActionCreator(mountElement));
  },
  removeNetworkElement: (element: UpdateNetworkElement) => dispatcher.dispatch(removeNetworkElementAsyncActionCreator(element))
});

type DialogSettings = {
  dialogTitle: string,
  dialogDescription: string,
  applyButtonText: string,
  cancelButtonText: string,
  enableMountIdEditor: boolean,
  enableUsernameEditor: boolean,
  enableExtendedEditor: boolean,
}

const settings: { [key: string]: DialogSettings } = {
  [EditNetworkElementDialogMode.None]: {
    dialogTitle: "",
    dialogDescription: "",
    applyButtonText: "",
    cancelButtonText: "",
    enableMountIdEditor: false,
    enableUsernameEditor: false,
    enableExtendedEditor: false,
  },

  [EditNetworkElementDialogMode.AddNewNetworkElement]: {
    dialogTitle: "Add new network element",
    dialogDescription: "Add this new network element:",
    applyButtonText: "Add network element",
    cancelButtonText: "Cancel",
    enableMountIdEditor: true,
    enableUsernameEditor: true,
    enableExtendedEditor: true,
  },
  [EditNetworkElementDialogMode.MountNetworkElement]: {
    dialogTitle: "Mount network element",
    dialogDescription: "mount this network element:",
    applyButtonText: "mount network element",
    cancelButtonText: "Cancel",
    enableMountIdEditor: false,
    enableUsernameEditor: false,
    enableExtendedEditor: false,
  },
  [EditNetworkElementDialogMode.UnmountNetworkElement]: {
    dialogTitle: "Unmount network element",
    dialogDescription: "unmount this network element:",
    applyButtonText: "Unmount network element",
    cancelButtonText: "Cancel",
    enableMountIdEditor: false,
    enableUsernameEditor: false,
    enableExtendedEditor: false,
  },
  [EditNetworkElementDialogMode.EditNetworkElement]: {
    dialogTitle: "Modify the network elements",
    dialogDescription: "Modify this network element",
    applyButtonText: "Modify",
    cancelButtonText: "Cancel",
    enableMountIdEditor: false,
    enableUsernameEditor: true,
    enableExtendedEditor: false,
  },
  [EditNetworkElementDialogMode.RemoveNetworkElement]: {
    dialogTitle: "Remove network element",
    dialogDescription: "Do you really want to remove this network element:",
    applyButtonText: "Remove network element",
    cancelButtonText: "Cancel",
    enableMountIdEditor: false,
    enableUsernameEditor: false,
    enableExtendedEditor: false,
  }
}

type EditNetworkElementDialogComponentProps = Connect<undefined, typeof mapDispatch> & {
  mode: EditNetworkElementDialogMode;
  initialNetworkElement: NetworkElementConnection;
  onClose: () => void;
};

type EditNetworkElementDialogComponentState = NetworkElementConnection;

class EditNetworkElementDialogComponent extends React.Component<EditNetworkElementDialogComponentProps, EditNetworkElementDialogComponentState> {
  constructor(props: EditNetworkElementDialogComponentProps) {
    super(props);

    this.state = {
      nodeId: this.props.initialNetworkElement.nodeId,
      isRequired: false,
      host: this.props.initialNetworkElement.host,
      port: this.props.initialNetworkElement.port,
    };
  }

  render(): JSX.Element {
    const setting = settings[this.props.mode];
    return (
      <Dialog open={this.props.mode !== EditNetworkElementDialogMode.None}>
        <DialogTitle id="form-dialog-title">{setting.dialogTitle}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {setting.dialogDescription}
          </DialogContentText>
          <TextField disabled={!setting.enableMountIdEditor} spellCheck={false} autoFocus margin="dense" id="name" label="Name" aria-label="name" type="text" fullWidth value={this.state.nodeId} onChange={(event) => { this.setState({ nodeId: event.target.value }); }} />
          <TextField disabled={!setting.enableMountIdEditor} spellCheck={false} margin="dense" id="ipaddress" label="IP address" aria-label="ip adress" type="text" fullWidth value={this.state.host} onChange={(event) => { this.setState({ host: event.target.value }); }} />
          <TextField disabled={!setting.enableMountIdEditor} spellCheck={false} margin="dense" id="netconfport" label="NetConf port" aria-label="netconf port" type="number" fullWidth value={this.state.port.toString()} onChange={(event) => { this.setState({ port: +event.target.value }); }} />
          {setting.enableUsernameEditor && <TextField disabled={!setting.enableUsernameEditor} spellCheck={false} margin="dense" id="username" label="Username" aria-label="username" type="text" fullWidth value={this.state.username} onChange={(event) => { this.setState({ username: event.target.value }); }} /> || null}
          {setting.enableUsernameEditor && <TextField disabled={!setting.enableUsernameEditor} spellCheck={false} margin="dense" id="password" label="Password" aria-label="password" type="password" fullWidth value={this.state.password} onChange={(event) => { this.setState({ password: event.target.value }); }} /> || null}
          <FormControl fullWidth disabled={!setting.enableUsernameEditor}>
            <InputLabel htmlFor="active">Required</InputLabel>
            <Select value={this.state.isRequired || false} onChange={(event) => {
              this.setState({ isRequired: event.target.value as any as boolean });
            }} inputProps={{ name: 'required', id: 'required' }} fullWidth >
              <MenuItem value={true as any as string} >True</MenuItem>
              <MenuItem value={false as any as string} >False</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={(event) => {
            this.onApply({
              isRequired: this.state.isRequired,
              id: this.state.nodeId,
              nodeId: this.state.nodeId,
              host: this.state.host,
              port: this.state.port,
              username: this.state.username,
              password: this.state.password,
            });
            event.preventDefault();
            event.stopPropagation();
          }} > {setting.applyButtonText} </Button>
          <Button onClick={(event) => {
            this.onCancel();
            event.preventDefault();
            event.stopPropagation();
          }} color="secondary"> {setting.cancelButtonText} </Button>
        </DialogActions>
      </Dialog>
    )
  }

  private onApply = (element: NetworkElementConnection) => {
    this.props.onClose && this.props.onClose();
    let updateElement: UpdateNetworkElement = {
      id: this.state.nodeId
    }
    switch (this.props.mode) {
      case EditNetworkElementDialogMode.AddNewNetworkElement:
        element && this.props.addNewNetworkElement(element);
        break;
      case EditNetworkElementDialogMode.MountNetworkElement:
        element && this.props.mountNetworkElement(element);
        break;
      case EditNetworkElementDialogMode.UnmountNetworkElement:
        element && this.props.unmountNetworkElement(element);
        break;
      case EditNetworkElementDialogMode.EditNetworkElement:
        if (this.props.initialNetworkElement.isRequired !== this.state.isRequired)
          updateElement.isRequired = this.state.isRequired;
        if (this.props.initialNetworkElement.username !== this.state.username)
          updateElement.username = this.state.username;
        if (this.props.initialNetworkElement.password !== this.state.password)
          updateElement.password = this.state.password;
        element && this.props.editNetworkElement(updateElement, element);
        break;
      case EditNetworkElementDialogMode.RemoveNetworkElement:
        element && this.props.removeNetworkElement(updateElement);
        break;
    }
  };

  private onCancel = () => {
    this.props.onClose && this.props.onClose();
  }

  static getDerivedStateFromProps(props: EditNetworkElementDialogComponentProps, state: EditNetworkElementDialogComponentState & { _initialNetworkElement: NetworkElementConnection }): EditNetworkElementDialogComponentState & { _initialNetworkElement: NetworkElementConnection } {
    if (props.initialNetworkElement !== state._initialNetworkElement) {
      state = {
        ...state,
        ...props.initialNetworkElement,
        _initialNetworkElement: props.initialNetworkElement,
      };
    }
    return state;
  }
}

export const EditNetworkElementDialog = connect(undefined, mapDispatch)(EditNetworkElementDialogComponent);
export default EditNetworkElementDialog;