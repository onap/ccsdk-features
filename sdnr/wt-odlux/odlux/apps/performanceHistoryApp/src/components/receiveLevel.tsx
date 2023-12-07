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
import { createReceiveLevelActions, createReceiveLevelProperties } from '../handlers/receiveLevelHandler';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { ReceiveLevelDatabaseDataType, ReceiveLevelDataType } from '../models/receiveLevelDataType';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';
import ToggleContainer from './toggleContainer';

const mapProps = (state: IApplicationStoreState) => ({
  receiveLevelProperties: createReceiveLevelProperties(state),
  currentView: state.performanceHistory.subViews.receiveLevel.subView,
  isFilterVisible: state.performanceHistory.subViews.receiveLevel.isFilterVisible,
  existingFilter: state.performanceHistory.receiveLevel.filter,
});

const mapDisp = (dispatcher: IDispatcher) => ({
  receiveLevelActions: createReceiveLevelActions(dispatcher.dispatch),
  setSubView: (value: 'chart' | 'table') => dispatcher.dispatch(new SetSubViewAction('receiveLevel', value)),
  toggleFilterButton: (value: boolean) => { dispatcher.dispatch(new SetFilterVisibility('receiveLevel', value)); },
});

type ReceiveLevelComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string;
};

const ReceiveLevelTable = MaterialTable as MaterialTableCtorType<ReceiveLevelDataType>;

/**
 * The Component which gets the receiveLevel data from the database based on the selected time period.
 */
class ReceiveLevelComponent extends React.Component<ReceiveLevelComponentProps> {
  onToggleFilterButton = () => {
    this.props.toggleFilterButton(!this.props.isFilterVisible);
  };


  onChange = (value: 'chart' | 'table') => {
    this.props.setSubView(value);
  };

  onFilterChanged = (property: string, filterTerm: string) => {
    this.props.receiveLevelActions.onFilterChanged(property, filterTerm);
    if (!this.props.receiveLevelProperties.showFilter)
      this.props.receiveLevelActions.onToggleFilter(false);
  };

  render(): JSX.Element {
    const properties = this.props.receiveLevelProperties;
    const actions = this.props.receiveLevelActions;

    const chartPagedData = this.getChartDataValues(properties.rows);
    const receiveLevelColumns: ColumnModel<ReceiveLevelDataType>[] = [
      { property: 'radioSignalId', title: 'Radio signal', type: ColumnType.text },
      { property: 'scannerId', title: 'Scanner ID', type: ColumnType.text },
      { property: 'timeStamp', title: 'End Time', type: ColumnType.text },
      {
        property: 'suspectIntervalFlag', title: 'Suspect Interval', type: ColumnType.boolean,
      },
    ];

    chartPagedData.datasets.forEach(ds => {
      receiveLevelColumns.push(addColumnLabels<ReceiveLevelDataType>(ds.name, ds.columnLabel));
    });

    return (
      <>
        <ToggleContainer onToggleFilterButton={this.onToggleFilterButton} showFilter={this.props.isFilterVisible} existingFilter={this.props.receiveLevelProperties.filter} onFilterChanged={this.onFilterChanged} selectedValue={this.props.currentView} onChange={this.onChange}>
          {lineChart(chartPagedData)}
          <ReceiveLevelTable stickyHeader idProperty={'_id'} tableId="receive-level-table" columns={receiveLevelColumns} {...properties} {...actions} />
        </ToggleContainer>
      </>
    );
  }

  /**
   * This function gets the performance values for ReceiveLevel according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */
  private getChartDataValues = (rows: ReceiveLevelDataType[]): IDataSetsObject => {
    const data_rows = [...rows];
    sortDataByTimeStamp(data_rows);

    const datasets: IDataSet[] = [{
      name: 'rxLevelMin',
      label: 'rx-level-min',
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'Rx min',
    }, {
      name: 'rxLevelAvg',
      label: 'rx-level-avg',
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'Rx avg',
    }, {
      name: 'rxLevelMax',
      label: 'rx-level-max',
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'Rx max',
    }];

    data_rows.forEach(row => {
      row.rxLevelMin = row.performanceData.rxLevelMin;
      row.rxLevelAvg = row.performanceData.rxLevelAvg;
      row.rxLevelMax = row.performanceData.rxLevelMax;
      datasets.forEach(ds => {
        ds.data.push({
          x: row['timeStamp' as keyof ReceiveLevelDataType] as string,
          y: row.performanceData[ds.name as keyof ReceiveLevelDatabaseDataType] as string,
        });
      });
    });
    return {
      datasets: datasets,
    };
  };
}

const ReceiveLevel = withRouter(connect(mapProps, mapDisp)(ReceiveLevelComponent));
export default ReceiveLevel;
