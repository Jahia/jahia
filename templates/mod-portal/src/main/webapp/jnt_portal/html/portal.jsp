<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<template:addResources type="css" resources="portal.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui-personalized-1.6rc2.min.js,inettuts.js"/>
<div id="columns">
<c:forEach var="column" begin="1" end="${currentNode.properties.columns.string}">
    <ul id="column${column}" class="column">
            <template:area path="column${column}" template="portal" forcedTemplate="portal" forceCreation="true"/>
    </ul>
</c:forEach>
</div>
<div id="widgets">
    <template:module node="${currentNode}" forcedTemplate="select"/>
</div>
<script type="text/javascript">
    iNettuts.addWidgetControls();
    iNettuts.makeSortable();
</script>
