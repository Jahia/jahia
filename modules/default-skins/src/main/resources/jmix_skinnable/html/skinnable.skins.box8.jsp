<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="box8.css"/>

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>

<c:if test="${renderContext.editMode or not empty fn:trim(wrappedContent)}">
    <c:if test="${not empty title}">
        <div class="clear"></div>
        <h4 class="box8-title box8-title${currentNode.properties['j:style'].string}${!empty currentNode.properties.icon ? ' box8-title-icon' : ''}">
            <c:if test="${!empty currentNode.properties.icon}">
                <span class="box8-icon" style="background-image: url(${currentNode.properties.icon.node.url})"></span>
            </c:if>
                ${fn:escapeXml(title.string)}
        </h4>

    </c:if>
    <div class="box8">
        <div class="box8 box5padding box5marginbottom">
            <div class="box8-content">
                    ${wrappedContent}
                <div class="clear"></div>
            </div>
        </div>
    </div>
</c:if>