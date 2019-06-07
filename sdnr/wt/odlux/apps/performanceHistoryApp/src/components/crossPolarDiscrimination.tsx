import * as React from 'react';

import { withRouter, RouteComponentProps } from 'react-router-dom';

import { MaterialTable, ColumnType, MaterialTableCtorType, ColumnModel } from '../../../../framework/src/components/material-table';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { CrossPolarDiscriminationDataType } from '../models/crossPolarDiscriminationDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { createCrossPolarDiscrimination15minProperties, createCrossPolarDiscrimination15minActions } from '../handlers/crossPolarDiscrimination15minHandler';
import { createCrossPolarDiscrimination24hoursProperties, createCrossPolarDiscrimination24hoursActions } from '../handlers/crossPolarDiscrimination24hoursHandler';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';

const mapProps = (state: IApplicationStoreState) => ({
  crossPolarDiscrimination15minProperties: createCrossPolarDiscrimination15minProperties(state),
  crossPolarDiscrimination24hoursProperties: createCrossPolarDiscrimination24hoursProperties(state)
});

const mapDisp = (dispatcher: IDispatcher) => ({
  crossPolarDiscrimination15minActions: createCrossPolarDiscrimination15minActions(dispatcher.dispatch),
  crossPolarDiscrimination24hoursActions: createCrossPolarDiscrimination24hoursActions(dispatcher.dispatch)
});

type CrossPolarDiscriminationComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string
};

const CrossPolarDiscriminationTable = MaterialTable as MaterialTableCtorType<CrossPolarDiscriminationDataType>;

/**
 * The Component which gets the crossPolarDiscrimination data from the database based on the selected time period.
 */
class CrossPolarDiscriminationComponent extends React.Component<CrossPolarDiscriminationComponentProps>{
  render(): JSX.Element {
    const properties = this.props.selectedTimePeriod === "15min"
      ? this.props.crossPolarDiscrimination15minProperties
      : this.props.crossPolarDiscrimination24hoursProperties;
    const actions = this.props.selectedTimePeriod === "15min"
      ? this.props.crossPolarDiscrimination15minActions
      : this.props.crossPolarDiscrimination24hoursActions;

    const chartPagedData = this.getChartDataValues(properties.rows);

    const cpdColumns: ColumnModel<CrossPolarDiscriminationDataType>[] = [
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
      cpdColumns.push(addColumnLabels<CrossPolarDiscriminationDataType>(ds.name, ds.columnLabel));
    });
    return (
      <>
        {lineChart(chartPagedData)}
        <CrossPolarDiscriminationTable idProperty={"_id"} columns={cpdColumns} {...properties} {...actions} />
      </>
    );
  };

  /**
   * This function gets the performance values for CPD according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */
  private getChartDataValues = (rows: CrossPolarDiscriminationDataType[]): IDataSetsObject => {
    const _rows = [...rows];
    sortDataByTimeStamp(_rows);

    const datasets: IDataSet[] = [{
      name: "xpd-min",
      label: "xpd-min",
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "CPD (min)[db]"
    }, {
      name: "xpd-avg",
      label: "xpd-avg",
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "CPD (avg)[db]"
    }, {
      name: "xpd-max",
      label: "xpd-max",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "CPD (max)[db]"
    }];

    _rows.forEach(row => {
      datasets.forEach(ds => {
        ds.data.push({
          x: row["time-stamp"],
          y: row[ds.name as keyof CrossPolarDiscriminationDataType] as string
        });
      });
    });
    return {
      datasets: datasets
    };
  }
}
const CrossPolarDiscrimination = withRouter(connect(mapProps, mapDisp)(CrossPolarDiscriminationComponent));
export default CrossPolarDiscrimination;
