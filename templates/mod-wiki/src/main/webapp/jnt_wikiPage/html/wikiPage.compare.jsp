<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<template:addWrapper name="wikiWrapper"/>

<div id="three"><!--start tab two-->
    <jcr:nodeVersion var="diff" node="${currentNode}" versionName="${param['diff']}"/>
    <jcr:nodeVersion var="oldid" node="${currentNode}" versionName="${param['oldid']}"/>

    <utility:textDiff oldText="${oldid.frozenNode.properties['text'].string}" newText="${diff.frozenNode.properties['text'].string}"/>

</div>
<!--stop tabtwo-->
