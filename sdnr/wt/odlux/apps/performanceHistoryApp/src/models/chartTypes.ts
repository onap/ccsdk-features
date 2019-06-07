export interface IData  {
  x: string;
  y: string;
}
  
/**
 * Structure of chartjs dataset with the chart properties.
 */
  export interface IDataSet {
    name: string,
    label: string,
    lineTension: 0,
    bezierCurve: boolean;
    fill: boolean,
    borderColor: string,
    data: IData[],
    columnLabel:string
  }

/**
 * Structure of chartjs dataset which is sent to the chart.
 */
  export interface IDataSetsObject  {
    datasets: IDataSet[]
  }

/**
 * Interface used by chart for sorting on time-stamp
 */
  export interface ITimeStamp {
    "time-stamp": string;
  }
  