<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<template:addWrapper name="wrapper.wiki"/>
<div id="two"><!--start tab two-->
    <form action="${currentNode.name}" method="post">
        <input type="hidden" name="autoCheckin" value="true">
        <input type="hidden" name="nodeType" value="jnt:wikiPage">
        <textarea name="text" rows="10" cols="80">
${currentNode.properties['text'].string}
</textarea>
        Comment : <input name="lastComment" />
    <input type="submit"/>

    </form>

</div>
<!--stop tabtwo-->
