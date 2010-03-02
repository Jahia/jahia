<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<div id="${currentNode.UUID}">

    <c:remove var="currentList" scope="request"/>
    <c:choose>
        <c:when test="${jcr:isNodeType(currentNode, 'jmix:pager')}">
            <c:set scope="request" var="paginationActive" value="true"/>
        </c:when>
        <c:otherwise>
            <c:set var="begin" value="0" scope="request"/>
        </c:otherwise>
    </c:choose>
    <template:module node="${currentNode}" forcedTemplate="hidden.load" editable="false">
        <template:param name="forcedSkin" value="none"/>
    </template:module>

    <c:if test="${empty editable}">
        <c:set var="editable" value="false"/>
    </c:if>
    <c:if test="${not empty paginationActive}">
        <template:option node="${currentNode}" nodetype="jmix:pager" template="hidden.init"/>
    </c:if>
    <c:if test="${not empty currentList}">
        <ul>
            <c:forEach items="${currentList}" var="subchild" begin="${begin}" end="${end}">
                <li>
                    <jcr:nodeProperty node="${subchild}" name="jcr:title" var="title"/>
                    <c:choose>
                        <c:when test='${jcr:isNodeType(subchild, "nt:file")}'>
                            <a href="${url.files}${subchild.path}"><c:out
                                    value="${not empty title && not empty title.string ? title.string : subchild.name}"/></a>
                        </c:when>
                        <c:otherwise>
                            <a href="${url.base}${subchild.path}.html"><c:out
                                    value="${not empty title && not empty title.string ? title.string : subchild.name}"/></a>
                        </c:otherwise>
                    </c:choose>
                </li>
            </c:forEach>
        </ul>
    </c:if>
    <div class="clear"></div>
    <c:if test="${not empty paginationActive}">
        <template:option node="${currentNode}" nodetype="jmix:pager" template="hidden"/>
    </c:if>
</div>
<template:removePager/>