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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType" scope="application"/>
<fieldset>
    <legend><strong>${fn:escapeXml(currentNode.name)}</strong></legend>
    <c:if test="${not empty currentNode.parent}">
        <c:url var="urlValue" value="${currentNode.parent.path}.raw?${pageContext.request.queryString}" context="${url.base}"/>
        <a href="${urlValue}">[..]</a>
    </c:if>
    <p>
        <strong>Path :&nbsp;</strong>${fn:escapeXml(currentNode.path)}<br/>
        <strong>ID :&nbsp;</strong>${fn:escapeXml(currentNode.identifier)}<br/>
        <strong>Types :&nbsp;</strong>${fn:escapeXml(currentNode.nodeTypes)}<br/>
        <strong>Mixins :&nbsp;</strong><c:forEach items="${currentNode.mixinNodeTypes}" var="mixin">${fn:escapeXml(mixin.name)}</c:forEach>
        <c:if test="${jcr:isNodeType(currentNode, 'nt:file')}">
            <br/><strong>File:&nbsp;</strong><a href="<c:url value="${currentNode.url}" context='/'/>">download</a>
        </c:if>
    </p>
    <p><strong>Properties:&nbsp;</strong><a href="?showProperties=${not param.showProperties}&amp;showNodes=${param.showNodes}">${param.showProperties ? 'hide' : 'show'}</a></p>
    <c:if test="${param.showProperties}">
        <ul>
            <c:if test="${functions:length(currentNode.properties) == 0}"><li>No properties present</li></c:if>
            <c:forEach items="${currentNode.properties}" var="property">
                <li><strong>${fn:escapeXml(property.name)}:&nbsp;</strong>
                    <c:if test="${property.multiple}" var="multiple">
                        <ul>
                            <c:if test="${empty property.values}">
                                <li>[]</li>
                            </c:if>
                            <c:forEach items="${property.values}" var="value">
                                <li><%@include file="value.jspf" %></li>
                            </c:forEach>
                        </ul>
                    </c:if>
                    <c:if test="${!multiple}">
                        <c:set var="value" value="${property.value}"/>
                        <%@include file="value.jspf" %>
                    </c:if>
                </li>
            </c:forEach>
        </ul>
    </c:if>
    <p><strong>Child nodes:&nbsp;</strong><a href="?showProperties=${param.showProperties}&amp;showNodes=${not param.showNodes}">${param.showNodes ? 'hide' : 'show'}</a></p>
    <c:if test="${param.showNodes}">
        <ul>
            <c:if test="${functions:length(currentNode.nodes) == 0}"><li>No child nodes present</li></c:if>
            <c:forEach items="${currentNode.nodes}" var="child">
                <c:url var="urlValue" value="${child.path}.raw?${pageContext.request.queryString}" context="${url.base}"/>
                <li><a href="${urlValue}">${fn:escapeXml(child.name)}</a> - types : ${fn:escapeXml(child.nodeTypes)}</li>
            </c:forEach>
        </ul>
    </c:if>
</fieldset>