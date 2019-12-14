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

/**
  * The PlainObject type is a JavaScript object containing zero or more key-value pairs.
  */
export interface PlainObject<T = any> {
  [key: string]: T;
}

export interface AjaxParameter {
  /**
    * The HTTP method to use for the request (e.g. "POST", "GET", "PUT").
    */
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'OPTIONS' | 'PATCH';
  /**
    * An object of additional header key/value pairs to send along with requests using the XMLHttpRequest
    * transport. The header X-Requested-With: XMLHttpRequest is always added, but its default
    * XMLHttpRequest value can be changed here. Values in the headers setting can also be overwritten from
    * within the beforeSend function.
    */
  headers?: PlainObject<string | null | undefined>;
  /**
    * Data to be sent to the server. It is converted to a query string, if not already a string. It's
    * appended to the url for GET-requests. See processData option to prevent this automatic processing.
    * Object must be Key/Value pairs. If value is an Array, jQuery serializes multiple values with same
    * key based on the value of the traditional setting (described below).
    */
  data?: PlainObject | string;
}



