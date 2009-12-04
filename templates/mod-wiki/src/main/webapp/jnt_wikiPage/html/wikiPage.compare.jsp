<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<template:addWrapper name="wikiWrapper"/>

<div id="three"><!--start tab two-->
    <c:if test="${not empty param['diff'] and not empty param['oldid']}">
    <jcr:nodeVersion var="diff" node="${currentNode}" versionName="${param['diff']}"/>
    <jcr:nodeVersion var="oldid" node="${currentNode}" versionName="${param['oldid']}"/>

    <utility:textDiff oldText="${oldid.frozenNode.properties['text'].string}" newText="${diff.frozenNode.properties['text'].string}"/>
    </c:if>
    <c:if test="${empty param['diff'] or empty param['oldid']}">
    Please select two versions to compare
    </c:if>
</div>
<!--stop tabtwo-->
