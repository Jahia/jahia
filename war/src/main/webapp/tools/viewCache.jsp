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
<%@page import="com.thoughtworks.xstream.io.xml.DomDriver"%>
<%@page import="org.jahia.services.cache.GroupCacheKey"%>
<%@page import="org.jahia.services.cache.ContainerHTMLCache"%>
<%@page import="org.jahia.services.cache.ContainerHTMLCacheEntry"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="com.thoughtworks.xstream.XStream"%>
<%@page import="com.thoughtworks.xstream.io.xml.XppDriver"%>
<%@page import="com.thoughtworks.xstream.io.HierarchicalStreamWriter"%>
<%@page import="com.thoughtworks.xstream.io.xml.CompactWriter"%>
<%@page import="java.io.Writer"%>
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
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%
    String cacheName = (String) pageContext.findAttribute("cacheName");
    String cacheKey = (String) pageContext.findAttribute("cacheKey");
    String resourceKey = "org.jahia.admin.status.ManageStatus.cache."
            + cacheName + ".description.label";

    Cache cache = ServicesRegistry.getInstance().getCacheService()
            .getCache(cacheName);
%>
<head>
    <title>View Jahia's Cache</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/andromeda.css" type="text/css" />
</head>
<body class="jahiaTool">
  <div id="mainAdminLayout">
    <%=cacheName%> : <strong><fmt:message key="<%=resourceKey%>"/></strong>
<%
    if (cacheKey != null && cacheKey.trim().length() > 0) {
        XStream serializer = new XStream(new DomDriver());
        Object objectKey = cacheKey;
        Class keyType = null;
        Object key = cache.getKeys().iterator().next();
        if (key != null) {
            keyType = key.getClass();
            if (!keyType.equals(cacheKey.getClass())) {
                if (keyType.equals(Integer.class)) {
                    objectKey = new Integer(cacheKey);
                } else if (keyType.equals(Long.class)) {
                    objectKey = new Long(cacheKey);
                } else if (keyType.equals(GroupCacheKey.class)) {
                    int keyIndex = cacheKey.indexOf(GroupCacheKey
                            .getKeyGroupSeparator());
                    String groupKey = keyIndex != -1 ? cacheKey
                            .substring(0, keyIndex) : null;
                    Set groups = new HashSet();
                    int groupIndex = cacheKey.indexOf(GroupCacheKey
                            .getGroupSeparator(),
                            keyIndex != -1 ? keyIndex
                                    + GroupCacheKey
                                            .getKeyGroupSeparator()
                                            .length() : 0);
                    while (groupIndex != -1) {
                        int nextGroupIndex = cacheKey.indexOf(
                                GroupCacheKey.getGroupSeparator(),
                                groupIndex
                                        + GroupCacheKey
                                                .getGroupSeparator()
                                                .length());
                        String group = cacheKey.substring(groupIndex
                                + GroupCacheKey.getGroupSeparator()
                                        .length(),
                                nextGroupIndex != -1 ? nextGroupIndex
                                        : cacheKey.length());
                        groups.add(group);
                        groupIndex = nextGroupIndex;
                    }
                    objectKey = groupKey != null ? new GroupCacheKey(
                            groupKey, groups) : new GroupCacheKey(
                            groups);
                }
            }
        }
%>
<br/>
  Key: <strong><%=cacheKey%></strong>
<%if ("ContainerHTMLCache".equals(cacheName)) { 
    ContainerHTMLCacheEntry entry = (ContainerHTMLCacheEntry)cache.get(objectKey);
%>
    <%=entry != null ? entry.getBodyContent() : "<empty>"%>
<%} else { %>
<pre>
<%=StringEscapeUtils.escapeXml(serializer.toXML(cache.get(objectKey)))%>
</pre>
<%
}
    } else {
%>
    <table border="1" cellspacing="0" cellpadding="5" class="evenOddTable">
<thead>
<tr>
<th>No.</th>
<th>Key</th>
<th>Content</th>
<th></th>
</tr>
</thead>
        <%
            Collection keys = cache.getKeys();
                int cacheCounter = 0;
                String cacheLineClass = "evenLine";
                for (Object objectKey : keys) {
                    if (cacheCounter % 2 == 0) {
                        cacheLineClass = "evenLine";
                    } else {
                        cacheLineClass = "oddLine";
                    }
                    cacheCounter++;
        %>
<tbody>
        <tr class="<%=cacheLineClass%>">
            <td>
            <%=cacheCounter%>.
            </td>
            <td>
            <%=objectKey%>
            </td>
            <td width="100%">
            <%=cache.get(objectKey)%>
            </td>
            <td>
<form name="jahiaTool" action='viewCache' method="post">
                <input type="hidden" name="key" size="20" title="Key" value="<%=objectKey%>"/>
                <nobr><a href="?itemview_<%=cacheName%>=1&key=<%=objectKey%>"><fmt:message key="org.jahia.admin.status.ManageStatus.remove.label"/></a></nobr>
</form>
            </td>
        </tr>            
<%
                }
            %>
</tbody>
</table>
<%
    }
%>
</div>
</body>
</html>