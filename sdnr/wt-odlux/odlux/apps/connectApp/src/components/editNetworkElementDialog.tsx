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

import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';
import FormControl from '@mui/material/FormControl';
import FormControlLabel from '@mui/material/FormControlLabel';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import Select from '@mui/material/Select';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';

import { connect, Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { removeWebUriAction } from '../actions/commonNetworkElementsActions';
import { mountNetworkElementAsyncActionCreator, unmountNetworkElementAsyncActionCreator } from '../actions/mountedNetworkElementsActions';
import {
  addNewNetworkElementAsyncActionCreator, editNetworkElementAsyncActionCreator, removeNetworkElementAsyncActionCreator,
} from '../actions/networkElementsActions';
import { loadAllTlsKeyListAsync } from '../actions/tlsKeyActions';
import { NetworkElementConnection, propertyOf, UpdateNetworkElement } from '../models/networkElementConnection';

export enum EditNetworkElementDialogMode {
  None = 'none',
  EditNetworkElement = 'editNetworkElement',
  RemoveNetworkElement = 'removeNetworkElement',
  AddNewNetworkElement = 'addNewNetworkElement',
  MountNetworkElement = 'mountNetworkElement',
  UnmountNetworkElement = 'unmountNetworkElement',
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

    const values = Object.keys(element);
    console.log('edit element');
    console.log(values);

    //make sure properties are there in case they get renamed
    const idProperty = propertyOf<UpdateNetworkElement>('id');
    const isRequiredProperty = propertyOf<UpdateNetworkElement>('isRequired');


    if (values.length === 2 && values.includes(idProperty as string) && values.includes(isRequiredProperty as string)) {
      // do not mount network element/node, if only isRequired is changed
      await dispatcher.dispatch(editNetworkElementAsyncActionCreator(element));

    } else if (!(values.length === 1 && values.includes(idProperty as string))) { //do not edit or mount network element/node , if only id was saved into object (no changes made!)
      await dispatcher.dispatch(editNetworkElementAsyncActionCreator(element));
      await dispatcher.dispatch(mountNetworkElementAsyncActionCreator(mountElement));
    }
  },
  removeNetworkElement: async (element: UpdateNetworkElement) => {
    await dispatcher.dispatch(removeNetworkElementAsyncActionCreator(element));
    dispatcher.dispatch(removeWebUriAction(element.id));
  },
  getAvailableTlsKeys: async () => dispatcher.dispatch(loadAllTlsKeyListAsync()),
});

type DialogSettings = {
  dialogTitle: string;
  dialogDescription: string;
  applyButtonText: string;
  cancelButtonText: string;
  enableMountIdEditor: boolean;
  enableUsernameEditor: boolean;
  enableExtendedEditor: boolean;
};

const settings: { [key: string]: DialogSettings } = {
  [EditNetworkElementDialogMode.None]: {
    dialogTitle: '',
    dialogDescription: '',
    applyButtonText: '',
    cancelButtonText: '',
    enableMountIdEditor: false,
    enableUsernameEditor: false,
    enableExtendedEditor: false,
  },

  [EditNetworkElementDialogMode.AddNewNetworkElement]: {
    dialogTitle: 'Add New Node',
    dialogDescription: 'Add this new node:',
    applyButtonText: 'Add node',
    cancelButtonText: 'Cancel',
    enableMountIdEditor: true,
    enableUsernameEditor: true,
    enableExtendedEditor: true,
  },
  [EditNetworkElementDialogMode.MountNetworkElement]: {
    dialogTitle: 'Mount Node',
    dialogDescription: 'Mount this node:',
    applyButtonText: 'Mount node',
    cancelButtonText: 'Cancel',
    enableMountIdEditor: false,
    enableUsernameEditor: false,
    enableExtendedEditor: false,
  },
  [EditNetworkElementDialogMode.UnmountNetworkElement]: {
    dialogTitle: 'Unmount Node',
    dialogDescription: 'Unmount this node:',
    applyButtonText: 'Unmount node',
    cancelButtonText: 'Cancel',
    enableMountIdEditor: false,
    enableUsernameEditor: false,
    enableExtendedEditor: false,
  },
  [EditNetworkElementDialogMode.EditNetworkElement]: {
    dialogTitle: 'Modify Node',
    dialogDescription: 'Modify this node',
    applyButtonText: 'Modify',
    cancelButtonText: 'Cancel',
    enableMountIdEditor: false,
    enableUsernameEditor: true,
    enableExtendedEditor: false,
  },
  [EditNetworkElementDialogMode.RemoveNetworkElement]: {
    dialogTitle: 'Remove Node',
    dialogDescription: 'Do you really want to remove this node?',
    applyButtonText: 'Remove node',
    cancelButtonText: 'Cancel',
    enableMountIdEditor: false,
    enableUsernameEditor: false,
    enableExtendedEditor: false,
  },
};

type EditNetworkElementDialogComponentProps = Connect<undefined, typeof mapDispatch> & {
  mode: EditNetworkElementDialogMode;
  initialNetworkElement: NetworkElementConnection;
  onClose: () => void;
  radioChecked: string;
};

type EditNetworkElementDialogComponentState = NetworkElementConnection & {
  isNameValid: boolean;
  isHostSet: boolean;
  isPasswordSelected: boolean;
  isTlsSelected: boolean;
  radioSelected: string;
  showPasswordTextField: boolean;
  showTlsDropdown: boolean;
};

class EditNetworkElementDialogComponent extends React.Component<EditNetworkElementDialogComponentProps, EditNetworkElementDialogComponentState> {
  constructor(props: EditNetworkElementDialogComponentProps) {
    super(props);
    this.handleRadioChange = this.handleRadioChange.bind(this);
    // Initialization of state is partly overwritten by update via react getDerivedStateFromProps() below.
    // Change initialization values in parent "networkElements.tsx" in "const emptyRequireNetworkElement"
    this.state = {
      nodeId: this.props.initialNetworkElement.nodeId,
      isRequired: this.props.initialNetworkElement.isRequired,
      host: this.props.initialNetworkElement.host,
      port: this.props.initialNetworkElement.port,
      isNameValid: true,
      isHostSet: true,
      isPasswordSelected: true,
      isTlsSelected: false,
      radioSelected: '',
      showPasswordTextField: true,
      showTlsDropdown: false,
    };
  }

  public handleRadioChange = (event: any) => {
    this.setState({
      radioSelected: event.target.value,
      showPasswordTextField: event.target.value === 'password',
      showTlsDropdown: event.target.value === 'tlsKey',
    });
  };

  render(): JSX.Element {
    const setting = settings[this.props.mode];
    let { showPasswordTextField, showTlsDropdown, radioSelected } = this.state;
    radioSelected = this.state.radioSelected.length > 0 ? this.state.radioSelected : this.props.radioChecked;

    if (radioSelected === 'password') {
      radioSelected = 'password';
      showPasswordTextField = true;
      showTlsDropdown = false;
    } else if (radioSelected === 'tlsKey') {
      radioSelected = 'tlsKey';
      showPasswordTextField = false;
      showTlsDropdown = true;
    }

    let tlsKeysList = this.props.state.connect.availableTlsKeys ? this.props.state.connect.availableTlsKeys.tlsKeysList ? this.props.state.connect.availableTlsKeys.tlsKeysList : [] : [];

    return (
      <Dialog open={this.props.mode !== EditNetworkElementDialogMode.None}>
        <DialogTitle id="form-dialog-title" aria-label={`${setting.dialogTitle.replace(/ /g, '-').toLowerCase()}-dialog`}>{setting.dialogTitle}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {setting.dialogDescription}
          </DialogContentText>
          <TextField variant="standard" disabled={!setting.enableMountIdEditor} spellCheck={false} autoFocus margin="dense"
            id="name" label="Node ID" aria-label="name" type="text" fullWidth value={this.state.nodeId} onChange={(event) => { this.setState({ nodeId: event.target.value }); }} />
          {!this.state.isNameValid && <Typography variant="body1" color="error">Node ID cannot be empty.</Typography>}
          <TextField variant="standard" disabled={!setting.enableMountIdEditor} spellCheck={false} margin="dense"
            id="ipaddress" label="Host/IP address" aria-label="ip adress" type="text" fullWidth value={this.state.host} onChange={(event) => { this.setState({ host: event.target.value }); }} />
          {!this.state.isHostSet && <Typography variant="body1" color="error">Host/IP address cannot be empty.</Typography>}

          <TextField variant="standard" disabled={!setting.enableMountIdEditor} spellCheck={false} margin="dense"
            id="netconfport" label="NETCONF port" aria-label="netconf port" type="number" fullWidth value={this.state.port.toString()}
            onChange={(event) => { this.setState({ port: +event.target.value }); }} />
          {setting.enableUsernameEditor && <TextField variant="standard" disabled={!setting.enableUsernameEditor} spellCheck={false} margin="dense"
            id="username" label="Username" aria-label="username" type="text" fullWidth value={this.state.username} onChange={(event) => { this.setState({ username: event.target.value }); }} /> || null}

          {setting.enableUsernameEditor &&
            <RadioGroup row aria-label="password-tls-key" name="password-tls-key" value={radioSelected}
              onChange={this.handleRadioChange} >
              <FormControlLabel aria-label="passwordSelection" value='password' control={<Radio color="secondary" />} label="Password" onChange={this.onRadioSelect} />
              <FormControlLabel aria-label="tlsKeySelection" value='tlsKey' control={<Radio color="secondary" />} label="TlsKey" onChange={this.onRadioSelect} />
            </RadioGroup> || null}

          {setting.enableUsernameEditor && showPasswordTextField &&
            <TextField variant="standard" disabled={!setting.enableUsernameEditor || !showPasswordTextField} spellCheck={false} margin="dense"
              id="password" aria-label="password" type="password" fullWidth value={this.state.password}
              onChange={(event) => { this.setState({ password: event.target.value }); }}
            /> || null}

          <FormControl variant="standard" fullWidth disabled={!setting.enableUsernameEditor}>
            {setting.enableUsernameEditor && showTlsDropdown &&
              <div>
                <InputLabel htmlFor="pass">--Select tls-key--</InputLabel>
                <Select variant="standard" disabled={!setting.enableUsernameEditor || !showTlsDropdown}
                  id="tlsKey" aria-label="tlsKey" value={this.state.tlsKey} fullWidth // displayEmpty
                  onChange={(event) => { this.setState({ tlsKey: event.target.value as any }); }}
                  inputProps={{ name: 'tlsKey', id: 'tlsKey' }}  >
                  <MenuItem value={''} disabled >--Select tls-key--</MenuItem>
                  {tlsKeysList.map(tlsKey =>
                    (<MenuItem value={tlsKey.key} key={tlsKey.key} aria-label={tlsKey.key} >{tlsKey.key}</MenuItem>))}
                </Select>
              </div>
            }
          </FormControl>

          <FormControl variant="standard" fullWidth disabled={!setting.enableUsernameEditor}>
            <InputLabel htmlFor="active">Required</InputLabel>
            <Select variant="standard" aria-label="required-selection" value={this.state.isRequired || false} onChange={(event) => {
              this.setState({ isRequired: event.target.value as any as boolean });
            }} inputProps={{ name: 'required', id: 'required' }} fullWidth >
              <MenuItem value={true as any as string} aria-label="true">True</MenuItem>
              <MenuItem value={false as any as string} aria-label="false">False</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button aria-label="dialog-confirm-button" onClick={(event) => {

            if (this.areRequieredFieldsValid()) {
              this.onApply({
                isRequired: this.state.isRequired,
                id: this.state.nodeId,
                nodeId: this.state.nodeId,
                host: this.state.host,
                port: this.state.port,
                username: this.state.username,
                password: this.state.password,
                tlsKey: this.state.tlsKey,
              });
            }
            event.preventDefault();
            event.stopPropagation();
          }} color="inherit" > {setting.applyButtonText} </Button>
          <Button aria-label="dialog-cancel-button" onClick={(event) => {
            this.onCancel();
            event.preventDefault();
            event.stopPropagation();
          }} color="secondary"> {setting.cancelButtonText} </Button>
        </DialogActions>
      </Dialog>
    );
  }

  public renderTlsKeys = () => {
    try {
      this.props.getAvailableTlsKeys();
    } catch (err) {
      console.log(err);
    }
  };

  public componentDidMount() {
    this.renderTlsKeys();
  }

  public onRadioSelect = (e: any) => {
    if (e.target.value == 'password') {
      this.setState({ isPasswordSelected: true, isTlsSelected: false });
    } else if (e.target.value == 'tlsKey') {
      this.setState({ isPasswordSelected: false, isTlsSelected: true });
    }
  };

  private onApply = (element: NetworkElementConnection) => {
    if (this.props.onClose) this.props.onClose();
    let updateElement: UpdateNetworkElement = {
      id: this.state.nodeId,
    };
    if (this.state.isPasswordSelected) {
      element.tlsKey = '';
    } else if (this.state.isTlsSelected) { //check here
      element.password = '';
    }

    switch (this.props.mode) {
      case EditNetworkElementDialogMode.AddNewNetworkElement:
        if (element) this.props.addNewNetworkElement(element);
        this.setState({
          radioSelected: '',
          isPasswordSelected: true,
        });
        break;
      case EditNetworkElementDialogMode.MountNetworkElement:
        if (element) this.props.mountNetworkElement(element);
        break;
      case EditNetworkElementDialogMode.UnmountNetworkElement:
        if (element) this.props.unmountNetworkElement(element);
        break;
      case EditNetworkElementDialogMode.EditNetworkElement:
        if (this.props.initialNetworkElement.isRequired !== this.state.isRequired)
          updateElement.isRequired = this.state.isRequired;
        if (this.props.initialNetworkElement.username !== this.state.username)
          updateElement.username = this.state.username;
        if (this.props.initialNetworkElement.password !== this.state.password && this.state.isPasswordSelected) {
          updateElement.password = this.state.password;
          updateElement.tlsKey = '';
        }
        if (this.props.initialNetworkElement.tlsKey !== this.state.tlsKey && this.state.isTlsSelected) {
          updateElement.tlsKey = this.state.tlsKey;
          updateElement.password = '';
        }
        if (element) this.props.editNetworkElement(updateElement, element);
        this.setState({
          radioSelected: '',
        });
        break;
      case EditNetworkElementDialogMode.RemoveNetworkElement:
        if (element) this.props.removeNetworkElement(updateElement);
        break;
    }

    this.setState({ password: '', username: '', tlsKey: '' });
    this.resetRequieredFields();
  };

  private onCancel = () => {
    if (this.props.onClose) this.props.onClose();
    this.setState({ password: '', username: '', tlsKey: '', radioSelected: '' });
    this.resetRequieredFields();
  };

  private resetRequieredFields() {
    this.setState({ isNameValid: true, isHostSet: true });
  }

  private areRequieredFieldsValid() {
    let areFieldsValid = true;

    if (this.state.nodeId == undefined || this.state.nodeId.trim().length === 0) {
      this.setState({ isNameValid: false });
      areFieldsValid = false;
    } else {
      this.setState({ isNameValid: true });
    }

    if (this.state.host == undefined || this.state.host.trim().length === 0) {
      this.setState({ isHostSet: false });
      areFieldsValid = false;
    } else {
      this.setState({ isHostSet: true });
    }

    return areFieldsValid;
  }

  static getDerivedStateFromProps(props: EditNetworkElementDialogComponentProps, state: EditNetworkElementDialogComponentState & { initialNetworkElement: NetworkElementConnection }): EditNetworkElementDialogComponentState & { initialNetworkElement: NetworkElementConnection } {
    let returnState = state;
    if (props.initialNetworkElement !== state.initialNetworkElement) {
      returnState = {
        ...state,
        ...props.initialNetworkElement,
        initialNetworkElement: props.initialNetworkElement,
      };
    }
    return returnState;
  }
}

export const EditNetworkElementDialog = connect(undefined, mapDispatch)(EditNetworkElementDialogComponent);
export default EditNetworkElementDialog;