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

import { ApplicationStore } from "../store/applicationStore";
import { UpdateUser } from "../actions/authentication";
import { ReplaceAction } from "../actions/navigationActions";

const maxMinutesTillLogout = 15;
let applicationStore: ApplicationStore | null;
let tickTimer = 15;


export const startForceLogoutService = (store: ApplicationStore) => {
    applicationStore = store;
    if (process.env.NODE_ENV === "development") {
        console.warn("logout timer not started in development mode");
    } else {
        createForceLogoutInterval();
    }

};

const createForceLogoutInterval = () => {
    console.log("logout timer running...");

    return setInterval(function () {
        if (applicationStore && applicationStore.state.framework.authenticationState.user) {
            tickTimer--;

            if (tickTimer === 0) {
                console.log("got logged out by timer")
                if (applicationStore) {
                    applicationStore.dispatch(new UpdateUser(undefined));
                    applicationStore.dispatch(new ReplaceAction("/login"));
                }
            }
        }

    }, 1 * 60000)
}

document.addEventListener("mousemove", function () { tickTimer = maxMinutesTillLogout; }, false)