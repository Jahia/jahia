<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.

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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="utils" uri="http://www.jahia.org/tags/utilityLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="javascript" resources="jquery.js,jquery-ui.min.js"/>
<template:addResources type="javascript" resources="timepicker.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript" resources="ckeditor/ckeditor.js,ckeditor/adapters/jquery.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript"
                       resources="jquery.treeview.min.js,jquery.treeview.async.jahia.js,jquery.fancybox.js"/>
<template:addResources type="javascript" resources="jquery.defer.js"/>
<template:addResources type="javascript" resources="treeselector.js"/>
<template:addResources type="css" resources="timepicker.css,datepicker.css,jquery.treeview.css,jquery.fancybox.css,contentlist.css,formcontribute.css"/>
<template:addResources type="javascript" resources="jquery.jeditable.treeItemSelector.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.UILocale}.js"/>
<utils:setBundle basename="JahiaContributeMode" useUILocale="true"/>
<%@include file="../../include/contributeCKEditorToolbar.jspf" %>
<div id="${currentNode.UUID}">
    <template:include templateType="html" view="hidden.header"/>
    <c:set var="animatedTasks" value=""/>
    <c:set var="animatedWFs" value=""/>
    <table width="100%" cellspacing="0" cellpadding="5" border="0" class="table">
        <thead>
        <tr>
            <th width="1%">&nbsp;</th>
            <th width="5%">
                <c:if test="${jcr:isNodeType(currentNode.parent,'jnt:contentFolder') || jcr:isNodeType(currentNode.parent,'jnt:folder')}">
                    <a title="parent" href="<c:url value='${url.base}${currentNode.parent.path}.html'/>"><img height="16" width="16"
                                                                                             border="0"
                                                                                             style="cursor: pointer;"
                                                                                             title="parent" alt="parent"
                                                                                             src="<c:url value='${url.templatesPath}/default/images/icons/folder_up.png'/>"></a>
                </c:if>
            </th>
            <th width="35%"><fmt:message key="label.title"/></th>
            <th width="15%" style="white-space: nowrap;"><fmt:message key="mix_created.jcr_created"/></th>
            <th width="15%" style="white-space: nowrap;"><fmt:message key="mix_lastModified.jcr_lastModified"/></th>
            <th width="15%"><fmt:message key="jmix_lastPublished.j_lastPublished"/></th>
            <th width="15%" style="white-space: nowrap;" class="lastCol"><fmt:message key="label.workflow"/></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${moduleMap.currentList}" var="child" begin="${moduleMap.begin}" end="${moduleMap.end}"
                   varStatus="status">
            <tr class="${status.count % 2 == 0 ? 'even' : 'odd'}">
                <td align="center">
                    <input type="checkbox" name="${child.identifier}" ${child.locked ? 'disabled=true':''}/>
                </td>
                <td>
                    <jcr:icon var="icon" node="${child}"/>
                    <img src="<c:url value='${url.templatesPath}/${icon}_large.png'/>"/>
                </td>
                <td>
                    <c:if test="${child.locked}">
                        <img height="16" width="16" border="0" style="cursor: pointer;" title="Locked" alt="Supprimer"
                             src="<c:url value='${url.templatesPath}/default/images/icons/locked.gif'/>">
                    </c:if>

                    <c:if test="${jcr:isNodeType(child, 'jnt:contentFolder')}">
                        <a href="<c:url value='${url.base}${child.path}.html'/>">
                            ${fn:escapeXml(!empty child.propertiesAsString['jcr:title'] ? child.propertiesAsString['jcr:title'] : child.name)}
                        </a>
                    </c:if>
                    <c:if test="${not jcr:isNodeType(child, 'jnt:contentFolder')}">
                        <a href="<c:url value='${url.base}${child.path}.editContent.html'/>">
                            ${fn:escapeXml(!empty child.propertiesAsString['jcr:title'] ? child.propertiesAsString['jcr:title'] : child.name)}
                        </a>
                    </c:if>
                </td>
                <td>
                    <fmt:formatDate value="${child.properties['jcr:created'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
                </td>
                <td>
                    <fmt:formatDate value="${child.properties['jcr:lastModified'].date.time}"
                                    pattern="yyyy-MM-dd HH:mm"/>
                </td>
                <td>
                    <fmt:formatDate value="${child.properties['j:lastPublished'].date.time}"
                                    pattern="yyyy-MM-dd HH:mm"/>
                </td>
                <td class="lastCol">
                    <workflow:activeWorkflow node="${child}" var="wfs"/>
                    <c:forEach items="${wfs}" var="wf">
                        ${fn:escapeXml(wf.workflowDefinition.displayName)}
                    </c:forEach>
                </td>
                <%--<td>--%>
                    <%--<%@include file="../../jnt_contentList/edit/workflow.jspf" %>--%>
                <%--</td>--%>
                <%--<td>--%>
                    <%--<c:if test="${child.locked}">--%>
                        <%--<img height="16" width="16" border="0" style="cursor: pointer;" title="Locked" alt="Locked"--%>
                             <%--src="${url.currentModule}/images/icons/locked.gif">--%>
                    <%--</c:if>--%>
                <%--</td>--%>
                <%--<td class="lastCol">--%>
                    <%--<%@include file="../../jnt_contentList/edit/edit.jspf" %>--%>
                <%--</td>--%>
            </tr>
        </c:forEach>
        </tbody>
    </table>
	<c:if test="${moduleMap.editable and renderContext.editMode}">
		<template:module path="*"/>
	</c:if>

    <template:include templateType="html" view="hidden.footer"/>
</div>


<c:if test="${not renderContext.ajaxRequest and jcr:hasPermission(currentNode, 'jcr:addChildNodes_default')}">
    <%@include file="../../jnt_contentList/html/addcontent.jspf" %>
</c:if>
