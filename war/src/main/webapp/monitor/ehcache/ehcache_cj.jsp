<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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
  Output cache monitoring JSP.
  User: rincevent
  Date: 28 mai 2008
  Time: 16:59:07
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<c:if test="${not empty param.key}">
<html>
<body>
<%
    Element elem = ((EhCacheProvider) ServicesRegistry.getInstance().getCacheService().getCacheProviders().get(
            "EH_CACHE")).getCacheManager().getCache(CacheFilter.CACHE_NAME).get(request.getParameter("key"));
    Object obj = elem != null ? ((CacheEntry) elem.getValue()).getObject() : null;
%><%= obj %>
</body>
</html>
</c:if>
<c:if test="${empty param.key}">
<html>
<head>
    <script type="text/javascript" src="jquery.min.js" language="JavaScript"></script>
    <script type="text/javascript" src="jquery.dataTables.min.js" language="JavaScript"></script>
    <title>Display content of module output cache</title>
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
<body style="background-color: #EAEAEA">
<a href="index.html" title="back to the overview of caches">overview</a>&nbsp;
<a href="?flush=true" onclick="return confirm('This will flush the content of the cache. Would you like to continue?')" title="flush the content of the module output cache">flush</a>&nbsp;
<a href="?viewContent=${param.viewContent ? 'false' : 'true'}">${param.viewContent ? 'hide content preview' : 'preview content'}</a>
<div id="statistics">
    <span>Cache Hits = <%=statistics.getCacheHits()%> (Cache hits in memory : <%=statistics.getInMemoryHits()%>; Cache hits on disk : <%=statistics.getOnDiskHits()%>)</span><br/>
    <span>Cache Miss = <%=statistics.getCacheMisses()%></span><br/>
    <span>Object counts = <%=statistics.getObjectCount()%></span><br/>
    <span>Memory size = <%=containerCache.getMemoryStoreSize()%></span><br/>
    <span>Disk size = <%=containerCache.getDiskStoreSize()%></span><br/>
    <span>Cache entries size = <span id="cacheSize"></span></span><br/>
</div>
<div id="keys">
    <table border="1" style="background-color: #CCCCCC" id="cacheTable">
        <thead>
        <tr>
            <th>Key</th>
            <th>Expiration</th>
            <th>Value</th>
            <th>Dependencies</th>
        </tr>
        </thead>
        <tbody>
        <% long cacheSize = 0; %>
        <c:set var="depsCacheSize" value="0"/>
        <c:forEach items="${keys}" var="key" varStatus="i">

            <tr <c:if test="${i.index mod 2 == 0}">style="background-color:#F8F8F8;"</c:if>>
                    <% String attribute = (String) pageContext.getAttribute("key");
                        final Element element1 = containerCache.getQuiet(attribute);
                        if (element1 != null) {
                    %>

                <td>${key}</td>
                <td><%=SimpleDateFormat.getDateTimeInstance().format(new Date(element1.getExpirationTime()))%></td>
                <% String content = (String)((CacheEntry)element1.getValue()).getObject();
                cacheSize += content != null ? content.length() : 0;
                %>
                <td>
                	<c:if test="${param.viewContent}" var="viewContent">
                		<%= content %>
                	</c:if>
                	<c:if test="${not viewContent}">
                		<center>
                		<a href="ehcache_cj.jsp?key=${key}" target="_blank">view</a>
                		<br/>[<%= content.length() %>&nbsp;bytes]
                		</center>
                	</c:if>
                </td>
                <td><%
                    Element element = depCache.getQuiet(attribute.split("__")[0]);
                    if(element !=null) {
                        Set<String> deps = (Set<String>) element.getValue();
                        for (String dep : deps) {
                            out.print(dep+"<br/>");
                        }
                    }
                %></td>
                <%}%>
            </tr>
        </c:forEach>
	    <script type="text/javascript">
		    $(document).ready(function() {
		        $("#cacheSize").before("<%= cacheSize %> bytes");
	        });
	    </script>
        </tbody>
    </table>
</div>
<a href="index.html">overview</a>
</body>
</html>
</c:if>