<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<template:addResources type="css" resources="wiki.css"/>

<template:addWrapper name="wrapper.wiki"/>
<div id="two"><!--start tab two-->
    <form action="${currentNode.name}" method="post">
        <input type="hidden" name="autoCheckin" value="true">
        <input type="hidden" name="nodeType" value="jnt:wikiPage">
        <textarea class="textareawiki" name="text" rows="30" cols="85">
${currentNode.properties['text'].string}
</textarea>
       <p>
        <fmt:message key="jnt_wiki.addComment"/> : <input name="lastComment" />
    <input class="button" type="submit"/>
		</p>
    </form>

</div>
<!--stop tabtwo-->
