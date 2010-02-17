<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="portal.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui-personalized-1.6rc2.min.js,inettuts.js"/>
<jcr:node path="/shared/portalComponents" var="widgets"/>
<ul>
    <c:forEach items="${widgets.children}" var="node" varStatus="status">
        <li>
            <div onclick="addWidget('${node.path}','${node.name}')">
                <h3><jcr:nodeProperty node="${node}" name="jcr:title" var="title"/><c:if
                        test="${not empty title}">${title.string}</c:if><c:if test="${empty title}">${node.name}</c:if></h3>
            </div>
        </li>
    </c:forEach>
    <li>
        <form action="" method="post">
            <label>Rss feed URL :</label>
            <input type="text" name="feedUrl" id="feedUrl" maxlength="256"/>
            <label>Number of feeds :</label>
            <input type="text" name="nbFeeds" id="nbFeeds" maxlength="2" value="5"/>
        </form>
        <button name="addRss" type="button" value="Add Rss" onclick="addRSSWidget()">Add Rss Widget</button>
    </li>
</ul>