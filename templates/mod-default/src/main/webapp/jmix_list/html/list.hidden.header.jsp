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
<c:if test="${not ommitFormatting}"><div id="${currentNode.UUID}"></c:if>
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
        <c:if test="${jcr:isNodeType(currentNode, 'jmix:orderedList')}">
            <query:definition var="listQuery" qomBeanName="listQuery" scope="request" >
                <c:forTokens var="prefix" items="first,second,third" delims=",">
                    <jcr:nodeProperty node="${currentNode}" name="${prefix}Field" var="sortPropertyName"/>
                    <c:if test="${!empty sortPropertyName}">
                        <jcr:nodeProperty node="${currentNode}" name="${prefix}Direction" var="order"/>
                        <query:sortBy propertyName="${sortPropertyName.string}" order="${order.string}"/>
                    </c:if>
                </c:forTokens>
            </query:definition>
        </c:if>
        <jcr:jqom var="result" qomBeanName="listQuery" scope="request"/>

        <%-- pager specific --%>
        <c:set var="end" value="${functions:length(result.nodes)}" scope="request"/>
        <c:set var="listTotalSize" value="${end}" scope="request"/>

        <%-- set result --%>
        <c:set value="${result.nodes}" var="currentList" scope="request"/>
    </c:if>

    <c:if test="${empty editable}">
        <c:set var="editable" value="false"/>
    </c:if>
    <c:if test="${not empty paginationActive}">
        <template:option node="${currentNode}" nodetype="jmix:pager" template="hidden.init"/>
    </c:if>
