import { Action } from "../flux/action";


export class SetWebsocketAction extends Action {
    constructor(public isConnected: boolean) {
        super();
    }
}