<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<template:addResources type="css" resources="portlet-spec-1.0.css,portlets.css"/>
<jcr:nodeProperty node="${currentNode}" name="j:node" var="Portletnode"/>
<ui:portletModes node="${Portletnode.node}"/>
<ui:portletRender portletNode="${Portletnode.node}" />