<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.cache.ehcache.EhCacheProvider" %>
<%@ page import="net.sf.ehcache.CacheManager" %>
<%@ page import="net.sf.ehcache.Cache" %>
<%@ page import="net.sf.ehcache.Statistics" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jahia.services.cache.*" %>
<%@ page import="java.util.Collections" %>
<%--
  Created by IntelliJ IDEA.
  User: rincevent
  Date: 28 mai 2008
  Time: 16:59:07
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--<%@ taglib uri="http://displaytag.sf.net/el" prefix="display" %>--%>
<html>
  <head><title>Display content of Container cache</title></head>
  <%
      EhCacheProvider provider = (EhCacheProvider) ServicesRegistry.getInstance().getCacheService().getCacheProviders().get("EH_CACHE");
      CacheManager cacheManager = provider.getCacheManager();
      Cache containerCache = cacheManager.getCache(ContainerHTMLCache.CONTAINER_HTML_CACHE);
      Statistics statistics = containerCache.getStatistics();
      List keys = containerCache.getKeys();
      Collections.sort(keys);
  %>
  <body>
  <a href="index.html">index</a>
    <div id="statistics">
        <span>Cache Hits = <%=statistics.getCacheHits()%> (Cache hits in memory : <%=statistics.getInMemoryHits()%>; Cache hits on disk : <%=statistics.getOnDiskHits()%>)</span><br/>
        <span>Cache Miss = <%=statistics.getCacheMisses()%></span><br/>
        <span>Object counts = <%=statistics.getObjectCount()%></span><br/>
        <span>Memory size = <%=containerCache.getMemoryStoreSize()%></span><br/>
        <span>Disk size = <%=containerCache.getDiskStoreSize()%></span><br/>
    </div>
    <div id="keys">
        <table>
        <%
            for (int i = 0; i < keys.size(); i++) {
                GroupCacheKey o = (GroupCacheKey) keys.get(i);
        %>
        <tr><td>key : <%=o.getKey()%></td></tr><tr><td>value : <%=((ContainerHTMLCacheEntry) ((CacheEntry) containerCache.get(o).getValue()).getObject()).getBodyContent()%></td></tr>
        <%
            }
        %>
        </table>
    </div>
  <a href="index.html">index</a>
  </body>
</html>