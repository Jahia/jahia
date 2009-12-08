<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addWrapper name="wrapper.wiki"/>
<div id="two"><!--start tab two-->

    <form method="post" action="${currentNode.name}/${param['newPageName']}">
        <input type="hidden" name="autoCheckin" value="true">
        <input type="hidden" name="nodeType" value="jnt:wikiPage"> 
        <input type="hidden" name="jcr:title" value="${param['newPageName']}"> 
<textarea name="text" rows="10" cols="80">
type content here
</textarea>
        Comment : <input name="lastComment" />
        <input type="submit"/>
    </form>

</div>
<!--stop tabtwo-->
