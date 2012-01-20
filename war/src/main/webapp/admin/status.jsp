<%@include file="/admin/include/header.inc"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import = "org.jahia.bin.*"%>
<%@page import="org.jahia.tools.jvm.ThreadMonitor" %>
<%@page import = "java.util.*" %>
<%@page import = "java.text.*" %>
<%@page import = "org.jahia.registries.*" %>
<%@page import = "org.jahia.services.cache.Cache" %>
<%@page import = "org.jahia.services.cache.CacheFactory" %>
<%@ page import="org.jahia.settings.SettingsBean"%>
<%@ page import="org.jahia.params.ProcessingContext"%>
<%@ page import="org.jahia.services.SpringContextSingleton" %>
<%@ page import="org.hibernate.stat.Statistics" %>
<%@ page import="org.hibernate.stat.SecondLevelCacheStatistics" %>
<%@ page import="org.hibernate.stat.EntityStatistics" %>
<%@ page import="org.springframework.orm.hibernate3.support.HibernateDaoSupport" %>
<%@ page import="org.hibernate.SessionFactory" %>
<%@page import="org.jahia.services.cache.CacheHelper"%>
<%@page import="org.jahia.services.cache.ehcache.EhCacheProvider"%>
<%@page import="net.sf.ehcache.CacheManager"%>
<%@ page import="org.jahia.bin.errors.ErrorFileDumper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"%>

<%
    Long freeMemoryInBytes       =  (Long) request.getAttribute("freeMemoryInBytes");
    long freeMemoryInKBytes      =  freeMemoryInBytes.longValue() >> 10;
    long freeMemoryInMBytes      =  freeMemoryInBytes.longValue() >> 20;
    Long totalMemoryInBytes      =  (Long) request.getAttribute("totalMemoryInBytes");
    long totalMemoryInKBytes     =  totalMemoryInBytes.longValue() >> 10;
    long totalMemoryInMBytes     =  totalMemoryInBytes.longValue() >> 20;
    Long maxMemoryInBytes        =  (Long) request.getAttribute("maxMemoryInBytes");
    long maxMemoryInKBytes       =  maxMemoryInBytes.longValue() >> 10;
    long maxMemoryInMBytes       =  maxMemoryInBytes.longValue() >> 20;
    stretcherToOpen   = 0;
    pageContext.setAttribute("timestamp", System.currentTimeMillis());
%>


<style type="text/css">



    h2 a:link, h2 a:visited {
      	background-color: #336699;
      	color: #fff;
      	text-shadow: 2px 2px 1px #336699;
      	padding: 4px 8px 0;
      	-moz-outline-style: none;
      	display: block;
      	height: 16px;
      	text-decoration: none;
    }

    #container {
        width: 560px;
        text-align: left;
        margin: 0 auto;
    }

    #header {
        height: 82px;
    }

    #content {
        padding: 0 0 5px 0;
        border-style : dashed;
        border-width : 1px 0px 1px 0px;
    }

    #content div {
        margin: 0;
        padding: 0;
        height: 1%;
    }

    .stretcher {
        background: none !important;
        background: #fff;
    }

    .stretcher p {
        margin : 0px;
        padding : 5px;
    }
    
</style>

<script type="text/javascript" src="<%=URL%>../javascript/moo/prototype.lite.js"></script>
<script type="text/javascript" src="<%=URL%>../javascript/moo/moo.fx.js"></script>
<script type="text/javascript" src="<%=URL%>../javascript/moo/moo.fx.pack.js"></script>
<script type="text/javascript">
    //the main function, call to the effect object
    function init(){

        var stretchers = document.getElementsByClassName('stretcher'); //div that stretches
        var toggles = document.getElementsByClassName('stretcher-head'); //h3s where I click on

        //accordion effect
        var myAccordion = new fx.Accordion(
                toggles, stretchers, {opacity: true, duration: 400}
                );

        //hash functions
        var found = false;
        toggles.each(function(h2, i){
            var div = Element.find(h2, 'nextSibling'); //element.find is located in prototype.lite
            if (window.location.href.indexOf('#' + h2.title) > 0) {
                myAccordion.showThisHideOpen(div);
                found = true;
            }
        });
        if (!found) myAccordion.showThisHideOpen(stretchers[0]);
    }
</script>

<div id="topTitle">
<h1>Jahia</h1>
<h2 class="edit"><fmt:message key="org.jahia.admin.serverStatus.label"/></h2>
</div>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
	cellspacing="0">
	<tbody>
		<tr>
			<td style="vertical-align: top;" align="left">
				<%@include file="/admin/include/tab_menu.inc"%>
			</td>
		</tr>
		<tr>
			<td style="vertical-align: top;" align="left" height="100%">
			<div class="dex-TabPanelBottom">
			<div class="tabContent">
            <jsp:include page="/admin/include/left_menu.jsp">
                <jsp:param name="mode" value="server"/>
            </jsp:include>
			<div id="content" class="fit">
            <div class="head">
                <div class="object-title">
                     <fmt:message key="org.jahia.admin.serverStatus.label"/>
                </div>
                <div style="float:right;display:inline;padding:5px 10px 0 0">
                    <a href="<c:url value='/tools/systemInfo.jsp?file=true'/>" target="_blank"><img src="<c:url value='/icons/download.png'/>" height="16" width="16" alt=" " align="top"/><fmt:message key="org.jahia.admin.status.ManageStatus.downloadStatus"/></a>
                </div>
            </div>
<form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"status","&sub=process")%>' method="post">

<div class="stretcher-head head" title="cache">
    <div class="object-title">
     <a href="#caches" class="sectionLink"><fmt:message key="label.cache"/></a>
    </div>                
</div>
<div class="stretcher">
    <p>
        <fmt:message key="org.jahia.admin.status.ManageStatus.cacheFlushingIntroduction.label"/>
    </p>
    <table border="0" cellspacing="0" cellpadding="5">
        <tr class="evenLine">
            <td width="100%">
                <strong><fmt:message key="org.jahia.admin.status.ManageStatus.flushOutputCaches.label"/>:</strong><br>
            </td>
            <td>
                <input type="submit" name="flushOutputCaches" value="<fmt:message key='org.jahia.admin.status.ManageStatus.flushOutputCaches.label'/>">
            </td>
        </tr>
        <% if (CacheHelper.canFlushHibernateCaches()) { %>
        <tr class="evenLine">
            <td width="100%">
                <strong><fmt:message key="org.jahia.admin.status.ManageStatus.flushHibernateCaches.label"/>:</strong><br>
            </td>
            <td>
                <input type="submit" name="flushHibernateCaches" value="<fmt:message key='org.jahia.admin.status.ManageStatus.flushHibernateCaches.label'/>">
            </td>
        </tr>
        <% } %>
        <tr class="evenLine">
            <td width="100%">
                <strong><fmt:message key="org.jahia.admin.status.ManageStatus.flushAllCaches.label"/>:</strong><br>
            </td>
            <td>
                <input type="submit" name="flushAllCaches" value="<fmt:message key='org.jahia.admin.status.ManageStatus.flushAllCaches.label'/>">
            </td>
        </tr>
    </table>
    <br>
    <table class="evenOddTable full" border="0" cellspacing="0" cellpadding="5">
        <%
            SortedSet sortedCacheNames = new TreeSet(ServicesRegistry.getInstance().getCacheService().getNames());
            Iterator cacheNameIte = sortedCacheNames.iterator();
            DecimalFormat percentFormat = new DecimalFormat("###.##");
            int cacheCounter = 0;
            String cacheLineClass = "evenLine";
            while (cacheNameIte.hasNext()) {
                String curCacheName = (String) cacheNameIte.next();
                Object objectCache = ServicesRegistry.getInstance().getCacheService().getCache (curCacheName);
                if (objectCache instanceof Cache && !(((Cache) objectCache).getCacheImplementation() instanceof org.jahia.services.cache.ehcache.EhCacheImpl)) {
                    if (cacheCounter % 2 == 0) {
                        cacheLineClass = "evenLine";
                    } else {
                        cacheLineClass = "oddLine";
                    }
                    cacheCounter++;
                    Cache curCache = (Cache) objectCache;
                    String resourceKey = "org.jahia.admin.status.ManageStatus.cache." + curCache.getName() + ".description.label";
                    String efficiencyStr = "0";
                    if (!Double.isNaN(curCache.getCacheEfficiency())) {
                        efficiencyStr = percentFormat.format(curCache.getCacheEfficiency());
                        pageContext.setAttribute("cacheEfficiency", Double.valueOf(curCache.getCacheEfficiency()));
                    } else {
                        pageContext.setAttribute("cacheEfficiency", Double.valueOf(0));
                    }
                    pageContext.setAttribute("cache", curCache);
        %>
        <tr class="<%=cacheLineClass%>">
            <td width="100%">
                <strong><%=curCache.getName()%></strong>: <strong><fmt:message key="<%=resourceKey%>"/></strong>
                <br>
                <%=curCache.size()%>&nbsp;
                <% if(curCache.size() != 1){
                %><fmt:message key="org.jahia.admin.entries.label"/><%
            } else {
            %><fmt:message key="org.jahia.admin.entrie.label"/><%
                } %>
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
                &nbsp;
                <fmt:message key="org.jahia.admin.status.ManageStatus.groupSize.label"/>:
                ${cache.groupsSize}
                &nbsp;
                <fmt:message key="org.jahia.admin.status.ManageStatus.groupsKeysTotal.label"/>:
                ${cache.groupsKeysTotal}
                <br>
                <fmt:message key="org.jahia.admin.status.ManageStatus.successfulHits.label"/>:
                ${cache.successHits} / ${cache.totalHits}
                &nbsp;
                <fmt:message key="org.jahia.admin.status.ManageStatus.totalHits.label"/>,
                <fmt:message key="org.jahia.admin.status.ManageStatus.efficiency.label"/>:
                <span style="color: ${effColour}"><%=efficiencyStr%> %</span>
            </td>
            <td>
                <input type="submit" name="flush_<%=curCache.getName()%>" value="<fmt:message key='org.jahia.admin.status.ManageStatus.flush.label'/>">
            </td>
        </tr>
        <%
                }
            }
        %>
    </table>
    <table width="100%" class="evenOddTable full tBorder" border="0" cellspacing="0" cellpadding="5">
        <thead>
            <tr>
                <th colspan="2"><fmt:message key='org.jahia.admin.status.ManageStatus.statistics.ehcache'/> (<a href="?do=status&amp;sub=display&amp;enableEhcacheStats=true&amp;timestamp=${timestamp}"><fmt:message key='org.jahia.admin.status.ManageStatus.statistics.all.enable'/></a> / <a href="?do=status&amp;sub=display&amp;enableEhcacheStats=false&amp;timestamp=${timestamp}"><fmt:message key='org.jahia.admin.status.ManageStatus.statistics.all.disable'/></a>)</th>
            </tr>
        </thead>
        <tbody>
        <%
        	CacheManager ehcacheManager = ((EhCacheProvider) SpringContextSingleton.getBean("ehCacheProvider")).getCacheManager();
        	pageContext.setAttribute("ehcacheManager", ehcacheManager);
        	String[] ehcacheNames = ehcacheManager.getCacheNames();
        	java.util.Arrays.sort(ehcacheNames);
        	pageContext.setAttribute("ehcacheNames", ehcacheNames);
        	if (request.getParameter("enableEhcacheStats") != null) {
        		boolean doEnable = Boolean.valueOf(request.getParameter("enableEhcacheStats"));
        		if (request.getParameter("ehcache") != null) {
        			ehcacheManager.getCache(request.getParameter("ehcache")).setStatisticsEnabled(doEnable);
        		} else {
        			for(String cacheName : ehcacheManager.getCacheNames()) {
        				ehcacheManager.getCache(cacheName).setStatisticsEnabled(doEnable);
        			}
        		}
        	}
        %>
        <c:forEach items="${ehcacheNames}" var="ehcacheName" varStatus="status">
        	<%
        		net.sf.ehcache.Cache theCache = ehcacheManager.getCache((String) pageContext.getAttribute("ehcacheName"));
	        	pageContext.setAttribute("ehcache", theCache);
	        	pageContext.setAttribute("ehcacheStats", theCache.getStatistics());
        	%>
        	<tr class="${status.index % 2 == 0 ? 'evenLine' : 'oddLine'}">
        		<td width="100%">
                    <fmt:message key="org.jahia.admin.status.ManageStatus.cache.${ehcacheName}.description.label" var="msg"/>
        			<strong>${ehcacheName}</strong>: ${fn:escapeXml(fn:contains(msg, '???') ? '' : msg)}<strong></strong>
        			<br/>
        			${ehcacheStats.objectCount}&nbsp;<fmt:message key="org.jahia.admin.${ehcacheStats.objectCount != 1 ? 'entries' : 'entrie'}.label"/>
        			<br/>
        			<c:if test="${ehcache.statisticsEnabled}">
		                <fmt:message key="org.jahia.admin.status.ManageStatus.successfulHits.label"/>:
		                ${ehcacheStats.cacheHits} / ${ehcacheStats.cacheHits + ehcacheStats.cacheMisses}
		                &nbsp;
		                <fmt:message key="org.jahia.admin.status.ManageStatus.totalHits.label"/>,
		                <fmt:message key="org.jahia.admin.status.ManageStatus.efficiency.label"/>:
		                
		                <c:set var="cacheEfficiency" value="${ehcacheStats.cacheHits + ehcacheStats.cacheMisses > 0 ? ehcacheStats.cacheHits * 100 / (ehcacheStats.cacheHits + ehcacheStats.cacheMisses) : 0}"/>
		                <c:set var="effColour" value="#222222"/>
		                <c:choose>
		                    <c:when test="${cacheEfficiency > 0 && cacheEfficiency < 30}">
		                        <c:set var="effColour" value="red"/>
		                    </c:when>
		                    <c:when test="$c >= 30 && cacheEfficiency < 70}">
		                        <c:set var="effColour" value="blue"/>
		                    </c:when>
		                    <c:when test="${cacheEfficiency >= 70}">
		                        <c:set var="effColour" value="green"/>
		                    </c:when>
		                </c:choose>
		                <span style="color: ${effColour}"><fmt:formatNumber value="${cacheEfficiency}" pattern="0.00"/>&nbsp;%</span>
		                <br/>
		                <fmt:message key='org.jahia.admin.status.ManageStatus.statistics'/>: <fmt:message key='org.jahia.admin.status.ManageStatus.enabled'/> (<fmt:message key='org.jahia.admin.status.ManageStatus.accuracy'/>: ${ehcacheStats.statisticsAccuracyDescription})
	                	<a href="?do=status&amp;sub=display&amp;enableEhcacheStats=false&amp;ehcache=${ehcacheName}&amp;timestamp=${timestamp}"><fmt:message key='org.jahia.admin.status.ManageStatus.statistics.disable'/></a>
	                </c:if>
        			<c:if test="${not ehcache.statisticsEnabled}">
		                <fmt:message key='org.jahia.admin.status.ManageStatus.statistics'/>: <fmt:message key='org.jahia.admin.status.ManageStatus.disabled'/> (<fmt:message key='org.jahia.admin.status.ManageStatus.accuracy'/>: ${ehcacheStats.statisticsAccuracyDescription})
	                	<a href="?do=status&amp;sub=display&amp;enableEhcacheStats=true&amp;ehcache=${ehcacheName}&amp;timestamp=${timestamp}"><fmt:message key='org.jahia.admin.status.ManageStatus.statistics.enable'/></a>
	                </c:if>
        		</td>
	            <td>
	                <input type="submit" name="flush_ehcache_${ehcacheName}" value="<fmt:message key='org.jahia.admin.status.ManageStatus.flush.label'/>">
	            </td>
        	</tr>
        </c:forEach>
        </tbody>
    </table>
    <table width="100%" class="evenOddTable full tBorder" border="0" cellspacing="0" cellpadding="5">
        <thead>
            <tr>
                <th colspan="6"><fmt:message key='org.jahia.admin.status.ManageStatus.hibernate.Statistics'/></th>
            </tr>
        </thead>
        <%
            SessionFactory factory = (SessionFactory) SpringContextSingleton.getBean("sessionFactory");
            Statistics statistics = factory.getStatistics();
            String enableStats = request.getParameter("enableStats");
            if (enableStats != null) {
                statistics.setStatisticsEnabled(Boolean.valueOf(enableStats));
            }
            if (statistics.isStatisticsEnabled()) {
        %>
        <tr class="oddLine">
            <td >Number of opened session: <%=statistics.getSessionOpenCount()%>
            </td>
            <td colspan="5">Number of closed session: <%=statistics.getSessionCloseCount()%>
            </td>
        </tr>
        <tr class="evenLine">
            <td>Number of query cache hits: <%=statistics.getQueryCacheHitCount()%>
            </td>
            <td>Number of query cache miss: <%=statistics.getQueryCacheMissCount()%>
            </td>
            <td>Number of query cache put: <%=statistics.getQueryCachePutCount()%>
            </td>
            <td>Number of query execution: <%=statistics.getQueryExecutionCount()%>
            </td>
            <td colspan="2">Max time for query execution in ms: <%=statistics.getQueryExecutionMaxTime()%>
            </td>

        </tr>
        <tr class="oddLine">
            <td colspan="2">Number of second level cache hits: <%=statistics.getSecondLevelCacheHitCount()%>
            </td>
            <td colspan="2">Number of second level cache miss: <%=statistics.getSecondLevelCacheMissCount()%>
            </td>
            <td colspan="2" align="left">Number of second level cache put: <%=statistics.getSecondLevelCachePutCount()%>
            </td>
        </tr>
        <tr class="evenLine">
            <td>Names of second level caches:</td>
            <td>Cache hits</td>
            <td>Cache miss</td>
            <td>Cache put</td>
            <td>Size In Memory</td>
        </tr>
        <% List names = Arrays.asList(statistics.getSecondLevelCacheRegionNames());
            Collections.sort(names);
            for (int i = 0; i < names.size(); i++) {
                String name = (String) names.get(i);
                SecondLevelCacheStatistics cacheStatistics = statistics.getSecondLevelCacheStatistics(name);
        %>
        <tr class="evenLine">
            <td><%=name%>
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
        <tr class="oddLine">
            <td>Number of entity load count: <%=statistics.getEntityLoadCount()%>
            </td>
            <td>Number of entity fetch count: <%=statistics.getEntityFetchCount()%>
            </td>
            <td>Number of entity insert count: <%=statistics.getEntityInsertCount()%>
            </td>
            <td>Number of entity update count: <%=statistics.getEntityUpdateCount()%>
            </td>
            <td>Number of entity delete count: <%=statistics.getEntityDeleteCount()%>
            </td>
        </tr>
        <tr class="evenLine">
            <td>Names of entity</td>
            <td>Loaded</td>
            <td>Fetched</td>
            <td>Inserted</td>
            <td>Updated</td>
            <td>Deleted</td>
        </tr>
        <% List entities = Arrays.asList(statistics.getEntityNames());
            Collections.sort(entities);
            for (int i = 0; i < entities.size(); i++) {
                String name = (String) entities.get(i);
                EntityStatistics entityStatistics = statistics.getEntityStatistics(name);
        %>
        <tr class="evenLine">
            <td>
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
            <tr class="oddLine">
                <td colspan="6"><a href="?do=status&amp;sub=display&amp;enableStats=false&amp;timestamp=${timestamp}"><fmt:message key='org.jahia.admin.status.ManageStatus.statistics.disable'/></a></td>
            </tr>
        <%
            } else {
        %>
            <tr class="oddLine">
                <td colspan="6"><a href="?do=status&amp;sub=display&amp;enableStats=true&amp;timestamp=${timestamp}"><fmt:message key='org.jahia.admin.status.ManageStatus.statistics.enable'/></a></td>
            </tr>
        <%  } %>
    </table>
</div>

<div class="stretcher-head head" title="memory">
    <div class="object-title">
      <a href="#memory" class="sectionLink"><fmt:message key="org.jahia.admin.status.ManageStatus.title.memorySection.label"/></a>
    </div>                
</div>
<c:if test="${param['gc']}">
<% System.gc(); %>
</c:if>
<div class="stretcher">
    <table class="evenOddTable full" width="100%" border="0" cellspacing="0" cellpadding="5">
        <tr class="evenLine">
            <td width="100%">
                <strong><fmt:message key="org.jahia.admin.status.ManageStatus.maxJvmMemory.label"/>&nbsp;:</strong><br>
            </td>
            <td>
                <%=maxMemoryInMBytes%>&nbsp;<fmt:message key="org.jahia.admin.status.ManageStatus.mB.label"/>&nbsp;(<%=maxMemoryInKBytes%>&nbsp;<fmt:message key="org.jahia.admin.status.ManageStatus.kB.label"/>)
            </td>
        </tr>
        <tr class="oddLine">
            <td width="100%">
                <strong><fmt:message key="org.jahia.admin.status.ManageStatus.totalJvmMemory.label"/>&nbsp;:</strong><br>
            </td>
            <td>
                <%=totalMemoryInMBytes%>&nbsp;<fmt:message key="org.jahia.admin.status.ManageStatus.mB.label"/>&nbsp;(<%=totalMemoryInKBytes%>&nbsp;<fmt:message key="org.jahia.admin.status.ManageStatus.kB.label"/>)
            </td>
        </tr>
        <tr class="evenLine">
            <td width="100%">
                <strong><fmt:message key="org.jahia.admin.status.ManageStatus.freeMemory.label"/>&nbsp;:</strong><br>
            </td>
            <td>
                <%=freeMemoryInMBytes%>&nbsp;<fmt:message key="org.jahia.admin.status.ManageStatus.mB.label"/>&nbsp;(<%=freeMemoryInKBytes%>&nbsp;<fmt:message key="org.jahia.admin.status.ManageStatus.kB.label"/>)
            </td>
        </tr>
        <tr class="oddLine">
            <td colspan="2" align="left">
                <a href="?do=status&amp;sub=display&amp;gc=true&amp;timestamp=${timestamp}#memory"><img src="<c:url value='/icons/showTrashboard.png'/>" height="16" width="16" alt=" " align="top"/><fmt:message key="org.jahia.admin.status.ManageStatus.runGarbageCollector"/></a>
            </td>
        </tr>
    </table>
</div>

<div class="stretcher-head head" title="threads">
    <div class="object-title">
      <a href="#threads" class="sectionLink"><fmt:message key="org.jahia.admin.status.ManageStatus.title.threadsSection.label"/></a>
    </div>                
</div>
<c:if test="${empty param.threadDumpCount && (param.threadDump == 'sysout' || param.threadDump == 'file')}">
<% ThreadMonitor.getInstance().dumpThreadInfo("sysout".equals(request.getParameter("threadDump")), "file".equals(request.getParameter("threadDump"))); %>
</c:if>
<c:if test="${not empty param.threadDumpCount && (param.threadDump == 'sysout' || param.threadDump == 'file')}">
<% ThreadMonitor.getInstance().dumpThreadInfoWithInterval("sysout".equals(request.getParameter("threadDump")), "file".equals(request.getParameter("threadDump")), Integer.parseInt(StringUtils.defaultIfEmpty(request.getParameter("threadDumpCount"), "10")), Integer.parseInt(StringUtils.defaultIfEmpty(request.getParameter("threadDumpInterval"), "10"))); %>
</c:if>
<div class="stretcher">
    <table class="evenOddTable full" width="100%" border="0" cellspacing="0" cellpadding="5">
        <tr class="evenLine">
            <td align="left">
                <a href="<c:url value='/tools/threadDump.jsp'/>" target="_blank"><img src="<c:url value='/icons/filePreview.png'/>" height="16" width="16" alt=" " align="top"/><fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump"/> (<fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump.window"/>)</a>
            </td>
        </tr>
        <tr class="oddLine">
            <td align="left">
                <a href="<c:url value='/tools/threadDump.jsp?file=true'/>" target="_blank"><img src="<c:url value='/icons/download.png'/>" height="16" width="16" alt=" " align="top"/><fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump"/> (<fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump.download"/>)</a>
            </td>
        </tr>
        <tr class="evenLine">
            <td align="left">
                <a href="?do=status&amp;sub=display&amp;threadDump=sysout&amp;timestamp=${timestamp}#threads"><img src="<c:url value='/icons/tab-workflow.png'/>" height="16" width="16" alt=" " align="top"/><fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump"/> (System.out)</a>
            </td>
        </tr>
        <tr class="oddLine">
            <td align="left">
                <a href="?do=status&amp;sub=display&amp;threadDump=file&amp;timestamp=${timestamp}#threads"><img src="<c:url value='/icons/globalRepository.png'/>" height="16" width="16" alt=" " align="top"/><fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump"/> (<fmt:message key="fileMenu.label"/>)</a>
            </td>
        </tr>
        <tr class="evenLine">
            <td align="left">
                <a href="#dump" onclick="this.href='?do=status&amp;sub=display&amp;threadDump=file&amp;threadDumpCount=' + document.getElementById('threadDumpCount').value + '&amp;threadDumpInterval=' + document.getElementById('threadDumpInterval').value + '&amp;timestamp=${timestamp}#threads'; return true;"><img src="<c:url value='/icons/workflowManager.png'/>" height="16" width="16" alt=" " align="top"/><fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump"/> (<fmt:message key="org.jahia.admin.status.ManageStatus.performThreadDump.multiple"/>)</a>
                &nbsp;&nbsp;
                <label for="threadDumpCount"><fmt:message key="column.count.label"/>:&nbsp;</label><input type="text" id="threadDumpCount" name="threadDumpCount" size="2" value="${not empty param.threadDumpCount ? param.threadDumpCount : '10'}"/>
                &nbsp;&nbsp;
                <label for="threadDumpInterval"><fmt:message key="label.interval"/>:&nbsp;</label><input type="text" id="threadDumpInterval" name="threadDumpInterval" size="2" value="${not empty param.threadDumpInterval ? param.threadDumpInterval : '10'}"/>&nbsp;<fmt:message key="label.seconds"/>
            </td>
        </tr>
        <tr class="oddLine">
            <td align="left"><img src="<c:url value='/icons/tab-workflow.png'/>" height="16" width="16" alt=" " align="top"/>
                <%
                    String enableThreadMonitor = request.getParameter("enableThreadMonitor");
                    if ("true".equals(enableThreadMonitor)) {
                        ThreadMonitor.getInstance().setActivated(true);
                    } else if ("false".equals(enableThreadMonitor)) {
                        ThreadMonitor.getInstance().setActivated(false);
                    }

                %>
                <fmt:message key="label.thread.monitor.is"/>&nbsp;<%if (ThreadMonitor.getInstance().isActivated()) {%> <fmt:message key="label.started"/>
                <% } else { %><fmt:message key="label.stopped"/> <% } %> -
                <%if (ThreadMonitor.getInstance().isActivated()) {%><a href="?do=status&amp;sub=display&amp;enableThreadMonitor=false#threads"><fmt:message key="label.stop.thread.monitor"/></a>
                <% } else { %><a href="?do=status&amp;sub=display&amp;enableThreadMonitor=true#threads"><fmt:message key="label.start.thread.monitor"/></a><% } %>
            </td>
        </tr>
        <tr class="evenLine">
            <td align="left"><img src="<c:url value='/icons/tab-workflow.png'/>" height="16" width="16" alt=" " align="top"/>
                <%
                    String enableErrorFileDumper = request.getParameter("enableErrorFileDumper");
                    if ("true".equals(enableErrorFileDumper)) {
                        ErrorFileDumper.setFileDumpActivated(true);
                    } else if ("false".equals(enableErrorFileDumper)) {
                        ErrorFileDumper.setFileDumpActivated(false);
                    }

                %>
                <fmt:message key="label.error.file.dumper.is"/>&nbsp;<%if (!ErrorFileDumper.isShutdown()) {%> <fmt:message key="label.started"/>
                <% } else { %><fmt:message key="label.stopped"/> <% } %> -
                <%if (ErrorFileDumper.isShutdown()) {%><a href="?do=status&amp;sub=display&amp;enableErrorFileDumper=true#threads"><fmt:message key="label.start.error.dumper"/></a>
                <% } else { %><a href="?do=status&amp;sub=display&amp;enableErrorFileDumper=false#threads"><fmt:message key="label.stop.error.dumper"/></a><% } %>
            </td>
        </tr>
        <tr class="oddLine">
            <td align="left">
                <a href="http://java.net/projects/tda/downloads/download/webstart/tda.jnlp"
                     title="<fmt:message key='org.jahia.admin.status.ManageStatus.launchTda.disclaimer'/>" 
                     target="_blank"><img src="<c:url value='/icons/tda.gif'/>" height="16" width="16" alt=" " align="top"/><fmt:message key="org.jahia.admin.status.ManageStatus.launchTda"/></a>                
            </td>
        </tr>
    </table>
</div>

<div class="stretcher-head head" title="systemInfo">
    <div class="object-title">
      <a href="#systemInfo" class="sectionLink"><fmt:message key="org.jahia.admin.status.ManageStatus.title.systemInfoSection.label"/></a>
    </div>                
</div>
<div class="stretcher">
    <table  class="evenOddTable full" width="100%" border="0" cellspacing="0" cellpadding="5" style="table-layout: fixed;">
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
</form>
</div>
</div>
			</div>
			</td>
		</tr>
	</tbody>
</table>
</div>
  <div id="actionBar">
  	<span class="dex-PushButton"> 
	  <span class="first-child">
      	 <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
      </span>
     </span> 	      
  </div>

</div>
<script type="text/javascript">
    Element.cleanWhitespace('content');
    init();
</script>

<%@include file="/admin/include/footer.inc"%>
