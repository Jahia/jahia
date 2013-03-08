<%@ page import="net.sf.ehcache.CacheManager" %>
<%@ page import="org.hibernate.SessionFactory" %>
<%@ page import="org.hibernate.stat.EntityStatistics" %>
<%@ page import="org.hibernate.stat.SecondLevelCacheStatistics" %>
<%@ page import="org.hibernate.stat.Statistics" %>
<%@ page import="org.jahia.services.SpringContextSingleton" %>
<%@ page import="org.jahia.services.cache.ehcache.EhCacheProvider" %>
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
        $("#subAccordion").accordion({collapsible:true, heightStyle:"content"});
    })
</script>

<%
    pageContext.setAttribute("timestamp", System.currentTimeMillis());
%>
<div id="accordion">

<h3><fmt:message key="label.cache"/></h3>

<div>
    <table width="100%" class="evenOddTable full tBorder" border="0" cellspacing="0" cellpadding="5">
        <thead>
        <tr>
            <th colspan="2"><fmt:message key='org.jahia.admin.status.ManageStatus.statistics.ehcache'/> (<a
                    href="?enableEhcacheStats=true&amp;timestamp=${timestamp}"><fmt:message
                    key='org.jahia.admin.status.ManageStatus.statistics.all.enable'/></a> / <a
                    href="?enableEhcacheStats=false&amp;timestamp=${timestamp}"><fmt:message
                    key='org.jahia.admin.status.ManageStatus.statistics.all.disable'/></a>)
            </th>
        </tr>
        </thead>
        <tbody>
        <%
            CacheManager ehcacheManager = ((EhCacheProvider) SpringContextSingleton.getBean(
                    "ehCacheProvider")).getCacheManager();
            pageContext.setAttribute("ehcacheManager", ehcacheManager);
            String[] ehcacheNames = ehcacheManager.getCacheNames();
            java.util.Arrays.sort(ehcacheNames);
            pageContext.setAttribute("ehcacheNames", ehcacheNames);
            if (request.getParameter("enableEhcacheStats") != null) {
                boolean doEnable = Boolean.valueOf(request.getParameter("enableEhcacheStats"));
                if (request.getParameter("ehcache") != null) {
                    ehcacheManager.getCache(request.getParameter("ehcache")).setStatisticsEnabled(doEnable);
                } else {
                    for (String cacheName : ehcacheManager.getCacheNames()) {
                        ehcacheManager.getCache(cacheName).setStatisticsEnabled(doEnable);
                    }
                }
            }
        %>
        <c:forEach items="${ehcacheNames}" var="ehcacheName" varStatus="status">
            <%
                net.sf.ehcache.Cache theCache = ehcacheManager.getCache((String) pageContext.getAttribute(
                        "ehcacheName"));
                pageContext.setAttribute("ehcache", theCache);
                pageContext.setAttribute("ehcacheStats", theCache.getStatistics());
            %>
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
                        <a href="?enableEhcacheStats=false&amp;ehcache=${ehcacheName}&amp;timestamp=${timestamp}"><fmt:message
                                key='org.jahia.admin.status.ManageStatus.statistics.disable'/></a>
                    </c:if>
                    <c:if test="${not ehcache.statisticsEnabled}">
                        <fmt:message key='org.jahia.admin.status.ManageStatus.statistics'/>: <fmt:message
                            key='org.jahia.admin.status.ManageStatus.disabled'/> (<fmt:message
                            key='org.jahia.admin.status.ManageStatus.accuracy'/>: ${ehcacheStats.statisticsAccuracyDescription})
                        <a href="?enableEhcacheStats=true&amp;ehcache=${ehcacheName}&amp;timestamp=${timestamp}"><fmt:message
                                key='org.jahia.admin.status.ManageStatus.statistics.enable'/></a>
                    </c:if>
                </td>
                    <%--<td>
                        <input type="submit" name="flush_ehcache_${ehcacheName}" value="<fmt:message key='org.jahia.admin.status.ManageStatus.flush.label'/>">
                    </td>--%>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<h3><fmt:message key="org.jahia.admin.status.ManageStatus.hibernate.Statistics"/></h3>

<div>
    <%
        SessionFactory factory = (SessionFactory) SpringContextSingleton.getBean("sessionFactory");
        Statistics statistics = factory.getStatistics();
        String enableStats = request.getParameter("enableStats");
        if (enableStats != null) {
            statistics.setStatisticsEnabled(Boolean.valueOf(enableStats));
        }
        if (!statistics.isStatisticsEnabled()) {
    %>
    <a href="?enableStats=true&amp;timestamp=${timestamp}"><fmt:message
            key='org.jahia.admin.status.ManageStatus.statistics.enable'/></a>
    <%
    } else {
    %>
    <div id="subAccordion">
        <h3><fmt:message key="org.jahia.admin.status.ManageStatus.hibernate.session"/></h3>

        <div>
            <p><a href="?enableStats=false&amp;timestamp=${timestamp}"><fmt:message
                    key='org.jahia.admin.status.ManageStatus.statistics.disable'/></a></p>
            <table width="100%" border="0" cellspacing="0" cellpadding="5">
                <thead>
                <tr>
                    <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.session.opened'/></th>
                    <th><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.session.closed'/></th>
                </tr>
                </thead>

                <tr>
                    <td><%=statistics.getSessionOpenCount()%>
                    </td>
                    <td><%=statistics.getSessionCloseCount()%>
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
                    <td><%=statistics.getQueryCacheHitCount()%>
                    </td>
                    <td><%=statistics.getQueryCacheMissCount()%>
                    </td>
                    <td><%=statistics.getQueryCachePutCount()%>
                    </td>
                    <td><%=statistics.getQueryExecutionCount()%>
                    </td>
                    <td><%=statistics.getQueryExecutionMaxTime()%>
                    </td>

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
                    <td><%=statistics.getSecondLevelCacheHitCount()%>
                    </td>
                    <td><%=statistics.getSecondLevelCacheMissCount()%>
                    </td>
                    <td><%=statistics.getSecondLevelCachePutCount()%>
                    </td>
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
                <% List names = Arrays.asList(statistics.getSecondLevelCacheRegionNames());
                    Collections.sort(names);
                    for (int i = 0; i < names.size(); i++) {
                        String name = (String) names.get(i);
                        SecondLevelCacheStatistics cacheStatistics = statistics.getSecondLevelCacheStatistics(name);
                %>
                <tr>
                    <td style="font-size: 10px;"><%=name%>
                    </td>
                    <td><%=cacheStatistics.getHitCount()%>
                    </td>
                    <td><%=cacheStatistics.getMissCount()%>
                    </td>
                    <td><%=cacheStatistics.getPutCount()%>
                    </td>
                    <td><%=cacheStatistics.getSizeInMemory()%>
                    </td>
                </tr>
                <%
                    }
                %>
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
                    <td><%=statistics.getEntityLoadCount()%>
                    </td>
                    <td><%=statistics.getEntityFetchCount()%>
                    </td>
                    <td><%=statistics.getEntityInsertCount()%>
                    </td>
                    <td><%=statistics.getEntityUpdateCount()%>
                    </td>
                    <td><%=statistics.getEntityDeleteCount()%>
                    </td>
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
                <% List entities = Arrays.asList(statistics.getEntityNames());
                    Collections.sort(entities);
                    for (int i = 0; i < entities.size(); i++) {
                        String name = (String) entities.get(i);
                        EntityStatistics entityStatistics = statistics.getEntityStatistics(name);
                %>
                <tr>
                    <td style="font-size: 10px;">
                        <%=name%>
                    </td>
                    <td><%=entityStatistics.getLoadCount()%>
                    </td>
                    <td><%=entityStatistics.getFetchCount()%>
                    </td>
                    <td><%=entityStatistics.getInsertCount()%>
                    </td>
                    <td><%=entityStatistics.getUpdateCount()%>
                    </td>
                    <td><%=entityStatistics.getDeleteCount()%>
                    </td>
                </tr>
                <%
                    }
                %>
            </table>
        </div>
    </div>
    <%}%>
</div>
</div>