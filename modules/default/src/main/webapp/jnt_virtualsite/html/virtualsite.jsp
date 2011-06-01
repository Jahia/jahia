<%@ page import="org.jahia.services.content.nodetypes.NodeTypeRegistry" %>
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

<%@ page import="javax.jcr.nodetype.NodeTypeIterator" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="org.jahia.data.templates.JahiaTemplatesPackage" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<c:if test="${currentNode.parent.name eq 'sites'}">
    <h1>Site: ${currentNode.name}</h1>

    <p>Title: <jcr:nodeProperty node="${currentNode}" name="j:title"/></p>

    <p>Server name: <jcr:nodeProperty node="${currentNode}" name="j:serverName"/></p>

    <p>Description: <jcr:nodeProperty node="${currentNode}" name="j:description"/></p>

    <p>Nodes:</p>
    <ul>
        <c:forEach var="child" items="${currentNode.nodes}">
            <li><a href="<c:url value='${url.base}${child.path}.html'/>">${child.name}</a></li>
        </c:forEach>
    </ul>
</c:if>
<c:if test="${currentNode.parent.name eq 'templateSets'}">
    <c:if test="${currentNode.properties['j:siteType'].string eq 'module'}">
        <h1>Module : ${currentNode.name}</h1>
    </c:if>
    <c:if test="${currentNode.properties['j:siteType'].string eq 'templatesSet'}">
        <h1>Templates Set : ${currentNode.name}</h1>
    </c:if>
    <jcr:node path="${currentNode.path}/templates/files/template.jpg" var="thumbnail"/>

    <p>
        <c:if test="${not empty thumbnail.url}">

            <img id ="themePreview" src="${thumbnail.url}"
                 width="270" height="141" alt="">

        </c:if>
        <c:if test="${empty thumbnail.url}">
            <img src="<c:url value='/engines/images/pictureNotAvailable.jpg'/>" width="200" height="200" alt="<fmt:message key='org.jahia.admin.site.ManageSites.NoTemplatePreview.label'/>" title="<fmt:message key='org.jahia.admin.site.ManageSites.NoTemplatePreview.label'/>"/>

        </c:if>
    </p>

    <p>Title: <jcr:nodeProperty node="${currentNode}" name="j:title"/></p>

    <p>Description: <jcr:nodeProperty node="${currentNode}" name="j:description"/></p>

    <jcr:jqom statement="select * from [jnt:template] as template where ISDESCENDANTNODE(template,'${currentNode.path}')" var="templates"/>

    <c:if test="${templates.nodes.size > 0}">
        <p>Template:</p>
        <ul>
            <c:forEach items="${templates.nodes}" var="template">
                <li>
                    <a href="<c:url value='${url.base}${template.path}'/>">${template.name}</a>
                </li>
            </c:forEach>
        </ul>
    </c:if>

    <jcr:jqom statement="select * from [jnt:page] as page where ISDESCENDANTNODE(page,'${currentNode.path}')" var="pages"/>
    <c:if test="${pages.nodes.size > 0}">
        <p>Prepackaged pages:</p>
        <ul>
            <c:forEach items="${pages.nodes}" var="page">
                <li>
                    <a href="<c:url value='${url.base}${page.path}'/>">${page.name}</a>
                </li>
            </c:forEach>
        </ul>
    </c:if>


    <%
        JCRNodeWrapper currentNode = (JCRNodeWrapper) pageContext.findAttribute("currentNode");
        JahiaTemplatesPackage pack = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(currentNode.getName());
        NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes(pack.getName());
        pageContext.setAttribute("nodeTypes",nti);
    %>
    <c:if test="${nodeTypes.size > 0}">
        <p>Nodetype:</p>
        <ul>
            <c:forEach items="${nodeTypes}" var="nodeType">
                <li>
                    <jcr:icon var="icon" type="${nodeType}"/>
                    <img src="<c:url value='${url.templatesPath}/${icon}.png'/>"/>
                        ${jcr:label(nodeType,currentResource.locale)}
                </li>
            </c:forEach>
        </ul>
    </c:if>
</c:if>
