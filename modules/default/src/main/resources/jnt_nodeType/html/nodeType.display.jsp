<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="admin-bootstrap.css,admin-server-settings.css"/>

<div class="page-header">
    <h1>${currentNode.name}</h1>
</div>

<div class="box-1">
<p><strong><fmt:message key="mix_title.jcr_title" /></strong>: ${currentNode.properties['jcr:title'].string}</p>
<p><strong><fmt:message key="jnt_nodeType.jcr_description" /></strong>: ${currentNode.properties['jcr:description'].string}</p>
<template:include view="details" />

<p>
<strong><fmt:message key="label.engineTab.definitions.properties" />:</strong>
<table class="table table-striped table-bordered table-hover">
    <thead>
    <tr>
        <th><fmt:message key="label.name" /></th>
        <th><fmt:message key="jnt_propertyDefinition.j_requiredType" /></th>
        <th><fmt:message key="jnt_propertyDefinition.j_defaultValues" /></th>
        <th><fmt:message key="jnt_propertyDefinition.j_isInternationalized" /></th>
        <th><fmt:message key="jnt_propertyDefinition.j_mandatory" /></th>
        <th><fmt:message key="jnt_propertyDefinition.j_protected" /></th>
        <th><fmt:message key="jnt_propertyDefinition.j_isHidden" /></th>
    </tr>
    </thead>
    <tbody>
        <c:forEach items="${jcr:getChildrenOfType(currentNode, 'jnt:propertyDefinition')}" var="child" varStatus="status">
            <template:module node="${child}" view="tableRow" />
        </c:forEach>
    </tbody>
</table>
</p>

<p>
<strong><fmt:message key="label.engineTab.definitions.nodes" />:</strong>
<table class="table table-striped table-bordered table-hover">
    <thead>
    <tr>
        <th><fmt:message key="label.name" /></th>
        <th><fmt:message key="jnt_childNodeDefinition.j_requiredPrimaryTypes" /></th>
        <th><fmt:message key="jnt_childNodeDefinition.j_mandatory" /></th>
    </tr>
    </thead>
    <tbody>
        <c:forEach items="${jcr:getChildrenOfType(currentNode, 'jnt:childNodeDefinition')}" var="child" varStatus="status">
            <template:module node="${child}" view="tableRow" />
        </c:forEach>
    </tbody>
</table>
</p>
</div>