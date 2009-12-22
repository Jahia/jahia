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

<c:if test="${not empty currentList}">
<ul>
<c:forEach items="${currentList}" var="subchild">
    <li>
		<jcr:nodeProperty node="${subchild}" name="jcr:title" var="title"/>
        <a href="${url.base}/${subchild.path}.html"><c:out value="${not empty title && not empty title.string ? title.string : currentNode.name}"/></a>
    </li>
</c:forEach>
</ul>
</c:if>