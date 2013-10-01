<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<template:tokenizedForm>
    <c:url value='${url.base}${currentNode.path}' var="url"/>
    <form id="taskDataForm_${currentNode.parent.identifier}" method="post" action="${url}">
        <input type="hidden" name="jcrMethodToCall" value="put"/>
        <div>
            Title : <input size="50" type="title" name="jcr:title"
                           value="${currentNode.properties['jcr:title'].string}"/>
        </div>
    </form>
</template:tokenizedForm>
