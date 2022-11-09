import { Action } from '../../../../framework/src/flux/action';
import { Dispatch } from '../../../../framework/src/flux/store';
import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import { PushAction, ReplaceAction } from "../../../../framework/src/actions/navigationActions";
import { AddErrorInfoAction } from "../../../../framework/src/actions/errorActions";

import { DisplayModeType, DisplaySpecification } from '../handlers/viewDescriptionHandler';
import { restService } from "../services/restServices";
import { YangParser } from "../yang/yangParser";
import { Module } from "../models/yang";
import { ViewSpecification, ViewElement, isViewElementReference, isViewElementList, isViewElementObjectOrList, isViewElementRpc, isViewElementChoise, ViewElementChoiseCase, ViewElementString } from "../models/uiModels";


export class EnableValueSelector extends Action {
  constructor(public listSpecification: ViewSpecification, public listData: any[], public keyProperty: string, public onValueSelected : (value: any) => void ) {
    super();
  }
}

export class SetCollectingSelectionData extends Action {
  constructor(public busy: boolean) {
    super();
  }
}

export class SetSelectedValue extends Action {
  constructor(public value: any) {
    super();
  }
}

export class UpdateDeviceDescription extends Action {
  constructor( public nodeId: string, public modules: { [name:string]: Module}, public views: ViewSpecification[]) {
    super();
  }
}

export class UpdatViewDescription extends Action {
  constructor (public vPath: string, public viewData: any, public displaySpecification: DisplaySpecification = { displayMode: DisplayModeType.doNotDisplay }) {
    super();
  }
}

export class UpdatOutputData extends Action {
  constructor (public outputData: any) {
    super();
  }
}

type HttpResult = {
  status: number;
  message?: string | undefined;
  data: {
      [key: string]: any;
  } | null | undefined;
};

const checkResponseCode = (restResult: HttpResult) =>{
  
  //403 gets handled by the framework from now on

  return restResult.status !== 403 && ( restResult.status < 200 || restResult.status > 299);

}

export const updateNodeIdAsyncActionCreator = (nodeId: string) => async (dispatch: Dispatch, getState: () => IApplicationStoreState ) => {

  dispatch(new UpdateDeviceDescription("", {}, []));
  dispatch(new SetCollectingSelectionData(true));
  
  const { availableCapabilities, unavailableCapabilities, importOnlyModules } = await restService.getCapabilitiesByMountId(nodeId);
  
  if (!availableCapabilities || availableCapabilities.length <= 0) {
    dispatch(new SetCollectingSelectionData(false));
    dispatch(new UpdateDeviceDescription(nodeId, {}, []));
    dispatch(new UpdatViewDescription("", [], {
      displayMode: DisplayModeType.displayAsMessage,
      renderMessage: `NetworkElement : "${nodeId}" has no capabilities.`
    }));
    throw new Error(`NetworkElement : [${nodeId}] has no capabilities.`);
  }

  const parser = new YangParser(unavailableCapabilities || undefined, importOnlyModules || undefined, nodeId);

  for (let i = 0; i < availableCapabilities.length; ++i){
    const capRaw = availableCapabilities[i];
    try {
      await parser.addCapability(capRaw.capability, capRaw.version);
    } catch (err) {
      console.error(`Error in ${capRaw.capability} ${capRaw.version}`, err);
    }
  }

  parser.postProcess();

  dispatch(new SetCollectingSelectionData(false));

  if (process.env.NODE_ENV === "development" ) {
      console.log(parser, parser.modules, parser.views);
  }

  return dispatch(new UpdateDeviceDescription(nodeId, parser.modules, parser.views));
}

export const splitVPath = (vPath: string, vPathParser : RegExp): [string, string?][] => {
  const pathParts: [string, string?][] = [];
  let partMatch: RegExpExecArray | null;
  if (vPath) do {
    partMatch = vPathParser.exec(vPath);
    if (partMatch) {
      pathParts.push([partMatch[1], partMatch[2] || undefined]);
    }
  } while (partMatch)
  return pathParts;
}

const getReferencedDataList = async (refPath: string, dataPath: string, modules: { [name: string]: Module }, views: ViewSpecification[]) => {
  const pathParts = splitVPath(refPath, /(?:(?:([^\/\:]+):)?([^\/]+))/g);  // 1 = opt: namespace / 2 = property
  const defaultNS = pathParts[0][0];
  let referencedModule = modules[defaultNS];

  let dataMember: string;
  let view: ViewSpecification;
  let currentNS: string | null = null;
  let dataUrls = [dataPath];
  let data: any;

  for (let i = 0; i < pathParts.length; ++i) {
    const [pathPartNS, pathPart] = pathParts[i];
    const namespace = pathPartNS != null ? (currentNS = pathPartNS) : currentNS;

    const viewElement = i === 0
      ? views[0].elements[`${referencedModule.name}:${pathPart}`]
      : view!.elements[`${pathPart}`] || view!.elements[`${namespace}:${pathPart}`];

    if (!viewElement) throw new Error(`Could not find ${pathPart} in ${refPath}`);
    if (i < pathParts.length - 1) {
      if (!isViewElementObjectOrList(viewElement)) {
        throw Error(`Module: [${referencedModule.name}].[${viewElement.label}]. Viewelement is not list or object.`);
      }
      view = views[+viewElement.viewId];
      const resultingDataUrls : string[] = [];
      if (isViewElementList(viewElement)) {
        for (let j = 0; j < dataUrls.length; ++j) {
          const dataUrl = dataUrls[j];
          const restResult = (await restService.getConfigData(dataUrl));
          if (restResult.data == null || checkResponseCode(restResult)) {
            const message = restResult.data && restResult.data.errors && restResult.data.errors.error && restResult.data.errors.error[0] && restResult.data.errors.error[0]["error-message"] || "";
            throw new Error(`Server Error. Status: [${restResult.status}]\n${message || restResult.message || ''}`);
          }

          let dataRaw = restResult.data[`${defaultNS}:${dataMember!}`];
          if (dataRaw === undefined) {
              dataRaw = restResult.data[dataMember!];
          }
          dataRaw = dataRaw instanceof Array
            ? dataRaw[0]
            : dataRaw;

          data = dataRaw && dataRaw[viewElement.label] || [];
          const keys: string[] = data.map((entry: { [key: string]: any } )=> entry[viewElement.key!]);
          resultingDataUrls.push(...keys.map(key => `${dataUrl}/${viewElement.label.replace(/\//ig, "%2F")}=${key.replace(/\//ig, "%2F")}`));
        }
        dataMember = viewElement.label;
      } else {
        // just a member, not a list
        const pathSegment = (i === 0
          ? `/${referencedModule.name}:${viewElement.label.replace(/\//ig, "%2F")}`
          : `/${viewElement.label.replace(/\//ig, "%2F")}`);
        resultingDataUrls.push(...dataUrls.map(dataUrl => dataUrl + pathSegment));
        dataMember = viewElement.label;
      }
      dataUrls = resultingDataUrls;
    } else {
      data = [];
      for (let j = 0; j < dataUrls.length; ++j) {
        const dataUrl = dataUrls[j];
        const restResult = (await restService.getConfigData(dataUrl));
        if (restResult.data == null || checkResponseCode(restResult)) {
          const message = restResult.data && restResult.data.errors && restResult.data.errors.error && restResult.data.errors.error[0] && restResult.data.errors.error[0]["error-message"] || "";
          throw new Error(`Server Error. Status: [${restResult.status}]\n${message || restResult.message || ''}`);
        }
        let dataRaw = restResult.data[`${defaultNS}:${dataMember!}`];
        if (dataRaw === undefined) {
            dataRaw = restResult.data[dataMember!];
        }
        dataRaw = dataRaw instanceof Array
          ? dataRaw[0]
          : dataRaw;
        data.push(dataRaw);
      }
      // BUG UUID ist nicht in den elements enthalten !!!!!!
      const key = viewElement && viewElement.label || pathPart;
      return {
        view: view!,
        data: data,
        key: key,
      };
    }
  }
  return null;
}

const resolveViewDescription = (defaultNS: string | null, vPath: string, view: ViewSpecification): ViewSpecification =>{

  // check if-feature | when | and resolve all references.
  view = { ...view };
  view.elements = Object.keys(view.elements).reduce<{ [name: string]: ViewElement }>((acc, cur) => {
    const resolveHistory : ViewElement[] = [];  
    let elm = view.elements[cur];
    const key = defaultNS && cur.replace(new RegExp(`^${defaultNS}:`, "i"),"") || cur;
    while (isViewElementReference(elm)) {
      const result = (elm.ref(vPath));  
      if (result) {
        const [referencedElement, referencedPath] = result;
        if (resolveHistory.some(hist => hist === referencedElement)) {
            console.error(`Circle reference found at: ${vPath}`, resolveHistory);
            break;
        }
        elm = referencedElement;
        vPath = referencedPath;
        resolveHistory.push(elm);
      }
    } 
    
    acc[key] = { ...elm, id: key };
    
    return acc;
  }, {});
  return view;
}

const flatenViewElements = (defaultNS: string | null, parentPath: string, elements: { [name: string]: ViewElement }, views: ViewSpecification[], currentPath: string ): { [name: string]: ViewElement } => {
  if (!elements) return {};
  return Object.keys(elements).reduce<{ [name: string]: ViewElement }>((acc, cur) => {
    const elm = elements[cur];

    // remove the detault namespace, and only the default namespace, sine it seems that this is also not in the restconf response
    const elmKey = defaultNS && elm.id.replace(new RegExp(`^${defaultNS}:`, "i"), "") || elm.id;
    const key = parentPath ? `${parentPath}.${elmKey}` : elmKey;

    if (isViewElementRpc(elm)) {
      console.warn(`Flaten of RFC not supported ! [${currentPath}][${elm.label}]`);
      return acc;
    } else if (isViewElementObjectOrList(elm)) {
      const view = views[+elm.viewId];
      const inner = view && flatenViewElements(defaultNS, key, view.elements, views, `${currentPath}/${view.name}`);
      inner && Object.keys(inner).forEach(k => (acc[k] = inner[k]));
    } else if (isViewElementChoise(elm)) {
     acc[key] = {
        ...elm,
        id: key,
        cases: Object.keys(elm.cases).reduce<{ [name: string]: ViewElementChoiseCase }>((accCases, curCases) => {
          const caseElement = elm.cases[curCases];
          accCases[curCases] = {
            ...caseElement,
            // Hint: do not use key it contains elmKey, which shell be omitted for cases.
            elements: flatenViewElements(defaultNS, /*key*/ parentPath, caseElement.elements, views, `${currentPath}/${elm.label}`)
          };
          return accCases;
        }, {}),
      };
    } else {
      acc[key] = {
        ...elm,
        id: key,
      };
    }
    return acc;
  }, {});
};

export const updateViewActionAsyncCreator = (vPath: string) => async (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
  const pathParts = splitVPath(vPath, /(?:([^\/\["]+)(?:\[([^\]]*)\])?)/g); // 1 = property / 2 = optional key
  const { configuration: { deviceDescription: { nodeId, modules, views } }, framework: { navigationState } } = getState();
  let dataPath = `/rests/data/network-topology:network-topology/topology=topology-netconf/node=${nodeId}/yang-ext:mount`;

  let inputViewSpecification: ViewSpecification | undefined = undefined;
  let outputViewSpecification: ViewSpecification | undefined = undefined;

  let viewSpecification: ViewSpecification = views[0];
  let viewElement: ViewElement;

  let dataMember: string;
  let extractList: boolean = false;

  let currentNS: string | null = null;
  let defaultNS: string | null = null;

  dispatch(new SetCollectingSelectionData(true));
  try {
    for (let ind = 0; ind < pathParts.length; ++ind) {
      const [property, key] = pathParts[ind];
      const namespaceInd = property && property.indexOf(":") || -1;
      const namespace: string | null = namespaceInd > -1 ? (currentNS = property.slice(0, namespaceInd)) : currentNS;

      if (ind === 0) { defaultNS = namespace };

      viewElement = viewSpecification.elements[property] || viewSpecification.elements[`${namespace}:${property}`];
      if (!viewElement) throw Error("Property [" + property + "] does not exist.");

      if (viewElement.isList && !key) {
        if (pathParts.length - 1 > ind) {
          dispatch(new SetCollectingSelectionData(false));
          throw new Error("No key for list [" + property + "]");
        } else if (vPath.endsWith("[]") && pathParts.length - 1 === ind) {

          // empty key is used for new element
          if (viewElement && "viewId" in viewElement) viewSpecification = views[+viewElement.viewId];
          const data = Object.keys(viewSpecification.elements).reduce<{ [name: string]: any }>((acc, cur) => {
            const elm = viewSpecification.elements[cur];
            if (elm.default) {
              acc[elm.id] = elm.default || ""
            }
            return acc;
          }, {});

          // create display specification
          const ds: DisplaySpecification = {
            displayMode: DisplayModeType.displayAsObject,
            viewSpecification: resolveViewDescription(defaultNS, vPath, viewSpecification),
            keyProperty: isViewElementList(viewElement!) && viewElement.key || undefined
          };

          // update display specification
          return dispatch(new UpdatViewDescription(vPath, data, ds));
        }
        if (viewElement && isViewElementList(viewElement) && viewSpecification.parentView === "0") {
          // check if there is a reference as key
          const listSpecification = views[+viewElement.viewId];
          const keyElement = viewElement.key && listSpecification.elements[viewElement.key];
          if (keyElement && isViewElementReference(keyElement)) {
            const refList = await getReferencedDataList(keyElement.referencePath, dataPath, modules, views);
            if (!refList) {
              throw new Error(`Could not find refList for [${keyElement.referencePath}].`);
            }
            if (!refList.key) {
              throw new Error(`Key property not found for [${keyElement.referencePath}].`);
            }
            dispatch(new EnableValueSelector(refList.view, refList.data, refList.key, (refKey) => {
              window.setTimeout(() => dispatch(new PushAction(`${vPath}[${refKey.replace(/\//ig, "%2F")}]`)));
            }));
          } else {
            // Found a list at root level of a module w/o a refenrece key.
            dataPath += `?content=config&fields=${encodeURIComponent(viewElement.id)}(${encodeURIComponent(viewElement.key || '')})`; 
            const restResult = (await restService.getConfigData(dataPath));
            if (restResult && restResult.status === 200 && restResult.data && restResult.data[viewElement.id] ){
                // spoof the not existing view here
                const refData = restResult.data[viewElement.id];
                const refView : ViewSpecification  = {
                    id: "-1",
                    canEdit: false,
                    config: false,
                    language: "en-US",
                    elements: {
                        [viewElement.key!] : { 
                           uiType: "string",
                           config: false,
                           id: viewElement.key,
                           label: viewElement.key,
                           isList: true,
                        } as ViewElementString
                    }
                };
                dispatch(new EnableValueSelector(refView, refData, viewElement.key!, (refKey) => {
                 window.setTimeout(() => dispatch(new PushAction(`${vPath}[${refKey.replace(/\//ig, "%2F")}]`))); 
                }));
            } else {
              throw new Error("Found a list at root level of a module and could not determine the keys.");
            }
            dispatch(new SetCollectingSelectionData(false));
          }
          return;
        }
        extractList = true;
      } else {
        // replaces unicode %2C if present
        dataPath += `/${property}${key ? `=${key.replace(/\%2C/g, ",").replace(/\//ig, "%2F")}` : ""}`;

        // in case of the root element the required namespace will be added later,
        // while extracting the data
        dataMember = namespace === defaultNS
          ? viewElement.label
          : `${namespace}:${viewElement.label}`;
        extractList = false;
      }

      if (viewElement && "viewId" in viewElement) {
        viewSpecification = views[+viewElement.viewId];
      } else if (viewElement.uiType === "rpc") {
        viewSpecification = views[+(viewElement.inputViewId || 0)];

        // create new instance & flaten
        inputViewSpecification = viewElement.inputViewId != null && {
          ...views[+(viewElement.inputViewId || 0)],
          elements: flatenViewElements(defaultNS, "", views[+(viewElement.inputViewId || 0)].elements, views, viewElement.label),
        } || undefined;
        outputViewSpecification = viewElement.outputViewId != null && {
          ...views[+(viewElement.outputViewId || 0)],
          elements: flatenViewElements(defaultNS, "", views[+(viewElement.outputViewId || 0)].elements, views, viewElement.label),
        } || undefined;

      }
    }

    let data: any = {};
    // do not get any data from netconf if there is no view specified || this is the root element [0] || this is an rpc
    if (viewSpecification && !(viewSpecification.id === "0" || viewElement!.uiType === "rpc")) {
      const restResult = (await restService.getConfigData(dataPath));
      if (!restResult.data) {
        // special case: if this is a list without any response
        if (extractList && restResult.status === 404) {
          if (!isViewElementList(viewElement!)) {
            throw new Error(`vPath: [${vPath}]. ViewElement has no key.`);
          }
          // create display specification
          const ds: DisplaySpecification = {
            displayMode: extractList ? DisplayModeType.displayAsList : DisplayModeType.displayAsObject,
            viewSpecification: resolveViewDescription(defaultNS, vPath, viewSpecification),
            keyProperty: viewElement.key
          };

          // update display specification
          return dispatch(new UpdatViewDescription(vPath, [], ds));
        }
        throw new Error(`Did not get response from Server. Status: [${restResult.status}]`);
      } else if (checkResponseCode(restResult)) {
        const message = restResult.data.errors && restResult.data.errors.error && restResult.data.errors.error[0] && restResult.data.errors.error[0]["error-message"] || "";
        throw new Error(`Server Error. Status: [${restResult.status}]\n${message}`);
      } else {
        // https://tools.ietf.org/html/rfc7951#section-4 the root element may countain a namesapce or not !  
        data = restResult.data[`${defaultNS}:${dataMember!}`];
        if (data === undefined) {
           data = restResult.data[dataMember!]; // extract dataMember w/o namespace
        }
      }

      // extract the first element list[key]
      data = data instanceof Array
        ? data[0]
        : data;

      // extract the list -> key: list
      data = extractList
        ? data[viewElement!.id] || data[viewElement!.label] || [] // if the list is empty, it does not exist
        : data;

    } else if (viewElement! && viewElement!.uiType === "rpc") {
      // set data to defaults
      data = {};
      inputViewSpecification && Object.keys(inputViewSpecification.elements).forEach(key => {
        const elm = inputViewSpecification && inputViewSpecification.elements[key];
        if (elm && elm.default != undefined) {
          data[elm.id] = elm.default;
        }
      });
    }
    
    // create display specification
    const ds: DisplaySpecification = viewElement! && viewElement!.uiType === "rpc"
      ? {
        dataPath,
        displayMode: DisplayModeType.displayAsRPC,
        inputViewSpecification: inputViewSpecification && resolveViewDescription(defaultNS, vPath, inputViewSpecification),
        outputViewSpecification: outputViewSpecification && resolveViewDescription(defaultNS, vPath, outputViewSpecification),
      }
      : {
        dataPath,
        displayMode: extractList ? DisplayModeType.displayAsList : DisplayModeType.displayAsObject,
        viewSpecification: resolveViewDescription(defaultNS, vPath, viewSpecification),
        keyProperty: isViewElementList(viewElement!) && viewElement.key || undefined,
        apidocPath:  isViewElementList(viewElement!) &&  `/apidoc/explorer/index.html?urls.primaryName=$$$standard$$$#/mounted%20${nodeId}%20${viewElement!.module || 'MODULE_NOT_DEFINED'}/$$$action$$$_${dataPath.replace(/^\//,'').replace(/[\/=\-\:]/g,'_')}_${viewElement! != null ? `${viewElement.id.replace(/[\/=\-\:]/g,'_')}_` : '' }` || undefined,
      };

    // update display specification
    return dispatch(new UpdatViewDescription(vPath, data, ds));
    // https://beta.just-run.it/#/configuration/Sim12600/core-model:network-element/ltp[LTP-MWPS-TTP-01]
    // https://beta.just-run.it/#/configuration/Sim12600/core-model:network-element/ltp[LTP-MWPS-TTP-01]/lp
  } catch (error) {
    history.back();
    dispatch(new AddErrorInfoAction({ title: "Problem", message: error.message || `Could not process ${dataPath}` }));
    dispatch(new SetCollectingSelectionData(false));
  } finally {
    return;
  }
};

export const updateDataActionAsyncCreator = (vPath: string, data: any) => async (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
  const pathParts = splitVPath(vPath, /(?:([^\/\["]+)(?:\[([^\]]*)\])?)/g); // 1 = property / 2 = optional key
  const { configuration: { deviceDescription: { nodeId, views } } } = getState();
  let dataPath = `/rests/data/network-topology:network-topology/topology=topology-netconf/node=${nodeId}/yang-ext:mount`;
  let viewSpecification: ViewSpecification = views[0];
  let viewElement: ViewElement;
  let dataMember: string;
  let embedList: boolean = false;
  let isNew: string | false = false;

  let currentNS: string | null = null;
  let defaultNS: string | null = null;

  dispatch(new SetCollectingSelectionData(true));
  try {
    for (let ind = 0; ind < pathParts.length; ++ind) {
      let [property, key] = pathParts[ind];
      const namespaceInd = property && property.indexOf(":") || -1;
      const namespace: string | null = namespaceInd > -1 ? (currentNS = property.slice(0, namespaceInd)) : currentNS;

      if (ind === 0) { defaultNS = namespace };
      viewElement = viewSpecification.elements[property] || viewSpecification.elements[`${namespace}:${property}`];
      if (!viewElement) throw Error("Property [" + property + "] does not exist.");

      if (isViewElementList(viewElement) && !key) {
        embedList = true;
        if (viewElement && viewElement.isList && viewSpecification.parentView === "0") {
          throw new Error("Found a list at root level of a module w/o a refenrece key.");
        }
        if (pathParts.length - 1 > ind) {
          dispatch(new SetCollectingSelectionData(false));
          throw new Error("No key for list [" + property + "]");
        } else if (vPath.endsWith("[]") && pathParts.length - 1 === ind) {
          // handle new element with any number of arguments
          let keyList = viewElement.key?.split(" ");
          let dataPathParam = keyList?.map(id => data[id]).join(",");
          key = viewElement.key && String(dataPathParam) || "";
          isNew = key;
          if (!key) {
            dispatch(new SetCollectingSelectionData(false));
            throw new Error("No value for key [" + viewElement.key + "] in list [" + property + "]");
          }
        }
      }

      dataPath += `/${property}${key ? `=${key.replace(/\//ig, "%2F")}` : ""}`;
      dataMember = viewElement.label;
      embedList = false;

      if (viewElement && "viewId" in viewElement) {
        viewSpecification = views[+viewElement.viewId];
      }
    }

    // remove read-only elements
    const removeReadOnlyElements = (viewSpecification: ViewSpecification, isList: boolean, data: any) => {
      if (isList) {
        return data.map((elm : any) => removeReadOnlyElements(viewSpecification, false, elm));
      } else {
        return Object.keys(data).reduce<{[key: string]: any}>((acc, cur)=>{
          const [nsOrName, name] = cur.split(':',1);
          const element = viewSpecification.elements[cur] || viewSpecification.elements[nsOrName] || viewSpecification.elements[name];
          if (!element && process.env.NODE_ENV === "development" ) {
            throw new Error("removeReadOnlyElements: Could not determine elment for data.");
          }
          if (element && element.config) {
            if (element.uiType==="object") {
              const view = views[+element.viewId];
              if (!view) {
                throw new Error("removeReadOnlyElements: Internal Error could not determine viewId: "+element.viewId);
              }
              acc[cur] = removeReadOnlyElements(view, element.isList != null && element.isList, data[cur]);
            } else {
              acc[cur] = data[cur];
            }
          }
          return acc;
        }, {});
      }
    };
    data = removeReadOnlyElements(viewSpecification, embedList, data);


    // embed the list -> key: list
    data = embedList
      ? { [viewElement!.label]: data }
      : data;

    // embed the first element list[key]
    data = isNew
      ? [data]
      : data;

    // do not extract root member (0)
    if (viewSpecification && viewSpecification.id !== "0") {
      const updateResult = await restService.setConfigData(dataPath, { [`${currentNS}:${dataMember!}`]: data }); // addDataMember using currentNS
      if (checkResponseCode(updateResult)) {
        const message = updateResult.data && updateResult.data.errors && updateResult.data.errors.error && updateResult.data.errors.error[0] && updateResult.data.errors.error[0]["error-message"] || "";
        throw new Error(`Server Error. Status: [${updateResult.status}]\n${message || updateResult.message || ''}`);
      }
    }

    if (isNew) {
      return dispatch(new ReplaceAction(`/configuration/${nodeId}/${vPath.replace(/\[\]$/i, `[${isNew}]`)}`)); // navigate to new element
    }

    // create display specification
    const ds: DisplaySpecification = {
      displayMode: embedList ? DisplayModeType.displayAsList : DisplayModeType.displayAsObject,
      viewSpecification: resolveViewDescription(defaultNS, vPath, viewSpecification),
      keyProperty: isViewElementList(viewElement!) && viewElement.key || undefined,
    };

    // update display specification
    return dispatch(new UpdatViewDescription(vPath, data, ds));
  } catch (error) {
    history.back();
    dispatch(new AddErrorInfoAction({ title: "Problem", message: error.message || `Could not change ${dataPath}` }));

  } finally {
    dispatch(new SetCollectingSelectionData(false));
    return;
  }
};

export const removeElementActionAsyncCreator = (vPath: string) => async (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
  const pathParts = splitVPath(vPath, /(?:([^\/\["]+)(?:\[([^\]]*)\])?)/g); // 1 = property / 2 = optional key
  const { configuration: { deviceDescription: { nodeId, views } } } = getState();
  let dataPath = `/rests/data/network-topology:network-topology/topology=topology-netconf/node=${nodeId}/yang-ext:mount`;
  let viewSpecification: ViewSpecification = views[0];
  let viewElement: ViewElement;

  let currentNS: string | null = null;
  let defaultNS: string | null = null;

  dispatch(new SetCollectingSelectionData(true));
  try {
    for (let ind = 0; ind < pathParts.length; ++ind) {
      let [property, key] = pathParts[ind];
      const namespaceInd = property && property.indexOf(":") || -1;
      const namespace: string | null = namespaceInd > -1 ? (currentNS = property.slice(0, namespaceInd)) : currentNS;

      if (ind === 0) { defaultNS = namespace };
      viewElement = viewSpecification.elements[property] || viewSpecification.elements[`${namespace}:${property}`];
      if (!viewElement) throw Error("Property [" + property + "] does not exist.");

      if (isViewElementList(viewElement) && !key) {
        if (viewElement && viewElement.isList && viewSpecification.parentView === "0") {
          throw new Error("Found a list at root level of a module w/o a refenrece key.");
        }
        if (pathParts.length - 1 > ind) {
          dispatch(new SetCollectingSelectionData(false));
          throw new Error("No key for list [" + property + "]");
        } else if (vPath.endsWith("[]") && pathParts.length - 1 === ind) {
          // remove the whole table
        }
      }

      dataPath += `/${property}${key ? `=${key.replace(/\//ig, "%2F")}` : ""}`;

      if (viewElement && "viewId" in viewElement) {
        viewSpecification = views[+viewElement.viewId];
      } else if (viewElement.uiType === "rpc") {
        viewSpecification = views[+(viewElement.inputViewId || 0)];
      }
    }

    const updateResult = await restService.removeConfigElement(dataPath);
    if (checkResponseCode(updateResult)) {
      const message = updateResult.data && updateResult.data.errors && updateResult.data.errors.error && updateResult.data.errors.error[0] && updateResult.data.errors.error[0]["error-message"] || "";
      throw new Error(`Server Error. Status: [${updateResult.status}]\n${message || updateResult.message || ''}`);
    }
  } catch (error) {
    dispatch(new AddErrorInfoAction({ title: "Problem", message: error.message || `Could not remove ${dataPath}` }));
  } finally {
    dispatch(new SetCollectingSelectionData(false));
  }


};

export const executeRpcActionAsyncCreator = (vPath: string, data: any) => async (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
  const pathParts = splitVPath(vPath, /(?:([^\/\["]+)(?:\[([^\]]*)\])?)/g); // 1 = property / 2 = optional key
  const { configuration: { deviceDescription: { nodeId, views }, viewDescription: oldViewDescription } } = getState();
  let dataPath = `/rests/operations/network-topology:network-topology/topology=topology-netconf/node=${nodeId}/yang-ext:mount`;
  let viewSpecification: ViewSpecification = views[0];
  let viewElement: ViewElement;
  let dataMember: string;
  let embedList: boolean = false;
  let isNew: string | false = false;

  let currentNS: string | null = null;
  let defaultNS: string | null = null;

  dispatch(new SetCollectingSelectionData(true));
  try {
    for (let ind = 0; ind < pathParts.length; ++ind) {
      let [property, key] = pathParts[ind];
      const namespaceInd = property && property.indexOf(":") || -1;
      const namespace: string | null = namespaceInd > -1 ? (currentNS = property.slice(0, namespaceInd)) : currentNS;

      if (ind === 0) { defaultNS = namespace };
      viewElement = viewSpecification.elements[property] || viewSpecification.elements[`${namespace}:${property}`];
      if (!viewElement) throw Error("Property [" + property + "] does not exist.");

      if (isViewElementList(viewElement) && !key) {
        embedList = true;
      //   if (viewElement && viewElement.isList && viewSpecification.parentView === "0") {
      //     throw new Error("Found a list at root level of a module w/o a refenrece key.");
      //   }
      //   if (pathParts.length - 1 > ind) {
      //     dispatch(new SetCollectingSelectionData(false));
      //     throw new Error("No key for list [" + property + "]");
      //   } else if (vPath.endsWith("[]") && pathParts.length - 1 === ind) {
      //     // handle new element
      //     key = viewElement.key && String(data[viewElement.key]) || "";
      //     isNew = key;
      //     if (!key) {
      //       dispatch(new SetCollectingSelectionData(false));
      //       throw new Error("No value for key [" + viewElement.key + "] in list [" + property + "]");
      //     }
      //   }
      }

      dataPath += `/${property}${key ? `=${key.replace(/\//ig, "%2F")}` : ""}`;
      dataMember = viewElement.label;
      embedList = false;

      if (viewElement && "viewId" in viewElement) {
        viewSpecification = views[+viewElement.viewId];
      } else if (viewElement.uiType === "rpc") {
        viewSpecification = views[+(viewElement.inputViewId || 0)];
      }
    }

    // re-inflate formerly flatten rpc data
    data = data && Object.keys(data).reduce < { [name: string ]: any }>((acc, cur) => {
      const pathParts = cur.split(".");
      let pos = 0;
      const updatePath = (obj: any, key: string) => {
        obj[key] = (pos >= pathParts.length)
          ? data[cur]
          : updatePath(obj[key] || {}, pathParts[pos++]);
        return obj;
      }
      updatePath(acc, pathParts[pos++]);
      return acc;
    }, {}) || null;

    // embed the list -> key: list
    data = embedList
      ? { [viewElement!.label]: data }
      : data;

    // embed the first element list[key]
    data = isNew
      ? [data]
      : data;

    // do not post root member (0)
    if ((viewSpecification && viewSpecification.id !== "0") || (dataMember! && !data)) {
      const updateResult = await restService.executeRpc(dataPath, { [`${defaultNS}:input`]: data || {} });
      if (checkResponseCode(updateResult)) {
        const message = updateResult.data && updateResult.data.errors && updateResult.data.errors.error && updateResult.data.errors.error[0] && updateResult.data.errors.error[0]["error-message"] || "";
        throw new Error(`Server Error. Status: [${updateResult.status}]\n${message || updateResult.message || ''}`);
      }
      dispatch(new UpdatOutputData(updateResult.data));
    } else {
      throw new Error(`There is NO RPC specified.`);
    }


    // // update display specification
    // return
  } catch (error) {
    dispatch(new AddErrorInfoAction({ title: "Problem", message: error.message || `Could not change ${dataPath}` }));

  } finally {
    dispatch(new SetCollectingSelectionData(false));
  }
};
