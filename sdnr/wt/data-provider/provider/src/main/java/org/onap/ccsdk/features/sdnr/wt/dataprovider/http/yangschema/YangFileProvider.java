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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YangFileProvider {

    private static final Logger LOG = LoggerFactory.getLogger(YangFileProvider.class);

    private static final FilenameFilter yangFilenameFilter = new FilenameFilter() {

        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".yang");
        }
    };

    private static final int BUFFER_SIZE = 1024;

    private final Path mainSourcePath;
    private final List<Path> additionalSources;

    public YangFileProvider(String path) {
        this.mainSourcePath = new File(path).toPath();
        this.additionalSources = new ArrayList<>();
    }

    public boolean hasFileForModule(String module, String version) {
        return this.mainSourcePath.resolve(YangFilename.createFilename(module, version)).toFile().exists();
    }

    public boolean hasFileForModule(String module) {
        return this.findYangFiles(module).size() > 0;
    }

    private List<YangFilename> findYangFiles(String module) {
        LOG.debug("try to find yang files for {}", module);
        List<YangFilename> list = new ArrayList<>();
        String[] files = this.mainSourcePath.toFile().list(yangFilenameFilter);
        YangFilename yangfile;
        for (String fn : files) {
            try {
                yangfile = new YangFilename(this.mainSourcePath.resolve(fn).toString());
                if (yangfile.getModule().equals(module)) {
                    list.add(yangfile);
                }
            } catch (ParseException e) {
                LOG.warn("unable to handle yangfile {}: {}", fn, e);
            }
        }

        for (Path addPath : this.additionalSources) {
            files = addPath.toFile().list(yangFilenameFilter);
            for (String file : files) {
                try {
                    yangfile = new YangFilename(addPath.resolve(file).toString());
                    if (yangfile.getModule().equals(module)) {
                        list.add(yangfile);
                    }
                } catch (ParseException e) {
                    LOG.warn("unable to handle yangfile {}: {}", file, e);
                }
            }
        }
        return list;
    }

    /**
     * get yang file from source with specified version or least newer one if version is null then the latest one
     *
     * @param module
     * @param version
     * @return
     * @throws ParseException
     */
    private @Nullable YangFilename getYangFile(@Nonnull String module, @Nullable String version) throws ParseException {
        YangFilename f = null;
        List<YangFilename> list = this.findYangFiles(module);

        list.sort(SortByDateAscComparator.getInstance());

        // find specific version or nearest oldest
        if (version != null) {
            Date rev = YangFilename.parseRevision(version);
            for (YangFilename item : list) {
                if (rev.equals(item.getRevision())) {
                    f = item;
                    break;
                }
                if (item.getRevision().after(rev)) {
                    f = item;
                    break;
                }
            }
        }
        // get latest
        else {
            f = list.get(list.size() - 1);
        }
        return f;
    }

    /**
     * write filestream directly to output stream easier for http handling
     *
     * @param module
     * @param version
     * @param outputStream
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public int writeOutput(@Nonnull String module, @Nullable String version, @Nonnull OutputStream outputStream)
            throws IOException, ParseException {
        YangFilename fn = this.getYangFile(module, version);
        if (fn == null) {
            return 0;
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        int sumlen = 0;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fn.getFilename());

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                sumlen += bytesRead;
            }
        } catch (IOException e) {
            LOG.warn("problem sending {}: {}", fn.getFilename(), e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return sumlen;
    }

    private static class SortByDateAscComparator implements Comparator<YangFilename> {

        private static SortByDateAscComparator instance;

        @Override
        public int compare(YangFilename o1, YangFilename o2) {
            return o1.getRevision().compareTo(o2.getRevision());
        }

        public static Comparator<YangFilename> getInstance() {
            if (instance == null) {
                instance = new SortByDateAscComparator();
            }
            return instance;
        }

    }

    public YangFilename getFileForModule(String module, String rev) throws ParseException {
        return this.getYangFile(module, rev);
    }

    public YangFilename getFileForModule(String module) throws ParseException {
        return this.getFileForModule(module, null);
    }

    public boolean hasFileOrNewerForModule(String module, String version) throws ParseException {
        return this.getYangFile(module, version) != null;
    }

}
