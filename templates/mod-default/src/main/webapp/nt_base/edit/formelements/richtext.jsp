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
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/ckeditor/ckeditor.js"/>
<label for="ckeditor${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}">${jcr:labelForLocale(propertyDefinition,renderContext.mainResourceLocale)}</label>
<input type="hidden" name="${propertyDefinition.name}" id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"/>
<textarea rows="50" cols="40" id="ckeditor${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"></textarea>
<script>
    var editor${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')} = undefined;

    $(document).ready(function() {
        editor${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')} = CKEDITOR.replace("ckeditor${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}", { toolbar : 'User'});
    });

    $("#${currentNode.name}${scriptTypeName}").submit(function() {
        if (editor${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')} !== undefined) {
            $("#${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}").val(editor${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}.getData());
            CKEDITOR.remove(editor${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')});
            editor${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')} = undefined;
        }
    });
</script>