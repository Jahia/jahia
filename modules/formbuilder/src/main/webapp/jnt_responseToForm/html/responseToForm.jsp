<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery.validate.js,jquery.maskedinput-1.2.2.js"/>
<template:addResources type="css" resources="formbuilder.css"/>
<jcr:node var="fieldsetsNode" path="${currentNode.parent.parent.path}/fieldsets"/>
<div>
    <c:forEach items="${fieldsetsNode.nodes}" var="fieldset">
        <c:forEach
                items="${jcr:getPropertiesAsStringFromNodeNameOfThatType(currentNode,fieldset,'jnt:formElement')}"
                var="entry">
            <jcr:node var="def" path="${fieldset.path}/${entry.key}"/>
            <c:if test="${jcr:isNodeType(def, 'jnt:automaticList')}" var="isAutomaticList">
                <jcr:nodeProperty node="${def}" name="type" var="type"/>
                <c:set var="renderers" value="${fn:split(type.string,'=')}"/>
                <c:if test="${fn:length(renderers) > 1}"><c:set var="renderer" value="${renderers[1]}"/></c:if>
                <c:if test="${not (fn:length(renderers) > 1)}"><c:set var="renderer" value=""/></c:if>
                <p><label>${entry.key}</label>&nbsp;<span>Value:<jcr:nodePropertyRenderer node="${currentNode}"
                                                                                          name="${entry.key}"
                                                                                          renderer="${renderer}"/></span>
                </p>
            </c:if>
            <c:if test="${not isAutomaticList}">
                <p>
                    <label>${entry.key}</label>&nbsp;<span>Value : ${entry.value}</span>
                </p>
            </c:if>
        </c:forEach>
        <c:forEach items="${currentNode.nodes}" var="subResponseNode">
            <template:module node="${subResponseNode}" template="default"/>
        </c:forEach>
    </c:forEach>
</div>