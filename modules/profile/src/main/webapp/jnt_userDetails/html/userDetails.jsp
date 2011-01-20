<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<c:set var="user" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<c:if test="${renderContext.editMode}">
    <fmt:message key="${fn:replace(currentNode.primaryNodeTypeName,':','_')}"/>
    <template:linker property="j:bindedComponent"/>
</c:if>

<c:if test="${not empty user}">

<%--<c:if test="${not jcr:isNodeType(user, 'jnt:user')}">--%>
<%--<jcr:node var="user" path="/users/${user.properties['jcr:createdBy'].string}"/>--%>
<%--</c:if>--%>
<c:if test="${not jcr:isNodeType(user, 'jnt:user')}">
    <jcr:node var="user" path="/users/${renderContext.user.username}"/>
</c:if>

<template:addResources type="css" resources="userProfile.css"/>
<c:set var="fields" value="${user.propertiesAsString}"/>
<jcr:nodePropertyRenderer node="${user}" name="j:title" renderer="resourceBundle" var="title"/>
<c:if test="${not empty title and not empty fields['j:firstName'] and not empty fields['j:lastName']}">
    <c:set var="person" value="${title.displayName} ${fields['j:firstName']} ${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and not empty fields['j:firstName'] and not empty fields['j:lastName']}">
    <c:set var="person" value="${fields['j:firstName']} ${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and empty fields['j:firstName'] and not empty fields['j:lastName']}">
    <c:set var="person" value="${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and not empty fields['j:firstName'] and empty fields['j:lastName']}">
    <c:set var="person" value="${fields['j:firstName']}"/>
</c:if>
<c:if test="${empty title and empty fields['j:firstName'] and empty fields['j:lastName']}">
    <c:set var="person" value=""/>
</c:if>

<h3 class="boxtitleh3" id="personDisplay2"><c:out value="${person}"/></h3>

</c:if>
