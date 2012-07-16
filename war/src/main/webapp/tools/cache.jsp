<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.jahia.utils.FileUtils"%>
<%@page import="net.sf.ehcache.Cache"%>
<%@page import="java.util.Arrays"%>
<%@page import="net.sf.ehcache.CacheManager"%>
<%@page import="org.jahia.services.cache.CacheHelper"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<c:set var="showActions" value="${functions:default(param.showActions, 'false')}"/>
<c:set var="showBytes" value="${functions:default(param.showBytes, 'false')}"/>
<c:set var="showConfig" value="${functions:default(param.showConfig, 'false')}"/>
<% pageContext.setAttribute("clusterActivated", Boolean.getBoolean("cluster.activated")); %>
<c:set var="flushIcon"><img src="<c:url value='/icons/showTrashboard.png'/>" height="16" width="16" alt=" " align="top"/></c:set>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" href="tools.css" type="text/css" />
    <c:if test="${showConfig}">
    <link type="text/css" href="resources/jquery.fancybox-1.3.4.css" rel="stylesheet"/>
    <script type="text/javascript" src="resources/jquery.min.js"></script>
    <script type="text/javascript" src="resources/jquery.fancybox-1.3.4.js"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            $('.configLink').fancybox({
                        'hideOnContentClick': false,
                        'titleShow' : false,
                        'transitionOut' : 'none',
                        'autoDimensions' : false,
                        'width' : 800,
                        'height' : 600
                    });
        });
    </script>
    </c:if>
    <script type="text/javascript">
        function go(id1, value1, id2, value2, id3, value3) {
            if (id1) {
                document.getElementById(id1).value=value1;
            }
            if (id2) {
                document.getElementById(id2).value=value2;
            }
            if (id3) {
                document.getElementById(id3).value=value3;
            }
            document.getElementById('navigateForm').submit();
        }
    </script>
    <title>Cache Management</title>
</head>
<body>
<h1>Cache Management</h1>
<fieldset style="position: absolute; right: 20px;">
    <legend><strong>Settings</strong></legend>
    <p>
        <input id="cbActions" type="checkbox" ${showActions ? 'checked="checked"' : ''}
                onchange="go('showActions', '${!showActions}')"/>&nbsp;<label for="cbActions">Show actions</label><br/>
        <input id="cbConfig" type="checkbox" ${showConfig ? 'checked="checked"' : ''}
                onchange="go('showConfig', '${!showConfig}')"/>&nbsp;<label for="cbConfig">Show config details</label><br/>
        <input id="cbBytes" type="checkbox" ${showBytes ? 'checked="checked"' : ''}
                onchange="go('showBytes', '${!showBytes}')"/>&nbsp;<label for="cbBytes">Show size in bytes (expensive)</label><br/>
    </p>
    <form id="navigateForm" action="?" method="get">
        <input type="hidden" id="showActions" name="showActions" value="${showActions}"/>
        <input type="hidden" id="showConfig" name="showConfig" value="${showConfig}"/>
        <input type="hidden" id="showBytes" name="showBytes" value="${showBytes}"/>
        <input type="hidden" id="action" name="action" value=""/>
        <input type="hidden" id="name" name="name" value=""/>
        <input type="hidden" id="propagate" name="propagate" value="false"/>
    </form>
</fieldset>

<c:if test="${not empty param.action}">
	<c:choose>
		<c:when test="${param.action == 'flushOutputCaches'}">
			<% CacheHelper.flushOutputCaches(Boolean.valueOf(request.getParameter("propagate"))); %>
			<p style="color: blue">Output HTML caches were successfully flushed</p>
		</c:when>
        <c:when test="${param.action == 'flushHibernateCaches'}">
			<% CacheHelper.flushHibernateCaches(Boolean.valueOf(request.getParameter("propagate"))); %>
            <p style="color: blue">Hibernate second level caches were successfully flushed</p>
        </c:when>
        <c:when test="${param.action == 'flushCaches' && not empty param.name}">
            <% CacheHelper.flushCachesForManager(request.getParameter("name"), Boolean.valueOf(request.getParameter("propagate"))); %>
            <p style="color: blue">Caches for manager ${param.name} were successfully flushed</p>
        </c:when>
        <c:when test="${param.action == 'flush' && not empty param.name}">
            <% CacheHelper.flushEhcacheByName(request.getParameter("name"), Boolean.valueOf(request.getParameter("propagate"))); %>
            <p style="color: blue">Cache ${param.name} was successfully flushed</p>
        </c:when>
        <c:when test="${param.action == 'flushAllCaches'}">
			<% CacheHelper.flushAllCaches(Boolean.valueOf(request.getParameter("propagate"))); %>
            <p style="color: blue">All caches were successfully flushed</p>
        </c:when>
        <c:when test="${(param.action == 'enableStats' || param.action == 'disableStats') && not empty param.name}">
            <c:set var="doDisable" value="${param.action == 'disableStats'}"/>
            <%
                CacheManager mgr = CacheHelper.getCacheManager(request.getParameter("name"));
                boolean doDisable = (Boolean) pageContext.getAttribute("doDisable");
                for (String cacheName : mgr.getCacheNames()) {
                    Cache cache = mgr.getCache(cacheName);
                    if (cache == null) {
                        continue;
                    }
                    if (doDisable && cache.isStatisticsEnabled() || !doDisable && !cache.isStatisticsEnabled()) {
                        cache.setStatisticsEnabled(!doDisable);
                        if (doDisable) {
                            cache.clearStatistics();
                        }
                    }
                }
             %>
            <p style="color: blue">Statistics ${doDisable ? 'disabled' : 'enabled'} for caches in manager ${param.name}</p>
        </c:when>
	</c:choose>
</c:if>
<p>
<a href="#refresh" onclick="go(); return false;" title="Refresh"><img src="<c:url value='/icons/refresh.png'/>" height="16" width="16" alt=" " align="top"/>Refresh</a>
&nbsp;&nbsp;
<a href="#flushOutputCaches" onclick="go('action', 'flushOutputCaches'); return false;" title="Performs flush of module output caches that are responsible for caching HTML page and fragment output, rendered in Live mode">${flushIcon}Flush HTML output caches</a>
&nbsp;&nbsp;
<c:if test="${clusterActivated}">
<a href="#flushOutputCaches" onclick="go('action', 'flushOutputCaches', 'propagate', 'true'); return false;" title="Does the same flush as 'Flush HTML output caches' also propagating flush to all cluster nodes">${flushIcon}Flush HTML output caches (all across the cluster)</a>
&nbsp;&nbsp;
</c:if>
<a href="#flushAllCaches" onclick="go('action', 'flushAllCaches'); return false;" title="Triggers the flush of all caches, including back-end, front-end (module output) and Hibernate second level caches">${flushIcon}Flush all caches</a>
<c:if test="${clusterActivated}">
<a href="#flushAllCaches" onclick="go('action', 'flushAllCaches', 'propagate', 'true'); return false;" title="Does the same flush as 'Flush all caches' also propagating flush to all cluster nodes">${flushIcon}Flush all caches (all across the cluster)</a>
&nbsp;&nbsp;
</c:if>
</p>
<h2>Status</h2>
<% pageContext.setAttribute("cacheManagers", CacheManager.ALL_CACHE_MANAGERS); %>
<c:forEach items="${cacheManagers}" var="manager" varStatus="managerStatus">
<h3>Cache Manager: ${manager.name}
<c:if test="${showConfig}">
&nbsp;
<a class="configLink" title="Cache configuration details" href="#managerconfig-${managerStatus.index}"><img src="<c:url value='/css/images/andromeda/icons/help.png'/>" width="16" height="16" alt="?" title="Cache configuration details"/></a>
<div style="display: none;">
    <div id="managerconfig-${managerStatus.index}">
        <h3>${fn:escapeXml(manager.name)}</h3>
        <pre>${fn:escapeXml(manager.activeConfigurationText)}</pre>
    </div>
</div>
</c:if>
</h3>
<c:if test="${showActions}">
<a href="#flushCaches" onclick="go('action', 'flushCaches', 'name', '${manager.name}'); return false;" title="Flushes all caches of this manager">${flushIcon}Flush caches for this manager</a>
<c:if test="${clusterActivated}">
&nbsp;&nbsp;
<a href="#flushCaches" onclick="go('action', 'flushCaches', 'name', '${manager.name}', 'propagate', 'true'); return false;" title="Does the same flush as 'Flush caches for this manager' also propagating flush to all cluster nodes">${flushIcon}Flush caches for this manager (all across the cluster)</a>
</c:if>
&nbsp;&nbsp;
<a href="#enableStats" onclick="go('action', 'enableStats', 'name', '${manager.name}'); return false;" title="Enables statistics on caches of this manager">Enable statistics</a>
&nbsp;&nbsp;
<a href="#disableStats" onclick="go('action', 'disableStats', 'name', '${manager.name}'); return false;" title="Disables statistics on caches of this manager">Disable statistics</a>
</c:if>
<table border="1" cellspacing="0" cellpadding="5">
    <thead>
        <tr>
            <th rowspan="2">#</th>
            <c:if test="${showConfig}">
                <th rowspan="2">?</th>
            </c:if>
            <th rowspan="2">Name</th>
            <th colspan="3">Entries</th>
            <c:if test="${showBytes}">
                <th colspan="2">Size</th>
            </c:if>
            <th colspan="4">Access statistics</th>
            <c:if test="${showActions}">
                <th rowspan="2">Actions</th>
            </c:if>
        </tr>
        <tr>
            <th>total</th>
            <th>memory</th>
            <th>disk</th>
            <c:if test="${showBytes}">
                <th>memory</th>
                <th>disk</th>
            </c:if>            
            <th>total</th>
            <th>hits</th>
            <th>misses</th>
            <th>rate, %</th>
        </tr>
    </thead>
    <tbody>
        <%
        CacheManager manager = (CacheManager) pageContext.getAttribute("manager");
        String[] names = manager.getCacheNames();
        Arrays.sort(names);
        pageContext.setAttribute("cacheNames", names);
        %>
        <c:set var="entriesTotal" value="0"/>
        <c:set var="entriesMemory" value="0"/>
        <c:set var="entriesDisk" value="0"/>
        
        <c:set var="sizeMemory" value="0"/>
        <c:set var="sizeDisk" value="0"/>
        
        <c:set var="accessTotal" value="0"/>
        <c:set var="accessHits" value="0"/>
        <c:set var="accessMisses" value="0"/>
        
        <c:forEach items="${cacheNames}" var="cacheName" varStatus="status">
        <%
            String cacheName = (String) pageContext.getAttribute("cacheName");
            Cache cache = manager.getCache(cacheName);
            pageContext.setAttribute("cache", cache);
            pageContext.setAttribute("activeCfg", manager.getActiveConfigurationText(cacheName));
        %>
        <c:set var="cfg" value="${cache.cacheConfiguration}"/>
        <c:set var="stats" value="${cache.statistics}"/>
        <c:set var="statsEnabled" value="${cache.statisticsEnabled}"/>
        
        <c:set var="entriesTotal" value="${entriesTotal + stats.objectCount}"/>
        <c:set var="entriesMemory" value="${entriesMemory + stats.memoryStoreObjectCount}"/>
        <c:set var="entriesDisk" value="${entriesDisk + stats.diskStoreObjectCount}"/>
        
        <tr>
            <td><strong>${status.index + 1}</strong></td>
            <c:if test="${showConfig}">
            <td align="center">
                <a class="configLink" title="Cache configuration details" href="#config-${managerStatus.index}-${status.index}"><img src="<c:url value='/css/images/andromeda/icons/help.png'/>" width="16" height="16" alt="?" title="Cache configuration details"/></a>
                <div style="display: none;">
                    <div id="config-${managerStatus.index}-${status.index}">
                        <h3>${fn:escapeXml(cache.name)}</h3>
                        <pre>${fn:escapeXml(activeCfg)}</pre>
                    </div>
                </div>
            </td>
            </c:if>
            <td>${cache.name}</td>
            <td align="center">${stats.objectCount}</td>
            <td align="center">${stats.memoryStoreObjectCount}</td>
            <td align="center">${cfg.overflowToDisk ? stats.diskStoreObjectCount : '-'}</td>
            
            <c:if test="${showBytes}">
                <% long inMemorySize = cache.calculateInMemorySize(); %>
                <c:set var="inMemorySize"><%= inMemorySize %></c:set>
                <c:set var="sizeMemory" value="${sizeMemory + inMemorySize}"/>
                <td align="center"><%= FileUtils.humanReadableByteCount(inMemorySize) %></td>
                <td align="center">
                    <c:if test="${cfg.overflowToDisk}">
                    <% long diskMemorySize = cache.calculateOnDiskSize(); %>
                    <c:set var="diskMemorySize"><%= diskMemorySize %></c:set>
                    <c:set var="sizeDisk" value="${sizeDisk + diskMemorySize}"/>
                    <%= FileUtils.humanReadableByteCount(diskMemorySize) %>
                    </c:if>
                    <c:if test="${!cfg.overflowToDisk}">
                    -
                    </c:if>
                </td>
            </c:if>
            
            <c:set var="accessTotal" value="${accessTotal + stats.cacheHits + stats.cacheMisses}"/>
            <c:set var="accessHits" value="${accessHits + stats.cacheHits}"/>
            <c:set var="accessMisses" value="${accessMisses + stats.cacheMisses}"/>
            
            <td align="center">${statsEnabled ? stats.cacheHits + stats.cacheMisses : '-'}</td>
            <td align="center">${statsEnabled ? stats.cacheHits : '-'}</td>
            <td align="center">${statsEnabled ? stats.cacheMisses : '-'}</td>
            <c:if test="${statsEnabled}">
                <c:set var="cacheEfficiency" value="${stats.cacheHits + stats.cacheMisses > 0 ? stats.cacheHits * 100 / (stats.cacheHits + stats.cacheMisses) : 0}"/>
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
                <td align="center"><span style="color: ${effColour}"><fmt:formatNumber value="${cacheEfficiency}" pattern="0.00"/></span></td>
            </c:if>
            <c:if test="${!statsEnabled}">
                <td align="center">-</td>
            </c:if>
            <c:if test="${showActions}">
                <td align="center"><a href="#flush" onclick="go('action', 'flush', 'name', '${cache.name}'); return false;" title="Remove all entries from the ${cache.name}">${flushIcon}</a></td>
            </c:if>
        </tr>
        </c:forEach>
        <tr>
            <td colspan="${showConfig ? '3' : '2'}">Total</td>
            <td align="center">${entriesTotal}</td>
            <td align="center">${entriesMemory}</td>
            <td align="center">${entriesDisk}</td>
            <c:if test="${showBytes}">
                <td align="center"><%= FileUtils.humanReadableByteCount((Long) pageContext.getAttribute("sizeMemory")) %></td>
                <td align="center"><%= FileUtils.humanReadableByteCount((Long) pageContext.getAttribute("sizeDisk")) %></td>
            </c:if>
            <td align="center">${accessTotal}</td>
            <td align="center">${accessHits}</td>
            <td align="center">${accessMisses}</td>
            <td>&nbsp;</td>
            <c:if test="${showActions}">
                <td align="center">&nbsp;</td>
            </c:if>
        </tr>
    </tbody>
</table>
</c:forEach>
<p>
    <a href="<c:url value='/administration/?do=status&sub=display'/>">to Server and Cache status (Jahia Administration)</a>
</p>
<%@ include file="gotoIndex.jspf" %>
</body>
</html>