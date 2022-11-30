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

import { ViewElement, ViewSpecification } from "./uiModels";

export enum ModuleState {
  stable,
  instable,
  importOnly,
  unavailable,
}

export type Token = {
  name: string;
  value: string;
  start: number;
  end: number;
}

export type Statement = {
  key: string;
  arg?: string;
  sub?: Statement[];
}

export type Identity = {
  id: string,
  label: string,
  base?: string,
  description?: string,
  reference?: string,
  children?: Identity[],
  values?: Identity[],
}

export type Revision = {
  description?: string,
  reference?: string
};

export type Module = {
  name: string;
  namespace?: string;
  prefix?: string;
  state: ModuleState;
  identities: { [name: string]: Identity };
  revisions: { [version: string]: Revision };
  imports: { [prefix: string]: string };
  features: { [feature: string]: { description?: string } };
  typedefs: { [type: string]: ViewElement };
  augments: { [path: string]: ViewSpecification[] };
  groupings: { [group: string]: ViewSpecification };
  views: { [view: string]: ViewSpecification };
  elements: { [view: string]: ViewElement };
  executionOrder?: number;
}