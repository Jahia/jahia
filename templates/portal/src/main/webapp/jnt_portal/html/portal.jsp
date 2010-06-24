<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<template:addResources type="css" resources="portal.css,slide.css"/>
<template:addResources type="inlinejavascript">
       var baseUrl = '${url.base}';
</template:addResources>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,inettuts.js"/>

<script type="text/javascript">
$(document).ready(function(){
	$(".btn-slide").click(function(){
		$(document).ready(function() {
            $.get('${url.base}${currentNode.path}.select.html',null,function(data) {
                $("#selectWidgetsArea").html(data);
            });
        });
		$("#panel").slideToggle("slow");
		$(this).toggleClass("active"); return false; 
		
	});
});
</script><!--refresh needed on class="btn-slide active" window.location='${url.base}${currentNode.path}.html';-->

<c:if test="${!renderContext.editMode}">

<div id="panel">
	<div id="selectWidgetsArea"></div>
</div>
            

<p class="slide"><a href="#" class="btn-slide">Add Widget</a></p>




</c:if>

<div id="columns">
<c:forEach var="column" begin="1" end="${currentNode.properties.columns.string}">
    <ul id="column${column}" class="column">
            <template:area path="column${column}" template="portal" forcedTemplate="portal" />
    </ul>
</c:forEach>
</div>
<script type="text/javascript">
    iNettuts.addWidgetControls();
    iNettuts.makeSortable();
</script>


