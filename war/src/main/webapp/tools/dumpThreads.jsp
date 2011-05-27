<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.lang.management.ThreadMXBean" %>
<%@ page import="java.lang.management.ManagementFactory" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.lang.management.ThreadInfo" %>
<%--
  User: loom
  Date: Nov 12, 2008
  Time: 3:41:07 PM
--%>
<html>
<head>
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>Current thread dump</title>
</head>
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