<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<jcr:nodeProperty node="${currentNode}" name="j:node" var="reference"/>
<jcr:nodeProperty node="${currentNode}" name="j:referenceView" var="referenceTemplate"/>
<template:module node="${reference.node}" editable="false" template="${referenceTemplate.string}">
    <template:param name="refTitle" value="${currentNode.properties['jcr:title'].string}"/>
</template:module>