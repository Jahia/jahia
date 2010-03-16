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
<label class="left">Date : ${jcr:labelForLocale(propertyDefinition,renderContext.mainResourceLocale)}</label>
<input type="hidden" name="${propertyDefinition.name}" id="${propertyDefinition.name}"/>
<input type="text" id="datePicker${fn:replace(propertyDefinition.name,':','_')}" readonly="readonly"/>
<c:if test="${dateTimePicker}">
    </p>
    <p class="field">
    <label class="left">Time : ${jcr:labelForLocale(propertyDefinition,renderContext.mainResourceLocale)}</label>
    <input type="text" class="selHrs" style="width:20px" value="14"
           id="hourPicker${fn:replace(propertyDefinition.name,':','_')}"/>
    <input type="text" class="selMins" style="width:20px" value="03"
           id="minPicker${fn:replace(propertyDefinition.name,':','_')}"/>
</c:if>
<ui:dateSelector fieldId="datePicker${fn:replace(propertyDefinition.name,':','_')}" time="${dateTimePicker}"
                 hourFieldId="hourPicker${fn:replace(propertyDefinition.name,':','_')}"
                 minFieldId="minPicker${fn:replace(propertyDefinition.name,':','_')}">
    {dateFormat: $.datepicker.ISO_8601, showButtonPanel: true, showOn:'focus'}
</ui:dateSelector>
<script>
    $("#${currentNode.name}").submit(function() {
        var datePicked = $("#datePicker${fn:replace(propertyDefinition.name,':','_')}").val();
        if (datePicked == "") {
            return false;
        }
    <c:if test="${dateTimePicker}">
        var hourPicked = $('#hourPicker${fn:replace(propertyDefinition.name,':','_')}').val();
        var minPicked = $('#minPicker${fn:replace(propertyDefinition.name,':','_')}').val();
        $("#${propertyDefinition.name}").val(datePicked + "T" + hourPicked + ":" + minPicked + ":00.0");
    </c:if>
    <c:if test="${not dateTimePicker}">
        $("#${propertyDefinition.name}").val(datePicked);
    </c:if>
    });
</script>
