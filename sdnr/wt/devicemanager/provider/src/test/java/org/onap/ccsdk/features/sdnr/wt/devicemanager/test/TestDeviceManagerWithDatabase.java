/*******************************************************************************
 * ============LICENSE_START======================================================= ONAP : ccsdk
 * feature sdnr wt ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * ================================================================================ Licensed under
 * the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License. ============LICENSE_END=========================================================
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.archiveservice.ArchiveCleanService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.database.HtDatabaseWebAPIClient;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.internalTypes.Resources;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.container.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.DeviceManagerImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.DeviceManagerService.Action;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.test.mock.ClusterSingletonServiceProviderMock;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.test.mock.DataBrokerNetconfMock;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.test.mock.MountPointMock;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.test.mock.MountPointServiceMock;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.test.mock.NotificationPublishServiceMock;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.test.mock.RpcProviderRegistryMock;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.test.util.DBCleanServiceHelper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.test.util.ReadOnlyTransactionMountpoint1211Mock;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.test.util.ReadOnlyTransactionMountpoint1211pMock;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.test.util.ReadOnlyTransactionMountpoint12Mock;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class TestDeviceManagerWithDatabase {

    private static int DATABASETIMEOUTSECONDS = 30;

    private static Path KARAF_ETC = Paths.get("etc");
    private static DeviceManagerImpl deviceManager;
    private static MountPointMock mountPoint;
    private static DataBrokerNetconfMock dataBrokerNetconf;

    private static final Logger LOG = LoggerFactory.getLogger(TestDeviceManagerWithDatabase.class);



    @BeforeClass
    public static void before() throws InterruptedException, IOException {

        System.out.println("Logger: " + LOG.getClass().getName() + " " + LOG.getName());
        // Call System property to get the classpath value
        Path etc = KARAF_ETC;
        delete(etc);

        System.out.println("Create empty:" + etc.toString());
        Files.createDirectories(etc);

        // Create mocks
        ReadOnlyTransactionMountpoint12Mock readOnlyTransaction = new ReadOnlyTransactionMountpoint12Mock();
        dataBrokerNetconf = new DataBrokerNetconfMock();
        dataBrokerNetconf.setReadOnlyTransaction(readOnlyTransaction);
        mountPoint = new MountPointMock();
        mountPoint.setReadOnlyTransaction(readOnlyTransaction);
        ClusterSingletonServiceProvider clusterSingletonService = new ClusterSingletonServiceProviderMock();
		MountPointService mountPointService = new MountPointServiceMock(mountPoint);
        NotificationPublishService notificationPublishService = new NotificationPublishServiceMock();
        RpcProviderRegistry rpcProviderRegistry = new RpcProviderRegistryMock();

        // start using blueprint interface
        String msg = "";
        try {
            deviceManager = new DeviceManagerImpl();

            deviceManager.setDataBroker(dataBrokerNetconf);
            deviceManager.setMountPointService(mountPointService);
            deviceManager.setNotificationPublishService(notificationPublishService);
            deviceManager.setRpcProviderRegistry(rpcProviderRegistry);
            deviceManager.setClusterSingletonService(clusterSingletonService);
            deviceManager.init();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            msg = sw.toString(); // stack trace as a string
            e.printStackTrace();
        }
        readOnlyTransaction.close();
        System.out.println("Initialization status: " + deviceManager.isDevicemanagerInitializationOk());
        assertTrue("Devicemanager not initialized: " + msg, deviceManager.isDevicemanagerInitializationOk());
        System.out.println("Initialization done");
        waitfordatabase();
    }

    @AfterClass
    public static void after() throws InterruptedException, IOException {

        System.out.println("Start shutdown");
        // close using blueprint interface
        try {
            deviceManager.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        delete(KARAF_ETC);

    }

    @Test
    public void test0() throws InterruptedException {
        HtDatabaseWebAPIClient client = new HtDatabaseWebAPIClient();
        try {
            String response = client.sendRequest("/mwtn/mediator-server/_search", "GET",
                    new JSONObject("{\"match\":{\"id\":id}}"));
            System.out.println(response);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {
        System.out.println("Test2: slave mountpoint");

        ReadOnlyTransactionMountpoint12Mock readOnlyTransaction = new ReadOnlyTransactionMountpoint12Mock();
        dataBrokerNetconf.setReadOnlyTransaction(readOnlyTransaction);
        mountPoint.setReadOnlyTransaction(readOnlyTransaction);
        NetconfNode nNode = readOnlyTransaction.getMock().getNetconfNode();

        mountPoint.setDatabrokerAbsent(true);
        NodeId nodeId = new NodeId("mountpointTest2");
        try {
            deviceManager.startListenerOnNodeForConnectedState(Action.CREATE, nodeId, nNode);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception received.");
        }

        readOnlyTransaction.close();
        System.out.println("Test2: Done");

    }

    @Test
    public void test3() {
        System.out.println("Test3: master mountpoint ONF Model 12");

        ReadOnlyTransactionMountpoint12Mock readOnlyTransaction = new ReadOnlyTransactionMountpoint12Mock();
        dataBrokerNetconf.setReadOnlyTransaction(readOnlyTransaction);
        mountPoint.setReadOnlyTransaction(readOnlyTransaction);
        NetconfNode nNode = readOnlyTransaction.getMock().getNetconfNode();

        mountPoint.setDatabrokerAbsent(false);
        NodeId nodeId = new NodeId("mountpointTest3");

        Capabilities capabilities = Capabilities.getAvailableCapabilities(nNode);
        System.out.println("Node capabilites: " + capabilities);

        try {
            deviceManager.startListenerOnNodeForConnectedState(Action.CREATE, nodeId, nNode);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception received.");
        }

        readOnlyTransaction.sendProblemNotification();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        readOnlyTransaction.close();
        System.out.println("Test3: Done");

    }

    @Test
    public void test4() {
        System.out.println("Test4: master mountpoint ONF Model 1211");

        ReadOnlyTransactionMountpoint1211Mock readOnlyTransaction = new ReadOnlyTransactionMountpoint1211Mock();
        dataBrokerNetconf.setReadOnlyTransaction(readOnlyTransaction);
        mountPoint.setReadOnlyTransaction(readOnlyTransaction);

        NetconfNode nNode = readOnlyTransaction.getMock().getNetconfNode();
        mountPoint.setDatabrokerAbsent(false);
        NodeId nodeId = new NodeId("mountpointTest4");

        Capabilities capabilities = Capabilities.getAvailableCapabilities(nNode);
        System.out.println("Node capabilites: " + capabilities);

        try {
            deviceManager.startListenerOnNodeForConnectedState(Action.CREATE, nodeId, nNode);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception received.");
        }

        readOnlyTransaction.sendProblemNotification();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        readOnlyTransaction.close();
        System.out.println("Test4: Done");

    }

    @Test
    public void test5() {
        System.out.println("Test5: master mountpoint ONF Model 1211p");

        ReadOnlyTransactionMountpoint1211pMock readOnlyTransaction = new ReadOnlyTransactionMountpoint1211pMock();
        dataBrokerNetconf.setReadOnlyTransaction(readOnlyTransaction);
        mountPoint.setReadOnlyTransaction(readOnlyTransaction);

        NetconfNode nNode = readOnlyTransaction.getMock().getNetconfNode();
        mountPoint.setDatabrokerAbsent(false);
        NodeId nodeId = new NodeId("mountpointTest5");

        Capabilities capabilities = Capabilities.getAvailableCapabilities(nNode);
        System.out.println("Node capabilites: " + capabilities);

        try {
            deviceManager.startListenerOnNodeForConnectedState(Action.CREATE, nodeId, nNode);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception received.");
        }
        readOnlyTransaction.sendProblemNotification();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        readOnlyTransaction.close();
        System.out.println("Test5: Done");

    }

    @Test
    public void test6() {

        System.out.println("Test6: Write zip data file file");
        File testFile = new File("etc/elasticsearch_update.zip");
        Resources.extractFileTo("elasticsearch_update.zip", testFile);
        int wait = 130;
        while (testFile.exists() && wait-- > 0) {
            System.out.println("Waiting " + wait);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }


        System.out.println("Test6: Done");

    }

    @Test
    public void test7() throws Exception {
        final int NUM = 5;
        final int ARCHIVE_DAYS = 30;
        final long ARCHIVE_LIMIT_SEC = TimeUnit.SECONDS.convert(ARCHIVE_DAYS, TimeUnit.DAYS);
        final long ARCHIVE_INTERVAL_SEC = 10;
        File propFile = KARAF_ETC.resolve("devicemanager.properties").toFile();

        ArchiveCleanService service = deviceManager.getArchiveCleanService();
        DBCleanServiceHelper helper = new DBCleanServiceHelper(deviceManager);

        // setEsConfg
        TestDevMgrPropertiesFile.writeFile(propFile, getContent(ARCHIVE_LIMIT_SEC, ARCHIVE_INTERVAL_SEC));
        //give time to read file
        sleep(5);
        System.out.println("Archive clean service configuration "+service);
        System.out.println("To delete elements older: "+service.getDateForOldElements());
        System.out.println("Status of elements is: "+service.countOldEntries());

        // create old data and check if the will be cleaned completely
        int elements = helper.writeDataToLogs(NUM, ARCHIVE_DAYS+5, 0 /*Hours*/);
        System.out.println("Written elements are: "+elements);

        waitForDeletion(service, 2 * ARCHIVE_INTERVAL_SEC, elements, "Entries are not cleared completely as expected");

        // create partial old and newer data and check that only half of all data are cleaned
        // New data are not counted as "old" ..
        int elementsToRemove = elements = helper.writeDataToLogs(NUM, ARCHIVE_DAYS+5, 0);
        elements += helper.writeDataToLogs(NUM, ARCHIVE_DAYS-5, 0);
        waitForDeletion(service, 2 * ARCHIVE_INTERVAL_SEC, elementsToRemove, "Entries are not cleared exactly half as expected");

        // create only newer data and check that nothing is cleaned
        elements = helper.writeDataToLogs(NUM, ARCHIVE_DAYS+2, 0);
        waitForDeletion(service, 2 * ARCHIVE_INTERVAL_SEC, elements, "Some entries were removed, but shouldn't.");

        service.close();
    }

    // ********************* Private

    private void waitForDeletion(ArchiveCleanService service, long timeout, long numberAtBeginning, String faultMessage) {
        int numberEntries = 0;
        while (timeout-- > 0) {
            sleep(1000);
            numberEntries = service.countOldEntries();
            if (numberEntries <= 0) {
                break;
            }
        }
        if (timeout == 0) {
            fail(faultMessage + " Timeout at:" + timeout + " Entries at beginning " + numberAtBeginning
                    + " remaining" + numberEntries);
        }
    }


    private static void waitfordatabase() throws InterruptedException {

        System.out.println("Test1: Wait for database");
        int timeout = DATABASETIMEOUTSECONDS;
        while (!deviceManager.isDatabaseInitializationFinished() && timeout-- > 0) {
            System.out.println("Test1: " + timeout);
            Thread.sleep(1000); // On second
        }
        System.out.println("Ddatabase initialized");
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOG.warn(e.getMessage());
            Thread.interrupted();
        }
    }

    private static void waitEnter() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter");
        sc.next();
        sc.close();
    }

    private static void delete(Path etc) throws IOException {
        if (Files.exists(etc)) {
            System.out.println("Found and remove:" + etc.toString());
            delete(etc.toFile());
        }
    }

    private static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }

    private String getContent(long archiveLimitSeconds, long esArchiveCheckIntervalSeconds) {
        return "[dcae]\n" + "dcaeUserCredentials=admin:admin\n" + "dcaeUrl=http://localhost:45/abc\n"
                + "dcaeHeartbeatPeriodSeconds=120\n" + "dcaeTestCollector=no\n" + "\n" + "[aots]\n"
                + "userPassword=passwd\n" + "soapurladd=off\n" + "soapaddtimeout=10\n" + "soapinqtimeout=20\n"
                + "userName=user\n" + "inqtemplate=inqreq.tmpl.xml\n" + "assignedto=userid\n"
                + "addtemplate=addreq.tmpl.xml\n" + "severitypassthrough=critical,major,minor,warning\n"
                + "systemuser=user\n" + "prt-offset=1200\n" + "soapurlinq=off\n" + "#smtpHost=\n" + "#smtpPort=\n"
                + "#smtpUsername=\n" + "#smtpPassword=\n" + "#smtpSender=\n" + "#smtpReceivers=\n" + "\n" + "[es]\n"
                + "esCluster=sendateodl5\n" + "esArchiveLifetimeSeconds=" + archiveLimitSeconds + "\n" + "esArchiveCheckIntervalSeconds="
                + esArchiveCheckIntervalSeconds + "\n" + "\n" + "[aai]\n" + "#keep comment\n"
                + "aaiHeaders=[\"X-TransactionId: 9999\"]\n" + "aaiUrl=off\n" + "aaiUserCredentials=AAI:AAI\n"
                + "aaiDeleteOnMountpointRemove=true\n" + "aaiTrustAllCerts=false\n" + "aaiApiVersion=aai/v13\n"
                + "aaiPropertiesFile=aaiclient.properties\n" + "\n" + "[pm]\n" + "pmCluster=sendateodl5\n"
                + "pmEnabled=true\n" + "[toggleAlarmFilter]\n" + "taEnabled=false\n" + "taDelay=5555\n" + "";
    }


}
