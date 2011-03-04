<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent and jcr:isNodeType(bindedComponent, 'jmix:list')}">
    <template:option node="${bindedComponent}" nodetype="${bindedComponent.primaryNodeTypeName},jmix:list" template="hidden.header"/>
    <c:choose>
        <c:when test="${not empty param.pagesize}">
            <c:set var="pageSize" value="${param.pagesize}"/>
        </c:when>
        <c:when test="${not empty param.src_itemsPerPage}">
            <c:set var="pageSize" value="${param.src_itemsPerPage}"/>
        </c:when>
        <c:otherwise>
            <c:set var="pageSize" value="${currentNode.properties['pageSize'].long}"/>
        </c:otherwise>
    </c:choose>
    <template:initPager totalSize="${moduleMap.listTotalSize}" pageSize="${pageSize}" id="${bindedComponent.identifier}"/>
    <c:if test="${currentNode.properties.displayPager.boolean}">
        <template:displayPagination/>
    </c:if>
</c:if>
