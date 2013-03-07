<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.jahia.bin.errors.ErrorFileDumper" %>
<%@ page import="org.jahia.tools.jvm.ThreadMonitor" %>
<%@ page import="java.util.Enumeration" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<script type="text/javascript">
    $(document).ready(function () {
        $("#accordion").accordion({collapsible:true, heightStyle:"content"});
    })
</script>
<%
    Long freeMemoryInBytes = new Long(Runtime.getRuntime().freeMemory());
    Long totalMemoryInBytes = new Long(Runtime.getRuntime().totalMemory());
    Long maxMemoryInBytes = new Long(Runtime.getRuntime().maxMemory());
    long freeMemoryInKBytes = freeMemoryInBytes.longValue() >> 10;
    long freeMemoryInMBytes = freeMemoryInBytes.longValue() >> 20;
    long totalMemoryInKBytes = totalMemoryInBytes.longValue() >> 10;
    long totalMemoryInMBytes = totalMemoryInBytes.longValue() >> 20;
    long maxMemoryInKBytes = maxMemoryInBytes.longValue() >> 10;
    long maxMemoryInMBytes = maxMemoryInBytes.longValue() >> 20;
    pageContext.setAttribute("timestamp", System.currentTimeMillis());
%>
<div id="content" class="fit">
    <div class="head">
        <div class="object-title">
            <fmt:message key="org.jahia.admin.serverStatus.label"/>
        </div>
        <div style="float:right;display:inline;padding:5px 10px 0 0">
            <a href="<c:url value='/tools/systemInfo.jsp?file=true'/>" target="_blank"><img
                    src="<c:url value='/icons/download.png'/>" height="16" width="16" alt=" " align="top"/><fmt:message
                    key="org.jahia.admin.status.ManageStatus.downloadStatus"/></a>
        </div>
    </div>
</div>
<p>&nbsp;</p>
<div id="accordion">
<h3><fmt:message key="org.jahia.admin.status.ManageStatus.title.memorySection.label"/></h3>
<c:if test="${param['gc']}">
    <% System.gc(); %>
</c:if>
<div>
    <table width="100%" border="0" cellspacing="0" cellpadding="5">
        <tr>
            <td width="100%">
                <strong><fmt:message
                        key="org.jahia.admin.status.ManageStatus.maxJvmMemory.label"/>&nbsp;:</strong><br>
            </td>
            <td>
                <%=maxMemoryInMBytes%>&nbsp;<fmt:message
                    key="org.jahia.admin.status.ManageStatus.mB.label"/>&nbsp;(<%=maxMemoryInKBytes%>
                &nbsp;<fmt:message key="org.jahia.admin.status.ManageStatus.kB.label"/>)
            </td>
        </tr>
        <tr>
            <td width="100%">
                <strong><fmt:message
                        key="org.jahia.admin.status.ManageStatus.totalJvmMemory.label"/>&nbsp;:</strong><br>
            </td>
            <td>
                <%=totalMemoryInMBytes%>&nbsp;<fmt:message
                    key="org.jahia.admin.status.ManageStatus.mB.label"/>&nbsp;(<%=totalMemoryInKBytes%>
                &nbsp;<fmt:message key="org.jahia.admin.status.ManageStatus.kB.label"/>)
            </td>
        </tr>
        <tr>
            <td width="100%">
                <strong><fmt:message
                        key="org.jahia.admin.status.ManageStatus.freeMemory.label"/>&nbsp;:</strong><br>
            </td>
            <td>
                <%=freeMemoryInMBytes%>&nbsp;<fmt:message
                    key="org.jahia.admin.status.ManageStatus.mB.label"/>&nbsp;(<%=freeMemoryInKBytes%>
                &nbsp;<fmt:message key="org.jahia.admin.status.ManageStatus.kB.label"/>)
            </td>
        </tr>
        <tr>
            <td colspan="2" align="left">
                <a href="?do=status&amp;sub=display&amp;gc=true&amp;timestamp=${timestamp}#memory"><img
                        src="<c:url value='/icons/showTrashboard.png'/>" height="16" width="16" alt=" "
                        align="top"/><fmt:message
                        key="org.jahia.admin.status.ManageStatus.runGarbageCollector"/></a>
            </td>
        </tr>
    </table>
</div>

<h3><fmt:message key="org.jahia.admin.status.ManageStatus.title.threadsSection.label"/></h3>
<c:if test="${empty param.threadDumpCount && (param.threadDump == 'sysout' || param.threadDump == 'file')}">
    <% ThreadMonitor.getInstance().dumpThreadInfo("sysout".equals(request.getParameter("threadDump")), "file".equals(
            request.getParameter("threadDump"))); %>
</c:if>
<c:if test="${not empty param.threadDumpCount && (param.threadDump == 'sysout' || param.threadDump == 'file')}">
    <% ThreadMonitor.getInstance().dumpThreadInfoWithInterval("sysout".equals(request.getParameter("threadDump")),
            "file".equals(request.getParameter("threadDump")), Integer.parseInt(StringUtils.defaultIfEmpty(
            request.getParameter("threadDumpCount"), "10")), Integer.parseInt(StringUtils.defaultIfEmpty(
            request.getParameter("threadDumpInterval"), "10"))); %>
</c:if>
<div>
    <table width="100%" border="0" cellspacing="0" cellpadding="5">
        <tr>
            <td align="left">
                <a href="<c:url value='/tools/threadDump.jsp'/>" target="_blank"><img
                        src="<c:url value='/icons/filePreview.png'/>" height="16" width="16" alt=" "
                        align="top"/><fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump"/>
                    (<fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump.window"/>)</a>
            </td>
        </tr>
        <tr>
            <td align="left">
                <a href="<c:url value='/tools/threadDump.jsp?file=true'/>" target="_blank"><img
                        src="<c:url value='/icons/download.png'/>" height="16" width="16" alt=" "
                        align="top"/><fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump"/>
                    (<fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump.download"/>)</a>
            </td>
        </tr>
        <tr>
            <td align="left">
                <a href="?do=status&amp;sub=display&amp;threadDump=sysout&amp;timestamp=${timestamp}#threads"><img
                        src="<c:url value='/icons/tab-workflow.png'/>" height="16" width="16" alt=" "
                        align="top"/><fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump"/>
                    (System.out)</a>
            </td>
        </tr>
        <tr>
            <td align="left">
                <a href="?do=status&amp;sub=display&amp;threadDump=file&amp;timestamp=${timestamp}#threads"><img
                        src="<c:url value='/icons/globalRepository.png'/>" height="16" width="16" alt=" "
                        align="top"/><fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump"/>
                    (<fmt:message key="fileMenu.label"/>)</a>
            </td>
        </tr>
        <tr>
            <td align="left">
                <a href="#dump"
                   onclick="this.href='?do=status&amp;sub=display&amp;threadDump=file&amp;threadDumpCount=' + document.getElementById('threadDumpCount').value + '&amp;threadDumpInterval=' + document.getElementById('threadDumpInterval').value + '&amp;timestamp=${timestamp}#threads'; return true;"><img
                        src="<c:url value='/icons/workflowManager.png'/>" height="16" width="16" alt=" "
                        align="top"/><fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump"/>
                    (<fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump.multiple"/>)</a>
                &nbsp;&nbsp;
                <label for="threadDumpCount"><fmt:message key="column.count.label"/>:&nbsp;</label><input
                    type="text" id="threadDumpCount" name="threadDumpCount" size="2"
                    value="${not empty param.threadDumpCount ? param.threadDumpCount : '10'}"/>
                &nbsp;&nbsp;
                <label for="threadDumpInterval"><fmt:message key="label.interval"/>:&nbsp;</label><input type="text"
                                                                                                         id="threadDumpInterval"
                                                                                                         name="threadDumpInterval"
                                                                                                         size="2"
                                                                                                         value="${not empty param.threadDumpInterval ? param.threadDumpInterval : '10'}"/>&nbsp;<fmt:message
                    key="label.seconds"/>
            </td>
        </tr>
        <tr>
            <td align="left"><img src="<c:url value='/icons/tab-workflow.png'/>" height="16" width="16" alt=" "
                                  align="top"/>
                <%
                    String enableThreadMonitor = request.getParameter("enableThreadMonitor");
                    if ("true".equals(enableThreadMonitor)) {
                        ThreadMonitor.getInstance().setActivated(true);
                    } else if ("false".equals(enableThreadMonitor)) {
                        ThreadMonitor.getInstance().setActivated(false);
                    }

                %>
                <fmt:message
                        key="label.thread.monitor.is"/>&nbsp;<%if (ThreadMonitor.getInstance().isActivated()) {%>
                <fmt:message key="label.started"/>
                <% } else { %><fmt:message key="label.stopped"/> <% } %> -
                <%if (ThreadMonitor.getInstance().isActivated()) {%><a
                        href="?do=status&amp;sub=display&amp;enableThreadMonitor=false#threads"><fmt:message
                        key="label.stop.thread.monitor"/></a>
                <% } else { %><a href="?do=status&amp;sub=display&amp;enableThreadMonitor=true#threads"><fmt:message
                        key="label.start.thread.monitor"/></a><% } %>
            </td>
        </tr>
        <tr>
            <td align="left"><img src="<c:url value='/icons/tab-workflow.png'/>" height="16" width="16" alt=" "
                                  align="top"/>
                <%
                    String enableErrorFileDumper = request.getParameter("enableErrorFileDumper");
                    if ("true".equals(enableErrorFileDumper)) {
                        ErrorFileDumper.setFileDumpActivated(true);
                    } else if ("false".equals(enableErrorFileDumper)) {
                        ErrorFileDumper.setFileDumpActivated(false);
                    }

                %>
                <fmt:message key="label.error.file.dumper.is"/>&nbsp;<%if (!ErrorFileDumper.isShutdown()) {%>
                <fmt:message key="label.started"/>
                <% } else { %><fmt:message key="label.stopped"/> <% } %> -
                <%if (ErrorFileDumper.isShutdown()) {%><a
                        href="?do=status&amp;sub=display&amp;enableErrorFileDumper=true#threads"><fmt:message
                        key="label.start.error.dumper"/></a>
                <% } else { %><a
                        href="?do=status&amp;sub=display&amp;enableErrorFileDumper=false#threads"><fmt:message
                        key="label.stop.error.dumper"/></a><% } %>
            </td>
        </tr>
        <tr>
            <td align="left">
                <a href="http://java.net/projects/tda/downloads/download/webstart/tda.jnlp"
                   title="<fmt:message key='org.jahia.admin.status.ManageStatus.launchTda.disclaimer'/>"
                   target="_blank"><img src="<c:url value='/icons/tda.gif'/>" height="16" width="16" alt=" "
                                        align="top"/><fmt:message
                        key="org.jahia.admin.status.ManageStatus.launchTda"/></a>
            </td>
        </tr>
    </table>
</div>

<h3><fmt:message key="org.jahia.admin.status.ManageStatus.title.systemInfoSection.label"/></h3>

<div>
    <table width="100%" border="0" cellspacing="0" cellpadding="5" style="table-layout: fixed;">
        <% Enumeration propertyNameEnum = System.getProperties().propertyNames();
            int maxWidth = 40;
            int propertyCounter = 0;
            String propertyLineClass = "evenLine";
            while (propertyNameEnum.hasMoreElements()) {
                if (propertyCounter % 2 == 0) {
                    propertyLineClass = "evenLine";
                } else {
                    propertyLineClass = "oddLine";
                }
                propertyCounter++;
                String curPropertyName = (String) propertyNameEnum.nextElement();
                String curPropertyValue = (String) System.getProperty(curPropertyName);
                pageContext.setAttribute("propName", curPropertyName);
                pageContext.setAttribute("propValue", curPropertyValue);
        %>
        <tr class="<%=propertyLineClass%>">
            <td style="width: 40%; overflow: hidden;" title="<c:out value='${propName}'/>">
                <strong><c:out value='${propName}'/></strong>
            </td>
            <td style="width: 60%; overflow: hidden;" title="<c:out value='${propValue}'/>">
                <%
                    while (curPropertyValue.length() > maxWidth) {
                        String curLine = curPropertyValue.substring(0, maxWidth);
                        out.println(curLine);
                        curPropertyValue = curPropertyValue.substring(maxWidth);
                    }
                    out.println(curPropertyValue);
                %>
            </td>
        </tr>
        <% } %>
    </table>
</div>
</div>