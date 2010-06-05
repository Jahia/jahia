<%@ page import="org.apache.jackrabbit.commons.json.JsonParser" %>
<%@ page import="org.json.JSONObject" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
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
    <c:remove var="listQuery" scope="request"/>
    <c:remove var="currentList" scope="request"/>
    <c:choose>
        <c:when test="${jcr:isNodeType(currentNode, 'jmix:pager')}">
            <c:set scope="request" var="paginationActive" value="true"/>
        </c:when>
        <c:otherwise>
            <c:set var="begin" value="0" scope="request"/>
        </c:otherwise>
    </c:choose>
    <template:include template="hidden.load" />

    <c:if test="${empty currentList and not empty listQuery}">
        <%-- move that to jmix:orderedList hidden.init tpl --%>
        <%--<c:if test="${jcr:isNodeType(currentNode, 'jmix:orderedList')}">--%>
            <%--<query:definition var="listQuery" qomBeanName="listQuery" scope="request" >--%>
                <%--<c:forTokens var="prefix" items="first,second,third" delims=",">--%>
                    <%--<jcr:nodeProperty node="${currentNode}" name="${prefix}Field" var="sortPropertyName"/>--%>
                    <%--<c:if test="${!empty sortPropertyName}">--%>
                        <%--<jcr:nodeProperty node="${currentNode}" name="${prefix}Direction" var="order"/>--%>
                        <%--<query:sortBy propertyName="${sortPropertyName.string}" order="${order.string}"/>--%>
                    <%--</c:if>--%>
                <%--</c:forTokens>--%>
            <%--</query:definition>--%>
        <%--</c:if>--%>
        <c:set var="renderOptions" value="before" />
        <c:if test="${jcr:isNodeType(currentNode, 'jmix:facets')}">
            <query:definition var="listQuery" qomBeanName="listQuery" scope="request" >
                <jcr:nodeProperty node="${currentNode}" name="facets" var="facets"/>
                <c:forEach items="${facets}" var="facet">
                    <query:column columnName="rep:facet(nodetype=${fn:substringBefore(facet.string, ';')}&column=${fn:substringAfter(facet.string, ';')}&facet.mincount=1)" propertyName="${fn:substringAfter(facet.string, ';')}"/>
                </c:forEach>
            </query:definition>
            <c:set var="renderOptions" value="before" scope="request"/>
        </c:if>
        <c:if test="${!empty param.delPropName}">
            <c:remove var="activeFacetsMapTmp" scope="request"/>
            <jsp:useBean id="activeFacetsMapTmp" class="java.util.HashMap" scope="request"/>
            <c:forEach items="${activeFacetsMap}" var="facet">
                <c:if test="${!(facet.key eq param.delPropName)}">
                    <c:set target="${activeFacetsMapTmp}" property="${facet.key}" value="${facet.value}"/>
                </c:if>
            </c:forEach>
            <c:remove var="activeFacetsMap" scope="session"/>
            <c:set var="activeFacetsMap" value="${activeFacetsMapTmp}" scope="session"/>
        </c:if>
        <c:if test="${!empty param.propName }">
            <c:if test="${empty activeFacetsMap}">
                <jsp:useBean id="activeFacetsMap" class="java.util.HashMap" scope="session"/>
            </c:if>
            <c:set target="${activeFacetsMap}" property="${param.propName}" value="${param.propValue}"/>
        </c:if>
        <query:definition var="listQuery" qomBeanName="listQuery" scope="request" >
            <c:forEach items="${activeFacetsMap}" var="facet">
                <query:equalTo value="${facet.value}" propertyName="${facet.key}"/>
            </c:forEach>
        </query:definition>
        <jcr:jqom var="result" qomBeanName="listQuery" scope="request"/>

        <%-- pager specific --%>
        <c:set var="end" value="${functions:length(result.nodes)}" scope="request"/>
        <c:set var="listTotalSize" value="${end}" scope="request"/>

        <%-- set result --%>
        <c:set value="${result.nodes}" var="currentList" scope="request"/>
    </c:if>

    <c:if test="${jcr:isNodeType(currentNode, 'jmix:orderedList')}">
        <jcr:sort list="${currentList}" properties="${currentNode.properties.firstField.string},${currentNode.properties.firstDirection.string},${currentNode.properties.secondField.string},${currentNode.properties.secondDirection.string},${currentNode.properties.thirdField.string},${currentNode.properties.thirdDirection.string}" var="currentList" scope="request"/>
    </c:if>

    <c:if test="${not empty param.filter}">
        <jcr:filter var="currentList" list="${currentList}" properties="${param.filter}" node="${currentNode}" scope="request"/>
    </c:if>

    <c:if test="${empty editable}">
        <c:set var="editable" value="false"/>
    </c:if>
    <c:if test="${not empty paginationActive}">
        <template:option node="${currentNode}" nodetype="jmix:pager" template="hidden.init"/>
    </c:if>

<c:if test="${renderContext.editMode && empty currentList}">
    <p><fmt:message key="search.results.no.results"/></p>
</c:if>
