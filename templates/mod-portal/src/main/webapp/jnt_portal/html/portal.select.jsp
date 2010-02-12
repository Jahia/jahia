<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="portal.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui-personalized-1.6rc2.min.js,inettuts.js"/>
<jcr:node path="/shared/portalComponents" var="widgets"/>
<script>
    function addWidget(id) {
        var data = {};
        data["nodeType"] = "jnt:nodeReference";
        data["j:node"] = id;
        data['methodToCall'] = 'post';
        $.post("${url.base}${currentNode.path}/column1/*", data, function(result) {
            alert("widget has been added to your portal page");
        }, "json");
    }
</script>
<ul>
    <c:forEach items="${widgets.children}" var="node" varStatus="status">
        <li>
            <div onclick="addWidget('${node.identifier}')">
                <h3><jcr:nodeProperty node="${node}" name="jcr:title"/></h3>
            </div>
        </li>
    </c:forEach>
</ul>