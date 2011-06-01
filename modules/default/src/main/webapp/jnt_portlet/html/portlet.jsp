<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<template:addResources type="css" resources="portlet-spec-1.0.css,portlets.css"/>
<ui:portletModes node="${currentNode}"/>
<ui:portletRender portletNode="${currentNode}" />