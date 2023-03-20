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
import { createTemperatureActions, createTemperatureProperties } from '../handlers/temperatureHandler';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { TemperatureDatabaseDataType, TemperatureDataType } from '../models/temperatureDataType';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';
import ToggleContainer from './toggleContainer';

const mapProps = (state: IApplicationStoreState) => ({
  temperatureProperties: createTemperatureProperties(state),
  currentView: state.performanceHistory.subViews.temperatur.subView,
  isFilterVisible: state.performanceHistory.subViews.temperatur.isFilterVisible,
  existingFilter: state.performanceHistory.temperature.filter,
});

const mapDisp = (dispatcher: IDispatcher) => ({
  temperatureActions: createTemperatureActions(dispatcher.dispatch),
  setSubView: (value: 'chart' | 'table') => dispatcher.dispatch(new SetSubViewAction('Temp', value)),
  toggleFilterButton: (value: boolean) => { dispatcher.dispatch(new SetFilterVisibility('Temp', value)); },

});

type TemperatureComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string;
};

const TemperatureTable = MaterialTable as MaterialTableCtorType<TemperatureDataType>;

/**
 * The Component which gets the temperature data from the database based on the selected time period.
 */
class TemperatureComponent extends React.Component<TemperatureComponentProps> {
  onToggleFilterButton = () => {
    this.props.toggleFilterButton(!this.props.isFilterVisible);
  };


  onChange = (value: 'chart' | 'table') => {
    this.props.setSubView(value);
  };

  onFilterChanged = (property: string, filterTerm: string) => {
    this.props.temperatureActions.onFilterChanged(property, filterTerm);
    if (!this.props.temperatureProperties.showFilter)
      this.props.temperatureActions.onToggleFilter(false);
  };

  render(): JSX.Element {
    const properties = this.props.temperatureProperties;
    const actions = this.props.temperatureActions;

    const chartPagedData = this.getChartDataValues(properties.rows);
    const temperatureColumns: ColumnModel<TemperatureDataType>[] = [
      { property: 'radioSignalId', title: 'Radio signal', type: ColumnType.text },
      { property: 'scannerId', title: 'Scanner ID', type: ColumnType.text },
      { property: 'timeStamp', title: 'End Time', type: ColumnType.text },
      {
        property: 'suspectIntervalFlag', title: 'Suspect Interval', type: ColumnType.boolean,
      },
    ];

    chartPagedData.datasets.forEach(ds => {
      temperatureColumns.push(addColumnLabels<TemperatureDataType>(ds.name, ds.columnLabel));
    });
    return (
      <>

        <ToggleContainer onToggleFilterButton={this.onToggleFilterButton} showFilter={this.props.isFilterVisible} existingFilter={this.props.temperatureProperties.filter} onFilterChanged={this.onFilterChanged} selectedValue={this.props.currentView} onChange={this.onChange}>
          {lineChart(chartPagedData)}
          <TemperatureTable stickyHeader idProperty={'_id'} tableId="temperature-table" columns={temperatureColumns} {...properties} {...actions} />
        </ToggleContainer>
      </>
    );
  }

  /**
   * This function gets the performance values for Temperature according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */

  private getChartDataValues = (rows: TemperatureDataType[]): IDataSetsObject => {
    const data_rows = [...rows];
    sortDataByTimeStamp(data_rows);

    const datasets: IDataSet[] = [{
      name: 'rfTempMin',
      label: 'rf-temp-min',
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'Rf Temp Min[deg C]',
    }, {
      name: 'rfTempAvg',
      label: 'rf-temp-avg',
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'Rf Temp Avg[deg C]',
    }, {
      name: 'rfTempMax',
      label: 'rf-temp-max',
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'Rf Temp Max[deg C]',
    }];

    data_rows.forEach(row => {
      row.rfTempMin = row.performanceData.rfTempMin;
      row.rfTempAvg = row.performanceData.rfTempAvg;
      row.rfTempMax = row.performanceData.rfTempMax;
      datasets.forEach(ds => {
        ds.data.push({
          x: row['timeStamp' as keyof TemperatureDataType] as string,
          y: row.performanceData[ds.name as keyof TemperatureDatabaseDataType] as string,
        });
      });
    });
    return {
      datasets: datasets,
    };
  };
}

const Temperature = withRouter(connect(mapProps, mapDisp)(TemperatureComponent));
export default Temperature;
