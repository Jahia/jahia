<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<template:addWrapper name="wikiWrapper"/>

<div id="three"><!--start tab two-->
    <jcr:nodeVersion var="diff" node="${currentNode}" versionName="${param['diff']}"/>
    <jcr:nodeVersion var="oldid" node="${currentNode}" versionName="${param['oldid']}"/>

    <div>
         ${param['diff']} : ${diff.frozenNode.properties['lastComment'].string}
    </div>
    <div>
        ${diff.frozenNode.properties['text'].string}
    </div>
    <div>---></div>
    <div>
        ${param['oldid']} : ${oldid.frozenNode.properties['lastComment'].string}
    </div>
    <div>
        ${oldid.frozenNode.properties['text'].string}
    </div>
</div>
<!--stop tabtwo-->
