<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<div>
	<fmt:message key="tags"/>:&nbsp;<template:module node="${jcr:getParentOfType(currentNode, 'jnt:page')}" forcedTemplate="hidden.tags"/>
	<template:module node="${jcr:getParentOfType(currentNode, 'jnt:page')}" forcedTemplate="hidden.addTag"/>
</div>