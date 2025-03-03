/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

import type { WhenAST } from '../yang/whenParser';

export type ViewElementBase = {
  'id': string;
  'label': string;
  'module': string;
  'path': string;
  'config': boolean;
  'ifFeature'?: string;
  'when'?: WhenAST;
  'mandatory'?: boolean;
  'description'?: string;
  'isList'?: boolean;
  'default'?: string;
  'status'?: 'current' | 'deprecated' | 'obsolete';
  'reference'?: string; // https://tools.ietf.org/html/rfc7950#section-7.21.4
};

// https://tools.ietf.org/html/rfc7950#section-9.8
export type ViewElementBinary = ViewElementBase & {
  'uiType': 'binary';
  'length'?: Expression<YangRange>;  // number of octets
};

// https://tools.ietf.org/html/rfc7950#section-9.7.4
export type ViewElementBits = ViewElementBase & {
  'uiType': 'bits';
  'flags': {
    [name: string]: number | undefined;    // 0 - 4294967295
  };
};

// https://tools.ietf.org/html/rfc7950#section-9
export type ViewElementString = ViewElementBase & {
  'uiType': 'string';
  'pattern'?: Expression<RegExp>;
  'length'?: Expression<YangRange>;
  'invertMatch'?: true;
};

// special case derived from 
export type ViewElementDate = ViewElementBase & {
  'uiType': 'date';
  'pattern'?: Expression<RegExp>;
  'length'?: Expression<YangRange>;
  'invertMatch'?: true;
};

// https://tools.ietf.org/html/rfc7950#section-9.3
export type ViewElementNumber = ViewElementBase & {
  'uiType': 'number';
  'min': number;
  'max': number;
  'range'?: Expression<YangRange>;
  'units'?: string;
  'format'?: string;
  'fDigits'?: number;
};

// https://tools.ietf.org/html/rfc7950#section-9.5
export type ViewElementBoolean = ViewElementBase & {
  'uiType': 'boolean';
  'trueValue'?: string;
  'falseValue'?: string;
};

// https://tools.ietf.org/html/rfc7950#section-9.6.4
export type ViewElementSelection = ViewElementBase & {
  'uiType': 'selection';
  'multiSelect'?: boolean;
  'options': {
    'key': string;
    'value': string;
    'description'?: string;
    'status'?: 'current' | 'deprecated' | 'obsolete';
    'reference'?: string;
  }[];
};

// is a list if isList is true ;-)
export type ViewElementObject = ViewElementBase & {
  'uiType': 'object';
  'isList'?: false;
  'viewId': string;
};

// Hint: read only lists do not need a key
export type ViewElementList = (ViewElementBase & {
  'uiType': 'object';
  'isList': true;
  'viewId': string;
  'key'?: string;
});

export type ViewElementReference = ViewElementBase & {
  'uiType': 'reference';
  'referencePath': string;
  'ref': (currentPath: string) => [ViewElement, string] | undefined;
};

export type ViewElementUnion = ViewElementBase & {
  'uiType': 'union';
  'elements': ViewElement[];
};

export type ViewElementChoiceCase = { id: string; label: string; description?: string; elements: { [name: string]: ViewElement }  };

export type ViewElementChoice = ViewElementBase & {
  'uiType': 'choice';
  'cases': {
    [name: string]: ViewElementChoiceCase;
  };
};

// https://tools.ietf.org/html/rfc7950#section-7.14.1
export type ViewElementRpc = ViewElementBase & {
  'uiType': 'rpc';
  'inputViewId'?: string;
  'outputViewId'?: string;
};

export type ViewElementEmpty = ViewElementBase & {
  'uiType': 'empty';
};

export type ViewElement =
  | ViewElementEmpty
  | ViewElementBits
  | ViewElementBinary
  | ViewElementString
  | ViewElementDate
  | ViewElementNumber
  | ViewElementBoolean
  | ViewElementObject
  | ViewElementList
  | ViewElementSelection
  | ViewElementReference
  | ViewElementUnion
  | ViewElementChoice
  | ViewElementRpc;

export const isViewElementString = (viewElement: ViewElement): viewElement is ViewElementString => {
  return viewElement && (viewElement.uiType === 'string' || viewElement.uiType === 'date');
};

export const isViewElementDate = (viewElement: ViewElement): viewElement is ViewElementDate => {
  return viewElement && (viewElement.uiType === 'date');
};

export const isViewElementNumber = (viewElement: ViewElement): viewElement is ViewElementNumber => {
  return viewElement && viewElement.uiType === 'number';
};

export const isViewElementBoolean = (viewElement: ViewElement): viewElement is ViewElementBoolean => {
  return viewElement && viewElement.uiType === 'boolean';
};

export const isViewElementObject = (viewElement: ViewElement): viewElement is ViewElementObject => {
  return viewElement && viewElement.uiType === 'object' && !viewElement.isList;
};

export const isViewElementList = (viewElement: ViewElement): viewElement is ViewElementList => {
  return viewElement && viewElement.uiType === 'object' && !!viewElement.isList;
};

export const isViewElementObjectOrList = (viewElement: ViewElement): viewElement is ViewElementObject | ViewElementList => {
  return viewElement && viewElement.uiType === 'object';
};

export const isViewElementSelection = (viewElement: ViewElement): viewElement is ViewElementSelection => {
  return viewElement && viewElement.uiType === 'selection';
};

export const isViewElementReference = (viewElement: ViewElement): viewElement is ViewElementReference => {
  return viewElement && viewElement.uiType === 'reference';
};

export const isViewElementUnion = (viewElement: ViewElement): viewElement is ViewElementUnion => {
  return viewElement && viewElement.uiType === 'union';
};

export const isViewElementChoice = (viewElement: ViewElement): viewElement is ViewElementChoice => {
  return viewElement && viewElement.uiType === 'choice';
};

export const isViewElementRpc = (viewElement: ViewElement): viewElement is ViewElementRpc => {
  return viewElement && viewElement.uiType === 'rpc';
};

export const isViewElementEmpty = (viewElement: ViewElement): viewElement is ViewElementRpc => {
  return viewElement && viewElement.uiType === 'empty';
};

export const ResolveFunction = Symbol('ResolveFunction');

export type ViewSpecification = {
  id: string;
  ns?: string;
  name?: string;
  title?: string;
  parentView?: string;
  language: string;
  ifFeature?: string;
  augmentations?: string[];
  when?: WhenAST;
  uses?: (string[]) & { [ResolveFunction]?: (parent: string) => void };
  elements: { [name: string]: ViewElement };
  config: boolean;
  readonly canEdit: boolean;
};

export type YangRange = {
  min: number;
  max: number;
};

export type Expression<T> =
  | T
  | Operator<T>;

export type Operator<T> = {
  operation: 'AND' | 'OR';
  arguments: Expression<T>[];
};