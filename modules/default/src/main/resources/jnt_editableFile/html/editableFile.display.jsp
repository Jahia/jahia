<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="javascript" resources="codemirror/lib/codemirror.js,codemirror/mode/xml/xml.js,codemirror/mode/htmlmixed/htmlmixed.js,codemirror/mode/javascript/javascript.js,codemirror/mode/clike/clike.js,codemirror/mode/css/css.js,codemirror/mode/htmlembedded/htmlembedded.js,codemirror.mode.jsp.js"/>
<template:addResources type="css" resources="codemirror/codemirror.css"/>
<span><fmt:message key="label.source"/> : ${currentNode.name}</span>

<textarea id="sourceCode" editable="false"><c:out value="${currentNode.properties.sourceCode.string}" escapeXml="true"/></textarea>
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

<script type="text/javascript">
    var myCodeMirror = CodeMirror.fromTextArea(document.getElementById("sourceCode"),{mode:"${mode}",lineNumbers:true, matchBrackets:true, readOnly:true});
    myCodeMirror.setSize("100%","100%");

</script>
