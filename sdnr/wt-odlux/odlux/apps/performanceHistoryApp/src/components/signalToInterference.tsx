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

import { ColumnModel, ColumnType, MaterialTable, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { connect, Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { SetFilterVisibility, SetSubViewAction } from '../actions/toggleActions';
import { createSignalToInterferenceActions, createSignalToInterferenceProperties } from '../handlers/signalToInterferenceHandler';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { SignalToInterferenceDatabaseDataType, SignalToInterferenceDataType } from '../models/signalToInteferenceDataType';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';
import ToggleContainer from './toggleContainer';

const mapProps = (state: IApplicationStoreState) => ({
  signalToInterferenceProperties: createSignalToInterferenceProperties(state),
  currentView: state.performanceHistory.subViews.SINR.subView,
  isFilterVisible: state.performanceHistory.subViews.SINR.isFilterVisible,
  existingFilter: state.performanceHistory.signalToInterference.filter,
});

const mapDisp = (dispatcher: IDispatcher) => ({
  signalToInterferenceActions: createSignalToInterferenceActions(dispatcher.dispatch),
  setSubView: (value: 'chart' | 'table') => dispatcher.dispatch(new SetSubViewAction('SINR', value)),
  toggleFilterButton: (value: boolean) => { dispatcher.dispatch(new SetFilterVisibility('SINR', value)); },
});

type SignalToInterferenceComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string;
};

const SignalToInterferenceTable = MaterialTable as MaterialTableCtorType<SignalToInterferenceDataType>;

/**
 * The Component which gets the signal to interference data from the database based on the selected time period.
 */
class SignalToInterferenceComponent extends React.Component<SignalToInterferenceComponentProps> {
  onToggleFilterButton = () => {
    this.props.toggleFilterButton(!this.props.isFilterVisible);
  };

  onChange = (value: 'chart' | 'table') => {
    this.props.setSubView(value);
  };

  onFilterChanged = (property: string, filterTerm: string) => {
    this.props.signalToInterferenceActions.onFilterChanged(property, filterTerm);
    if (!this.props.signalToInterferenceProperties.showFilter)
      this.props.signalToInterferenceActions.onToggleFilter(false);
  };

  render(): JSX.Element {
    const properties = this.props.signalToInterferenceProperties;
    const actions = this.props.signalToInterferenceActions;

    const chartPagedData = this.getChartDataValues(properties.rows);

    const sinrColumns: ColumnModel<SignalToInterferenceDataType>[] = [
      { property: 'radioSignalId', title: 'Radio signal', type: ColumnType.text },
      { property: 'scannerId', title: 'Scanner ID', type: ColumnType.text },
      { property: 'timeStamp', title: 'End Time', type: ColumnType.text },
      {
        property: 'suspectIntervalFlag', title: 'Suspect Interval', type: ColumnType.boolean,
      },
    ];

    chartPagedData.datasets.forEach(ds => {
      sinrColumns.push(addColumnLabels<SignalToInterferenceDataType>(ds.name, ds.columnLabel));
    });
    return (
      <>
        <ToggleContainer onToggleFilterButton={this.onToggleFilterButton} showFilter={this.props.isFilterVisible}
          existingFilter={this.props.signalToInterferenceProperties.filter} onFilterChanged={this.onFilterChanged} selectedValue={this.props.currentView} onChange={this.onChange}>
          {lineChart(chartPagedData)}
          <SignalToInterferenceTable stickyHeader idProperty={'_id'} tableId="signal-to-interference-table" columns={sinrColumns} {...properties} {...actions}
          />
        </ToggleContainer>
      </>
    );
  }

  /**
   * This function gets the performance values for SINR according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */

  private getChartDataValues = (rows: SignalToInterferenceDataType[]): IDataSetsObject => {
    const data_rows = [...rows];
    sortDataByTimeStamp(data_rows);

    const datasets: IDataSet[] = [{
      name: 'snirMin',
      label: 'snir-min',
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'SINR (min)[db]',
    }, {
      name: 'snirAvg',
      label: 'snir-avg',
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'SINR (avg)[db]',
    }, {
      name: 'snirMax',
      label: 'snir-max',
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'SINR (max)[db]',
    }];

    data_rows.forEach(row => {
      row.snirMin = row.performanceData.snirMin;
      row.snirAvg = row.performanceData.snirAvg;
      row.snirMax = row.performanceData.snirMax;
      datasets.forEach(ds => {
        ds.data.push({
          x: row['timeStamp' as keyof SignalToInterferenceDataType] as string,
          y: row.performanceData[ds.name as keyof SignalToInterferenceDatabaseDataType] as string,
        });
      });
    });
    return {
      datasets: datasets,
    };
  };
}

const SignalToInterference = withRouter(connect(mapProps, mapDisp)(SignalToInterferenceComponent));
export default SignalToInterference;
