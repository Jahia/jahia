<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<template:include templateType="html" template="hidden.header"/>
<c:forEach items="${currentList}" var="subchild" begin="${begin}" end="${end}">
    <template:module node="${subchild}" templateType="edit" forcedTemplate="edit" >
        <c:if test="${not empty forcedSkin}">
            <template:param name="forcedSkin" value="${forcedSkin}"/>
        </c:if>
        <c:if test="${not empty renderOptions}">
            <template:param name="renderOptions" value="${renderOptions}"/>
        </c:if>
    </template:module>
</c:forEach>
<div class="clear"></div>
<c:if test="${editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>
<template:include templateType="html" template="hidden.footer"/>

<c:if test="${empty param.ajaxcall}">
    <%-- include add nodes forms --%>
    <jcr:nodeProperty node="${currentNode}" name="j:allowedTypes" var="types"/>
    <c:if test="${types != null}">
        <li>Forms :
            <ul>
                <c:forEach items="${types}" var="type">
                    <template:module node="${currentNode}" templateType="edit" template="add">
                        <template:param name="resourceNodeType" value="${type.string}"/>
                    </template:module>
                    <li>${category.string}</li>
                </c:forEach>
            </ul>
        </li>
    </c:if>
</c:if>
