<%@ page import="org.hibernate.stat.EntityStatistics" %>
<%@ page import="org.hibernate.stat.SecondLevelCacheStatistics" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="ehcacheStats" type="net.sf.ehcache.Statistics"--%>
<%--@elvariable id="ehcaches" type="java.util.List"--%>
<%--@elvariable id="secondLevelCacheStats" type="java.util.List"--%>
<%--@elvariable id="entityStats" type="java.util.List"--%>
<%--@elvariable id="hibernateStats" type="org.hibernate.stat.Statistics"--%>
<%--@elvariable id="secLevelStat" type="org.hibernate.stat.SecondLevelCacheStatistics"--%>
<%--@elvariable id="entityStat" type="org.hibernate.stat.EntityStatistics"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<script type="text/javascript">
    $(document).ready(function () {
        $("#accordion").accordion({collapsible:true, heightStyle:"content"});
        $("#subAccordion").accordion({collapsible:true, heightStyle:"content"});
    })
</script>

<div id="accordion">

<h3><fmt:message key="label.cache"/></h3>

<div>
    <p>

    <form action="${flowExecutionUrl}" method="POST">
        <input type="submit" name="_eventId_flushAllCaches"
               value="<fmt:message key='org.jahia.admin.status.ManageStatus.flushAllCaches.label'/>"/>
    </form>
    <form action="${flowExecutionUrl}" method="POST">
        <input type="submit" name="_eventId_flushOutputCaches"
               value="<fmt:message key='org.jahia.admin.status.ManageStatus.flushOutputCaches.label'/>"/>
    </form>
    <form action="${flowExecutionUrl}" method="POST">
        <input type="submit" name="_eventId_flushHibernateCaches"
               value="<fmt:message key='org.jahia.admin.status.ManageStatus.flushHibernateCaches.label'/>"/>
    </form>
    </p>
    <table width="100%" class="evenOddTable full tBorder" border="0" cellspacing="0" cellpadding="5">
        <thead>
        <tr>
            <th colspan="2"><fmt:message key='org.jahia.admin.status.ManageStatus.statistics.ehcache'/> (
                <form action="${flowExecutionUrl}" method="POST">
                    <input type="submit" name="_eventId_enableAllEHCacheStats"
                           value="<fmt:message key='org.jahia.admin.status.ManageStatus.statistics.all.enable'/>"/>
                </form>
                /
                <form action="${flowExecutionUrl}" method="POST">
                    <input type="submit" name="_eventId_disableAllEHCacheStats"
                           value="<fmt:message key='org.jahia.admin.status.ManageStatus.statistics.all.disable'/>"/>
                </form>
                )
            </th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${ehcaches}" var="ehcache" varStatus="status">
            <c:set var="ehcacheStats" value="${ehcache.statistics}"/>
            <c:set var="ehcacheName" value="${ehcache.name}"/>
            <tr class="${status.index % 2 == 0 ? 'evenLine' : 'oddLine'}">
                <td width="100%">
                    <fmt:message key="org.jahia.admin.status.ManageStatus.cache.${ehcacheName}.description.label"
                                 var="msg"/>
                    <strong>${ehcacheName}</strong>: ${fn:escapeXml(fn:contains(msg, '???') ? '' : msg)}<strong></strong>
                    <br/>
                        ${ehcacheStats.objectCount}&nbsp;<fmt:message
                        key="org.jahia.admin.${ehcacheStats.objectCount != 1 ? 'entries' : 'entrie'}.label"/>
                    <br/>
                    <c:if test="${ehcache.statisticsEnabled}">
                        <fmt:message key="org.jahia.admin.status.ManageStatus.successfulHits.label"/>:
                        ${ehcacheStats.cacheHits} / ${ehcacheStats.cacheHits + ehcacheStats.cacheMisses}
                        &nbsp;
                        <fmt:message key="org.jahia.admin.status.ManageStatus.totalHits.label"/>,
                        <fmt:message key="org.jahia.admin.status.ManageStatus.efficiency.label"/>:

                        <c:set var="cacheEfficiency"
                               value="${ehcacheStats.cacheHits + ehcacheStats.cacheMisses > 0 ? ehcacheStats.cacheHits * 100 / (ehcacheStats.cacheHits + ehcacheStats.cacheMisses) : 0}"/>
                        <c:set var="effColour" value="#222222"/>
                        <c:choose>
                            <c:when test="${cacheEfficiency > 0 && cacheEfficiency < 30}">
                                <c:set var="effColour" value="red"/>
                            </c:when>
                            <c:when test="${cacheEfficiency >= 30 && cacheEfficiency < 70}">
                                <c:set var="effColour" value="blue"/>
                            </c:when>
                            <c:when test="${cacheEfficiency >= 70}">
                                <c:set var="effColour" value="green"/>
                            </c:when>
                        </c:choose>
                        <span style="color: ${effColour}"><fmt:formatNumber value="${cacheEfficiency}" pattern="0.00"/>&nbsp;%</span>
                        <br/>
                        <fmt:message key='org.jahia.admin.status.ManageStatus.statistics'/>: <fmt:message
                            key='org.jahia.admin.status.ManageStatus.enabled'/> (<fmt:message
                            key='org.jahia.admin.status.ManageStatus.accuracy'/>: ${ehcacheStats.statisticsAccuracyDescription})
                        <form action="${flowExecutionUrl}" method="POST">
                            <input type="hidden" name="ehcache" value="${ehcacheName}"/>
                            <input type="submit" name="_eventId_disableEHCacheStats"
                                   value="<fmt:message key='org.jahia.admin.status.ManageStatus.statistics.disable'/>"/>
                        </form>
                    </c:if>
                    <c:if test="${not ehcache.statisticsEnabled}">
                        <fmt:message key='org.jahia.admin.status.ManageStatus.statistics'/>: <fmt:message
                            key='org.jahia.admin.status.ManageStatus.disabled'/> (<fmt:message
                            key='org.jahia.admin.status.ManageStatus.accuracy'/>: ${ehcacheStats.statisticsAccuracyDescription})
                        <form action="${flowExecutionUrl}" method="POST">
                            <input type="hidden" name="ehcache" value="${ehcacheName}"/>
                            <input type="submit" name="_eventId_enableEHCacheStats"
                                   value="<fmt:message key='org.jahia.admin.status.ManageStatus.statistics.enable'/>"/>
                        </form>
                    </c:if>
                </td>
                <td>
                    <form action="${flowExecutionUrl}" method="POST">
                        <input type="hidden" name="ehcache" value="${ehcacheName}"/>
                        <input type="submit" name="_eventId_flushCache"
                               value="<fmt:message key='org.jahia.admin.status.ManageStatus.flush.label'/>"/>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<h3><fmt:message key="org.jahia.admin.status.ManageStatus.hibernate.Statistics"/></h3>

<div>
    <c:choose>
    <c:when test="${not hibernateStats.statisticsEnabled}">
        <p>

        <form action="${flowExecutionUrl}" method="POST">
            <input type="submit" name="_eventId_toggleHibernateStats"
                   value="<fmt:message key='org.jahia.admin.status.ManageStatus.statistics.enable'/>"/>
        </form>
        </p>
    </c:when>

    <c:otherwise>
        <div id="subAccordion">
            <h3><fmt:message key="org.jahia.admin.status.ManageStatus.hibernate.session"/></h3>

            <div>
                <p>

                <form action="${flowExecutionUrl}" method="POST">
                    <input type="submit" name="_eventId_toggleHibernateStats"
                           value="<fmt:message key='org.jahia.admin.status.ManageStatus.statistics.disable'/>"/>
                </form>
                </p>
                <table width="100%" border="0" cellspacing="0" cellpadding="5">
                    <thead>
                    <tr>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.session.opened'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.session.closed'/></th>
                    </tr>
                    </thead>

                    <tr>
                        <td>${hibernateStats.sessionOpenCount}</td>
                        <td>${hibernateStats.sessionCloseCount}</td>
                        </td>
                    </tr>
                </table>
            </div>
            <h3><fmt:message key="org.jahia.admin.status.ManageStatus.hibernate.query"/></h3>

            <div>
                <table width="100%" border="0" cellspacing="0" cellpadding="5">
                    <thead>
                    <tr>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.hits'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.miss'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.puts'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.query.executions'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.query.time.max'/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td>${hibernateStats.queryCacheHitCount}</td>
                        <td>${hibernateStats.queryCacheMissCount}</td>
                        <td>${hibernateStats.queryCachePutCount}</td>
                        <td>${hibernateStats.queryExecutionCount}</td>
                        <td>${hibernateStats.queryExecutionMaxTime}</td>
                    </tr>
                </table>
            </div>


            <h3><fmt:message key="org.jahia.admin.status.ManageStatus.hibernate.secondlevel.statistics"/></h3>

            <div>
                <table width="100%" class="evenOddTable full tBorder" border="0" cellspacing="0" cellpadding="5">
                    <thead>
                    <tr>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.hits'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.miss'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.puts'/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td>${hibernateStats.secondLevelCacheHitCount}</td>
                        <td>${hibernateStats.secondLevelCacheMissCount}</td>
                        <td>${hibernateStats.secondLevelCachePutCount}</td>
                    </tr>
                </table>
            </div>

            <h3><fmt:message key="org.jahia.admin.status.ManageStatus.hibernate.secondlevel.details"/></h3>

            <div>
                <table width="100%" class="evenOddTable full tBorder" border="0" cellspacing="0" cellpadding="5">
                    <thead>
                    <tr>
                        <th><fmt:message key='label.name'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.hits'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.miss'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.puts'/></th>
                        <th><fmt:message key='label.size'/></th>
                    </tr>
                    </thead>
                    <c:forEach items="${secondLevelCacheStats}" var="secLevelStat">
                    <tr>
                        <td style="font-size: 10px;">${secLevelStat.categoryName}</td>
                        <td>${secLevelStat.hitCount}</td>
                        <td>${secLevelStat.missCount}</td>
                        <td>${secLevelStat.putCount}</td>
                        <td>${secLevelStat.sizeInMemory}</td>
                    </tr>
                    </c:forEach>
                </table>
            </div>

            <h3><fmt:message key="org.jahia.admin.status.ManageStatus.hibernate.entity.statistics"/></h3>

            <div>
                <table width="100%" class="evenOddTable full tBorder" border="0" cellspacing="0" cellpadding="5">
                    <thead>
                    <tr>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.entity.load'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.entity.fetch'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.entity.insert'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.entity.update'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.entity.delete'/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td>${hibernateStats.entityLoadCount}</td>
                        <td>${hibernateStats.entityFetchCount}</td>
                        <td>${hibernateStats.entityInsertCount}</td>
                        <td>${hibernateStats.entityUpdateCount}</td>
                        <td>${hibernateStats.entityDeleteCount}</td>
                    </tr>
                </table>
            </div>

            <h3><fmt:message key="org.jahia.admin.status.ManageStatus.hibernate.entity.details"/></h3>

            <div>
                <table width="100%" class="evenOddTable full tBorder" border="0" cellspacing="0" cellpadding="5">
                    <thead>
                    <tr>
                        <th><fmt:message key='label.name'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.entity.load'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.entity.fetch'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.entity.insert'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.entity.update'/></th>
                        <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.entity.delete'/></th>
                    </tr>
                    </thead>
                    <c:forEach items="${entityStats}" var="entityStat">
                    <tr>
                        <td style="font-size: 10px;">${entityStat.categoryName}</td>
                        <td>${entityStat.loadCount}</td>
                        <td>${entityStat.fetchCount}</td>
                        <td>${entityStat.insertCount}</td>
                        <td>${entityStat.updateCount}</td>
                        <td>${entityStat.deleteCount}</td>
                    </tr>
                    </c:forEach>
                </table>
            </div>
        </div>
    </c:otherwise>
    </c:choose>
</div>
</div>