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
export type Result<TSource extends {}> = {
  "data-provider:output": {
    pagination?: {
      size: number;
      page: number;
      total: number;
    },
    data: TSource[];
  }
}

export type SingeResult<TSource extends {}> = {
  "data-provider:output": TSource;
}


export type ResultTopology<TSource extends {}> = {
  "output": {
    pagination?: {
      size: number;
      page: number;
      total: number;
    },
    data: TSource[];
  }
}

export type HitEntry<TSource extends {}> = {
  _index: string;
  _type: string;
  _id: string;
  _score: number;
  _source: TSource;
}

type ActionResponse ={
  _index: string;
  _type: string;
  _id: string;
  _shards: {
    total: number,
    successful: number,
    failed: number
    },

}

export type PostResponse = ActionResponse & {
  created: boolean
}

export type DeleteResponse = ActionResponse & {
  found: boolean
}

