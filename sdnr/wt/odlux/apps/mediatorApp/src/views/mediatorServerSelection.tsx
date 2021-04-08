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
import { WithStyles, withStyles, createStyles, Theme, Tooltip } from '@material-ui/core';

import AddIcon from '@material-ui/icons/Add';
import IconButton from '@material-ui/core/IconButton';
import EditIcon from '@material-ui/icons/Edit';
import DeleteIcon from '@material-ui/icons/Delete';
import Refresh from '@material-ui/icons/Refresh';

import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { IDispatcher, Connect } from '../../../../framework/src/flux/connect';
import MaterialTable, { MaterialTableCtorType, ColumnType } from '../../../../framework/src/components/material-table';

import { createAvaliableMediatorServersProperties, createAvaliableMediatorServersActions } from '../handlers/avaliableMediatorServersHandler';

import { MediatorServer } from '../models/mediatorServer';
import EditMediatorServerDialog, { EditMediatorServerDialogMode } from '../components/editMediatorServerDialog';
import RefreshMediatorDialog, { RefreshMediatorDialogMode } from '../components/refreshMediatorDialog';
import { NavigateToApplication } from '../../../../framework/src/actions/navigationActions';

const MediatorServersTable = MaterialTable as MaterialTableCtorType<MediatorServer>;

const styles = (theme: Theme) => createStyles({
  button: {
    margin: 0,
    padding: "6px 6px",
    minWidth: 'unset',
  },
  spacer: {
    marginLeft: theme.spacing(1),
    marginRight: theme.spacing(1),
    display: "inline",
  },
});

const mapProps = (state: IApplicationStoreState) => ({
  mediatorServersProperties: createAvaliableMediatorServersProperties(state),
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  mediatorServersActions: createAvaliableMediatorServersActions(dispatcher.dispatch),
  selectMediatorServer: (mediatorServerId: string) => mediatorServerId && dispatcher.dispatch(new NavigateToApplication("mediator", mediatorServerId)),
});

const emptyMediatorServer: MediatorServer = {
  id: "",
  name: "",
  url: ""
};

type MediatorServerSelectionComponentProps = Connect<typeof mapProps, typeof mapDispatch> & WithStyles<typeof styles>;

type MediatorServerSelectionComponentState = {
  mediatorServerToEdit: MediatorServer,
  mediatorServerEditorMode: EditMediatorServerDialogMode,
  refreshMediatorEditorMode: RefreshMediatorDialogMode
}

let initialSorted = false;

class MediatorServerSelectionComponent extends React.Component<MediatorServerSelectionComponentProps, MediatorServerSelectionComponentState> {

  constructor(props: MediatorServerSelectionComponentProps) {
    super(props);

    this.state = {
      mediatorServerEditorMode: EditMediatorServerDialogMode.None,
      mediatorServerToEdit: emptyMediatorServer,
      refreshMediatorEditorMode: RefreshMediatorDialogMode.None
    }
  }

  render() {
    const { classes } = this.props;
    const refreshMediatorAction = {
      icon: Refresh, tooltip: 'Refresh Mediator Server Table', onClick: () => {
        this.setState({
          refreshMediatorEditorMode: RefreshMediatorDialogMode.RefreshMediatorTable
        });
      }
    };

    const addMediatorServerActionButton = {
      icon: AddIcon, tooltip: 'Add', onClick: () => {
        this.setState({
          mediatorServerEditorMode: EditMediatorServerDialogMode.AddMediatorServer,
          mediatorServerToEdit: emptyMediatorServer,
        });
      }
    };
    return (
      <>
        <MediatorServersTable stickyHeader title={"Mediator"} customActionButtons={[refreshMediatorAction, addMediatorServerActionButton]} idProperty={"id"}
          {...this.props.mediatorServersActions} {...this.props.mediatorServersProperties} columns={[
            { property: "name", title: "Name", type: ColumnType.text },
            { property: "url", title: "Url", type: ColumnType.text },
            {
              property: "actions", title: "Actions", type: ColumnType.custom, customControl: ({ rowData }) => (
                <div className={classes.spacer}>
                  <Tooltip title={"Edit"} ><IconButton className={classes.button} onClick={event => { this.onEditMediatorServer(event, rowData); }}><EditIcon /></IconButton></Tooltip>
                  <Tooltip title={"Remove"} ><IconButton className={classes.button} onClick={event => { this.onRemoveMediatorServer(event, rowData); }}><DeleteIcon /></IconButton></Tooltip>
                </div>
              )
            }
          ]} onHandleClick={this.onSelectMediatorServer} />
        <EditMediatorServerDialog
          mediatorServer={this.state.mediatorServerToEdit}
          mode={this.state.mediatorServerEditorMode}
          onClose={this.onCloseEditMediatorServerDialog} />
        <RefreshMediatorDialog
          mode={this.state.refreshMediatorEditorMode}
          onClose={this.onCloseRefreshMediatorDialog}
        />
      </>
    );
  }

  public componentDidMount() {

    if (!initialSorted) {
      initialSorted = true;
      this.props.mediatorServersActions.onHandleRequestSort("name");
    } else {
      this.props.mediatorServersActions.onRefresh();
    }
  }

  private onSelectMediatorServer = (event: React.MouseEvent<HTMLElement>, server: MediatorServer) => {
    event.preventDefault();
    event.stopPropagation();
    this.props.selectMediatorServer(server && server.id);

  }

  private onEditMediatorServer = (event: React.MouseEvent<HTMLElement>, server: MediatorServer) => {
    event.preventDefault();
    event.stopPropagation();
    this.setState({
      mediatorServerEditorMode: EditMediatorServerDialogMode.EditMediatorServer,
      mediatorServerToEdit: server,
    });
  }

  private onRemoveMediatorServer = (event: React.MouseEvent<HTMLElement>, server: MediatorServer) => {
    event.preventDefault();
    event.stopPropagation();
    this.setState({
      mediatorServerEditorMode: EditMediatorServerDialogMode.RemoveMediatorServer,
      mediatorServerToEdit: server,
    });
  }

  private onCloseEditMediatorServerDialog = () => {
    this.setState({
      mediatorServerEditorMode: EditMediatorServerDialogMode.None,
      mediatorServerToEdit: emptyMediatorServer,
    });
  }
  private onCloseRefreshMediatorDialog = () => {
    this.setState({
      refreshMediatorEditorMode: RefreshMediatorDialogMode.None
    });
  }
}


export const MediatorServerSelection = withStyles(styles)(connect(mapProps, mapDispatch)(MediatorServerSelectionComponent));
export default MediatorServerSelection;