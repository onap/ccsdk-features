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

import { MaterialTable, ColumnType, ColumnModel, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { TemperatureDataType } from '../models/temperatureDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { createTemperatureProperties, createTemperatureActions } from '../handlers/temperatureHandler';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';

const mapProps = (state: IApplicationStoreState) => ({
  temperatureProperties: createTemperatureProperties(state),
});

const mapDisp = (dispatcher: IDispatcher) => ({
  temperatureActions: createTemperatureActions(dispatcher.dispatch),
});

type TemperatureComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string
};

const TemperatureTable = MaterialTable as MaterialTableCtorType<TemperatureDataType>;

/**
 * The Component which gets the temperature data from the database based on the selected time period.
 */
class TemperatureComponent extends React.Component<TemperatureComponentProps>{
  render(): JSX.Element {
    const properties = this.props.temperatureProperties;
    const actions = this.props.temperatureActions;

    const chartPagedData = this.getChartDataValues(properties.rows);
    const temperatureColumns: ColumnModel<TemperatureDataType>[] = [
      { property: "radioSignalId", title: "Radio signal", type: ColumnType.text },
      { property: "scannerId", title: "Scanner ID", type: ColumnType.text },
      { property: "utcTimeStamp", title: "End Time", type: ColumnType.text, disableFilter: true },
      {
        property: "suspectIntervalFlag", title: "Suspect Interval", type: ColumnType.custom, customControl: ({ rowData }) => {
          const suspectIntervalFlag = rowData["suspectIntervalFlag"].toString();
          return <div >{suspectIntervalFlag} </div>
        }
      }
    ];

    chartPagedData.datasets.forEach(ds => {
      temperatureColumns.push(addColumnLabels<TemperatureDataType>(ds.name, ds.columnLabel));
    });
    return (
      <>
        {lineChart(chartPagedData)}
        <TemperatureTable idProperty={"_id"} columns={temperatureColumns} {...properties} {...actions} />
      </>
    );
  };

  /**
   * This function gets the performance values for Temperature according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */

  private getChartDataValues = (rows: TemperatureDataType[]): IDataSetsObject => {
    const _rows = [...rows];
    sortDataByTimeStamp(_rows);

    const datasets: IDataSet[] = [{
      name: "rfTempMin",
      label: "rf-temp-min",
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Rf Temp Min[deg C]"
    }, {
      name: "rfTempAvg",
      label: "rf-temp-avg",
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Rf Temp Avg[deg C]"
    }, {
      name: "rfTempMax",
      label: "rf-temp-max",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Rf Temp Max[deg C]"
    }];

    _rows.forEach(row => {
      datasets.forEach(ds => {
        ds.data.push({
          x: row["utcTimeStamp" as keyof TemperatureDataType] as string,
          y: row[ds.name as keyof TemperatureDataType] as string
        });
      });
    });
    return {
      datasets: datasets
    };
  }
}

const Temperature = withRouter(connect(mapProps, mapDisp)(TemperatureComponent));
export default Temperature;
