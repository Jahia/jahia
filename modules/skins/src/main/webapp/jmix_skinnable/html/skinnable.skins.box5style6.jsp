<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="box5.css"/>

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>

<c:if test="${not empty title}">
    <h4 class="box5-title box5-titlered">${title.string}</h4>
</c:if>
<div class="box5">
<div class="box5padding16 box5marginbottom16">
    <div class="box5-inner">
            <div class="box5-inner-border"><!--start box5 -->
                ${wrappedContent}
                <div class="clear"></div>
            </div>
        </div>
    </div>
</div>