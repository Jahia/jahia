<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="box2.css"/>

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<div class="box2 ">
    <div class="box2-topright"></div><div class="box2-topleft"></div>
    <c:if test="${not empty title}">
    <h3 class="box2-header"><span>${title.string}</span></h3>
</c:if>
  <div class="box2-text">
      ${wrappedContent}
  </div>
    <div class="box2-bottomright"></div>
    <div class="box2-bottomleft"></div>
<div class="clear"></div></div>
