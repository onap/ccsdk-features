/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Message object for registering for notifications
 *
 * @author jack
 *
 */
public class ScopeRegistration {

    private static final String REGEX_RATIO = "^([\\d]+)\\/(min)$";
    private static final Pattern PATTERN_RATIO = Pattern.compile(REGEX_RATIO);

    @Override
    public String toString() {
        return "ScopeRegistration [data=" + data + ", scopes=" + scopes + ", ratio=" + ratio + ", isvalid="
                + this.validate() + "]";
    }

    private DataType data;

    public DataType getData() {
        return data;
    }

    public void setData(DataType data) {
        this.data = data;
    }

    private void setScopes(List<Scope> scopes) {
        this.scopes = scopes;
    }

    private List<Scope> scopes;
    private String ratio;

    @JsonIgnore
    private long rationLong;

    public boolean validate() {
        return this.data != null && this.validateScopes();
    }

    private boolean validateScopes() {
        if (this.scopes == null) {
            return false;
        }
        for (Scope scope : this.scopes) {
            if (!scope.isValid()) {
                return false;
            }
        }
        return true;
    }

    public boolean isType(DataType type) {
        return this.data == type;
    }

    public List<Scope> getScopes() {
        return this.scopes;
    }

    public String getRatio() {
        return this.ratio;
    }

    @JsonIgnore
    public boolean hasRatioLimit() {
        return this.ratio != null;
    }

    @JsonIgnore
    public long getRatioPerMinute() {
        return this.rationLong;
    }

    public void setRatio(String ratio) {
        assertRatioExpression(ratio);
        this.ratio = ratio;
    }

    private void assertRatioExpression(String ratio) {
        final Matcher matcher = PATTERN_RATIO.matcher(ratio);
        if (!matcher.find()) {
            throw new IllegalArgumentException(ratio + " is not a valid ratio expression");
        } else {
            this.rationLong = Long.parseLong(matcher.group(1));
        }
    }

    public enum DataType {
        scopes;


    }

    public static ScopeRegistration forNotifications(List<Scope> scopes) {
        ScopeRegistration reg = new ScopeRegistration();
        reg.setData(DataType.scopes);
        reg.setScopes(scopes);
        return reg;
    }

}
