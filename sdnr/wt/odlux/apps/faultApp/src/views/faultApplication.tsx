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

import { withRouter, RouteComponentProps } from 'react-router-dom';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';

import { MaterialTable, ColumnType, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { Panel } from '../../../../framework/src/components/material-ui';

import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { Fault, FaultAlarmNotification } from '../models/fault';
import { PanelId } from '../models/panelId';

import { createCurrentProblemsProperties, createCurrentProblemsActions, currentProblemsReloadAction } from '../handlers/currentProblemsHandler';
import { createAlarmLogEntriesProperties, createAlarmLogEntriesActions, alarmLogEntriesReloadAction } from '../handlers/alarmLogEntriesHandler';
import { setPanelAction } from '../actions/panelChangeActions';
import { Tooltip, IconButton, AppBar, Tabs, Tab } from '@material-ui/core';
import RefreshIcon from '@material-ui/icons/Refresh';
import ClearStuckAlarmsDialog, { ClearStuckAlarmsDialogMode } from '../components/clearStuckAlarmsDialog';
import { SetPartialUpdatesAction } from '../actions/partialUpdatesAction';

const mapProps = (state: IApplicationStoreState) => ({
  panelId: state.fault.currentOpenPanel,
  currentProblemsProperties: createCurrentProblemsProperties(state),
  faultNotifications: state.fault.faultNotifications,
  alarmLogEntriesProperties: createAlarmLogEntriesProperties(state)
});

const mapDisp = (dispatcher: IDispatcher) => ({
  currentProblemsActions: createCurrentProblemsActions(dispatcher.dispatch),
  alarmLogEntriesActions: createAlarmLogEntriesActions(dispatcher.dispatch),
  reloadCurrentProblems: () => dispatcher.dispatch(currentProblemsReloadAction),
  reloadAlarmLogEntries: () => dispatcher.dispatch(alarmLogEntriesReloadAction),
  switchActivePanel: (panelId: PanelId) => {
    dispatcher.dispatch(setPanelAction(panelId));
  },
  setPartialUpdates: (active: boolean) => dispatcher.dispatch(new SetPartialUpdatesAction(active))
});

type FaultApplicationComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp>;

type FaultApplicationState = {
  clearAlarmDialogMode: ClearStuckAlarmsDialogMode,
  stuckAlarms: string[]
}


const FaultTable = MaterialTable as MaterialTableCtorType<Fault>;
const FaultAlarmNotificationTable = MaterialTable as MaterialTableCtorType<FaultAlarmNotification>;

let currentProblemsInitalSorted = false;
let alarmLogInitialSorted = false;

class FaultApplicationComponent extends React.Component<FaultApplicationComponentProps, FaultApplicationState>{

  /**
   *
   */
  constructor(props: FaultApplicationComponentProps) {
    super(props);
    this.state = { clearAlarmDialogMode: ClearStuckAlarmsDialogMode.None, stuckAlarms: [] }
  }

  onDialogClose = () => {
    this.setState({ clearAlarmDialogMode: ClearStuckAlarmsDialogMode.None, stuckAlarms: [] })
  }

  onDialogOpen = () => {
    const stuckAlarms = [...new Set(this.props.currentProblemsProperties.rows.map(item => item.nodeId))];
    this.setState({ clearAlarmDialogMode: ClearStuckAlarmsDialogMode.Show, stuckAlarms: stuckAlarms })
  }

  private onHandleTabChange = (event: React.ChangeEvent<{}>, newValue: PanelId) => {
    this.onToggleTabs(newValue);
  }

  private onToggleTabs = (panelId: PanelId) => {
    const nextActivePanel = panelId;
    this.props.switchActivePanel(nextActivePanel);
    switch (nextActivePanel) {
      case 'CurrentProblem':
        if (!currentProblemsInitalSorted) {
          currentProblemsInitalSorted = true;
          this.props.currentProblemsActions.onHandleExplicitRequestSort("timestamp", "desc");
        } else {
          this.props.reloadCurrentProblems();
        }
        break;
      case 'AlarmLog':
        if (!alarmLogInitialSorted) {
          alarmLogInitialSorted = true;
          this.props.alarmLogEntriesActions.onHandleExplicitRequestSort("timestamp", "desc");
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

    const refreshButton = {
      icon: RefreshIcon, tooltip: 'Clear stuck alarms', onClick: this.onDialogOpen
    };
    const areFaultsAvailable = this.props.currentProblemsProperties.rows && this.props.currentProblemsProperties.rows.length > 0
    const customAction = areFaultsAvailable ? [refreshButton] : [];

    const { panelId: activePanelId } = this.props;

    return (
      <>
        <AppBar position="static" >
          <Tabs value={activePanelId} onChange={this.onHandleTabChange} aria-label="fault tabs">
            <Tab label="Current Problem List" value="CurrentProblem" />
            <Tab label={`Alarm Notifications (${this.props.faultNotifications.faults.length})`} value="AlarmNotifications" />
            <Tab label="Alarm Log" value="AlarmLog" />
          </Tabs>
        </AppBar>
        {
          activePanelId === 'CurrentProblem' && <FaultTable stickyHeader idProperty={'id'} customActionButtons={customAction} columns={[
            { property: "icon", title: "", type: ColumnType.custom, customControl: this.renderIcon },
            { property: "timestamp", type: ColumnType.text, title: "Time Stamp" },
            { property: "nodeId", title: "Node Name", type: ColumnType.text },
            { property: "counter", title: "Count", type: ColumnType.numeric, width: "100px" },
            { property: "objectId", title: "Object Id", type: ColumnType.text },
            { property: "problem", title: "Alarm Type", type: ColumnType.text },
            { property: "severity", title: "Severity", type: ColumnType.text, width: "140px" },
          ]} {...this.props.currentProblemsProperties} {...this.props.currentProblemsActions} />
        }
        {activePanelId === 'AlarmNotifications' &&

          <FaultAlarmNotificationTable stickyHeader rows={this.props.faultNotifications.faults} asynchronus columns={[
            { property: "icon", title: "", type: ColumnType.custom, customControl: this.renderIcon },
            { property: "timeStamp", title: "Time Stamp" },
            { property: "nodeName", title: "Node Name" },
            { property: "counter", title: "Count", width: "100px", type: ColumnType.numeric },
            { property: "objectId", title: "Object Id" },
            { property: "problem", title: "Alarm Type" },
            { property: "severity", title: "Severity", width: "140px" },
          ]} idProperty={'id'} />

        }

        {activePanelId === 'AlarmLog' && <FaultTable stickyHeader idProperty={'id'} columns={[
          { property: "icon", title: "", type: ColumnType.custom, customControl: this.renderIcon },
          { property: "timestamp", title: "Time Stamp" },
          { property: "nodeId", title: "Node Name" },
          { property: "counter", title: "Count", type: ColumnType.numeric, width: "100px" },
          { property: "objectId", title: "Object Id" },
          { property: "problem", title: "Alarm Type" },
          { property: "severity", title: "Severity", width: "140px" },
          { property: "sourceType", title: "Source", width: "140px" },
        ]} {...this.props.alarmLogEntriesProperties} {...this.props.alarmLogEntriesActions} />

        }
        {
          this.state.clearAlarmDialogMode !== ClearStuckAlarmsDialogMode.None && <ClearStuckAlarmsDialog mode={this.state.clearAlarmDialogMode} numberDevices={this.state.stuckAlarms.length} stuckAlarms={this.state.stuckAlarms} onClose={this.onDialogClose} />
        }
      </>
    )

  };

  componentWillUnmount() {
    this.props.setPartialUpdates(false);
  }

  public componentDidMount() {
    if (this.props.panelId === null) { //set default tab if none is set
      this.onToggleTabs("CurrentProblem");
    }
    this.props.setPartialUpdates(true);
  }

  private renderIcon = (props: { rowData: Fault | FaultAlarmNotification }) => {
    return (
      <FontAwesomeIcon icon={faExclamationTriangle} />
    );
  };

}

export const FaultApplication = withRouter(connect(mapProps, mapDisp)(FaultApplicationComponent));
export default FaultApplication;
