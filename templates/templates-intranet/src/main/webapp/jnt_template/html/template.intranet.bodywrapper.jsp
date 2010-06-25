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
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../../common/declarations.jspf" %>
<jcr:nodeProperty var="theme" node="${renderContext.mainResource.node}" name="j:theme" inherited="true"/>
<c:if test="${!empty theme}">
    <c:forEach var="themeFile" items="${jcr:getChildrenOfType(theme.node,'nt:file')}">
        <template:addResources type="css" resources="${themeFile.url}" insert="true"/>
    </c:forEach>
</c:if>
<template:addResources type="css" resources="960.css,01web.css,02mod.css,navigation.css,navigationN1-1.css,navigationN1-2.css,navigationN1-3.css,navigationN2-1.css,navigationN2-2.css" />
<c:if test="${renderContext.editMode}">
    <template:addResources type="css" resources="edit.css" />
</c:if>
<jcr:node var="rootPage" path="/sites/${renderContext.site.siteKey}/home"/>

<div id="bodywrapper"><!--start bodywrapper-->
    <div id="topheader"><!--start topheader-->
        <div class="container container_16">
            <div class="grid_16">
                <div class="logotop"><a href="#"><template:area path="logo"/></a></div>
            </div>
        </div>
        <div class="clear"></div>
    </div>
    <!--stop topheader-->
    <div id="bottomheader"><!--start bottomheader-->
        <div class="container container_16">
            <div class="grid_10">
                <div id="banner"><!--start banner-->
                    <template:area path="pagetitle"/>
                </div>
			<div class="clear"></div></div>
            <div class="grid_6">
                <div id="search-bar">
                    <template:area path="simpleSearch"/>
                </div>
			<div class="clear"></div></div>
            <div class="grid_16">
                <template:area path="topMenu"/>
            </div>
        </div>
        <div class="clear"></div>
    </div>
    <!--stop bottomheader-->
    <div id="content"><!--start content-->
        <div class="container container_16">
            <div class="grid_16">
                <template:area path="wrappercontent"/>
            </div>
        </div>
        <div class="clear"></div>
    </div>
    <!--stop content-->


    <div id="topfooter"><!--start topfooter-->
        <div class="container container_16"> <!--start container_16-->
			<div class="grid_16">
            	<template:area path="topfooter" nodeTypes="jnt:row" />
			<div class="clear"></div></div>
        <div class="clear"></div></div> <!--stop container_16-->
    <div class="clear"></div></div><!--stop topfooter-->

    <div id="bottomfooter"><!--start bottomfooter-->
        <div class="container container_16"> <!--start container_16-->
			<div class="grid_16">
                 <template:area path="footer" nodeTypes="jnt:row" />
			<div class="clear"></div></div>       
        <div class="clear"></div></div> <!--stop container_16-->
    </div>
    <!--stop bottomfooter-->


    <div class="clear"></div>
</div>
<!--stop bodywrapper-->