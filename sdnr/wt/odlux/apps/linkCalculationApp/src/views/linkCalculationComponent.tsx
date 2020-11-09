/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
import './Style.scss'

import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import { UpdateFrequencyAction, UpdateLatLonAction, UpdateRainAttAction, UpdateRainValAction, UpdateFslCalculation, isCalculationServerReachableAction, UpdatePolAction, UpdateDistanceAction, updateAltitudeAction, UpdateAbsorptionLossAction, UpdateWorstMonthRainAction } from "../actions/commonLinkCalculationActions";
import { faPlaneArrival, faAlignCenter } from "@fortawesome/free-solid-svg-icons";
import ConnectionInfo from '../components/connectionInfo'
import { red } from "@material-ui/core/colors";



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
  aglB:state.linkCalculation.calculations.aglB,
  absorptionOxygen : state.linkCalculation.calculations.absorptionOxygen,
  absorptionWater : state.linkCalculation.calculations.absorptionWater,
  month : state.linkCalculation.calculations.month
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
  },

  UpdateAbsorption : (OxLoss:number , WaterLoss:number) => {
    dispatcher.dispatch (new UpdateAbsorptionLossAction (OxLoss, WaterLoss))
  },
  // UpdateWorstMonth : (worstmonth:boolean) => {
  //   dispatcher.dispatch (new UpdateWorstMonthAction(worstmonth))
  // }, 

  UpdateWorstMonthRain : (month:string) => {
      dispatcher.dispatch (new UpdateWorstMonthRainAction(month))
    }

  
});




type linkCalculationProps = Connect<typeof mapProps, typeof mapDispatch>;

interface initialState {
  rainMethodDisplay: boolean, 
  horizontalBoxChecked: boolean,
    latitude1Error: string,
      longitude1Error:string
      latitude2Error: string,
     longitude2Error:string,
     frequencyError: string,
     rainMethodError: string,
     worstmonth : boolean,
     showWM : string
     
  
}

class LinkCalculation extends React.Component<linkCalculationProps, initialState> {
  constructor(props: any) {
    super(props);
    this.state = {
      rainMethodDisplay: false,
      horizontalBoxChecked: true,
      latitude1Error: '',
      longitude1Error:'',
      latitude2Error: '',
      longitude2Error:'',
      frequencyError: '',
      rainMethodError: '',
      worstmonth : false,
      showWM: ''
        };
  } 
  
  updateAutoDistance = async (lat1: number, lon1: number, lat2: number, lon2: number)=>{
     const result = await fetch(BASE_URL+'/calculations/distance/' + lat1 + '/' + lon1 + '/' + lat2 + '/' + lon2)
      const json = await result.json()
      return json.distanceInKm
      }

  updateLatLon = (e:any) => {
    
    e.target.id== 'Lat1'? this.props.updateLatLon(e.target.value, this.props.lon1, this.props.lat2, this.props.lon2) : null
    e.target.id== 'Lon1'? this.props.updateLatLon(this.props.lat1, e.target.value, this.props.lat2, this.props.lon2) : null
    e.target.id== 'Lat2'? this.props.updateLatLon(this.props.lat1, this.props.lon1, e.target.value, this.props.lon2) : null
    e.target.id== 'Lon2'? this.props.updateLatLon(this.props.lat1, e.target.value, this.props.lat2, e.target.value) : null

   
  }

  updatePoli = (val: string) =>{

    this.setState({horizontalBoxChecked: !this.state.horizontalBoxChecked});
    this.props.updatePolarization(val);
   
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

  rainAttCal = (lat1: any, lon1: any, lat2: any, lon2: any, frequency: any, distance: number, polarization : any, worstmonth:boolean) => {
    if(!worstmonth){
      fetch(BASE_URL+'/calculations/rain/Annual/' + lat1 + '/' + lon1 + '/' + lat2 + '/' + lon2 + '/' + frequency+ '/'+ distance + '/' + polarization)
      .then(res => res.json())
      .then(result => { this.props.UpdateRainAtt(result.RainAtt) ; this.props.updateRainValue(result.rainfall) })
    }
    else {
      fetch(BASE_URL+'/calculations/rain/WM/' + lat1 + '/' + lon1 + '/' + lat2 + '/' + lon2 + '/' + frequency+ '/'+ distance + '/' + polarization)
      .then(res => res.json())
      .then(result => { this.props.UpdateRainAtt(result.RainAtt) ; this.props.updateRainValue(result.rainfallWM); this.props.UpdateWorstMonthRain (result.month);  this.setState({showWM: '- Wm is : '})})
    }
  }


  manualRain = (rainfall: number, frequency: number, distance:number, polarization : any) => {
    fetch(BASE_URL+'/calculations/rain/' + rainfall + '/' + frequency + '/' + distance+ '/' + polarization)
      .then(res => res.json())
      .then(result => { this.props.specificRain(result.RainAtt) })
    }


    FSL = (distance:number, frequency:number) => {
      fetch(BASE_URL+'/calculations/FSL/' + distance + '/' + frequency)
      .then(res=>res.json())
      .then (result => {this.props.FSL(result.free)})
    }
  
    AbsorptionAtt =(lat1: number, lon1: number, lat2: number, lon2: number, distance:number, frequency:number, worstmonth:boolean) => {
      if(!worstmonth)
      {
      fetch(BASE_URL+'/calculations/absorption/Annual/' +lat1 + '/' + lon1 + '/' + lat2 + '/' + lon2 + '/' +  distance + '/' + frequency)
      .then(res=>res.json())
      .then (result => {this.props.UpdateAbsorption(result.OxLoss, result.WaterLoss)})
      }
      else {
       fetch(BASE_URL+'/calculations/absorption/WM/' +lat1 + '/' + lon1 + '/' + lat2 + '/' + lon2 + '/' +  distance + '/' + frequency)
      .then(res=>res.json())
      .then (result => {this.props.UpdateAbsorption(result.OxLoss, result.WaterLoss)})
      }
    }

    formValid = () => {
      
      this.props.lat1 === 0 ? this.setState({latitude1Error: 'Enter a number between -90 to 90'}) : null
      this.props.lat2 === 0 ? this.setState({latitude2Error: 'Enter a number between -90 to 90'}) : null
      this.props.lon1 === 0 ? this.setState({longitude1Error: 'Enter a number between -180 to 180' }) : null
      this.props.lon2 === 0 ? this.setState({longitude2Error: 'Enter a number between -180 to 180' }) : null
      this.props.frequency === 0 ? this.setState({frequencyError: 'Select a frequency' }) : this.setState({frequencyError: ''})
      
      this.state.rainMethodError === null  && this.props.rainVal === 0 ? this.setState({rainMethodError: 'Select the rain method'}) : this.setState({rainMethodError: ''})
      
      console.log(this.props.lat1 !== 0 && this.props.lat2 !== 0 && this.props.lon1 !== 0 && this.props.lon2 !==0 && this.props.frequency!==0);

      return this.props.lat1 !== 0 && this.props.lat2 !== 0 && this.props.lon1 !== 0 && this.props.lon2 !==0 && this.props.frequency!==0 

     }
  

    
  buttonHandler = async () => {
   
     if (this.formValid()) {

      this.props.updateAutoDistance(await this.updateAutoDistance(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2))
      this.FSL(this.props.distance, this.props.frequency)

      if (this.state.worstmonth===false) {
        this.setState({showWM : ' '})
        this.props.UpdateWorstMonthRain('')
        this.AbsorptionAtt (this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.props.distance, this.props.frequency, this.state.worstmonth)

        if (this.state.rainMethodDisplay === true){

          this.manualRain(this.props.rainVal, this.props.frequency, this.props.distance, this.props.polarization); 
        }
        else {
          // this.updateRainValue(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.state.worstmonth)
          this.rainAttCal(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.props.frequency, this.props.distance, this.props.polarization, this.state.worstmonth);
        }
      } 
      else { 
        this.AbsorptionAtt (this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.props.distance, this.props.frequency, this.state.worstmonth)

          // this.updateRainValue(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.state.worstmonth)

          this.rainAttCal(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.props.frequency, this.props.distance, this.props.polarization, this.state.worstmonth);
      }
    }
      else null

  }

  componentDidMount = () => {
    fetch (BASE_URL+'/calculations/fsl/0/0')
    .then(res => {if (res.ok) {this.props.reachable===false && this.props.UpdateConectivity(true)}else {this.props.reachable===true && this.props.UpdateConectivity(false)} })
    .catch (res => {this.props.reachable===true && this.props.UpdateConectivity(false)} )
  }

  handleChange =(e:any) => {
    
    switch (e.target.id){
    case 'Lat1' : if ( e.target.value >90 || e.target.value<-90 )
    { this.setState({latitude1Error: 'Enter a number between -90 to 90'})}
    else {this.updateLatLon(e)
       this.setState({latitude1Error: ''}) }
    break;
    case 'Lat2' : if ( e.target.value >90 || e.target.value<-90 )
    { this.setState({latitude2Error: 'Enter a number between -90 to 90'})}
    else {this.updateLatLon(e)
       this.setState({latitude2Error: ''}) }
    break;
    case 'Lon1' : if ( e.target.value >180 || e.target.value<-180 )
    { this.setState({longitude1Error: 'Enter a number between -180 to 180'})}
    else {this.updateLatLon(e)
       this.setState({longitude1Error: ''}) }
    break;
    case 'Lon2' : if ( e.target.value >180 || e.target.value<-180 )
    { this.setState({longitude2Error: 'Enter a number between -180 to 180'})}
    else {this.updateLatLon(e)
       this.setState({longitude2Error: ''}) }
    break;
   
    }
  }

    render() {
    return (
    
    <div >
      
      {!this.props.formView && 

        <div className = 'parent'>

          <div >Site A
          <form >
          <label>Latitude:  <input className={this.state.latitude1Error.length>0 ? 'error' : 'input'} id='Lat1' type='number' onChange={(e: any) => {this.handleChange(e)} }/></label>
          <div style={{fontSize:12, color:'red'}}> {this.state.latitude1Error}  </div></form> 
          <form><label>Longitude: <input className={this.state.longitude1Error.length>0 ? 'error' : 'input'} id='Lon1' type='number' onChange={(e: any) => this.handleChange(e) } /></label><div style={{fontSize:12, color:'red'}}> {this.state.longitude1Error} </div>
          </form> 
          </div>
          
          <div>Site B
          <form>
          <label>Latitude: <input className={this.state.latitude2Error.length>0 ? 'error' : 'input'} id='Lat2' type='number' onChange={(e: any) => {this.handleChange(e) }} /></label><div style={{fontSize:12, color:'red'}}> {this.state.latitude2Error} </div></form>
          <form><label>Longitude: <input className={this.state.longitude2Error.length>0 ? 'error' : 'input'}  id='Lon2' type='number' onChange={(e: any) => {this.handleChange(e) } }/></label><div style={{fontSize:12, color:'red'}}> {this.state.longitude2Error} </div></form>
          </div>
          
          </div>
        }


        <div className='container-1'>
          <div>{<form><input type='checkbox' id='Annual' value ="Annual" checked= {this.state.worstmonth===false} onClick= {(e: any) => this.setState ({worstmonth: false})}></input>Annual
                      <input style={{marginLeft:10}} type='checkbox' id='Worst Month' value ="Worst" checked= {this.state.worstmonth===true} onClick= {(e:any)=>this.setState ({worstmonth: true})}></input>WM</form>}</div>


          <div className='column1'>
            <div>&nbsp;</div>
          <div >Site Name</div>
          <div>Latitude</div>
          <div>Longitude</div>
          <div>Azimuth</div>
          <div>Average Mean Sea Level</div>
          <div>Antenna Height Above Ground</div>
          <div>Distance</div>
          <div style={{marginTop:20}}>Polarization</div>
          <div style={{marginTop:20}}>Frequency</div>
          <div>Free Space Loss</div>
          <div style={{marginTop:10}}>Rain Model</div>
          <div style={{marginTop:20}}>Rainfall Rate</div>
          <div>Rain Loss</div>
          <div>Oxygen Specific Attenuation</div>
          <div>Water Vapor Specific Attenuation</div>
          </div>
        

          <div className='middlecolumn'>
          <div  >Site A</div>
          <div> {this.props.siteA }</div>
          <div> {this.props.lat1 && this.LatLonToDMS(this.props.lat1)}</div>
          <div>{this.props.lon1 && this.LatLonToDMS(this.props.lon1)}</div>
          <div>0</div>
          <div>{this.props.amslA.toFixed(2)} m</div>
          <div>{this.props.aglA.toFixed(2)} m</div>
        

          <div className='column2'>
          <div>{this.props.distance.toFixed(3)} km</div>
          <div>{<form><input type='checkbox' id='Horizontal' value ="Horizontal" checked= {this.props.polarization==='Horizontal'} onClick= {(e: any) => this.props.updatePolarization(e.target.value)}></input>Horizontal
                      <input style={{marginLeft:10}} type='checkbox' id='Vertical' value ="Vertical" checked= {this.props.polarization==='Vertical'} onClick= {(e:any)=>{this.props.updatePolarization(e.target.value)}}></input>Vertical</form>}</div>
          
              <div> {<select className={this.state.frequencyError.length>0 ? 'error' : 'input'}  onChange={(e) => {this.props.updateFrequency(Number(e.target.value)); e.target.value==='0'? this.setState({frequencyError: 'select a frequency'}): this.setState({frequencyError:''})}}> 
                      
                      <option value='0' >Select Freq</option>
                      <option value='7' >7 GHz</option>
                      <option value='11' >11 GHz</option>
                      <option value='15' >15 GHz</option>
                      <option value='23' >23 GHz</option>
                      <option value='26' >26 GHz</option>
                      <option value='28' >28 GHz</option>
                      <option value='38' >38 GHz</option>
                      <option value='42' >42 GHz</option>
                      <option value='80' >80 GHz</option>
                      </select>} <div style={{fontSize:12, color:'red'}}>  {this.state.frequencyError} </div> </div>

          <div>{this.props.fsl.toFixed(3)} dB</div>

          <div> {<select className={this.state.rainMethodError.length>0 ? 'error' : 'input'} onChange = {(e) => {e.target.value === 'itu' ? this.setState({ rainMethodDisplay: false}):this.setState({ rainMethodDisplay: true}); e.target.value===''? this.setState({rainMethodError: 'select a Rain model'}): this.setState({rainMethodError:''}) }}>
                        <option value='' >Select Rain Method</option>
                        <option value='itu' >ITU-R P.837-7</option>
                        <option value='manual'  >Specific Rain</option>
                        </select>} <div style={{fontSize:12,color:'red'}}>{this.state.rainMethodError}</div>
            </div> 
            <div> {<form><input type="number" style={{ width: 70, height: 15, fontSize: 14 }} onChange={(e) => { this.props.updateRainValue(Number(e.target.value)) }}
                    value={this.props.rainVal} disabled={this.state.rainMethodDisplay === false ? true : false}>
                    </input>  mm/hr  {this.state.showWM} {this.props.month}</form> } </div>
          <div>{this.props.rainAtt.toFixed(3)} dB</div>
          <div>{this.props.absorptionOxygen.toFixed(3)} dB</div>
          <div>{this.props.absorptionWater.toFixed(3)} dB</div>
          <div>{<button style={{color: '#222', fontFamily:'Arial', boxAlign: 'center', display:'inline-block', insetInlineStart: '20' , alignSelf:'center' }}
                    onClick = {(e) => this.buttonHandler()} >Calculate</button>} </div>


          </div>
          </div>
          <div className= 'middlecolumn'>
          <div  >Site B</div>
          <div> {this.props.siteB}</div>
          <div> {this.props.lat2 && this.LatLonToDMS(this.props.lat2)}</div>
          <div>{this.props.lon2 && this.LatLonToDMS(this.props.lon2)}</div>
          <div>0</div>
          <div>{this.props.amslB.toFixed(2)} m</div>
          <div>{this.props.aglB.toFixed(2)} m</div>

          
          </div>


        </div>

        
        <ConnectionInfo />
        
        
        
        </div>
      )
    }
    
  }

export default connect(mapProps, mapDispatch)(LinkCalculation);
