<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="portal.css"/>
<template:addResources type="javascript" resources="jquery.js,ui.core.js,inettuts.js"/>
<jcr:node path="/shared/portalComponents" var="widgets"/>
<script type="text/javascript">
function addWidget(source, newName) {
    var data = {};
    data["source"] = source;
    data["target"] = "${currentNode.path}/column1";
    data["newName"] = newName;
    $.post("${url.base}${currentNode.path}/column1.clone.do", data, function(data) {
        alert("widget has been added to your portal page");
    });
}
function addRSSWidget() {
    var data = {};
    data["nodeType"] = "jnt:rss";
    data["url"] = $("#feedUrl").val();
    data["nbEntries"] = $("#nbFeeds").val();
    $.post("${url.base}${currentNode.path}/column1/*", data, function(data) {
        alert("rss widget has been added to your portal page");
    });
}
</script>
<div class="content clearfix">
<div class="left">
<h3>Corporate Portal</h3>
<p class="grey">Jahia offers the ability to place portlets or social gadgets on any page of your site as easily as if you were adding a piece of text or a picture. Thanks to its built-in Mashup Center, empowered end users can manage, categorize or instantiate the hundreds of possible micro-applications through a unified and centralized interface, regardless of the underlying technology.</p>

</div>
<div class="left">
<h3>Add Portal Components</h3>
<ul class="panellist">
    <c:forEach items="${widgets.children}" var="node" varStatus="status">
        <li>
            <div onclick="addWidget('${node.path}','${node.name}')">
                <span><jcr:nodeProperty node="${node}" name="jcr:title" var="title"/><c:if
                        test="${not empty title}">${title.string}</c:if><c:if test="${empty title}">${node.name}</c:if></span>
            </div>
        </li>
    </c:forEach>
</ul>
</div>
<div class="left right">
	<h3>Add RSS</h3>
        <form  class="Form" action="" method="post">
            <p>
            <label>Rss feed URL :</label>
            <input type="text" name="feedUrl" id="feedUrl" maxlength="256"/>
            </p>
            <p>
            <label>Number of feeds :</label>
            <input type="text" name="nbFeeds" id="nbFeeds" maxlength="2" value="5"/>
            </p>
        </form>
        <button name="addRss" type="button" value="Add Rss" onclick="addRSSWidget()">Add Rss Widget</button>

</div>
</div>