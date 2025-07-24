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


/**
 * @author Michael Dürre
 *
 */
public class DatabaseVersion {

    private final String raw;
    private final int major;
    private final int minor;
    private final int revision;

    public DatabaseVersion(String version) throws ParseException {
        String[] hlp = version.split("\\.");
        if (hlp.length < 3) {
            throw new ParseException("unable to parse version string: " + version, 0);
        }
        this.raw = version;
        this.major = Integer.parseInt(hlp[0]);
        this.minor = Integer.parseInt(hlp[1]);
        this.revision = Integer.parseInt(hlp[2]);
    }

    @Override
    public String toString() {
        return String.format("%d.%d.%d", this.major, this.minor, this.revision);
    }

    /**
     * @param major
     * @param minor
     * @param revision
     */
    public DatabaseVersion(int major, int minor, int revision) {
        this.raw = String.format("%d.%d.%d", major, minor, revision);
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    /**
     * @return the revision
     */
    public int getRevision() {
        return revision;
    }

    /**
     * @return the minor
     */
    public int getMinor() {
        return minor;
    }

    /**
     * @return the major
     */
    public int getMajor() {
        return major;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DatabaseVersion)) {
            return false;
        }
        DatabaseVersion esobj = (DatabaseVersion) obj;
        return this.major == esobj.major && this.minor == esobj.minor && this.revision == esobj.revision;
    }

    @Override
    public int hashCode() {
        return this.raw.hashCode();
    }

    public boolean isNewerOrEqualThan(DatabaseVersion v) {
        if (this.equals(v)) {
            return true;
        }
        return this.isNewerThan(v);
    }
    public boolean isNewerThan(DatabaseVersion v) {
        if (this.major > v.major) {
            return true;
        } else if (this.major < v.major) {
            return false;
        }
        if (this.minor > v.minor) {
            return true;
        } else if (this.minor < v.minor) {
            return false;
        }
        if (this.revision > v.revision) {
            return true;
        }
        return false;
    }

    public boolean isOlderOrEqualThan(DatabaseVersion v) {
        if (this.equals(v)) {
            return true;
        }
        return this.isOlderThan(v);
    }

    public boolean isOlderThan(DatabaseVersion v) {
        if (this.major < v.major) {
            return true;
        } else if (this.major > v.major) {
            return false;
        }
        if (this.minor < v.minor) {
            return true;
        } else if (this.minor > v.minor) {
            return false;
        }
        if (this.revision < v.revision) {
            return true;
        }
        return false;
    }

}
