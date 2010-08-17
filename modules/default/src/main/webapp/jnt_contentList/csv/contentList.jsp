<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:forEach items="${currentNode.nodes}" var="subchild">
<template:module node="${subchild}" editable="false"/><%= System.getProperty("line.separator") %> 
</c:forEach>