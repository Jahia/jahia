<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="portal.css"/>
<template:addResources type="javascript" resources="jquery.js,jquery-ui.min.js,inettuts.js"/>
<jcr:node path="${param['path']}" var="widgets"/>
<div class="content clearfix">
    <div class="left">
        <h3>Add Components</h3>
        <ul class="panellist">
            <c:forEach items="${widgets.nodes}" var="node" varStatus="status">
                <li>
                    <div onclick="addWidget('${node.path}','${node.name}')">
                        <span>${node.displayableName}</span>
                    </div>
                </li>
            </c:forEach>
        </ul>
    </div>
        <div class="left">
        <h3>Add RSS</h3>
        <form class="Form" action="" method="post">
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
    <div class="left right">

        <h3>Add Script Gadget</h3>

        <form class="Form" action="" method="post">
            <p>
                <label>Script :</label>
                <input type="text" name="scriptGadget" id="scriptGadget"/>
            </p>
        </form>
        <button name="addRss" type="button" value="Add Rss" onclick="addScriptWidget()">Add Script Widget</button>

    </div>
</div>