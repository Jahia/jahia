<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<template:addWrapper name="wrapper.wiki"/>
<div id="two"><!--start tab two-->

    <form method="post" action="${currentNode.name}/${param['newPageName']}">
        <input type="hidden" name="autoCheckin" value="true">
        <input type="hidden" name="nodeType" value="jnt:wikiPage">
        <input type="hidden" name="jcr:title" value="${param['newPageName']}">
        <textarea name="text" rows="30" cols="85">
        <fmt:message key="jnt_wiki.typeContentHere"/>
        </textarea>
        <p>
        <fmt:message key="jnt_wiki.addComment"/> : <input name="lastComment" />
        <input type="submit"/>
		</p>
    </form>

</div>
<!--stop tabtwo-->
