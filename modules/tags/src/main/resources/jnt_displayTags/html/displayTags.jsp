<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
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
<template:addResources type="css" resources="pagetagging.css"/>
<template:addResources type="css" resources="tagged.css"/>
<c:set var="boundComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty boundComponent}">
    <c:set var="nodeLocked" value="${not jcr:hasPermission(boundComponent, 'jcr:modifyProperties_default') or jcr:isLockedAndCannotBeEdited(boundComponent)}"/>
    <div id="tagThisPage${boundComponent.identifier}" class="tagthispage">

        <jcr:nodeProperty node="${boundComponent}" name="j:tagList" var="assignedTags"/>
        <c:set var="separator" value="${functions:default(currentResource.moduleParams.separator, ', ')}"/>
        <c:if test="${not nodeLocked}">
            <script type="text/javascript">
                function deleteTag_${fn:replace(boundComponent.identifier, "-", "_")} (htmlDeleteLink) {
                    var tagItem = $(htmlDeleteLink).parent();
                    var tag = htmlDeleteLink.dataset.tag;
                    $.post("<c:url value="${url.base}${boundComponent.path}"/>.removeTag.do", {"tag":tag, "unescape":true}, function(result) {
                        tagItem.hide();
                        if(result.size == "0"){
                            var spanNotYetTag = $('<span><fmt:message key="label.tags.notag"/></span>').attr('class', 'notaggeditem${boundComponent.identifier}');
                            $("#jahia-tags-${boundComponent.identifier}").append(spanNotYetTag)
                        }
                    }, "json");
                    return false;
                }
            </script>
        </c:if>
        <div class="tagged">
            <span><fmt:message key="label.tags"/>:</span>
            <span id="jahia-tags-${boundComponent.identifier}">
                <c:choose>
                    <c:when test="${not empty assignedTags}">
                        <c:forEach items="${assignedTags}" var="tag" varStatus="status">
                            <div style="display:inline;">
                                <span class="taggeditem">${fn:escapeXml(tag.string)}</span>
                                <c:if test="${not nodeLocked}">
                                    <a class="delete" onclick="deleteTag_${fn:replace(boundComponent.identifier, "-", "_")} (this); return false;"
                                       data-tag="${functions:escapePath(tag.string)}" href="#"></a>${!status.last ? separator : ''}
                                </c:if>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <span class="notaggeditem${boundComponent.identifier}"><fmt:message
                                key="label.tags.notag"/></span>
                    </c:otherwise>
                </c:choose>
            </span>
        </div>
    </div>
</c:if>
