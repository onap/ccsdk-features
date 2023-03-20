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
import { createAdaptiveModulationActions, createAdaptiveModulationProperties } from '../handlers/adaptiveModulationHandler';
import { AdaptiveModulationDatabaseDataType, AdaptiveModulationDataType } from '../models/adaptiveModulationDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';
import ToggleContainer from './toggleContainer';

const mapProps = (state: IApplicationStoreState) => ({
  adaptiveModulationProperties: createAdaptiveModulationProperties(state),
  currentView: state.performanceHistory.subViews.adaptiveModulation.subView,
  isFilterVisible: state.performanceHistory.subViews.adaptiveModulation.isFilterVisible,
  existingFilter: state.performanceHistory.adaptiveModulation.filter,
});

const mapDisp = (dispatcher: IDispatcher) => ({
  adaptiveModulationActions: createAdaptiveModulationActions(dispatcher.dispatch),
  setSubView: (value: 'chart' | 'table') => dispatcher.dispatch(new SetSubViewAction('adaptiveModulation', value)),
  toggleFilterButton: (value: boolean) => { dispatcher.dispatch(new SetFilterVisibility('adaptiveModulation', value)); },
});

type AdaptiveModulationComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string;
};

const AdaptiveModulationTable = MaterialTable as MaterialTableCtorType<AdaptiveModulationDataType>;

/**
 * The Component which gets the adaptiveModulation data from the database based on the selected time period.
 */
class AdaptiveModulationComponent extends React.Component<AdaptiveModulationComponentProps> {
  onToggleFilterButton = () => {
    this.props.toggleFilterButton(!this.props.isFilterVisible);
  };

  onChange = (value: 'chart' | 'table') => {
    this.props.setSubView(value);
  };

  onFilterChanged = (property: string, filterTerm: string) => {
    this.props.adaptiveModulationActions.onFilterChanged(property, filterTerm);
    if (!this.props.adaptiveModulationProperties.showFilter)
      this.props.adaptiveModulationActions.onToggleFilter(false);
  };

  render(): JSX.Element {
    const properties = this.props.adaptiveModulationProperties;
    const actions = this.props.adaptiveModulationActions;

    const chartPagedData = this.getChartDataValues(properties.rows);
    const adaptiveModulationColumns: ColumnModel<AdaptiveModulationDataType>[] = [
      { property: 'radioSignalId', title: 'Radio signal', type: ColumnType.text },
      { property: 'scannerId', title: 'Scanner ID', type: ColumnType.text },
      { property: 'timeStamp', title: 'End Time', type: ColumnType.text },
      {
        property: 'suspectIntervalFlag', title: 'Suspect Interval', type: ColumnType.boolean,
      }];

    chartPagedData.datasets.forEach(ds => {
      adaptiveModulationColumns.push(addColumnLabels<AdaptiveModulationDataType>(ds.name, ds.columnLabel));
    });

    return (
      <>
        <ToggleContainer onToggleFilterButton={this.onToggleFilterButton} showFilter={this.props.isFilterVisible}
          existingFilter={this.props.adaptiveModulationProperties.filter} onFilterChanged={this.onFilterChanged} selectedValue={this.props.currentView} onChange={this.onChange}>
          {lineChart(chartPagedData)}
          <AdaptiveModulationTable stickyHeader idProperty={'_id'} tableId="adaptive-modulation-table" columns={adaptiveModulationColumns} {...properties} {...actions} />
        </ToggleContainer>
      </>
    );
  }

  /**
   * This function gets the performance values for Adaptive modulation according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */

  private getChartDataValues = (rows: AdaptiveModulationDataType[]): IDataSetsObject => {
    const data_rows = [...rows];
    sortDataByTimeStamp(data_rows);

    const datasets: IDataSet[] = [{
      name: 'time2StatesS',
      label: 'QAM2S',
      borderColor: '#62a309fc',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM2S',
    }, {
      name: 'time2States',
      label: 'QAM2',
      borderColor: '#62a309fc',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM2',
    }, {
      name: 'time2StatesL',
      label: 'QAM2L',
      borderColor: '#62a309fc',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM2L',
    }, {
      name: 'time4StatesS',
      label: 'QAM4S',
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM4S',
    }, {
      name: 'time4States',
      label: 'QAM4',
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM4',
    }, {
      name: 'time4StatesL',
      label: 'QAM4L',
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM4L',
    }, {
      name: 'time16StatesS',
      label: 'QAM16S',
      borderColor: '#9b15e2',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM16S',
    }, {
      name: 'time16States',
      label: 'QAM16',
      borderColor: '#9b15e2',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM16',
    }, {
      name: 'time16StatesL',
      label: 'QAM16L',
      borderColor: '#9b15e2',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM16L',
    }, {
      name: 'time32StatesS',
      label: 'QAM32S',
      borderColor: '#2704f5f0',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM32S',
    }, {
      name: 'time32States',
      label: 'QAM32',
      borderColor: '#2704f5f0',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM32',
    }, {
      name: 'time32StatesL',
      label: 'QAM32L',
      borderColor: '#2704f5f0',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM32L',
    }, {
      name: 'time64StatesS',
      label: 'QAM64S',
      borderColor: '#347692',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM64S',
    }, {
      name: 'time64States',
      label: 'QAM64',
      borderColor: '#347692',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM64',
    }, {
      name: 'time64StatesL',
      label: 'QAM64L',
      borderColor: '#347692',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM64L',
    }, {
      name: 'time128StatesS',
      label: 'QAM128S',
      borderColor: '#885e22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM128S',
    }, {
      name: 'time128States',
      label: 'QAM128',
      borderColor: '#885e22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM128',
    }, {
      name: 'time128StatesL',
      label: 'QAM128L',
      borderColor: '#885e22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM128L',
    }, {
      name: 'time256StatesS',
      label: 'QAM256S',
      borderColor: '#de07807a',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM256S',
    }, {
      name: 'time256States',
      label: 'QAM256',
      borderColor: '#de07807a',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM256',
    }, {
      name: 'time256StatesL',
      label: 'QAM256L',
      borderColor: '#de07807a',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM256L',
    }, {
      name: 'time512StatesS',
      label: 'QAM512S',
      borderColor: '#8fdaacde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM512S',
    }, {
      name: 'time512States',
      label: 'QAM512',
      borderColor: '#8fdaacde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM512',
    }, {

      name: 'time512StatesL',
      label: 'QAM512L',
      borderColor: '#8fdaacde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM512L',
    }, {

      name: 'time1024StatesS',
      label: 'QAM1024S',
      borderColor: '#435b22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM1024S',
    }, {

      name: 'time1024States',
      label: 'QAM1024',
      borderColor: '#435b22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM1024',
    }, {

      name: 'time1024StatesL',
      label: 'QAM1024L',
      borderColor: '#435b22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM1024L',
    }, {
      name: 'time2048StatesS',
      label: 'QAM2048S',
      borderColor: '#e87a5b',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM2048S',
    }, {
      name: 'time2048States',
      label: 'QAM2048',
      borderColor: '#e87a5b',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM2048',
    }, {
      name: 'time2048StatesL',
      label: 'QAM2048L',
      borderColor: '#e87a5b',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM2048L',
    }, {
      name: 'time4096StatesS',
      label: 'QAM4096S',
      borderColor: '#5be878',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM4096S',
    }, {
      name: 'time4096States',
      label: 'QAM4096',
      borderColor: '#5be878',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM4096',
    }, {
      name: 'time4096StatesL',
      label: 'QAM4096L',
      borderColor: '#5be878',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM4096L',
    }, {
      name: 'time8192StatesS',
      label: 'QAM8192s',
      borderColor: '#cb5be8',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM8192S',
    }, {
      name: 'time8192States',
      label: 'QAM8192',
      borderColor: '#cb5be8',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM8192',
    }, {
      name: 'time8192StatesL',
      label: 'QAM8192L',
      borderColor: '#cb5be8',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'QAM8192L',
    },
    ];

    data_rows.forEach(row => {
      row.time2StatesS = row.performanceData.time2StatesS;
      row.time2States = row.performanceData.time2States;
      row.time2StatesL = row.performanceData.time2StatesL;
      row.time4StatesS = row.performanceData.time4StatesS;
      row.time4States = row.performanceData.time4States;
      row.time4StatesL = row.performanceData.time4StatesL;
      row.time16StatesS = row.performanceData.time16StatesS;
      row.time16States = row.performanceData.time16States;
      row.time16StatesL = row.performanceData.time16StatesL;
      row.time32StatesS = row.performanceData.time32StatesS;
      row.time32States = row.performanceData.time32States;
      row.time32StatesL = row.performanceData.time32StatesL;
      row.time64StatesS = row.performanceData.time64StatesS;
      row.time64States = row.performanceData.time64States;
      row.time64StatesL = row.performanceData.time64StatesL;
      row.time128StatesS = row.performanceData.time128StatesS;
      row.time128States = row.performanceData.time128States;
      row.time128StatesL = row.performanceData.time128StatesL;
      row.time256StatesS = row.performanceData.time256StatesS;
      row.time256States = row.performanceData.time256States;
      row.time256StatesL = row.performanceData.time256StatesL;
      row.time512StatesS = row.performanceData.time512StatesS;
      row.time512States = row.performanceData.time512States;
      row.time512StatesL = row.performanceData.time512StatesL;
      row.time1024StatesS = row.performanceData.time1024StatesS;
      row.time1024States = row.performanceData.time1024States;
      row.time1024StatesL = row.performanceData.time1024StatesL;
      row.time2048StatesS = row.performanceData.time2048StatesS;
      row.time2048States = row.performanceData.time2048States;
      row.time2048StatesL = row.performanceData.time2048StatesL;
      row.time4096StatesS = row.performanceData.time4096StatesS;
      row.time4096States = row.performanceData.time4096States;
      row.time4096StatesL = row.performanceData.time4096StatesL;
      row.time8192StatesS = row.performanceData.time8192StatesS;
      row.time8192States = row.performanceData.time8192States;
      row.time8192StatesL = row.performanceData.time8192StatesL;
      datasets.forEach(ds => {
        ds.data.push({
          x: row['timeStamp' as keyof AdaptiveModulationDataType] as string,
          y: row.performanceData[ds.name as keyof AdaptiveModulationDatabaseDataType] as string,
        });
      });
    });

    return {
      datasets: datasets,
    };
  };
}
const AdaptiveModulation = withRouter(connect(mapProps, mapDisp)(AdaptiveModulationComponent));
export default AdaptiveModulation;
