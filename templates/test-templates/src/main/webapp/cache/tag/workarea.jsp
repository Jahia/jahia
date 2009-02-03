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

<%@ page import="org.jahia.data.beans.ContainerBean" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="java.util.Date" %>
<%@ include file="../../common/declarations.jspf" %>
<jsp:useBean id="now" class="java.util.Date"/>
<jsp:useBean id="lastDate" class="java.util.Date"/>
<jsp:useBean id="dependencies" class="java.util.HashSet"/>
<p>
    Display a long view of the container and reuse the the id of objects displayed to define antoher piece of cache
    that need to be flushed when on of this objects is updated.
</p>
<%
    Date date = null;
%>
<template:containerList name="cachecontent" id="cachecontentList">
    <template:addDependency set="${dependencies}" bean="${cachecontentList}"/>
    <template:container id="cachecontentContainer">
        <div><fmt:formatDate value="${now}" dateStyle="full" type="both"/><br/>
            <template:field name="mainContentTitle"/>
            <template:field name="mainContentBody"/>
            <%
                // This code is purely for example as the latest publication date of a container list
                // is available as a metadata
                ContainerBean containerBean = (ContainerBean) pageContext.getAttribute("cachecontentContainer");
                final Date metadataAsDate = containerBean.getContentContainer().getMetadataAsDate("lastPublishingDate", (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean"));
                if (metadataAsDate != null) {
                    if (date == null) {
                        date = metadataAsDate;
                    } else if (metadataAsDate.getTime() > date.getTime()) {
                        date = metadataAsDate;
                    }
                    lastDate = date;
                }
            %></div>
    </template:container>
</template:containerList>

<p>When one of the dependencies objects will be updated this piece of cache will also be updated</p>
<span>start of cache</span>
<template:cache cacheKey="tagkey" dependencies="${dependencies}">
    <span>The last content publication date is <fmt:formatDate value="${lastDate}" dateStyle="full"
                                                               type="both"/><br/></span>
</template:cache>
<span>end of cache</span>

<p>
    An example of how to cache a full container list in only one cache entry :
</p>
<template:cache cacheKey="fullListInOneEntry">
    <template:containerList name="cachecontent" id="cachecontentList">
        <template:addDependency bean="${cachecontentList}"/>
        <template:container id="cachecontentContainer" cache="off">
            <template:addDependency bean="${cachecontentContainer}"/>
            <div>
                <fmt:formatDate value="${now}" dateStyle="full" type="both"/><br/>
                <template:field name="mainContentTitle" beanID="title"/>
                <template:field name="mainContentBody" beanID="body"/>
            </div>
        </template:container>
    </template:containerList>
</template:cache>
<p>
    Rss output of http://www.theserverside.com/ with an expiration of one minute for testing purpose
</p>
<template:cache cacheKey="rssOutput" expiration="60">
    <c:import var="xml" url="http://www.theserverside.com/rss/theserverside-rss2.xml"/>    
    <x:parse var="rss" xml="${xml}"/>
    <ul>
        <x:forEach select="$rss//channel/item" var="n">
            <li>
                <a href="<x:out select="$n/link"/>">
                    <x:out select="$n/title"/>
                </a><br/>
                <span><x:out select="$n/description" escapeXml="false"/></span>
            </li>
        </x:forEach>
    </ul>
    <span> Rss updated on : <fmt:formatDate value="${now}" dateStyle="full" type="both"/><br/></span>
</template:cache>