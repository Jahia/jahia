<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<c:if test="${renderContext.editMode and renderContext.mainResource.contextConfiguration eq 'page'}">
    <internal:gwtInit modules="org.jahia.ajax.gwt.module.edit.Edit"/>
    <internal:gwtGenerateDictionary/>
</c:if>
<template:includeResources invertCss="true"/>
