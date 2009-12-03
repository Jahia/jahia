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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../../common/declarations.jspf" %>
<template:addResources type="css" resources="960.css,01web.css" nodetype="jnt:page"/>
<jcr:node var="rootPage" path="/sites/${renderContext.site.siteKey}/home"/>

<div id="bodywrapper"><!--start bodywrapper-->
    <div id="topheader"><!--start topheader-->
        <div class="container container_16">
            <div class="grid_16">
                <div class="logotop"><a href="#"><template:module path="${rootPage.path}/logo" nodeTypes="jnt:image"/></a></div>
            </div>
        </div>
        <div class="clear"></div>
    </div>
    <!--stop topheader-->
    <div id="bottomheader"><!--start bottomheader-->
        <div class="container container_16">
            <div class="grid_16">
                <div id="search-bar"><!--start search-bar-->
                    <form class="active" method="get" action="#">
                        <div class="form-container">
                            <input type="text" value="Start Searching ..." name="term" class="text-input"/>
                            <input type="submit" value="Search" class="submit"/>
                        </div>
                    </form>
                </div>
                <!--stop search-bar-->
                <div id="breadcrumbs"><!--start breadcrumbs-->
                    <c:set var="currentPath" value=""/>
                    <c:forTokens items="${currentNode.path}" delims="/" var="itemPath" varStatus="status">
                        <c:set var="currentPath" value="${currentPath}/${itemPath}"/>
                        <jcr:node var="node" path="${currentPath}"/>
                        <c:if test="${jcr:isNodeType(node, 'jnt:page')}">
                        <c:if test="${not status.last}"><a href="${url.base}${currentPath}.html"></c:if>${node.propertiesAsString['jcr:title']}<c:if test="${not status.last}"></a> /</c:if>
                        </c:if>
                    </c:forTokens>
                </div>
                <!--stop breadcrumbs-->
                <h1>${currentNode.propertiesAsString['jcr:title']}</h1>
                <div class="clear"></div>
                    <template:module path="${rootPage.path}/topMenu">
                        <template:import>
                            <topMenu xmlns:j='http://www.jahia.org/jahia/1.0' xmlns:jcr='http://www.jcp.org/jcr/1.0'
                                    jcr:primaryType='jnt:navBar' j:maxDepth='1' j:nodename='jnt_navBar' j:startLevel='1' jcr:mixinTypes='jmix:renderable'
                                    j:template='dropDown'/>
                        </template:import>
                    </template:module>
            </div>
        </div>
        <div class="clear"></div>
    </div>
    <!--stop bottomheader-->
    <div id="content"><!--start content-->
        <div class="container container_16"><!--start container_16-->
            ${wrappedContent}
            <div class='clear'></div>
        </div>
        <!--stop container_16-->
        <div class="clear"></div>
    </div>
    <!--stop content-->


    <div id="bottomfooter"><!--start bottomfooter-->
        <div class="container container_16">

            <div class="grid_16">
          <p class="copyright">
          <span>COPYRIGHT (C) 2009 <a href="http://www.jahia.com/">Jahia Solutions Group</a></span>|
          <span><a href="http://www.jahia.com/jahia/Jahia/Home/products/jahia_editions/licenses">Licence</a></span>|   
          <span><a href="http://www.jahia.org/forum">COMMUNITY FORUMS</a></span>
          <span><a href="http://www.jahia.com/jahia/Jahia/Home/services/annual_developer_subscription">COMMERCIAL SUPPORT</a></span>
      </p>
                <template:module path="${rootPage.path}/footer" autoCreateType="jnt:contentList"/>
            </div>

            <div class='clear'></div>

        </div>

        <div class="clear"></div>
    </div>
    <!--stop bottomfooter-->


    <div class="clear"></div>
</div>
<!--stop bodywrapper-->