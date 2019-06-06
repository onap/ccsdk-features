import { FaultApplicationBaseAction } from './notificationActions';
import { getFaultStateFromDatabase } from '../services/faultStatusService';
import { Dispatch } from '../../../../framework/src/flux/store';


export class SetFaultStatusAction extends FaultApplicationBaseAction {
  constructor (public criticalFaults: number, public majorFaults: number, public minorFaults: number, public warnings: number) {
    super();
  }
}


export const refreshFaultStatusAsyncAction = async (dispatch: Dispatch ) => {
  const result = await getFaultStateFromDatabase().catch(_=>null);
  if (result) {
    const statusAction = new SetFaultStatusAction(
      result["Critical"] || 0,
      result["Major"] || 0,
      result["Minor"] || 0,
      result["Warning"] || 0
    );
    dispatch(statusAction);
    return;
  }
  dispatch(new SetFaultStatusAction(0, 0, 0, 0));
}
