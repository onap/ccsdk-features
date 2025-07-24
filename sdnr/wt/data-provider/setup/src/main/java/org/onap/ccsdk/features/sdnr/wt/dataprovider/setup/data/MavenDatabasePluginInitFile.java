/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Update Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.ReleaseInformation;


/**
 * @author Michael Dürre
 *
 */
public class MavenDatabasePluginInitFile {
    private static final int replicas = 1;
    private static final int shards = 5;
    private static final String mappings = "\"mappings\":%s";
    private static final String settings =
            "\"settings\":{\"index\":{\"number_of_shards\":%d,\"number_of_replicas\":%d},\"analysis\":{\"analyzer\":{\"content\":"
                    + "{\"type\":\"custom\",\"tokenizer\":\"whitespace\"}}}}";

    public static void create(Release release, String filename) throws IOException {

        ReleaseInformation ri = ReleaseInformation.getInstance(release);
        Set<ComponentName> comps = ri.getComponents();
        List<String> lines = new ArrayList<>();
        lines.add("PUT:_cluster/settings/:{\"persistent\":{\"action.auto_create_index\":\"true\"}}");
        for (ComponentName c : comps) {
            lines.add(String.format("PUT:%s/:{" + settings + "," + mappings + "}", ri.getIndex(c), shards, replicas,
                    ri.getDatabaseMapping(c)));
            lines.add(String.format("PUT:%s/_alias/%s/:{}", ri.getIndex(c), ri.getAlias(c)));
        }

        File filePath = new File(filename);
        if (filePath.getParentFile() != null && !filePath.getParentFile().exists()) {
            //Crate Directory if missing
            filePath.getParentFile().mkdirs();
        }
        Files.write(filePath.toPath(), lines);
    }
}
