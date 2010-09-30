<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<template:addResources type="css" resources="portal.css,slide.css"/>
<template:addResources>
    <script type="text/javascript">
        var baseUrl = '${url.base}';
    </script>
</template:addResources>
<template:addResources type="javascript"
                       resources="jquery.min.js,jquery-ui.min.js,inettuts.js,ajaxreplace.js"/>
<template:addResources>
    <script type="text/javascript">
        function addWidget(source, newName) {
            var data = {};
            data["source"] = source;
            data["target"] = "${currentNode.path}/column1";
            data["newName"] = newName;
            $.post("${url.base}${currentNode.path}/column1.clone.do", data, function(data) {
                window.location.reload();
            });
        }
        function addRSSWidget() {
            var data = {};
            data["nodeType"] = "jnt:rss";
            data["url"] = $("#feedUrl").val();
            data["nbEntries"] = $("#nbFeeds").val();
            $.post("${url.base}${currentNode.path}/column1/*", data, function(data) {
                window.location.reload();
            });
        }
    </script>
</template:addResources>
<script type="text/javascript">
    $(document).ready(function() {
        $(".btn-slide").click(function() {
            $(document).ready(function() {
                $.get('${url.base}${currentNode.path}.select.html.ajax', null, function(data) {
                    $("#selectWidgetsArea").html(data);
                });
            });
            $("#panel").slideToggle("slow");
            $(this).toggleClass("active");
            return false;

        });
    });
</script>
<!--refresh needed on class="btn-slide active" window.location='${url.base}${currentNode.path}.html';-->

<c:if test="${!renderContext.editMode}">

    <div id="panel">
        <div id="selectWidgetsArea"></div>
    </div>


    <p class="slide"><a href="#" class="btn-slide">Add Widget</a></p>
</c:if>
<div id="columns">
    <c:forEach var="column" begin="1" end="${currentNode.properties.columns.string}">
        <ul id="column${column}" class="column">
            <template:area path="column${column}" template="portal"/>
        </ul>
    </c:forEach>
</div>
<c:if test="${!renderContext.editMode}">
    <script type="text/javascript">
        iNettuts.addWidgetControls();
        iNettuts.makeSortable();
    </script>
</c:if>
