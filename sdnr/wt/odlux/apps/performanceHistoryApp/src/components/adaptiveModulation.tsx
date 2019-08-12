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

import { AdaptiveModulationDataType } from '../models/adaptiveModulationDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { createAdaptiveModulationProperties, createAdaptiveModulationActions } from '../handlers/adaptiveModulationHandler';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';

const mapProps = (state: IApplicationStoreState) => ({
  adaptiveModulationProperties: createAdaptiveModulationProperties(state),
});

const mapDisp = (dispatcher: IDispatcher) => ({
  adaptiveModulationActions: createAdaptiveModulationActions(dispatcher.dispatch),
});

type AdaptiveModulationComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string
};

const AdaptiveModulationTable = MaterialTable as MaterialTableCtorType<AdaptiveModulationDataType>;

/**
 * The Component which gets the adaptiveModulation data from the database based on the selected time period.
 */
class AdaptiveModulationComponent extends React.Component<AdaptiveModulationComponentProps>{
  render(): JSX.Element {
    const properties = this.props.adaptiveModulationProperties;
    const actions = this.props.adaptiveModulationActions;

    const chartPagedData = this.getChartDataValues(properties.rows);
    const adaptiveModulationColumns: ColumnModel<AdaptiveModulationDataType>[] = [
      { property: "radioSignalId", title: "Radio signal", type: ColumnType.text },
      { property: "scannerId", title: "Scanner ID", type: ColumnType.text },
      { property: "utcTimeStamp", title: "End Time", type: ColumnType.text, disableFilter: true },
      {
        property: "suspectIntervalFlag", title: "Suspect Interval", type: ColumnType.custom, customControl: ({ rowData }) => {
          const suspectIntervalFlag = rowData["suspectIntervalFlag"].toString();
          return <div >{suspectIntervalFlag} </div>
        }
      }];

    chartPagedData.datasets.forEach(ds => {
      adaptiveModulationColumns.push(addColumnLabels<AdaptiveModulationDataType>(ds.name, ds.columnLabel));
    });

    return (
      <>
        {lineChart(chartPagedData)}
        <AdaptiveModulationTable idProperty={"_id"} columns={adaptiveModulationColumns} {...properties} {...actions} />
      </>
    );
  };

  /**
   * This function gets the performance values for Adaptive modulation according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */

  private getChartDataValues = (rows: AdaptiveModulationDataType[]): IDataSetsObject => {
    const _rows = [...rows];
    sortDataByTimeStamp(_rows);

    const datasets: IDataSet[] = [{
      name: "time2StatesS",
      label: "QAM2S",
      borderColor: '#62a309fc',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM2S",
    }, {
      name: "time2States",
      label: "QAM2",
      borderColor: '#62a309fc',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM2",
    }, {
      name: "time2StatesL",
      label: "QAM2L",
      borderColor: '#62a309fc',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM2L",
    }, {
      name: "time4StatesS",
      label: "QAM4S",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM4S",
    }, {
      name: "time4States",
      label: "QAM4",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM4",
    }, {
      name: "time4StatesL",
      label: "QAM4L",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM4L",
    }, {
      name: "time16StatesS",
      label: "QAM16S",
      borderColor: '#9b15e2',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM16S",
    }, {
      name: "time16States",
      label: "QAM16",
      borderColor: '#9b15e2',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM16",
    }, {
      name: "time16StatesL",
      label: "QAM16L",
      borderColor: '#9b15e2',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM16L",
    }, {
      name: "time32StatesS",
      label: "QAM32S",
      borderColor: '#2704f5f0',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM32S",
    }, {
      name: "time32States",
      label: "QAM32",
      borderColor: '#2704f5f0',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM32",
    }, {
      name: "time32StatesL",
      label: "QAM32L",
      borderColor: '#2704f5f0',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM32L",
    }, {
      name: "time64StatesS",
      label: "QAM64S",
      borderColor: '#347692',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM64S",
    }, {
      name: "time64States",
      label: "QAM64",
      borderColor: '#347692',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM64",
    }, {
      name: "time64StatesL",
      label: "QAM64L",
      borderColor: '#347692',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM64L",
    }, {
      name: "time128StatesS",
      label: "QAM128S",
      borderColor: '#885e22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM128S",
    }, {
      name: "time128States",
      label: "QAM128",
      borderColor: '#885e22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM128",
    }, {
      name: "time128StatesL",
      label: "QAM128L",
      borderColor: '#885e22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM128L",
    }, {
      name: "time256StatesS",
      label: "QAM256S",
      borderColor: '#de07807a',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM256S",
    }, {
      name: "time256States",
      label: "QAM256",
      borderColor: '#de07807a',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM256",
    }, {
      name: "time256StatesL",
      label: "QAM256L",
      borderColor: '#de07807a',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM256L",
    }, {
      name: "time512StatesS",
      label: "QAM512S",
      borderColor: '#8fdaacde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM512S",
    }, {
      name: "time512States",
      label: "QAM512",
      borderColor: '#8fdaacde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM512",
    }, {

      name: "time512StatesL",
      label: "QAM512L",
      borderColor: '#8fdaacde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM512L",
    }, {

      name: "time1024StatesS",
      label: "QAM1024S",
      borderColor: '#435b22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM1024S",
    }, {

      name: "time1024States",
      label: "QAM1024",
      borderColor: '#435b22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM1024",
    }, {

      name: "time1024StatesL",
      label: "QAM1024L",
      borderColor: '#435b22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM1024L",
    }, {
      name: "time2048StatesS",
      label: "QAM2048S",
      borderColor: '#e87a5b',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM2048S",
    }, {
      name: "time2048States",
      label: "QAM2048",
      borderColor: '#e87a5b',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM2048",
    }, {
      name: "time2048StatesL",
      label: "QAM2048L",
      borderColor: '#e87a5b',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM2048L",
    }, {
      name: "time4096StatesS",
      label: "QAM4096S",
      borderColor: '#5be878',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM4096S",
    }, {
      name: "time4096States",
      label: "QAM4096",
      borderColor: '#5be878',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM4096",
    }, {
      name: "time4096StatesL",
      label: "QAM4096L",
      borderColor: '#5be878',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM4096L",
    }, {
      name: "time8192StatesS",
      label: "QAM8192s",
      borderColor: '#cb5be8',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM8192S",
    }, {
      name: "time8192States",
      label: "QAM8192",
      borderColor: '#cb5be8',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM8192",
    }, {
      name: "time8192StatesL",
      label: "QAM8192L",
      borderColor: '#cb5be8',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM8192L",
    }
    ];

    _rows.forEach(row => {
      datasets.forEach(ds => {
        ds.data.push({
          x: row["utcTimeStamp" as keyof AdaptiveModulationDataType] as string,
          y: row[ds.name as keyof AdaptiveModulationDataType] as string
        });
      });
    });

    return {
      datasets: datasets
    };
  }
}
const AdaptiveModulation = withRouter(connect(mapProps, mapDisp)(AdaptiveModulationComponent));
export default AdaptiveModulation;
