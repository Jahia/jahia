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
<%@page contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<%@page import = "org.jahia.bin.*"%>
<%@page import = "java.util.*" %>
<%@page import = "java.text.*" %>
<%@page import = "org.jahia.registries.*" %>
<%@page import = "org.jahia.services.cache.Cache" %>
<%@page import = "org.jahia.services.cache.CacheFactory" %>
<%@page import="org.jahia.settings.SettingsBean"%>
<%@page import="org.jahia.params.ProcessingContext"%>
<%@page import="org.jahia.hibernate.manager.SpringContextSingleton" %>
<%@page import="org.jahia.hibernate.cache.JahiaBatchingClusterCacheHibernateProvider" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>

<head>
    <title>View Jahia's Cache</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/andromeda.css" type="text/css" />
</head>
<body class="jahiaTool">
  <div id="mainAdminLayout">

<form name="jahiaTool" action='viewCache' method="post">
    <table border="1" cellspacing="0" cellpadding="5" class="evenOddTable">
<tbody>
        <%
            Set<String> sortedCacheNames = new TreeSet<String>(ServicesRegistry.getInstance().getCacheService().getNames());
            DecimalFormat percentFormat = new DecimalFormat("###.##");
            int cacheCounter = 0;
            String cacheLineClass = "evenLine";
            for (String curCacheName : sortedCacheNames) {
                if (cacheCounter % 2 == 0) {
                    cacheLineClass = "evenLine";
                } else {
                    cacheLineClass = "oddLine";
                }
                cacheCounter++;
                Object objectCache = ServicesRegistry.getInstance().getCacheService().getCache (curCacheName);
                if (objectCache instanceof Cache) {
                    Cache curCache = (Cache) objectCache;
                    String resourceKey = "org.jahia.admin.status.ManageStatus.cache." + curCache.getName() + ".description.label";
                    long cacheLimit = curCache.getCacheLimit()/(1024*1024);
                    String efficiencyStr = "0";
                    if (!Double.isNaN(curCache.getCacheEfficiency())) {
                        efficiencyStr = percentFormat.format(curCache.getCacheEfficiency());
                    }
        %>
        <tr class="<%=cacheLineClass%>">
            <td width="100%">
                <%=curCache.getName()%> : <strong><fmt:message key="<%=resourceKey%>"/></strong>
                <br>
                <%=curCache.size()%>&nbsp;
                <% if(curCache.size() > 1){
                %><fmt:message key="org.jahia.admin.entries.label"/><%
            } else {
            %><fmt:message key="org.jahia.admin.entrie.label"/><%
                } %>
                &nbsp;
                <fmt:message key="org.jahia.admin.status.ManageStatus.groupSize.label"/> :
                <%=curCache.getGroupsSize()%>
                &nbsp;
                <fmt:message key="org.jahia.admin.status.ManageStatus.groupsKeysTotal.label"/> :
                <%=curCache.getGroupsKeysTotal()%>
                <br/>
                <fmt:message key="org.jahia.admin.status.ManageStatus.successfulHits.label"/> :
                <%=curCache.getSuccessHits()%> / <%=curCache.getTotalHits()%>&nbsp;
                <fmt:message key="org.jahia.admin.status.ManageStatus.totalHits.label"/>,
                <fmt:message key="org.jahia.admin.status.ManageStatus.efficiency.label"/> :
                <%=efficiencyStr%> %
            </td>
            <td>
                Key (optional): <input type="text" name="key_<%=curCache.getName()%>" size="20" title="Key"/> 
                <input type="submit" name="view_<%=curCache.getName()%>" value="<fmt:message key="org.jahia.admin.status.ManageStatus.flush.label"/>
                <nobr><a href="?view_<%=curCache.getName()%>=1">View all</a></nobr>
            </td>
        </tr>
        <%
                }
            }
        %>
</tbody>
</table>
</form>
</div>
</body>