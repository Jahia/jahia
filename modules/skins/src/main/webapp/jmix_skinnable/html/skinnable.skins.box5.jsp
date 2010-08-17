<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="box5.css"/>

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>

<div class="box5">
<div class="box5grey box5padding16 box5marginbottom16">
    <div class="box5-inner">
            <div class="box5-inner-border"><!--start box5 -->
                <c:if test="${not empty title}">
                    <h3 class="box5titleh3">${title.string}</h3>
                </c:if>
                ${wrappedContent}
                <div class="clear"></div>
            </div>
        </div>
    </div>
</div>