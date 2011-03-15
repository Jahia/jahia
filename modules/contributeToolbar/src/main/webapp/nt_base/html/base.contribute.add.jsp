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
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="css" resources="formcontribute.css"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType"
                      scope="application"/>
<utility:useConstants var="selectorType" className="org.jahia.services.content.nodetypes.SelectorType"
                      scope="application"/>
<utility:setBundle basename="JahiaContributeToolbar" useUILocale="true"/>
<c:set var="resourceNodeType" value="${currentResource.moduleParams.resourceNodeType}"/>
<c:if test="${empty resourceNodeType}">
    <c:set var="resourceNodeType" value="${param.resourceNodeType}"/>
</c:if>
<jcr:nodeType name="${resourceNodeType}" var="type"/>
<c:set var="scriptTypeName" value="${fn:replace(type.name,':','_')}"/>
<div class="FormContribute">
    <c:choose>
        <c:when test="${not empty currentResource.moduleParams.workflowStartForm}">
            <c:set var="formAction" value="${url.base}${currentNode.path}.startWorkflow.do"/>
        </c:when>
        <c:when test="${not empty currentResource.moduleParams.workflowTaskForm}">
            <c:set var="formAction" value="${url.base}${currentNode.path}.executeTask.do"/>
        </c:when>
        <c:otherwise>
            <c:set var="formAction" value="${url.base}${currentNode.path}/*"/>
        </c:otherwise>
    </c:choose>
    <c:set var="jsNodeName" value="${fn:replace(fn:replace(currentNode.name,'-','_'),'.','_')}"/>
    <form action="<c:url value='${formAction}'/>" method="post"
    <c:if test="${!(resourceNodeType eq 'jnt:file' || resourceNodeType eq 'jnt:folder')}">
          id="${jsNodeName}${scriptTypeName}"
    </c:if>
    <c:if test="${resourceNodeType eq 'jnt:file'}">
        enctype="multipart/form-data"
    </c:if>
            >
        <c:choose>
            <c:when test="${not empty currentResource.moduleParams.workflowStartForm}">
                <input type="hidden" name="process" value="${currentResource.moduleParams.workflowStartForm}"/>
            </c:when>
            <c:when test="${not empty currentResource.moduleParams.workflowTaskForm}">
                <input type="hidden" name="action" value="${currentResource.moduleParams.workflowTaskForm}"/>
                <input type="hidden" name="outcome" id="outcome"/>
            </c:when>
            <c:otherwise>
                <input type="hidden" name="nodeType" value="${type.name}"/>
                <input type="hidden" name="redirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>"/>
                <%-- Define the output format for the newly created node by default html or by redirectTo--%>
                <input type="hidden" name="newNodeOutputFormat" value="html"/>
            </c:otherwise>
        </c:choose>
        <fieldset>
            <legend>${jcr:label(type,renderContext.mainResourceLocale)}</legend>
            <c:forEach items="${type.propertyDefinitions}" var="propertyDefinition">
                <c:if test="${propertyDefinition.name eq 'jcr:title'}">
                    <label class="left"
                           for="${fn:replace(propertyDefinition.name,':','_')}">${jcr:labelInNodeType(propertyDefinition,renderContext.mainResourceLocale,type)}</label>
                    <input type="text" id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                           name="${propertyDefinition.name}" <c:if test="${not empty workflowTaskFormTask}">value="${workflowTaskFormTask.variables[propertyDefinition.name][0].value}"</c:if>/>

                </c:if>
            </c:forEach>
            <c:forEach items="${type.propertyDefinitions}" var="propertyDefinition">
                <c:if test="${!propertyDefinition.multiple and propertyDefinition.contentItem and !(propertyDefinition.name eq 'jcr:title')}">
                    <p class="field">
                        <c:choose>
                            <c:when test="${(propertyDefinition.requiredType == jcrPropertyTypes.REFERENCE || propertyDefinition.requiredType == jcrPropertyTypes.WEAKREFERENCE)}">
                                <c:choose>
                                <c:when test="${propertyDefinition.selector eq selectorType.FILEUPLOAD or propertyDefinition.selector eq selectorType.CONTENTPICKER}">
                                    <%@include file="formelements/file.jsp" %>
                                </c:when>
                                <c:when test="${propertyDefinition.selector eq selectorType.CHOICELIST}">
                                    <%@include file="formelements/select.jsp" %>
                                </c:when>
                                    <c:otherwise>
                                        <%@include file="formelements/reference.jsp" %>
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:when test="${propertyDefinition.requiredType == jcrPropertyTypes.DATE}">
                                <%@include file="formelements/datepicker.jsp" %>
                            </c:when>
                            <c:when test="${propertyDefinition.selector eq selectorType.CHOICELIST}">
                                <%@include file="formelements/select.jsp" %>
                            </c:when>
                            <c:when test="${propertyDefinition.selector eq selectorType.RICHTEXT}">
                                <%@include file="formelements/richtext.jsp" %>
                            </c:when>
                            <c:when test="${propertyDefinition.requiredType == jcrPropertyTypes.BOOLEAN}">
                                <label class="left"
                                       for="${fn:replace(propertyDefinition.name,':','_')}">${jcr:labelInNodeType(propertyDefinition,renderContext.mainResourceLocale,type)}</label>
                                <input type="radio" value="true" class="radio"
                                       id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                                       name="${propertyDefinition.name}" checked="true"/><fmt:message key="label.yes"/>
                                <input type="radio" value="false" class="radio"
                                       id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                                       name="${propertyDefinition.name}"/><fmt:message key="label.no"/>
                            </c:when>
                            <c:otherwise>
                                <label class="left"
                                       for="${fn:replace(propertyDefinition.name,':','_')}">${jcr:labelInNodeType(propertyDefinition,renderContext.mainResourceLocale,type)}</label>
                                <input type="text" id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                                       name="${propertyDefinition.name}" <c:if test="${not empty workflowTaskFormTask}">value="${workflowTaskFormTask.variables[propertyDefinition.name][0].value}"</c:if>/>
                            </c:otherwise>
                        </c:choose>
                    </p>
                </c:if>
            </c:forEach>
            <c:if test="${resourceNodeType eq 'jnt:folder'}">
                <p class="field"><label class="left"
                                        for="${scriptTypeName}jnt_folder">${jcr:label('jnt:folder',renderContext.mainResourceLocale)}</label>
                    <input type="text" id="${scriptTypeName}jnt_folder" name="JCRnodeName"/>
                    <c:if test="${currentResource.properties['j:editableInContribution'].boolean}">
                    <input type="hidden" name="jcr:mixinTypes" value="jmix:contributeMode"/>
                    <input type="hidden" name="j:editableInContribution" value="true"/>
                    <input type="hidden" name="j:canDeleteInContribution" value="true"/>
                    <input type="hidden" name="j:canOrderInContribution" value="true"/>
                    </c:if>

                </p>
            </c:if>
            <c:if test="${resourceNodeType eq 'jnt:file'}">
                <p class="field">
                    <label class="left"
                                        for="${scriptTypeName}jnt_folder">${jcr:label('jnt:folder',renderContext.mainResourceLocale)}</label>
                    <input type="hidden" name="targetDirectory" value="${currentNode.path}"/>
                    <input type="file" name="file"/>

                </p>
            </c:if>
            <div class="divButton">
                <c:choose>
                    <c:when test="${not empty currentResource.moduleParams.workflowStartForm}">
                        <button type="button" class="form-button" onclick="$('.form-button').attr('disabled',true);$('.form-button').addClass('disabled');$('#${jsNodeName}${scriptTypeName}').ajaxSubmit(options${jsNodeName}${scriptTypeName});"><span
                                class="icon-contribute icon-accept"></span>Start:&nbsp;${currentResource.moduleParams.workflowStartFormWFName}
                        </button>
                    </c:when>
                    <c:when test="${not empty currentResource.moduleParams.workflowTaskForm}">
                        <c:forEach items="${workflowTaskFormTask.outcomes}" var="outcome" varStatus="status">
                            <button type="button" class="form-button"
                                    onclick="$('#outcome').val('${outcome}');$('.form-button').attr('disabled',true);$('.form-button').addClass('disabled');$('#${jsNodeName}${scriptTypeName}').ajaxSubmit(options${jsNodeName}${scriptTypeName});"><span
                                    class="icon-contribute icon-accept"></span>&nbsp;${workflowTaskFormTask.displayOutcomes[status.index]}
                            </button>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <button type="button" class="form-button" onclick="if (!checkWCAGCompliace($('textarea.newContentCkeditorContribute'))) return false; $('.form-button').attr('disabled',true);$('.form-button').addClass('disabled'); $('#${jsNodeName}${scriptTypeName}').ajaxSubmit(options${jsNodeName}${scriptTypeName});"><span class="icon-contribute icon-accept"></span><fmt:message
                                key="label.add.new.content.submit"/></button>
                    </c:otherwise>
                </c:choose>

                <button type="reset"  class="form-button"><span class="icon-contribute icon-cancel"></span><fmt:message
                        key="label.add.new.content.reset"/></button>
            </div>
        </fieldset>
    </form>
        <script type="text/javascript">
            var options${jsNodeName}${scriptTypeName} = {
                success: function() {
                    window.location.reload();
                },
                dataType: "json",
                clearForm: true
            };// wait for the DOM to be loaded
        </script>
</div>
