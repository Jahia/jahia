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
<template:addResources type="javascript" resources="jquery.js,jquery.jeditable.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.defer.js"/>
<label for="file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}">${jcr:labelInNodeType(propertyDefinition,renderContext.mainResourceLocale,type)}</label>
<input type="hidden" name="${propertyDefinition.name}" id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"/>
<fmt:message key="label.select.file" var="fileLabel"/>
<ui:fileSelector fieldId="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                 displayFieldId="file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}" valueType="identifier"
        label="${fileLabel}"
        onSelect="function(uuid, path, title) {
            $('#${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}').val(uuid);
            $('#display${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}').html('selected'+title);
            return false;
        }"
        onClose="$.defer( 200, function() {
            $.fancybox({
                'content':$('.FormContribute'),
                'height':600,
                'width':600,
                'autoScale':false,
                'autoDimensions':false,
                'onComplete':function() {
                    $(\".newContentCkeditorContribute${currentNode.identifier}${fn:replace(resourceNodeType,':','_')}\").each(function() { $(this).ckeditor(); $(this).data('ckeditorInstance').checkWCAGCompliance=wcagCompliant; })
                },

                'onCleanup':function() {
                    $(\".newContentCketempditorContribute${currentNode.identifier}${fn:replace(resourceNodeType,':','_')}\").each(function() { if ($(this).data('ckeditorInstance')) { $(this).data('ckeditorInstance').destroy()  } });
                }
             }
            );
        })"
        fancyboxOptions="{
            onStart: function() {
                $(\".newContentCkeditorContribute${currentNode.identifier}${fn:replace(resourceNodeType,':','_')}\").each(function() { if ($(this).data('ckeditorInstance')) { $(this).data('ckeditorInstance').destroy()  } });
                $('#addNewContent').append($('.FormContribute'))
            }
        }"/>
<span><fmt:message key="label.or"/></span>
<div id="file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}" jcr:id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}">
    <span><fmt:message key="add.file"/></span>
</div>
<div id="display${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}" jcr:id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}">
</div>
<template:addResources>
<script>
    $(document).ready(function() {
        $("#file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}").editable('${url.base}${renderContext.mainResource.node.path}', {
            type : 'ajaxupload',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit',
            callback : function (data, status,original) {
                var id = $(original).attr('jcr:id');
                $("#"+id).val(data.uuids[0]);
                $("#display${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}").html(data.urls[0]);
                $("#file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}").html('<span><fmt:message key="add.file"/></span>');
            }
        });
    });
</script>
</template:addResources>