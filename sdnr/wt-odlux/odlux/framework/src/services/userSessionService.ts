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
import { logoutUser } from "../actions/authentication";
import { ReplaceAction } from "../actions/navigationActions";
import { AuthMessage, getBroadcastChannel } from "./broadcastService";
import { User } from "../models/authentication";

let currentUser: User | null;
let applicationStore: ApplicationStore | null = null;
let timer : null | ReturnType<typeof setTimeout> = null;

export const startUserSessionService = (store: ApplicationStore) =>{
    applicationStore=store;
}

export const startUserSession = (user: User) => {
    console.log("user session started...")

    const currentTime = new Date();
    //get time differnce between login time and now (eg after user refreshes page)
    const timeDiffernce =(currentTime.valueOf()/1000 - user.loginAt);

    currentUser = user;

    if (process.env.NODE_ENV === "development") {
        //console.warn("logout timer not started in development mode");

        const expiresIn = (user.logoutAt - user.loginAt) - timeDiffernce;
        console.log("user should be logged out in: "+expiresIn/60 +"minutes")
        createForceLogoutInterval(expiresIn);
    } else {
        const expiresIn = (user.logoutAt - user.loginAt) - timeDiffernce;
        console.log("user should be logged out in: "+expiresIn/60 +"minutes")
        createForceLogoutInterval(expiresIn);
    }
};

const createForceLogoutInterval = (intervalInSec: number) => {
    console.log("logout timer running...");

    if(timer!==null){
        console.error("an old session was available");
        clearTimeout(timer);
    }
   
    timer = setTimeout(function () {
        if (currentUser && applicationStore) {

            applicationStore.dispatch(logoutUser());
            applicationStore.dispatch(new ReplaceAction("/login"));

        }

    }, intervalInSec * 1000)
}

export const endUserSession = ()=>{

    if(timer!==null){
        clearTimeout(timer);
        timer=null;
    }
}