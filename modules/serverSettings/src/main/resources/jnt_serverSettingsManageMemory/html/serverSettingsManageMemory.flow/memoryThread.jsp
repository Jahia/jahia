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
<template:addResources type="javascript" resources="jquery.js,admin-bootstrap.js"/>
<template:addResources type="css" resources="admin-bootstrap.css"/>

<div class="accordion" id="accordion2">
    <div class="accordion-group">
        <div class="accordion-heading">
            <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseOne">
                <strong><fmt:message key="serverSettings.manageMemory.memory"/>&nbsp;(${memoryInfo.memoryUsage}%&nbsp;<fmt:message
                    key="serverSettings.manageMemory.used"/>)</strong>
            </a>
        </div>
        <div id="collapseOne" class="accordion-body collapse${memoryInfo.mode == 'memory' ? ' in' : ''}">
            <div class="accordion-inner">
                <table class="table table-striped table-bordered table-hover">
                    <tr>
                        <td>
                            <strong><fmt:message key="serverSettings.manageMemory.memory.used"/></strong><br>
                        </td>
                        <td>
                            ${memoryInfo.usedMemory}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <strong><fmt:message key="serverSettings.manageMemory.memory.free"/></strong><br>
                        </td>
                        <td>
                            ${memoryInfo.freeMemory}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <strong><fmt:message key="serverSettings.manageMemory.memory.total"/></strong><br>
                        </td>
                        <td>
                            ${memoryInfo.totalMemory}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <strong><fmt:message key="serverSettings.manageMemory.memory.max"/></strong><br>
                        </td>
                        <td>
                            ${memoryInfo.maxMemory}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                                <button class="btn" type="submit" name="_eventId_refresh">
                                    <i class="icon-refresh"></i>
                                    &nbsp;<fmt:message key='label.refresh'/>
                                </button>
                            </form>
                        </td>
                        <td>
                            <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                                <button class="btn" type="submit" name="_eventId_gc">
                                    <i class="icon-cog"></i>
                                    &nbsp;<fmt:message key='serverSettings.manageMemory.memory.gc'/>
                                </button>
                            </form>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
    </div>
    <div class="accordion-group">
        <div class="accordion-heading">
                <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseTwo">
                <strong><fmt:message key="serverSettings.manageMemory.threads"/></strong></a>
        </div>
        <div id="collapseTwo" class="accordion-body collapse${memoryInfo.mode == 'threads' ? ' in' : ''}">
            <div class="accordion-inner">
                <table class="table table-striped table-bordered table-hover">
                    <tr>
                        <td align="left">
                            <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                                <button class="btn" type="submit" name="_eventId_showTD">
                                    <i class="icon-cog"></i>
                                    &nbsp;<fmt:message key='serverSettings.manageMemory.threads.performThreadDump.page'/>
                                </button>
                            </form>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">
                            <a class="btn" href="<c:url value='/tools/threadDump.jsp?file=true'/>" target="_blank">
                                <i class="icon-cog"></i>
                                <fmt:message key="serverSettings.manageMemory.threads.performThreadDump.file.download"/>
                            </a>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">
                            <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                                <input type="hidden" name="threadDump" value="sysout"/>
                                <button class="btn" type="submit" name="_eventId_performTD">
                                    <i class="icon-cog"></i>
                                    &nbsp;<fmt:message key='serverSettings.manageMemory.threads.performThreadDump.system.out'/>
                                </button>
                            </form>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">
                            <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                                <input type="hidden" name="threadDump" value="file"/>
                                <button class="btn" type="submit" name="_eventId_performTD">
                                    <i class="icon-cog"></i>
                                    &nbsp;<fmt:message key='serverSettings.manageMemory.threads.performThreadDump.file'/>
                                </button>
                            </form>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">
                            <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                                <input type="hidden" name="threadDump" value="file"/>
                                <button class="btn" type="submit" name="_eventId_scheduleTD">
                                    <i class="icon-cog"></i>
                                    &nbsp;<fmt:message key='serverSettings.manageMemory.threads.performThreadDump.multiple'/>
                                </button>
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

                                    <c:choose>
                                        <c:when test="${memoryInfo.threadMonitorActivated}">
                                        <button class="btn btn-danger" type="submit" name="_eventId_toggleTD">
                                            <i class="icon-stop icon-white"></i>
                                            &nbsp;
                                            <fmt:message key="serverSettings.manageMemory.threads.monitor.stop"/>
                                        </button>
                                        </c:when>
                                        <c:otherwise>
                                        <button class="btn btn-success" type="submit" name="_eventId_toggleTD">
                                            <i class="icon-play icon-white"></i>
                                            <fmt:message key="serverSettings.manageMemory.threads.monitor.start"/>
                                        </button>
                                        </c:otherwise>
                                    </c:choose>
                            </form>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">
                            <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
                                    <c:choose>
                                        <c:when test="${memoryInfo.errorFileDumperActivated}">
                                        <button class="btn btn-danger" type="submit" name="_eventId_toggleEFD">
                                            <i class="icon-stop icon-white"></i>
                                            &nbsp;
                                            <fmt:message key="serverSettings.manageMemory.errors.dumper.stop"/>
                                        </button>
                                        </c:when>
                                        <c:otherwise>
                                        <button class="btn btn-success" type="submit" name="_eventId_toggleEFD">
                                            <i class="icon-play icon-white"></i>
                                            &nbsp;
                                            <fmt:message key="serverSettings.manageMemory.errors.dumper.start"/>
                                        </button>
                                        </c:otherwise>
                                    </c:choose>

                            </form>
                        </td>
                    </tr>
                    <tr>
                        <td align="left">
                            <a class="btn" href="http://java.net/projects/tda/downloads/download/webstart/tda.jnlp" target="_blank">
                                <i class="icon-share"></i>
                                &nbsp;<fmt:message key="serverSettings.manageMemory.launchTda"/>
                            </a>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
    </div>

</div>
