<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="box3.css"/>
<template:addResources type="css" resources="box3-ie6.css" condition="if lte IE 6" media="screen" />
<div class="box3-container box3-style1">
    <div class="box3-topright"></div>
    <div class="box3-topleft"></div>

    <div class="box3-text">
        ${wrappedContent}
    </div>
    <div class="box3-bottomright"></div>
    <div class="box3-bottomleft"></div>
</div>
