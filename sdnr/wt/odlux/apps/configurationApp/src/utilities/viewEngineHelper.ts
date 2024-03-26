import { storeService } from '../../../../framework/src/services/storeService';
import { WhenAST, WhenTokenType } from '../yang/whenParser';

import {
  ViewSpecification,
  ViewElement,
  isViewElementReference,
  isViewElementList,
  isViewElementObjectOrList,
  isViewElementRpc,
  isViewElementChoice,
  ViewElementChoiceCase,
  isViewElementObject,
} from '../models/uiModels';

import { Module } from '../models/yang';

import { restService } from '../services/restServices';

export type HttpResult = {
  status: number;
  message?: string | undefined;
  data: {
    [key: string]: any;
  } | null | undefined;
};

export const checkResponseCode = (restResult: HttpResult) =>{
  //403 gets handled by the framework from now on
  return restResult.status !== 403 && ( restResult.status < 200 || restResult.status > 299);
};

export const resolveVPath = (current: string, vPath: string): string => {
  if (vPath.startsWith('/')) {
    return vPath;
  }
  const parts = current.split('/');
  const vPathParts = vPath.split('/');
  for (const part of vPathParts) {
    if (part === '.') {
      continue;
    } else if (part === '..') {
      parts.pop();
    } else {
      parts.push(part);
    }
  }
  return parts.join('/');
};

export const splitVPath = (vPath: string, vPathParser : RegExp): [string, (string | undefined | null)][] => {
  const pathParts: [string, (string | undefined | null)][] = [];
  let partMatch: RegExpExecArray | null;
  if (vPath) do {
    partMatch = vPathParser.exec(vPath);
    if (partMatch) {
      pathParts.push([partMatch[1], partMatch[2] || (partMatch[0].includes('[]') ? null : undefined)]);
    }
  } while (partMatch);
  return pathParts;
};

const derivedFrom = (vPath: string, when: WhenAST, viewData: any, includeSelf = false) => {
  if (when.args?.length !== 2) {
    throw new Error('derived-from or derived-from-or-self requires 2 arguments.');
  }
  const [arg1, arg2] = when.args;
  if (arg1.type !== WhenTokenType.IDENTIFIER || arg2.type !== WhenTokenType.STRING) {
    throw new Error('derived-from or derived-from-or-self requires first argument IDENTIFIER and second argument STRING.');
  }

  if (!storeService.applicationStore) {
    throw new Error('storeService.applicationStore is not defined.');
  }

  const pathParts = splitVPath(arg1.value as string || '', /(?:(?:([^\/\:]+):)?([^\/]+))/g); 
  const referenceValueParts = /(?:(?:([^\/\:]+):)?([^\/]+))/g.exec(arg2.value as string || ''); 

  if (!pathParts || !referenceValueParts || pathParts.length === 0 || referenceValueParts.length === 0) {
    throw new Error('derived-from or derived-from-or-self requires first argument PATH and second argument IDENTITY.');
  }

  if (pathParts[0][1]?.startsWith('..') || pathParts[0][1]?.startsWith('/')) {
    throw new Error('derived-from or derived-from-or-self currently only supports relative paths.');
  }

  const { configuration: { deviceDescription: { modules } } } = storeService.applicationStore.state;
  const dataValue = pathParts.reduce((acc, [ns, prop]) => {
    if (prop === '.') {
      return acc;
    } 
    if (acc && prop) {
      const moduleName = ns && (Object.values(modules).find((m: Module) => m.prefix === ns) || Object.values(modules).find((m: Module) => m.name === ns))?.name;
      return (moduleName) ? acc[`${moduleName}:${prop}`] ||  acc[prop] : acc[prop];
    }
    return undefined;
  }, viewData);

  let dataValueParts = dataValue && /(?:(?:([^\/\:]+):)?([^\/]+))/g.exec(dataValue);
  if (!dataValueParts || dataValueParts.length < 2) {
    throw new Error(`derived-from or derived-from-or-self value referenced by first argument [${arg1.value}] not found.`);
  }
  let [, dataValueNs, dataValueProp] = dataValueParts;
  let dataValueModule: Module = dataValueNs && (Object.values(modules).find((m: Module) => m.name === dataValueNs));
  let dataValueIdentity = dataValueModule && dataValueModule.identities && (Object.values(dataValueModule.identities).find((i) => i.label === dataValueProp));

  if (!dataValueIdentity) {
    throw new Error(`derived-from or derived-from-or-self identity [${dataValue}] referenced by first argument [${arg1.value}] not found.`);
  }

  const [, referenceValueNs, referenceValueProp] = referenceValueParts;
  const referenceValueModule = referenceValueNs && (Object.values(modules).find((m: Module) => m.prefix === referenceValueNs));
  const referenceValueIdentity = referenceValueModule && referenceValueModule.identities && (Object.values(referenceValueModule.identities).find((i) => i.label === referenceValueProp));

  if (!referenceValueIdentity) {
    throw new Error(`derived-from or derived-from-or-self identity [${arg2.value}] referenced by second argument not found.`);
  }

  let result = includeSelf && (referenceValueIdentity === dataValueIdentity);
  while (dataValueIdentity && dataValueIdentity.base && !result) {
    dataValueParts = dataValue && /(?:(?:([^\/\:]+):)?([^\/]+))/g.exec(dataValueIdentity.base);
    const [, innerDataValueNs, innerDataValueProp] = dataValueParts;
    dataValueModule = innerDataValueNs && (Object.values(modules).find((m: Module) => m.prefix === innerDataValueNs)) || dataValueModule;
    dataValueIdentity = dataValueModule && dataValueModule.identities && (Object.values(dataValueModule.identities).find((i) => i.label === innerDataValueProp)) ;
    result = (referenceValueIdentity === dataValueIdentity);
  }

  return result;
};

const evaluateWhen = async (vPath: string, when: WhenAST, viewData: any): Promise<boolean> => {
  switch (when.type) {
    case WhenTokenType.FUNCTION:
      switch (when.name) {
        case 'derived-from-or-self':
          return derivedFrom(vPath, when, viewData, true);
        case 'derived-from':
          return derivedFrom(vPath, when, viewData, false);
        default:
          throw new Error(`Unknown function ${when.name}`);
      }
    case WhenTokenType.AND:
      return !when.left || !when.right || (await evaluateWhen(vPath, when.left, viewData) && await evaluateWhen(vPath, when.right, viewData));
    case WhenTokenType.OR:
      return !when.left || !when.right || (await evaluateWhen(vPath, when.left, viewData) || await evaluateWhen(vPath, when.right, viewData));
    case WhenTokenType.NOT:
      return !when.right || ! await evaluateWhen(vPath, when.right, viewData);
    case WhenTokenType.EXPRESSION:
      return !(when.value && typeof when.value !== 'string') || await evaluateWhen(vPath, when.value, viewData);
  }   
  return true;
};

export const getReferencedDataList = async (refPath: string, dataPath: string, modules: { [name: string]: Module }, views: ViewSpecification[]) => {
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
        throw Error(`Module: [${referencedModule.name}].[${viewElement.label}]. View element is not list or object.`);
      }
      view = views[+viewElement.viewId];
      const resultingDataUrls : string[] = [];
      if (isViewElementList(viewElement)) {
        for (let j = 0; j < dataUrls.length; ++j) {
          const dataUrl = dataUrls[j];
          const restResult = (await restService.getConfigData(dataUrl));
          if (restResult.data == null || checkResponseCode(restResult)) {
            const message = restResult.data && restResult.data.errors && restResult.data.errors.error && restResult.data.errors.error[0] && restResult.data.errors.error[0]['error-message'] || '';
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
          resultingDataUrls.push(...keys.map(key => `${dataUrl}/${viewElement.label.replace(/\//ig, '%2F')}=${key.replace(/\//ig, '%2F')}`));
        }
        dataMember = viewElement.label;
      } else {
        // just a member, not a list
        const pathSegment = (i === 0
          ? `/${referencedModule.name}:${viewElement.label.replace(/\//ig, '%2F')}`
          : `/${viewElement.label.replace(/\//ig, '%2F')}`);
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
          const message = restResult.data && restResult.data.errors && restResult.data.errors.error && restResult.data.errors.error[0] && restResult.data.errors.error[0]['error-message'] || '';
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
};

export const resolveViewDescription = (defaultNS: string | null, vPath: string, view: ViewSpecification): ViewSpecification =>{

  // resolve all references.
  view = { ...view };
  view.elements = Object.keys(view.elements).reduce<{ [name: string]: ViewElement }>((acc, cur) => {
    const resolveHistory : ViewElement[] = [];  
    let elm = view.elements[cur];
    const key = defaultNS && cur.replace(new RegExp(`^${defaultNS}:`, 'i'), '') || cur;
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
};

export const flattenViewElements = (defaultNS: string | null, parentPath: string, elements: { [name: string]: ViewElement }, views: ViewSpecification[], currentPath: string ): { [name: string]: ViewElement } => {
  if (!elements) return {};
  return Object.keys(elements).reduce<{ [name: string]: ViewElement }>((acc, cur) => {
    const elm = elements[cur];

    // remove the default namespace, and only the default namespace, sine it seems that this is also not in the restconf response
    const elmKey = defaultNS && elm.id.replace(new RegExp(`^${defaultNS}:`, 'i'), '') || elm.id;
    const key = parentPath ? `${parentPath}.${elmKey}` : elmKey;

    if (isViewElementRpc(elm)) {
      console.warn(`Flatten of RFC not supported ! [${currentPath}][${elm.label}]`);
      return acc;
    } else if (isViewElementObjectOrList(elm)) {
      const view = views[+elm.viewId];
      const inner = view && flattenViewElements(defaultNS, key, view.elements, views, `${currentPath}/${view.name}`);
      if (inner) {
        Object.keys(inner).forEach(k => (acc[k] = inner[k]));
      }
    } else if (isViewElementChoice(elm)) {
      acc[key] = {
        ...elm,
        id: key,
        cases: Object.keys(elm.cases).reduce<{ [name: string]: ViewElementChoiceCase }>((accCases, curCases) => {
          const caseElement = elm.cases[curCases];
          accCases[curCases] = {
            ...caseElement,
            // Hint: do not use key it contains elmKey, which shell be omitted for cases.
            elements: flattenViewElements(defaultNS, /*key*/ parentPath, caseElement.elements, views, `${currentPath}/${elm.label}`),
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

export const filterViewElements = async (vPath: string, viewData: any, viewSpecification: ViewSpecification) => {
  // filter elements of viewSpecification by evaluating when property
  return Object.keys(viewSpecification.elements).reduce(async (accPromise, cur) => {
    const acc = await accPromise;
    const elm = viewSpecification.elements[cur];
    if (!elm.when || await evaluateWhen(vPath || '', elm.when, viewData).catch((ex) => {
      console.warn(`Error evaluating when clause at: ${viewSpecification.name} for element: ${cur}`, ex);
      return true;
    })) {
      acc.elements[cur] = elm;
    }
    return acc;
  }, Promise.resolve({ ...viewSpecification, elements: {} as { [key: string]: ViewElement } }));
};

export const createViewData = (namespace: string | null, viewSpecification: ViewSpecification, views: ViewSpecification[]) => Object.keys(viewSpecification.elements).reduce<{ [name: string]: any }>((acc, cur) => {
  const elm = viewSpecification.elements[cur];
  let currentNamespace = namespace;
  const key = elm.id;
  if (elm.default) {
    acc[key] = elm.default || '';
  } else if (elm.uiType === 'boolean') {
    acc[key] = false;
  } else if (elm.uiType === 'number') {
    acc[key] = 0;
  } else if (elm.uiType === 'string') {
    acc[key] = '';
  } else if (isViewElementObject(elm)) {
    const view = views[+elm.viewId];
    if (view) {
      if (view.ns) {
        currentNamespace = view.ns;
      }
      acc[key] = createViewData(currentNamespace, view, views);
    }
  } else if (isViewElementList(elm)) {
    acc[key] = [];
  }
  return acc;
}, {});