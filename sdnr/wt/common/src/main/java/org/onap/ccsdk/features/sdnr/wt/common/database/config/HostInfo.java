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
package org.onap.ccsdk.features.sdnr.wt.common.database.config;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HostInfo {

    public enum Protocol {
        HTTP("http"), HTTPS("https");//,
        //		FILETRANSFERPROTOCOL("ftp");

        private final String value;

        private Protocol(String s) {
            this.value = s;
        }

        @Override
        public String toString() {
            return this.value;
        }

        public String getValue() {
            return value;
        }

        public static Protocol getValueOf(String s) {
            s = s.toLowerCase();
            for (Protocol p : Protocol.values()) {
                if (p.value.equals(s)) {
                    return p;
                }
            }
            return HTTP;
        }
    }

    private static final Protocol DEFAULT_PROTOCOL = Protocol.HTTP;
    public final String hostname;
    public final int port;
    public final Protocol protocol;

    public HostInfo(String hostname, int port, Protocol protocol) {
        this.hostname = hostname;
        this.port = port;
        this.protocol = protocol;

    }

    public HostInfo(String hostname, int port) {
        this(hostname, port, DEFAULT_PROTOCOL);

    }

    @Override
    public String toString() {
        return "HostInfo [hostname=" + hostname + ", port=" + port + ", protocol=" + protocol + "]";
    }

    public String toUrl() {
        return String.format("%s://%s:%d", this.protocol, this.hostname, this.port);
    }

    public static HostInfo getDefault() {
        return new HostInfo("localhost", 9200, Protocol.HTTP);
    }

    /**
     * @param dbUrl
     * @return
     */
    public static HostInfo parse(String dbUrl) throws ParseException {
        final String regex = "^(https?):\\/\\/([^:]*):?([0-9]{0,5})$";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(dbUrl);
        if (!matcher.find() || matcher.groupCount() < 2) {
            throw new ParseException("url '" + dbUrl + "' not parseable. Expected http://xyz", 0);
        }
        Protocol p = Protocol.getValueOf(matcher.group(1));
        String host = matcher.group(2);
        int port = p == Protocol.HTTP ? 80 : 443;
        if (matcher.groupCount() > 2) {
            port = Integer.parseInt(matcher.group(3));
        }
        return new HostInfo(host, port, p);
    }
}
