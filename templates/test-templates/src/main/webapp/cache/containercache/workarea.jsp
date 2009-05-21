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
<%@ include file="../../common/declarations.jspf" %>
<p>
    Display summary of the container (title field only), no cache key pass :<br/>
    <code>&lt;content:containerList name="cachecontent" id="cachecontentList"&gt;<br/>
        &lt;content:container id="cachecontentContainer"&gt;<br/>
        &lt;content:field name="mainContentTitle"&gt;<br/>
        &lt;/content:container&gt;<br/>
        &lt;/content:containerList&gt;<br/></code>
</p>
<template:containerList name="cachecontent" id="cachecontentList">
    <template:container id="cachecontentContainer">
        <template:field name="mainContentTitle"/>
    </template:container>
</template:containerList>

<p>
    Try to display a long view of the container but without any cache information :<br/>
    <code>&lt;content:containerList name="cachecontent" id="cachecontentList"&gt;<br/>
        &lt;content:container id="cachecontentContainer"&gt;<br/>
        &lt;content:field name="mainContentTitle"&gt;<br/>
        &lt;content:field name="mainContentBody"&gt;<br/>
        &lt;/content:container&gt;<br/>
        &lt;/content:containerList&gt;</code>
</p>
<template:containerList name="cachecontent" id="cachecontentList">
    <template:container id="cachecontentContainer">
        <template:field name="mainContentTitle"/>
        <template:field name="mainContentBody"/>
    </template:container>
</template:containerList>
<p>
    Here we see that this display is the same as the first one as jahia reuse our container from the cache<br/>
    and so the subtags have not been parsed.
</p>

<p>
    Try to display a long view of the container but with cache information :<br/>
    <code>&lt;template:containerList name="cachecontent" id="cachecontentList"&gt;<br/>
        &lt;template:container id="cachecontentContainer" cacheKey="longView"&gt;<br/>
        &lt;template:field name="mainContentTitle"&gt;<br/>
        &lt;template:field name="mainContentBody"&gt;<br/>
        &lt;/template:container&gt;<br/>
        &lt;/template:containerList&gt;</code>
</p>
<template:containerList name="cachecontent" id="cachecontentList">
    <template:container id="cachecontentContainer" cacheKey="longView">
        <template:field name="mainContentTitle"/>
        <template:field name="mainContentBody"/>
    </template:container>
</template:containerList>
<p>
    Try to display a long view of the container but with dynamic cache information :<br/>
    <code>&lt;template:containerList name="cachecontent" id="cachecontentList"&gt;<br/>
        &lt;template:container id="cachecontentContainer" cacheKey="${param.selectedColor}"&gt;<br/>
        &lt;template:field name="mainContentTitle"&gt;<br/>
        &lt;template:field name="mainContentBody"&gt;<br/>
        &lt;/template:container&gt;<br/>
        &lt;/template:containerList&gt;</code>
</p>
<template:containerList name="cachecontent" id="cachecontentList">
    <template:container id="cachecontentContainer" cacheKey="${param.selectedColor}">
        <div style="background-color:${param.selectedColor}">
            <template:field name="mainContentTitle"/>
            <template:field name="mainContentBody"/>
        </div>
    </template:container>
</template:containerList>
<form method="get" action="">
    <select name="selectedColor">
        <option value="red">Red</option>
        <option value="green">Green</option>
    </select>
    <input type="submit"/>
</form>

<p>
    Display a long view of the container but with an expiration different than the default one, expiration is expressed
    in seconds :<br/>
    <code>&lt;template:containerList name="cachecontent" id="cachecontentList"&gt;<br/>
        &lt;template:container id="cachecontentContainer" cacheKey="expired" expiration="30"&gt;<br/>
        &lt;template:field name="mainContentTitle"&gt;<br/>
        &lt;template:field name="mainContentBody"&gt;<br/>
        &lt;/template:container&gt;<br/>
        &lt;/template:containerList&gt;</code>
</p>
<template:containerList name="cachecontent" id="cachecontentList">
    <template:container id="cachecontentContainer" cacheKey="expired" expiration="30">
        <jsp:useBean id="now" class="java.util.Date"/>
        <fmt:formatDate value="${now}" dateStyle="full" type="both"/><br/>
        <template:field name="mainContentTitle"/>
        <template:field name="mainContentBody"/>
    </template:container>
</template:containerList>
