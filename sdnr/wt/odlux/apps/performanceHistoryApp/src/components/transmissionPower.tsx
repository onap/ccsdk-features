import * as React from 'react';

import { withRouter, RouteComponentProps } from 'react-router-dom';

import { MaterialTable, ColumnType, ColumnModel, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { TransmissionPowerDataType } from '../models/transmissionPowerDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { createTransmissionPower15minProperties, createTransmissionPower15minActions } from '../handlers/transmissionPower15minHandler';
import { createTransmissionPower24hoursProperties, createTransmissionPower24hoursActions } from '../handlers/transmissionPower24hoursHandler';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';

const mapProps = (state: IApplicationStoreState) => ({
  transmissionPower15minProperties: createTransmissionPower15minProperties(state),
  transmissionPower24hoursProperties: createTransmissionPower24hoursProperties(state)
});

const mapDisp = (dispatcher: IDispatcher) => ({
  transmissionPower15minActions: createTransmissionPower15minActions(dispatcher.dispatch),
  transmissionPower24hoursActions: createTransmissionPower24hoursActions(dispatcher.dispatch)
});

type TransmissionPowerComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string
}

const TransmissionPowerTable = MaterialTable as MaterialTableCtorType<TransmissionPowerDataType>;

/**
 * The Component which gets the transmission power data from the database based on the selected time period.
 */
class TransmissionPowerComponent extends React.Component<TransmissionPowerComponentProps>{
  render(): JSX.Element {
    const properties = this.props.selectedTimePeriod === "15min"
      ? this.props.transmissionPower15minProperties
      : this.props.transmissionPower24hoursProperties;
    const actions = this.props.selectedTimePeriod === "15min"
      ? this.props.transmissionPower15minActions
      : this.props.transmissionPower24hoursActions;

    const chartPagedData = this.getChartDataValues(properties.rows);

    const transmissionColumns: ColumnModel<TransmissionPowerDataType>[] = [
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
      transmissionColumns.push(addColumnLabels<TransmissionPowerDataType>(ds.name, ds.columnLabel));
    });

    return (
      <>
        {lineChart(chartPagedData)}
        <TransmissionPowerTable idProperty={"_id"} columns={transmissionColumns} {...properties} {...actions} />
      </>
    );
  };

  /**
   * This function gets the performance values for TransmissionPower according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */

  private getChartDataValues = (rows: TransmissionPowerDataType[]): IDataSetsObject => {
    const _rows = [...rows];
    sortDataByTimeStamp(_rows);

    const datasets: IDataSet[] = [{
      name: "tx-level-min",
      label: "tx-level-min",
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Tx min"
    }, {
      name: "tx-level-avg",
      label: "tx-level-avg",
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Tx avg"
    }, {
      name: "tx-level-max",
      label: "tx-level-max",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Tx max"
    }];

    _rows.forEach(row => {
      datasets.forEach(ds => {
        ds.data.push({
          x: row["time-stamp"],
          y: row[ds.name as keyof TransmissionPowerDataType] as string
        });
      });
    });
    return {
      datasets: datasets
    };
  }
}

const TransmissionPower = withRouter(connect(mapProps, mapDisp)(TransmissionPowerComponent));
export default TransmissionPower;
