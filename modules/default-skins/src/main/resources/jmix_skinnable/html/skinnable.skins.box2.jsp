<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="box2.css"/>
<template:addResources type="css" resources="box2-ie6.css" condition="if lte IE 6" media="screen" />
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<div class="box2 ">
    <div class="box2-topright"></div><div class="box2-topleft"></div>
    <c:if test="${not empty title}">
    <h3 class="box2-header"><span>${fn:escapeXml(title.string)}</span></h3>
</c:if>
  <div class="box2-text">
      ${wrappedContent}
  </div>
    <div class="box2-bottomright"></div>
    <div class="box2-bottomleft"></div>
<div class="clear"></div></div>
