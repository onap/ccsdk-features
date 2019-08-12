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
export interface AccessFlags {
  "read": boolean,
  "write": boolean,
  "create": boolean
}

export interface ViewElementBase {
  "id": string;
  "label": string;
  "viewId": string;
  "leafrefPath": string;
  "accessFlags": AccessFlags;
  "description": string;
}

export interface ViewElementString extends ViewElementBase {
  "uiType": "string";
}

export interface ViewElementNumber extends ViewElementBase {
  "uiType": "number";
  "min"?: number;
  "max"?: number;
  "unit"?: string;
  "format"?: string;
}

export interface ViewElementBoolean extends ViewElementBase {
  "uiType": "boolean";
  "trueValue"?: string;
  "falseValue"?: string;
}

export interface ViewElementObject extends ViewElementBase {
  "uiType": "object";
  "viewId": string;
}

export interface ViewElementSelection extends ViewElementBase {
  "uiType": "selection";
  "multiSelect"?: boolean
  "options": {
    "key": string,
    "value": string,
    "description": string
  }[],
}

export interface ViewElementList extends ViewElementBase {
  "uiType": "list",
  "listType": "object" | "string" | "number",
  "viewId": string,
}

export type ViewElement =
  | ViewElementString
  | ViewElementNumber
  | ViewElementBoolean
  | ViewElementObject
  | ViewElementSelection
  | ViewElementList;

export interface ViewSpecification {
  "id": string;
  "parentView": string;
  "name": string;
  "language": string;
  "title"?: string;
  "url": string;
  "dataPath": string;
  "elements": ViewElement[];
}

