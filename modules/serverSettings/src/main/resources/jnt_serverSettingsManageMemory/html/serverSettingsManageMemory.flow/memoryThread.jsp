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
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="memoryInfo" type="org.jahia.modules.serversettings.memoryThread.MemoryThreadInformationManagement"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<script type="text/javascript">
    $(document).ready(function () {
        $("#accordion").accordion({collapsible:true, heightStyle:"content"${param.tab == 'threads' ? ',active:1' : ''}});
    })
</script>

<div id="accordion">
    <h3><fmt:message key="label.memory"/>&nbsp;(${memoryInfo.memoryUsage}%&nbsp;<fmt:message
            key="serverSettings.manageMemory.used"/>)</h3>

    <div>
        <table width="100%" border="0" cellspacing="0" cellpadding="5">
            <tr>
                <td>
                    <strong><fmt:message key="label.memory.used"/>&nbsp;:</strong><br>
                </td>
                <td>
                    ${memoryInfo.usedMemory}
                </td>
            </tr>
            <tr>
                <td>
                    <strong><fmt:message key="label.memory.free"/>&nbsp;:</strong><br>
                </td>
                <td>
                    ${memoryInfo.freeMemory}
                </td>
            </tr>
            <tr>
                <td>
                    <strong><fmt:message key="label.memory.total"/>&nbsp;:</strong><br>
                </td>
                <td>
                    ${memoryInfo.totalMemory}
                </td>
            </tr>
            <tr>
                <td>
                    <strong><fmt:message key="label.memory.max"/>:</strong><br>
                </td>
                <td>
                    ${memoryInfo.maxMemory}
                </td>
            </tr>
            <tr>
                <td>
                    <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                        <input type="submit" name="_eventId_refresh"
                               value="<fmt:message key="label.refresh"/>"/>
                    </form>
                </td>
                <td>
                    <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                        <input type="submit" name="_eventId_gc"
                               value="<fmt:message key="label.memory.gc"/>"/>
                    </form>
                </td>
            </tr>
        </table>
    </div>

    <h3><fmt:message key="label.threads"/></h3>

    <div>
        <table width="100%" border="0" cellspacing="0" cellpadding="5">
            <tr>
                <td align="left">
                    <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                        <input type="submit" name="_eventId_showTD"
                               value="<fmt:message key="label.threads.performThreadDump.page"/>"/>
                    </form>
                </td>
            </tr>
            <tr>
                <td align="left">
                    <a href="<c:url value="/tools/threadDump.jsp?file=true"/>" target="_blank"><fmt:message key="label.threads.performThreadDump.file.download"/></a>
                </td>
            </tr>
            <tr>
                <td align="left">
                    <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                        <input type="hidden" name="threadDump" value="sysout"/>
                        <input type="submit" name="_eventId_performTD"
                               value="<fmt:message key="label.threads.performThreadDump.system.out"/>"/>
                    </form>
                </td>
            </tr>
            <tr>
                <td align="left">
                    <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                        <input type="hidden" name="threadDump" value="file"/>
                        <input type="submit" name="_eventId_performTD"
                               value="<fmt:message key="label.threads.performThreadDump.file"/>"/>
                    </form>
                </td>
            </tr>
            <tr>
                <td align="left">
                    <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                        <input type="hidden" name="threadDump" value="file"/>
                        <input type="submit" name="_eventId_scheduleTD"
                               value="<fmt:message key="label.threads.performThreadDump.multiple"/>"/>
                        &nbsp;&nbsp;
                        <label for="threadDumpCount"><fmt:message key="column.count.label"/>:&nbsp;</label>
                        <input type="text" id="threadDumpCount" name="threadDumpCount" size="2" value="10"/>
                        &nbsp;&nbsp;
                        <label for="threadDumpInterval"><fmt:message key="label.interval"/>:&nbsp;</label>
                        <input type="text" id="threadDumpInterval" name="threadDumpInterval" size="2" value="10"/>&nbsp;<fmt:message
                            key="label.seconds"/>
                    </form>
                </td>
            </tr>
            <tr>
                <td align="left">
                    <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                        <input type="submit" name="_eventId_toggleTD"
                               value="<c:choose><c:when test="${memoryInfo.threadMonitorActivated}"><fmt:message key="label.threads.monitor.stop"/></c:when><c:otherwise><fmt:message key="label.threads.monitor.start"/></c:otherwise></c:choose>"/>
                    </form>
                </td>
            </tr>
            <tr>
                <td align="left">
                    <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                        <input type="submit" name="_eventId_toggleEFD"
                               value="<c:choose><c:when test="${memoryInfo.errorFileDumperActivated}"><fmt:message key="label.errors.dumper.stop"/></c:when><c:otherwise><fmt:message key="label.errors.dumper.start"/></c:otherwise></c:choose>"/>
                    </form>
                </td>
            </tr>
            <tr>
                <td align="left">
                    <a href="http://java.net/projects/tda/downloads/download/webstart/tda.jnlp"
                       target="_blank"><img src="<c:url value='/icons/tda.gif'/>" height="16" width="16" alt=" "
                                            align="top"/><fmt:message
                            key="label.launchTda"/></a>
                </td>
            </tr>
        </table>
    </div>

</div>
