<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set target="${renderContext}" property="contentType" value="text/csv;charset=UTF-8"/>
<c:forEach items="${currentNode.children}" var="subchild" varStatus="status" end="20">
<c:if test="${status.first}">
<template:module node="${subchild}" template="headers"/><% out.println(); %>
</c:if>
<template:module node="${subchild}"/><% out.println(); %>
</c:forEach>