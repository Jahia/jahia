<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="box.css"/>

<div class="box box-style1">
    <div class="box-content">
        ${wrappedContent}
    </div>
    <div class="clear"></div>
</div>