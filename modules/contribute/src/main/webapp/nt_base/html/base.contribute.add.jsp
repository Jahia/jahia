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
<%--
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="css" resources="formcontribute.css"/>
--%>
<template:addResources type="css" resources="contribute.min.css"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType"
                      scope="application"/>
<utility:useConstants var="selectorType" className="org.jahia.services.content.nodetypes.SelectorType"
                      scope="application"/>
<utility:setBundle basename="JahiaContributeMode" useUILocale="true"/>
<c:set var="resourceNodeType" value="${currentResource.moduleParams.resourceNodeType}"/>
<c:if test="${empty resourceNodeType}">
    <c:set var="resourceNodeType" value="${param.resourceNodeType}"/>
</c:if>
<jcr:nodeType name="${resourceNodeType}" var="type"/>
<c:set var="scriptTypeName" value="${fn:replace(type.name,':','_')}"/>
<div class="FormContribute">
    <c:url var="formAction" value="${url.base}${currentNode.path}/*"/>
    <c:set var="jsNodeName" value="${fn:replace(fn:replace(currentNode.name,'-','_'),'.','_')}"/>
    <c:if test="${!(resourceNodeType eq 'jnt:file' || resourceNodeType eq 'jnt:folder')}">
        <c:set var="formID">
            id="${jsNodeName}${scriptTypeName}"
        </c:set>
    </c:if>
    <c:if test="${resourceNodeType eq 'jnt:file'}">
        <c:set var="enctype">
            enctype="multipart/form-data"
        </c:set>
    </c:if>

    <form action="${formAction}" method="post" ${formID} ${enctype}>
        <input type="hidden" name="jcrNodeType" value="${type.name}"/>
        <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>"/>
        <%-- Define the output format for the newly created node by default html or by redirectTo--%>
        <input type="hidden" name="jcrNewNodeOutputFormat" value="html"/>
        <input type="hidden" name="jcrNormalizeNodeName" value="true"/>
        <fieldset>
            <legend>${jcr:label(type,renderContext.UILocale)}</legend>
            <label class="left" for="JCRnodeName"><fmt:message key="label.name"/></label>
            <input type="text" id="JCRnodeName" name="jcrNodeName"/>
            <c:forEach items="${type.propertyDefinitions}" var="propertyDefinition">
                <c:if test="${propertyDefinition.name eq 'jcr:title'}">
                    <label class="left"
                           for="${fn:replace(propertyDefinition.name,':','_')}">${jcr:labelInNodeType(propertyDefinition,renderContext.UILocale,type)}</label>
                    <input type="text" id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                           name="${propertyDefinition.name}"/>
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
                                       for="${fn:replace(propertyDefinition.name,':','_')}">${jcr:labelInNodeType(propertyDefinition,renderContext.UILocale,type)}</label>
                                <input type="radio" value="true" class="radio"
                                       id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                                       name="${propertyDefinition.name}" checked="true"/><fmt:message key="label.yes"/>
                                <input type="radio" value="false" class="radio"
                                       id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                                       name="${propertyDefinition.name}"/><fmt:message key="label.no"/>
                            </c:when>
                            <c:otherwise>
                                <label class="left"
                                       for="${fn:replace(propertyDefinition.name,':','_')}">${jcr:labelInNodeType(propertyDefinition,renderContext.UILocale,type)}</label>
                                <input type="text" id="${scriptTypeName}${fn:replace(propertyDefinition.name,':','_')}"
                                       name="${propertyDefinition.name}"/>
                            </c:otherwise>
                        </c:choose>
                    </p>
                </c:if>
            </c:forEach>
            <c:if test="${resourceNodeType eq 'jnt:folder'}">
                <p class="field"><label class="left"
                                        for="${scriptTypeName}jnt_folder">${jcr:label('jnt:folder',renderContext.UILocale)}</label>
                    <input type="text" id="${scriptTypeName}jnt_folder" name="jcrNodeName"/>
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
                           for="${scriptTypeName}jnt_folder">${jcr:label('jnt:folder',renderContext.UILocale)}</label>
                    <input type="hidden" name="jcrTargetDirectory" value="${currentNode.path}"/>
                    <input type="file" name="file"/>

                </p>
            </c:if>
            <div class="divButton">
                <button type="button" class="form-button" onclick="if (!checkWCAGCompliace($('textarea.newContentCkeditorContribute'))) return false; $('.form-button').attr('disabled',true);$('.form-button').addClass('disabled'); $('#${jsNodeName}${scriptTypeName}').ajaxSubmit(options${jsNodeName}${scriptTypeName});"><span class="icon-contribute icon-accept"></span><fmt:message
                        key="label.add.new.content.submit"/></button>
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
            clearForm: true,
            error: function(jqXHR, textStatus, errorThrown) {
 		       alert('<fmt:message key="failure.invalid.constraint.nohighlight.label"/>');
                $('.form-button').attr('disabled',false);
                $('.form-button').removeClass('disabled');
            }
        };// wait for the DOM to be loaded
    </script>
</div>
