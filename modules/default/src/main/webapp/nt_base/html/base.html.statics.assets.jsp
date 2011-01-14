<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
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
<c:if test="${renderContext.editMode and renderContext.mainResource.contextConfiguration eq 'page'}">
    <internal:gwtInit modules="org.jahia.ajax.gwt.module.edit.Edit"/>
    <internal:gwtGenerateDictionary/>
</c:if>
<c:if test="${renderContext.contributionMode and renderContext.mainResource.contextConfiguration eq 'page'}">
<script type="text/javascript">
var jahiaGWTParameters={contextPath:"${url.context}",uilang:"${renderContext.mainResourceLocale}",siteUuid:"${renderContext.site.identifier}",wcag:${renderContext.site.WCAGComplianceCheckEnabled}}
</script>
</c:if>
<template:includeResources />
