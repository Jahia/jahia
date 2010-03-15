<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<jcr:node var="fieldsetsNode" path="${currentNode.path}/fieldsets"/>
<c:forEach items="${fieldsetsNode.nodes}" var="fieldset" varStatus="fieldsetStatus">
	<c:forEach items="${jcr:getChildrenOfType(fieldset, 'jnt:formElement')}" var="field" varStatus="fieldStatus">
		${fieldsetStatus.index + fieldStatus.index > 0 ? ',' : ''}${field.name}
	</c:forEach>
</c:forEach><%= System.getProperty("line.separator") %>
<template:area path="${currentNode.path}/responses" nodeTypes="jnt:responseToForm" editable="false" forceCreation="false" templateType="csv"/>