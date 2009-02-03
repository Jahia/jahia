<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.lang.management.ThreadMXBean" %>
<%@ page import="java.lang.management.ManagementFactory" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.lang.management.ThreadInfo" %>
<%--
  Created by IntelliJ IDEA.
  User: loom
  Date: Nov 12, 2008
  Time: 3:41:07 PM
  To change this template use File | Settings | File Templates.
--%>
<html>
<head><title>Current thread dump</title></head>
<body>
    <h1>Thread state information</h1>
    <h2>Deadlocked threads</h2>
<table border="1" cellpadding="1" cellspacing="0">
    <thead>
        <tr>
            <th>Thread ID</th>
        </tr>
    </thead>
    <%
        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreadIDs = mbean.findMonitorDeadlockedThreads();
        if (deadlockedThreadIDs != null) {
            for (long currentThreadID : deadlockedThreadIDs) {
                out.print("<tr><td>"+currentThreadID+"</td></tr>");
            }
        }
    %>
</table>
    <h2>Thread info data</h2>
<table border="1" cellpadding="1" cellspacing="0">
    <thead>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>State</th>
            <th>Lock name</th>
            <th>Lock owner ID</th>
            <th>Lock owner name</th>
            <th>Blocked count</th>
            <th>Blocked time</th>
            <th>Waited count</th>
            <th>Waited time</th>
            <th>In native</th>
            <th>Suspended</th>
            <th>Stack trace</th>
        </tr>
    </thead>
    <%
        long[] threadIDs = mbean.getAllThreadIds();
        if (threadIDs != null) {
            for (long currentThreadID : threadIDs) {
                ThreadInfo curThreadInfo = mbean.getThreadInfo(currentThreadID, Integer.MAX_VALUE);
                if (curThreadInfo != null) {
                    out.print("<tr>");
                    out.print("<td>");
                    out.print(currentThreadID);
                    out.print("</td>");
                    out.print("<td>");
                    out.print(curThreadInfo.getThreadName());
                    out.print("</td>");
                    out.print("<td>");
                    out.print(curThreadInfo.getThreadState());
                    out.print("</td>");
                    out.print("<td>");
                    out.print(curThreadInfo.getLockName());
                    out.print("</td>");
                    out.print("<td>");
                    out.print(curThreadInfo.getLockOwnerId());
                    out.print("</td>");
                    out.print("<td>");
                    out.print(curThreadInfo.getLockOwnerName());
                    out.print("</td>");
                    out.print("<td>");
                    out.print(curThreadInfo.getBlockedCount());
                    out.print("</td>");
                    out.print("<td>");
                    out.print(curThreadInfo.getBlockedTime());
                    out.print("</td>");
                    out.print("<td>");
                    out.print(curThreadInfo.getWaitedCount());
                    out.print("</td>");
                    out.print("<td>");
                    out.print(curThreadInfo.getWaitedTime());
                    out.print("</td>");                   
                    out.print("<td>");
                    out.print(curThreadInfo.isInNative());
                    out.print("</td>");
                    out.print("<td>");
                    out.print(curThreadInfo.isSuspended());
                    out.print("</td>");

                    out.print("<td>");
                    for (StackTraceElement curStackElement : curThreadInfo.getStackTrace()) {
                        out.print(curStackElement.toString() + "<br/>");
                    }
                    out.print("</td>");
                    out.print("</tr>");
                }
            }
        }
    %>
</table>
    <h2>Thread dump</h2>
<table border="1" cellpadding="1" cellspacing="0">
    <thead>
        <tr>
            <th>Thread ID</th>
            <th>Thread name</th>
            <th>Thread stack</th>
        </tr>
    </thead>
    <%
        Set<Map.Entry<Thread, StackTraceElement[]>> threadMap = Thread.getAllStackTraces().entrySet();
        for (Map.Entry<Thread, StackTraceElement[]> curEntry : threadMap) {
            Thread currentThread = curEntry.getKey();
            out.print("<tr>");
            out.print("<td>"+currentThread.getId() + "</td>");
            out.print("<td>"+currentThread.toString()+"</td>");
            out.print("<td>");
            for (StackTraceElement curStackElement : curEntry.getValue()) {
                out.print(curStackElement.toString() + "<br/>");
            }
            out.print("</td>");
            out.print("</tr>");

        }
    %>
</table>
</body>
</html>