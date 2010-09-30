<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="docspace.css,files.css,toggle-docspace.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>

<template:addResources type="javascript" resources="jquery.cuteTime.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.fancybox.pack.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<c:set var="aclNode" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty aclNode}">
    <ul class="docspacelist docspacelistusers" id="displayAclUsers">
        <c:forEach items="${aclNode.aclEntries}" var="acls">
            <li>
                <c:set var="users" value="${fn:substringBefore(acls.key, ':')}"/>
                <c:choose>
                    <c:when test="${users eq 'u'}">
                        <c:set value="user_32" var="iconName"/>
                    </c:when>
                    <c:when test="${users eq 'g'}">
                        <c:set value="group-icon" var="iconName"/>
                    </c:when>
                </c:choose>
                <img class="floatleft" alt="user default icon" src="${url.currentModule}/images/${iconName}.png"/>
                <a class="floatleft" href="#"><c:out value="${fn:substringAfter(acls.key,':')}"/></a>

                <div class='clear'></div>
            </li>
        </c:forEach>
    </ul>
</c:if>
<template:linker property="j:bindedComponent"/>