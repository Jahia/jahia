<%@page import="org.jahia.registries.ServicesRegistry"%>
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

<%@page import="org.jahia.services.usermanager.JahiaUser"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
<div>
    <img src="${pageContext.request.contextPath}/icons/jnt_groupsFolder_large.png" alt=" " style="float: left" />
    <h2>
    <fmt:message key="label.group"/>:&nbsp;${currentNode.displayableName}
    </h2>
    <jcr:nodeProperty node="${currentNode}" name="jcr:description" />
    <ul>
    <c:forEach items="${currentNode.nodes}" var="child">
        <c:if test="${jcr:isNodeType(child, 'jnt:members')}">
        <li>
            <h3><fmt:message key="label.members"/></h3>
            <ul>
            <c:forEach items="${child.nodes}" var="subchild">
                <li>
                    <jcr:nodeProperty node="${subchild}" name="j:member" var="memberRef"/>
                    <c:set var="member" value="${memberRef.node}"/>
                    <div>
                        <c:if test="${jcr:isNodeType(member, 'jnt:group')}" var="isGroup">
                            <img src="${pageContext.request.contextPath}/icons/jnt_groupsFolder_large.png" alt=" " style="float: left" />
                        </c:if>
                        <c:if test="${!isGroup}">
                            <img src="${pageContext.request.contextPath}/icons/jnt_user_large.png" alt=" " style="float: left" />
                        </c:if>
                        <jcr:nodeProperty node="${member}" name="jcr:title" var="title"/>
                        <c:set var="params" value=""/>
                        <a href="<c:url value='${url.base}${member.path}.html${params}'/>"><strong>
                        <c:if test="${not empty title}">
                            ${title.string}&nbsp(${member.name})
                        </c:if>
                        <c:if test="${empty title}">
                            ${member.name}
                        </c:if>
                        </strong></a><br/>
                        <jcr:nodeProperty node="${member}" name="jcr:description" />
                    </div><br style="clear: both"/>
                </li>
            </c:forEach>
            </ul>
        </li>
        </c:if>
    </c:forEach>
    </ul>
</div>