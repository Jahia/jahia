/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.tools.jvm;

import static java.lang.management.ManagementFactory.*;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.ThreadInfo;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.management.*;
import java.io.*;

/**
 * Warning : generating thread dumps is an operation that locks the JVM and therefore should not be done while
 * high load is running on the system.
 */
public class ThreadMonitor {
    private MBeanServerConnection server;
    private ThreadMXBean tmbean;

    private String dumpPrefix = "\nFull thread dump ";

    /**
     * Constructs a ThreadMonitor object to get thread information in a remote JVM.
     */
    public ThreadMonitor(MBeanServerConnection server) {
        setMBeanServerConnection(server);
        try {
            parseMBeanInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs a ThreadMonitor object to get thread information in the local JVM.
     */
    public ThreadMonitor() {
        this(getPlatformMBeanServer());
    }

    /**
     * Prints the thread dump information to System.out.
     */
    public void dumpThreadInfo() {
        generateThreadInfo(new PrintWriter(System.out));
    }

    /**
     * Generates a string with the full thread dump information.
     * 
     * @return
     */
    public void generateThreadInfo(PrintWriter writer) {
        StringBuilder dump = new StringBuilder();

        dumpThreadInfo(dump);

        writer.println(dump.toString());
    }

    private static String INDENT = "    ";

    private void dumpThreadInfo(StringBuilder dump) {
        dump.append(getDumpDate());
        dump.append(dumpPrefix);
        dump.append("\n");
        long[] tids = tmbean.getAllThreadIds();
        ThreadInfo[] tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
        for (int i = 0; i < tinfos.length; i++) {
            ThreadInfo ti = tinfos[i];
            printThreadInfo(ti, dump);
        }
    }

    private void printThreadInfo(ThreadInfo ti, StringBuilder dump) {
        // print thread information
        printThread(ti, dump);

        // print stack trace with locks
        StackTraceElement[] stacktrace = ti.getStackTrace();
        for (int i = 0; i < stacktrace.length; i++) {
            StackTraceElement ste = stacktrace[i];
            dump.append(INDENT + "at " + ste.toString());
            dump.append("\n");
        }
        dump.append("\n");
    }

    private void printThread(ThreadInfo ti, StringBuilder dump) {
        StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\""
                + " nid=" + ti.getThreadId() + " state=" + ti.getThreadState());
        if (ti.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (ti.isInNative()) {
            sb.append(" (running in native)");
        }
        sb.append(" []\n"
                + ti.getThreadState().getClass().getName().replace("$", ".")
                + ": " + ti.getThreadState());
        if (ti.getLockName() != null
                && ti.getThreadState() != Thread.State.BLOCKED) {
            String[] lockInfo = ti.getLockName().split("@");
            sb.append("\n" + INDENT + "- waiting on <0x" + lockInfo[1]
                    + "> (a " + lockInfo[0] + ")");
            sb.append("\n" + INDENT + "- locked <0x" + lockInfo[1] + "> (a "
                    + lockInfo[0] + ")");
        } else if (ti.getLockName() != null
                && ti.getThreadState() == Thread.State.BLOCKED) {
            String[] lockInfo = ti.getLockName().split("@");
            sb.append("\n" + INDENT + "- waiting to lock <0x" + lockInfo[1]
                    + "> (a " + lockInfo[0] + ")");
        }
        dump.append(sb.toString());
        dump.append("\n");
        if (ti.getLockOwnerName() != null) {
            dump.append(INDENT + " owned by " + ti.getLockOwnerName() + " id="
                    + ti.getLockOwnerId());
            dump.append("\n");
        }
    }

    /**
     * create dump date similar to format used by 1.6 VMs
     * 
     * @return dump date (e.g. 2007-10-25 08:00:00)
     */
    private String getDumpDate() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.ENGLISH);
        return (sdfDate.format(new Date()));
    }

    /**
     * Checks if any threads are deadlocked. If any, print the thread dump information.
     */
    public String findDeadlock() {
        StringBuilder dump = new StringBuilder();
        long[] tids = tmbean.findMonitorDeadlockedThreads();
        if (tids == null) {
            return null;
        }
        dump.append("\n\nFound one Java-level deadlock:\n");
        dump.append("==============================\n");
        ThreadInfo[] infos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
        for (int i = 1; i < infos.length; i++) {
            ThreadInfo ti = infos[i];
            // print thread information
            printThreadInfo(ti, dump);
        }

        return (dump.toString());
    }

    /**
     * reset mbean server connection
     * 
     * @param mbs
     */
    void setMBeanServerConnection(MBeanServerConnection mbs) {
        this.server = mbs;
        try {
            this.tmbean = (ThreadMXBean) ManagementFactory
                    .newPlatformMXBeanProxy(server,
                            ManagementFactory.THREAD_MXBEAN_NAME,
                            ThreadMXBean.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseMBeanInfo() throws IOException {
            setDumpPrefix();
    }

    private void setDumpPrefix() {
        try {
            RuntimeMXBean rmbean = (RuntimeMXBean) ManagementFactory
                    .newPlatformMXBeanProxy(server,
                            ManagementFactory.RUNTIME_MXBEAN_NAME,
                            RuntimeMXBean.class);
            dumpPrefix += rmbean.getVmName() + " (" + rmbean.getVmVersion()
                    + ")\n";
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}