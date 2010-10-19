<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<template:addResources type="css" resources="faq.css"/>

<div class="summary faqSummary">
    <h3>Summary</h3>
    <jcr:jqom var="results">
        <query:selector nodeTypeName="jnt:faq"/>
        <query:descendantNode path="${renderContext.mainResource.node.path}"/>
    </jcr:jqom>
    <ol>
        <c:forEach items="${results.nodes}" var="subchild">
            <template:module node="${subchild}" template="summary" />
        </c:forEach>
    </ol>
</div>