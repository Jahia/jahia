<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
<%@ page import="net.sf.ehcache.Cache" %>
<%@ page import="net.sf.ehcache.CacheManager" %>
<%@ page import="net.sf.ehcache.Statistics" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.cache.ehcache.EhCacheProvider" %>
<%@ page import="org.jahia.services.render.filter.cache.CacheFilter" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.jahia.services.cache.CacheEntry" %>
<%@ page import="net.sf.ehcache.Element" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
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
<head>
    <script type="text/javascript" src="jquery.js" language="JavaScript"></script>
    <script type="text/javascript" src="jquery.dataTables.min.js" language="JavaScript"></script>
    <%--<script type="text/javascript" src="fixedHeader.js" language="JavaScript"></script>--%>
    <title>Display content of Container cache</title>
    <script type="text/javascript">
        var myTable = $(document).ready(function() {
            $('#cacheTable').dataTable({
                "bPaginate": true,
                "bLengthChange": true,
                "bFilter": true,
                "bSort": true,
                "bInfo": false,
                "bAutoWidth": true,
                "bStateSave" : true,
                "aoColumns": [
                    null,
                    {
                        "sType": "html"
                    }
                ]
            });
        });
    </script>
</head>
<%
    EhCacheProvider provider = (EhCacheProvider) ServicesRegistry.getInstance().getCacheService().getCacheProviders().get(
            "EH_CACHE");
    CacheManager cacheManager = provider.getCacheManager();
    Cache containerCache = cacheManager.getCache(CacheFilter.CACHE_NAME);
    Cache depCache = cacheManager.getCache(CacheFilter.CACHE_NAME+"dependencies");
    Statistics statistics = containerCache.getStatistics();
    if (pageContext.getRequest().getParameter("flush") != null) {
        containerCache.flush();
        containerCache.clearStatistics();
        depCache.flush();
        depCache.clearStatistics();
    }
    List keys = containerCache.getKeys();
    Collections.sort(keys);
    pageContext.setAttribute("keys", keys);
%>
<body bgcolor="whitesmoke">
<a href="index.html">index</a>&nbsp;<a href="ehcache_cj.jsp?flush=true">flush</a>

<div id="statistics">
    <span>Cache Hits = <%=statistics.getCacheHits()%> (Cache hits in memory : <%=statistics.getInMemoryHits()%>; Cache hits on disk : <%=statistics.getOnDiskHits()%>)</span><br/>
    <span>Cache Miss = <%=statistics.getCacheMisses()%></span><br/>
    <span>Object counts = <%=statistics.getObjectCount()%></span><br/>
    <span>Memory size = <%=containerCache.getMemoryStoreSize()%></span><br/>
    <span>Disk size = <%=containerCache.getDiskStoreSize()%></span><br/>
</div>
<div id="keys">
    <table border="1" bgcolor="rgb(230,230,230)" id="cacheTable">
        <thead>
        <tr>
            <th>Key</th>
            <th>Expiration</th>
            <th>Value</th>
            <th>Dependencies</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${keys}" var="key" varStatus="i">

            <tr <c:if test="${i.index mod 2 == 0}">style="background-color:rgb(180,180,180);"</c:if>>
                    <% String attribute = (String) pageContext.getAttribute("key");
                        final Element element1 = containerCache.get(attribute);
                        if (element1 != null) {
                    %>

                <td>${key}</td>
                <td><%=SimpleDateFormat.getDateTimeInstance().format(new Date(element1.getExpirationTime()))%></td>
                <td><%=((CacheEntry) element1.getValue()).getObject()%>
                </td>
                <td><%
                    Element element = depCache.get(attribute.split("__")[0]);
                    if(element !=null) {
                        Set<String> deps = (Set<String>) element.getValue();
                        for (String dep : deps) {
                            out.print(dep+"<br/>");
                        }
                    }
                %></td>
                <%  containerCache.put(element1);
                    }%>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
<a href="index.html">index</a>
</body>
</html>