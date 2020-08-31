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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.http.about;

import java.io.File;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class SystemInfo {
    private static NumberFormat fmtI = new DecimalFormat("###,###", new DecimalFormatSymbols(Locale.ENGLISH));
    private static NumberFormat fmtDec = new DecimalFormat("###,###.##", new DecimalFormatSymbols(Locale.ENGLISH));
    private static NumberFormat fmtD = new DecimalFormat("###,##0.000", new DecimalFormatSymbols(Locale.ENGLISH));
    private static OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    protected static boolean showMemoryPools = false;

    public static String getOnapVersion(String def) {
        return getOnapVersion("", def);
    }

    public static String getMdSalVersion(String def) {
        return getMdSalVersion("", def);
    }

    public static String getYangToolsVersion(String def) {
        return getYangToolsVersion("", def);
    }

    public static String getOnapVersion(String baseOdlDirectory, String def) {
        return getFeatureVersionByFolder(baseOdlDirectory, "system/org/onap/sdnc/northbound/sdnc-northbound-all/", def);
    }

    public static String getMdSalVersion(String baseOdlDirectory, String def) {
        return getFeatureVersionByFolder(baseOdlDirectory, "system/org/opendaylight/mdsal/mdsal-binding-api/", def);
    }

    public static String getYangToolsVersion(String baseOdlDirectory, String def) {
        return getFeatureVersionByFolder(baseOdlDirectory, "system/org/opendaylight/yangtools/odl-yangtools-common/",
                def);
    }

    private static String getFeatureVersionByFolder(String baseOdlDirectory, String dir, String def) {
        final String regex = "^[0-9]+\\.[0-9]+\\.[0-9]+(-SNAPSHOT)?$";
        Stream<Path> entries = null;
        try {
            if (baseOdlDirectory != null && baseOdlDirectory.length() > 0 && !baseOdlDirectory.endsWith("/")) {
                baseOdlDirectory += "/";
            }
            entries = Files.list(new File(baseOdlDirectory + dir).toPath());
        } catch (IOException e) {

        }
        if (entries == null) {
            return def;
        }
        final Pattern pattern = Pattern.compile(regex);

        Iterator<Path> it = entries.iterator();
        Path p;
        File f;
        while (it.hasNext()) {
            p = it.next();
            f = p.toFile();
            if (f.isDirectory()) {
                final Matcher matcher = pattern.matcher(f.getName().toString());
                if (matcher.find()) {
                    def = matcher.group(0);
                    break;
                }
            }
        }
        entries.close();
        return def;
    }

    public static String get() throws Exception {
        StringBuilder sb = new StringBuilder();
        int maxNameLen;

        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();

        //
        // print Karaf informations
        //
        maxNameLen = 25;
        sb.append("Karaf\n");
        printValue(sb, "Karaf version", maxNameLen, System.getProperty("karaf.version"));
        printValue(sb, "Karaf home", maxNameLen, System.getProperty("karaf.home"));
        printValue(sb, "Karaf base", maxNameLen, System.getProperty("karaf.base"));
        String osgi = getOsgiFramework();
        if (osgi != null) {
            printValue(sb, "OSGi Framework", maxNameLen, osgi);
        }

        sb.append("JVM\n");
        printValue(sb, "Java Virtual Machine", maxNameLen, runtime.getVmName() + " version " + runtime.getVmVersion());
        printValue(sb, "Version", maxNameLen, System.getProperty("java.version"));
        printValue(sb, "Vendor", maxNameLen, runtime.getVmVendor());
        printValue(sb, "Pid", maxNameLen, getPid());
        printValue(sb, "Uptime", maxNameLen, printDuration(runtime.getUptime()));
        try {
            Class<?> sunOS = Class.forName("com.sun.management.OperatingSystemMXBean");
            printValue(sb, "Process CPU time", maxNameLen,
                    printDuration(getValueAsLong(sunOS, "getProcessCpuTime") / 1000000.0));
            printValue(sb, "Process CPU load", maxNameLen, fmtDec.format(getValueAsDouble(sunOS, "getProcessCpuLoad")));
            printValue(sb, "System CPU load", maxNameLen, fmtDec.format(getValueAsDouble(sunOS, "getSystemCpuLoad")));
        } catch (Throwable t) {
        }
        try {
            Class<?> unixOS = Class.forName("com.sun.management.UnixOperatingSystemMXBean");
            printValue(sb, "Open file descriptors", maxNameLen,
                    printLong(getValueAsLong(unixOS, "getOpenFileDescriptorCount")));
            printValue(sb, "Max file descriptors", maxNameLen,
                    printLong(getValueAsLong(unixOS, "getMaxFileDescriptorCount")));
        } catch (Throwable t) {
        }
        printValue(sb, "Total compile time", maxNameLen,
                printDuration(ManagementFactory.getCompilationMXBean().getTotalCompilationTime()));

        sb.append("Threads\n");
        printValue(sb, "Live threads", maxNameLen, Integer.toString(threads.getThreadCount()));
        printValue(sb, "Daemon threads", maxNameLen, Integer.toString(threads.getDaemonThreadCount()));
        printValue(sb, "Peak", maxNameLen, Integer.toString(threads.getPeakThreadCount()));
        printValue(sb, "Total started", maxNameLen, Long.toString(threads.getTotalStartedThreadCount()));

        sb.append("Memory\n");
        printValue(sb, "Current heap size", maxNameLen, printSizeInKb(mem.getHeapMemoryUsage().getUsed()));
        printValue(sb, "Maximum heap size", maxNameLen, printSizeInKb(mem.getHeapMemoryUsage().getMax()));
        printValue(sb, "Committed heap size", maxNameLen, printSizeInKb(mem.getHeapMemoryUsage().getCommitted()));
        printValue(sb, "Pending objects", maxNameLen, Integer.toString(mem.getObjectPendingFinalizationCount()));
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            String val = "Name = '" + gc.getName() + "', Collections = " + gc.getCollectionCount() + ", Time = "
                    + printDuration(gc.getCollectionTime());
            printValue(sb, "Garbage collector", maxNameLen, val);
        }

        //		if (showMemoryPools) {
        //			List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        //			sb.append("Memory Pools\n");
        //			printValue(sb, "Total Memory Pools", maxNameLen, printLong(memoryPools.size()));
        //			String spaces4 = "   ";
        //			for (MemoryPoolMXBean pool : memoryPools) {
        //				String name = pool.getName();
        //				MemoryType type = pool.getType();
        //				printValue(sb, spaces4 + "Pool (" + type + ")", maxNameLen, name);
        //
        //				// PeakUsage/CurrentUsage
        //				MemoryUsage peakUsage = pool.getPeakUsage();
        //				MemoryUsage usage = pool.getUsage();
        //
        //				if (usage != null && peakUsage != null) {
        //					long init = peakUsage.getInit();
        //					long used = peakUsage.getUsed();
        //					long committed = peakUsage.getCommitted();
        //					long max = peakUsage.getMax();
        //					sb.append(spaces4 + spaces4 + "Peak Usage\n");
        //					printValue(sb, spaces4 + spaces4 + spaces4 + "init", maxNameLen, printLong(init));
        //					printValue(sb, spaces4 + spaces4 + spaces4 + "used", maxNameLen, printLong(used));
        //					printValue(sb, spaces4 + spaces4 + spaces4 + "committed", maxNameLen, printLong(committed));
        //					printValue(sb, spaces4 + spaces4 + spaces4 + "max", maxNameLen, printLong(max));
        //
        //					init = usage.getInit();
        //					used = usage.getUsed();
        //					committed = usage.getCommitted();
        //					max = usage.getMax();
        //					sb.append(spaces4 + spaces4 + "Current Usage\n");
        //					printValue(sb, spaces4 + spaces4 + spaces4 + "init", maxNameLen, printLong(init));
        //					printValue(sb, spaces4 + spaces4 + spaces4 + "used", maxNameLen, printLong(used));
        //					printValue(sb, spaces4 + spaces4 + spaces4 + "committed", maxNameLen, printLong(committed));
        //					printValue(sb, spaces4 + spaces4 + spaces4 + "max", maxNameLen, printLong(max));
        //				}
        //			}
        //		}

        sb.append("Classes\n");
        printValue(sb, "Current classes loaded", maxNameLen, printLong(cl.getLoadedClassCount()));
        printValue(sb, "Total classes loaded", maxNameLen, printLong(cl.getTotalLoadedClassCount()));
        printValue(sb, "Total classes unloaded", maxNameLen, printLong(cl.getUnloadedClassCount()));

        sb.append("Operating system\n");
        printValue(sb, "Name", maxNameLen, os.getName() + " version " + os.getVersion());
        printValue(sb, "Architecture", maxNameLen, os.getArch());
        printValue(sb, "Processors", maxNameLen, Integer.toString(os.getAvailableProcessors()));
        try {
            printValue(sb, "Total physical memory", maxNameLen,
                    printSizeInKb(getSunOsValueAsLong(os, "getTotalPhysicalMemorySize")));
            printValue(sb, "Free physical memory", maxNameLen,
                    printSizeInKb(getSunOsValueAsLong(os, "getFreePhysicalMemorySize")));
            printValue(sb, "Committed virtual memory", maxNameLen,
                    printSizeInKb(getSunOsValueAsLong(os, "getCommittedVirtualMemorySize")));
            printValue(sb, "Total swap space", maxNameLen,
                    printSizeInKb(getSunOsValueAsLong(os, "getTotalSwapSpaceSize")));
            printValue(sb, "Free swap space", maxNameLen,
                    printSizeInKb(getSunOsValueAsLong(os, "getFreeSwapSpaceSize")));
        } catch (Throwable t) {
        }
        return sb.toString();
    }

    private static String getPid() {
        // In Java 9 the new process API can be used:
        // long pid = ProcessHandle.current().getPid();
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String[] parts = name.split("@");
        return parts[0];
    }

    private static long getSunOsValueAsLong(OperatingSystemMXBean os, String name) throws Exception {
        Method mth = os.getClass().getMethod(name);
        return (Long) mth.invoke(os);
    }

    private static long getValueAsLong(Class<?> osImpl, String name) throws Exception {
        if (osImpl.isInstance(os)) {
            Method mth = osImpl.getMethod(name);
            return (Long) mth.invoke(os);
        }
        return -1;
    }

    private static double getValueAsDouble(Class<?> osImpl, String name) throws Exception {
        if (osImpl.isInstance(os)) {
            Method mth = osImpl.getMethod(name);
            return (Double) mth.invoke(os);
        }
        return -1;
    }

    private static String printLong(long i) {
        return fmtI.format(i);
    }

    private static String printSizeInKb(double size) {
        return fmtI.format((long) (size / 1024)) + " kbytes";
    }

    protected static String printDuration(double uptime) {
        uptime /= 1000;
        if (uptime < 60) {
            return fmtD.format(uptime) + " seconds";
        }
        uptime /= 60;
        if (uptime < 60) {
            long minutes = (long) uptime;
            String s = fmtI.format(minutes) + (minutes > 1 ? " minutes" : " minute");
            return s;
        }
        uptime /= 60;
        if (uptime < 24) {
            long hours = (long) uptime;
            long minutes = (long) ((uptime - hours) * 60);
            String s = fmtI.format(hours) + (hours > 1 ? " hours" : " hour");
            if (minutes != 0) {
                s += " " + fmtI.format(minutes) + (minutes > 1 ? " minutes" : " minute");
            }
            return s;
        }
        uptime /= 24;
        long days = (long) uptime;
        long hours = (long) ((uptime - days) * 24);
        String s = fmtI.format(days) + (days > 1 ? " days" : " day");
        if (hours != 0) {
            s += " " + fmtI.format(hours) + (hours > 1 ? " hours" : " hour");
        }
        return s;
    }

    static void printSysValue(StringBuilder sb, String prop, int pad) {
        printValue(sb, prop, pad, System.getProperty(prop));
    }

    static void printValue(StringBuilder sb, String name, int pad, String value) {
        sb.append("  " + // SimpleAnsi.INTENSITY_BOLD +
                name + // SimpleAnsi.INTENSITY_NORMAL +
                spaces(pad - name.length()) + "   " + value + "\n");
    }

    static String spaces(int nb) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nb; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    static String getOsgiFramework() {
        try {
            Callable<String> call = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
                    Bundle sysBundle = context.getBundle(0);
                    return sysBundle.getSymbolicName() + "-" + sysBundle.getVersion();
                }
            };
            return call.call();
        } catch (Throwable t) {
            // We're not in OSGi, just safely return null
            return null;
        }
    }
}
