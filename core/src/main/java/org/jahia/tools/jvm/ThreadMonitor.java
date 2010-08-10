/*
 * @(#)ThreadMonitor.java	1.3 04/07/27
 * 
 * Copyright (c) 2004 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduce the above copyright notice, 
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may 
 * be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL 
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST 
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY 
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, 
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
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
