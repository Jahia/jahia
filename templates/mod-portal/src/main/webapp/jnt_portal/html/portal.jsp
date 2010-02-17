<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<template:addResources type="css" resources="portal.css,slide.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui-personalized-1.6rc2.min.js,inettuts.js"/>
<script type="text/javascript">
    $(document).ready(function() {

	// Expand Panel
	$("#open").click(function(){
        $(document).ready(function() {
            $.get('${url.base}${currentNode.path}.select.html',{ajaxcall:true},function(data) {
                $("#selectWidgetsArea").html(data);
            });
        });
 		$("div#panel").slideDown("slow");

	});

	// Collapse Panel
	$("#close").click(function(){
		$("div#panel").slideUp("slow");
        window.location='${url.base}${currentNode.path}.html';
	});

	// Switch buttons from "Log In | Register" to "Close Panel" on click
	$("#toggle a").click(function () {
		$("#toggle a").toggle();
	});
    });
</script>
<div id="columns">
<c:forEach var="column" begin="1" end="${currentNode.properties.columns.string}">
    <ul id="column${column}" class="column">
            <template:area path="column${column}" template="portal" forcedTemplate="portal" forceCreation="true"/>
    </ul>
</c:forEach>
</div>


<c:if test="${!renderContext.editMode}">
<div id="toppanel">
    <div id="panel">
        <div class="content clearfix">
            <div id="selectWidgetsArea">
            </div>
        </div>
    </div>
    <div class="tab">
        <ul class="login">
            <li class="left">&nbsp;</li>
            <li id="toggle">
                <a id="open" class="open" href="#">Add Widget</a>
                <a id="close" style="display: none;" class="close" href="#">Close Panel</a>			</li>
            <li class="right">&nbsp;</li>
        </ul>
    </div> <!-- / top -->

</div>
<script type="text/javascript">
    iNettuts.addWidgetControls();
    iNettuts.makeSortable();
</script>
</c:if>