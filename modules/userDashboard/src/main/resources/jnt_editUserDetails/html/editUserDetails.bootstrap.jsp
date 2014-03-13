<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="javax.jcr.RepositoryException" %>
<%@ page import="javax.jcr.Value" %>
<%@ page import="java.util.HashSet" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%@ taglib prefix="facet" uri="http://www.jahia.org/tags/facetLib" %>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%!
    final String PUBLICPROPERTIES_PROPERTY = "j:publicProperties";

    HashSet<String> getPublicProperties(PageContext pageContext) throws RepositoryException {
        HashSet<String> publicProperties = (HashSet<String>) pageContext.getAttribute("editUserDetailsPublicProperties");
        if (publicProperties != null) {
            return new HashSet<String>(publicProperties);
        } else {
            publicProperties = new HashSet<String>();
            JCRNodeWrapper user = (JCRNodeWrapper) pageContext.getAttribute("user");
            Value[] values = null;
            if (user.hasProperty(PUBLICPROPERTIES_PROPERTY)) {
                values = user.getProperty(PUBLICPROPERTIES_PROPERTY).getValues();
            }
            if (values != null) {
                for (Value value : values) {
                    publicProperties.add("&quot;" + value.getString() + "&quot;");
                }
            }
            pageContext.setAttribute("editUserDetailsPublicProperties", publicProperties);
            return publicProperties;
        }
    }
    String getPublicPropertiesData(PageContext pageContext, String propertyName) throws RepositoryException {
        HashSet<String> publicProperties = getPublicProperties(pageContext);
        publicProperties.add("&quot;" + propertyName + "&quot;");
        return "{&quot;" + PUBLICPROPERTIES_PROPERTY + "&quot;:[" + StringUtils.join(publicProperties, ",") + "]}";
    }
%>
<%@ include file="../../getUser.jspf"%>

<%-- CSS inclusions --%>
<template:addResources type="css" resources="admin-bootstrap.css"/>
<template:addResources type="css" resources="bootstrap-datetimepicker.min.css"/>
<template:addResources type="css" resources="bootstrap-switch.css"/>

<%-- Javascripts inclusions --%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="javascript" resources="admin-bootstrap.js"/>
<template:addResources type="javascript" resources="bootstrap-switch.js"/>
<template:addResources type="javascript" resources="bootbox.min.js"/>
<%--<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>--%>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="bootstrap-datetimepicker.min.js"/>
<template:addResources type="javascript" resources="ckeditor/ckeditor.js"/>
<template:addResources type="javascript" resources="ckeditor/adapters/jquery.js"/>
<template:addResources type="javascript" resources="editUserDetailsUtils.js"/>
<template:addResources type="javascript" resources="bootstrap-datetimepicker.${currentResource.locale}.js"/>

<template:addCacheDependency node="${user}"/>

<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="fields" value="${user.propertiesAsString}"/>

<jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>

<jcr:nodeProperty node="${user}" name="j:publicProperties" var="publicProperties" />
<c:forEach items="${publicProperties}" var="value">
    <c:set var="publicPropertiesAsString" value="${value.string} ${publicPropertiesAsString}"/>
</c:forEach>

<jcr:propertyInitializers node="${user}" name="j:gender" var="genderInit"/>
<jcr:propertyInitializers node="${user}" name="j:title" var="titleInit"/>
<%--<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>--%>
<template:addResources>
<script type="text/javascript">
var context = "${url.context}";
var changePasswordUrl = '<c:url value="${url.base}${user.path}.changePassword.do"/>';
var getUrl="<c:url value="${url.baseUserBoardFrameEdit}${currentNode.path}.bootstrap.html.ajax?includeJavascripts=false&userUuid=${user.identifier}"/>";

function updateProperties(cssClass)
{
    formToJahiaCreateUpdateProperties("editDetailsForm", "${user.identifier}", "${currentResource.locale}", cssClass, ajaxReloadCallback,undefined);
}

var visibilityNumber = 0;

$(document).ready(function(){
    $('body').on('click','#tabView a',function (e) {
        e.preventDefault();
        $(this).tab('show');
    });
    $('body').on('click','#datePickerParent',function (e) {
        e.preventDefault();
        $('#birthDate').datetimepicker({
            format: 'yyyy-MM-dd',
            pickTime: false,
            language: '${currentResource.locale}'
        });
    });

    // Activating the checkbox buttons
    $('body').on('click','#switchParent',function (e) {
        e.preventDefault();
        for(var currentvisibility=0;currentvisibility<visibilityNumber;currentvisibility++)
         {
             $("#publicProperties"+currentvisibility).bootstrapSwitch();
         }
         for(var currentvisibility=0;currentvisibility<visibilityNumber;currentvisibility++)
         {
             $('#publicProperties'+currentvisibility).on('switchChange', function (e, data)
             {
                 //getting the switch form element and its value
             var $element = $(data.el),value = data.value;
             var elementId=$element.attr("id");
             //getting the switch number contained in its css id by removing the text part
             var number = parseInt(elementId.replace("publicProperties",''));

             //calling the change visibility function with the number total of visibility switches, the number of the visibility to change and the state to put
             editVisibility(visibilityNumber,number,value,"${user.identifier}", "${currentResource.locale}");

             });
         }
    });

    $(".btnMoreAbout").click(function(){
        $(".aboutMeText").css( { height:"100%",maxHeight: "500px", overflow: "auto", paddingRight: "5px" }, { queue:false, duration:500 });
        $(".btnMoreAbout").hide();
        $(".btnLessAbout").show();
    });

    $(".btnLessAbout").click(function(){
        $(".aboutMeText").css( { height:"100px", overflow: "hidden" }, { queue:false, duration:500 });
        $(".btnLessAbout").hide();
        $(".btnMoreAbout").show();
    });


});
</script>
</template:addResources>
<div id="editDetailspage">

    <ul class="nav nav-tabs" id="tabView">
        <li class="active"><a href="#private"><fmt:message key="mySettings.privateView"/></a></li>
        <li><a href="#public"><fmt:message key="mySettings.publicView"/></a></li>
    </ul>
    <div class="tab-content">
        <div class="tab-pane active" id="private">
            <form enctype= multipart/form-data onkeypress="return event.keyCode != 13;" id="editDetailsForm" class="form-horizontal user-profile-table" onsubmit="return false;">
                <div>
                    <div id="detailsHead" class="row-fluid">
                        <div class="span2"></div>
                        <div class="span8 alert alert-info" style="padding-right: 10px">
                            <div class="row-fluid ">
                                <div id="imageDiv" class="span2">
                                    <c:if test="${currentNode.properties['j:picture'].boolean}">
                                        <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                                        <div id="image">
                                            <div id="imageDisplay" class="row-fluid">
                                                <c:choose>
                                                    <c:when test="${empty picture}">
                                                        <img class="img-polaroid pull-left" src="<c:url value='${url.currentModule}/img/userbig.png'/>"
                                                             alt="" border="0"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <img class="img-polaroid pull-left" src="${picture.node.thumbnailUrls['avatar_120']}"
                                                             alt="${fn:escapeXml(person)}"/>
                                                    </c:otherwise>
                                                </c:choose>
                                                <br/>
                                                <div style="clear: both;"></div>
                                            </div>
                                            <div id="pictureEditButton" class="row-fluid">
                                                <button class="btn btn-primary" type="button" onclick="$('#about').hide();$('#image_form').show();$('#image').hide()">
                                                    <fmt:message key="mysettings.picture.edit"/>
                                                </button>
                                            </div>
                                        </div>
                                    </c:if>
                                </div>
                                <div id="aboutMeDiv" class="span10">
                                    <c:if test="${currentNode.properties['j:about'].boolean}">
                                        <div id="about">
                                            <div id="about-text-part">
                                                <h1>
                                                    <fmt:message key='jnt_user.j_about'/>
                                                </h1>
                                                <div id="aboutMeText" class="aboutMeText lead" style="height: 100px; text-align: justify; overflow: hidden">
                                                        ${user.properties['j:about'].string}
                                                </div>
                                                <br />
                                            </div>
                                            <div id="about-button-part" class="row-fluid">
                                                <button id="btnMoreAbout" class="btn btn-small btn-primary btnMoreAbout" <%--onclick="showMoreText()"--%>>
                                                    <fmt:message key='mySettings.readMore'/>
                                                </button>
                                                <button id="btnLessAbout" class="btn btn-small btn-primary hide btnLessAbout" <%--onclick="hideMoreText()"--%>>
                                                    <fmt:message key='mySettings.readLess'/>
                                                </button>
                                                <c:if test="${user:isPropertyEditable(user,'j:about')}">
                                                    <button class="btn btn-primary pull-right" type="button" onclick="switchRow('about')">
                                                        <fmt:message key="label.clickToEdit"/>
                                                    </button>
                                                </c:if>
                                            </div>
                                        </div>
                                        <div id="image_form" class="hide span10">
                                            <div class="image_form_preview">
                                                <c:choose>
                                                    <c:when test="${empty picture}">
                                                        <img class="img-polaroid pull-left" src="<c:url value='${url.currentModule}/img/userbig.png'/>"
                                                             alt="" border="0"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <img class="img-polaroid pull-left" src="${picture.node.thumbnailUrls['avatar_120']}"
                                                             alt="${fn:escapeXml(person)}"/>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="image_form_inputs">
                                                <div class="control-group">
                                                    <div class="controls">
                                                        <input id="uploadedImage" type="file" name="file"/>
                                                    </div>
                                                </div>
                                                <div class="form-actions">
                                                    <button type="button" class="btn btn-danger" onclick="ajaxReloadCallback(null,'cancel')"><fmt:message key="cancel"/></button>
                                                    <button id="DeletePictureButton" class="btn btn-warning" type="button" onclick="jahiaAPIStandardCall('${url.context}','default','${currentResource.locale}','nodes', '${user.identifier}/properties/j__picture','DELETE', '' , ajaxReloadCallback(), undefined)">
                                                        <fmt:message key="mySettings.picture.delete"/>
                                                    </button>
                                                    <button id="imageUploadButton" class="btn btn-success" type="button" onclick="updatePhoto('uploadedImage','${currentResource.locale}', '${user.path}','${user.identifier}',ajaxReloadCallback, undefined );">
                                                        <fmt:message key="save"/>
                                                    </button>
                                                </div>
                                            </div>
                                            <fmt:message key="myFiles.alertInfoCharacters"/> */:
                                            <div><span id="imageUploadError" class="hide"><fmt:message key="mySettings.errors.picture.upload"/></span><span id="imageUploadNameError" class="hide"><fmt:message key="mySettings.errors.picture.name.upload"/></span><span id="imageUploadEmptyError" class="hide"><fmt:message key="mySettings.errors.picture.empty.upload"/></span></div>
                                        </div>
                                        <c:if test="${user:isPropertyEditable(user,'j:about')}">
                                            <div id="about_form" class="hide span10">
                                                <div id="about_editor">
                                                        ${fields['j:about']}
                                                </div>
                                                <script type="text/javascript">
                                                    var editor = $( '#about_editor' ).ckeditor({toolbar:"Mini"});
                                                </script>
                                                <br />
                                                <div class="pull-right">
                                                    <button type="button" class="btn btn-danger" onclick="ajaxReloadCallback(null,'cancel')">
                                                        <fmt:message key="cancel"/>
                                                    </button>
                                                    <button class="btn btn-success" type="button" onclick="saveCkEditorChanges('about','${user.identifier}', '${currentResource.locale}',ajaxReloadCallback,undefined)">
                                                        <fmt:message key="save"/>
                                                    </button>
                                                </div>
                                            </div>
                                        </c:if>
                                    </c:if>
                                </div>
                            </div>
                        </div>

                        <div class="span2"></div>
                    </div>
                </div>
                <div class="row-fluid" >
                    <div class="span2" ></div>
                    <div class="span8">
                            <%@include file="editUserDetailsRows.jspf" %>
                    </div>
                    <div class="span2"></div>
                </div>
            </form>
        </div>
        <div class="tab-pane" id="public">
            <%@include file="editUserDetailsPublicView.jspf" %>
        </div>
    </div>
</div>