<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="javax.jcr.RepositoryException" %>
<%@ page import="javax.jcr.Value" %>
<%@ page import="java.util.HashSet" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="function" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
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

<%-- CSS --%>
<template:addResources type="css" resources="userProfile.css"/>
<template:addResources type="css" resources="dashboardUserProfile.css"/>
<template:addResources type="css" resources="timepicker.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources type="css" resources="bootstrap-switch.css"/>

<%-- Javascripts --%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,jquery.jeditable.js,jquery.blockUI.js,workInProgress.js"/>
<template:addResources type="javascript" resources="jquery.blockUI.js,workInProgress.js"/>
<template:addResources type="javascript" resources="ckeditor/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.treeItemSelector.js"/>
<template:addResources type="javascript" resources="ckeditor/adapters/jquery.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="timepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="i18n/jquery.ui.datepicker-${currentResource.locale}.js"/>
<template:addResources type="javascript" resources="bootstrap-switch.js"/>
<template:addResources type="javascript" resources="editUserDetailsUtils.js"/>
<template:addCacheDependency node="${user}"/>
<jsp:useBean id="now" class="java.util.Date"/>
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
<%--<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>--%>
<template:addResources>
<script type="text/javascript">
    /* User Picture */
    function updatePhoto()
    {
        /*$.post($(".userPicture${currentNode.identifier}").attr('jcr:fileUrl'), )*/
        $(".userPicture${currentNode.identifier}").editable('', {
            type : 'ajaxupload',
            target: $(".userPicture${currentNode.identifier}").attr('jcr:fileUrl'),
            callback : function (data, status,original) {
                var datas;
                var initData = $(original).attr('init:data');
                if (initData != null) {
                    datas = $.parseJSON(initData);
                }
                if (datas == null) {
                    datas = {};
                }
                datas['jcrMethodToCall'] = 'put';
                var callableUrl = $(original).attr('jcr:url');
                datas[$(original).attr('jcr:id').replace("_", ":")] = data.uuids[0];
                $.post($(original).attr('jcr:url'), datas, function(result) {
                    ajaxReloadCallback();
                }, "json");
                $(".userPicture${currentNode.identifier}").html(original.revert);
            }
        });
    }

    /* About me functions */
    /**
     * @Author : Jahia(rahmed)
     * This function Hide the extra part of the about text when the user finish to read it
     */
    function hideMoreText()
    {

        $('div.more').css({
            'height': '115px'
        });
        $('.morelink').show();
        $('.lesslink').hide();
    }

    /**
     * @Author : Jahia(rahmed)
     * This function Show the extra part of the about text so the user can read it
     */
    function showMoreText()
    {
        $('div.more').css({
            'height': 'auto'
        });
        $('.lesslink').show();
        $('.morelink').hide();
    }

    /* Privacy properties functions */
    /**
    * @Author : Jahia(rahmed)
    * This function post on the privacy properties in order to update JCR
    * The post is in string in order to allow multiple values on publicProperties as the Jahia API
    * Doesn't allow the JSON table attributes.
    * propertiesNumber is the number of properties in the loop
    *
    */
    function editVisibility(propertieNumber)
    {
        //loop counter
        var currentPropertieIndex=0;

        //data to Post
        var dataToPost="jcrMethodToCall=put";

        //Looping on properties and filling the data
        for(currentPropertieIndex=0;currentPropertieIndex<propertieNumber;currentPropertieIndex++)
        {
            if($('#publicProperties'+currentPropertieIndex).bootstrapSwitch('state') == true)
            {
                //Parsing the ":" to "%3A" while adding data
                dataToPost+='&j%3ApublicProperties='+$("#publicProperties"+currentPropertieIndex).val().replace(":","%3A");
            }
        }
        //posting the properties visibility to JCR
        $.post("<c:url value='${url.basePreview}${user.path}'/>",dataToPost);
    }

    /* General Table form functions */

    /**
     * @Author : Jahia(rahmed)
     * This function switch a row from the display view to the form view
     * elementId : id of the row to switch
     */
    function switchRow(elementId)
    {
        //building css element id
        elementId="#"+elementId;

        //building css form id
        var elementFormId = elementId+"_form";

        //Checking which element to show and which element to hide
        if( $(elementId).is(":visible"))
        {
            $(elementId).hide();
            $(elementFormId).show();
        }
        else
        {
            $(elementFormId).hide();
            $(elementId).show();
        }
    }

    function ajaxReloadCallback(jcrId)
    {

        /*alert('tempo');*/
        console.log('in callback');
        if (jcrId == 'preferredLanguage')
        {
            console.log('language change detected');
            window.location.reload();
        } else
        {
            console.log('other change detected');
            $('#editDetailspage').parent().load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax?includeJavascripts=false&userUuid=${user.identifier}"/>');
        }
    }

    function wait(){
        if($(".modal").is(":visible"))
        {
            $(".modal").hide();
        }
        else
        {
            $(".modal").show();
        }
    }


    /**
     * @Author : Jahia(rahmed)
     * This function make a JSON Post of all the form entries (textInputs, select and ckeditors) contained in a Row
     * rowId: the Id of the from which post the form entries
     */
    function saveChangesByRowId(rowId)
    {
        console.log('getting in save changes !');
        var data;
        var reloadType='other';

        //initializing data table
        var initData = $(this).attr('init:data');
        if (initData != null)
        {
            data = $.parseJSON(initData);
        }
        if (data == null)
        {
            data = {};
        }

        //presetting the jcr method
        data['jcrMethodToCall'] = 'put';
        var divId = "#"+rowId+'_form';

        console.log('getting selects');
        //getting the form selects
        $(divId+' select').each(function() {
            if(this.name=='preferredLanguage')
            {
                reloadType='preferredLanguage';
                data[this.name]=this.value;
            }
            else
            {
                data['j:'+this.name]=this.value;
            }

        });

        console.log('getting inputs');
        //getting the form inputs
        $(divId+' input').each(function() {
            data['j:'+this.name]=this.value;
        });

        console.log('getting ckEditors');
        //getting the ckeditors
        var editorId = rowId+"_editor";
        var editor = CKEDITOR.instances[editorId];
        if(editor != null)
        {
            data['j:'+rowId]=editor.getData();
        }

        //calling ajax POST
        var thisField = this;


        /*console.log('posting : '+jsonSubmitted);*/
        $.post("<c:url value='${url.basePreview}${user.path}'/>",data,null,"json").done(function(){
            /*console.log('before work in progress');
            $('#indicator').show();
            setTimeout("update", 10);
            window.setInterval(ajaxReloadCallback(reloadType),15000);
            $('#indicator').hide();
            console.log('after work in progress');*/
            /*wait();
            setTimeout(function(){ajaxReloadCallback(reloadType)},1000);*/
            ajaxReloadCallback(reloadType);
        });
        console.log('getting out save changes !');
    }
    var visibilityNumber = 0;

</script>
</template:addResources>
<div class="modal hide" id="pleaseWaitDialog" data-backdrop="static" data-keyboard="false">
    <div class="modal-header">
        <h1>Processing...</h1>
    </div>
    <div class="modal-body">
        <div class="progress progress-striped active">
            <div class="bar" style="width: 100%;"></div>
        </div>
    </div>
</div>
<fieldset class="well" id="editDetailspage">
    <div class="row detailshead">
        <div class="span2">
            <c:if test="${currentNode.properties['j:picture'].boolean}">
                <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                <div id="image" class='image'>
                    <c:choose>
                        <c:when test="${empty picture}">
                            <div class='itemImage itemImageLeft'>
                                <img class="userProfileImage" src="<c:url value='${url.currentModule}/img/userbig.png'/>"
                                     alt="" border="0"/></div>
                        </c:when>
                        <c:otherwise>
                            <div class='itemImage itemImageLeft'><img class="userProfileImage"
                                                                      src="${picture.node.thumbnailUrls['avatar_120']}"
                                                                      alt="${fn:escapeXml(person)}"/></div>
                        </c:otherwise>
                    </c:choose>
                    <div class="image_edit_button">
                        <button class="btn btn-primary" type="button" onclick="switchRow('image')">
                            <fmt:message key="label.clickToEdit"/>
                        </button>
                    </div>
                </div>
                <div class="clear"></div>
            </c:if>
            <div id="image_form" class="hide">
                <div class="image_form_preview">
                    <c:choose>
                        <c:when test="${empty picture}">
                            <div class='itemImage itemImageLeft'>
                                <img class="userProfileImage" src="<c:url value='${url.currentModule}/img/userbig.png'/>"
                                     alt="" border="0"/></div>
                        </c:when>
                        <c:otherwise>
                            <div class='itemImage itemImageLeft'><img class="userProfileImage"
                                                                      src="${picture.node.thumbnailUrls['avatar_120']}"
                                                                      alt="${fn:escapeXml(person)}"/></div>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="image_form_inputs">
                    <form id="pictureform" name="pictureform" onsubmit="return false;">
                        <label for="uploadedImage" value="Upload an Image"/>
                        <input id="uploadedImage" type="file" name="uploadedImage" maxlength="15" size="15"/>
                        <button class="btn btn-primary" type="button" onclick="updatePhoto()">
                            <fmt:message key="label.clickToEdit"/>
                        </button>
                    </form>
                </div>
            </div>
        </div>
        <div class="span10 secondhead">
            <c:if test="${currentNode.properties['j:about'].boolean}">
                <div id="about" class="row">
                    <div class="comment more">
                        <h3><fmt:message key='jnt_user.j_about'/></h3>
                            ${fields['j:about']}
                    </div>
                    <a href="#" class="morelink" onclick="showMoreText()">... more</a>
                    <a href="#" class="lesslink hide" onclick="hideMoreText()">less</a>
                    <c:if test="${user:isPropertyEditable(user,'j:about')}">
                        <div class="about_edit_button">
                            <button class="btn btn-primary" type="button" onclick="switchRow('about')">
                                <fmt:message key="label.clickToEdit"/>
                            </button>
                        </div>
                    </c:if>
                </div>
                <c:if test="${user:isPropertyEditable(user,'j:about')}">
                    <div id="about_form" class="row hide">
                        <div id="about_editor">
                                ${fields['j:about']}
                        </div>
                        <script type="text/javascript">
                            var editor = $( '#about_editor' ).ckeditor();
                        </script>
                        <div id="about_save_button">
                            <button class="btn btn-primary" type="button" onclick="saveChangesByRowId('about')">
                                Save changes
                            </button>
                        </div>
                    </div>
                </c:if>
            </c:if>
        </div>
    </div>
    <div class="row formtable">
        <div class="span2"></div>
        <div class="span10">
            <form class="form-horizontal user-profile-table" onsubmit="return false;">
                <table cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered" id="editUserDetails_table">
                    <tbody>
                    <%@include file="editUserDetailsTableRow.jspf" %>
                    </tbody>
                </table>
            </form>
        </div>
        <div class="span2"></div>
    </div>
</fieldset>
