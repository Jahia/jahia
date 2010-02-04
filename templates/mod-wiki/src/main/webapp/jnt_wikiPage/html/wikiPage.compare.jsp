<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<template:addResources type="css" resources="wiki.css"/>
<template:addWrapper name="wrapper.wiki"/>

<div id="three"><!--start tab three-->
    <c:if test="${not empty param['diff'] and not empty param['oldid']}">
    <jcr:nodeVersion var="diff" node="${currentNode}" versionName="${param['diff']}"/>
    <jcr:nodeVersion var="oldid" node="${currentNode}" versionName="${param['oldid']}"/>

    <utility:textDiff oldText="${oldid.frozenNode.properties['text'].string}" newText="${diff.frozenNode.properties['text'].string}"/>
    </c:if>
    <c:if test="${empty param['diff'] or empty param['oldid']}">
    <fmt:message key="jnt_wiki.selectTwoVersions"/>
    </c:if>
</div>
<!--stop tab three-->
