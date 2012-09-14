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
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.defer.js"/>
<template:addResources type="css" resources="files.css"/>
<label for="file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}">${jcr:labelInNodeType(propertyDefinition,currentResource.locale,type)}</label>
<input type="hidden" name="${propertyDefinition.name}" id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"/>
<fmt:message key="label.select.file" var="fileLabel"/>
<fmt:message key="label.select.page" var="pageLabel"/>
<fmt:message key="label.select.folder" var="folderLabel"/>
<fmt:message key="label.selected" var="selected"/>
<c:url value="${url.files}" var="previewPath"/>


<c:set var="onSelect">function(uuid, path, title) {
    $('\#${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}').val(uuid);
    <c:choose>
        <c:when test="${propertyDefinition.selectorOptions.type == 'image'}">
            $('#display${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}').html('${selected} <img src="${previewPath}'+path+'"/>');
        </c:when>
        <c:otherwise>
            var filePath = '${previewPath}'+path;
            var filename=filePath.substring(filePath.lastIndexOf("/") + 1,filePath.length);
            var fileType=filePath.substring(filePath.lastIndexOf(".") + 1,filePath.length);
            $('#display${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}').html('<strong>${selected}</strong> <a class="icon '+fileType+'" href="'+filePath+'">'+filename+'</a>');
        </c:otherwise>
    </c:choose>
    return false;
    }</c:set>
<c:set var="onClose">function(){$("#treepreview").empty().hide();}</c:set>
<c:set var="fancyboxOptions">{
    height:600,
    width:600
    }</c:set>
<fieldset>
    <c:choose>
        <c:when test="${propertyDefinition.selectorOptions.type == 'image'}">
            <ui:fileSelector fieldId="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                             displayFieldId="file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}" valueType="identifier"
                             label="${fileLabel}"
                             nodeTypes="nt:folder,jmix:image,jnt:virtualsite"
                             selectableNodeTypes="jmix:image"
                             onSelect="${onSelect}"
                             onClose="${onClose}"
                             fancyboxOptions="${fancyboxOptions}" treeviewOptions="{preview:true,previewPath:'${previewPath}'}"/>
        </c:when>
        <c:when test="${propertyDefinition.selectorOptions.type == 'folder'}">
            <ui:fileSelector fieldId="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                             displayFieldId="file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}" valueType="identifier"
                             label="${folderLabel}"
                             nodeTypes="nt:folder,jnt:virtualsite"
                             selectableNodeTypes="jnt:folder"
                             onSelect="${onSelect}"
                             onClose="${onClose}"
                             fancyboxOptions="${fancyboxOptions}" treeviewOptions="{preview:false,previewPath:'${previewPath}'}"/>
        </c:when>
        <c:when test="${propertyDefinition.selectorOptions.type == 'contentfolder'}">
            <ui:fileSelector fieldId="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                             displayFieldId="file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}" valueType="identifier"
                             label="${folderLabel}"
                             nodeTypes="jnt:contentFolder"
                             selectableNodeTypes="jnt:contentFolder"
                             onSelect="${onSelect}"
                             onClose="${onClose}"
                             fancyboxOptions="${fancyboxOptions}" treeviewOptions="{preview:false,previewPath:'${previewPath}'}"/>
        </c:when>        
        <c:when test="${propertyDefinition.selectorOptions.type == 'page'}">
            <ui:fileSelector fieldId="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                             displayFieldId="file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}" valueType="identifier"
                             label="${pageLabel}"
                             nodeTypes="jnt:page"
                             selectableNodeTypes="jnt:page"
                             onSelect="${onSelect}"
                             onClose="${onClose}"
                             fancyboxOptions="${fancyboxOptions}" treeviewOptions="{preview:false,previewPath:'${previewPath}'}"/>
        </c:when>
        <c:otherwise>
            <ui:fileSelector fieldId="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                             displayFieldId="file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}" valueType="identifier"
                             label="${fileLabel}"
                             onSelect="${onSelect}"
                             onClose="${onClose}"
                             fancyboxOptions="${fancyboxOptions}" treeviewOptions="{preview:true,previewPath:'${previewPath}'}"/>
        </c:otherwise>
    </c:choose>
    <c:if test="${propertyDefinition.selectorOptions.type != 'folder' && propertyDefinition.selectorOptions.type != 'contentfolder' && propertyDefinition.selectorOptions.type != 'page'}">
        <strong><fmt:message key="label.or"/></strong>
        <div id="file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}" jcr:id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}">
            <span><fmt:message key="add.file"/></span>
        </div>
    </c:if>
    <div id="display${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}" jcr:id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}">
    </div>
    <fmt:message key="add.file" var="i18nAddFile"/>
</fieldset>
<template:addResources>
    <script>
        $(document).ready(function() {
            $("#file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}").editable('<c:url value="${url.base}${param['path'] == null ? renderContext.mainResource.node.path : param['path']}"><c:param name="jcrContributePost" value="true"/></c:url>', {
                type : 'ajaxupload',
                onblur : 'ignore',
                submit : 'OK',
                cancel : 'Cancel',
                submitdata : {'jcrContributePost':'true'},
                tooltip : 'Click to edit',
                callback : function (data, status,original) {
                    var id = $(original).attr('jcr:id');
                    $("#"+id).val(data.uuids[0]);
                    <c:choose>
                    <c:when test="${propertyDefinition.selectorOptions.type == 'image'}">
                    $("#display${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}").html("${selected} <img src='"+data.urls[0]+"'/>");
                    </c:when>
                    <c:otherwise>
                    var fileType=data.urls[0].substring(data.urls[0].lastIndexOf(".") + 1,data.urls[0].length);
                    var filename=data.urls[0].substring(data.urls[0].lastIndexOf("/") + 1,data.urls[0].length);
                    $("#display${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}").html("<strong>${selected}</strong> <a class='icon "+fileType+"' href='"+data.urls[0]+"'>"+filename+"</a>");
                    </c:otherwise>
                    </c:choose>
                    $("#file${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}").html('<span>${functions:escapeJavaScript(i18nAddFile)}</span>');
                }
            });
        });
    </script>
</template:addResources>