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
${fn:escapeXml(currentNode.properties.text.string)}

<p>
<jsp:include page="dynamically_included.jsp">
    <jsp:param name="useNodeNameAsTitle" value="true"/>
</jsp:include>
</p>

<p>
<%@include file="text.bundle.jspf"%>
</p>

<p>
<jsp:include page="/modules/default/6.7.0.0-SNAPSHOT/jnt_text/html/text.jsp">
    <jsp:param name="useNodeNameAsTitle" value="true"/>
</jsp:include>
</p>

<template:addResources type="css" resources="test.css" />

- rendered from OSGi Jahia Module