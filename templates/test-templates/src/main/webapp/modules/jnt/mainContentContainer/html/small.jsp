<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<p>
title=<template:field name='mainContentTitle'/><br/>
URL: <a href="<%= request.getContextPath() %>/render/default${currentNode.path}.small.html">${currentNode.name}.small.html</a>
</p>
