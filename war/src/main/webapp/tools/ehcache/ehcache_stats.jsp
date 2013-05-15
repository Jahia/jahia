<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="net.sf.ehcache.Ehcache" %>
<%@ page import="net.sf.ehcache.Element" %>
<%@ page import="org.jahia.services.cache.CacheEntry" %>
<%@ page import="org.jahia.services.cache.ehcache.EhCacheStatisticsWrapper" %>
<%@ page import="org.jahia.services.render.filter.cache.AclCacheKeyPartGenerator" %>
<%@ page import="org.jahia.services.render.filter.cache.ModuleCacheProvider" %>
<%--
  Output cache monitoring JSP.
  User: rincevent
  Date: 28 mai 2008
  Time: 16:59:07
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<c:if test="${not empty param.flushkey}">
    <%
        System.out.println(request.getParameter("flushkey"));
        boolean removed = ModuleCacheProvider.getInstance().getCache().remove(request.getParameter("flushkey"));
        pageContext.setAttribute("removed", removed);
    %>
</c:if>
<c:if test="${not empty param.key}">
    <html>
    <body>
    <%
        System.out.println(request.getParameter("key"));
        Element elem = ModuleCacheProvider.getInstance().getCache().getQuiet(request.getParameter("key"));
        Object obj = elem != null ? ((CacheEntry) elem.getValue()).getObject() : null;
    %><%= obj %>
    </body>
    </html>
</c:if>
<c:if test="${empty param.key}">
    <html>
    <head>
        <style type="text/css" title="currentStyle">
            @import "../resources/css/demo_page.css";
            @import "../resources/css/demo_table_jui.css";
            @import "../resources/css/TableTools_JUI.css";
            @import "../resources/css/le-frog/jquery-ui-1.8.13.custom.css";
        </style>
        <script type="text/javascript" src="../resources/jquery.min.js"></script>
        <title>Display content of module output cache</title>
    </head>
    <%
        ModuleCacheProvider cacheProvider = ModuleCacheProvider.getInstance();
        Ehcache cache = cacheProvider.getCache();
        Ehcache depCache = cacheProvider.getDependenciesCache();
        if (pageContext.getRequest().getParameter("flush") != null) {
            System.out.println("Flushing cache content");
            cache.flush();
            cache.removeAll();
            depCache.flush();
            depCache.removeAll();
            ((AclCacheKeyPartGenerator) cacheProvider.getKeyGenerator().getPartGenerator("acls")).flushUsersGroupsKey();
        }
        pageContext.setAttribute("cache", cache);
        pageContext.setAttribute("stats", new EhCacheStatisticsWrapper(cache.getStatistics()));
        pageContext.setAttribute("depstats", new EhCacheStatisticsWrapper(depCache.getStatistics()));
    %>
    <body id="dt_example">
    <a href="../index.jsp" title="back to the overview of caches">overview</a>&nbsp;
    <a href="?refresh">refresh</a>&nbsp;
    <a href="?flush=true"
       onclick="return confirm('This will flush the content of the cache. Would you like to continue?')"
       title="flush the content of the module output cache">flush</a>&nbsp;
    <a href="?viewContent=${param.viewContent ? 'false' : 'true'}">${param.viewContent ? 'hide content preview' : 'preview content'}</a>
    <c:if test="${not empty removed and removed}">
        <p>Key (${requestScope.flushkey}) has been flushed</p>
    </c:if>
    <div id="statistics">
        <p>Module statistics</p>
        <span>Cache Hits: ${stats.cacheHitCount} (Cache hits in memory : ${stats.localHeapHitCount}; Cache hits on disk : ${stats.localDiskHitCount})</span><br/>
        <span>Cache Miss: ${stats.cacheMissCount}</span><br/>
        <span>Object counts: ${stats.size}</span><br/>
        <span>Memory size: ${cache.memoryStoreSize}</span><br/>
        <span>Disk size: ${cache.diskStoreSize}</span><br/>

        <p>Dependencies statistics</p>
        <span>Cache Hits: ${depstats.cacheHitCount} (Cache hits in memory : ${depstats.localHeapHitCount}; Cache hits on disk : ${depstats.localDiskHitCount})</span><br/>
        <span>Cache Miss: ${depstats.cacheMissCount}</span><br/>
        <span>Object counts: ${depstats.size}</span><br/>
    </div>
    </body>
    </html>
</c:if>
