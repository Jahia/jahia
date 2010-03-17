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

<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType"
                      scope="application"/>
<utility:useConstants var="selectorType" className="org.jahia.services.content.nodetypes.SelectorType"
                      scope="application"/>
<script type="text/javascript">
    $(document).ready(function() {
        $(".edit").editable(function (value, settings) {
            var url = $(this).attr('jcr:url');
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post(url, data, null, "json");
            return(value);
        }, {
            type    : 'text',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit'
        });

        $(".ckeditorEdit").editable(function (value, settings) {
            var url = $(this).attr('jcr:url');
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post(url, data, null, "json");
            return(value);
        }, {
            type : 'ckeditor',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit'
        });

        $(".dateEdit").editable(function (value, settings) {
            var url = $(this).attr('jcr:url');
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            if (value.match("[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9]")) {
                var split = value.split("/");
                var birth = new Date();
                birth.setFullYear(split[2], split[1], split[0]);
                var month = "";
                if (birth.getMonth() < 10) {
                    month = "0" + birth.getMonth();
                } else month = birth.getMonth();
                var hour = '00';
                if($("#hour"+submitId).length) {
                    hour=$("#hour"+submitId).text().trim();
                }
                var min = '00';
                if($("#min"+submitId).length) {
                    min=$("#min"+submitId).text().trim();
                }
                data[submitId] = birth.getFullYear() + '-' + month + '-' + birth.getDate() + 'T'+hour+':'+min+':00';
                data['methodToCall'] = 'put';
                $.post(url, data, function(result) {
                }, "json");
            }
            return(value);
        }, {
            type : 'datepicker',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit',
            datepicker : {
                flat: true,
                date: '${not empty editBirthDate ? editBirthDate : editNowDate}',
                format: 'd/m/Y',
                view: 'years',
                current: '${not empty editBirthDate ? editBirthDate : editNowDate}',
                calendars: 1,
                starts: 1
            }
        });
    });
</script>
<div>
    <c:set var="type" value="${currentNode.primaryNodeType}"/>
    <c:forEach items="${type.propertyDefinitions}" var="propertyDefinition">
        <c:if test="${!propertyDefinition.multiple and propertyDefinition.contentItem}">
            <c:set var="prop" value="${currentNode.properties[propertyDefinition.name]}"/>
            <p>
                <span class="label">${jcr:labelForLocale(propertyDefinition,renderContext.mainResourceLocale)}&nbsp;:</span>
                <c:choose>
                    <c:when test="${(propertyDefinition.requiredType == jcrPropertyTypes.REFERENCE || propertyDefinition.requiredType == jcrPropertyTypes.WEAKREFERENCE)}">
                        <c:if test="${propertyDefinition.selector eq selectorType.FILEUPLOAD or propertyDefinition.selector eq selectorType.FILEPICKER}">
                        </c:if>
                    </c:when>
                    <c:when test="${propertyDefinition.requiredType == jcrPropertyTypes.DATE}">
                        <c:set var="dateTimePicker" value="${propertyDefinition.selector eq selectorType.DATETIMEPICKER}"/>
                        <span id="${fn:replace(propertyDefinition.name,':','_')}" class="dateEdit"
                              jcr:url="${url.base}${currentNode.path}">
                            <c:if test="${not empty prop}">
                                <fmt:formatDate value="${prop.date.time}" pattern="dd, MMMM yyyy"/>
                            </c:if>
                        </span>
                        <c:if test="${dateTimePicker}">
                        <span>
                            <c:if test="${not empty prop}">
                                <fmt:formatDate value="${prop.date.time}" pattern="HH:mm"/>
                            </c:if>
                        </span>
                        </c:if>
                    </c:when>
                    <c:when test="${propertyDefinition.selector eq selectorType.CHOICELIST}">
                    </c:when>
                    <c:when test="${propertyDefinition.selector eq selectorType.RICHTEXT}">
                        <span id="${fn:replace(propertyDefinition.name,':','_')}" class="ckeditorEdit"
                              jcr:url="${url.base}${currentNode.path}">${prop.string}</span>
                    </c:when>
                    <c:otherwise>
                        <span id="${fn:replace(propertyDefinition.name,':','_')}" class="edit"
                              jcr:url="${url.base}${currentNode.path}">${prop.string}</span>
                    </c:otherwise>
                </c:choose>
            </p>
        </c:if>
    </c:forEach>
</div>
<hr/>
