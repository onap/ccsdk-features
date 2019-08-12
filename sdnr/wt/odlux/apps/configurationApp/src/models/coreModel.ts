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
export interface NameValue {
  "value-name": string;
  value: string;
}

export interface LpResponse {
  uuid: string;
  "administrative-state": AdministrativeState;
  "is-protection-lock-out": boolean;
  "termination-state": string;
  "local-id": NameValue[];
  "configured-client-capacity": string;
  "extension": NameValue[];
  "operational-state": string;
  "administrative-control": string;
  "name": NameValue[];
  "lp-direction": string;
  "fc-blocks-signal-to-lp": string;
  "layer-protocol-name": string;
  "lifecycle-state": string;
  "label": NameValue[];
}

export interface LtpResponse {
  uuid: string;
  "administrative-state": AdministrativeState;
  "local-id": NameValue[];
  extension: NameValue[];
  "operational-state": string;
  lp: LpResponse[];
  "administrative-control": string;
  name: NameValue[];
  "ltp-in-other-view": string[];
  "ltp-direction": string;
  "lifecycle-state": string;
  "client-ltp": string[];
  "label": NameValue[];
  "server-ltp": string[];
}

export interface CoreModelNetworkElement {
  "administrative-state": AdministrativeState;
  ltp: LtpResponse[];
  "local-id": NameValue[];
  "lifecycle-state": string;
  uuid: string;
  label: NameValue[];
  extension: NameValue[];
  "operational-state": string;
  "administrative-control": string;
  name: NameValue[];
}


export type AdministrativeState = "unlocked" | "locked";

