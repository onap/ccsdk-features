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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Michael Dürre
 *
 *         Entry of list indices http request (/_cat/indices)
 *
 *         yellow open inventoryequipment-v1 5nNPRbJ3T9arMxqxBbJKyQ 5 1 0 0 1.2kb 1.2kb
 */
@Deprecated
public class IndicesEntry {

    private final String status;
    private final String status2;
    private final String name;
    private final String hash;
    private final int shards;
    private final int replicas;
    private final int c1;
    private final int c2;
    private final String size1;
    private final String size2;

    public String getStatus() {
        return status;
    }

    public String getStatus2() {
        return status2;
    }

    public String getName() {
        return name;
    }

    public String getHash() {
        return hash;
    }

    public int getShards() {
        return shards;
    }

    public int getReplicas() {
        return replicas;
    }

    public int getC1() {
        return c1;
    }

    public int getC2() {
        return c2;
    }

    public String getSize1() {
        return size1;
    }

    public String getSize2() {
        return size2;
    }

    protected IndicesEntry(String name, String status, String status2, String hash, int shards, int replicas, int c1,
            int c2, String size1, String size2) {
        this.name = name;
        this.status = status;
        this.status2 = status2;
        this.hash = hash;
        this.shards = shards;
        this.replicas = replicas;
        this.c1 = c1;
        this.c2 = c2;
        this.size1 = size1;
        this.size2 = size2;
    }

    public IndicesEntry(String line) throws ParseException {
        List<String> allElem = Arrays.stream(line.split(" ")).filter(e -> !e.isEmpty()).collect(Collectors.toList());
        List<String> possibleStatus = List.of("yellow","red","green");
        if (allElem.isEmpty() || !possibleStatus.contains(allElem.get(0))) {
            throw new ParseException("unable to parse status:" + line, 0);
        }
        try {
            if (allElem.size() == 10) {
                // new format
                this.status = allElem.get(0);
                this.status2 = allElem.get(1);
                this.name = allElem.get(2);
                this.hash = allElem.get(3);
                this.shards = Integer.parseInt(allElem.get(4));
                this.replicas = Integer.parseInt(allElem.get(5));
                this.c1 = Integer.parseInt(allElem.get(6));
                this.c2 = Integer.parseInt(allElem.get(7));
                this.size1 = allElem.get(8);
                this.size2 = allElem.get(9);
            } else if (allElem.size() == 9) {
                // old format without hash
                //for ES 2.2.0
                this.status = allElem.get(0);
                this.status2 = allElem.get(1);
                this.name = allElem.get(2);
                this.hash = "";
                this.shards = Integer.parseInt(allElem.get(3));
                this.replicas = Integer.parseInt(allElem.get(4));
                this.c1 = Integer.parseInt(allElem.get(5));
                this.c2 = Integer.parseInt(allElem.get(6));
                this.size1 = allElem.get(7);
                this.size2 = allElem.get(8);
            } else {
                throw new ParseException("unable to parse string:" + line, 0);
            }
        } catch (NumberFormatException e) {
            throw new ParseException("unable to parse int:" + line, 0);
        }
    }
}
