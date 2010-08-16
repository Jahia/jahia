/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

/*
 * @(#)ThreadMonitor.java	1.3 04/07/27
 */
package org.jahia.tools.jvm;

import static java.lang.management.ManagementFactory.*;
import java.lang.management.ThreadMXBean;
import java.lang.management.ThreadInfo;
import javax.management.*;
import java.io.*;

/**
 * Example of using the java.lang.management API to dump stack trace
 * and to perform deadlock detection.
 *
 * @author  Mandy Chung
 * @version %% 07/27/04
 */
public class ThreadMonitor {
    private ThreadMXBean tmbean;

    /**
     * Constructs a ThreadMonitor object to get thread information
     * in a remote JVM.
     */
    public ThreadMonitor(MBeanServerConnection server) throws IOException {
       this.tmbean = newPlatformMXBeanProxy(server,
                                            THREAD_MXBEAN_NAME,
                                            ThreadMXBean.class);
    }

    /**
     * Constructs a ThreadMonitor object to get thread information
     * in the local JVM.
     */
    public ThreadMonitor() {
        this.tmbean = getThreadMXBean();
    }

    /**
     * Prints the thread dump information to System.out.
     */
    public void dumpThreadInfo() {
        generateThreadInfo(new PrintWriter(System.out));
    }

    /**
     * Generates a string with the full thread dump information.
     * @return
     */
    public void generateThreadInfo(PrintWriter writer) {
        writer.println("Full Java thread dump\n");
        long[] tids = tmbean.getAllThreadIds();
        ThreadInfo[] tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
        for (ThreadInfo ti : tinfos) {
            writer.print(generateThreadInfo(ti));
        }
    }

    private static String INDENT = "    ";
    private String generateThreadInfo(ThreadInfo ti) {
       StringBuilder sb = new StringBuilder("\"" + ti.getThreadName() + "\"" +
                                            " Id=" + ti.getThreadId() +
                                            " in " + ti.getThreadState());
       if (ti.getLockName() != null) {
           sb.append(" on lock=" + ti.getLockName()); 
       }
       if (ti.isSuspended()) {
           sb.append(" (suspended)");
       }
       if (ti.isInNative()) {
           sb.append(" (running in native)");
       }
       sb.append("\n");
       if (ti.getLockOwnerName() != null) {
            sb.append(INDENT + " owned by " + ti.getLockOwnerName() +
                               " Id=" + ti.getLockOwnerId() + "\n");
       }
       for (StackTraceElement ste : ti.getStackTrace()) {
           sb.append(INDENT + "at " + ste.toString() + "\n");
       }
       return sb.toString();
    }

    /**
     * Checks if any threads are deadlocked. If any, print
     * the thread dump information.
     */
    public boolean findDeadlock(PrintWriter writer) {
       long[] tids = tmbean.findMonitorDeadlockedThreads();
       if (tids == null) {
           writer.println("No deadlock found.");
           return false;
       } else {
           writer.println("Deadlock found :-");
           ThreadInfo[] tinfos = tmbean.getThreadInfo(tids, Integer.MAX_VALUE);
           for (ThreadInfo ti : tinfos) {
               writer.print(generateThreadInfo(ti));
           }
           return true;
       }
    }
}
