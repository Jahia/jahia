<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<template:addResources type="css" resources="box6.css"/>

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>

<div class="box6 box6-${currentNode.properties['j:style'].string} box6shadow"><!--box6-style1/2/3..10-->
                <c:if test="${not empty title}">
                    <h3 class="box6-title">${fn:escapeXml(title.string)}</h3>
                </c:if>
                ${wrappedContent}
                <div class="clear"></div>
</div>