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
import TextField from '@mui/material/TextField';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';

import { IDispatcher, connect, Connect } from '../../../../framework/src/flux/connect';

import { addAvaliableMediatorServerAsyncActionCreator, removeAvaliableMediatorServerAsyncActionCreator, updateAvaliableMediatorServerAsyncActionCreator } from '../actions/avaliableMediatorServersActions';
import { MediatorServer } from '../models/mediatorServer';
import { Typography } from '@mui/material';

export enum EditMediatorServerDialogMode {
  None = "none",
  AddMediatorServer = "addMediatorServer",
  EditMediatorServer = "editMediatorServer",
  RemoveMediatorServer = "removeMediatorServer",
}

const mapDispatch = (dispatcher: IDispatcher) => ({
  addMediatorServer: (element: MediatorServer) => {
    dispatcher.dispatch(addAvaliableMediatorServerAsyncActionCreator(element));
  },
  updateMediatorServer: (element: MediatorServer) => {
    dispatcher.dispatch(updateAvaliableMediatorServerAsyncActionCreator(element));
  },
  removeMediatorServer: (element: MediatorServer) => {
    dispatcher.dispatch(removeAvaliableMediatorServerAsyncActionCreator(element));
  },
});

type DialogSettings = {
  dialogTitle: string;
  dialogDescription: string;
  applyButtonText: string;
  cancelButtonText: string;
  readonly: boolean;
};

const settings: { [key: string]: DialogSettings } = {
  [EditMediatorServerDialogMode.None]: {
    dialogTitle: "",
    dialogDescription: "",
    applyButtonText: "",
    cancelButtonText: "",
    readonly: true,
  },
  [EditMediatorServerDialogMode.AddMediatorServer]: {
    dialogTitle: "Add Mediator Server",
    dialogDescription: "",
    applyButtonText: "Add",
    cancelButtonText: "Cancel",
    readonly: false,
  },
  [EditMediatorServerDialogMode.EditMediatorServer]: {
    dialogTitle: "Edit Mediator Server",
    dialogDescription: "",
    applyButtonText: "Update",
    cancelButtonText: "Cancel",
    readonly: false,
  },
  [EditMediatorServerDialogMode.RemoveMediatorServer]: {
    dialogTitle: "Remove Mediator Server",
    dialogDescription: "",
    applyButtonText: "Remove",
    cancelButtonText: "Cancel",
    readonly: true,
  },
};

type EditMediatorServerDialogComponentProps = Connect<undefined, typeof mapDispatch> & {
  mode: EditMediatorServerDialogMode;
  mediatorServer: MediatorServer;
  onClose: () => void;
};

const urlRegex = RegExp("^https?://");

type EditMediatorServerDialogComponentState = MediatorServer & { errorMessage: string[] };

class EditMediatorServerDialogComponent extends React.Component<EditMediatorServerDialogComponentProps, EditMediatorServerDialogComponentState> {
  constructor(props: EditMediatorServerDialogComponentProps) {
    super(props);

    this.state = {
      ...this.props.mediatorServer,
      errorMessage: []
    };
  }

  areFieldsValid = () => {
    return this.state.name.trim().length > 0 && this.state.url.trim().length > 0 && urlRegex.test(this.state.url);
  }

  createErrorMessages = () => {

    let messages = [];
    if (this.state.name.trim().length === 0 && this.state.url.trim().length === 0) {
      messages.push("The server name and the url must not be empty.")
    }
    else
      if (this.state.url.trim().length === 0) {
        messages.push("The server url must not be empty.")

      } else if (this.state.name.trim().length === 0) {
        messages.push("The server name must not be empty.")
      }

    if (!urlRegex.test(this.state.url)) {
      if (messages.length > 0) {
        return messages.concat(["The server url must start with 'http(s)://'."])
      } else {
        return ["The server url must start with 'http(s)://'."]
      }
    }

    return messages;
  }



  render(): JSX.Element {
    const setting = settings[this.props.mode];

    return (
      <Dialog open={this.props.mode !== EditMediatorServerDialogMode.None}>
        <DialogTitle id="form-dialog-title">{setting.dialogTitle}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {setting.dialogDescription}
          </DialogContentText>
          {/* <TextField disabled spellCheck={false} autoFocus margin="dense" id="id" label="Id" type="text" fullWidth value={ this.state._id } onChange={(event)=>{ this.setState({_id: event.target.value}); } } /> */}
          <TextField variant="standard" disabled={setting.readonly} spellCheck={false} margin="dense" id="name" label="Name" type="text" fullWidth value={this.state.name} onChange={(event) => { this.setState({ name: event.target.value }); }} />
          <TextField variant="standard" disabled={setting.readonly} spellCheck={false} margin="dense" id="url" label="Url" type="text" fullWidth value={this.state.url} onChange={(event) => { this.setState({ url: event.target.value }); }} />

          <Typography id="errorMessage" component={"div"} color="error">{this.state.errorMessage.map((error, index) => <div key={index}>{error}</div>)}</Typography>

        </DialogContent>
        <DialogActions>
          <Button onClick={(event) => {

            if (this.areFieldsValid()) {
              this.setState({ errorMessage: [] });
              this.onApply({
                id: this.state.id,
                name: this.state.name,
                url: this.state.url
              });
            } else {
              const errorMessage = this.createErrorMessages()
              this.setState({ errorMessage: errorMessage })
            }

            event.preventDefault();
            event.stopPropagation();
          }} color="inherit" > {setting.applyButtonText} </Button>
          <Button onClick={(event) => {
            this.onCancel();
            this.setState({ errorMessage: [] });
            event.preventDefault();
            event.stopPropagation();
          }} color="secondary"> {setting.cancelButtonText} </Button>
        </DialogActions>
      </Dialog>
    )
  }

  private onApply = (element: MediatorServer) => {
    this.props.onClose && this.props.onClose();
    switch (this.props.mode) {
      case EditMediatorServerDialogMode.AddMediatorServer:
        element && this.props.addMediatorServer(element);
        break;
      case EditMediatorServerDialogMode.EditMediatorServer:
        element && this.props.updateMediatorServer(element);
        break;
      case EditMediatorServerDialogMode.RemoveMediatorServer:
        element && this.props.removeMediatorServer(element);
        break;
    }
  };

  private onCancel = () => {
    this.props.onClose && this.props.onClose();
  }

  static getDerivedStateFromProps(props: EditMediatorServerDialogComponentProps, state: EditMediatorServerDialogComponentState & { _initialMediatorServer: MediatorServer }): EditMediatorServerDialogComponentState & { _initialMediatorServer: MediatorServer } {
    if (props.mediatorServer !== state._initialMediatorServer) {
      state = {
        ...state,
        ...props.mediatorServer,
        _initialMediatorServer: props.mediatorServer,
      };
    }
    return state;
  }
}

export const EditMediatorServerDialog = connect(undefined, mapDispatch)(EditMediatorServerDialogComponent);
export default EditMediatorServerDialog;