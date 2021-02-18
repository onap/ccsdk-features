/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

import { link } from "./link";

export type Site = {
    id: string,
    name: string,
    address: Address,
    heightAmslInMeters?: number, //AboveGroundLevel
    antennaHeightAmslInMeters?: number,
    type?: string,
    operator: string,
    location:{lon: number, lat: number},
    devices: Device[],
    links: link[]
}

export type Address={
    streetAndNr: string,
    city: string,
    zipCode: string | null,
    country: string
}

export class Device {
    id: string;
    type: string;
    name: string;
    manufacturer: string;
    owner: string;
    status?: string;
    port: number[];
}