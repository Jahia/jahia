<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="cacheManagement" type="org.jahia.modules.serversettings.cache.CacheManagement"--%>
<%--@elvariable id="cacheManager" type="org.jahia.modules.serversettings.flow.CacheManagerHandler"--%>
<%--@elvariable id="manager" type="java.lang.String"--%>
<%--@elvariable id="cacheEntry" type="org.jahia.modules.serversettings.flow.CacheManagerHandler.SerializedCacheManager"--%>

<c:set var="flushIcon"><img src="<c:url value='${url.currentModule}/images/showTrashboard.png'/>" height="16" width="16" alt=" " align="top"/></c:set>

<c:if test="${cacheManagement.showConfig}">
    <template:addResources type="css" resources="jquery.fancybox.css"/>
    <template:addResources type="javascript" resources="jquery.js,jquery.fancybox.pack.js"/>
    <template:addResources type="inline">
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
    </template:addResources>
</c:if>
<template:addResources type="inline">
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
</template:addResources>
<fieldset style="position: absolute; right: 20px;">
    <form action="${flowExecutionUrl}" id="navigateForm" method="POST">
        <legend><strong><fmt:message key="serverSettings.cache.settings"/></strong></legend>
        <p>
            <input id="cbActions" type="checkbox" name="showActions" ${cacheManagement.showActions?" checked":""} onclick="document.getElementById('navigateForm').submit()"/>&nbsp;<label for="cbActions"><fmt:message key="serverSettings.cache.showActions"/></label><br/>
            <input id="cbConfig" type="checkbox" name="showConfig" ${cacheManagement.showConfig?" checked":""} onclick="document.getElementById('navigateForm').submit()"/>&nbsp;<label for="cbConfig"><fmt:message key="serverSettings.cache.showConfig"/></label><br/>
            <input id="cbBytes" type="checkbox" name="showBytes" ${cacheManagement.showBytes?" checked":""} onclick="document.getElementById('navigateForm').submit()"/>&nbsp;<label for="cbBytes"><fmt:message key="serverSettings.cache.showBytes"/></label><br/>
        </p>
        <input type="hidden" name="_showActions"/>
        <input type="hidden" name="_showConfig"/>
        <input type="hidden" name="_showBytes"/>
        <input type="hidden" id="action" name="action" value=""/>
        <input type="hidden" id="name" name="name" value=""/>
        <input type="hidden" id="propagate" name="propagate" value="false"/>
        <input type="hidden" name="_eventId" value="submit"/>
    </form>
</fieldset>

<p>
    <a href="#refresh" onclick="go(); return false;" title="Refresh"><img src="<c:url value='${url.currentModule}/images/refresh.png'/>" height="16" width="16" alt=" " align="top"/>Refresh</a>
    &nbsp;&nbsp;
    <a href="#flushOutputCaches" onclick="go('action', 'flushOutputCaches'); return false;" title="<fmt:message key="serverSettings.cache.flushOutputCaches.title"/>">${flushIcon}<fmt:message key="serverSettings.cache.flushOutputCaches"/></a>
    &nbsp;&nbsp;
    <c:if test="${cacheManager.clusterActivated}">
        <a href="#flushOutputCaches" onclick="go('action', 'flushOutputCaches', 'propagate', 'true'); return false;" title="<fmt:message key="serverSettings.cache.flushOutputCaches.cluster.title"/>">${flushIcon}<fmt:message key="serverSettings.cache.flushOutputCaches.cluster"/></a>
        &nbsp;&nbsp;
    </c:if>
    <a href="#flushAllCaches" onclick="go('action', 'flushAllCaches'); return false;" title="<fmt:message key="serverSettings.cache.flushAllCaches.title"/>">${flushIcon}<fmt:message key="serverSettings.cache.flushAllCaches"/></a>
    <c:if test="${cacheManager.clusterActivated}">
        <a href="#flushAllCaches" onclick="go('action', 'flushAllCaches', 'propagate', 'true'); return false;" title="<fmt:message key="serverSettings.cache.flushAllCaches.cluster.title"/>">${flushIcon}<fmt:message key="serverSettings.cache.flushAllCaches.cluster"/></a>
        &nbsp;&nbsp;
    </c:if>
</p>

<c:forEach items="${cacheManager.managersMap}" var="entry" varStatus="managerStatus">
    <c:set var="manager" value="${entry.key}"/>
    <h2>Cache Manager: ${manager}
        <c:if test="${cacheManagement.showConfig}">
            &nbsp;
            <a class="configLink" title="<fmt:message key="serverSettings.cache.configLink.title"/>" href="#managerconfig-${managerStatus.index}"><img src="<c:url value='${url.currentModule}/images/help.png'/>" width="16" height="16" alt="?" title="<fmt:message key="serverSettings.cache.configLink.title"/>"/></a>
            <div style="display: none;">
                <div id="managerconfig-${managerStatus.index}">
                    <h3>${fn:escapeXml(manager)}</h3>
                    <pre>${fn:escapeXml(manager)}</pre>
                </div>
            </div>
        </c:if>
    </h2>
    <c:if test="${cacheManagement.showActions}">
        <a href="#flushCaches" onclick="go('action', 'flushCaches', 'name', '${manager}'); return false;" title="<fmt:message key="serverSettings.cache.flushCaches.title"/>">${flushIcon}<fmt:message key="serverSettings.cache.flushCaches"/>&nbsp;${manager}</a>
        <c:if test="${cacheManager.clusterActivated}">
            &nbsp;&nbsp;
            <a href="#flushCaches" onclick="go('action', 'flushCaches', 'name', '${manager}', 'propagate', 'true'); return false;" title="<fmt:message key="serverSettings.cache.flushCaches.cluster.title"/>">${flushIcon}<fmt:message key="serverSettings.cache.flushCaches.cluster"/>&nbsp;${manager}</a>
        </c:if>
        &nbsp;&nbsp;
        <a href="#enableStats" onclick="go('action', 'enableStats', 'name', '${manager}'); return false;" title="<fmt:message key="serverSettings.cache.stats.enable.title"/>"><fmt:message key="serverSettings.cache.stats.enable"/></a>
        <a href="#disableStats" onclick="go('action', 'disableStats', 'name', '${manager}'); return false;" title="<fmt:message key="serverSettings.cache.stats.disable.title"/>"><fmt:message key="serverSettings.cache.stats.disable"/></a>
    </c:if>
    <table border="1" cellspacing="0" cellpadding="5">
        <thead>
        <tr>
            <th rowspan="2">#</th>
            <c:if test="${cacheManagement.showConfig}">
                <th rowspan="2">?</th>
            </c:if>
            <th rowspan="2"><fmt:message key="serverSettings.cache.names"/></th>
            <th colspan="3">Entries<fmt:message key="serverSettings.cache.entries"/></th>
            <c:if test="${cacheManagement.showBytes}">
                <th colspan="2"><fmt:message key="serverSettings.cache.size"/></th>
            </c:if>
            <th colspan="4"><fmt:message key="serverSettings.cache.stats"/></th>
            <c:if test="${cacheManagement.showActions}">
                <th rowspan="2"><fmt:message key="label.actions"/></th>
            </c:if>
        </tr>
        <tr>
            <th><fmt:message key="serverSettings.cache.total"/></th>
            <th><fmt:message key="serverSettings.cache.memory"/></th>
            <th><fmt:message key="serverSettings.cache.disk"/></th>
            <c:if test="${cacheManagement.showBytes}">
                <th><fmt:message key="serverSettings.cache.memory"/></th>
                <th><fmt:message key="serverSettings.cache.disk"/></th>
            </c:if>
            <th><fmt:message key="serverSettings.cache.total"/></th>
            <th><fmt:message key="serverSettings.cache.hits"/></th>
            <th><fmt:message key="serverSettings.cache.misses"/></th>
            <th><fmt:message key="serverSettings.cache.rate"/></th>
        </tr>
        </thead>
        <tbody>
        <c:set var="entriesTotal" value="0"/>
        <c:set var="entriesMemory" value="0"/>
        <c:set var="entriesDisk" value="0"/>

        <c:set var="sizeMemory" value="0"/>
        <c:set var="sizeDisk" value="0"/>

        <c:set var="accessTotal" value="0"/>
        <c:set var="accessHits" value="0"/>
        <c:set var="accessMisses" value="0"/>

        <c:forEach items="${cacheManager.managersMap[manager]}" var="cacheEntry" varStatus="status">
            <c:set var="activeCfg" value="${cacheEntry.config}"/>
            <c:set var="statsEnabled" value="${cacheEntry.statisticsEnabled}"/>

            <tr>
                <td><strong>${status.index + 1}</strong></td>
                <c:if test="${cacheManagement.showConfig}">
                    <td align="center">
                        <a class="configLink" title="<fmt:message key="serverSettings.cache.showDetail"/>" href="#config-${managerStatus.index}-${status.index}"><img src="<c:url value='${url.currentModule}/images/help.png'/>" width="16" height="16" alt="?" title="<fmt:message key="serverSettings.cache.showDetail"/>"/></a>
                        <div style="display: none;">
                            <div id="config-${managerStatus.index}-${status.index}">
                                <h3>${fn:escapeXml(cacheEntry.name)}</h3>
                                <pre>${fn:escapeXml(activeCfg)}</pre>
                            </div>
                        </div>
                    </td>
                </c:if>

                <td>${cacheEntry.name}</td>
                <td align="center">${cacheEntry.objectCount}</td>
                <td align="center">${cacheEntry.memoryStoreObjectCount}</td>
                <td align="center">${cacheEntry.overflowToDisk ? cacheEntry.diskStoreObjectCount : '-'}</td>
                <c:set var="entriesTotal" value="${entriesTotal + cacheEntry.objectCount}"/>
                <c:set var="entriesMemory" value="${entriesMemory + cacheEntry.memoryStoreObjectCount}"/>
                <c:set var="entriesDisk" value="${entriesDisk +  cacheEntry.diskStoreObjectCount}"/>

                <c:if test="${cacheManagement.showBytes}">
                    <td align="center">${cacheEntry.calculateInMemorySize}</td>
                    <td align="center">
                        <c:if test="${cacheEntry.overflowToDisk}">
                            ${cacheEntry.calculateOnDiskSize}
                        </c:if>
                        <c:if test="${!cacheEntry.overflowToDisk}">
                            -
                        </c:if>
                    </td>
                </c:if>

                <c:set var="accessTotal" value="${accessTotal + cacheEntry.cacheHits + cacheEntry.cacheMisses}"/>
                <c:set var="accessHits" value="${accessHits + cacheEntry.cacheHits}"/>
                <c:set var="accessMisses" value="${accessMisses + cacheEntry.cacheMisses}"/>

                <td align="center">${statsEnabled ? cacheEntry.cacheHits + cacheEntry.cacheMisses : '-'}</td>
                <td align="center">${statsEnabled ? cacheEntry.cacheHits : '-'}</td>
                <td align="center">${statsEnabled ? cacheEntry.cacheMisses : '-'}</td>
                <c:if test="${statsEnabled}">
                    <c:set var="cacheEfficiency" value="${cacheEntry.cacheHits + cacheEntry.cacheMisses > 0 ? cacheEntry.cacheHits * 100 / (cacheEntry.cacheHits + cacheEntry.cacheMisses) : 0}"/>
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
                <c:if test="${cacheManagement.showActions}">
                    <td align="center"><a href="#flush" onclick="go('action', 'flush', 'name', '${cacheEntry.name}'); return false;" title="Remove all entries from the ${cacheEntry.name}">${flushIcon}</a></td>
                </c:if>
            </tr>
        </c:forEach>
        <tr>
            <td colspan="${cacheManagement.showConfig ? '3' : '2'}">Total</td>
            <td align="center">${entriesTotal}</td>
            <td align="center">${entriesMemory}</td>
            <td align="center">${entriesDisk}</td>
            <c:if test="${cacheManagement.showBytes}">
                <td align="center">${cacheManager.sizeMemory}</td>
                <td align="center">${cacheManager.sizeDisk}</td>
            </c:if>
            <td align="center">${accessTotal}</td>
            <td align="center">${accessHits}</td>
            <td align="center">${accessMisses}</td>
            <td>&nbsp;</td>
            <c:if test="${cacheManagement.showActions}">
                <td align="center">&nbsp;</td>
            </c:if>
        </tr>
        </tbody>
    </table>
</c:forEach>