<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="box5.css"/>

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>

<c:if test="${not empty title}">
    <div class="clear"></div>
    <h4 class="box5-title box5-title${currentNode.properties['j:style'].string}">${fn:escapeXml(title.string)}</h4>
</c:if>
<div class="box5">
    <div class="box5 box5padding box5marginbottom">
        <div class="box5-content">
                    ${wrappedContent}
                    <div class="clear"></div>
        </div>
    </div>
</div>