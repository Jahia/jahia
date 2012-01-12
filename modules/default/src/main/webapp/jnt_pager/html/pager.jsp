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
<c:set var="boundComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty boundComponent and jcr:isNodeType(boundComponent, 'jmix:list')}">
    <template:addCacheDependency node="${boundComponent}"/>
    <template:option node="${boundComponent}" nodetype="${boundComponent.primaryNodeTypeName},jmix:list" view="hidden.header"/>
    <c:set var="pagesizeid" value="pagesize${boundComponent.identifier}"/>
    <c:choose>
        <c:when test="${not empty param[pagesizeid]}">
            <c:set var="pageSize" value="${param[pagesizeid]}"/>
        </c:when>
        <c:when test="${not empty param.src_itemsPerPage}">
            <c:set var="pageSize" value="${param.src_itemsPerPage}"/>
        </c:when>
        <c:otherwise>
            <c:set var="pageSize" value="${currentNode.properties['pageSize'].long}"/>
        </c:otherwise>
    </c:choose>
    <template:initPager totalSize="${moduleMap.listTotalSize}" pageSize="${pageSize}" id="${boundComponent.identifier}"/>
    <c:if test="${currentNode.properties.displayPager.boolean}">
        <template:displayPagination id="${boundComponent.identifier}" nbOfPages="${currentNode.properties.nbOfPages.string}" displayNumberOfItemsPerPage="${currentNode.properties.displayNbOfItemsPerPage.boolean}"/>
    </c:if>
</c:if>
