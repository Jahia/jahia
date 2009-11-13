<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:remove var="currentList" scope="request"/>
<template:module node="${currentNode}" forcedTemplate="hidden.load" editable="false" >
    <template:param name="forcedSkin" value="none" />
</template:module>

<c:if test="${empty editable}">
    <c:set var="editable" value="false"/>
</c:if>

<div class="columns2"><!--start 2columns -->
    <c:forEach items="${currentList}" var="subchild">
        <div class="column-item">
            <template:module node="${subchild}" template="${subNodesTemplate}" editable="${editable}" >
                <c:if test="${not empty forcedSkin}">
                    <template:param name="forcedSkin" value="${forcedSkin}"/>
                </c:if>
                <c:if test="${not empty renderOptions}">
                    <template:param name="renderOptions" value="${renderOptions}"/>
                </c:if>
            </template:module>
        </div>
    </c:forEach>

    <div class="clear"> </div>
</div>
