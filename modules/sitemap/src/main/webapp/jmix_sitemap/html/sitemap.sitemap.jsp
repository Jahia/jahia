<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="option" type="org.jahia.services.content.nodetypes.initializers.ChoiceListValue"--%>
<c:set target="${renderContext}" property="contentType" value="text/html;charset=UTF-8"/>
<template:addResources type="css" resources="slickmap.css"/>


<c:if test="${empty currentResource.moduleParams.level}">
    <c:set var="level" value="1"/>
</c:if>

<c:if test="${not empty currentResource.moduleParams.level}">
    <c:set var="level" value="${currentResource.moduleParams.level}"/>
</c:if>


<c:if test="${level eq 1}"><div class="sitemap"><ul><li></c:if>
<c:if test="${level > 1}">
    <li>
</c:if>
<a href='<c:url value="${url.base}${currentNode.path}.html"/>'>${currentNode.displayableName}</a>
<c:forEach items="${jcr:getChildrenOfType(currentNode,'jmix:sitemap')}" var="child" varStatus="childStatus">
    <c:if test="${childStatus.first}">
        <ul <c:if test="${level eq 1}"><c:set var="nbSubItems" value="${jcr:getChildrenOfType(currentNode,'jmix:sitemap')}"/> id="primaryNav" class="col${fn:length(nbSubItems)}"</c:if>>
    </c:if>
    <template:module node="${child}" view="sitemap" editable="false">
        <template:param name="level" value="${level +1}"/>
    </template:module>
    <c:if test="${childStatus.last}">
        </ul>
    </c:if>
</c:forEach>
<c:if test="${level > 1}">
    </li>
</c:if>
<c:if test="${level eq 1}"></li></ul></div></c:if>
<c:set var="level" value="${level - 1}" scope="request"/>
