<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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