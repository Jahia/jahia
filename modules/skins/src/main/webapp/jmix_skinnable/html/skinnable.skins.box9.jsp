<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<template:addResources type="css" resources="box9.css"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<div class="box5padding box5marginbottom box9 box9-bg${currentNode.properties['j:style'].string}">
    <c:if test="${not empty title}">
      <div class="clear"></div>
      <h4 class="box9-title">${fn:escapeXml(title.string)}</h4>
    </c:if>
    <div class="box9-content"> ${wrappedContent}
      <div class="clear"></div>
    </div>
</div>
