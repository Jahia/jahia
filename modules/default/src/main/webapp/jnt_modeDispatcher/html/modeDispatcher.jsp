<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
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
<template:addResources type="javascript" resources="jquery.min.js"/>
<div id="item-${currentNode.identifier}">
    <c:if test="${renderContext.editMode}">
        Loaded from ${currentNode.properties.mode.string} :
	<c:forEach items="${jcr:getChildrenOfType(currentNode,'jmix:droppableContent')}" var="child">
                  <template:module node="${child}" />
                </c:forEach>
      <template:module path="*" />
    </c:if>
    <c:if test="${not renderContext.editMode}">
      <c:choose>
          <c:when test="${renderContext.liveMode and currentNode.properties.mode.string eq 'live'
                            or renderContext.contributionMode and currentNode.properties.mode.string eq 'contribute'
                            or renderContext.previewMode and currentNode.properties.mode.string eq 'preview'}">
              <c:set var="modeDispatcherId" value="item-${currentNode.identifier}" scope="request"/>cssddcdcs
              <c:forEach items="${jcr:getChildrenOfType(currentNode,'jmix:droppableContent')}" var="child">
                  <template:module node="${child}" />
              </c:forEach>
            </c:when>
            <c:otherwise>
                <script type="text/javascript">
                    <c:if test="${currentNode.properties.mode.string eq 'live'}">
                    $('#item-${currentNode.identifier}').load('<c:url value="${url.baseLive}${currentNode.path}.html.ajax"/>');
                    </c:if>
                    <c:if test="${currentNode.properties.mode.string eq 'contribute'}">
                    $('#item-${currentNode.identifier}').load('<c:url value="${url.baseContribute}${currentNode.path}.html.ajax"/>');
                    </c:if>
                    <c:if test="${currentNode.properties.mode.string eq 'preview'}">
                    $('#item-${currentNode.identifier}').load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax"/>');
                    </c:if>
                </script>
            </c:otherwise>
        </c:choose>
    </c:if>
</div>
