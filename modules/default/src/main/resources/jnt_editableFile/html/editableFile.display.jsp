<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.js, codemirror/lib/codemirror.js,codemirror/mode/xml/xml.js,codemirror/mode/htmlmixed/htmlmixed.js,codemirror/mode/javascript/javascript.js,codemirror/mode/clike/clike.js,codemirror/mode/css/css.js,codemirror/mode/htmlembedded/htmlembedded.js,codemirror.mode.jsp.js"/>
<template:addResources type="css" resources="01web.css,codemirror/codemirror.css"/>
<template:addResources type="css" resources="admin-bootstrap.css"/>
<template:addResources type="javascript" resources="admin-bootstrap.js"/>
<div class="container-fluid">
    <div class="row-fluid">
        <div class="span12">
            <c:url var="postURL" value="${url.base}${currentNode.path}"/>
            <c:set var="lastModidiedLoaded" value="${currentNode.properties['jcr:lastModified'].date.timeInMillis}"/>
            <c:set var="locked" value="false"/>
            <fmt:message key="jnt_editableFile.save.error" var="saveError" />
            <c:set var="saveError" value="${functions:escapeJavaScript(saveError)}"/>
            <c:if test="${currentNode.locked and !(currentNode.lockOwner eq renderContext.user.name)}">
                <c:set var="locked" value="true"/>
            </c:if>
            <c:if test="${locked}">
                <div class="alert">
                    <fmt:message key="label.edit.engine.heading.locked.by">
                        <fmt:param value="${currentNode.lock.lockOwner}"/>
                    </fmt:message>
                </div>
            </c:if>
            <div class="alert  alert-error hide" id="alertReload">
                <fmt:message key="jnt_editableFile.must.reload"/>
            </div>
            <div class="well">
                <c:set var="isResourceBundle" value="${jcr:isNodeType(currentNode, 'jnt:resourceBundleFile')}"/>
                <template:tokenizedForm disableXSSFiltering="true" allowsMultipleSubmits="true">
                    <form name="sourceForm" id="sourceForm" method="POST" action="${postURL}">
                        <textarea id="sourceCode" name="sourceCode" editable="false"><c:out value="${isResourceBundle ? functions:unescapeJava(currentNode.properties.sourceCode.string) : currentNode.properties.sourceCode.string}" escapeXml="true"/></textarea>
                    </form>
                </template:tokenizedForm>
                <c:choose>
                    <c:when test="${fn:endsWith(currentNode.name,'.css')}">
                        <c:set var="mode" value="css"/>
                    </c:when>
                    <c:when test="${fn:endsWith(currentNode.name,'.js')}">
                        <c:set var="mode" value="javascript"/>
                    </c:when>
                    <c:when test="${fn:endsWith(currentNode.name,'.java')}">
                        <c:set var="mode" value="text/x-java"/>
                    </c:when>
                    <c:when test="${fn:endsWith(currentNode.name,'.xml')}">
                        <c:set var="mode" value="xml"/>
                    </c:when>
                    <c:when test="${fn:endsWith(currentNode.name,'.properties')}">
                        <c:set var="mode" value="properties"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="mode" value="jsp"/>
                    </c:otherwise>
                </c:choose>
                <c:set var="saveEnabled" value="${not locked and not isResourceBundle}"/>
                <c:if test="${saveEnabled}">
                    <button class="btn btn-primary" name="save" id="saveButton" onclick="saveSourceCode();" disabled><fmt:message key="label.save"/></button>
                </c:if>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    var myCodeMirror = CodeMirror.fromTextArea(document.getElementById("sourceCode"),{mode:"${mode}",lineNumbers:true, matchBrackets:true, readOnly:'${jcr:isNodeType(currentNode, 'jnt:resourceBundleFile') or locked?"nocursor":""}'});
    myCodeMirror.setSize("100%","100%");
    <c:if test="${saveEnabled}">
    var doEditTest = true;


    myCodeMirror.on("beforeChange", function() {
        if (doEditTest && $('#saveButton').prop('disabled')) {
            $.get("<c:url value="${url.base}${currentNode.path}.lockEditableFile.do?type=editSource&lastModifiedLoaded=${lastModidiedLoaded}"/>",null,function(data,status,jqXHR) {
                if (data.error != undefined) {
                    myCodeMirror.setOption("readOnly","nocursor");
                    $("#alertReload").show();
                } else {
                    $('#saveButton').prop('disabled', false);
                }
            }, "json");
            doEditTest = false;
        }
    });


    function saveSourceCode() {
        disabled = $('#saveButton').prop('disabled');
        if (!disabled) {
            var data = {"sourceCode" : myCodeMirror.getValue(),
                "form-token" : document.forms['sourceForm'].elements['form-token'].value,
                "jcrMethodToCall":"put"};
            $.ajax({
                type: 'POST',
                url: '${postURL}',
                data: data,
                success: function() {
                   $.get("<c:url value="${url.base}${currentNode.path}.unlock.do?type=editSource"/>",null,function() {
                       location.reload();
                   });
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    $('#saveButton').prop('disabled', true);
                    error = "${saveError}";
                    if (textStatus == 'error') {
                        error = $.parseJSON(jqXHR.responseText)['error']
                    }
                    $("#alertReload").html(error);
                    $("#alertReload").show();
                },
                dataType: 'json'
            });
        }
    }
    $(window).blur(function() {
        saveSourceCode();
    });
    $(window).bind('keydown', function(event) {
        if (event.ctrlKey || event.metaKey) {
            switch (String.fromCharCode(event.which).toLowerCase()) {
                case 's':
                    event.preventDefault();
                    saveSourceCode();
                    break;
            }
        }
    });
    </c:if>
</script>
