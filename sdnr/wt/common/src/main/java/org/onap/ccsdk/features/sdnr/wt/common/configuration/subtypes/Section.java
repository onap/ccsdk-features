/*******************************************************************************
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.exception.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Section {

    private static final Logger LOG = LoggerFactory.getLogger(Section.class);
    private static final String DELIMITER = "=";
    private static final String COMMENTCHARS[] = {"#", ";"};

    private final String name;
    private final List<String> rawLines;
    private final LinkedHashMap<String, SectionValue> values;

    public Section(String name) {
        LOG.debug("new section created: '{}'", name);
        this.name = name;
        this.rawLines = new ArrayList<>();
        this.values = new LinkedHashMap<>();
    }

    public void addLine(String line) {
        LOG.trace("adding raw line:" + line);
        this.rawLines.add(line);
    }

    public String getProperty(String key) {
        return this.getProperty(key, "");
    }

    public String getProperty(String key, String defValue) {
        if (values.containsKey(key)) {
            return values.get(key).getValue();
        }
        return defValue;
    }

    public String getName() {
        return name;
    }

    public void setProperty(String key, String value) {
        boolean isuncommented = this.isCommentLine(key);
        if (isuncommented) {
            key = key.substring(1);
        }
        if (this.values.containsKey(key)) {
            this.values.get(key).setValue(value).setIsUncommented(isuncommented);
        } else {
            this.values.put(key, new SectionValue(value,isuncommented));
        }
    }

    public void parseLines() {
        this.values.clear();
        List<String> commentsForValue = new ArrayList<>();
        boolean uncommented = false;
        for (String line : rawLines) {

            if (this.isCommentLine(line)) {
                if (!line.contains(DELIMITER)) {
                    commentsForValue.add(line);
                    continue;
                } else {
                    uncommented = true;
                    line = line.substring(1);
                }
            }
            if (!line.contains(DELIMITER)) {
                continue;
            }
            String hlp[] = line.split(DELIMITER);
            if (hlp.length > 1) {
                String key = hlp[0];
                String value =
                        line.length() > (key + DELIMITER).length() ? line.substring((key + DELIMITER).length())
                                : "";
                if (this.values.containsKey(key)) {
                    this.values.get(key).setValue(value);
                } else {
                    this.values.put(key, new SectionValue(value, commentsForValue, uncommented));
                    commentsForValue = new ArrayList<>();
                }
            } else {
                LOG.warn("ignoring unknown formatted line:" + line);
            }
            uncommented = false;
        }
    }

    private boolean isCommentLine(String line) {
        for (String c : COMMENTCHARS) {
            if (line.startsWith(c)) {
                return true;
            }
        }
        return false;
    }

    public String[] toLines() {
        List<String> lines = new ArrayList<>();
        if (!this.name.isEmpty()) {
            lines.add("[" + this.name + "]");
        }
        for (Entry<String, SectionValue> entry : this.values.entrySet()) {
            SectionValue sectionValue = entry.getValue();
            if (sectionValue.getComments().size() > 0) {
                for (String comment : sectionValue.getComments()) {
                    lines.add(comment);
                }
            }
            lines.add((sectionValue.isUncommented() ? COMMENTCHARS[0] : "") + entry.getKey() + DELIMITER
                    + sectionValue.getValue());
        }
        String[] alines = new String[lines.size()];
        return lines.toArray(alines);
    }

    public String getString(String key, String def) {
        return this.getProperty(key, def);
    }

    public boolean getBoolean(String key, boolean def) throws ConversionException {
        String v = this.getProperty(key);
        if (v == null || v.isEmpty()) {
            return def;
        }
        if (v.equals("true")) {
            return true;
        }
        if (v.equals("false")) {
            return false;
        }
        throw new ConversionException("invalid value for key " + key);
    }

    public int getInt(String key, int def) throws ConversionException {
        String v = this.getProperty(key);
        if (v == null || v.isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new ConversionException(e.getMessage());
        }
    }

    public Optional<Long> getLong(String key) {
        String v = this.getProperty(key);
        try {
            return Optional.of(Long.parseLong(v));
        } catch (NumberFormatException e) {
        }
           return Optional.empty();
    }

    public boolean hasValues() {
        return this.values.size() > 0;
    }

    public boolean hasKey(String key) {
        return this.values.containsKey(key);
    }

    @Override
    public String toString() {
        return "Section [name=" + name + ", rawLines=" + rawLines + ", values=" + values + "]";
    }

}
