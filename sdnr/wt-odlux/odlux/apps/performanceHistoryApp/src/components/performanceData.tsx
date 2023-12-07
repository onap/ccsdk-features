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
import { createPerformanceDataActions, createPerformanceDataProperties } from '../handlers/performanceDataHandler';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { PerformanceDatabaseDataType, PerformanceDataType } from '../models/performanceDataType';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';
import ToggleContainer from './toggleContainer';

const mapProps = (state: IApplicationStoreState) => ({
  performanceDataProperties: createPerformanceDataProperties(state),
  currentView: state.performanceHistory.subViews.performanceData.subView,
  isFilterVisible: state.performanceHistory.subViews.performanceData.isFilterVisible,
  existingFilter: state.performanceHistory.performanceData.filter,
});

const mapDisp = (dispatcher: IDispatcher) => ({
  performanceDataActions: createPerformanceDataActions(dispatcher.dispatch),
  setSubView: (value: 'chart' | 'table') => dispatcher.dispatch(new SetSubViewAction('performanceData', value)),
  toggleFilterButton: (value: boolean) => { dispatcher.dispatch(new SetFilterVisibility('performanceData', value)); },
});

type PerformanceDataComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string;
};

const PerformanceDataTable = MaterialTable as MaterialTableCtorType<PerformanceDataType>;

/**
 * The Component which gets the performance data from the database based on the selected time period.
 */
class PerformanceDataComponent extends React.Component<PerformanceDataComponentProps> {
  onToggleFilterButton = () => {
    this.props.toggleFilterButton(!this.props.isFilterVisible);
  };

  onFilterChanged = (property: string, filterTerm: string) => {
    this.props.performanceDataActions.onFilterChanged(property, filterTerm);
    if (!this.props.performanceDataProperties.showFilter)
      this.props.performanceDataActions.onToggleFilter(false);
  };

  render(): JSX.Element {
    const properties = this.props.performanceDataProperties;
    const actions = this.props.performanceDataActions;

    const chartPagedData = this.getChartDataValues(properties.rows);
    const performanceColumns: ColumnModel<PerformanceDataType>[] = [
      { property: 'radioSignalId', title: 'Radio signal', type: ColumnType.text },
      { property: 'scannerId', title: 'Scanner ID', type: ColumnType.text },
      { property: 'timeStamp', title: 'End Time', type: ColumnType.text },
      {
        property: 'suspectIntervalFlag', title: 'Suspect Interval', type: ColumnType.boolean,
      },
    ];

    chartPagedData.datasets.forEach(ds => {
      performanceColumns.push(addColumnLabels<PerformanceDataType>(ds.name, ds.columnLabel));
    });
    return (
      <>
        <ToggleContainer onToggleFilterButton={() => this.props.toggleFilterButton(!this.props.isFilterVisible)}
          existingFilter={this.props.existingFilter} onFilterChanged={this.onFilterChanged} selectedValue={this.props.currentView} showFilter={this.props.isFilterVisible} onChange={this.props.setSubView}>
          {lineChart(chartPagedData)}
          <PerformanceDataTable stickyHeader idProperty={'_id'} tableId="performance-data-table" columns={performanceColumns} {...properties} {...actions} />
        </ToggleContainer>
      </>
    );
  }

  /**
   * This function gets the performance values for PerformanceData according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */
  private getChartDataValues = (rows: PerformanceDataType[]): IDataSetsObject => {
    const data_rows = [...rows];
    sortDataByTimeStamp(data_rows);

    const datasets: IDataSet[] = [{
      name: 'es',
      label: 'es',
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'ES',
    }, {
      name: 'ses',
      label: 'ses',
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'SES',
    }, {
      name: 'unavailability',
      label: 'unavailability',
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'Unavailability',
    }];

    data_rows.forEach(row => {
      row.es = row.performanceData.es;
      row.ses = row.performanceData.ses;
      row.unavailability = row.performanceData.unavailability;
      datasets.forEach(ds => {
        ds.data.push({
          x: row['timeStamp' as keyof PerformanceDataType] as string,
          y: row.performanceData[ds.name as keyof PerformanceDatabaseDataType] as string,
        });
      });
    });

    return {
      datasets: datasets,
    };
  };
}

const PerformanceData = withRouter(connect(mapProps, mapDisp)(PerformanceDataComponent));
export default PerformanceData;
