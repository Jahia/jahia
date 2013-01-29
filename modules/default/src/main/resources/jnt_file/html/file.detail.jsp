<%@ page import="org.jahia.utils.FileUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="files.css"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:description" var="description"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
<fmt:formatDate value="${created.time}" dateStyle="full" var="displayDate"/>
<span class="icon <%=FileUtils.getFileIcon( ((JCRNodeWrapper)pageContext.findAttribute("currentNode")).getName()) %>"></span>
<a href="${currentNode.url}" alt="${fn:escapeXml(not empty refTitle ? refTitle : not empty title.string ? title.string : currentNode.name)}" >
    ${fn:escapeXml(not empty refTitle ? refTitle : not empty title.string ? title.string : currentNode.name)}
</a>
- ${displayDate}
- ${currentNode.fileContent.contentType}
&nbsp;${currentNode.fileContent.contentLength}&nbsp;bytes
