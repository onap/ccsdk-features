import * as React from 'react';

import { withRouter, RouteComponentProps } from 'react-router-dom';

import { MaterialTable, ColumnType, ColumnModel, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { TemperatureDataType } from '../models/temperatureDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { createTemperature15minProperties, createTemperature15minActions } from '../handlers/temperature15minHandler';
import { createTemperature24hoursProperties, createTemperature24hoursActions } from '../handlers/temperature24hoursHandler';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';

const mapProps = (state: IApplicationStoreState) => ({
  temperature15minProperties: createTemperature15minProperties(state),
  temperature24hoursProperties: createTemperature24hoursProperties(state)
});

const mapDisp = (dispatcher: IDispatcher) => ({
  temperature15minActions: createTemperature15minActions(dispatcher.dispatch),
  temperature24hoursActions: createTemperature24hoursActions(dispatcher.dispatch)
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
    const properties = this.props.selectedTimePeriod === "15min"
      ? this.props.temperature15minProperties
      : this.props.temperature24hoursProperties;
    const actions = this.props.selectedTimePeriod === "15min"
      ? this.props.temperature15minActions
      : this.props.temperature24hoursActions;

    const chartPagedData = this.getChartDataValues(properties.rows);
    const temperatureColumns: ColumnModel<TemperatureDataType>[] = [
      { property: "radio-signal-id", title: "Radio signal", type: ColumnType.text },
      { property: "scanner-id", title: "Scanner ID", type: ColumnType.text },
      { property: "time-stamp", title: "End Time", type: ColumnType.text, disableFilter: true },
      {
        property: "suspect-interval-flag", title: "Suspect Interval", type: ColumnType.custom, customControl: ({ rowData }) => {
          const suspectIntervalFlag = rowData["suspect-interval-flag"].toString();
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
      name: "rf-temp-min",
      label: "rf-temp-min",
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Rf Temp Min[deg C]"
    }, {
      name: "rf-temp-avg",
      label: "rf-temp-avg",
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Rf Temp Avg[deg C]"
    }, {
      name: "rf-temp-max",
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
          x: row["time-stamp"],
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
