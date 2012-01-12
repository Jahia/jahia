<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
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
<template:addResources type="css" resources="contentinfo.css"/>

<c:set var="boundComponent" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<div class="contentinfos">
    <h3><fmt:message key="contentInformation"/></h3>
    <jcr:nodeProperty node="${boundComponent}" name="j:defaultCategory" var="assignedCategories"/>
    <c:set var="separator" value="${functions:default(currentResource.moduleParams.separator, ', ')}"/>
    <jsp:useBean id="filteredCategories" class="java.util.LinkedHashMap"/>
    <c:set var="props" value="${currentNode.properties}"/>
        <dl>
            <c:if test="${props.creationdate.boolean}">
                    <dt><fmt:message key="mix_created.jcr_created"/></dt>
                    <dd><fmt:formatDate value="${boundComponent.properties['jcr:created'].time}"
                                        pattern="dd, MMMM yyyy HH:mm"/></dd>
            </c:if>
            <c:if test="${props.creator.boolean}">
                    <dt><fmt:message key="mix_created.jcr_createdBy"/></dt>
                    <dd>${boundComponent.properties['jcr:createdBy'].string}</dd>
            </c:if>
            <c:if test="${props.lastmodification.boolean}">
                    <dt><fmt:message key="mix_lastModified.jcr_lastModified"/></dt>
                    <dd><fmt:formatDate value="${boundComponent.properties['jcr:lastModified'].time}"
                                        pattern="dd, MMMM yyyy HH:mm"/></dd>
            </c:if>
            <c:if test="${props.lastcontributor.boolean}">
                    <dt><fmt:message key="mix_lastModified.jcr_lastModifiedBy"/></dt>
                    <dd>${boundComponent.properties['jcr:lastModifiedBy'].string}</dd>
            </c:if>
            <c:if test="${props.description.boolean}">
                    <dt><fmt:message key="mix_title.jcr_description"/></dt>
                    <c:if test="${not empty boundComponent.properties['jcr:description']}">
                            <dd>${boundComponent.properties['jcr:description'].string}  </dd>
                    </c:if>
            </c:if>
            <c:if test="${props.keywords.boolean}">
                    <dt><fmt:message key="jmix_keywords.j_keywords"/></dt>
                    <c:if test="${not empty boundComponent.properties['j:keywords']}">
                        <dd>
                        <c:forEach items="${boundComponent.properties['j:keywords']}" var="keyword">
                                ${keyword.string}
                            </c:forEach>
                        </dd>
                    </c:if>
            </c:if>
            <c:if test="${props.categories.boolean}">
                    <dt><fmt:message key="jmix_categorized.j_defaultCategory"/></dt>
                        <c:forEach items="${assignedCategories}" var="category" varStatus="status">
                            <dd>
                            <c:if test="${not empty category.node}">
                                <c:set target="${filteredCategories}" property="${category.node.properties['jcr:title'].string}"
                                       value="${category.node.properties['jcr:title'].string}"/>
                            </c:if>
                        </c:forEach>
                            </dd>
                        <c:choose>
                            <c:when test="${not empty filteredCategories}">
                                <c:forEach items="${filteredCategories}" var="category" varStatus="status">
                                    ${fn:escapeXml(category.value)}${!status.last ? separator : ''}
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <fmt:message key="label.categories.noCategory"/>
                            </c:otherwise>
                        </c:choose>
            </c:if>
        </dl>
</div>

