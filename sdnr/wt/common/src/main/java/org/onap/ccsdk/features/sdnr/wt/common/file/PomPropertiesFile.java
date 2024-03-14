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
package org.onap.ccsdk.features.sdnr.wt.common.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Example Content: #Generated by org.apache.felix.bundleplugin #Tue Nov 19 10:17:57 CET 2019 version=0.7.0-SNAPSHOT
 * groupId=org.onap.ccsdk.features.sdnr.wt artifactId=sdnr-wt-data-provider-provider
 * 
 * @author jack
 *
 */
public class PomPropertiesFile {

    private Date buildDate;
    private String version;
    private String groupId;
    private String artifactId;

    public Date getBuildDate() {
        return this.buildDate;
    }

    public String getVersion() {
        return this.version;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    private final DateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

    public PomPropertiesFile(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int intRead;
        while ((intRead = is.read()) != -1) {
            bos.write(intRead);
        }
        this.parse(new String(bos.toByteArray()));
    }

    private void parse(String s) {
        String[] lines = s.split("\n");
        for (String line : lines) {

            if (line.startsWith("#Generated")) {

            } else if (line.startsWith("groupId")) {
                this.groupId = line.substring("groupId=".length());
            } else if (line.startsWith("artifactId")) {
                this.artifactId = line.substring("artifactId=".length());
            } else if (line.startsWith("#")) {
                try {
                    this.buildDate = sdf.parse(line.substring(1));
                } catch (ParseException e) {

                }
            }
            if (version != null && this.buildDate != null && this.groupId != null && this.artifactId != null) {
                break;
            }
        }
    }
}