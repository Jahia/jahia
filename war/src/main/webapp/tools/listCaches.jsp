<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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