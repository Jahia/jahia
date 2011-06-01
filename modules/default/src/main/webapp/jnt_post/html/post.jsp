<%@ page contentType="text/html; UTF-8" %>
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

<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${currentNode}" name="content" var="content"/>
<c:if test="${createdBy.string ne ' guest '}">
    <jcr:node var="userNode" path="${functions:lookupUser(createdBy.string).localPath}"/>
    <c:forEach items="${userNode.properties}" var="property">
        <c:if test="${property.name == 'j:firstName'}"><c:set var="firstname" value="${property.string}"/></c:if>
        <c:if test="${property.name == 'j:lastName'}"><c:set var="lastname" value="${property.string}"/></c:if>
        <c:if test="${property.name == 'j:email'}"><c:set var="email" value="${property.string}"/></c:if>
        <c:if test="${property.name == 'j:title'}"><c:set var="title" value="${property.string}"/></c:if>
    </c:forEach>
</c:if>
<li class="genericListCommentLi">
    <div class="image">
        <div class="itemImage itemImageLeft">

            <jcr:nodeProperty var="picture" node="${userNode}" name="j:picture"/>
            <c:if test="${not empty picture}"><img
                    src="${picture.node.thumbnailUrls['avatar_60']}"
                    alt="${title} ${firstname} ${lastname}"
                    width="60"
                    height="60"/>
            </c:if>
            <c:if test="${empty picture}"><img alt="" src="<c:url value='${url.currentModule}/images/userbig.png'/>"/></c:if>
        </div>
    </div>

    <h5 class="commentTitle"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h5>
    <jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/>
    <span class="docspacedate timestamp"><fmt:formatDate value="${lastModified.time}"
                                                         pattern="yyyy/MM/dd HH:mm"/></span>

    <p>
        <span class="author">
            <c:if test="${createdBy.string ne 'guest'}">
                <a href="<c:url value='${url.base}${functions:lookupUser(createdBy.string).localPath}.html'/>">${createdBy.string}</a></c:if>
            <c:if test="${createdBy.string eq 'guest'}">${fn:escapeXml(currentNode.properties.pseudo.string)}</c:if>:&nbsp;</span>
        ${fn:escapeXml(content.string)}
    </p>

    <div class='clear'></div>
</li>