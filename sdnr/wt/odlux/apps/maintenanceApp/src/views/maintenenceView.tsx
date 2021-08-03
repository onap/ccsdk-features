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

import { Theme, createStyles, WithStyles, withStyles, Tooltip } from '@material-ui/core';

import { faBan } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import AddIcon from '@material-ui/icons/Add';
import EditIcon from '@material-ui/icons/Edit';
import RemoveIcon from '@material-ui/icons/RemoveCircleOutline';
import Refresh from '@material-ui/icons/Refresh';
import { MenuItem, Divider, Typography } from '@material-ui/core';

import connect, { IDispatcher, Connect } from '../../../../framework/src/flux/connect';
import MaterialTable, { MaterialTableCtorType, ColumnType } from '../../../../framework/src/components/material-table';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { MaintenenceEntry, spoofSymbol } from '../models/maintenenceEntryType';

import EditMaintenenceEntryDialog, { EditMaintenenceEntryDialogMode } from '../components/editMaintenenceEntryDialog';
import RefreshMaintenanceEntriesDialog, { RefreshMaintenanceEntriesDialogMode } from '../components/refreshMaintenanceEntries';
import { convertToLocaleString } from '../utils/timeUtils';
import { createmaintenanceEntriesActions, createmaintenanceEntriesProperties, maintenanceEntriesReloadAction } from '../handlers/maintenenceEntriesHandler';

const styles = (theme: Theme) => createStyles({
  button: {
    margin: 0,
    padding: "6px 6px",
    minWidth: 'unset'
  },
  spacer: {
    marginLeft: theme.spacing(1),
    marginRight: theme.spacing(1),
    display: "inline"
  }
});

const MaintenenceEntriesTable = MaterialTable as MaterialTableCtorType<MaintenenceEntry>;

const mapProps = (state: IApplicationStoreState) => ({
  maintenanceEntriesProperties: createmaintenanceEntriesProperties(state)
});

const mapDispatcher = (dispatcher: IDispatcher) => ({
  maintenanceEntriesActions: createmaintenanceEntriesActions(dispatcher.dispatch),
  onLoadMaintenanceEntries: async () => {
    await dispatcher.dispatch(maintenanceEntriesReloadAction)
  }
});

const emptyMaintenenceEntry: MaintenenceEntry = {
  _id: '',
  nodeId: '',
  description: '',
  start: convertToLocaleString(new Date().valueOf()),
  end: convertToLocaleString(new Date().valueOf()),
  active: false,
};

type MaintenenceViewComponentProps = Connect<typeof mapProps, typeof mapDispatcher> & WithStyles<typeof styles> & {

}

type MaintenenceViewComponentState = {
  maintenenceEntryToEdit: MaintenenceEntry;
  maintenenceEntryEditorMode: EditMaintenenceEntryDialogMode;
  refreshMaintenenceEntriesEditorMode: RefreshMaintenanceEntriesDialogMode;
};

let initialSorted = false;

class MaintenenceViewComponent extends React.Component<MaintenenceViewComponentProps, MaintenenceViewComponentState> {

  constructor(props: MaintenenceViewComponentProps) {
    super(props);

    this.state = {
      maintenenceEntryToEdit: emptyMaintenenceEntry,
      maintenenceEntryEditorMode: EditMaintenenceEntryDialogMode.None,
      refreshMaintenenceEntriesEditorMode: RefreshMaintenanceEntriesDialogMode.None
    };

  }

  getContextMenu(rowData: MaintenenceEntry): JSX.Element[] {
    let buttonArray = [
      <MenuItem aria-label={"1hr-from-now"} onClick={event => this.onOpenPlus1hEditMaintenenceEntryDialog(event, rowData)}><Typography>+1h</Typography></MenuItem>,
      <MenuItem aria-label={"8hr-from-now"} onClick={event => this.onOpenPlus8hEditMaintenenceEntryDialog(event, rowData)}><Typography>+8h</Typography></MenuItem>,
      <Divider />,
      <MenuItem aria-label={"edit"} onClick={event => this.onOpenEditMaintenenceEntryDialog(event, rowData)}><EditIcon /><Typography>Edit</Typography></MenuItem>,
      <MenuItem aria-label={"remove"} onClick={event => this.onOpenRemoveMaintenenceEntryDialog(event, rowData)}><RemoveIcon /><Typography>Remove</Typography></MenuItem>
    ];
    return buttonArray;
  }

  render() {
    const { classes } = this.props;
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
          maintenenceEntryEditorMode: EditMaintenenceEntryDialogMode.AddMaintenenceEntry
        });
      }
    };

    const refreshMaintenanceEntriesAction = {
      icon: Refresh, tooltip: 'Refresh Maintenance Entries', ariaLabel: 'refresh', onClick: () => {
        this.setState({
          refreshMaintenenceEntriesEditorMode: RefreshMaintenanceEntriesDialogMode.RefreshMaintenanceEntriesTable
        });
      }
    };

    const now = new Date().valueOf();
    return (
      <>
        <MaintenenceEntriesTable stickyHeader tableId="maintenance-table" title={"Maintenance"} customActionButtons={[refreshMaintenanceEntriesAction, addMaintenenceEntryAction]} columns={
          [
            { property: "nodeId", title: "Node Name", type: ColumnType.text },
            {
              property: "notifications", title: "Notification", width: 50, align: "center", type: ColumnType.custom, customControl: ({ rowData }) => (
                rowData.active && (Date.parse(rowData.start).valueOf() <= now) && (Date.parse(rowData.end).valueOf() >= now) && <FontAwesomeIcon icon={faBan} /> || null
              )
            },
            { property: "active", title: "Activation State", type: ColumnType.boolean, labels: { "true": "active", "false": "not active" }, },
            { property: "start", title: "Start Date (UTC)", type: ColumnType.text },
            { property: "end", title: "End Date (UTC)", type: ColumnType.text }
          ]
        } idProperty={'_id'}{...this.props.maintenanceEntriesActions} {...this.props.maintenanceEntriesProperties} asynchronus createContextMenu={rowData => {
          return this.getContextMenu(rowData);
        }} >
        </MaintenenceEntriesTable>
        <EditMaintenenceEntryDialog initialMaintenenceEntry={this.state.maintenenceEntryToEdit} mode={this.state.maintenenceEntryEditorMode}
          onClose={this.onCloseEditMaintenenceEntryDialog} />
        <RefreshMaintenanceEntriesDialog mode={this.state.refreshMaintenenceEntriesEditorMode}
          onClose={this.onCloseRefreshMaintenenceEntryDialog} />
      </>
    );
  }

  public componentDidMount() {

    if (!initialSorted) {
      initialSorted = true;
      this.props.maintenanceEntriesActions.onHandleRequestSort("node-id");
    } else {
      this.props.onLoadMaintenanceEntries();
    }


  }

  private onOpenPlus1hEditMaintenenceEntryDialog = (event: React.MouseEvent<HTMLElement>, entry: MaintenenceEntry) => {
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
      maintenenceEntryEditorMode: EditMaintenenceEntryDialogMode.EditMaintenenceEntry
    });
  }

  private onOpenPlus8hEditMaintenenceEntryDialog = (event: React.MouseEvent<HTMLElement>, entry: MaintenenceEntry) => {
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
      maintenenceEntryEditorMode: EditMaintenenceEntryDialogMode.EditMaintenenceEntry
    });
  }

  private onOpenEditMaintenenceEntryDialog = (event: React.MouseEvent<HTMLElement>, entry: MaintenenceEntry) => {
    // event.preventDefault();
    // event.stopPropagation();
    const startTime = (new Date().valueOf());
    const endTime = startTime;
    this.setState({
      maintenenceEntryToEdit: {
        ...entry,
        ...(entry.start && endTime)
          ? { start: convertToLocaleString(entry.start), end: convertToLocaleString(entry.end) }
          : { start: convertToLocaleString(startTime), end: convertToLocaleString(endTime) }
      },
      maintenenceEntryEditorMode: EditMaintenenceEntryDialogMode.EditMaintenenceEntry
    });
  }

  private onOpenRemoveMaintenenceEntryDialog = (event: React.MouseEvent<HTMLElement>, entry: MaintenenceEntry) => {
    // event.preventDefault();
    // event.stopPropagation();
    const startTime = (new Date().valueOf());
    const endTime = startTime;
    this.setState({
      maintenenceEntryToEdit: {
        ...entry,
        ...(entry.start && endTime)
          ? { start: convertToLocaleString(entry.start), end: convertToLocaleString(entry.end) }
          : { start: convertToLocaleString(startTime), end: convertToLocaleString(endTime) }
      },
      maintenenceEntryEditorMode: EditMaintenenceEntryDialogMode.RemoveMaintenenceEntry
    });
  }

  private onCloseEditMaintenenceEntryDialog = () => {
    this.setState({
      maintenenceEntryToEdit: emptyMaintenenceEntry,
      maintenenceEntryEditorMode: EditMaintenenceEntryDialogMode.None,
    });
  }

  private onCloseRefreshMaintenenceEntryDialog = () => {
    this.setState({
      refreshMaintenenceEntriesEditorMode: RefreshMaintenanceEntriesDialogMode.None,
    });
  }
}

export const MaintenenceView = withStyles(styles)(connect(mapProps, mapDispatcher)(MaintenenceViewComponent));
export default MaintenenceView;
