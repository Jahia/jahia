<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery.validate.js,jquery.maskedinput-1.2.2.js"/>
<template:addResources type="css" resources="formbuilder.css"/>
<jcr:node var="actionNode" path="${currentNode.path}/action"/>
<jcr:node var="fieldsetsNode" path="${currentNode.path}/fieldsets"/>
<c:if test="${not renderContext.editMode}">
    <script>
        $(document).ready(function() {
            $("#${currentNode.name}").validate({
                rules: {
                    <c:forEach items="${fieldsetsNode.nodes}" var="fieldset">
                    <c:forEach items="${jcr:getNodes(fieldset,'jnt:formElement')}" var="formElement" varStatus="status">
                    <c:set var="validations" value="${jcr:getNodes(formElement,'jnt:formElementValidation')}"/>
                    <c:if test="${fn:length(validations) > 0}">
                    ${formElement.name} : {
                        <c:forEach items="${jcr:getNodes(formElement,'jnt:formElementValidation')}" var="formElementValidation" varStatus="val">
                        <template:module node="${formElementValidation}" template="default" editable="true"/><c:if test="${not val.last}">,</c:if>
                        </c:forEach>
                    }<c:if test="${not status.last}">,</c:if>
                    </c:if>
                    </c:forEach>
                    </c:forEach>
                },formId : "${currentNode.name}"
            });
        });
    </script>
</c:if>

<c:set var="action" value="${url.base}${currentNode.path}/*"/>
<c:if test="${not empty actionNode.nodes}">
    <c:forEach items="${actionNode.nodes}" var="node">
        <c:if test="${node.properties['j:action'].string != 'default'}">
            <c:set var="action" value="${url.base}${currentNode.path}.${node.properties['j:action'].string}.do"/>
        </c:if>
    </c:forEach>
</c:if>

<h2><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h2>


<div class="intro">
    ${currentNode.propertiesAsString['j:intro']}
</div>
<c:if test="${renderContext.editMode}">
    <div class="addaction">
        <span>Add your action here</span>
        <c:set var="editable" value="${not empty actionNode.nodes}"/>
        <template:area path="${currentNode.path}/action" nodeTypes="jnt:formAction" editable="${editable}"/>
    </div>
    </c:if>
<div class="Form FormBuilder">
    <c:if test="${not renderContext.editMode}">
    <form action="${action}" method="post" id="${currentNode.name}">
        </c:if>
        <input type="hidden" name="nodeType" value="jnt:responseToForm"/>
        <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
        <%-- Define the output format for the newly created node by default html or by redirectTo--%>
        <input type="hidden" name="newNodeOutputFormat" value="html"/>
        <c:if test="${renderContext.editMode}">
        <div class="addfieldsets">
            <span>Add your new fieldsets here</span>
            </c:if>
            <template:area path="${currentNode.path}/fieldsets" nodeTypes="jnt:fieldset" editable="true"/>
            <c:if test="${renderContext.editMode}">
        </div>
        </c:if>
        <div class="<c:if test="${not renderContext.editMode}">divButton</c:if><c:if test="${renderContext.editMode}">addbuttons</c:if>">
            <c:if test="${renderContext.editMode}">
            <span>Add your new form buttons here</span>
            </c:if>
            <template:area path="${currentNode.path}/formButtons" nodeTypes="jnt:formButton" editable="true"/>
        </div>
            
        <c:if test="${not renderContext.editMode}">
        <div class="validation"></div>
    </form>
    </c:if>
</div>
<br/><br/>

<div>
    <h2>Responses</h2>
    <c:forEach items="${jcr:getNodes(currentNode,'jnt:responseToForm')}" var="response">
        <div>
            <c:forEach items="${fieldsetsNode.nodes}" var="fieldset">
                <c:forEach
                        items="${jcr:getPropertiesAsStringFromNodeNameOfThatType(response,fieldset,'jnt:formElement')}"
                        var="entry">
                    <jcr:node var="def" path="${fieldset.path}/${entry.key}"/>
                    <c:if test="${jcr:isNodeType(def, 'jnt:automaticList')}">
                        <jcr:nodeProperty node="${def}" name="type" var="type"/>
                        <c:set var="renderers" value="${fn:split(type.string,'=')}"/>
                        <c:if test="${fn:length(renderers) > 1}"><c:set var="renderer" value="${renderers[1]}"/></c:if>
                        <c:if test="${not (fn:length(renderers) > 1)}"><c:set var="renderer" value=""/></c:if>
                        <p><label>${entry.key}</label>&nbsp;<span>Value:<jcr:nodePropertyRenderer node="${response}"
                                                                                                  name="${entry.key}"
                                                                                                  renderer="${renderer}"/></span>
                        </p>
                    </c:if>
                    <c:if test="${not jcr:isNodeType(def, 'jnt:automaticList')}">
                        <p>
                            <label>${entry.key}</label>&nbsp;<span>Value : ${entry.value}</span>
                        </p>
                    </c:if>
                </c:forEach>
            </c:forEach>
        </div>
    </c:forEach>
</div>

