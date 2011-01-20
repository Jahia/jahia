<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<c:set var="user" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<c:if test="${renderContext.editMode}">
    <fmt:message key="${fn:replace(currentNode.primaryNodeTypeName,':','_')}"/>
    <template:linker property="j:bindedComponent"/>
</c:if>

<%--<c:if test="${not jcr:isNodeType(user, 'jnt:user')}">--%>
<%--<jcr:node var="user" path="/users/${user.properties['jcr:createdBy'].string}"/>--%>
<%--</c:if>--%>
<c:if test="${empty user or not jcr:isNodeType(user, 'jnt:user')}">
    <jcr:node var="user" path="/users/${renderContext.user.username}"/>
</c:if>

<template:addResources type="css" resources="userProfile.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery-ui.datepicker.min.js,jquery.jeditable.datepicker.js"/>

<c:set var="fields" value="${user.propertiesAsString}"/>
<jcr:nodePropertyRenderer node="${user}" name="j:title" renderer="resourceBundle" var="title"/>
<c:if test="${not empty title and not empty fields['j:firstName'] and not empty fields['j:lastName']}">
    <c:set var="person" value="${title.displayName} ${fields['j:firstName']} ${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and not empty fields['j:firstName'] and not empty fields['j:lastName']}">
    <c:set var="person" value="${fields['j:firstName']} ${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and empty fields['j:firstName'] and not empty fields['j:lastName']}">
    <c:set var="person" value="${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and not empty fields['j:firstName'] and empty fields['j:lastName']}">
    <c:set var="person" value="${fields['j:firstName']}"/>
</c:if>
<c:if test="${empty title and empty fields['j:firstName'] and empty fields['j:lastName']}">
    <c:set var="person" value=""/>
</c:if>
<jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
<c:if test="${not empty birthDate}">
    <fmt:formatDate value="${birthDate.date.time}" pattern="yyyy" var="birthYear"/>
    <fmt:formatDate value="${now}" pattern="yyyy" var="currentYear"/>
</c:if>
<c:if test="${not empty birthDate}">
    <fmt:formatDate value="${birthDate.date.time}" pattern="dd/MM/yyyy" var="editBirthDate"/>
</c:if>
<fmt:formatDate value="${now}" pattern="dd/MM/yyyy" var="editNowDate"/>
<jcr:propertyInitializers node="${user}" name="j:gender" var="genderInit"/>
<jcr:propertyInitializers node="${user}" name="j:title" var="titleInit"/>
<script>

    var genderMap = "{<c:forEach items="${genderInit}" varStatus="status" var="gender"><c:if test="${status.index > 0}">,</c:if>'${gender.value.string}':'${gender.displayName}'</c:forEach>}";
    var titleMap = "{<c:forEach items="${titleInit}" varStatus="status" var="title"><c:if test="${status.index > 0}">,</c:if>'${title.value.string}':'${title.displayName}'</c:forEach>}";

    $(document).ready(function() {
        $(".edit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.basePreview}${user.path}", data, function(result) {
                var j_title = "";
                if (result && typeof result.j_title != 'undefined')
                    j_title = eval("datas=" + titleMap)[result.j_title];
                var j_firstname = "";
                if (result && typeof result.j_firstName != 'undefined')
                    j_firstname = result.j_firstName;
                var j_lastname = "";
                if (result && typeof result.j_lastName != 'undefined')
                    j_lastname = result.j_lastName;
                $("#personDisplay2").html(j_title + " " + j_firstname + " " + j_lastname);
                $("#personDisplay1").html(j_title + " " + j_firstname + " " + j_lastname);
                if (result && result.j_email != 'undefined')
                    $("#emailDisplay").html(result.j_email);
            }, "json");
            return(value);
        }, {
            type    : 'text',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>'
        });

        $(".imageEdit").editable('${url.basePreview}${user.path}', {
            type : 'ajaxupload',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>',
            callback : function (data, status) {
                uploadedImageCallback(data, status);
            }
        });

        function uploadedImageCallback(data, status) {
            var datas = {};
            datas['j:picture'] = data.uuids[0];
            datas['methodToCall'] = 'put';
            $.post('${url.basePreview}${user.path}', datas, function(result) {
                var input = $('<div class="itemImage itemImageRight"><img src="' + result.j_picture + '/avatar_120" /></div>');
                $("#portrait").html(input);
            }, "json");
        }

        $(".ckeditorEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.basePreview}${user.path}", data, function(result) {
            }, "json");
            return(value);
        }, {
            type : 'ckeditor',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>'
        });

        $(".dateEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.basePreview}${user.path}", data, function(result) {
            }, "json");
            return(value);
        }, {
            type : 'datepicker',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>'
        });

        $(".genderEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.basePreview}${user.path}", data, null, "json");
            return eval("values=" + genderMap)[value];
        }, {
            type    : 'select',
            data   : genderMap,
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>'
        });

        $(".titleEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.basePreview}${user.path}", data, function(result) {
                var j_title = result.j_title;
                j_title = eval("datas=" + titleMap)[j_title];
                $("#personDisplay2").html(j_title + " " + result.j_firstName + " " + result.j_lastName);
                $("#personDisplay1").html(j_title + " " + result.j_firstName + " " + result.j_lastName);
                $("#emailDisplay").html(result.j_email);
            }, "json");
            return eval("values=" + titleMap)[value];
        }, {
            type    : 'select',
            data   : titleMap,
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>'
        });

        $(".prefEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.basePreview}${currentNode.path}", data, null, "json");
            <c:forEach items='${functions:availableAdminBundleLocale(renderContext.mainResourceLocale)}' var="adLocale" varStatus="status">
            <c:choose>
            <c:when test="${status.first}">
            if (value=="${adLocale}") return "${functions:capitalize(functions:displayLocaleNameWith(adLocale,adLocale))}";
                    </c:when>
                    <c:otherwise>
            else if (value=="${adLocale}") return "${functions:capitalize(functions:displayLocaleNameWith(adLocale,adLocale))}";
            </c:otherwise>
            </c:choose>
            </c:forEach>
        }, {
            type    : 'select',
            data   : "{<c:forEach items='${functions:availableAdminBundleLocale(renderContext.mainResourceLocale)}' var="adLocale" varStatus="status"><c:if test="${not status.first}">,</c:if>'${adLocale}':'${functions:capitalize(functions:displayLocaleNameWith(adLocale,adLocale))}'</c:forEach>}",
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : '<fmt:message key="label.clickToEdit"/>'
        });
    });
</script>

<ul class="user-profile-list">
    <c:if test="${currentNode.properties['j:firstName'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_firstName'/></span>

            <div class="edit" id="j_firstName">
                <c:if test="${empty fields['j:firstName']}"><fmt:message key="label.clickToEdit"/></c:if>
                <c:if test="${!empty fields['j:firstName']}">${fields['j:firstName']}</c:if>
            </div>
        </li>
    </c:if>
    <c:if test="${currentNode.properties['j:lastName'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_lastName'/></span>

            <div class="edit" id="j_lastName">
                <c:if test="${empty fields['j:lastName']}"><fmt:message key="label.clickToEdit"/></c:if>
                <c:if test="${!empty fields['j:lastName']}">${fields['j:lastName']}</c:if>
            </div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:gender'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.profile.sexe"/> : </span>

            <div class="genderEdit" id="j_gender">
                <jcr:nodePropertyRenderer node="${user}" name="j:gender" renderer="resourceBundle"/>
            </div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:title'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.j_title"/></span>

            <div class="titleEdit" id="j_title"><jcr:nodePropertyRenderer node="${user}" name="j:title"
                                                                          renderer="resourceBundle"/></div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:birthDate'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.j_birthDate"/></span>
            <jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
            <c:if test="${not empty birthDate}">
                <fmt:formatDate value="${birthDate.date.time}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
            </c:if>
            <c:if test="${empty birthDate}">
                <jsp:useBean id="now" class="java.util.Date"/>
                <fmt:formatDate value="${now}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
            </c:if>
            <div class="dateEdit" id="j_birthDate">${displayBirthDate}</div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:organization'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_organization'/></span>

            <div class="edit" id="j_organization">
                <c:if test="${empty fields['j:organization']}"><fmt:message key="label.clickToEdit"/></c:if>
                <c:if test="${!empty fields['j:organization']}">${fields['j:organization']}</c:if>
            </div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:function'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_function'/></span>

            <div class="edit" id="j_function">
                <c:if test="${empty fields['j:function']}"><fmt:message key="label.clickToEdit"/></c:if>
                <c:if test="${!empty fields['j:function']}">${fields['j:function']}</c:if>
            </div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:about'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_about'/></span>

            <div class="ckeditorEdit j_aboutEdit" id="j_about">${fields['j:about']}</div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:email'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.j_email"/> : </span>
            <span id="j_email" class="edit">${fields['j:email']}</span><br/>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:skypeID'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_skypeID'/></span>

            <div class="edit" id="j_skypeID">
                <c:if test="${empty fields['j:skypeID']}"><fmt:message key="label.clickToEdit"/></c:if>
                <c:if test="${!empty fields['j:skypeID']}">${fields['j:skypeID']}</c:if>
            </div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:twitterID'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_twitterID'/></span>

            <div class="edit" id="j_twitterID">
                <c:if test="${empty fields['j:twitterID']}"><fmt:message key="label.clickToEdit"/></c:if>
                <c:if test="${!empty fields['j:twitterID']}">${fields['j:twitterID']}</c:if>
            </div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:facebookID'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_facebookID'/></span>

            <div class="edit" id="j_facebookID">
                <c:if test="${empty fields['j:facebookID']}"><fmt:message key="label.clickToEdit"/></c:if>
                <c:if test="${!empty fields['j:facebookID']}">${fields['j:facebookID']}</c:if>
            </div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:linkedinID'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_linkedinID'/></span>

            <div class="edit" id="j_linkedinID">
                <c:if test="${empty fields['j:linkedinID']}"><fmt:message key="label.clickToEdit"/></c:if>
                <c:if test="${!empty fields['j:linkedinID']}">${fields['j:linkedinID']}</c:if>
            </div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:picture'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_picture'/></span>

            <div class="imageEdit">
                <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                <c:if test="${not empty picture}">
                    <img src="${picture.node.thumbnailUrls['avatar_120']}" alt="${person}"/>
                </c:if>
                <c:if test="${empty picture}">
                    <span><fmt:message key="jnt_user.profile.uploadPicture"/></span>
                </c:if>
            </div>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:preferredLanguage'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.preference.preferredLanguage"/></span>

            <div class="prefEdit" id="preferredLanguage">
                <c:choose>
                    <c:when test="${not empty fields.preferredLanguage}">
                        ${functions:capitalize(functions:displayLocaleNameWith(functions:toLocale(fields.preferredLanguage),functions:toLocale(fields.preferredLanguage)))}
                    </c:when>
                </c:choose>
            </div>

        </li>
    </c:if>
</ul>
