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
<c:url var="postURL" value="${url.base}${currentNode.path}"/>
<c:set var="locked" value="false"/>
<fmt:message key="jnt_editableFile.save.error" var="saveError" />
<c:set var="saveError" value="${functions:escapeJavaScript(saveError)}"/>
<c:if test="${currentNode.locked and !(currentNode.lockOwner eq renderContext.user.name)}">
    <c:set var="locked" value="true"/>
</c:if>
<c:if test="${locked}">
    <span class="text"><fmt:message key="label.edit.engine.heading.locked.by"></span>
    <fmt:param value="${currentNode.lock.lockOwner}"/>
</fmt:message>
</c:if>
<template:tokenizedForm disableXSSFiltering="true" allowsMultipleSubmits="true">
    <form name="sourceForm" id="sourceForm" method="POST" action="${postURL}">
        <textarea id="sourceCode" name="sourceCode" editable="false"><c:out value="${currentNode.properties.sourceCode.string}" escapeXml="true"/></textarea>
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
<c:if test="${not locked and not jcr:isNodeType(currentNode, 'jnt:resourceBundleFile')}">
    <button name="save" id="saveButton" onclick="saveSourceCode();" disabled><fmt:message key="label.save"/></button>
</c:if>
<script type="text/javascript">
    var myCodeMirror = CodeMirror.fromTextArea(document.getElementById("sourceCode"),{mode:"${mode}",lineNumbers:true, matchBrackets:true, readOnly:${jcr:isNodeType(currentNode, 'jnt:resourceBundleFile') or locked}});
    myCodeMirror.setSize("100%","100%");
    myCodeMirror.on("change", function() {
        if ($('#saveButton').prop('disabled')) {
            $.get("<c:url value="${url.base}${currentNode.path}.lock.do?type=editSource"/>");
        }
        $('#saveButton').prop('disabled', false);
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
                    $('#saveButton').prop('disabled', true);
                    $.get("<c:url value="${url.base}${currentNode.path}.unlock.do?type=editSource"/>");
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    $('#saveButton').prop('disabled', true);
                    error = "${saveError}";
                    if (textStatus == 'error') {
                        error = $.parseJSON(jqXHR.responseText)['error']
                    }
                    alert(error);
                },
                dataType: 'json'
            });
        }
    }
    $(window).blur(function() {
        saveSourceCode();
    });
    $(window).onUnload(function() {
        $.get("<c:url value="${url.base}${currentNode.path}.unLock.do?type=editSource"/>");
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
</script>
