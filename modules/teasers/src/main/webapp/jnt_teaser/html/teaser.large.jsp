<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="teaser.css"/>
<jcr:nodeProperty node="${currentNode}" name="image" var="image"/>

    <!--start box -->
    <div class="box2teaser "><!--start box 2 default-->
        <div class="box2teaser-topright"></div><div class="box2teaser-topleft"></div>
        <h3 class="box2teaser-header"><span><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></span></h3>
        <div class="box2teaser-illustration" style="background-image:url(${image.node.url})"></div>

        <div class="box2teaser-text">${currentNode.properties.abstract.string}</div>
        <div class="box2teaser-more"><a href="<c:url value='${url.base}${currentNode.properties.link.node.path}.html'/>"><fmt:message key="jnt_teaser.readMore"/></a></div>
        <div class="box2teaser-bottomright"></div>
        <div class="box2teaser-bottomleft"></div>
        <div class="clear"> </div>
    </div>
    <!--stop box -->
<div class="clear"> </div>