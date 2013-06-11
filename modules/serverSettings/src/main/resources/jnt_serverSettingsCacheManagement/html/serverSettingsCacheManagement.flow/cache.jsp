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
<template:addResources type="javascript" resources="jquery.js,jquery.fancybox.pack.js,admin-bootstrap.js,jquery.metadata.js,jquery.tablesorter.js,jquery.tablecloth.js"/>
<template:addResources type="css" resources="jquery.fancybox.css,tablecloth.css"/>
<c:if test="${cacheManagement.showConfig}">
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
<template:addResources>
    <script type="text/javascript" charset="utf-8">
        $(document).ready(function() {
            $("table").tablecloth({
                theme: "default",
                sortable: true
            });
        });
    </script>
</template:addResources>
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

    <form style="margin: 0 0 5px 0;" action="${flowExecutionUrl}" id="navigateForm" method="POST">
            <fieldset>
            <div style="margin: 0;" class="alert alert-info">
                <h2 style="margin: 0;"><fmt:message key="serverSettings.cache.settings"/></h2>
                <label class="checkbox inline" for="cbActions">
                    <input id="cbActions" type="checkbox" name="showActions" ${cacheManagement.showActions?" checked":""} onclick="document.getElementById('navigateForm').submit()"/> <fmt:message key="serverSettings.cache.showActions"/>
                </label>
                <label class="checkbox inline" for="cbConfig">
                    <input id="cbConfig" type="checkbox" name="showConfig" ${cacheManagement.showConfig?" checked":""} onclick="document.getElementById('navigateForm').submit()"/> <fmt:message key="serverSettings.cache.showConfig"/>
                </label>
                <label class="checkbox inline" for="cbBytes">
                    <input id="cbBytes" type="checkbox" name="showBytes" ${cacheManagement.showBytes?" checked":""} onclick="document.getElementById('navigateForm').submit()"/> <fmt:message key="serverSettings.cache.showBytes"/>
                </label>
            </div>
            <input type="hidden" name="_showActions"/>
            <input type="hidden" name="_showConfig"/>
            <input type="hidden" name="_showBytes"/>
            <input type="hidden" id="action" name="action" value=""/>
            <input type="hidden" id="name" name="name" value=""/>
            <input type="hidden" id="propagate" name="propagate" value="false"/>
            <input type="hidden" name="_eventId" value="submit"/>
        </fieldset>
    </form>


<div>
    <a class="btn" href="#refresh" onclick="go(); return false;" title="<fmt:message key='label.refresh'/>">
        <i class="icon-refresh"></i>
        &nbsp;<fmt:message key="label.refresh"/>
    </a>
    <a class="btn" href="#flushOutputCaches" onclick="go('action', 'flushOutputCaches'); return false;" title="<fmt:message key="serverSettings.cache.flushOutputCaches.title"/>">
        <i class="icon-trash"></i>
        &nbsp;<fmt:message key="serverSettings.cache.flushOutputCaches"/>
    </a>
    <c:if test="${cacheManager.clusterActivated}">
        <a class="btn" href="#flushOutputCaches" onclick="go('action', 'flushOutputCaches', 'propagate', 'true'); return false;" title="<fmt:message key="serverSettings.cache.flushOutputCaches.cluster.title"/>">
            <i class="icon-trash"></i>
            &nbsp;<fmt:message key="serverSettings.cache.flushOutputCaches.cluster"/>
        </a>
    </c:if>
    <a class="btn" href="#flushAllCaches" onclick="go('action', 'flushAllCaches'); return false;" title="<fmt:message key="serverSettings.cache.flushAllCaches.title"/>">
        <i class="icon-trash"></i>
        &nbsp;<fmt:message key="serverSettings.cache.flushAllCaches"/>
    </a>
    <c:if test="${cacheManager.clusterActivated}">
        <a class="btn" href="#flushAllCaches" onclick="go('action', 'flushAllCaches', 'propagate', 'true'); return false;" title="<fmt:message key="serverSettings.cache.flushAllCaches.cluster.title"/>">
            <i class="icon-trash"></i>
            &nbsp;<fmt:message key="serverSettings.cache.flushAllCaches.cluster"/>
        </a>
    </c:if>
</div>

<c:forEach items="${cacheManager.managersMap}" var="entry" varStatus="managerStatus">
    <c:set var="manager" value="${entry.key}"/>
    <h2>Cache Manager: ${manager}
        <c:if test="${cacheManagement.showConfig}">
            &nbsp;
            <a class="btn btn-info configLink" title="<fmt:message key='serverSettings.cache.configLink.title'/>" href="#managerconfig-${managerStatus.index}">
                <i class="icon-info-sign icon-white"></i>
            </a>
            <div style="display: none;">
                <div id="managerconfig-${managerStatus.index}">
                    <h3>${fn:escapeXml(manager)}</h3>
                    <pre>${fn:escapeXml(manager)}</pre>
                </div>
            </div>
        </c:if>
    </h2>
    <c:if test="${cacheManagement.showActions}">
        <p>
            <a class="btn" href="#flushCaches" onclick="go('action', 'flushCaches', 'name', '${manager}'); return false;" title="<fmt:message key="serverSettings.cache.flushCaches.title"/>">
                <i class="icon-trash"></i>
                &nbsp;<fmt:message key="serverSettings.cache.flushCaches"/>
                &nbsp;${manager}
            </a>
            <c:if test="${cacheManager.clusterActivated}">
                <a class="btn" href="#flushCaches" onclick="go('action', 'flushCaches', 'name', '${manager}', 'propagate', 'true'); return false;" title="<fmt:message key="serverSettings.cache.flushCaches.cluster.title"/>">
                    <i class="icon-trash"></i>
                    &nbsp;<fmt:message key="serverSettings.cache.flushCaches.cluster"/>
                    &nbsp;${manager}
                </a>
            </c:if>
            <a class="btn" href="#enableStats" onclick="go('action', 'enableStats', 'name', '${manager}'); return false;" title="<fmt:message key="serverSettings.cache.stats.enable.title"/>">
                <i class="icon-play"></i>
                <fmt:message key="serverSettings.cache.stats.enable"/>
            </a>
            <a class="btn" href="#disableStats" onclick="go('action', 'disableStats', 'name', '${manager}'); return false;" title="<fmt:message key="serverSettings.cache.stats.disable.title"/>">
                <i class="icon-stop"></i>
                <fmt:message key="serverSettings.cache.stats.disable"/>
            </a>
        </p>
    </c:if>
    <table class="table table-bordered table-hover table-striped">
        <thead>
        <tr>
            <th rowspan="2">#</th>
            <c:if test="${cacheManagement.showConfig}">
                <th rowspan="2">?</th>
            </c:if>
            <th rowspan="2"><fmt:message key="serverSettings.cache.names"/></th>
            <th colspan="3"><fmt:message key="serverSettings.cache.entries"/></th>
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
                        <a class="btn btn-info configLink" title="<fmt:message key='serverSettings.cache.showDetail'/>" href="#config-${managerStatus.index}-${status.index}">
                            <i class="icon-info-sign icon-white"></i>
                        </a>
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
                            <c:set var="effColour" value="label-important"/>
                        </c:when>
                        <c:when test="${cacheEfficiency >= 30 && cacheEfficiency < 70}">
                            <c:set var="effColour" value="label-info"/>
                        </c:when>
                        <c:when test="${cacheEfficiency >= 70}">
                            <c:set var="effColour" value="label-success"/>
                        </c:when>
                    </c:choose>
                    <td align="center"><span class="label ${effColour}"><fmt:formatNumber value="${cacheEfficiency}" pattern="0.00"/></span></td>
                </c:if>
                <c:if test="${!statsEnabled}">
                    <td align="center">-</td>
                </c:if>
                <c:if test="${cacheManagement.showActions}">
                    <td align="center">
                        <a class="btn" href="#flush" onclick="go('action', 'flush', 'name', '${cacheEntry.name}'); return false;" title="Remove all entries from the ${cacheEntry.name}">
                            <i class="icon-trash"></i>
                        </a>
                    </td>
                </c:if>
            </tr>
        </c:forEach>
        <tr class="info">
            <td colspan="${cacheManagement.showConfig ? '3' : '2'}"><fmt:message key="serverSettings.cache.total"/></td>
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