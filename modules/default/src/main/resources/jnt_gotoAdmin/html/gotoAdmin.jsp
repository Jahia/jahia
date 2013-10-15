<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:if test="${!renderContext.settings.readOnlyMode}">
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="css" resources="goto-links.css"/>
<c:if test="${!empty currentNode.properties['toAdminComponent']}">
    <template:addResources>
    <script type="text/javascript">
        $.post("${url.context}/welcome/adminmode", null, null, "json");
    </script>
    </template:addResources>
    <c:choose>
        <c:when test="${currentNode.properties['toAdminComponent'].string eq 'users'}">
            <c:url var="link" value="/cms/admin/${renderContext.UILocale.language}/settings.manageUsers.html"/>
        </c:when>
        <c:when test="${currentNode.properties['toAdminComponent'].string eq 'groups'}">
            <c:url var="link" value="/cms/edit/${renderContext.UILocale.language}/${renderContext.site.path}.manageGroups.html"/>
        </c:when>
        <c:otherwise>
            <c:url var="link" value="/cms/admin/default/${renderContext.UILocale.language}/settings.aboutJahia.html"/>
        </c:otherwise>
    </c:choose>
    <c:set var="optionURL" value="?do=${currentNode.properties['toAdminComponent'].string}&sub=display"/>
</c:if>
<c:set var="linkTarget" value="${!empty currentNode.properties['j:target'] ? currentNode.properties['j:target'].string : ''}"/>
<c:if test="${not empty linkTarget}"><c:set var="linkTarget"> target="${linkTarget}"</c:set></c:if>
<c:choose>
    <c:when test="${not empty currentNode.properties['buttonClass']}">
        <a href="${link}" class="${currentNode.properties['buttonClass'].string}"${linkTarget}>
            <c:if test="${!empty currentNode.properties['jcr:title']}" var="titlePresent">
                ${fn:escapeXml(currentNode.properties["jcr:title"].string)}
            </c:if>
            <c:if test="${!titlePresent}">
                <fmt:message key="label.administration"/>
            </c:if>
        </a>
    </c:when>
    <c:otherwise>
        <img src="${url.context}/icons/admin.png" width="16" height="16" alt=" " role="presentation"
             style="position:relative; top: 4px; margin-right:2px; "/><a
            href="${link}"${linkTarget}>
        <c:if test="${!empty currentNode.properties['jcr:title']}" var="titlePresent">
            ${fn:escapeXml(currentNode.properties["jcr:title"].string)}
        </c:if>
        <c:if test="${!titlePresent}">
            <fmt:message key="label.administration"/>
        </c:if>
    </a>
    </c:otherwise>
</c:choose>
</c:if>