<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources>
    <script type="text/javascript">
		$(document).ready(sizeFrameContent);
		$(window).resize(sizeFrameContent);
		function sizeFrameContent() {
			$("#toolsFrame").css("height", $("html").height() * 0.96 + "px");
  		}
	</script>
</template:addResources>
<template:addResources>
<style type="text/css">
html, body {height:100%; overflow: hidden;}
</style>
</template:addResources>
<iframe id="toolsFrame" src="<c:url value='/tools/index.jsp'/>" width="100%" style="border:1px solid #fff ; width: 100%;"></iframe>
