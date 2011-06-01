<%@ page import="org.jahia.utils.FileUtils" %>
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

<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
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
<div id="fileList${currentNode.identifier}">
    <template:addResources type="css" resources="fileList.css"/>
    <jcr:node var="targetNode" path="${targetNodePath}"/>
    <ul class="filesList">
        <c:forEach items="${targetNode.nodes}" var="subchild">
            <c:if test="${jcr:isNodeType(subchild, 'jnt:file')}">
                <li>
                    <jcr:nodeProperty node="${subchild}" name="jcr:title" var="title"/>
                    <c:choose>
                        <c:when test="${fn:startsWith(subchild.fileContent.contentType,'image/')}">
                            <div onclick="return false;" ondblclick="CKEDITOR.instances.editContent.insertHtml('<img src=\'${subchild.url}\'/>')">
                                <img width="100" src="${subchild.url}"  alt="${fn:escapeXml(subchild.name)}" onmousedown="return false;" />
                                    ${fn:escapeXml(not empty title ? title : subchild.name)}
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:set var="title" value="${fn:escapeXml(not empty title.string ? title.string : subchild.name)}"/>
                            <div onclick="return false;" ondblclick="CKEDITOR.instances.editContent.insertHtml('<a href=\'${subchild.url}\' title=\'${title}\'>${title}</a>')">
                                <span class="icon <%=FileUtils.getFileIcon( ((JCRNodeWrapper) pageContext.findAttribute("subchild")).getName()) %>"></span>
                                <a href="${subchild.url}" onmousedown="return false;" title="${title}">${title}</a>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${jcr:hasPermission(subchild,'jcr:removeNode')}">
                        <form action="<c:url value='${url.base}${subchild.path}'/>" method="post"
                              id="jahia-blog-item-delete-${subchild.UUID}">
                            <input type="hidden" name="jcrMethodToCall" value="delete"/>
                            <button><fmt:message key="label.delete"/></button>
                            <script type="text/javascript">
                                <c:url var="urlPath" value="${url.base}${currentNode.path}.html.ajax">
                                    <c:param name="targetNodePath" value="${targetNode.path}"/>
                                </c:url>
                                $(document).ready(function() {
                                    // bind 'myForm' and provide a simple callback function
                                    var options = {
                                        success: function() {
                                            $('#fileList${currentNode.identifier}').load('${urlPath}');
                                            var dataText =CKEDITOR.instances.editContent.getData();
                                            while ((i = dataText.search('${subchild.url}')) > 0 ) {
                                                var before = dataText.substring(0,i);
                                                var after = dataText.substring(i);
                                                dataText = before.substring(0,before.lastIndexOf('<'));
                                                if (after.substring(after.indexOf(">")-1,after.indexOf(">")) == "/") {
                                                    dataText += after.substring(after.indexOf("/>") + 2);
                                                } else {
                                                    dataText += after.substring(after.indexOf("/a>") + 3);
                                                }
                                            }
                                            CKEDITOR.instances.editContent.setData(dataText);
                                        }
                                    }
                                    $('#jahia-blog-item-delete-${subchild.UUID}').ajaxForm(options);
                                });
                            </script>
                        </form>
                    </c:if>
                </li>
            </c:if>
        </c:forEach>
        <li><fmt:message key="label.dblClickToAdd"/></li>
    </ul>
</div>
<template:addCacheDependency path="${targetNodePath}"/>
