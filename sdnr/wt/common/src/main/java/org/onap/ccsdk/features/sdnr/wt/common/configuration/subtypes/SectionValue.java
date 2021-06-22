/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael Dürre, Herbert Eiselt
 *
 */
class SectionValue {

    // variables
    private String value;
    private final List<String> comments;
    private boolean isUncommented;
    // end of variables

    // constructors
    public SectionValue(String value, List<String> commentsForValue, boolean isuncommented) {
        this.comments = commentsForValue;
        this.value = value;
        this.isUncommented = isuncommented;
    }

    public SectionValue(String value) {
        this(value, new ArrayList<String>(), false);
    }

    public SectionValue(String value, boolean isUncommented) {
        this(value, new ArrayList<String>(), isUncommented);
    }
    // end of constructors

    // getters and setters
    public String getValue() {
        return value;
    }

    public SectionValue setValue(String value) {
        this.value = value;
        return this;
    }

    public boolean isUncommented() {
        return isUncommented;
    }

    public SectionValue setIsUncommented(boolean isUncommented) {
        this.isUncommented = isUncommented;
        return this;
    }

    public void addComment(String comment) {
        this.comments.add(comment);
        this.isUncommented = false;
    }

    public void removeComment(String comment) {
        this.comments.remove(comment);
        this.isUncommented = this.comments.isEmpty();
    }

    public List<String> getComments() {
        return comments;
    }
    // end of getters and setters

    // public methods
    @Override
    public String toString() {
        return "SectionValue [value=" + value + ", comments=" + comments + ", isUncommented=" + isUncommented + "]";
    }
    // end of public methods


}
