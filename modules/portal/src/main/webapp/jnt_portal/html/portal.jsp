<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

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
<template:addResources type="inlinejavascript">
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
</template:addResources>
<script type="text/javascript">
$(document).ready(function(){
	$(".btn-slide").click(function(){
		$(document).ready(function() {
            $.get('${url.base}${currentNode.path}.select.html.ajax',null,function(data) {
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
            <template:area path="column${column}" template="portal" />
    </ul>
</c:forEach>
</div>
<script type="text/javascript">
    iNettuts.addWidgetControls();
    iNettuts.makeSortable();
</script>

