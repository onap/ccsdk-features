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

import { faBan } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import Refresh from '@mui/icons-material/Refresh';
import RemoveIcon from '@mui/icons-material/RemoveCircleOutline';
import { Divider, MenuItem, Theme, Typography } from '@mui/material';
import { WithStyles } from '@mui/styles';
import createStyles from '@mui/styles/createStyles';
import withStyles from '@mui/styles/withStyles';

import MaterialTable, { ColumnType, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { connect, Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import EditMaintenenceEntryDialog, { EditMaintenenceEntryDialogMode } from '../components/editMaintenenceEntryDialog';
import RefreshMaintenanceEntriesDialog, { RefreshMaintenanceEntriesDialogMode } from '../components/refreshMaintenanceEntries';
import { createmaintenanceEntriesActions, createmaintenanceEntriesProperties, maintenanceEntriesReloadAction } from '../handlers/maintenanceEntriesHandler';
import { MaintenanceEntry } from '../models/maintenanceEntryType';
import { convertToLocaleString } from '../utils/timeUtils';

const styles = (theme: Theme) => createStyles({
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

const MaintenanceEntriesTable = MaterialTable as MaterialTableCtorType<MaintenanceEntry>;

const mapProps = (state: IApplicationStoreState) => ({
  maintenanceEntriesProperties: createmaintenanceEntriesProperties(state),
});

const mapDispatcher = (dispatcher: IDispatcher) => ({
  maintenanceEntriesActions: createmaintenanceEntriesActions(dispatcher.dispatch),
  onLoadMaintenanceEntries: async () => {
    await dispatcher.dispatch(maintenanceEntriesReloadAction);
  },
});

const emptyMaintenenceEntry: MaintenanceEntry = {
  mId: '',
  nodeId: '',
  description: '',
  start: convertToLocaleString(new Date().valueOf()),
  end: convertToLocaleString(new Date().valueOf()),
  active: false,
};

type MaintenanceViewComponentProps = Connect<typeof mapProps, typeof mapDispatcher> & WithStyles<typeof styles> & {};

type MaintenenceViewComponentState = {
  maintenenceEntryToEdit: MaintenanceEntry;
  maintenanceEntryEditorMode: EditMaintenenceEntryDialogMode;
  refreshMaintenenceEntriesEditorMode: RefreshMaintenanceEntriesDialogMode;
};

let initialSorted = false;

class MaintenenceViewComponent extends React.Component<MaintenanceViewComponentProps, MaintenenceViewComponentState> {

  constructor(props: MaintenanceViewComponentProps) {
    super(props);

    this.state = {
      maintenenceEntryToEdit: emptyMaintenenceEntry,
      maintenanceEntryEditorMode: EditMaintenenceEntryDialogMode.None,
      refreshMaintenenceEntriesEditorMode: RefreshMaintenanceEntriesDialogMode.None,
    };

  }

  getContextMenu(rowData: MaintenanceEntry): JSX.Element[] {
    let buttonArray = [
      <MenuItem aria-label={'1hr-from-now'} onClick={event => this.onOpenPlus1hEditMaintenenceEntryDialog(event, rowData)}><Typography>+1h</Typography></MenuItem>,
      <MenuItem aria-label={'8hr-from-now'} onClick={event => this.onOpenPlus8hEditMaintenenceEntryDialog(event, rowData)}><Typography>+8h</Typography></MenuItem>,
      <Divider />,
      <MenuItem aria-label={'edit'} onClick={event => this.onOpenEditMaintenenceEntryDialog(event, rowData)}><EditIcon /><Typography>Edit</Typography></MenuItem>,
      <MenuItem aria-label={'remove'} onClick={event => this.onOpenRemoveMaintenenceEntryDialog(event, rowData)}><RemoveIcon /><Typography>Remove</Typography></MenuItem>,
    ];
    return buttonArray;
  }

  render() {
    const addMaintenenceEntryAction = {
      icon: AddIcon, tooltip: 'Add', ariaLabel:'add-element', onClick: () => {
        const startTime = (new Date().valueOf());
        const endTime = startTime;
        this.setState({
          maintenenceEntryToEdit: {
            ...emptyMaintenenceEntry,
            start: convertToLocaleString(startTime),
            end: convertToLocaleString(endTime),
          },
          maintenanceEntryEditorMode: EditMaintenenceEntryDialogMode.AddMaintenenceEntry,
        });
      },
    };

    const refreshMaintenanceEntriesAction = {
      icon: Refresh, tooltip: 'Refresh Maintenance Entries', ariaLabel: 'refresh', onClick: () => {
        this.setState({
          refreshMaintenenceEntriesEditorMode: RefreshMaintenanceEntriesDialogMode.RefreshMaintenanceEntriesTable,
        });
      },
    };

    const now = new Date().valueOf();
    return (
      <>
        <MaintenanceEntriesTable stickyHeader tableId="maintenance-table" title={'Maintenance'} customActionButtons={[refreshMaintenanceEntriesAction, addMaintenenceEntryAction]} columns={
          [
            { property: 'nodeId', title: 'Node Name', type: ColumnType.text },
            {
              property: 'notifications', title: 'Notification', width: 50, align: 'center', type: ColumnType.custom, customControl: ({ rowData }) => (
                rowData.active && (Date.parse(rowData.start).valueOf() <= now) && (Date.parse(rowData.end).valueOf() >= now) && <FontAwesomeIcon icon={faBan} /> || null
              ),
            },
            { property: 'active', title: 'Activation State', type: ColumnType.boolean, labels: { 'true': 'active', 'false': 'not active' } },
            { property: 'start', title: 'Start Date (UTC)', type: ColumnType.text },
            { property: 'end', title: 'End Date (UTC)', type: ColumnType.text },
          ]
        } idProperty={'mId'}{...this.props.maintenanceEntriesActions} {...this.props.maintenanceEntriesProperties} asynchronus createContextMenu={rowData => {
          return this.getContextMenu(rowData);
        }} >
        </MaintenanceEntriesTable>
        <EditMaintenenceEntryDialog initialMaintenenceEntry={this.state.maintenenceEntryToEdit} mode={this.state.maintenanceEntryEditorMode}
          onClose={this.onCloseEditMaintenenceEntryDialog} />
        <RefreshMaintenanceEntriesDialog mode={this.state.refreshMaintenenceEntriesEditorMode}
          onClose={this.onCloseRefreshMaintenenceEntryDialog} />
      </>
    );
  }

  public componentDidMount() {

    if (!initialSorted) {
      initialSorted = true;
      this.props.maintenanceEntriesActions.onHandleRequestSort('node-id');
    } else {
      this.props.onLoadMaintenanceEntries();
    }


  }

  private onOpenPlus1hEditMaintenenceEntryDialog = (event: React.MouseEvent<HTMLElement>, entry: MaintenanceEntry) => {
    // event.preventDefault();
    // event.stopPropagation();
    const startTime = (new Date().valueOf());
    const endTime = startTime + (1 * 60 * 60 * 1000);
    this.setState({
      maintenenceEntryToEdit: {
        ...entry,
        start: convertToLocaleString(startTime),
        end: convertToLocaleString(endTime),
      },
      maintenanceEntryEditorMode: EditMaintenenceEntryDialogMode.EditMaintenenceEntry,
    });
  };

  private onOpenPlus8hEditMaintenenceEntryDialog = (event: React.MouseEvent<HTMLElement>, entry: MaintenanceEntry) => {
    // event.preventDefault();
    // event.stopPropagation();
    const startTime = (new Date().valueOf());
    const endTime = startTime + (8 * 60 * 60 * 1000);
    this.setState({
      maintenenceEntryToEdit: {
        ...entry,
        start: convertToLocaleString(startTime),
        end: convertToLocaleString(endTime),
      },
      maintenanceEntryEditorMode: EditMaintenenceEntryDialogMode.EditMaintenenceEntry,
    });
  };

  private onOpenEditMaintenenceEntryDialog = (event: React.MouseEvent<HTMLElement>, entry: MaintenanceEntry) => {
    // event.preventDefault();
    // event.stopPropagation();
    const startTime = (new Date().valueOf());
    const endTime = startTime;
    this.setState({
      maintenenceEntryToEdit: {
        ...entry,
        ...(entry.start && endTime ? { start: convertToLocaleString(entry.start), end: convertToLocaleString(entry.end) } : { start: convertToLocaleString(startTime), end: convertToLocaleString(endTime) }),
      },
      maintenanceEntryEditorMode: EditMaintenenceEntryDialogMode.EditMaintenenceEntry,
    });
  };

  private onOpenRemoveMaintenenceEntryDialog = (event: React.MouseEvent<HTMLElement>, entry: MaintenanceEntry) => {
    // event.preventDefault();
    // event.stopPropagation();
    const startTime = (new Date().valueOf());
    const endTime = startTime;
    this.setState({
      maintenenceEntryToEdit: {
        ...entry,
        ...(entry.start && endTime ? { start: convertToLocaleString(entry.start), end: convertToLocaleString(entry.end) } : { start: convertToLocaleString(startTime), end: convertToLocaleString(endTime) }),
      },
      maintenanceEntryEditorMode: EditMaintenenceEntryDialogMode.RemoveMaintenenceEntry,
    });
  };

  private onCloseEditMaintenenceEntryDialog = () => {
    this.setState({
      maintenenceEntryToEdit: emptyMaintenenceEntry,
      maintenanceEntryEditorMode: EditMaintenenceEntryDialogMode.None,
    });
  };

  private onCloseRefreshMaintenenceEntryDialog = () => {
    this.setState({
      refreshMaintenenceEntriesEditorMode: RefreshMaintenanceEntriesDialogMode.None,
    });
  };
}

export const MaintenanceView = withStyles(styles)(connect(mapProps, mapDispatcher)(MaintenenceViewComponent));

