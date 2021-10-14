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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.http.yangschema;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YangFilename {

    private static final String REGEX = "([^\\/]{1,2048})@([0-9]{4}-[0-9]{2}-[0-9]{2}).yang";
    private static final Pattern pattern = Pattern.compile(REGEX, Pattern.MULTILINE);
    private final String filename;
    private final Matcher matcher;
    private Date revision;
    private String module;

    public static Date parseRevision(String sRevision) throws ParseException {
        final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        return fmt.parse(sRevision);
    }

    public YangFilename(String fn) throws ParseException {
        this.filename = fn;
        matcher = pattern.matcher(this.filename);
        if (!matcher.find()) {
            throw new ParseException("unknown filename format", 0);
        }
        this.module = matcher.group(1);
        this.revision = parseRevision(matcher.group(2));

    }

    public static String createFilename(String module, String rev) {
        return String.format("%s@%s.yang", module, rev);
    }

    public YangFilename(String module, String rev) throws ParseException {
        this(createFilename(module, rev));
    }

    public String getFilename() {
        return this.filename;
    }

    public Date getRevision() {
        return this.revision;
    }

    public String getModule() {
        return this.module;
    }
}
