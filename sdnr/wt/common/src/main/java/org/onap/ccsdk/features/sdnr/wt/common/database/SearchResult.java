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
package org.onap.ccsdk.features.sdnr.wt.common.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Deprecated
public class SearchResult<T> {

    /**
     * objects in results
     */
    private final List<T> hits;
    /**
     * size of all potential hits not necessarily the number of hits
     */
    private long total;

    public SearchResult(T[] hits) {
        this(hits, hits == null ? 0 : hits.length);
    }

    public SearchResult(T[] hits, long total) {
        this.hits = Arrays.asList(hits);
        this.total = total;
    }

    public SearchResult() {
        this.hits = new ArrayList<>();
        this.total = 0;
    }

    public List<T> getHits() {
        return this.hits;
    }
    public Set<T> getHitSets() {
        return new HashSet<>(this.hits);
    }

    public long getTotal() {
        return this.total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void add(T object) {
        this.hits.add(object);
    }

    @Override
    public String toString() {
        return "SearchResult [hits=" + hits + ", total=" + total + "]";
    }


}
