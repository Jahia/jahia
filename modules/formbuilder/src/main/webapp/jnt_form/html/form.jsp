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
<template:addResources type="javascript" resources="jquery.js,jquery.validate.js,jquery.maskedinput-1.2.2.js"/>
<template:addResources type="css" resources="formbuilder.css"/>
<jcr:node var="actionNode" path="${currentNode.path}/action"/>
<jcr:node var="fieldsetsNode" path="${currentNode.path}/fieldsets"/>
<c:if test="${not renderContext.editMode}">
    <template:addResources>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#${currentNode.name}").validate({
                rules: {
                    <c:forEach items="${fieldsetsNode.nodes}" var="fieldset">
                    <c:forEach items="${jcr:getNodes(fieldset,'jnt:formElement')}" var="formElement" varStatus="status">
                    <c:set var="validations" value="${jcr:getNodes(formElement,'jnt:formElementValidation')}"/>
                    <c:if test="${fn:length(validations) > 0}">
                    '${formElement.name}' : {
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
    </template:addResources>
</c:if>

<c:set var="action" value="${url.base}${currentNode.path}/responses/*"/>
<c:if test="${not empty actionNode.nodes}">
    <c:if test="${fn:length(actionNode.nodes) > 1}">
        <c:set var="action" value="${url.base}${currentNode.path}/responses.chain.do"/>
        <c:set var="chainActive" value=""/>
        <c:forEach items="${actionNode.nodes}" var="node" varStatus="stat">
            <c:set var="chainActive" value="${chainActive}${node.properties['j:action'].string}"/>
            <c:if test="${not stat.last}"><c:set var="chainActive" value="${chainActive},"/></c:if>
        </c:forEach>
    </c:if>
    <c:if test="${fn:length(actionNode.nodes) eq 1}">
        <c:forEach items="${actionNode.nodes}" var="node">
            <c:if test="${node.properties['j:action'].string != 'default'}">
                <c:set var="action" value="${url.base}${currentNode.path}/responses.${node.properties['j:action'].string}.do"/>
            </c:if>
        </c:forEach>
    </c:if>
</c:if>

<h2><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h2>


<div class="intro">
    ${currentNode.propertiesAsString['j:intro']}
</div>
<c:if test="${renderContext.editMode}">
    <div class="addaction">
        <span><fmt:message key="form.addAction"/> </span>
        <c:set var="editable" value="${not empty actionNode.nodes}"/>
        <template:list path="action" listType="jnt:actionList" editable="true"/>
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
        <c:if test="${not empty chainActive}">
            <input type="hidden" name="chainOfAction" value="${chainActive}"/>
        </c:if>
        <c:if test="${renderContext.editMode}">
        <div class="addfieldsets">
            <span><fmt:message key="form.addFieldSet"/></span>
            </c:if>
            <template:list path="fieldsets" listType="jnt:fieldsetstList" editable="true"/>
            <c:if test="${renderContext.editMode}">
        </div>
        </c:if>
        <div class="<c:if test="${not renderContext.editMode}">divButton</c:if><c:if test="${renderContext.editMode}">addbuttons</c:if>">
            <c:if test="${renderContext.editMode}">
                <span><fmt:message key="form.addButtons"/></span>
            </c:if>
            <template:list path="formButtons" listType="jnt:formButtonsList" editable="true"/>
        </div>

        <c:if test="${not renderContext.editMode}">
        <div class="validation"></div>
    </form>
    </c:if>
</div>
<br/><br/>

<div>
    <h2><fmt:message key="form.responses"/> (<a href="<c:url value='${currentNode.path}.csv' context='${url.base}'/>" target="_blank">CSV</a>)</h2>
    <template:list path="responses" listType="jnt:responsesList" editable="true" />
</div>

