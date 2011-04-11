<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="box2.css"/>
<template:addResources>
<!--[if lte IE 6]><link rel="stylesheet" type="text/css" href="<c:url value='${url.currentModule}/css/box2-ie6.css'/>" media="screen" /><![endif]-->
</template:addResources>
<div class="box2 box2-style1">
    <jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<c:if test="${not empty title}">
    <div class="box2-topright"></div><div class="box2-topleft"></div>
    <h3 class="box2-header"><span>${fn:escapeXml(title.string)}</span></h3>
</c:if>
  <div class="box2-text">
      ${wrappedContent}
  </div>
    <div class="box2-bottomright"></div>
    <div class="box2-bottomleft"></div>
<div class="clear"></div></div>
