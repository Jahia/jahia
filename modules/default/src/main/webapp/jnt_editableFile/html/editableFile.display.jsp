<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="javascript" resources="codemirror/lib/codemirror.js,codemirror/mode/xml/xml.js,codemirror/mode/htmlmixed/htmlmixed.js,codemirror/mode/javascript/javascript.js,codemirror/mode/javascript/javascript.js"/>
<template:addResources type="css" resources="codemirror/codemirror.css"/>
<span><fmt:message key="label.source"/> : ${currentNode.name}</span>

<textarea id="sourceCode" editable="false"><c:out value="${currentNode.properties.sourceCode.string}" escapeXml="true"/></textarea>

<script type="text/javascript">
    var myCodeMirror = CodeMirror.fromTextArea(document.getElementById("sourceCode"),{mode:"${fn:endsWith(currentNode.name,".css")?".css":fn:endsWith(currentNode.name,".js")?"javascript":"htmlmixed"}",lineNumbers:true, matchBrackets:true, readOnly:true});
    myCodeMirror.setSize("100%","100%");

</script>
