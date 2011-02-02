<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<template:addResources type="css" resources="wiki.css"/>
<template:tokenizedForm>
<form class="formWiki" action="${currentNode.name}" method="post">
    <input type="hidden" name="autoCheckin" value="true">
    <input type="hidden" name="nodeType" value="jnt:wikiPage">
    <textarea class="textareawiki" name="text" rows="30" cols="85">${currentNode.properties['text'].string}</textarea>

    <p>
        <label><fmt:message key="jnt_wiki.addComment"/>: </label> <input name="lastComment"/>
    </p>
    <input class="button" type="submit"/>
</form>
</template:tokenizedForm>

