import { Action } from "../../../../framework/src/flux/action";
import { currentViewType } from "../models/toggleDataType";


export class SetSubViewAction extends Action {
    constructor(public currentView: currentViewType, public selectedTab: "chart" | "table") {
        super();
    }
}

export class ResetAllSubViewsAction extends Action {
    constructor() {
        super();
    }
}