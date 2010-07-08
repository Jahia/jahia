<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="datepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>
<template:addResources type="javascript" resources="animatedcollapse.js"/>
<template:include templateType="html" template="hidden.header"/>
<c:set var="animatedTasks" value=""/>
<c:set var="animatedWFs" value=""/>

        <c:set var="inSite" value="true"/>
        <c:forEach items="${moduleMap.currentList}" var="child" begin="${moduleMap.begin}" end="${moduleMap.end}" varStatus="status">
            <%@include file="edit.jspf" %>
            <%@include file="workflow.jspf" %>
            <div id="edit-${child.identifier}">
                <template:module templateType="html" node="${child}"/>
            </div>
            <hr/>
        </c:forEach>
        <div class="clear"></div>
        <c:if test="${moduleMap.editable and renderContext.editMode}">
            <template:module path="*"/>
        </c:if>
        <template:include templateType="html" template="hidden.footer"/>


<c:if test="${not renderContext.ajaxRequest}">
    <%-- include add nodes forms --%>
    <c:choose>
        <c:when test="${empty restrictions}">
            <jcr:nodeProperty node="${currentNode}" name="j:contributeTypes" var="types"/>
        </c:when>
        <c:otherwise>
            <c:set var="type" value="${restrictions}"/>
        </c:otherwise>
    </c:choose>
    <h3 class="titleaddnewcontent">
        <img title="" alt="" src="${url.currentModule}/images/add.png"/><fmt:message key="label.add.new.content"/>
    </h3>
    <script language="JavaScript">
        <c:forEach items="${types}" var="type" varStatus="status">
        animatedcollapse.addDiv('add${currentNode.identifier}-${status.index}', 'fade=1,speed=700,group=newContent');
        </c:forEach>
        animatedcollapse.init();
    </script>
    <c:if test="${types != null}">
        <div class="listEditToolbar">
            <c:forEach items="${types}" var="type" varStatus="status">
                <jcr:nodeType name="${type.string}" var="nodeType"/>
                <button onclick="animatedcollapse.toggle('add${currentNode.identifier}-${status.index}');"><span
                        class="icon-contribute icon-add"></span>${jcr:label(nodeType, renderContext.mainResourceLocale)}
                </button>
            </c:forEach>
        </div>

        <c:forEach items="${types}" var="type" varStatus="status">
            <div style="display:none;" id="add${currentNode.identifier}-${status.index}">
                <template:module node="${currentNode}" templateType="edit" template="add">
                    <template:param name="resourceNodeType" value="${type.string}"/>
                    <template:param name="currentListURL" value="${url.current}.ajax"/>
                </template:module>
            </div>
        </c:forEach>

    </c:if>
</c:if>

<c:if test="${jcr:getParentOfType(currentNode, 'jnt:page') eq null && !jcr:isNodeType(currentNode,'jnt:page')}">
</div>

</c:if>