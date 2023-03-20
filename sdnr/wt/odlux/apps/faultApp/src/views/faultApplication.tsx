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
import { RouteComponentProps, withRouter } from 'react-router-dom';

import Refresh from '@mui/icons-material/Refresh';
import Sync from '@mui/icons-material/Sync';
import { AppBar, Tab, Tabs } from '@mui/material';

import { ColumnType, MaterialTable, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { connect, Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { setPanelAction } from '../actions/panelChangeActions';
import ClearStuckAlarmsDialog, { ClearStuckAlarmsDialogMode } from '../components/clearStuckAlarmsDialog';
import RefreshAlarmLogDialog, { RefreshAlarmLogDialogMode } from '../components/refreshAlarmLogDialog';
import RefreshCurrentAlarmsDialog, { RefreshCurrentAlarmsDialogMode } from '../components/refreshCurrentAlarmsDialog';
import { alarmLogEntriesReloadAction, createAlarmLogEntriesActions, createAlarmLogEntriesProperties } from '../handlers/alarmLogEntriesHandler';
import { createCurrentAlarmsActions, createCurrentAlarmsProperties, currentAlarmsReloadAction } from '../handlers/currentAlarmsHandler';
import { Fault, FaultAlarmNotification } from '../models/fault';
import { PanelId } from '../models/panelId';

const mapProps = (state: IApplicationStoreState) => ({
  panelId: state.fault.currentOpenPanel,
  currentAlarmsProperties: createCurrentAlarmsProperties(state),
  faultNotifications: state.fault.faultNotifications,
  alarmLogEntriesProperties: createAlarmLogEntriesProperties(state),
});

const mapDisp = (dispatcher: IDispatcher) => ({
  currentAlarmsActions: createCurrentAlarmsActions(dispatcher.dispatch),
  alarmLogEntriesActions: createAlarmLogEntriesActions(dispatcher.dispatch),
  reloadCurrentAlarms: () => dispatcher.dispatch(currentAlarmsReloadAction),
  reloadAlarmLogEntries: () => dispatcher.dispatch(alarmLogEntriesReloadAction),
  switchActivePanel: (panelId: PanelId) => {
    dispatcher.dispatch(setPanelAction(panelId));
  },
});

type FaultApplicationComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp>;

type FaultApplicationState = {
  clearAlarmDialogMode: ClearStuckAlarmsDialogMode;
  stuckAlarms: string[];
  refreshAlarmLogEditorMode: RefreshAlarmLogDialogMode;
  refreshCurrentAlarmsEditorMode: RefreshCurrentAlarmsDialogMode;
};


const FaultTable = MaterialTable as MaterialTableCtorType<Fault>;
const FaultAlarmNotificationTable = MaterialTable as MaterialTableCtorType<FaultAlarmNotification>;

let currentAlarmsInitalSorted = false;
let alarmLogInitialSorted = false;

class FaultApplicationComponent extends React.Component<FaultApplicationComponentProps, FaultApplicationState> {
  constructor(props: FaultApplicationComponentProps) {
    super(props);
    this.state = {
      clearAlarmDialogMode: ClearStuckAlarmsDialogMode.None,
      stuckAlarms: [],
      refreshAlarmLogEditorMode: RefreshAlarmLogDialogMode.None,
      refreshCurrentAlarmsEditorMode: RefreshCurrentAlarmsDialogMode.None,
    };
  }

  onDialogClose = () => {
    this.setState({ clearAlarmDialogMode: ClearStuckAlarmsDialogMode.None, stuckAlarms: [] });
  };

  onDialogOpen = () => {
    const stuckAlarms = [...new Set(this.props.currentAlarmsProperties.rows.map(item => item.nodeId))];
    this.setState({ clearAlarmDialogMode: ClearStuckAlarmsDialogMode.Show, stuckAlarms: stuckAlarms });
  };

  private onHandleTabChange = (event: React.SyntheticEvent, newValue: PanelId) => {
    this.onToggleTabs(newValue);
  };

  private onToggleTabs = (panelId: PanelId) => {
    const nextActivePanel = panelId;
    this.props.switchActivePanel(nextActivePanel);
    switch (nextActivePanel) {
      case 'CurrentAlarms':
        if (!currentAlarmsInitalSorted) {
          currentAlarmsInitalSorted = true;
          this.props.currentAlarmsActions.onHandleExplicitRequestSort('timestamp', 'desc');
        } else {
          this.props.reloadCurrentAlarms();
        }
        break;
      case 'AlarmLog':
        if (!alarmLogInitialSorted) {
          alarmLogInitialSorted = true;
          this.props.alarmLogEntriesActions.onHandleExplicitRequestSort('timestamp', 'desc');
        } else {
          this.props.reloadAlarmLogEntries();
        }
        break;
      case 'AlarmNotifications':
      case null:
      default:
        // nothing to do
        break;
    }
  };



  render(): JSX.Element {

    const clearAlarmsAction = {
      icon: Sync, tooltip: 'Clear stuck alarms', ariaLabel:'clear-stuck-alarms', onClick: this.onDialogOpen,
    };

    const refreshCurrentAlarmsAction = {
      icon: Refresh, tooltip: 'Refresh Current Alarms List', ariaLabel:'refresh', onClick: () => {
        this.setState({
          refreshCurrentAlarmsEditorMode: RefreshCurrentAlarmsDialogMode.RefreshCurrentAlarmsTable,
        });
      },
    };

    const refreshAlarmLogAction = {
      icon: Refresh, tooltip: 'Refresh Alarm log table', ariaLabel:'refresh', onClick: () => {
        this.setState({
          refreshAlarmLogEditorMode: RefreshAlarmLogDialogMode.RefreshAlarmLogTable,
        });
      },
    };

    const areFaultsAvailable = this.props.currentAlarmsProperties.rows && this.props.currentAlarmsProperties.rows.length > 0;
    const customActions = areFaultsAvailable ? [clearAlarmsAction, refreshCurrentAlarmsAction] : [refreshCurrentAlarmsAction];

    const { panelId: activePanelId } = this.props;

    return (
      <>
        <AppBar enableColorOnDark position="static" >
          <Tabs indicatorColor="secondary" textColor="inherit" value={activePanelId} onChange={this.onHandleTabChange} aria-label="fault-tabs">
            <Tab aria-label="current-alarms-list-tab" label="Current Alarms" value="CurrentAlarms" />
            <Tab aria-label="alarm-notifications-list-tab" label={`Alarm Notifications (${this.props.faultNotifications.faults.length})`} value="AlarmNotifications" />
            <Tab aria-label="alarm-log-tab" label="Alarm Log" value="AlarmLog" />
          </Tabs>
        </AppBar>
        {
          activePanelId === 'CurrentAlarms' &&
          <>
            <FaultTable stickyHeader tableId="current-alarms-table" idProperty="id" customActionButtons={customActions} columns={[
              // { property: "icon", title: "", type: ColumnType.custom, customControl: this.renderIcon },
              { property: 'severity', title: 'Severity', type: ColumnType.text, width: '140px' },
              { property: 'timestamp', type: ColumnType.text, title: 'Timestamp' },
              { property: 'nodeId', title: 'Node Name', type: ColumnType.text },
              { property: 'counter', title: 'Count', type: ColumnType.numeric, width: '100px' },
              { property: 'objectId', title: 'Object Id', type: ColumnType.text },
              { property: 'problem', title: 'Alarm Type', type: ColumnType.text },
            ]} {...this.props.currentAlarmsProperties} {...this.props.currentAlarmsActions} />
            <RefreshCurrentAlarmsDialog
              mode={this.state.refreshCurrentAlarmsEditorMode}
              onClose={this.onCloseRefreshCurrentAlarmsDialog}
            />
          </>
        }
        {activePanelId === 'AlarmNotifications' &&

          <FaultAlarmNotificationTable stickyHeader tableId="alarm-notifications-table" idProperty="id" defaultSortColumn='timeStamp' defaultSortOrder='desc' rows={this.props.faultNotifications.faults} asynchronus columns={[
            // { property: "icon", title: "", type: ColumnType.custom, customControl: this.renderIcon },
            { property: 'severity', title: 'Severity', width: '140px', type: ColumnType.text },
            { property: 'timeStamp', title: 'Timestamp', type: ColumnType.text },
            { property: 'nodeName', title: 'Node Name', type: ColumnType.text },
            { property: 'counter', title: 'Count', width: '100px', type: ColumnType.numeric },
            { property: 'objectId', title: 'Object Id', type: ColumnType.text },
            { property: 'problem', title: 'Alarm Type', type: ColumnType.text },
          ]} />
        }

        {activePanelId === 'AlarmLog' &&
          <>
            <FaultTable stickyHeader idProperty={'id'} tableId="alarm-log-table" customActionButtons={[refreshAlarmLogAction]}
              columns={[
                // { property: "icon", title: "", type: ColumnType.custom, customControl: this.renderIcon },
                { property: 'severity', title: 'Severity', width: '140px' },
                { property: 'timestamp', title: 'Timestamp' },
                { property: 'nodeId', title: 'Node Name' },
                { property: 'counter', title: 'Count', type: ColumnType.numeric, width: '100px' },
                { property: 'objectId', title: 'Object Id' },
                { property: 'problem', title: 'Alarm Type' },
                { property: 'sourceType', title: 'Source', width: '140px' },
              ]} {...this.props.alarmLogEntriesProperties} {...this.props.alarmLogEntriesActions} />
            <RefreshAlarmLogDialog
              mode={this.state.refreshAlarmLogEditorMode}
              onClose={this.onCloseRefreshAlarmLogDialog}
            />
          </>

        }
        {
          this.state.clearAlarmDialogMode !== ClearStuckAlarmsDialogMode.None && <ClearStuckAlarmsDialog mode={this.state.clearAlarmDialogMode} numberDevices={this.state.stuckAlarms.length} stuckAlarms={this.state.stuckAlarms} onClose={this.onDialogClose} />
        }
      </>
    );
  }

  public componentDidMount() {
    if (this.props.panelId === null) { //set default tab if none is set
      this.onToggleTabs('CurrentAlarms');
    } else {
      this.onToggleTabs(this.props.panelId);
    }
  }

  // private renderIcon = (props: { rowData: Fault | FaultAlarmNotification }) => {
  //   return (
  //     <FontAwesomeIcon icon={faExclamationTriangle} />
  //   );
  // };

  private onCloseRefreshAlarmLogDialog = () => {
    this.setState({
      refreshAlarmLogEditorMode: RefreshAlarmLogDialogMode.None,
    });
  };

  private onCloseRefreshCurrentAlarmsDialog = () => {
    this.setState({
      refreshCurrentAlarmsEditorMode: RefreshCurrentAlarmsDialogMode.None,
    });
  };
}

export const FaultApplication = withRouter(connect(mapProps, mapDisp)(FaultApplicationComponent));
export default FaultApplication;
