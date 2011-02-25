<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<template:addResources type="css" resources="wiki.css"/>
<template:addResources type="css" resources="wiki.css"/>
<template:addResources type="css" resources="markitupStyle.css"/>
<template:addResources type="css" resources="markitupWikiStyle.css"/>
<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="javascript" resources="jquery.markitup.js"/>
<template:addResources type="javascript" resources="markitupWikiSet.js"/>

<h2>${currentNode.properties["jcr:title"].string}</h2>
<template:tokenizedForm>
<form name="formWiki" class="formWiki" action="${currentNode.name}" method="post">
    <input type="hidden" name="autoCheckin" value="true">
    <input type="hidden" name="nodeType" value="jnt:wikiPage">
    <script type="text/javascript">
            $(document).ready(function() {
                // Add markItUp! to your textarea in one line
                // $('textarea').markItUp( { Settings }, { OptionalExtraSettings } );
                $('#text-${currentNode.identifier}').markItUp(mySettings);

            });
    </script>
    <textarea class="textareawiki" name="wikiContent" id="text-${currentNode.identifier}" rows="30" cols="85">${currentNode.properties['wikiContent'].string}</textarea>

    <p>
        <label><fmt:message key="jnt_wiki.addComment"/>: </label> <input name="lastComment" value=" "/>
    </p>
    <input class="button" type="submit"/>
</form>
</template:tokenizedForm>

