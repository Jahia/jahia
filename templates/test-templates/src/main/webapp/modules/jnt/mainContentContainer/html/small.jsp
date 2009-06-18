<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
title=<template:field name='mainContentTitle'/>
URL: <a href="<%= request.getContextPath() %>/render/default${currentNode.path}.small.html"><%= request.getContextPath() %>/render/default/${currentNode.path}.small.html</a>
