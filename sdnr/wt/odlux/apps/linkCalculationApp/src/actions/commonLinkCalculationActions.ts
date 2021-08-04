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

import { Action } from "../../../../framework/src/flux/action";
import { Dispatch } from "../../../../framework/src/flux/store";
import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";


export class UpdateLinkIdAction extends Action {
    constructor(public linkId: string) {
        super();
    }
}

export class UpdateFrequencyAction extends Action {
    constructor(public frequency: number) {
        super();
    }
}
export class UpdateSiteAction extends Action {
    constructor(
        public siteA?: any,
        public siteB?: any
    ) {
        super();
    }
}
export class UpdateRainAttAction extends Action {

    constructor(public rainAtt: number) {
        super();
    }
}
export class UpdateRainValAction extends Action {
    constructor(public rainVal: number) {
        super();
    }
}

export class updateHideForm extends Action {
    constructor(public formView: boolean) {
        super();
    }
}
export class UpdateDistanceAction extends Action {
    constructor(public distance: number) {
        super();
    }
}

export class UpdateFslCalculation extends Action {
    constructor(public fsl: number) {
        super();
    }
}


export class UpdateLatLonAction extends Action {
    constructor(
        public Lat1: number,
        public Lon1: number,
        public Lat2: number,
        public Lon2: number
    ) {
        super();

    }
}
export class UpdatePolAction extends Action {
    constructor(public polarization: string) {
        super();
    }
}
export class isCalculationServerReachableAction extends Action {
    constructor(public reachable: boolean) {
        super();
    }
}
export class updateAltitudeAction extends Action {
    constructor(
        public amslA: number,
        public aglA: number,
        public amslB: number,
        public aglB: number
    ) {
        super();
    }
}
export class UpdateAbsorptionLossAction extends Action {
    constructor(
        public absorptionOxygen: number,
        public absorptionWater: number,

    ) {
        super();
    }
}
export class UpdateWorstMonthRainAction extends Action {
    constructor(public month: string) {
        super();
    }
}

export class UpdateEIRPAction extends Action {
    constructor(public eirpA: number,public eirpB: number) {
        super();
    }
}
export class UpdateAntennaGainAction extends Action {
    constructor(public antennaGainList: string[]) {
        super();
    }
}
export class UpdateAntennaListAction extends Action {
    constructor(public antennaList: string[]) {
        super();
    }
}
export class UpdateAntennaAction extends Action {
    constructor(public antennaA: string | null, public antennaB : string | null) {
        super();
    }
}
export class UpdateRadioAttributesAction extends Action {
    constructor(public som: number , public eirpA : number, public eirpB : number) {
        super();
    }
}
export class UpdateTxPowerAction extends Action {
    constructor(public txPowerA: string | null , public txPowerB : string | null) {
        super();
    }
}
export class UpdateRxSensitivityAction extends Action {
    constructor(public rxSensitivityA: string | null , public rxSensitivityB : string | null) {
        super();
    }
}


export const updateAntennaList = (frequency: number) => async (dispatcher: Dispatch, getState: () => IApplicationStoreState) => {
    let antennaList: string[] = []
    let antennaDiameterList: string[] = []
    let antennaGainList :string[] =[]
    //switch case here     frequency = 26? antennaList.push
    switch (frequency) {
        case 7: antennaList.push('ANDREW VHLPX2.5-7W', 'ANDREW VHLPX3-7W', 'ANDREW VHLPX4-7W', 'ANDREW VHLPX6-7W' ), antennaDiameterList.push('0.6','0.9','1.2','1.8'), antennaGainList.push('33.90','35.50','37.30','40.61'); break
        case 11: antennaList.push('ANDREW VHLPX2-11W', 'ANDREW VHLPX3-11W', 'ANDREW VHLPX4-11W'), antennaDiameterList.push('0.6','0.9','1.2'), antennaGainList.push('34.50','38.4','40.70');break
        case 15: antennaList.push('ANDREW VHLPX1-15', 'ANDREW VHLPX2-15', 'ANDREW VHLPX3-15', 'ANDREW VHLPX4-15'), antennaDiameterList.push('0.3','0.6','0.9','1.2'), antennaGainList.push('32.00','36.80','41.11','42.90');break
        case 23: antennaList.push('ANDREW VHLPX1-23', 'ANDREW VHLPX2-23', 'ANDREW VHLPX3-23', 'ANDREW VHLPX4-23'), antennaDiameterList.push('0.3','0.6','0.9','1.2'), antennaGainList.push('35.30','40.21','44.80','46.71');break
        case 26: antennaList.push('ANDREW VHLPX1-26', 'ANDREW VHLPX2-26', 'ANDREW VHLPX3-26'), antennaDiameterList.push('0.3','0.6','0.9'), antennaGainList.push('36.61','40.21','41.21','45.80');break
        case 28: antennaList.push('ANDREW VHLPX1-28', 'ANDREW VHLPX2-28'), antennaDiameterList.push('0.3','0.6'), antennaGainList.push('38.11','42.21');break
        case 38: antennaList.push('ANDREW VHLPX1-38', 'ANDREW VHLPX2-38'), antennaDiameterList.push('0.3','0.6'), antennaGainList.push('40.11','45.21');break
        case 42: antennaList.push('ANDREW VHLPX1-42-XXX/D', 'ANDREW VHLPX2-42-XXX/A'), antennaDiameterList.push('0.3','0.6'), antennaGainList.push('40.80','46.00');break
        case 80: antennaList.push('Radio Waves HPCPE-80', 'Radio Waves HPLP2-80'), antennaDiameterList.push('0.3','0.6'), antennaGainList.push('43.80','50.80');break
    }
    dispatcher(new UpdateAntennaListAction(antennaList))
    dispatcher(new UpdateAntennaGainAction(antennaGainList))
}

