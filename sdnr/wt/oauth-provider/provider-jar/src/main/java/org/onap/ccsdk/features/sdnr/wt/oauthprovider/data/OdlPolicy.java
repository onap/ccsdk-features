/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.data;

public class OdlPolicy {

    private String path;
    private PolicyMethods methods;


    public OdlPolicy() {

    }

    public OdlPolicy(String path, PolicyMethods methods) {
        this.path = path;
        this.methods = methods;
    }

    public PolicyMethods getMethods() {
        return methods;
    }

    public void setMethods(PolicyMethods methods) {
        this.methods = methods;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public static OdlPolicy allowAll(String path) {
        return new OdlPolicy(path, PolicyMethods.allowAll());
    }

    public static OdlPolicy denyAll(String path) {
        return new OdlPolicy(path, PolicyMethods.denyAll());
    }

    public static class PolicyMethods {
        private boolean get;
        private boolean post;
        private boolean put;
        private boolean delete;
        private boolean patch;

        public PolicyMethods() {
            this(false, false, false, false, false);
        }

        public PolicyMethods(boolean get, boolean post, boolean put, boolean del, boolean patch) {
            this.get = get;
            this.post = post;
            this.put = put;
            this.delete = del;
            this.patch = patch;
        }

        public boolean isGet() {
            return get;
        }

        public void setGet(boolean get) {
            this.get = get;
        }

        public boolean isPost() {
            return post;
        }

        public void setPost(boolean post) {
            this.post = post;
        }

        public boolean isPut() {
            return put;
        }

        public void setPut(boolean put) {
            this.put = put;
        }

        public boolean isDelete() {
            return delete;
        }

        public void setDelete(boolean delete) {
            this.delete = delete;
        }

        public boolean isPatch() {
            return patch;
        }

        public void setPatch(boolean patch) {
            this.patch = patch;
        }

        public static PolicyMethods allowAll() {
            return new PolicyMethods(true, true, true, true, true);
        }

        public static PolicyMethods denyAll() {
            return new PolicyMethods(false, false, false, false, false);
        }
    }
}
