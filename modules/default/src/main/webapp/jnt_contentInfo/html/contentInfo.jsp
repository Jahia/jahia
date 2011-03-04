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

<c:set var="linked" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<div class="contentinfos">
    <h3><fmt:message key="contentInformation"/></h3>
        <dl>
            <dt><fmt:message key="mix_created.jcr_createdBy"/></dt>
            <dd>${linked.properties['jcr:createdBy'].string}</dd>
            <dt><fmt:message key="mix_created.jcr_created"/></dt>
            <dd><fmt:formatDate value="${linked.properties['jcr:created'].date.time}" pattern="yyyy-MM-dd HH:mm"/></dd>
            <dt><fmt:message key="mix_lastModified.jcr_lastModifiedBy"/></dt>
            <dd>${linked.properties['jcr:lastModifiedBy'].string}</dd>
            <dt><fmt:message key="mix_lastModified.jcr_lastModified"/></dt>
            <dd><fmt:formatDate value="${linked.properties['jcr:lastModified'].date.time}" pattern="yyyy-MM-dd HH:mm"/></dd>
        </dl>
</div>

