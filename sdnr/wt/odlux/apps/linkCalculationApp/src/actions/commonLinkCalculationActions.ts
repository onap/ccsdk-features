import { Action } from "../../../../framework/src/flux/action";
import { Dispatch } from "../../../../framework/src/flux/store";
import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";


export class UpdateLinkIdAction extends Action{
    constructor(public linkId: string){
        super();
    }
}

export class UpdateFrequencyAction extends Action{
    constructor(public frequency: number){
        super();
    }
}
export class UpdateSiteAction extends Action{
    constructor(
        public siteA?: any,
        public siteB?: any
         ){
        super();
    }
}
export class UpdateRainAttAction extends Action{
    
    constructor(public rainAtt: number){
        super();
    }
}
export class UpdateRainValAction extends Action{
    constructor(public rainVal: number){
        super();
    }
}

export class updateHideForm extends Action{
    constructor(public formView: boolean){
        super();
    }
}
export class UpdateDistanceAction extends Action{
    constructor(public distance: number){
        super();
    }
}

export class UpdateFslCalculation extends Action{
    constructor(public fsl: number){
        super();
    }
}


export class UpdateLatLonAction extends Action{
    constructor(
        public Lat1: number,
        public Lon1:number,
        public Lat2: number, 
        public Lon2: number
        ){
        super();
        
    }
}
export class isCalculationServerReachableAction extends Action{
    constructor(public reachable: boolean){
        super();
    }
}

// export const checkCalculationsServerConnectivityAction = (callback: Promise<any>) => (dispatcher: Dispatch, getState: () => IApplicationStoreState)=>{
//     callback
//     .then(res =>{ 
//         const {linkCalculation:{calculations: {isCalculationServerAvailable}}} = getState();
//         if(!isToplogyServerAvailable){
//             dispatcher(new IsTopologyServerReachableAction(true))
//         }
//     })
//     .catch(error=>{
//         const {network:{connectivity: {isToplogyServerAvailable}}} = getState();
//         if(isToplogyServerAvailable){
//            dispatcher(new IsTopologyServerReachableAction(false))
//         }
//     })
// }
