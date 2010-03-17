<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="prop" type="org.jahia.services.content.JCRPropertyWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="960.css"/>
<template:addResources type="css" resources="datepicker.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/ckeditor/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="datepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>

<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType"
                      scope="application"/>
<utility:useConstants var="selectorType" className="org.jahia.services.content.nodetypes.SelectorType"
                      scope="application"/>
<div>
    <c:set var="type" value="${currentNode.primaryNodeType}"/>
    <c:forEach items="${type.propertyDefinitions}" var="propertyDefinition">
        <c:if test="${!propertyDefinition.multiple and propertyDefinition.contentItem}">
            <c:set var="prop" value="${currentNode.properties[propertyDefinition.name]}"/>
            <c:set var="scriptPropName" value="${fn:replace(propertyDefinition.name,':','_')}"/>
            <p>
            <span class="label">${jcr:labelForLocale(propertyDefinition,renderContext.mainResourceLocale)}&nbsp;:</span>
            <c:choose>
                <c:when test="${(propertyDefinition.requiredType == jcrPropertyTypes.REFERENCE || propertyDefinition.requiredType == jcrPropertyTypes.WEAKREFERENCE)}">
                    <c:if test="${propertyDefinition.selector eq selectorType.FILEUPLOAD or propertyDefinition.selector eq selectorType.FILEPICKER}">
                        <script>
                            $(document).ready(function() {
                                $("#file${currentNode.name}${scriptPropName}").editable('${url.base}${currentNode.path}', {
                                    type : 'ajaxupload',
                                    onblur : 'ignore',
                                    submit : 'OK',
                                    cancel : 'Cancel',
                                    tooltip : 'Click to edit',
                                    callback : function (data, status) {
                                        var datas = {};
                                        datas['${propertyDefinition.name}'] = data.uuids[0];
                                        datas['methodToCall'] = 'put';
                                        $.post('${url.base}${currentNode.path}', datas, function(result) {
                                            $("#file${currentNode.name}${scriptPropName}").html($('<span>file uploaded</span>'));
                                        }, "json");
                                    }
                                });
                            })
                        </script>
                        <div id="file${currentNode.name}${scriptPropName}">
                            <span>add a file (file will be uploaded in your files directory before submitting the form)</span>
                        </div>
                        <template:module node="${prop.node}" template="default" templateType="html"/>
                    </c:if>
                </c:when>
                <c:when test="${propertyDefinition.requiredType == jcrPropertyTypes.DATE}">
                    <c:set var="dateTimePicker"
                           value="${propertyDefinition.selector eq selectorType.DATETIMEPICKER}"/>
                        <span jcr:id="${propertyDefinition.name}" class="dateEdit"
                              jcr:url="${url.base}${currentNode.path}">
                            <c:if test="${not empty prop}">
                                <fmt:formatDate value="${prop.date.time}" pattern="dd, MMMM yyyy HH:mm"/>
                            </c:if>
                        </span>
                </c:when>
                <c:when test="${propertyDefinition.selector eq selectorType.CHOICELIST}">
                    <jcr:propertyInitializers var="options" nodeType="${type.name}"
                                              name="${propertyDefinition.name}"/>
                    <script>
                        var ${scriptPropName}Map = "{<c:forEach items="${options}" varStatus="status" var="option"><c:if test="${status.index > 0}">,</c:if>'${option.value.string}':'${option.displayName}'</c:forEach>}";
                        $(document).ready(function() {
                            $(".choicelistEdit${currentNode.name}${scriptPropName}").editable(function (value, settings) {
                                var url = $(this).attr('jcr:url');
                                var submitId = $(this).attr('jcr:id').replace("_", ":");
                                var data = {};
                                data[submitId] = value;
                                data['methodToCall'] = 'put';
                                $.post(url, data, null, "json");
                                return eval("values=" + ${scriptPropName}Map)[value];
                            }, {
                                type    : 'select',
                                data   : ${scriptPropName}Map,
                                onblur : 'ignore',
                                submit : 'OK',
                                cancel : 'Cancel',
                                tooltip : 'Click to edit'
                            });
                        });
                    </script>
                        <span jcr:id="${propertyDefinition.name}" class="choicelistEdit${currentNode.name}${scriptPropName}"
                              jcr:url="${url.base}${currentNode.path}">${prop.string}</span>
                </c:when>
                <c:when test="${propertyDefinition.selector eq selectorType.RICHTEXT}">
                        <span jcr:id="${propertyDefinition.name}" class="ckeditorEdit"
                              jcr:url="${url.base}${currentNode.path}">${prop.string}</span>
                </c:when>
                <c:otherwise>
                        <span jcr:id="${propertyDefinition.name}" class="edit"
                              jcr:url="${url.base}${currentNode.path}">${prop.string}</span>
                </c:otherwise>
            </c:choose>
            </p>
        </c:if>
    </c:forEach>
</div>
