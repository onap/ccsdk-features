import * as React from 'react';

import { withRouter, RouteComponentProps } from 'react-router-dom';

import { MaterialTable, ColumnType, ColumnModel, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { AdaptiveModulationDataType } from '../models/adaptiveModulationDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { createAdaptiveModulation15minProperties, createAdaptiveModulation15minActions } from '../handlers/adaptiveModulation15minHandler';
import { createAdaptiveModulation24hoursProperties, createAdaptiveModulation24hoursActions } from '../handlers/adaptiveModulation24hoursHandler';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';

const mapProps = (state: IApplicationStoreState) => ({
  adaptiveModulation15minProperties: createAdaptiveModulation15minProperties(state),
  adaptiveModulation24hoursProperties: createAdaptiveModulation24hoursProperties(state)
});

const mapDisp = (dispatcher: IDispatcher) => ({
  adaptiveModulation15minActions: createAdaptiveModulation15minActions(dispatcher.dispatch),
  adaptiveModulation24hoursActions: createAdaptiveModulation24hoursActions(dispatcher.dispatch)
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
    const properties = this.props.selectedTimePeriod === "15min"
      ? this.props.adaptiveModulation15minProperties
      : this.props.adaptiveModulation24hoursProperties;

    const actions = this.props.selectedTimePeriod === "15min"
      ? this.props.adaptiveModulation15minActions
      : this.props.adaptiveModulation24hoursActions;

    const chartPagedData = this.getChartDataValues(properties.rows);
    const adaptiveModulationColumns: ColumnModel<AdaptiveModulationDataType>[] = [
      { property: "radio-signal-id", title: "Radio signal", type: ColumnType.text },
      { property: "scanner-id", title: "Scanner ID", type: ColumnType.text },
      { property: "time-stamp", title: "End Time", type: ColumnType.text, disableFilter: true },
      {
        property: "suspect-interval-flag", title: "Suspect Interval", type: ColumnType.custom, customControl: ({ rowData }) => {
          const suspectIntervalFlag = rowData["suspect-interval-flag"].toString();
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
      name: "time2-states-s",
      label: "QAM2S",
      borderColor: '#62a309fc',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM2S",
    }, {
      name: "time2-states",
      label: "QAM2",
      borderColor: '#62a309fc',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM2",
    }, {
      name: "time2-states-l",
      label: "QAM2L",
      borderColor: '#62a309fc',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM2L",
    }, {
      name: "time4-states-s",
      label: "QAM4S",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM4S",
    }, {
      name: "time4-states",
      label: "QAM4",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM4",
    }, {
      name: "time4-states-l",
      label: "QAM4L",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM4L",
    }, {
      name: "time16-states-s",
      label: "QAM16S",
      borderColor: '#9b15e2',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM16S",
    }, {
      name: "time16-states",
      label: "QAM16",
      borderColor: '#9b15e2',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM16",
    }, {
      name: "time16-states-l",
      label: "QAM16L",
      borderColor: '#9b15e2',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM16L",
    }, {
      name: "time32-states-s",
      label: "QAM32S",
      borderColor: '#2704f5f0',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM32S",
    }, {
      name: "time32-states",
      label: "QAM32",
      borderColor: '#2704f5f0',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM32",
    }, {
      name: "time32-states-l",
      label: "QAM32L",
      borderColor: '#2704f5f0',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM32L",
    }, {
      name: "time64-states-s",
      label: "QAM64S",
      borderColor: '#347692',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM64S",
    }, {
      name: "time64-states",
      label: "QAM64",
      borderColor: '#347692',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM64",
    }, {
      name: "time64-states-l",
      label: "QAM64L",
      borderColor: '#347692',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM64L",
    }, {
      name: "time128-states-s",
      label: "QAM128S",
      borderColor: '#885e22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM128S",
    }, {
      name: "time128-states",
      label: "QAM128",
      borderColor: '#885e22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM128",
    }, {
      name: "time128-states-l",
      label: "QAM128L",
      borderColor: '#885e22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM128L",
    }, {
      name: "time256-states-s",
      label: "QAM256S",
      borderColor: '#de07807a',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM256S",
    }, {
      name: "time256-states",
      label: "QAM256",
      borderColor: '#de07807a',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM256",
    }, {
      name: "time256-states-l",
      label: "QAM256L",
      borderColor: '#de07807a',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM256L",
    }, {
      name: "time512-states-s",
      label: "QAM512S",
      borderColor: '#8fdaacde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM512S",
    }, {
      name: "time512-states",
      label: "QAM512",
      borderColor: '#8fdaacde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM512",
    }, {

      name: "time512-states-l",
      label: "QAM512L",
      borderColor: '#8fdaacde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM512L",
    }, {

      name: "time1024-states-s",
      label: "QAM1024S",
      borderColor: '#435b22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM1024S",
    }, {

      name: "time1024-states",
      label: "QAM1024",
      borderColor: '#435b22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM1024",
    }, {

      name: "time1024-states-l",
      label: "QAM1024L",
      borderColor: '#435b22',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM1024L",
    }, {
      name: "time2048-states-s",
      label: "QAM2048S",
      borderColor: '#e87a5b',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM2048S",
    }, {
      name: "time2048-states",
      label: "QAM2048",
      borderColor: '#e87a5b',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM2048",
    }, {
      name: "time2048-states-l",
      label: "QAM2048L",
      borderColor: '#e87a5b',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM2048L",
    }, {
      name: "time4096-states-s",
      label: "QAM4096S",
      borderColor: '#5be878',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM4096S",
    }, {
      name: "time4096-states",
      label: "QAM4096",
      borderColor: '#5be878',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM4096",
    }, {
      name: "time4096-states-l",
      label: "QAM4096L",
      borderColor: '#5be878',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM4096L",
    }, {
      name: "time8192-states-s",
      label: "QAM8192s",
      borderColor: '#cb5be8',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM8192S",
    }, {
      name: "time8192-states",
      label: "QAM8192",
      borderColor: '#cb5be8',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "QAM8192",
    }, {
      name: "time8192-states-l",
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
          x: row["time-stamp"],
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
