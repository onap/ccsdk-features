/* eslint-disable @typescript-eslint/no-unused-expressions */
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

import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';
import TextField from '@mui/material/TextField';

import { connect, Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { FormControl, InputLabel, MenuItem, Select, Typography } from '@mui/material';
import {
  addOrUpdateMaintenenceEntryAsyncActionCreator,
  removeFromMaintenenceEntrysAsyncActionCreator,
} from '../actions/maintenenceActions';
import { MaintenanceEntry } from '../models/maintenanceEntryType';

export enum EditMaintenenceEntryDialogMode {
  None = 'none',
  AddMaintenenceEntry = 'addMaintenenceEntry',
  EditMaintenenceEntry = 'editMaintenenceEntry',
  RemoveMaintenenceEntry = 'removeMaintenenceEntry',
}

const mapDispatch = (dispatcher: IDispatcher) => ({
  addOrUpdateMaintenenceEntry: (entry: MaintenanceEntry) => {
    dispatcher.dispatch(addOrUpdateMaintenenceEntryAsyncActionCreator(entry));
  },
  removeMaintenenceEntry: (entry: MaintenanceEntry) => {
    dispatcher.dispatch(removeFromMaintenenceEntrysAsyncActionCreator(entry));
  },
});

type DialogSettings = {
  dialogTitle: string;
  dialogDescription: string;
  applyButtonText: string;
  cancelButtonText: string;
  enableMountIdEditor: boolean;
  enableTimeEditor: boolean;
};

const settings: { [key: string]: DialogSettings } = {
  [EditMaintenenceEntryDialogMode.None]: {
    dialogTitle: '',
    dialogDescription: '',
    applyButtonText: '',
    cancelButtonText: '',
    enableMountIdEditor: false,
    enableTimeEditor: false,
  },
  [EditMaintenenceEntryDialogMode.AddMaintenenceEntry]: {
    dialogTitle: 'Add new maintenence entry',
    dialogDescription: '',
    applyButtonText: 'Add',
    cancelButtonText: 'Cancel',
    enableMountIdEditor: true,
    enableTimeEditor: true,
  },
  [EditMaintenenceEntryDialogMode.EditMaintenenceEntry]: {
    dialogTitle: 'Edit maintenence entry',
    dialogDescription: '',
    applyButtonText: 'Save',
    cancelButtonText: 'Cancel',
    enableMountIdEditor: false,
    enableTimeEditor: true,
  },
  [EditMaintenenceEntryDialogMode.RemoveMaintenenceEntry]: {
    dialogTitle: 'Remove maintenence entry',
    dialogDescription: '',
    applyButtonText: 'Remove',
    cancelButtonText: 'Cancel',
    enableMountIdEditor: false,
    enableTimeEditor: false,
  },
};

type EditMaintenenceEntryDIalogComponentProps = Connect<undefined, typeof mapDispatch> & {
  mode: EditMaintenenceEntryDialogMode;
  initialMaintenenceEntry: MaintenanceEntry;
  onClose: () => void;
};

type EditMaintenenceEntryDIalogComponentState = MaintenanceEntry & { isErrorVisible: boolean };

class EditMaintenenceEntryDIalogComponent extends React.Component<EditMaintenenceEntryDIalogComponentProps, EditMaintenenceEntryDIalogComponentState> {
  constructor(props: EditMaintenenceEntryDIalogComponentProps) {
    super(props);

    this.state = {
      ...this.props.initialMaintenenceEntry,
      isErrorVisible: false,
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
          <TextField variant="standard" disabled={!setting.enableMountIdEditor} spellCheck={false} autoFocus margin="dense" id="name" label="Name" type="text" fullWidth value={this.state.nodeId} onChange={(event) => { this.setState({ nodeId: event.target.value }); }} />
          {this.state.isErrorVisible && <Typography variant="body1" color="error" >Name must not be empty.</Typography>}
          <TextField variant="standard" disabled={!setting.enableTimeEditor} spellCheck={false} autoFocus margin="dense" id="start"
            label="Start (Local DateTime)" type="datetime-local" fullWidth value={this.state.start} onChange={(event) => { this.setState({ start: event.target.value }); }} />
          <TextField variant="standard" disabled={!setting.enableTimeEditor} spellCheck={false} autoFocus margin="dense" id="end"
            label="End (Local DateTime)" type="datetime-local" fullWidth value={this.state.end} onChange={(event) => { this.setState({ end: event.target.value }); }} />
          <FormControl variant="standard" fullWidth disabled={!setting.enableTimeEditor}>
            <InputLabel htmlFor="active">Active</InputLabel>
            <Select variant="standard" value={this.state.active || false} onChange={(event) => {
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
                mId: this.state.mId || this.state.nodeId,
                nodeId: this.state.nodeId,
                description: this.state.description,
                start: this.state.start,
                end: this.state.end,
                active: this.state.active,
              });
              this.setState({ isErrorVisible: false });
            }

            event.preventDefault();
            event.stopPropagation();
          }} color="inherit" > {setting.applyButtonText} </Button>
          <Button onClick={(event) => {
            this.onCancel();
            event.preventDefault();
            event.stopPropagation();
            this.setState({ isErrorVisible: false });
          }} color="secondary"> {setting.cancelButtonText} </Button>
        </DialogActions>
      </Dialog>
    );
  }

  private onApply = (entry: MaintenanceEntry) => {
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
  };

  static getDerivedStateFromProps(props: EditMaintenenceEntryDIalogComponentProps, state: EditMaintenenceEntryDIalogComponentState & { initialMaintenenceEntrie: MaintenanceEntry }): EditMaintenenceEntryDIalogComponentState & { initialMaintenenceEntrie: MaintenanceEntry } {
    if (props.initialMaintenenceEntry !== state.initialMaintenenceEntrie) {
      // eslint-disable-next-line no-param-reassign
      state = {
        ...state,
        ...props.initialMaintenenceEntry,
        initialMaintenenceEntrie: props.initialMaintenenceEntry,
      };
    }
    return state;
  }

}

export const EditMaintenenceEntryDIalog = connect(undefined, mapDispatch)(EditMaintenenceEntryDIalogComponent);
export default EditMaintenenceEntryDIalog;