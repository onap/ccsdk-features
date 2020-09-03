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
import * as React from "react";

import { Connect, connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { MaterialTable, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { TextField, Tabs, Tab, Typography, AppBar, Button, Tooltip, Checkbox, Table, TableCell, TableHead, TableRow, TableBody, Paper } from '@material-ui/core';
import DenseTable from '../components/denseTable'

import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import { UpdateFrequencyAction, UpdateLatLonAction, UpdateRainAttAction, UpdateRainValAction, UpdateFslCalculation, isCalculationServerReachableAction, UpdatePolAction, UpdateDistanceAction, updateAltitudeAction } from "../actions/commonLinkCalculationActions";
import { faPlaneArrival } from "@fortawesome/free-solid-svg-icons";
import ConnectionInfo from '../components/connectionInfo'

const mapProps = (state: IApplicationStoreState) => ({
  linkId: state.linkCalculation.calculations.linkId,
  frequency: state.linkCalculation.calculations.frequency,
  lat1: state.linkCalculation.calculations.Lat1,
  lon1: state.linkCalculation.calculations.Lon1,
  lat2: state.linkCalculation.calculations.Lat2,
  lon2: state.linkCalculation.calculations.Lon2,
  rainAtt: state.linkCalculation.calculations.rainAtt,
  rainVal: state.linkCalculation.calculations.rainVal,
  formView: state.linkCalculation.calculations.formView,
  fsl:state.linkCalculation.calculations.fsl,
  siteA: state.linkCalculation.calculations.siteA,
  siteB: state.linkCalculation.calculations.siteB,
  distance: state.linkCalculation.calculations.distance,
  reachable :state.linkCalculation.calculations.reachable,
  polarization:state.linkCalculation.calculations.polarization,
  amslA:state.linkCalculation.calculations.amslA,
  amslB:state.linkCalculation.calculations.amslB,
  aglA:state.linkCalculation.calculations.aglA,
  aglB:state.linkCalculation.calculations.aglB
});

const BASE_URL="/topology/services"

const mapDispatch = (dispatcher: IDispatcher) => ({

  updateFrequency: (frequency: number) => {
    dispatcher.dispatch(new UpdateFrequencyAction(frequency))

  },
  updateLatLon: (Lat1: number, Lon1: number, Lat2: number, Lon2: number) => {
    dispatcher.dispatch(new UpdateLatLonAction(Lat1, Lon1, Lat2, Lon2))
  },
  
  updateRainValue: (rainVal: number) => {
    dispatcher.dispatch(new UpdateRainValAction(rainVal))
  },

  UpdateRainAtt: (rainAtt: number) => {
    dispatcher.dispatch(new UpdateRainAttAction(rainAtt))
  },

  specificRain: (rainAtt: number) => {
    dispatcher.dispatch(new UpdateRainAttAction(rainAtt))

  },
 
  FSL :(free:number)=> {
    dispatcher.dispatch(new UpdateFslCalculation (free))
  },

  UpdateConectivity : (reachable:boolean) => {
    dispatcher.dispatch (new isCalculationServerReachableAction (reachable))
  },

  updatePolarization :(polarization:any)=>{
    dispatcher.dispatch (new UpdatePolAction(polarization))
  },

  updateAutoDistance : (distance:number)=>{
    dispatcher.dispatch (new UpdateDistanceAction(distance))
  }
});

type linkCalculationProps = Connect<typeof mapProps, typeof mapDispatch>;

class LinkCalculation extends React.Component<linkCalculationProps, {rainMethodDisplay: boolean, horizontalBoxChecked: boolean}> {
  constructor(props: any) {
    super(props)
    this.state = { rainMethodDisplay: false,
      horizontalBoxChecked: true
                }
    } 
  updateAutoDistance = async (lat1: number, lon1: number, lat2: number, lon2: number)=>{
     const result = await fetch(BASE_URL+'/calculations/distance/' + lat1 + '/' + lon1 + '/' + lat2 + '/' + lon2)
      const json = await result.json()
      return json.distanceInKm
      }

  updateLatLon = (e: any) => {
    
    if (e.target.id == 'Lat1') this.props.updateLatLon(e.target.value, this.props.lon1, this.props.lat2, this.props.lon2)
    if (e.target.id == 'Lon1') this.props.updateLatLon(this.props.lat1, e.target.value, this.props.lat2, this.props.lon2)
    if (e.target.id == 'Lat2') this.props.updateLatLon(this.props.lat1, this.props.lon1, e.target.value, this.props.lon2)
    if (e.target.id == 'Lon2') this.props.updateLatLon(this.props.lat1, this.props.lon1, this.props.lat2, e.target.value)

  }

  updatePoli = (val: string) =>{

    this.setState({horizontalBoxChecked: !this.state.horizontalBoxChecked});
    this.props.updatePolarization(val);
    //this.forceUpdate();
  }

  LatLonToDMS = (value: number, isLon: boolean = false) => {
    const absoluteValue = Math.abs(value);
    const d = Math.floor(absoluteValue);
    const m = Math.floor((absoluteValue - d) * 60);
    const s = (absoluteValue - d - m / 60) * 3600;
    const dms = `${d}° ${m}' ${s.toFixed(2)}"`

    const sign = Math.sign(value);

    if (isLon) {
      return (sign === -1 || sign === -0) ? dms + " W" : dms + " E";
    } else {
      return (sign === -1 || sign === -0) ? dms + " S" : dms + " N";
    }
  }

  rainAttCal = (lat1: any, lon1: any, lat2: any, lon2: any, frequency: any, distance: number, polarization : any) => {
    fetch(BASE_URL+'/calculations/rain/' + lat1 + '/' + lon1 + '/' + lat2 + '/' + lon2 + '/' + frequency+ '/'+ distance + '/' + polarization)
      .then(res => res.json())
      .then(result => { this.props.UpdateRainAtt(result.RainAtt) })
  }


  manualRain = (rainfall: number, frequency: number, distance:number, polarization : any) => {
    fetch(BASE_URL+'/calculations/rain/' + rainfall + '/' + frequency + '/' + distance+ '/' + polarization)
      .then(res => res.json())
      .then(result => { this.props.specificRain(result.RainAtt) })
    }

  updateRainValue = (lat1: any, lon1: any, lat2: any, lon2: any) => {
      fetch(BASE_URL+'/calculations/rainval/' + lat1 + '/' + lon1 + '/' + lat2 + '/' + lon2)
        .then(res => res.json())
        .then(result => {this.props.updateRainValue(result.rainFall) })
    }

    FSL = (distance:number, frequency:number) => {
      fetch(BASE_URL+'/calculations/FSL/' + distance + '/' + frequency)
      .then(res=>res.json())
      .then (result => {this.props.FSL(result.free)})
    }

   

  buttonHandler = async () => {
      this.props.updateAutoDistance(await this.updateAutoDistance(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2))

      this.FSL(this.props.distance, this.props.frequency)

      if (this.state.rainMethodDisplay === true){

        this.manualRain(this.props.rainVal, this.props.frequency, this.props.distance, this.props.polarization); 
      }
      else {
        this.updateRainValue(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2)
        this.rainAttCal(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.props.frequency, this.props.distance, this.props.polarization);
      }

  }

  componentDidMount = () => {
    fetch (BASE_URL+'/calculations/fsl/0/0')
    .then(res => {if (res.ok) {this.props.reachable===false && this.props.UpdateConectivity(true)}else {this.props.reachable===true && this.props.UpdateConectivity(false)} })
    .catch (res => {this.props.reachable===true && this.props.UpdateConectivity(false)} )
  }

  handleChange =(e:any) => {
  this.props.updatePolarization(e.target.value)
  }

  // AbsorptionAttW = () => {
  //   fetch(BASE_URL+'/calculations/FSL/' + distance + '/' + frequency)
  //   .then(res=>res.json())
  //   .then (result => {this.props.FSL(result.free)})
  // }

  // AbsorptionAttOx =() => {
  //   fetch(BASE_URL+'/calculations/FSL/' + distance + '/' + frequency)
  //   .then(res=>res.json())
  //   .then (result => {this.props.FSL(result.free)})
  // }


  render() {
    
    return <div style={{position: 'relative'}}>

      {!this.props.formView && <form>
          <div>
          
            <br />Site A 
            <br /> Site Id:
                <label style={{ marginInlineStart: 20 }}> latitude
                <input id='Lat1' type='number' onChange={(e: any) => this.updateLatLon(e)} />
            </label>
            <label style={{ marginInlineStart: 20 }}>longitude
                <input id='Lon1' type='number' onChange={(e: any) => this.updateLatLon(e)} />
            </label><br /><br />

          </div>
          <div> <br />Site B<br /> Site Id:
                <label style={{ marginInlineStart: 20 }}> latitude
                <input id='Lat2' type='number' onChange={(e: any) => this.updateLatLon(e)} />
            </label>
            <label style={{ marginInlineStart: 20 }}>longitude
                <input id='Lon2' type='number' onChange={(e: any) => this.updateLatLon(e)} />
            </label>
            <br />
          </div>
        </form>
        }

<Paper style={{borderRadius:"0px"}}>
       <div style={{ height:600, overflow:"auto"}}>
            <Table stickyHeader size="small" aria-label="a dense table" >
                <TableHead>
                    <TableRow>
                    <TableCell >{""}  </TableCell>
                    <TableCell >{"Site A"}</TableCell>
                    <TableCell > {""} </TableCell>
                    <TableCell >{"Site B"}  </TableCell>
                </TableRow>
                </TableHead>
              <TableBody>
                <TableRow>
                    <TableCell >{"Site Name"}  </TableCell>
                    <TableCell >{this.props.siteA}</TableCell>
                    <TableCell > {""} </TableCell>
                    <TableCell >{this.props.siteB}  </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell >{"Latitude"}  </TableCell>
                    <TableCell >{this.props.lat1  && this.LatLonToDMS(this.props.lat1)} </TableCell>
                    <TableCell > {""} </TableCell>
                    <TableCell >{this.props.lat2 && this.LatLonToDMS(this.props.lat2)}  </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell >{"Longitude"}  </TableCell>
                    <TableCell >{this.props.lon1 && this.LatLonToDMS(this.props.lon1)}</TableCell>
                    <TableCell > {""} </TableCell>
                    <TableCell >{this.props.lon2 && this.LatLonToDMS(this.props.lon2)}  </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell >{"Azimuth"}  </TableCell>
                    <TableCell >{""}</TableCell>
                    <TableCell > {""} </TableCell>
                    <TableCell >{""}  </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell >{"Average Mean Sea Level"}  </TableCell>
                    <TableCell >{this.props.amslA + ' m'}</TableCell>
                    <TableCell > {""} </TableCell>
                    <TableCell >{this.props.amslB+ ' m'}  </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell >{"Antenna Height Above Ground"}  </TableCell>
                    <TableCell >{this.props.aglA+ ' m'}</TableCell>
                    <TableCell > {""} </TableCell>
                    <TableCell >{this.props.aglB+ ' m'}  </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell >{"Distance"}  </TableCell>
                    <TableCell >{""}</TableCell>
                    <TableCell > {this.props.distance.toFixed(3)+ ' km'} </TableCell>
                    <TableCell >{""}  </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell >{"Polarization"}  </TableCell>
                    <TableCell >{""}</TableCell>
                    <TableCell > {<form><input type='checkbox' id='Horizontal' value ="Horizontal" checked= {this.props.polarization==='Horizontal'} onClick= {(e: any) => this.props.updatePolarization(e.target.value)}></input>Horizontal<br />
                    <input type='checkbox' id='Vertical' value ="Vertical" checked= {this.props.polarization==='Vertical'} onClick= {(e:any)=>{this.props.updatePolarization(e.target.value)}}></input>Vertical</form>} </TableCell>
                    <TableCell >{""}  </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell >{"Frequency"}  </TableCell>
                    <TableCell >{""}</TableCell>
                    <TableCell > {<select onChange={(e) => this.props.updateFrequency(Number(e.target.value))}>
                        <option value='' >Select Freq</option>
                        <option value='7' >7 GHz</option>
                        <option value='11' >11 GHz</option>
                        <option value='15' >15 GHz</option>
                        <option value='23' >23 GHz</option>
                        <option value='26' >26 GHz</option>
                        <option value='28' >28 GHz</option>
                        <option value='38' >38 GHz</option>
                        <option value='42' >42 GHz</option>
                        <option value='80' >80 GHz</option>
                        </select>} 
                      </TableCell>
                    <TableCell >{""}  </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell >{"Free Space Loss"}  </TableCell>
                    <TableCell >{""}</TableCell>
                    <TableCell > {this.props.fsl + ' dB'} </TableCell>
                    <TableCell >{""}  </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell >{"Rain Model"}  </TableCell>
                    <TableCell >{""}</TableCell>
                    <TableCell > {<select onChange = {(e) => {e.target.value === 'itu' ? this.setState({ rainMethodDisplay: false}):this.setState({ rainMethodDisplay: true}) }}>
                      <option >Select Rain Method</option>
                      <option value='itu' >ITU-R P.837-7</option>
                      <option value='manual'  >Specific Rain</option>
                      </select>} </TableCell>
                    <TableCell >{""} </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell >{"Rainfall Rate"}  </TableCell>
                    <TableCell >{""}</TableCell>
                    <TableCell > {<form><input type="number" style={{ width: 70, height: 15, fontSize: 14 }} onChange={(e) => { this.props.updateRainValue(Number(e.target.value)) }}
                    value={this.props.rainVal} disabled={this.state.rainMethodDisplay === false ? true : false}>
                    </input>  mm/hr</form> } </TableCell>
                    <TableCell >{""}  </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell >{"Rain Loss"}  </TableCell>
                    <TableCell >{""}</TableCell>
                    <TableCell > {this.props.rainAtt + ' dB'} </TableCell>
                    <TableCell >{""}  </TableCell>
                </TableRow>
                <TableRow>
                    <TableCell >{""}  </TableCell>
                    <TableCell >{""}</TableCell>
                    <TableCell > {<button style={{color: '#222', fontFamily:'Arial', boxAlign: 'center', display:'inline-block', insetInlineStart: '20' }}
                    onClick = {(e) => this.buttonHandler()} >Calculate</button>} </TableCell>
                    <TableCell >{""}  </TableCell>
                </TableRow>

                </TableBody>
            </Table>
            </div>
        </Paper>
      <ConnectionInfo />
        
        
        </div>
    }
}

export default connect(mapProps, mapDispatch)(LinkCalculation);
