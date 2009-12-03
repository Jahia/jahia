<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<template:addWrapper name="wikiWrapper"/>
<div id="two"><!--start tab two-->
<jcr:node var="content" path="${currentNode.path}/content"/>
<textarea name="content" rows="10" cols="80">
${content.properties.text.string}
</textarea>

</div>
<!--stop tabtwo-->
