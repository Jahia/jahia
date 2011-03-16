<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set target="${renderContext}" property="contentType" value="text/csv;charset=UTF-8"/>
<c:remove var="itemsList" scope="request"/>
<c:forTokens items="firstname,lastname,title,age,birthdate,gender,profession,maritalStatus,hobbies,contact,address,city,state,zip,country,remarks" delims="," var="propName" varStatus="status">
    <c:if test="${currentNode.properties[propName].boolean}">
        <c:if test="${not empty itemsList}">
            <c:set scope="request" var="itemsList" value="${itemsList},${propName}"/>
        </c:if>
        <c:if test="${empty itemsList}">
            <c:set scope="request" var="itemsList" value="${propName}"/>
        </c:if>
    </c:if>
</c:forTokens>
<c:forEach items="${jcr:getChildrenOfType(currentNode, 'jnt:contact')}" var="subchild" varStatus="status" end="20">
<c:if test="${status.first}">
<template:module node="${subchild}" view="headers"/>
</c:if><template:module node="${subchild}"/></c:forEach>