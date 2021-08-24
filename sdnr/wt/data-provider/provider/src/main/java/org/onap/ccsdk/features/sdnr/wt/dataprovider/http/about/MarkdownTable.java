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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.http.about;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Michael Dürre
 *
 */
public class MarkdownTable {

    private String[] columns;
    private final List<String[]> rows;

    public MarkdownTable() {
        this.rows = new ArrayList<>();
    }

    public void setHeader(String[] cols) {
        this.columns = cols;
    }

    public void addRow(String[] values) {
        this.rows.add(values);
    }

    private int getColNumbers() {
        int cols = 0;
        if (this.columns != null) {
            cols = this.columns.length;
        } else {
            cols = !this.rows.isEmpty() ? this.rows.get(0).length : 0;
        }
        return cols;
    }

    public String toMarkDown() {
        StringBuilder sb = new StringBuilder();

        final int cols = this.getColNumbers();
        if (cols > 0) {
            sb.append("|");
            for (int i = 0; i < cols; i++) {
                sb.append(String.format(" %s |", this.columns != null ? this.columns[i] : ""));
            }
            sb.append("\n");
            sb.append("|");
            for (int i = 0; i < cols; i++) {
                sb.append(" --- |");
            }
            sb.append("\n");
            sb.append("|");
            for (String[] row : this.rows) {
                for (int i = 0; i < cols; i++) {
                    sb.append(String.format(" %s |", row[i]));
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public String toJson() {
        JSONArray a = new JSONArray();
        final int cols = this.getColNumbers();
        if (cols > 0 && this.columns!=null) {
            for (String[] row : this.rows) {
                JSONObject o = new JSONObject();
                for (int i = 0; i < cols; i++) {
                    o.put(this.columns[i], row[i]);
                }
                a.put(o);
            }
        }
        return a.toString();
    }
}
