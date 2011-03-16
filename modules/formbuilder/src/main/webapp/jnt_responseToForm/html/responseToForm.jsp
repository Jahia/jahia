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
<template:addResources type="javascript" resources="jquery.js,jquery.validate.js,jquery.maskedinput-1.2.2.js"/>
<template:addResources type="css" resources="formbuilder.css"/>
<jcr:node var="fieldsetsNode" path="${currentNode.parent.parent.path}/fieldsets"/>
<div>
    <c:forEach items="${fieldsetsNode.nodes}" var="fieldset">
        <c:forEach
                items="${jcr:getPropertiesAsStringFromNodeNameOfThatType(currentNode,fieldset,'jnt:formElement')}"
                var="entry">
            <jcr:node var="def" path="${fieldset.path}/${entry.key}"/>
            <c:if test="${jcr:isNodeType(def, 'jnt:automaticList')}" var="isAutomaticList">
                <jcr:nodeProperty node="${def}" name="type" var="type"/>
                <c:set var="renderers" value="${fn:split(type.string,'=')}"/>
                <c:if test="${fn:length(renderers) > 1}"><c:set var="renderer" value="${renderers[1]}"/></c:if>
                <c:if test="${not (fn:length(renderers) > 1)}"><c:set var="renderer" value=""/></c:if>
                <p><label>${entry.key}</label>&nbsp;<span>Value:<jcr:nodePropertyRenderer node="${currentNode}"
                                                                                          name="${entry.key}"
                                                                                          renderer="${renderer}"/></span>
                </p>
            </c:if>
            <c:if test="${not isAutomaticList}">
                <p>
                    <label>${entry.key}</label>&nbsp;<span>Value : ${entry.value}</span>
                </p>
            </c:if>
        </c:forEach>
        <c:forEach items="${currentNode.nodes}" var="subResponseNode">
            <template:module node="${subResponseNode}" view="default"/>
        </c:forEach>
    </c:forEach>
</div>