<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>

<template:module node="${currentNode.properties['j:node'].node}" editable="false" view="${currentNode.properties['j:referenceView'].string}">
    <template:param name="refTitle" value="${currentNode.properties['jcr:title'].string}"/>
</template:module>