<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="portal.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui-personalized-1.6rc2.min.js,inettuts.js"/>
<jcr:node path="/shared/portalComponents" var="widgets"/>
<script>
    function addWidget(source, newName) {
        var data = {};
        data["source"] = source;
        data["target"] = "${currentNode.path}/column1";
        data["newName"] = newName;
        $.post("${url.base}${currentNode.path}/column1.clone.do", data, function(result) {
            alert("widget has been added to your portal page");
        }, "json");
    }
</script>
<ul>
    <c:forEach items="${widgets.children}" var="node" varStatus="status">
        <li>
            <div onclick="addWidget('${node.path}','${node.name}')">
                <h3><jcr:nodeProperty node="${node}" name="jcr:title" var="title"/><c:if
                        test="${not empty title}">${title.string}</c:if><c:if test="${empty title}">${node.name}</c:if></h3>
            </div>
        </li>
    </c:forEach>
</ul>