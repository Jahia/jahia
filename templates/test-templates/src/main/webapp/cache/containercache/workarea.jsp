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
