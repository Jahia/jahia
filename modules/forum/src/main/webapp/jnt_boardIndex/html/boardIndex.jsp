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
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="forum.css"/>
<jcr:sql var="numberOfPostsQuery"
         sql="select * from [jnt:post] as post  where isdescendantnode(post, ['${currentNode.path}']) order by post.[jcr:lastModified] desc"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.nodes.size}"/>
<c:forEach items="${numberOfPostsQuery.nodes}" var="node" varStatus="status" end="2">
    <c:if test="${status.first}">
        <c:set value="${node}" var="lastModifiedNode"/>
        <jcr:nodeProperty node="${node}" name="jcr:lastModified" var="lastModified"/>
        <jcr:nodeProperty node="${lastModifiedNode}" name="jcr:createdBy" var="createdBy"/>
    </c:if>
</c:forEach>
<div id="forum-body">
    <div class="forum-box forum-box-style1 room">
        <span class="forum-corners-top"><span></span></span>
        <ul class="forum-list forums">
            <li class="row">
                <dl>
                    <dt title="posts">
                        <a class="forum-title" href="${url.base}${currentNode.path}.detail.html"><jcr:nodeProperty
                                node="${currentNode}" name="boardSubject"/></a><br/>
                        <div class="boardDescription"><jcr:nodeProperty node="${currentNode}" name="boardDescription"/></div>
                        
                        </dt>
                    <dd class="topics">${functions:length(currentNode.nodes)}<dfn>Topics</dfn></dd>
                    <dd class="posts">${numberOfPosts} <dfn>Posts</dfn></dd>
                    <dd class="lastpost"><c:if test="${numberOfPosts > 0}">
                        <span>
					<dfn>Last post</dfn> by <a href="${url.base}${lastModifiedNode.parent.path}.html">
                            <img height="9" width="11" title="View the latest post" alt="View the latest post"
                                 src="/jahia/modules/jahia_forum/img/icon_topic_latest.gif"/>${createdBy.string}
                        </a><br/><fmt:formatDate value="${lastModified.time}" dateStyle="full" type="both"/></span>
                    </c:if></dd>
                </dl>
            </li>
        </ul>
        <div class="clear"></div>
        <span class="forum-corners-bottom"><span></span></span>
    </div>
</div>