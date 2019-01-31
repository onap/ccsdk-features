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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.database;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Nullable;
import org.apache.lucene.util.Version;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.node.Node;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.Resources;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.impl.AkkaConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.impl.EsConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.impl.GeoConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.config.util.ClusterNodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtDatabaseNode implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtDatabaseNode.class);
    private static final String DBCONFIGFILENAME = "etc/elasticsearch.yml";
    private static int MIN_PORT_NUMBER = 1024;
    private static int MAX_PORT_NUMBER = 65535;
    private static int ES_PORT = 9200;
    private static int DELAYSECONDS = 120;
    private static String PLUGINFOLDER = "etc/elasticsearch-plugins";

    private static HtDatabaseNode oneNode = null;
    private static Object initializationLock = new Object();
    private static Integer initializedTarget = 0;
    private static Integer initializedReached = 0;

    private final Node node;

    private HtDatabaseNode() {
        LOGGER.debug("Start elasticsearch service");

        LOGGER.debug("Lucine version: " + Version.LATEST);

        node = nodeBuilder().settings(Settings.builder().put("path.home", "etc").put("path.conf", "etc")).node();
        LOGGER.info("Starting Database service. Wait {} s", DELAYSECONDS);
        // Wait for orange status for single node without redundancy
        ClusterHealthResponse nodeStatus = node.client().admin().cluster().prepareHealth().setWaitForYellowStatus()
                .setTimeout(TimeValue.timeValueSeconds(DELAYSECONDS)).get();

        LOGGER.debug("Elasticsearch service started with status {}", nodeStatus.toString());
    }

    /**
     * Close node
     */
    @Override
    public void close() {
        node.close();
        oneNode = null; // Release the one instance that was started !
    }

    /**
     * Provide indication if all Index initializations are done.
     *
     * @return true if all index initializations are ready, false if not
     */
    public Boolean getInitialized() {
        synchronized (initializationLock) {
            return initializedTarget != 0 && initializedReached == initializedTarget;
        }
    }

    public void setInitializedReached() {
        synchronized (initializationLock) {
            HtDatabaseNode.initializedReached++;
        }
    }

    public void setInitializedTarget() {
        synchronized (initializationLock) {
            HtDatabaseNode.initializedTarget++;
        }
    }

    public Client getClient() {
        return node.client();
    }


    /*
     * --------------------------------------- Static functions below
     */


    private static void extractZip(String zipFile, String outputFolder) {

        byte[] buffer = new byte[1024];

        //        try {

        // create output directory is not exists
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // get the zip file content
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            // get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();

                File newFile = new File(outputFolder + File.separator + fileName);
                System.out.println("file unzip : " + newFile.getAbsoluteFile());
                if (ze.isDirectory()) {
                    newFile.mkdir();
                } else {

                    // create all non exists folders
                    // else you will hit FileNotFoundException for compressed folder
                    new File(newFile.getParent()).mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                    }
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (IOException ex) {
            LOGGER.warn("problem extracting {} to {}",zipFile, outputFolder);
        }
    }

    // Visibility package for test purpose
    static void checkorcreateplugins(String pluginFolder) {
        File f = new File(pluginFolder);
        String tmpFilename = pluginFolder + "/tmp.zip";
        if (!f.exists()) {
            f.mkdir();
        }

        f = new File(pluginFolder + "/head");
        File tmpFile = new File(tmpFilename);
        if (!f.exists()) {
            LOGGER.debug("extracting head plugin");
            if (Resources.extractFileTo("/elasticsearch/plugins/head.zip", tmpFile)) {
                extractZip(tmpFile.getAbsolutePath(), pluginFolder);
            } else {
                LOGGER.debug("problem extracting plugin res");
            }
        }
        f = new File(pluginFolder + "/delete-by-query");
        if (!f.exists()) {
            LOGGER.debug("extracting head delete-by-query plugin");
            if (Resources.extractFileTo("/elasticsearch/plugins/delete-by-query.zip", tmpFile)) {
                extractZip(tmpFile.getAbsolutePath(), pluginFolder);
            } else {
                LOGGER.debug("problem extracting plugin res");
            }
        }
        if (tmpFile.exists()) {
            boolean couldRemove = tmpFile.delete();
            LOGGER.info("Remove file {} with result {}",tmpFile.getName() , couldRemove);
        }

    }

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     */
    private static boolean isPortAvailable(int port) {
        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

    private static void checkorcreateConfigFile(EsConfig config, AkkaConfig akkaConfig, GeoConfig geoConfig) {
        File f = new File(DBCONFIGFILENAME);
        if (!f.exists()) {
            LOGGER.debug("no " + DBCONFIGFILENAME + " found - extracting from resources");
            if (Resources.extractFileTo("/elasticsearch/elasticsearch.yml", f)) {
                // replace template values
                LOGGER.debug("replace template values with config:" + config);
                Charset charset = StandardCharsets.UTF_8;
                try {
                    Path p = f.toPath();
                    String hostName = "0.0.0.0"; // Default as initialisation value
                    if (akkaConfig != null && akkaConfig.isCluster()) {
                        LOGGER.debug("cluster configuration found");
                        hostName = akkaConfig.getClusterConfig().getHostName(hostName);
                        String clusterDBName = akkaConfig.getClusterConfig().getDBClusterName(null);
                        String nodeName = String.format("node%d.%s", akkaConfig.getClusterConfig().getRoleMemberIndex(),
                                clusterDBName);
                        if (clusterDBName != null) {
                            config.setCluster(clusterDBName);
                            config.setNode(nodeName);
                            config.save();
                            LOGGER.info("set db name to " + clusterDBName + " nodename=" + nodeName);
                        } else {
                            LOGGER.warn("unable to set correct db clustername");
                        }
                    }
                    String content = new String(Files.readAllBytes(p), charset);
                    content = content.replaceAll("\\$clustername", config.getCluster())
                            .replaceAll("\\$nodename", config.getNode()).replaceAll("\\$hostname", hostName);

                    // add cluster configuration
                    if (akkaConfig != null && akkaConfig.isCluster()) {
                        List<ClusterNodeInfo> seedNodes = akkaConfig.getClusterConfig().getSeedNodes();
                        String nodesJSONString = "[\"" + seedNodes.get(0).getRemoteAddress() + "\"";
                        for (int i = 1; i < seedNodes.size(); i++) {
                            nodesJSONString += ",\"" + seedNodes.get(i).getRemoteAddress() + "\"";
                        }
                        nodesJSONString += "]";
                        content += System.lineSeparator()
                                + String.format("discovery.zen.ping.unicast.hosts: %s", nodesJSONString);

                        if (geoConfig != null) {
                            LOGGER.debug("adding zone configuration");
                            content += System.lineSeparator() + String
                                    .format("cluster.routing.allocation.awareness.force.zone.values: zone1,zone2");
                            content += System.lineSeparator()
                                    + String.format("cluster.routing.allocation.awareness.attributes: zone");
                            if (geoConfig.isPrimary(akkaConfig.getClusterConfig().getRoleMember())) {
                                content += System.lineSeparator() + String.format("node.zone: zone1");
                                LOGGER.debug("setting zone to zone1");
                            } else {
                                content += System.lineSeparator() + String.format("node.zone: zone2");
                                LOGGER.debug("setting zone to zone2");
                            }
                        }
                    }
                    Files.write(p, content.getBytes(charset));
                } catch (IOException e) {
                    LOGGER.warn("problem replacing values in file: " + e.getMessage());

                }
            } else {
                LOGGER.warn("problem writing database.yml to etc folder from res");
            }
        }
    }

    /**
     * Start as singleton
     *
     * @return the node or null if external node used
     */
    public static @Nullable HtDatabaseNode start(EsConfig config) throws IllegalStateException {
        return start(config, null, null);
    }

    public static HtDatabaseNode start(EsConfig config, AkkaConfig akkaConfig, GeoConfig geoConfig) {
        if (isPortAvailable(ES_PORT)) {
            LOGGER.info("ES Port not in use. Start internal ES.");
            if (oneNode == null) {
                checkorcreateplugins(PLUGINFOLDER);
                checkorcreateConfigFile(config, akkaConfig, geoConfig);
                oneNode = new HtDatabaseNode();
            } else {
                throw new IllegalStateException(
                        "Database is already started, but can only be started once. Stop here.");
            }
        } else {
            LOGGER.info("ES Port in use. External ES used.");
        }

        return oneNode;
    }

}
