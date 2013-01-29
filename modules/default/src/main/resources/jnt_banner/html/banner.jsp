<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="banner.css"/>
<jcr:nodeProperty node="${currentNode}" name="background" var="background"/>
<template:addCacheDependency node="${background.node}"/>
<div id="banner" style="background:transparent url(${background.node.url}) no-repeat top left;">
    <div class="banner-text"
         style='margin-top:${currentNode.properties.positionTop.string}px; margin-left:${currentNode.properties.positionLeft.string}px'>
        <h2><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h2>

        <p>${currentNode.properties.cast.string}</p>

        <div class="clear"></div>
    </div>
</div>
