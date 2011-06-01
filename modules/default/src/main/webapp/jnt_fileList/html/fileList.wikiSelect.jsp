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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>

<c:set var="targetNodePath" value="${renderContext.mainResource.node.path}"/>
<c:if test="${!empty param.targetNodePath}">
    <c:set var="targetNodePath" value="${param.targetNodePath}"/>
</c:if>
<c:if test="${!empty currentNode.properties.folder}">
    <c:set var="targetNodePath" value="${currentNode.properties.folder.node.path}"/>
</c:if>
<script type="text/javascript">
    function insertImgSyntax(content) {
        document.formWiki.wikiContent.value += content;
    }
</script>
<div id="fileList${currentNode.identifier}">
    <template:addResources type="css" resources="fileList.css"/>
    <jcr:node var="targetNode" path="${targetNodePath}"/>
    <ul class="filesList">
        <li><fmt:message key="label.dblClickToAddInWiki"/></li>
        <c:forEach items="${targetNode.nodes}" var="subchild">
            <c:if test="${jcr:isNodeType(subchild, 'jnt:file')}">
                <li>
                    <c:if test="${fn:startsWith(subchild.fileContent.contentType,'image/')}">
                        <div onclick="return false;" ondblclick="insertImgSyntax('\n [[image:${subchild.url}||width=${subchild.properties["j:width"].string} height=${subchild.properties["j:height"].string}]]')">
                            <img name="" width="100" src="${subchild.url}" alt="${fn:escapeXml(subchild.name)}" onmousedown="return false;" />
                                ${fn:escapeXml(not empty title.string ? title.string : subchild.name)}
                        </div>
                    </c:if>
                    <c:if test="${!fn:startsWith(subchild.fileContent.contentType,'image/')}">
                        <div onclick="return false;" ondblclick="insertImgSyntax('\n[[${fn:escapeXml(not empty title.string ? title.string : subchild.name)}>>${subchild.url}]]')">
                            <a href="${subchild.url}" title="${fn:escapeXml(subchild.name)}" onmousedown="return false;" >
                                    ${fn:escapeXml(not empty title.string ? title.string : subchild.name)}
                            </a>
                        </div>
                    </c:if>
                    <c:if test="${jcr:hasPermission(subchild,'jcr:removeNode')}">
                        <c:url var="urlNodePath" value="${url.base}${currentNode.path}.html.ajax">
                            <c:param name="targetNodePath" value="${targetNode.path}"/>
                        </c:url>
                        <form action="<c:url value='${url.base}${subchild.path}'/>" method="post"
                              id="jahia-wiki-item-delete-${subchild.UUID}">
                            <input type="hidden" name="jcrMethodToCall" value="delete"/>
                            <button><fmt:message key="label.delete"/></button>
                            <script type="text/javascript">
                                $(document).ready(function() {
                                    // bind 'myForm' and provide a simple callback function
                                    var options = {
                                        success: function() {
                                            $('#fileList${currentNode.identifier}').load('${urlNodePath}');
                                        }
                                    }
                                    $('#jahia-wiki-item-delete-${subchild.UUID}').ajaxForm(options);
                                });
                            </script>
                        </form>
                    </c:if>
                </li>
            </c:if>
        </c:forEach>
    </ul>
</div>
<template:addCacheDependency path="${targetNodePath}"/>
