<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addWrapper name="wikiWrapper"/>
<div id="two"><!--start tab two-->
    <form method="post" action="${currentNode.name}.newWikiPage.do">
        <input type="hidden" name="link" value="${param['link']}" />

<textarea name="content" rows="10" cols="80">
type content here
</textarea>

        <input type="submit"/>
    </form>
</div>
<!--stop tabtwo-->
