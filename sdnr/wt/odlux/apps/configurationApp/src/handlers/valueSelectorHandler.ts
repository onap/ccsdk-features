import { IActionHandler } from "../../../../framework/src/flux/action";
import { ViewSpecification } from "../models/uiModels";
import { EnableValueSelector, SetSelectedValue, UpdateDeviceDescription, SetCollectingSelectionData, UpdatViewDescription } from "../actions/deviceActions";

export interface IValueSelectorState {
  collectingData: boolean;
  keyProperty: string | undefined;
  listSpecification: ViewSpecification | null;
  listData: any[];
  onValueSelected: (value: any) => void;
}

const nc = (val: React.SyntheticEvent) => { };
const valueSelectorStateInit: IValueSelectorState = {
  collectingData: false,
  keyProperty: undefined,
  listSpecification: null,
  listData: [],
  onValueSelected: nc,
};

export const valueSelectorHandler: IActionHandler<IValueSelectorState> = (state = valueSelectorStateInit, action) => {
  if (action instanceof SetCollectingSelectionData) {
    state = {
      ...state,
     collectingData: action.busy,
    };
  } else if (action instanceof EnableValueSelector) {
    state = {
      ...state,
      collectingData: false,
      keyProperty: action.keyProperty,
      listSpecification: action.listSpecification,
      onValueSelected: action.onValueSelected,
      listData: action.listData,
    };
  } else if (action instanceof SetSelectedValue) {
    state.keyProperty && state.onValueSelected(action.value[state.keyProperty]);
    state = {
      ...state,
      collectingData: false,
      keyProperty: undefined,
      listSpecification: null,
      onValueSelected: nc,
      listData: [],
    };
  } else if (action instanceof UpdateDeviceDescription || action instanceof UpdatViewDescription) {
    state = {
      ...state,
      collectingData: false,
      keyProperty: undefined,
      listSpecification: null,
      onValueSelected: nc,
      listData: [],
    };
  }
  return state;
};
