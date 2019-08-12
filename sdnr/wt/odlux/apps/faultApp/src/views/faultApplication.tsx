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

import { Fault } from '../models/fault';
import { PanelId } from '../models/panelId';

import { createCurrentProblemsProperties, createCurrentProblemsActions, currentProblemsReloadAction } from '../handlers/currentProblemsHandler';
import { createAlarmLogEntriesProperties, createAlarmLogEntriesActions, alarmLogEntriesReloadAction } from '../handlers/alarmLogEntriesHandler';
import { SetPanelAction } from '../actions/panelChangeActions';

const mapProps = (state: IApplicationStoreState) => ({
  activePanel: state.fault.currentOpenPanel,
  currentProblemsProperties: createCurrentProblemsProperties(state),
  faultNotifications: state.fault.faultNotifications,
  alarmLogEntriesProperties: createAlarmLogEntriesProperties(state)
});

const mapDisp = (dispatcher: IDispatcher) => ({
  currentProblemsActions: createCurrentProblemsActions(dispatcher.dispatch),
  alarmLogEntriesActions: createAlarmLogEntriesActions(dispatcher.dispatch),
  reloadCurrentProblems: () => dispatcher.dispatch(currentProblemsReloadAction),
  reloadAlarmLogEntries: () => dispatcher.dispatch(alarmLogEntriesReloadAction),
  setCurrentPanel: (panelId: PanelId) => dispatcher.dispatch(new SetPanelAction(panelId))
});

type FaultApplicationComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp>;


const FaultTable = MaterialTable as MaterialTableCtorType<Fault>;

class FaultApplicationComponent extends React.Component<FaultApplicationComponentProps>{

  render(): JSX.Element {

    const { activePanel } = this.props;

    const onTogglePanel = (panelId: PanelId) => {
      const nextActivePanel = panelId === this.props.activePanel ? null : panelId;
      this.props.setCurrentPanel(nextActivePanel);

      switch (nextActivePanel) {
        case 'CurrentProblem':
          this.props.reloadCurrentProblems();
          break;
        case 'AlarmLog':
          this.props.reloadAlarmLogEntries();
          break;
        case 'AlarmNotifications':
        case null:
        default:
          // nothing to do
          break;
      }
    };

    return (
      <>
        <Panel activePanel={ activePanel } panelId={ 'CurrentProblem' } onToggle={ onTogglePanel } title={ 'Current Problem List' }>
          <FaultTable idProperty={ '_id' }  columns={ [
              { property: "icon", title: "", type: ColumnType.custom, customControl: this.renderIcon },
              { property: "timeStamp", type: ColumnType.text, title: "Time Stamp" },
              { property: "nodeName", title: "Node Name", type: ColumnType.text },
              { property: "counter", title: "Count", type: ColumnType.numeric, width: "100px" },
              { property: "objectId", title: "Object Id", type: ColumnType.text } ,
              { property: "problem", title: "Alarm Type", type: ColumnType.text },
              { property: "severity", title: "Severity", type: ColumnType.text, width: "140px" },
              ] } { ...this.props.currentProblemsProperties } { ...this.props.currentProblemsActions }  />
        </Panel>
        <Panel activePanel={ activePanel } panelId={ 'AlarmNotifications' } onToggle={ onTogglePanel } title={ `Alarm Notifications ${this.props.faultNotifications.faults.length} since ${this.props.faultNotifications.since}` }>
          <FaultTable rows={ this.props.faultNotifications.faults } asynchronus columns={ [
            { property: "icon", title: "", type: ColumnType.custom, customControl: this.renderIcon },
            { property: "timeStamp", title: "Time Stamp" },
            { property: "nodeName", title: "Node Name" },
            { property: "counter", title: "Count", width: "100px" },
            { property: "objectId", title: "Object Id" },
            { property: "problem", title: "Alarm Type" },
            { property: "severity", title: "Severity", width: "140px" },
            ] } idProperty={ '_id' } />
        </Panel>
        <Panel activePanel={ activePanel } panelId={ 'AlarmLog' } onToggle={ onTogglePanel } title={ 'Alarm Log' }>
          <FaultTable idProperty={ '_id' } columns={ [
            { property: "icon", title: "", type: ColumnType.custom, customControl: this.renderIcon },
            { property: "timeStamp", title: "Time Stamp" },
            { property: "nodeName", title: "Node Name" },
            { property: "counter", title: "Count", type: ColumnType.numeric, width: "100px" },
            { property: "objectId", title: "Object Id" },
            { property: "problem", title: "Alarm Type" },
            { property: "severity", title: "Severity", width: "140px" },
          ] } { ...this.props.alarmLogEntriesProperties } { ...this.props.alarmLogEntriesActions }/>
         </Panel>
      </>
    );
  };

  public componentDidMount() {
    this.props.alarmLogEntriesActions.onToggleFilter();
    this.props.currentProblemsActions.onToggleFilter();
  }
  private renderIcon = (props: { rowData: Fault }) => {
    return (
      <FontAwesomeIcon icon={ faExclamationTriangle } />
    );
  };

}

export const FaultApplication = withRouter(connect(mapProps, mapDisp)(FaultApplicationComponent));
export default FaultApplication;
