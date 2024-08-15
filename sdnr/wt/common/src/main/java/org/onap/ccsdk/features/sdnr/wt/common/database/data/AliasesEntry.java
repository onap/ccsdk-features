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
package org.onap.ccsdk.features.sdnr.wt.common.database.data;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michael Dürre
 *
 */
@Deprecated
public class AliasesEntry {
    private static final String regex = "^([^\\ ]+)[\\ ]+([^\\ ]+)[\\ ]+.*$";
    private static final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);


    public String getAlias() {
        return alias;
    }

    public String getIndex() {
        return index;
    }

    private final String alias;
    private final String index;

    public AliasesEntry(String index, String alias) {
        this.alias = alias;
        this.index = index;
    }
    public AliasesEntry(String line) throws ParseException {
        final Matcher matcher = pattern.matcher(line);
        if (!matcher.find() || matcher.groupCount() < 2) {
            throw new ParseException("unable to parse string:" + line, 0);
        }
        this.alias = matcher.group(1);
        this.index = matcher.group(2);
    }
}
