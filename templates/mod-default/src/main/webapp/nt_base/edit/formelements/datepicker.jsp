<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
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
<%--@elvariable id="selectorType" type="org.jahia.services.content.nodetypes.SelectorType"--%>
<template:addResources type="css" resources="960.css"/>
<template:addResources type="css" resources="formbuilder.css"/>
<template:addResources type="css" resources="ui.slider.css"/>
<template:addResources type="css" resources="datepicker.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.core.min.js,jquery-ui.slider.min.js"/>
<c:set var="dateTimePicker" value="${propertyDefinition.selector eq selectorType.DATETIMEPICKER}"/>
<jsp:useBean id="now" class="java.util.Date"/>
<label class="left" for="datePicker${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}">Date : ${jcr:labelForLocale(propertyDefinition,renderContext.mainResourceLocale)}</label>
<input type="hidden" name="${propertyDefinition.name}" id="${propertyDefinition.name}"/>
<input type="text" id="datePicker${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}" readonly="readonly" value="<fmt:formatDate value="${now}" pattern="yyyy-MM-dd HH:mm:ss"/>"/>
<ui:dateSelector fieldId="datePicker${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}" time="${dateTimePicker}">
    {dateFormat: $.datepicker.ISO_8601, showButtonPanel: true, showOn:'focus'}
</ui:dateSelector>
<script>
    $("#${currentNode.name}${scriptTypeName}").submit(function() {
        var datePicked = $("#datePicker${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}").val().trim().replace(" ","T");
        if (datePicked == "") {
            return false;
        }
        $("#${propertyDefinition.name}").val(datePicked);
    });
</script>
