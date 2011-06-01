<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<template:addResources type="css" resources="portlet-spec-1.0.css,portlets.css"/>
<jcr:nodeProperty node="${currentNode}" name="j:node" var="Portletnode"/>
<c:if test="${!empty Portletnode}">
    <ui:portletModes node="${Portletnode.node}"/>
    <ui:portletRender portletNode="${Portletnode.node}" />
</c:if>