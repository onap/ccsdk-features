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
import { UpdateFrequencyAction, UpdateLatLonAction, UpdateRainAttAction, UpdateRainValAction, UpdateFslCalculation, isCalculationServerReachableAction, UpdatePolAction, UpdateDistanceAction, updateAltitudeAction, UpdateAbsorptionLossAction, UpdateWorstMonthRainAction, updateAntennaList, UpdateAntennaAction, UpdateRadioAttributesAction, UpdateTxPowerAction, UpdateRxSensitivityAction } from "../actions/commonLinkCalculationActions";
import { faPlaneArrival, faAlignCenter } from "@fortawesome/free-solid-svg-icons";
import ConnectionInfo from '../components/connectionInfo'
import { red } from "@material-ui/core/colors";
import { Dvr } from "@material-ui/icons";



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
  fsl: state.linkCalculation.calculations.fsl,
  siteA: state.linkCalculation.calculations.siteA,
  siteB: state.linkCalculation.calculations.siteB,
  distance: state.linkCalculation.calculations.distance,
  reachable: state.linkCalculation.calculations.reachable,
  polarization: state.linkCalculation.calculations.polarization,
  amslA: state.linkCalculation.calculations.amslA,
  amslB: state.linkCalculation.calculations.amslB,
  aglA: state.linkCalculation.calculations.aglA,
  aglB: state.linkCalculation.calculations.aglB,
  absorptionOxygen: state.linkCalculation.calculations.absorptionOxygen,
  absorptionWater: state.linkCalculation.calculations.absorptionWater,
  month: state.linkCalculation.calculations.month,
  eirpSiteA: state.linkCalculation.calculations.eirpA,
  eirpSiteB: state.linkCalculation.calculations.eirpB,
  antennaGainA: state.linkCalculation.calculations.antennaGainA,
  antennaGainB: state.linkCalculation.calculations.antennaGainB,
  antennaList: state.linkCalculation.calculations.antennaList,
  antennaGainList: state.linkCalculation.calculations.antennaGainList,
  antennaA: state.linkCalculation.calculations.antennaA,
  antennaB: state.linkCalculation.calculations.antennaB,
  systemOperatingMargin : state.linkCalculation.calculations.systemOperatingMargin

});

const BASE_URL = "/topology/linkcalculator"

const mapDispatch = (dispatcher: IDispatcher) => ({

  updateFrequency: (frequency: number) => {

    dispatcher.dispatch(new UpdateFrequencyAction(frequency))
    dispatcher.dispatch(updateAntennaList(frequency))
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

  FSL: (free: number) => {
    dispatcher.dispatch(new UpdateFslCalculation(free))
  },

  UpdateConectivity: (reachable: boolean) => {
    dispatcher.dispatch(new isCalculationServerReachableAction(reachable))
  },

  updatePolarization: (polarization: any) => {
    dispatcher.dispatch(new UpdatePolAction(polarization))
  },

  updateAutoDistance: (distance: number) => {
    dispatcher.dispatch(new UpdateDistanceAction(distance))
  },
  UpdateAbsorption: (OxLoss: number, WaterLoss: number) => {
    dispatcher.dispatch(new UpdateAbsorptionLossAction(OxLoss, WaterLoss))
  },
  UpdateWorstMonthRain: (month: string) => {
    dispatcher.dispatch(new UpdateWorstMonthRainAction(month))
  },
  UpdateAntenas: (antennaA: string | null, antennaB: string | null) => {
    dispatcher.dispatch(new UpdateAntennaAction(antennaA, antennaB))
  },
  UpdateRadioAttributes :(som: number, eirpA: number, eirpB: number)=>{
    dispatcher.dispatch(new UpdateRadioAttributesAction(som,eirpA, eirpB))
  },
  UpdateTxPower :(txPowerA: string | null, txPowerB: string | null)=>{
    dispatcher.dispatch(new UpdateTxPowerAction(txPowerA, txPowerB))
  }, 
  UpdateRxSensitivity :(rxSensitivityA : string | null, rxSensitivityB : string | null)=>{
    dispatcher.dispatch(new UpdateRxSensitivityAction(rxSensitivityA, rxSensitivityB))
  }
});


type linkCalculationProps = Connect<typeof mapProps, typeof mapDispatch>;

interface initialState {
  rainMethodDisplay: boolean,
  absorptionMethod: string;
  horizontalBoxChecked: boolean,
  latitude1Error: string,
  longitude1Error: string
  latitude2Error: string,
  longitude2Error: string,
  frequencyError: string,
  rainMethodError: string,
  antennaTypeError: string,
  attenuationMethodError: string,
  worstmonth: boolean,
  showWM: string,
}

class LinkCalculation extends React.Component<linkCalculationProps, initialState> {
  constructor(props: any) {
    super(props);
    this.state = {
      rainMethodDisplay: false,
      horizontalBoxChecked: true,
      absorptionMethod: '0',
      latitude1Error: '',
      longitude1Error: '',
      latitude2Error: '',
      longitude2Error: '',
      frequencyError: '',
      rainMethodError: '',
      attenuationMethodError: '',
      antennaTypeError: '',
      worstmonth: false,
      showWM: '',
    };
  }

  updateAutoDistance = async (lat1: number, lon1: number, lat2: number, lon2: number) => {
    const result = await fetch(BASE_URL + '/distance/' + lat1 + ',' + lon1 + ',' + lat2 + ',' + lon2)
    const json = await result.json()
    return json.distanceInKm
  }

  updateLatLon = (e: any) => {

    e.target.id == 'Lat1' ? this.props.updateLatLon(e.target.value, this.props.lon1, this.props.lat2, this.props.lon2) : null
    e.target.id == 'Lon1' ? this.props.updateLatLon(this.props.lat1, e.target.value, this.props.lat2, this.props.lon2) : null
    e.target.id == 'Lat2' ? this.props.updateLatLon(this.props.lat1, this.props.lon1, e.target.value, this.props.lon2) : null
    e.target.id == 'Lon2' ? this.props.updateLatLon(this.props.lat1, e.target.value, this.props.lat2, e.target.value) : null


  }

  updatePoli = (val: string) => {

    this.setState({ horizontalBoxChecked: !this.state.horizontalBoxChecked });
    this.props.updatePolarization(val);

  }

  LatLonToDMS = (value: number, isLon: boolean = false) => {
    const absoluteValue = Math.abs(value);
    const d = Math.floor(absoluteValue);
    const m = Math.floor((absoluteValue - d) * 60);
    const s = (absoluteValue - d - m / 60) * 3600;
    const dms = `${d}° ${m}' ${s.toFixed(2)}"`;

    const sign = Math.sign(value);

    if (isLon) {
      return (sign === -1 || sign === -0) ? dms + " W" : dms + " E";
    } else {
      return (sign === -1 || sign === -0) ? dms + " S" : dms + " N";
    }
  }

  rainAttCal = (lat1: any, lon1: any, lat2: any, lon2: any, frequency: any, distance: number, polarization: string, worstmonth: boolean) => {
    if (!worstmonth) {
      fetch(BASE_URL + '/rain/annual/' + lat1 + ',' + lon1 + ',' + lat2 + ',' + lon2 + '/' + frequency + '/' + distance + '/' + polarization.toUpperCase())
        .then(res => res.json())
        .then(result => { this.props.UpdateRainAtt(result.rainAttenuation); this.props.updateRainValue(result.rainFall.rainrate) })
    }
    else {
      fetch(BASE_URL + '/rain/worstmonth/' + lat1 + ',' + lon1 + ',' + lat2 + ',' + lon2 + '/' + frequency + '/' + distance + '/' + polarization.toUpperCase())
        .then(res => res.json())
        .then(result => { this.props.UpdateRainAtt(result.rainAttenuation); this.props.updateRainValue(result.rainFall.rainrate); this.props.UpdateWorstMonthRain(result.rainFall.period); this.setState({ showWM: '- Wm is : ' }) })
    }
  }


  manualRain = (rainfall: number, frequency: number, distance: number, polarization: string) => {
    fetch(BASE_URL + '/rain/' + rainfall + '/' + frequency + '/' + distance + '/' + polarization.toUpperCase())
      .then(res => res.json())
      .then(result => { this.props.specificRain(result.rainAttenuation) })
  }


  FSL = (distance: number, frequency: number) => {
    fetch(BASE_URL + '/fsl/' + distance + '/' + frequency)
      .then(res => res.json())
      .then(result => { this.props.FSL(result.fspl) })
  }

  AbsorptionAtt = (lat1: number, lon1: number, lat2: number, lon2: number, distance: number, frequency: number, worstmonth: boolean, absorptionMethod: string) => {
    if (!worstmonth) {
      fetch(BASE_URL + '/absorption/annual/' + lat1 + ',' + lon1 + ',' + lat2 + ',' + lon2 + '/' + distance + '/' + frequency + '/' + absorptionMethod)
        .then(res => res.json())
        .then(result => { this.props.UpdateAbsorption(result.oxygenLoss, result.waterLoss) })
    }
    else {
      fetch(BASE_URL + '/absorption/annual/' + lat1 + ',' + lon1 + ',' + lat2 + ',' + lon2 + '/' + distance + '/' + frequency + '/' + absorptionMethod)
        .then(res => res.json())
        .then(result => { this.props.UpdateAbsorption(result.oxygenLoss, result.waterLoss) })
    }
  }

  linkBudget = (antennaA: string, antennaB: string, transmissionPowerA: number, transmissionPowerB: number) => {
    fetch(BASE_URL + '/linkbudget/' + antennaA + '/' + antennaB + '/' + transmissionPowerA + '/' + transmissionPowerB)
    .then(res=>res.json())
    .then(result => {this.props.UpdateRadioAttributes(result.systemOperatingMargin, result.eirpA, result.eirpB)})
  }

  formValid = () => {

    this.props.lat1 === 0 ? this.setState({ latitude1Error: 'Enter a number between -90 to 90' }) : null
    this.props.lat2 === 0 ? this.setState({ latitude2Error: 'Enter a number between -90 to 90' }) : null
    this.props.lon1 === 0 ? this.setState({ longitude1Error: 'Enter a number between -180 to 180' }) : null
    this.props.lon2 === 0 ? this.setState({ longitude2Error: 'Enter a number between -180 to 180' }) : null
    this.props.frequency === 0 ? this.setState({ frequencyError: 'Select a frequency' }) : this.setState({ frequencyError: '' })

    this.state.rainMethodDisplay === null && this.props.rainVal === 0 ? this.setState({ rainMethodError: 'Select the rain method' }) : this.setState({ rainMethodError: '' })
    this.state.absorptionMethod === '0' ? this.setState({ attenuationMethodError: 'Select the attenuation method' }) : this.setState({ attenuationMethodError: '' })
    console.log(this.state);
    console.log(this.props.lat1 !== 0 && this.props.lat2 !== 0 && this.props.lon1 !== 0 && this.props.lon2 !== 0 && this.props.frequency !== 0 && this.state.rainMethodError === '' && this.state.attenuationMethodError === '');

    return this.props.lat1 !== 0 && this.props.lat2 !== 0 && this.props.lon1 !== 0 && this.props.lon2 !== 0 && this.props.frequency !== 0 && this.state.rainMethodError === '' && this.state.attenuationMethodError === '';

  }



  buttonHandler = async () => {

    if (this.formValid()) {

      this.props.updateAutoDistance(await this.updateAutoDistance(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2))
      this.FSL(this.props.distance, this.props.frequency)

      if (this.state.worstmonth === false) {
        this.setState({ showWM: ' ' })
        this.props.UpdateWorstMonthRain('')
        this.AbsorptionAtt(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.props.distance, this.props.frequency, this.state.worstmonth, this.state.absorptionMethod)

        if (this.state.rainMethodDisplay === true) {

          this.manualRain(this.props.rainVal, this.props.frequency, this.props.distance, this.props.polarization!);
        }
        else {
          // this.updateRainValue(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.state.worstmonth)
          this.rainAttCal(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.props.frequency, this.props.distance, this.props.polarization!, this.state.worstmonth);
        }
      }
      else {
        this.AbsorptionAtt(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.props.distance, this.props.frequency, this.state.worstmonth, this.state.absorptionMethod)

        // this.updateRainValue(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.state.worstmonth)

        this.rainAttCal(this.props.lat1, this.props.lon1, this.props.lat2, this.props.lon2, this.props.frequency, this.props.distance, this.props.polarization!, this.state.worstmonth);
      }
    }
    else console.log('form is not valid')

  }

  componentDidMount = () => {
    fetch(BASE_URL + '/fsl/1/1')
      .then(res => { if (res.ok) { this.props.UpdateConectivity(true) } else { this.props.UpdateConectivity(false) } })
      .catch(res => { this.props.UpdateConectivity(false) })
  }

  handleChange = (e: any) => {

    switch (e.target.id) {
      case 'Lat1': if (e.target.value > 90 || e.target.value < -90) { this.setState({ latitude1Error: 'Enter a number between -90 to 90' }) }
      else {
        this.updateLatLon(e)
        this.setState({ latitude1Error: '' })
      }
        break;
      case 'Lat2': if (e.target.value > 90 || e.target.value < -90) { this.setState({ latitude2Error: 'Enter a number between -90 to 90' }) }
      else {
        this.updateLatLon(e)
        this.setState({ latitude2Error: '' })
      }
        break;
      case 'Lon1': if (e.target.value > 180 || e.target.value < -180) { this.setState({ longitude1Error: 'Enter a number between -180 to 180' }) }
      else {
        this.updateLatLon(e)
        this.setState({ longitude1Error: '' })
      }
        break;
      case 'Lon2': if (e.target.value > 180 || e.target.value < -180) { this.setState({ longitude2Error: 'Enter a number between -180 to 180' }) }
      else {
        this.updateLatLon(e)
        this.setState({ longitude2Error: '' })
      }
        break;

    }
  }

  render() {
    return (

      <div >

        {!this.props.formView &&

          <div className='container1'>
            <div className='firstBox'>
              <div>SiteA</div>
              <div>SiteB</div>
            </div>

            <div className='firstBox'>
              <div>
                <form >
                  <label>Latitude:  <input aria-label="site-a-latitude-input" className={this.state.latitude1Error.length > 0 ? 'error' : 'input'} id='Lat1' type='number' onChange={(e: any) => { this.handleChange(e) }} /></label>
                  <div style={{ fontSize: 12, color: 'red' }}> {this.state.latitude1Error}  </div>
                </form></div>
              <div>
                <form>
                  <label>Latitude: <input aria-label="site-b-latitude-input" className={this.state.latitude2Error.length > 0 ? 'error' : 'input'} id='Lat2' type='number' onChange={(e: any) => { this.handleChange(e) }} /></label><div style={{ fontSize: 12, color: 'red' }}> {this.state.latitude2Error} </div>
                </form></div>
            </div>

            <div className='firstBox'>
              <div>
                <form><label>Longitude: <input aria-label="site-a-longitude-input" className={this.state.longitude1Error.length > 0 ? 'error' : 'input'} id='Lon1' type='number' onChange={(e: any) => this.handleChange(e)} /></label><div style={{ fontSize: 12, color: 'red' }}> {this.state.longitude1Error} </div>
                </form></div>
              <div>
                <form><label>Longitude: <input aria-label="site-b-longitude-input" className={this.state.longitude2Error.length > 0 ? 'error' : 'input'} id='Lon2' type='number' onChange={(e: any) => { this.handleChange(e) }} /></label><div style={{ fontSize: 12, color: 'red' }}> {this.state.longitude2Error} </div></form>
              </div>
            </div>



          </div>
        }


        <div className='container1'>
          <div >{<form><input aria-label="annual" type='checkbox' id='Annual' value="Annual" checked={this.state.worstmonth === false} onClick={(e: any) => this.setState({ worstmonth: false })}></input>Annual
                      <input aria-label="worst-month" style={{ marginLeft: 10 }} type='checkbox' id='Worst Month' value="Worst" checked={this.state.worstmonth === true} onClick={(e: any) => this.setState({ worstmonth: true })}></input>WM</form>}
          </div>
          <div className='firstBox'>
            <div>Site A</div>
            <div>Site B</div>
          </div>
          {/* <div>&nbsp;</div> */}
          <div>
            {(this.props.siteA.length > 0 || this.props.siteB.length > 0) && <div >Site Name</div>}
            <div>  {this.props.siteA}</div>
            <div>  {this.props.siteB}</div>
          </div>
          <div>
            <div>Latitude</div>
            <div aria-label="site-a-latitude-dms"> {this.props.lat1 && this.LatLonToDMS(this.props.lat1)}</div>
            <div aria-label="site-b-latitude-dms"> {this.props.lat2 && this.LatLonToDMS(this.props.lat2)}</div>

          </div>
          <div>
            <div>Longitude</div>
            <div aria-label="site-a-longitude-dms">{this.props.lon1 && this.LatLonToDMS(this.props.lon1)}</div>
            <div aria-label="site-b-longitude-dms">{this.props.lon2 && this.LatLonToDMS(this.props.lon2)}</div>
          </div>
          <div>
            <div>Azimuth</div>
            <div>0</div>
            <div>0</div>
          </div>
          <div>
            <div>Average Mean Sea Level</div>
            <div aria-label="site-a-amsl">{this.props.amslA.toFixed(2)} m</div>
            <div aria-label="site-b-amsl">{this.props.amslB.toFixed(2)} m</div>
          </div>
          <div>
            <div>Antenna Height Above Ground</div>
            <div aria-label="site-a-antenna-amsl">{this.props.aglA.toFixed(2)} m</div>
            <div aria-label="site-b-antenna-amsl">{this.props.aglB.toFixed(2)} m</div>
          </div>
          <div>
            <div >Distance</div>
            <div aria-label="distance-between-sites">{this.props.distance?.toFixed(3)} km</div>
          </div>
          <div>
            <div >Polarization</div>
            <div >{<form><input aria-label="polarization-horizontal" type='checkbox' id='Horizontal' value="Horizontal" checked={this.props.polarization === 'Horizontal'} onClick={(e: any) => this.props.updatePolarization(e.target.value)}></input>Horizontal
                      <input aria-label="polarization-vertical" style={{ marginLeft: 10 }} type='checkbox' id='Vertical' value="Vertical" checked={this.props.polarization === 'Vertical'} onClick={(e: any) => { this.props.updatePolarization(e.target.value) }}></input>Vertical</form>}</div>
          </div>
          <div>
            <div style={{ marginTop: 5 }}>Frequency</div>
            <div style={{ marginTop: 5 }}> {<select aria-label="select-frequency-in-ghz" className={this.state.frequencyError.length > 0 ? 'error' : 'input'} onChange={(e) => { this.props.updateFrequency(Number(e.target.value)); e.target.value === '0' ? this.setState({ frequencyError: 'select a frequency' }) : this.setState({ frequencyError: '' }); this.props.UpdateAntenas('0', '0') }}>

              <option value='0' aria-label="none-value" >Select Freq</option>
              <option value='7' aria-label="7" >7 GHz</option>
              <option value='11' aria-label="11" >11 GHz</option>
              <option value='15' aria-label="15" >15 GHz</option>
              <option value='23' aria-label="23">23 GHz</option>
              <option value='26' aria-label="26">26 GHz</option>
              <option value='28' aria-label="28">28 GHz</option>
              <option value='38' aria-label="38">38 GHz</option>
              <option value='42' aria-label="42">42 GHz</option>
              <option value='80' aria-label="80">80 GHz</option>
            </select>} <div style={{ fontSize: 12, color: 'red' }}>  {this.state.frequencyError} </div> </div>
          </div>
          <div>
            <div>Free Space Loss</div>
            <div aria-label="fspl-value">{this.props.fsl.toFixed(3)} dB</div>
          </div>
          <div>
            <div>Rain Model</div>
            <div> {<select aria-label="select-rain-method" className={this.state.rainMethodError.length > 0 ? 'error' : 'input'} onChange={(e) => { e.target.value === 'itu' ? this.setState({ rainMethodDisplay: false }) : this.setState({ rainMethodDisplay: true }); e.target.value === '0' ? this.setState({ rainMethodError: 'select a Rain model' }) : this.setState({ rainMethodError: '' }) }}>
              <option value='0' aria-label="none-value" >Select Rain Method</option>
              <option value='itu' aria-label="itur8377">ITU-R P.837-7</option>
              <option value='manual' aria-label="manual-entry">Specific Rain</option>
            </select>} <div style={{ fontSize: 12, color: 'red' }}>{this.state.rainMethodError}</div>
            </div>
          </div>
          <div>
            <div>Rainfall Rate</div>
            <div> {<form><input aria-label="rain-value" type="number" style={{ width: 70, height: 15, fontSize: 14 }} onChange={(e) => { this.props.updateRainValue(Number(e.target.value)) }}
              value={this.props.rainVal} disabled={this.state.rainMethodDisplay === false ? true : false}>
            </input>  mm/hr  {this.state.showWM} {this.props.month}</form>} </div>
          </div>
          <div>
            <div>Rain Loss</div>
            <div aria-label="rain-attenuation-value">{this.props.rainAtt.toFixed(3)} dB</div>
          </div>
          <div>
            <div>Absorption Model</div>
            <div> {<select aria-label="select-absorption-method" className={this.state.attenuationMethodError.length > 0 ? 'error' : 'input'} onChange={(e) => { if (e.target.value !== '') { this.setState({ absorptionMethod: e.target.value }); this.setState({ attenuationMethodError: '' }) } }}>
              <option value='0' aria-label="none-value" >Select Absorption Method</option>
              <option value='ITURP67612' aria-label="iturp67612" >ITU-R P.676-12</option>
              <option value='ITURP67611' aria-label="iturp67611"  >ITU-R P.676-11</option>
              <option value='ITURP67610' aria-label="iturp67610" >ITU-R P.676-10</option>
            </select>} <div style={{ fontSize: 12, color: 'red' }}>{this.state.attenuationMethodError}</div>
            </div>
          </div>
          <div>
            <div>Oxygen Specific Attenuation</div>
            <div aria-label="absorption-oxygen-value">{this.props.absorptionOxygen.toFixed(3)} dB</div>
          </div>
          <div>
            <div>Water Vapor Specific Attenuation</div>
            <div aria-label="absorption-water-value">{this.props.absorptionWater.toFixed(3)} dB</div>
          </div>
          <div>
            <div>System Operating Margin</div>
            <div aria-label="system-operating-margin">{this.props.systemOperatingMargin} dB</div>
          </div>
          <div>
            <div>Radio Transmitted Power</div>    
            <div> {<form><input aria-label="site-a-transmitted-power" type="number" style={{ width: 70, height: 15, fontSize: 14 }} onChange={(e) => {if (e.target.value !== '') this.props.UpdateTxPower(e.target.value,null) }}
            >
            </input> dBm </form>} </div>
            <div> {<form><input aria-label="site-b-transmitted-power" type="number" style={{ width: 70, height: 15, fontSize: 14 }} onChange={(e) => { if (e.target.value !== '') this.props.UpdateTxPower(null,e.target.value) }}
            >
            </input>  dBm  </form>} </div>
          </div>
          <div>
            <div>RF Receiver Sensitivity</div>
            <div> {<form><input aria-label="site-a-receiver-sensitivity" type="number" style={{ width: 70, height: 15, fontSize: 14 }} onChange={(e) => { if (e.target.value !== '') this.props.UpdateRxSensitivity(e.target.value, null) }}
            >
            </input> dBm </form>} </div>
            <div> {<form><input aria-label="site-b-receiver-sensitivity" type="number" style={{ width: 70, height: 15, fontSize: 14 }} onChange={(e) => { if (e.target.value !== '') this.props.UpdateRxSensitivity(null, e.target.value) }}
            >
            </input>  dBm  </form>} </div>
          </div>
        </div>
        <div className='antennaContainer'>
          <div>
            <div></div>
            <div className='antennaFont'>Antenna Settings</div>
          </div>
          <div>
            <div>Antenna</div>

            <div> {<select aria-label="site-a-select-antenna" value={this.props.antennaA} style={{ width: 160, height: 22, fontSize: 13 }} className={this.state.antennaTypeError.length > 0 ? 'error' : 'input'} onChange={(e) => { if (e.target.value !== '') { this.props.UpdateAntenas(e.target.value, null); this.setState({ antennaTypeError: '' }) } }}>
              <option value='0' aria-label="none-value" >Select Antenna</option>
              {this.props.antennaList.map(antenna => <option value={antenna}>{antenna}</option>)}

            </select>} <div style={{ fontSize: 12, color: 'red' }}>{this.state.antennaTypeError}</div>
            </div>
            <div> {<select aria-label="site-b-select-antenna" value={this.props.antennaB} style={{ width: 160, height: 22, fontSize: 13 }} className={this.state.antennaTypeError.length > 0 ? 'error' : 'input'} onChange={(e) => { if (e.target.value !== '') { this.props.UpdateAntenas(null, e.target.value); this.setState({ antennaTypeError: '' }) } }}>
              <option value='0' aria-label="none-value" >Select Antenna</option>
              {this.props.antennaList.map(antenna => <option value={antenna}>{antenna}</option>)}

            </select>} <div style={{ fontSize: 12, color: 'red' }}>{this.state.antennaTypeError}</div>
            </div>
          </div>
          <div>
            <div>EIRP</div>
            <div aria-label="site-a-effective-isotropic-radiated-power">{this.props.eirpSiteA} dBm</div>
            <div aria-label="site-b-effective-isotropic-radiated-power">{this.props.eirpSiteB} dBm</div>
          </div>
          
          <div>
            <div>Gain</div>
            <div aria-label="site-a-antenna-gain" > {this.props.antennaGainList[this.props.antennaList.indexOf(this.props.antennaA)]} dBi</div>
            <div aria-label="site-b-antenna-gain">{this.props.antennaGainList[this.props.antennaList.indexOf(this.props.antennaB)]} dBi</div>

          </div>
          <div>
            <div></div>
            <div>{<button aria-label="calculate-button" style={{ color: '#222', fontFamily: 'Arial', boxAlign: 'center', display: 'inline-block', insetInlineStart: '20', alignSelf: 'center' }}
              onClick={(e) => this.buttonHandler()} >Calculate</button>} </div>
          </div>
        </div>


        <ConnectionInfo />


      </div>

    )
  }

}

export default connect(mapProps, mapDispatch)(LinkCalculation);
