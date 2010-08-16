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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="forum.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.cuteTime.js"/>
<%-- Get all contents --%>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="content" var="content"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
<c:if test="${currentNode.propertiesAsString['jcr:createdBy'] == renderContext.user.name}">
    <form action="${url.base}${currentNode.path}" method="post"
          id="jahia-forum-post-delete-${currentNode.UUID}">
        <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
            <%-- Define the output format for the newly created node by default html or by redirectTo--%>
        <input type="hidden" name="newNodeOutputFormat" value="html"/>
        <input type="hidden" name="methodToCall" value="delete"/>
    </form>
</c:if>
<template:option node="${currentNode}" template="hidden.plusone_minorone_form" nodetype="jmix:rating"/>
<span class="forum-corners-top"><span></span></span>
<div class="forum-postbody">
    <ul class="forum-profile-icons">
        <c:if test="${renderContext.user.name != 'guest'}">
            <li class="forum-report-icon"><a title="<fmt:message key='report.post'/>" href="#"><span><fmt:message key='report.post'/></span></a></li>
            <li class="forum-quote-icon">
                <a title="<fmt:message key='reply.quote'/>" href="#threadPost"
                   onclick="jahiaForumQuote('jahia-forum-thread-${currentNode.parent.UUID}', '${fn:escapeXml(functions:escapeJavaScript(content.string))}');"><span><fmt:message key='reply.quote'/></span></a>
            </li>
            <li><template:option node="${currentNode}" template="hidden.plusone_minorone" nodetype="jmix:rating"/></li>
        </c:if>
        <c:if test="${currentNode.propertiesAsString['jcr:createdBy'] == renderContext.user.name}">
            <li class="delete-post-icon"><a title="<fmt:message key='delete.post'/>" href="#"
                                            onclick="document.getElementById('jahia-forum-post-delete-${currentNode.UUID}').submit();"><span><fmt:message key="delete.post"/></span></a>
            </li>
            <li class="edit-post-icon"><a title="<fmt:message key="edit.post"/>" href="#"><span><fmt:message key="edit.post"/></span></a></li>
        </c:if>

    </ul>

    <h4 class="forum-h4-first">${title.string}</h4>

    <p class="forum-author"><fmt:message key="by"/><strong>&nbsp;<a
            href="${url.base}${renderContext.site.path}/users/${createdBy.string}.html">${createdBy.string}</a></strong>&nbsp;&raquo;&nbsp;<span class="timestamp"><fmt:formatDate
            value="${created.time}" pattern="yyyy/MM/dd HH:mm"/></span></p>

    <div class="content">${content.string}</div>
</div>
<jcr:sql var="numberOfPostsQuery"
         sql="select [jcr:uuid] from [jnt:post] as p  where p.[jcr:createdBy] = '${createdBy.string}'"/>
<c:set var="numberOfPosts" value="${functions:length(numberOfPostsQuery.rows)}"/>
<dl class="forum-postprofile">
    <dt>
        <jcr:node var="userNode" path="/users/${createdBy.string}"/>
        <template:module node="${userNode}" template="mini"/>
    </dt>
    <br/>
    <dd><strong><fmt:message key="number.of.posts"/></strong>&nbsp;${numberOfPosts}</dd>
    <dd><strong><fmt:message key="registration.date"/></strong> <jcr:nodeProperty node="${userNode}" name="jcr:lastModified"
                                                   var="userCreated"/><fmt:formatDate value="${userCreated.time}"
                                                                                      type="date" dateStyle="medium"/>
    </dd>
</dl>
<div class="back2top"><a title="Top" class="top" href="#wrap">Top</a></div>
<div class="clear"></div>
<span class="forum-corners-bottom"><span></span></span>
