import { Action } from '../../../../framework/src/flux/action';
import { Dispatch } from '../../../../framework/src/flux/store';
import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import { PushAction, ReplaceAction } from "../../../../framework/src/actions/navigationActions";

import { restService } from "../services/restServices";
import { YangParser } from "../yang/yangParser";
import { Module } from "../models/yang";
import { ViewSpecification, ViewElement, isViewElementReference, isViewElementList, isViewElementObjectOrList } from "../models/uiModels";
import { AddErrorInfoAction } from "../../../../framework/src/actions/errorActions";

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
  constructor(public vPath: string, public view: ViewSpecification, public viewData: any, public displayAsList: boolean = false, public key?: string ) {
    super();
  }
}

export const updateNodeIdAsyncActionCreator = (nodeId: string) => async (dispatch: Dispatch, getState: () => IApplicationStoreState ) => {
  const { configuration: { connectedNetworkElements : { rows }} } = getState();
  dispatch(new SetCollectingSelectionData(true));
  const networkElement = rows.find(r => r.nodeId === nodeId) || await restService.getMountedNetworkElementByMountId(nodeId);
  if (!networkElement) {
    console.error(new Error(`NetworkElement : [${nodeId}] does not exist.`));
    return dispatch(new UpdateDeviceDescription("", { }, [ ]));
  }

  if (!networkElement.nodeDetails || !networkElement.nodeDetails.availableCapabilities) {
    throw new Error(`NetworkElement : [${nodeId}] has no capabilities.`);
  }
  const parser = new YangParser();

  const capParser = /^\(.*\?revision=(\d{4}-\d{2}-\d{2})\)(\S+)$/i;
  for (let i = 0; i < networkElement.nodeDetails.availableCapabilities.length; ++i){
    const capRaw = networkElement.nodeDetails.availableCapabilities[i];
    const capMatch = capRaw && capParser.exec(capRaw);
    try {
      capMatch && await parser.addCapability(capMatch[2], capMatch[1]);
    } catch (err) {
      console.error(err);
    }
  }

  parser.postProcess();

  console.log(parser.modules, parser.views)

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
  let referencedModule = modules[pathParts[0][0]];

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
          if (restResult.data == null || restResult.status < 200 || restResult.status > 299) {
            const message = restResult.data && restResult.data.errors && restResult.data.errors.error && restResult.data.errors.error[0] && restResult.data.errors.error[0]["error-message"] || "";
            throw new Error(`Server Error. Status: [${restResult.status}]\n${message || restResult.message || ''}`);
          }

          let dataRaw = restResult.data[dataMember!];
          dataRaw = dataRaw instanceof Array
            ? dataRaw[0]
            : dataRaw;

          data = dataRaw && dataRaw[viewElement.label] || [];
          const keys: string[] = data.map((entry: { [key: string]: any } )=> entry[viewElement.key!]);
          resultingDataUrls.push(...keys.map(key => `${dataUrl}/${viewElement.label.replace(/\//ig, "%2F")}/${key.replace(/\//ig, "%2F")}`));
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
        if (restResult.data == null || restResult.status < 200 || restResult.status > 299) {
          const message = restResult.data && restResult.data.errors && restResult.data.errors.error && restResult.data.errors.error[0] && restResult.data.errors.error[0]["error-message"] || "";
          throw new Error(`Server Error. Status: [${restResult.status}]\n${message || restResult.message || ''}`);
        }
        let dataRaw = restResult.data[dataMember!];
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

const resolveViewDescription = (defaultNS: string | null, vPath: string, view: ViewSpecification, viewData: any, displayAsList: boolean = false, key?: string): UpdatViewDescription =>{

  // check if-feature | when | and resolve all references.
  view = { ...view };
  view.elements = Object.keys(view.elements).reduce<{ [name: string]: ViewElement }>((acc, cur) => {
    const elm = view.elements[cur];
    const key = defaultNS && cur.replace(new RegExp(`^${defaultNS}:`, "i"),"") || cur;
    if (isViewElementReference(elm)) {
      acc[key] = { ...(elm.ref(vPath) || elm), id: key };
    } else {
      acc[key] = { ...elm, id: key };
    }
    return acc;
  }, {});
  return new UpdatViewDescription(vPath, view, viewData, displayAsList, key);
}

export const updateViewActionAsyncCreator = (vPath: string) => async (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
  const pathParts = splitVPath(vPath, /(?:([^\/\["]+)(?:\[([^\]]*)\])?)/g); // 1 = property / 2 = optional key
  const { configuration: { deviceDescription: { nodeId, modules, views } }, framework: { navigationState } } = getState();
  let dataPath = `/restconf/config/network-topology:network-topology/topology/topology-netconf/node/${nodeId}/yang-ext:mount`;
  let viewSpecification: ViewSpecification = views[0];
  let viewElement: ViewElement;

  let dataMember: string;
  let extractList: boolean = false;

  let currentNS : string | null = null;
  let defaultNS : string | null = null;

  dispatch(new SetCollectingSelectionData(true));
  try {
    for (let ind = 0; ind < pathParts.length; ++ind) {
      const [property, key] = pathParts[ind];
      const namespaceInd = property && property.indexOf(":") || -1;
      const namespace : string | null = namespaceInd > -1 ? (currentNS = property.slice(0, namespaceInd)) : currentNS;

      if (ind === 0) { defaultNS = namespace };

      viewElement = viewSpecification.elements[property];
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
          return dispatch(resolveViewDescription(defaultNS, vPath, viewSpecification, data, false, isViewElementList(viewElement!) && viewElement.key || undefined));
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
            dispatch(new SetCollectingSelectionData(false));
            throw new Error("Found a list at root level of a module w/o a refenrece key.");
          }
          return;
        }
        extractList = true;
      } else {
        dataPath += `/${property}${key ? `/${key.replace(/\//ig, "%2F")}` : ""}`;
        dataMember = namespace === defaultNS
          ? viewElement.label
          : `${namespace}:${viewElement.label}`;
        extractList = false;
      }

      if (viewElement && "viewId" in viewElement) viewSpecification = views[+viewElement.viewId];
    }

    let data: any = {};
    if (viewSpecification && viewSpecification.id !== "0") {
      const restResult = (await restService.getConfigData(dataPath));
      if (!restResult.data) {
        // special case: if this is a list without any response
        if (extractList && restResult.status === 404) {
          if (!isViewElementList(viewElement!)) {
            throw new Error(`vPath: [${vPath}]. ViewElement has no key.`);
          }
          return dispatch(resolveViewDescription(defaultNS, vPath, viewSpecification, [], extractList, viewElement.key));
        }
        throw new Error(`Did not get response from Server. Status: [${restResult.status}]`);
      } else if (restResult.status < 200 || restResult.status > 299) {
        const message = restResult.data.errors && restResult.data.errors.error && restResult.data.errors.error[0] && restResult.data.errors.error[0]["error-message"] || "";
        throw new Error(`Server Error. Status: [${restResult.status}]\n${message}`);
      } else {
        data = restResult.data[dataMember!]; // extract dataMember
      }

      // extract the first element list[key]
      data = data instanceof Array
        ? data[0]
        : data;

      // extract the list -> key: list
      data = extractList
        ? data[viewElement!.label] || [] // if the list is empty, it does not exist
        : data;
    }

    return dispatch(resolveViewDescription(defaultNS, vPath, viewSpecification, data, extractList, isViewElementList(viewElement!) && viewElement.key || undefined));
    // https://beta.just-run.it/#/configuration/Sim12600/core-model:network-element/ltp[LTP-MWPS-TTP-01]
    // https://beta.just-run.it/#/configuration/Sim12600/core-model:network-element/ltp[LTP-MWPS-TTP-01]/lp
  } catch (error) {
    history.back();
    dispatch(new AddErrorInfoAction({ title: "Problem", message: error.message || `Could not process ${dataPath}` }));
    dispatch(new SetCollectingSelectionData(false));
  } finally {
    return;
  }
}

export const updateDataActionAsyncCreator = (vPath: string, data: any) => async (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
  const pathParts = splitVPath(vPath, /(?:([^\/\["]+)(?:\[([^\]]*)\])?)/g); // 1 = property / 2 = optional key
  const { configuration: { deviceDescription: { nodeId, views } } } = getState();
  let dataPath = `/restconf/config/network-topology:network-topology/topology/topology-netconf/node/${nodeId}/yang-ext:mount`;
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
      viewElement = viewSpecification.elements[property];
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
          // handle new element
          key = viewElement.key && String(data[viewElement.key]) || "";
          isNew = key;
          if (!key) {
            dispatch(new SetCollectingSelectionData(false));
            throw new Error("No value for key [" + viewElement.key +"] in list [" + property + "]");
          }
        }
      }

      dataPath += `/${property}${key ? `/${key.replace(/\//ig, "%2F")}` : ""}`;
      dataMember = viewElement.label;
      embedList = false;

      if (viewElement && "viewId" in viewElement) {
        viewSpecification = views[+viewElement.viewId];
      }
    }

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
      const updateResult = await restService.setConfigData(dataPath, { [dataMember!]: data }); // extractDataMember
      if (updateResult.status < 200 || updateResult.status > 299) {
        const message = updateResult.data && updateResult.data.errors && updateResult.data.errors.error && updateResult.data.errors.error[0] && updateResult.data.errors.error[0]["error-message"] || "";
        throw new Error(`Server Error. Status: [${updateResult.status}]\n${message || updateResult.message || ''}`);
      }
    }

    return isNew
      ? dispatch(new ReplaceAction(`/configuration/${nodeId}/${vPath.replace(/\[\]$/i,`[${isNew}]`)}`)) // navigate to new element
      : dispatch(resolveViewDescription(defaultNS, vPath, viewSpecification, data, embedList, isViewElementList(viewElement!) && viewElement.key || undefined));
  } catch (error) {
    history.back();
    dispatch(new AddErrorInfoAction({ title: "Problem", message: error.message || `Could not change ${dataPath}` }));
    dispatch(new SetCollectingSelectionData(false));
  } finally {
    return;
  }
}