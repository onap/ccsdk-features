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

import { IDispatcher, connect, Connect } from '../../../../framework/src/flux/connect';

import {
  addOrUpdateMaintenenceEntryAsyncActionCreator,
  removeFromMaintenenceEntrysAsyncActionCreator
} from '../actions/maintenenceActions';

import { MaintenenceEntry } from '../models/maintenenceEntryType';
import { FormControl, InputLabel, Select, MenuItem, Typography } from '@material-ui/core';

export enum EditMaintenenceEntryDialogMode {
  None = "none",
  AddMaintenenceEntry = "addMaintenenceEntry",
  EditMaintenenceEntry = "editMaintenenceEntry",
  RemoveMaintenenceEntry = "removeMaintenenceEntry"
}

const mapDispatch = (dispatcher: IDispatcher) => ({
  addOrUpdateMaintenenceEntry: (entry: MaintenenceEntry) => {
    dispatcher.dispatch(addOrUpdateMaintenenceEntryAsyncActionCreator(entry));
  },
  removeMaintenenceEntry: (entry: MaintenenceEntry) => {
    dispatcher.dispatch(removeFromMaintenenceEntrysAsyncActionCreator(entry));
  },
});

type DialogSettings = {
  dialogTitle: string,
  dialogDescription: string,
  applyButtonText: string,
  cancelButtonText: string,
  enableMountIdEditor: boolean,
  enableTimeEditor: boolean,
}

const settings: { [key: string]: DialogSettings } = {
  [EditMaintenenceEntryDialogMode.None]: {
    dialogTitle: "",
    dialogDescription: "",
    applyButtonText: "",
    cancelButtonText: "",
    enableMountIdEditor: false,
    enableTimeEditor: false,
  },
  [EditMaintenenceEntryDialogMode.AddMaintenenceEntry]: {
    dialogTitle: "Add new maintenence entry",
    dialogDescription: "",
    applyButtonText: "Add",
    cancelButtonText: "Cancel",
    enableMountIdEditor: true,
    enableTimeEditor: true,
  },
  [EditMaintenenceEntryDialogMode.EditMaintenenceEntry]: {
    dialogTitle: "Edit maintenence entry",
    dialogDescription: "",
    applyButtonText: "Save",
    cancelButtonText: "Cancel",
    enableMountIdEditor: false,
    enableTimeEditor: true,
  },
  [EditMaintenenceEntryDialogMode.RemoveMaintenenceEntry]: {
    dialogTitle: "Remove maintenence entry",
    dialogDescription: "",
    applyButtonText: "Remove",
    cancelButtonText: "Cancel",
    enableMountIdEditor: false,
    enableTimeEditor: false,
  },
}

type EditMaintenenceEntryDIalogComponentProps = Connect<undefined, typeof mapDispatch> & {
  mode: EditMaintenenceEntryDialogMode;
  initialMaintenenceEntry: MaintenenceEntry;
  onClose: () => void;
};

type EditMaintenenceEntryDIalogComponentState = MaintenenceEntry & { isErrorVisible: boolean };

class EditMaintenenceEntryDIalogComponent extends React.Component<EditMaintenenceEntryDIalogComponentProps, EditMaintenenceEntryDIalogComponentState> {
  constructor(props: EditMaintenenceEntryDIalogComponentProps) {
    super(props);

    this.state = {
      ...this.props.initialMaintenenceEntry,
      isErrorVisible: false
    };
  }

  render(): JSX.Element {
    const setting = settings[this.props.mode];
    return (
      <Dialog open={this.props.mode !== EditMaintenenceEntryDialogMode.None}>
        <DialogTitle id="form-dialog-title">{setting.dialogTitle}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {setting.dialogDescription}
          </DialogContentText>
          <TextField disabled={!setting.enableMountIdEditor} spellCheck={false} autoFocus margin="dense" id="name" label="Name" type="text" fullWidth value={this.state.nodeId} onChange={(event) => { this.setState({ nodeId: event.target.value }); }} />
          {this.state.isErrorVisible && <Typography variant="body1" color="error" >Name must not be empty.</Typography>}
          <TextField disabled={!setting.enableTimeEditor} spellCheck={false} autoFocus margin="dense" id="start" label="Start (Local DateTime)" type="datetime-local" fullWidth value={this.state.start} onChange={(event) => { this.setState({ start: event.target.value }); }} />
          <TextField disabled={!setting.enableTimeEditor} spellCheck={false} autoFocus margin="dense" id="end" label="End (Local DateTime)" type="datetime-local" fullWidth value={this.state.end} onChange={(event) => { this.setState({ end: event.target.value }); }} />
          <FormControl fullWidth disabled={!setting.enableTimeEditor}>
            <InputLabel htmlFor="active">Active</InputLabel>
            <Select value={this.state.active || false} onChange={(event) => {
              this.setState({ active: event.target.value as any as boolean });
            }} inputProps={{ name: 'active', id: 'active' }} fullWidth >
              <MenuItem value={true as any as string}>active</MenuItem>
              <MenuItem value={false as any as string}>not active</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={(event) => {

            if (this.props.mode === EditMaintenenceEntryDialogMode.AddMaintenenceEntry && this.state.nodeId.trim().length === 0) {
              this.setState({ isErrorVisible: true });
            } else {
              this.onApply({
                _id: this.state._id || this.state.nodeId,
                nodeId: this.state.nodeId,
                description: this.state.description,
                start: this.state.start,
                end: this.state.end,
                active: this.state.active
              });
              this.setState({ isErrorVisible: false });
            }

            event.preventDefault();
            event.stopPropagation();
          }} > {setting.applyButtonText} </Button>
          <Button onClick={(event) => {
            this.onCancel();
            event.preventDefault();
            event.stopPropagation();
            this.setState({ isErrorVisible: false });
          }} color="secondary"> {setting.cancelButtonText} </Button>
        </DialogActions>
      </Dialog>
    )
  }

  private onApply = (entry: MaintenenceEntry) => {
    this.props.onClose && this.props.onClose();
    switch (this.props.mode) {
      case EditMaintenenceEntryDialogMode.AddMaintenenceEntry:
        entry && this.props.addOrUpdateMaintenenceEntry(entry);
        break;
      case EditMaintenenceEntryDialogMode.EditMaintenenceEntry:
        entry && this.props.addOrUpdateMaintenenceEntry(entry);
        break;
      case EditMaintenenceEntryDialogMode.RemoveMaintenenceEntry:
        entry && this.props.removeMaintenenceEntry(entry);
        break;
    }
  };


  private onCancel = () => {
    this.props.onClose && this.props.onClose();
  }

  static getDerivedStateFromProps(props: EditMaintenenceEntryDIalogComponentProps, state: EditMaintenenceEntryDIalogComponentState & { _initialMaintenenceEntry: MaintenenceEntry }): EditMaintenenceEntryDIalogComponentState & { _initialMaintenenceEntry: MaintenenceEntry } {
    if (props.initialMaintenenceEntry !== state._initialMaintenenceEntry) {
      state = {
        ...state,
        ...props.initialMaintenenceEntry,
        _initialMaintenenceEntry: props.initialMaintenenceEntry,
      };
    }
    return state;
  }

}

export const EditMaintenenceEntryDIalog = connect(undefined, mapDispatch)(EditMaintenenceEntryDIalogComponent);
export default EditMaintenenceEntryDIalog;