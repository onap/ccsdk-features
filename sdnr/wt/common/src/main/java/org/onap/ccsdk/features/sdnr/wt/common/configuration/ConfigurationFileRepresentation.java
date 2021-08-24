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
package org.onap.ccsdk.features.sdnr.wt.common.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.ConfigFileObserver;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of configuration file with section.<br>
 * A root section is used for parameters, not assigned to a specific section.<br>
 * The definitions of the configuration are attributes of a java class<br>
 */
public class ConfigurationFileRepresentation implements IConfigChangedListener {

    // constants
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationFileRepresentation.class);

    private static final long FILE_POLL_INTERVAL_MS = 1000;
    public static final String SECTIONNAME_ROOT = "";
    private static final String LR = "\n";
    private static final String EMPTY = "";

    private static final String LOG_UNKNWON_CONFIG_SECTION = "Unknown configuration section {}";
    // end of constants

    // variables
    /** Related configuration file **/
    private final File mFile;
    /** Monitor changes of file **/
    private final ConfigFileObserver fileObserver;
    /** List of sections **/
    private final HashMap<String, Section> sections;
    // end of variables

    // constructors
    public ConfigurationFileRepresentation(File f) {

        this.mFile = f;
        this.sections = new HashMap<String, Section>();
        try {
            if (!this.mFile.exists()) {
                if (!this.mFile.createNewFile()) {
                    LOG.error("Can not create file {}", f.getAbsolutePath());
                }
                if (!this.mFile.setReadable(true, false)) {
                    LOG.warn("unable to set file as readable");
                }
                if (!this.mFile.setWritable(true, false)) {
                    LOG.warn("unable to set file as writable");
                }
            }
            reLoad();

        } catch (IOException e) {
            LOG.error("Problem loading config file {} : {}", f.getAbsolutePath(), e.getMessage());
        }
        this.fileObserver = new ConfigFileObserver(f.getAbsolutePath(), FILE_POLL_INTERVAL_MS);
        this.fileObserver.start();
        this.fileObserver.registerConfigChangedListener(this);
    }

    public ConfigurationFileRepresentation(String configurationfile) {
        this(new File(configurationfile));
    }
    // end of constructors

    // getters and setters
    public synchronized Optional<Section> getSection(String name) {
        return Optional.ofNullable(sections.get(name));
    }
    // end of getters and setters

    // private methods
    private synchronized void reLoad() {
        sections.clear();
        addSection(SECTIONNAME_ROOT);
        load();
    }

    private synchronized void load() {
        LOG.debug("loading file {}", getMFileName());
        String curSectionName = SECTIONNAME_ROOT;
        Optional<Section> sectionOptional = this.getSection(curSectionName);
        Section curSection = sectionOptional.isPresent() ? sectionOptional.get() : this.addSection(curSectionName);
        try (BufferedReader br = new BufferedReader(new FileReader(this.mFile))) {
            for (String line; (line = br.readLine()) != null;) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("[") && line.endsWith("]")) {
                    curSectionName = line.substring(1, line.length() - 1);
                    curSection = this.addSection(curSectionName);
                } else {
                    curSection.addLine(line);
                }
            }

        } catch (Exception e) {
            LOG.info("Problem loading configuration file. {} {}", getMFileName(), e);
        }
        LOG.debug("finished loading file");
        LOG.debug("start parsing sections");
        for (Section section : this.sections.values()) {
            section.parseLines();
        }
        LOG.debug("finished parsing " + this.sections.size() + " sections");
    }

    private String getMFileName() {
        return mFile.getAbsolutePath();
    }

    // end of private methods

    // public methods
    public synchronized Section addSection(String name) {
        if (this.sections.containsKey(name)) {
            return this.sections.get(name);
        }
        Section s = new Section(name);
        this.sections.put(name, s);
        return s;
    }

    public synchronized void save() {
        LOG.debug("Write configuration to {}", getMFileName());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.mFile, false))) {
            for (Section section : this.sections.values()) {
                if (section.hasValues()) {
                    bw.write(String.join(LR, section.toLines()) + LR + LR);
                }
            }
        } catch (Exception e) {
            LOG.warn("problem saving value: {}", e.getMessage());
        }
    }

    public void registerConfigChangedListener(IConfigChangedListener l) {
        this.fileObserver.registerConfigChangedListener(l);
    }

    public void unregisterConfigChangedListener(IConfigChangedListener l) {
        this.fileObserver.unregisterConfigChangedListener(l);
    }

    @Override
    public void onConfigChanged() {
        LOG.debug("Reload on change {}", getMFileName());
        reLoad();
    }

    @Override
    public String toString() {
        return "ConfigurationFileRepresentation [mFile=" + mFile + ", sections=" + sections + "]";
    }

    @Override
    protected void finalize() throws Throwable {
        if (this.fileObserver != null) {
            this.fileObserver.interrupt();
        }
    }

    /*
     * Property access set/get
     */
    public synchronized void setProperty(String section, String key, Object value) {
        Optional<Section> os = this.getSection(section);
        if (os.isPresent()) {
            os.get().setProperty(key, value == null ? "null" : value.toString());
            save();
        } else {
            LOG.info(LOG_UNKNWON_CONFIG_SECTION, section);
        }
    }

    public synchronized String getProperty(String section, String propertyKey) {
        Optional<Section> os = this.getSection(section);
        if (os.isPresent()) {
            return os.get().getProperty(propertyKey);
        } else {
            LOG.debug(LOG_UNKNWON_CONFIG_SECTION, section);
            return EMPTY;
        }
    }

    public synchronized Optional<Long> getPropertyLong(String section, String propertyKey) {
        Optional<Section> os = this.getSection(section);
        if (os.isPresent()) {
            return os.get().getLong(propertyKey);
        } else {
            LOG.debug(LOG_UNKNWON_CONFIG_SECTION, section);
            return Optional.empty();
        }
    }

    public synchronized boolean isPropertyAvailable(String section, String propertyKey) {
        Optional<Section> s = this.getSection(section);
        return s.isPresent() && s.get().hasKey(propertyKey);
    }

    public synchronized void setPropertyIfNotAvailable(String section, String propertyKey, Object propertyValue) {
        if (!isPropertyAvailable(section, propertyKey)) {
            setProperty(section, propertyKey, propertyValue.toString());
        }
    }

    public synchronized boolean getPropertyBoolean(String section, String propertyKey) {
        return getProperty(section, propertyKey).equalsIgnoreCase("true");
    }
    // end of public methods

}
