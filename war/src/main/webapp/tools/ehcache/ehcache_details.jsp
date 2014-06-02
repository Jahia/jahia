<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="net.sf.ehcache.Ehcache" %>
<%@ page import="net.sf.ehcache.Element" %>
<%@ page import="org.apache.commons.io.FileUtils" %>
<%@ page import="org.jahia.services.cache.CacheHelper" %>
<%@ page import="org.jahia.services.cache.ehcache.EhCacheStatisticsWrapper" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.List" %>
<%@ page import="java.lang.instrument.Instrumentation" %>
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
        Ehcache cache = CacheHelper.getCacheManager(pageContext.getRequest().getParameter("name")).getEhcache(pageContext.getRequest().getParameter("cache"));
        boolean removed = cache.remove(request.getParameter("flushkey"));
        pageContext.setAttribute("removed", removed);
    %>
</c:if>
<html>
<head>
    <style type="text/css" title="currentStyle">
        @import "../resources/css/demo_page.css";
        @import "../resources/css/demo_table_jui.css";
        @import "../resources/css/TableTools_JUI.css";
        @import "../resources/css/le-frog/jquery-ui-1.8.13.custom.css";
    </style>
    <script type="text/javascript" src="../resources/jquery.min.js"></script>
    <script type="text/javascript" src="../resources/jquery.dataTables.min.js"></script>
    <script type="text/javascript" src="../resources/ZeroClipboard.js"></script>
    <script type="text/javascript" src="../resources/TableTools.js"></script>
    <title>Display content of module output cache</title>
    <script type="text/javascript">
        var myTable = $(document).ready(function () {
            $('#cacheTable').dataTable({
                "bLengthChange": true,
                "bFilter": true,
                "bSort": true,
                "bInfo": false,
                "bAutoWidth": true,
                "bStateSave": true,
                "bJQueryUI": true,
                "sPaginationType": "full_numbers",
                "aLengthMenu": [
                    [50, 100, 200, -1],
                    [50, 100, 200, "All"]
                ],
                "sDom": '<"H"Tlfr>t<"F"p>',
                "oTableTools": {
                    "sSwfPath": "../resources/swf/copy_cvs_xls.swf",
                    "aButtons": [
                        "copy", "csv", "xls",
                        {
                            "sExtends": "collection",
                            "sButtonText": "Save",
                            "aButtons": [ "csv", "xls" ]
                        }
                    ]
                }
            });
        });
    </script>
</head>
<%

    Ehcache cache = CacheHelper.getCacheManager(pageContext.getRequest().getParameter("name")).getEhcache(pageContext.getRequest().getParameter("cache"));
    if (pageContext.getRequest().getParameter("flush") != null) {
        System.out.println("Flushing cache content");
        cache.flush();
        cache.removeAll();
    }
    List keys = cache.getKeys();
    pageContext.setAttribute("keys", keys);
    pageContext.setAttribute("cache", cache);
    EhCacheStatisticsWrapper ehCacheStatisticsWrapper = new EhCacheStatisticsWrapper(cache.getStatistics());
    pageContext.setAttribute("stats", ehCacheStatisticsWrapper);
%>
<body id="dt_example">
<a href="../index.jsp" title="back to the overview of caches">overview</a>&nbsp;
<a href="?refresh&name=${param.name}&cache=${param.cache}">refresh</a>&nbsp;
<div id="statistics">
    <span>Cache Hits: ${stats.cacheHitCount} (Cache hits in memory : ${stats.localHeapHitCount}; Cache hits on disk : ${stats.localDiskHitCount})</span><br/>
    <span>Cache Miss: ${stats.cacheMissCount}</span><br/>
    <span>Object counts: ${stats.size}</span><br/>
    <span>Memory size: <%=FileUtils.byteCountToDisplaySize(ehCacheStatisticsWrapper.getLocalHeapSizeInBytes())%></span><br/>
    <span>Disk size: <%=FileUtils.byteCountToDisplaySize(ehCacheStatisticsWrapper.getLocalDiskSizeInBytes())%></span><br/>
    <span>Cache entries size = <span id="cacheSize"></span></span><br/>
    <span><%=ehCacheStatisticsWrapper%></span><br/>
</div>
<div id="keys">
    <table id="cacheTable" class="display">
        <thead>
        <tr>
            <th>Key</th>
            <th>Expiration</th>
            <th>Value</th>
        </tr>
        </thead>
        <tbody>
        <% long cacheSize = 0; %>
        <c:forEach items="${keys}" var="key" varStatus="i">

            <tr class="gradeA">
                <td>${key}</td>
                <% String attribute = (String) pageContext.getAttribute("key");
                    final Element element1 = cache.getQuiet(attribute);
                %>

                <td><%=SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(new Date(
                        element1.getExpirationTime()))%>
                </td>
                <%
                    cacheSize += element1.getSerializedSize();
                %>
                <td>
                    <div style="text-align: center;">
                        <c:url var="flushUrl" value="ehcache_details.jsp?">
                            <c:param name="flushkey" value="${key}"/>
                            <c:param name="name" value="${param.name}"/>
                            <c:param name="cache" value="${param.cache}"/>
                        </c:url>
                        <a href="${flushUrl}">flush</a>
                        <br/>[<%= FileUtils.byteCountToDisplaySize(element1.getSerializedSize()).replace(" ", "&nbsp;") %>
                        ]
                    </div>
                </td>
            </tr>
        </c:forEach>
        <script type="text/javascript">
            $(document).ready(function () {
                $("#cacheSize").before("<%= FileUtils.byteCountToDisplaySize(cacheSize) %>");
            });
        </script>
        </tbody>
    </table>
</div>
</body>
</html>