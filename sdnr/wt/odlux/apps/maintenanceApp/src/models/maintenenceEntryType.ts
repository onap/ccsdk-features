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
/** Represents the elestic search db type for maintenence enrties */
export type MaintenenceEntryType = {
    node: string;
    // According to the arrangement from 2019.02.15 there will be currently only one element in the filters array.
    filter: [{
      definition: { "object-id-ref": string ,problem: string},
      description: string,
      /** The end date for the maintenence mode formated as ISO date string in UTC.  */
      end: string,
      /** The start date for the maintenence mode formated as ISO date string in UTC.  */
      start: string
    }],
    /** Determines if the filter set is activated or not. */
    active: boolean;
}

export const spoofSymbol = Symbol("Spoof");

/** Represents the type for an maintenence entry. */
export type MaintenenceEntry = {
  [spoofSymbol]?: boolean;
  _id: string;
  mountId: string;
  description: string;
  start: string;
  end: string;
  active: boolean;
}