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
import { TextField, Tabs, Tab, Typography, AppBar, Button, Tooltip } from '@material-ui/core';
import DenseTable from '../components/denseTable'

import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import { UpdateFrequencyAction, UpdateLatLonAction, UpdateRainAttAction, UpdateRainValAction, UpdateFslCalculation, isCalculationServerReachableAction } from "../actions/commonLinkCalculationActions";
import { faPlaneArrival } from "@fortawesome/free-solid-svg-icons";

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
  reachable :state.linkCalculation.calculations.reachable
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
  }
});

class LinkCalculation extends React.Component<Connect<typeof mapProps, typeof mapDispatch>, { rainMethodDisplay: boolean }> {
  constructor(props: any) {
    super(props)
    this.state = { rainMethodDisplay: true }
  }

  handleChange = (e: number) => {
    this.props.updateFrequency(e)
  }

  updateLatLon = (e: any) => {
    
    if (e.target.id == 'Lat1') this.props.updateLatLon(e.target.value, this.props.lon1, this.props.lat2, this.props.lon2)
    if (e.target.id == 'Lon1') this.props.updateLatLon(this.props.lat1, e.target.value, this.props.lat2, this.props.lon2)
    if (e.target.id == 'Lat2') this.props.updateLatLon(this.props.lat1, this.props.lon1, e.target.value, this.props.lon2)
    if (e.target.id == 'Lon2') this.props.updateLatLon(this.props.lat1, this.props.lon1, this.props.lat2, e.target.value)

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

  calRain = (lat1: any, lon1: any, lat2: any, lon2: any, frequency: any) => {
    fetch(BASE_URL+'/calculations/rain/' + lat1 + '/' + lon1 + '/' + lat2 + '/' + lon2 + '/' + frequency)
      .then(res => res.json())
      .then(result => { this.props.UpdateRainAtt(result.RainAtt) })
  }


  updateRainValue = (lat1: any, lon1: any, lat2: any, lon2: any) => {
    fetch(BASE_URL+'/calculations/rain/' + lat1 + '/' + lon1 + '/' + lat2 + '/' + lon2)
      .then(res => res.json())
      .then(result => { this.props.updateRainValue(result.RainAtt) })
  }


  specificRain = (rainfall: number, frequency: number) => {
    fetch(BASE_URL+'/calculations/rain/' + rainfall + '/' + frequency)
      .then(res => res.json())
      .then(result => { this.props.specificRain(result.RainAtt) })
    }

    FSL = (distance:number, frequency:number) => {
      fetch(BASE_URL+'/calculations/FSL/' + distance + '/' + frequency)
      .then(res=>res.json())
      .then (result => {this.props.FSL(result.free)})
    }

  buttonHandler =() => {
      this.FSL(this.props.distance, this.props.frequency)

      if (this.state.rainMethodDisplay === true){

        this.specificRain(this.props.rainVal, this.props.frequency); 
      }
      else {
        this.calRain(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.props.frequency);
        this.updateRainValue(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2)
      }
  }

  componentDidMount = () => {
    fetch (BASE_URL+'/calculations/fsl/0/0')
    .then(res => {if (res.ok) {this.props.reachable===false && this.props.UpdateConectivity(true)}else {this.props.reachable===true && this.props.UpdateConectivity(false)} })
    .catch (res => {this.props.reachable===true && this.props.UpdateConectivity(false)} )
  }
  
  render() {
    console.log(this.props);
    const data = [

      { name: "Site Name", val1: this.props.siteA, val2: '', val3:  this.props.siteB},
      { name: "Latitude", val1: this.props.lat1 && this.LatLonToDMS(this.props.lat1), val2:'', val3: this.props.lat2 && this.LatLonToDMS(this.props.lat2) },
      { name: "Longitude", val1: this.props.lon1 && this.LatLonToDMS(this.props.lon1, true), val2:'', val3: this.props.lon2 && this.LatLonToDMS(this.props.lon2, true) },
      { name: "Azimuth in °", val1: '', val2: '' , val3:''},
      { name: "", val1: '', val2: '' , val3:''},
      { name: "Distance (km)", val1: '', val2: (this.props.distance).toFixed(3) ,val3:'' },
      {name: 'Polarization', val1:'', val2: <div><input type='checkbox' id='Horizontal' value ="Horizontal"></input>Horizontal<br />
      <input type='checkbox' id='Vertical' value ="Vertical"></input>Vertical
      </div>, val3:''},
      {name : 'Frequency (GHz)', val1: '', val2:  <div> 
      <select onChange={(e) => this.handleChange(Number(e.target.value))}>
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
        </select></div>,val3: ''},
      {name: 'Free Space Loss (dB)' ,val1: '', val2: this.props.fsl,val3: ''},
      {name:'Rain Model', val1:'', val2: <div>
      <select onChange={e => { this.setState({ rainMethodDisplay: !this.state.rainMethodDisplay }) }} >
        <option value='' >Select Rain Method</option>
        <option value='itu' onSelect={e => { this.setState({ rainMethodDisplay: false }) }}>ITU-R P.837-7</option>
        <option value='manual' onSelect={(e) => { this.updateRainValue(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2) }}  >Specific Rain</option>
      </select> </div>, val3:''},
      {name: 'Rainfall Rate (mm/h)', val1: '', val2:<label>
       <input type="number" style={{ width: 70, height: 15, fontSize: 14 }} onChange={(e) => { this.props.updateRainValue(Number(e.target.value)) }}
        value={this.props.rainVal} disabled={this.state.rainMethodDisplay === false ? true : false}>
      </input></label>, val3:''},
      {name: 'Rain Loss (dB/km)', val1: '', val2: this.props.rainAtt, val3: ''},
      {name: '', val1:'', val2:<button style={{color: '#222', fontFamily:'Arial', boxAlign: 'center', display:'inline-block', insetInlineStart: '20' }}
      onClick = {(e) => this.buttonHandler()} >Calculate</button>, val3:'' }
     
    ];


    return <div>
      Link Calculation app. LinkId: {this.props.linkId} <br />

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
      
        <DenseTable height={600} width={1300} hover={true} headers={["", "Site A","", "Site B"]} data={data}> </DenseTable>

          

      
    </div>

  }


}

export default connect(mapProps, mapDispatch)(LinkCalculation);
