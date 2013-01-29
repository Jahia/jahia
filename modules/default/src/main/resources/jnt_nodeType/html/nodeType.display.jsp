<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h4>${currentNode.name}</h4>

<p><fmt:message key="mix_title.jcr_title" />: ${currentNode.properties['jcr:title'].string}</p>
<p><fmt:message key="jnt_nodeType.jcr_description" />: ${currentNode.properties['jcr:description'].string}</p>
<template:include view="details" />

<p>
<fmt:message key="label.engineTab.definitions.properties" />:
<table cellspacing="0" cellpadding="5" border="0">
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
<fmt:message key="label.engineTab.definitions.nodes" />:
<table cellspacing="0" cellpadding="5" border="0">
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
