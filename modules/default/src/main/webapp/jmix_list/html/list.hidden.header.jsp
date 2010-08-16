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
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:if test="${not omitFormatting}"><div id="${currentNode.UUID}"></c:if>
    <c:choose>
        <c:when test="${not empty subNodesTemplate}">
            <c:set target="${moduleMap}" property="subNodesTemplate" value="${subNodesTemplate}"/>
            <c:remove var="subNodesTemplate" scope="request" />
        </c:when>
        <c:when test="${jcr:isNodeType(currentNode, 'jmix:renderableList')}">
            <c:set target="${moduleMap}" property="subNodesTemplate" value="${currentNode.properties['j:subNodesTemplate'].string}"/>
        </c:when>
    </c:choose>
    <c:choose>
        <c:when test="${jcr:isNodeType(currentNode, 'jmix:pager')}">
            <c:set target="${moduleMap}" property="paginationActive" value="true"/>
        </c:when>
        <c:otherwise>
            <c:set target="${moduleMap}" property="begin" value="0" />
        </c:otherwise>
    </c:choose>
    <template:include template="hidden.load" />
    <c:if test="${empty moduleMap.currentList and not empty moduleMap.listQuerySql}">
        <jcr:sql var="result" sql="${moduleMap.listQuerySql}"/>
        <c:set target="${moduleMap}" property="currentList" value="${result.nodes}" />
        <c:set target="${moduleMap}" property="end" value="${functions:length(result.nodes)}" />
        <c:set target="${moduleMap}" property="listTotalSize" value="${moduleMap.end}" />
    </c:if>
    <c:set var="facetParamVarName" value="N-${currentNode.name}"/>
    <c:set var="activeFacetMapVarName" value="afm-${currentNode.name}"/>
    <c:if test="${not empty param[facetParamVarName] and empty activeFacetsVars[facetParamVarName]}">
        <c:if test="${activeFacetsVars == null}">
           <jsp:useBean id="activeFacetsVars" class="java.util.HashMap" scope="request"/>
        </c:if>
        <c:set target="${activeFacetsVars}" property="${facetParamVarName}" value="${facet:decodeFacetUrlParam(param[facetParamVarName])}"/>
        <c:set target="${activeFacetsVars}" property="${activeFacetMapVarName}" value="${facet:getAppliedFacetFilters(activeFacetsVars[facetParamVarName])}"/>
    </c:if>
    <c:if test="${empty moduleMap.currentList and not empty moduleMap.listQuery}">
        <query:definition var="listQuery" qom="${moduleMap.listQuery}" >
            <c:forEach items="${activeFacetsVars[activeFacetMapVarName]}" var="facet">
                <c:forEach items="${facet.value}" var="facetValue">
                    <query:fullTextSearch propertyName="rep:filter(${jcr:escapeIllegalJcrChars(facet.key)})" searchExpression="${facetValue.value}"/>
                </c:forEach>
            </c:forEach>
        </query:definition>
        <jcr:jqom var="result" qomBeanName="listQuery" />

        <%-- pager specific --%>
        <c:set target="${moduleMap}" property="end" value="${functions:length(result.nodes)}" />
        <c:set target="${moduleMap}" property="listTotalSize" value="${moduleMap.end}" />

        <%-- set result --%>
        <c:set target="${moduleMap}" property="currentList" value="${result.nodes}"/>
    </c:if>

    <c:if test="${jcr:isNodeType(currentNode, 'jmix:orderedList')}">
        <jcr:sort list="${moduleMap.currentList}" properties="${currentNode.properties.firstField.string},${currentNode.properties.firstDirection.string},${currentNode.properties.secondField.string},${currentNode.properties.secondDirection.string},${currentNode.properties.thirdField.string},${currentNode.properties.thirdDirection.string}" var="currentList" />
        <c:set value="${currentList}" target="${moduleMap}" property="currentList" />
    </c:if>

    <c:if test="${not empty param.filter}">
        <jcr:filter var="currentList" list="${moduleMap.currentList}" properties="${param.filter}" node="${currentNode}" />
        <c:set value="${currentList}" target="${moduleMap}" property="currentList" />
    </c:if>

    <c:if test="${empty moduleMap.editable}">
        <c:set target="${moduleMap}" property="editable" value="false" />
    </c:if>

    <c:if test="${not empty moduleMap.paginationActive}">
        <template:option node="${currentNode}" nodetype="jmix:pager" template="hidden.init"/>
    </c:if>

<c:if test="${renderContext.editMode && empty moduleMap.currentList}">
    <p><fmt:message key="search.results.no.results"/></p>
</c:if>