<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="net.sf.ehcache.Ehcache" %>
<%@ page import="net.sf.ehcache.Element" %>
<%@ page import="org.jahia.services.cache.CacheEntry" %>
<%@ page import="org.jahia.services.render.filter.cache.DefaultCacheKeyGenerator" %>
<%@ page import="org.jahia.services.render.filter.cache.ModuleCacheProvider" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
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
        System.out.println(request.getParameter("key"));
        Element elem = ModuleCacheProvider.getInstance().getCache().get(request.getParameter("key"));
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
        <script type="text/javascript" src="../resources/jquery.min.js" language="JavaScript"></script>
        <script type="text/javascript" src="../resources/jquery.dataTables.min.js" language="JavaScript"></script>
        <script type="text/javascript" src="../resources/ZeroClipboard.js"></script>
        <script type="text/javascript" src="../resources/TableTools.js"></script>
        <title>Display content of module cache dependencies</title>
        <script type="text/javascript">
            var myTable = $(document).ready(function () {
                $('#cacheTable').dataTable({
                    "bLengthChange":true,
                    "bFilter":true,
                    "bSort":true,
                    "bInfo":false,
                    "bAutoWidth":true,
                    "bStateSave":true,
                    "sPaginationType":"full_numbers",
                    "bJQueryUI":true,
                    "aLengthMenu":[
                        [50, 100, 200, -1],
                        [50, 100, 200, "All"]
                    ],
                    "sDom":'<"H"Tlfr>t<"F"p>',
                    "oTableTools":{
                        "sSwfPath":"../resources/swf/copy_cvs_xls.swf",
                        "aButtons":[
                            "copy", "csv", "xls", {
                                "sExtends":"collection",
                                "sButtonText":"Save",
                                "aButtons":[ "csv", "xls" ]
                            }
                        ]
                    }
                });
            });
        </script>
    </head>
    <%
        ModuleCacheProvider cacheProvider = ModuleCacheProvider.getInstance();
        Ehcache cache = cacheProvider.getCache();
        Ehcache depCache = cacheProvider.getDependenciesCache();
        if (pageContext.getRequest().getParameter("flush") != null) {
            System.out.println("Flushing cache content");
            cache.flush();
            cache.clearStatistics();
            cache.removeAll();
            depCache.flush();
            depCache.clearStatistics();
            depCache.removeAll();
            ((DefaultCacheKeyGenerator) cacheProvider.getKeyGenerator()).flushUsersGroupsKey();
        }
        List keys = depCache.getKeys();
        Collections.sort(keys);
        pageContext.setAttribute("keys", keys);
        pageContext.setAttribute("cache", cache);
    %>
    <body id="dt_example">
    <a href="../index.jsp" title="back to the overview of caches">overview</a>&nbsp;
    <a href="?flush=true"
       onclick="return confirm('This will flush the content of the cache. Would you like to continue?')"
       title="flush the content of the module output cache">flush</a>&nbsp;
    <div id="keys">
        <table id="cacheTable" class="display">
            <thead>
            <tr>
                <th>Key</th>
                <th>Dependencies</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${keys}" var="key" varStatus="i">
                <% String attribute = (String) pageContext.getAttribute("key");
                    final Element element = depCache.getQuiet(attribute);
                    if (element != null) {
                %>
                <tr class="gradeA">

                    <td>${key}</td>
                    <td><%
                        Set<String> deps = (Set<String>) element.getValue();
                        if(deps.size()>10) {
                        out.print("Number of dependencies : "+deps.size());
                        } else {
                            for (String dep : deps) {
                                out.print(dep + "<br/>");
                            }
                        }%>
                        <br/>
                    </td>
                </tr>
                <%}%>
            </c:forEach>
            </tbody>
        </table>
    </div>
    </body>
    </html>
</c:if>