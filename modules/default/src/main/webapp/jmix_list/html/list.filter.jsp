<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:include view="hidden.load"/>

<c:if test="${empty editable}">
    <c:set var="editable" value="false"/>
</c:if>

<div id="${currentNode.UUID}">


    <template:addResources type="javascript" resources="ajaxreplace.js"/>


    <jcr:node var="category" path="${jcr:getSystemSitePath()}/categories"/>
    <form id="filter">
        Category : <select name="categorykey"
                           onchange="javascript:jreplace('${currentNode.UUID}','<c:url value="${url.current}.ajax"/>?categorykey='+document.forms.filter.categorykey.value,null,null,true)">
        <c:if test="${empty param.categorykey}">
            <option selected value="">All</option>
        </c:if>
        <c:if test="${not empty param.categorykey}">
            <option value="">All</option>
        </c:if>
        <c:forEach items="${category.nodes}" var="cat">
            <c:if test="${jcr:isNodeType(cat, 'jnt:category')}">
                <jcr:nodeProperty node="${cat}" name="jcr:title" var="catTitle"/>
                <c:if test="${cat.name eq param.categorykey}">
                    <option selected value="${cat.name}">${catTitle.string}</option>
                </c:if>
                <c:if test="${cat.name ne param.categorykey}">
                    <option value="${cat.name}">${catTitle.string}</option>
                </c:if>
            </c:if>
        </c:forEach>
    </select>
    </form>
    <c:forEach items="${moduleMap.currentList}" var="subchild">
        <p>
            <c:if test="${empty param.categorykey}">
                <template:module node="${subchild}" view="${moduleMap.subNodesView}" editable="${moduleMap.editable}"/>
            </c:if>

            <c:if test="${not empty param.categorykey}">
                <jcr:nodeProperty node="${subchild}" name="j:defaultCategory" var="category"/>

                <c:set var="contains" value="false"/>
                <c:forEach items="${category}" var="val">
                    <c:if test="${val.node.name == param.categorykey}">
                        <c:set var="contains" value="true"/>
                    </c:if>
                </c:forEach>
                <c:if test="${contains eq true}">
                    <template:module node="${subchild}" view="${moduleMap.subNodesView}"
                                     editable="${moduleMap.editable}"/>
                </c:if>
            </c:if>
        </p>
    </c:forEach>
</div>