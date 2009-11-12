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
<template:addResources type="css" resources="web.css" nodetype="jnt:page"/>
<jcr:node var="rootPage" path="/content/sites/${renderContext.site.siteKey}/home" scope="request"/>

<div id="bodywrapper"><!--start bodywrapper-->
    <div id="header"><!--start header-->
        <div class="utilities">
            <div class="content">
                <a name="pagetop"></a>
                <span class="breadcrumbs">
                    <fmt:message key='youAreHere'/>
                    <c:set var="currentPath" value=""/>
                    <c:forTokens items="${currentNode.path}" delims="/" var="itemPath" varStatus="status">
                        <c:set var="currentPath" value="${currentPath}/${itemPath}"/>
                        <jcr:node var="node" path="${currentPath}"/>
                        <c:if test="${jcr:isNodeType(node, 'jnt:page')}">
                        <c:if test="${not status.last}"><a href="${url.base}${currentPath}.html"></c:if>${node.propertiesAsString['jcr:title']}<c:if test="${not status.last}"></a> /</c:if>
                        </c:if>
                    </c:forTokens>
                </span>
                <!--stop pagepath-->                     
                <ui:languageSwitchingLinks display="horizontal" linkDisplay="flag" displayLanguageState="true"/>
                <template:include page="common/breadcrumbs.jsp" cache="false"/>
            </div>
        </div>
    </div>
    <div id="pagecontent">
        <div class="content2cols">
            <div id="columnA">
                <template:include page="common/columnA.jsp"/>
            </div>
            <div id="columnB">
                <h2><c:out value="${requestScope.currentPage.highLightDiffTitle}"/></h2>
                
                ${wrappedContent}
                                
                <div>
                    <a class="bottomanchor" href="#pagetop"><fmt:message key='pageTop'/></a>
                </div>                            
            </div>
            <br class="clear"/>
        </div>
    </div>        
</div>
<!--stop bodywrapper-->