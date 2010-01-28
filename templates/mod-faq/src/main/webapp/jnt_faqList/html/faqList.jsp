<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="faq.css"/>

<div class="summary faqSummary">
    <ol>
        <c:forEach items="${currentNode.editableChildren}" var="subchild">
            <li><template:module node="${subchild}" template="summary" /></li>
        </c:forEach>
    </ol>
</div>

<div class="faqList">
    <ol>
        <c:forEach items="${currentNode.editableChildren}" var="subchild">
            <li><template:module node="${subchild}"/></li>
        </c:forEach>
    </ol>
</div>