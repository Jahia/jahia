<%@ tag body-content="empty" description="Renders the trigger link and the tree control to select an item." %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted item value with." %>
<%@ attribute name="displayFieldId" required="false" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted item display title with." %>
<%@ attribute name="fieldIdIncludeChildren" required="false" type="java.lang.String"
              description="The ID and name of the include children input field." %>
<%@ attribute name="displayIncludeChildren" required="false" type="java.lang.Boolean"
              description="Do we need to show the include children checkbox? [true]" %>
<%@ attribute name="includeChildren" type="java.lang.Boolean"
              description="The initial value for the include children checkbox field. [true]" %>
<%@ attribute name="label" required="false" type="java.lang.String"
              description="The trigger link text." %>
<%@ attribute name="includeChildrenLabel" required="false" type="java.lang.String"
              description="The include children checkbox text." %>
<%@ attribute name="onSelect" required="false" type="java.lang.String"
              description="The JavaScript function to be called after an item is selectd. Three paramaters are passed as arguments: node identifier, node path and display name. If the function retuns true, the value will be also set into the field value. Otherwise nothing will be done by this tag." %>
<%@ attribute name="onClose" required="false" type="java.lang.String"
              description="The JavaScript function to be called after window is closed." %>
<%@ attribute name="nodeTypes" required="false" type="java.lang.String"
              description="Comma-separated list of node types to filter out the tree. Empty by default, i.e. all nodes will be displayed." %>
<%@ attribute name="selectableNodeTypes" required="false" type="java.lang.String"
              description="Comma-separated list of node types that can be selected in the tree. Empty by default, i.e. all nodes will be selectable." %>
<%@ attribute name="root" required="false" type="java.lang.String"
              description="The path of the root node for the tree. [current site path]" %>
<%@ attribute name="valueType" required="false" type="java.lang.String"
              description="Either identifier, path or title of the selected item. This value will be stored into the target field. [path]" %>
<%@ attribute name="fancyboxOptions" required="false" type="java.lang.String"
              description="In case the options for the jQuery FancyBox plugin needs to be overridden, they should be specified here in a form {option1: value1, opttion2: value2}. If option value is a literal, please, enclose it into single quotes." %>
<%@ attribute name="treeviewOptions" required="false" type="java.lang.String"
              description="In case the options for the jQuery Treeview plugin needs to be overridden, they should be specified here in a form {option1: value1, opttion2: value2}. If option value is a literal, please, enclose it into single quotes." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<template:addResources type="css" resources="jquery.treeview.css,jquery.fancybox.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.treeview.min.js,jquery.treeview.async.jahia.js,jquery.fancybox.js"/>
<template:addResources type="javascript" resources="treeselector.js"/>
<c:set var="root" value="${functions:default(root, renderContext.site.path)}"/>
<c:set var="displayIncludeChildren" value="${functions:default(displayIncludeChildren, 'true')}"/>
<c:if test="${empty displayFieldId}"><c:set var="displayFieldId" value="${fieldId}-treeItemSelectorTrigger"/></c:if>
<c:if test="${empty fieldIdIncludeChildren}"><c:set var="fieldIdIncludeChildren" value="${fieldId}_includeChildren"/></c:if>
<%-- by default set includeChildren to 'true' to search in subnodes --%>
<c:set var="includeChildren" value="${functions:default(includeChildren, 'true')}"/>
<%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
<c:set var="includeChildren" value="${functions:default(param[fieldIdIncludeChildren], empty paramValues[fieldId] ? includeChildren : 'false')}"/>
<c:if test="${empty label}"><c:set var="label"><fmt:message key="selectors.select"/></c:set></c:if>
<c:if test="${empty includeChildrenLabel}"><c:set var="includeChildrenLabel"><fmt:message key="selectors.includeChildren"/></c:set></c:if>
<a href="#${fieldId}-treeItemSelector" id="${fieldId}-treeItemSelectorTrigger">${fn:escapeXml(label)}</a>
<c:if test="${displayIncludeChildren}">
    &nbsp;<input type="checkbox" id="${fieldIdIncludeChildren}" name="${fieldIdIncludeChildren}" value="true" ${includeChildren ? 'checked="checked"' : ''}/>&nbsp;<label for="${fieldIdIncludeChildren}">${fn:escapeXml(includeChildrenLabel)}</label>
</c:if>
<c:choose>
    <c:when test="${renderContext.liveMode}">
        <c:url value='${url.baseLive}' var="baseURL"/>
    </c:when>
    <c:otherwise>
        <c:url value='${url.basePreview}' var="baseURL"/>
    </c:otherwise>
</c:choose>

<script type="text/javascript">
    $(document).ready(function() { jahiaCreateTreeItemSelector("${fieldId}", "${displayFieldId}", "${baseURL}", "${root}", "${nodeTypes}", "${selectableNodeTypes}", "${valueType}", ${not empty onSelect ? onSelect : 'null'}, ${not empty onClose ? onClose : 'null'}, ${not empty treeviewOptions ? treeviewOptions :  'null'}, ${not empty fancyboxOptions ? fancyboxOptions : 'null'}); });
</script>

<div style="display:none"><div id="${fieldId}-treeItemSelector"><ul id="${fieldId}-treeItemSelectorTree"></ul></div></div>