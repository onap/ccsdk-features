/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

module.exports = {
  "/about": {
    target: "http://sdncweb:8080",
    secure: false
  },
  "/yang-schema/": {
    target: "http://sdncweb:8080",
    secure: false
  },
  "/oauth/": {
    target: "http://sdncweb:8080",
    secure: false
  },
  "/database/": {
    target: "http://sdncweb:8080",
    secure: false
  },
  "/restconf/": {
    target: "http://sdncweb:8080",
    secure: false
  },
  "/rests/": {
    target: "http://sdncweb:8080",
    secure: false
  },
  "/userdata": {
    target: "http://sdncweb:8080",
    secure: false
  },
  "/userdata/": {
    target: "http://sdncweb:8080",
    secure: false
  },
  "/help/": {
    target: "http://sdncweb:8080",
    secure: false
  },
  "/about/": {
    target: "http://sdncweb:8080",
    secure: false
  },
  "/tree/": {
    target: "http://sdncweb:8080",
    secure: false
  },
  "/sitedoc/": {
    target: "http://sdncweb:8080",
    secure: false
  },
  "/topology/": {
    target: "http://sdncweb:8080",
    secure: false
  },

  "/websocket": {
    target: "http://sdncweb:8080",
    ws: true,
    changeOrigin: true,
    secure: false
  },
  "/apidoc": {
    target: "http://sdncweb:8080",
    ws: true,
    changeOrigin: true,
    secure: false
  },
  "/tiles/": {
    target: "http://sdncweb:8080",
    headers: {
      "Connection": "keep-alive"
    },
    secure: false
  },
  "/swagger/": {
    target: "http://swagger.t1.lab.osn-lab.com",
    secure: false,
    pathRewrite(pathname) {
      return pathname.replace(/^\/swagger/, '/');
    }
  },
  "/electromagnetic-field/": {
    target: "http://sdncweb:8080",
    ws: true,
    changeOrigin: true,
    secure: false
  },
}

