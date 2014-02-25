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
<template:addResources type="css" resources="bootstrap-datetimepicker.min.css"/>
<template:addResources type="css" resources="bootstrap-switch.css"/>
<template:addResources type="css" resources="dashboardUserProfile.css"/>

<%-- Javascripts inclusions --%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="javascript" resources="bootstrap-datetimepicker.min.js"/>
<template:addResources type="javascript" resources="bootstrap-datetimepicker.min.${currentResource.locale}.js"/>
<template:addResources type="javascript" resources="bootstrap-switch.js"/>
<template:addResources type="javascript" resources="ckeditor/ckeditor.js"/>
<template:addResources type="javascript" resources="ckeditor/adapters/jquery.js"/>
<template:addResources type="javascript" resources="editUserDetailsUtils.js"/>
<template:addCacheDependency node="${user}"/>

<jsp:useBean id="now" class="java.util.Date"/>
<c:set var="fields" value="${user.propertiesAsString}"/>

<jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>

<jcr:propertyInitializers node="${user}" name="j:gender" var="genderInit"/>
<jcr:propertyInitializers node="${user}" name="j:title" var="titleInit"/>
<%--<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>--%>
<template:addResources>
<script type="text/javascript">
/* User Picture */
var currentElement = "";
var currentForm = "";

/**
 * @Author : Jahia(rahmed)
 * This function Upload a picture the user picked and update his user picture with it
 * The picture to upload is directly picked from the form
 */
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
    $('#aboutMeText').css({
        'height': 'auto',
        'overflow': 'hidden'
    });
    $('#aboutMeBlock').css({
        'height': '115px'
    });
    $('#aboutMeLessLink').hide();
    $('#aboutMeMoreLink').show();

}

/**
 * @Author : Jahia(rahmed)
 * This function Show the extra part of the about text so the user can read it (scroll view)
 */
function showMoreText()
{
    $('#aboutMeText').css({
        'height': '115px',
        'overflow': 'auto'
    });
    $('#aboutMeBlock').css({
        'height': '160px',
        'overflow': 'auto'
    });
    $('#aboutMeMoreLink').hide();
    $('#aboutMeLessLink').show();
}

/* Privacy properties functions */
/**
 * @Author : Jahia(rahmed)
 * This function post the privacy properties in order to update JCR
 * The post is in string in order to allow multiple values on publicProperties as the Jahia API
 * Doesn't allow the JSON table attributes.
 * propertiesNumber: the number of properties in the loop
 * idNumber: The id of the switch triggering the update (for the check image near the switch)
 * propertiesNumber: The updated state by the switch (for the check image near the switch)
 */
function editVisibility(propertiesNumber,idNumber, value)
{
    //data to Post
    var dataToPost = "jcrMethodToCall=put";

    //the image to put near the switch once the post is successful
    var doneImageId = '';

    if(value == true)
    {
        doneImageId = '#switchOn'+idNumber;
    }
    else
    {
        doneImageId = '#switchOff'+idNumber;
    }

    //Looping on properties and filling the data
    for(var currentPropertieIndex=0;currentPropertieIndex<propertiesNumber;currentPropertieIndex++)
    {
        //replacing the list of public properties by the list of all the switches in true state
        if($('#publicProperties'+currentPropertieIndex).bootstrapSwitch('state') == true)
        {
            //Parsing the ":" to "%3A" while adding data
            dataToPost += '&j%3ApublicProperties='+$("#publicProperties"+currentPropertieIndex).val().replace(":","%3A");
        }
    }
    //posting the properties visibility to JCR
    $.post("<c:url value='${url.basePreview}${user.path}'/>",dataToPost,function (){
        //hiding all the others images near the switches
        $('.switchIcons').hide();

        //showing the image near the switch
        $(doneImageId).fadeIn('slow').delay(1000).fadeOut('slow');
    });
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
    console.log('elementId : '+elementId);
    console.log('elementFormId : '+elementFormId);
    //Checking which element to show and which element to hide
    if( $(elementId).is(":visible"))
    {
        if(currentForm!='')
        {
            $(currentForm).hide();
            $(currentElement).show();
        }
        //Hide the display row
        $(elementId).hide();
        //Show the form
        $(elementFormId).show();
    }
    else
    {
        //Hide the Form
        $(elementFormId).hide();
        //Show the display Row
        $(elementId).show();
    }
    currentElement = elementId;
    currentForm = elementFormId;
}

function ajaxReloadCallback(jcrId)
{
    if (jcrId == 'preferredLanguage')
    {
        var windowToRefresh = window.parent;
        if(windowToRefresh == undefined)
            windowToRefresh = window;
        windowToRefresh.location.reload();
    } else
    {
        $('#editDetailspage').parent().load('<c:url value="${url.basePreview}${currentNode.path}.html.ajax?includeJavascripts=false&userUuid=${user.identifier}"/>');
    }
}

/**
 * @Author : Jahia(rahmed)
 * This function make a JSON Post of all the form entries (textInputs, select and ckeditors) contained in a Row
 * rowId: the Id of the from which post the form entries
 */
function saveChangesByRowId(rowId)
{
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

    //getting the form inputs
    $(divId+' input').each(function() {
        if(this.name == 'birthDate' && this.value.length == 0 )
        {
            //TODO With the New Jahia API
        }
        else
        {
            data['j:'+this.name]=this.value;
        }
    });

    //getting the ckeditors
    var editorId = rowId+"_editor";
    var editor = CKEDITOR.instances[editorId];
    if(editor != null)
    {
        data['j:'+rowId]=editor.getData();
    }

    //calling ajax POST
    var thisField = this;
    $.post("<c:url value='${url.basePreview}${user.path}'/>",data,null,"json").done(function(){ajaxReloadCallback(reloadType);});
}

/**
 * @Author : Jahia(rahmed)
 * This function changes the user Password calling the action changePassword.do
 * The new password is picked directly from the password change form in this page.
 */
function changePassword()
{
    //passwords checks
    if ($("#passwordField").val() == "") {
        alert("<fmt:message key='serverSettings.user.errors.password.mandatory'/>");
        return false;
    }
    if ($("#passwordField").val() != $("#passwordconfirm").val()) {
        alert("<fmt:message key='serverSettings.user.errors.password.not.matching'/>");
        return false;
    }
    var params = {password: $("#passwordField").val()};
    $.post( '<c:url value="${url.base}${user.path}.changePassword.do"/>', { password: $("#passwordField").val(), passwordconfirm:  $("#passwordconfirm").val()},
            function(result)
            {
                if(result['result'] != 'success')
                {
                    $('#passwordError').html(result['errorMessage']);
                    $('#passwordError').fadeIn('slow').delay(4000).fadeOut('slow');
                    $('#passwordField').focus();
                }
                else
                {
                    switchRow('password');
                    $('#passwordSuccess').html(result['errorMessage']);
                    $('#passwordSuccess').fadeIn('slow').delay(4000).fadeOut('slow');
                }
            },
            'json');
}

var visibilityNumber = 0;
$(document).ready(function(){
    // Activating the more/less links */
    if($('#aboutMeText').height()>$('#aboutMeBlock').height())
    {
        $('#aboutMeMoreLink').show();
    }

    // Activating the checking buttons
    var currentvisibility=0;
    for(currentvisibility=0;currentvisibility<visibilityNumber;currentvisibility++)
    {
        $("#publicProperties"+currentvisibility).bootstrapSwitch();
    }
    for(currentvisibility=0;currentvisibility<visibilityNumber;currentvisibility++)
    {
        $('#publicProperties'+currentvisibility).on('switchChange', function (e, data)
        {
            //getting the switch form element and its value
            var $element = $(data.el),value = data.value;
            var elementId=$element.attr("id");
            //getting the switch number contained in its css id by removing the text part
            var number = parseInt(elementId.replace("publicProperties",''));
            //calling the change visibility function with the number total of visibility switches, the number of the visibility to change and the state to put
            editVisibility(visibilityNumber,number,value);
        });
    }

    //Activating the Date-Picker
    $('#birthDate').datetimepicker({
        format: 'yyyy-MM-dd',
        pickTime: false,
        language: '${currentResource.locale}'
    });
});
</script>
</template:addResources>
<fieldset class="well" id="editDetailspage">
    <div id="detailsHead" class="row">
        <div id="imageDiv" class="span2">
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
                            <div class='itemImage itemImageLeft'>
                                <img class="userProfileImage"
                                     src="${picture.node.thumbnailUrls['avatar_120']}"
                                     alt="${fn:escapeXml(person)}"/>
                            </div>
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
        <div id="aboutMeDiv" class="span10">
            <c:if test="${currentNode.properties['j:about'].boolean}">
                <div id="about" class="row">
                    <div id="aboutMeBlock" class="comment more">
                        <h3 id="aboutMeTitle"><fmt:message key='jnt_user.j_about'/></h3>
                           <div id="aboutMeText"> ${user.properties['j:about'].string}</div>
                    </div>
                    <a href="#" id="aboutMeMoreLink" class="hide" onclick="showMoreText()">... more</a>
                    <div class="pull-right"><a href="#" id="aboutMeLessLink" class="hide" onclick="hideMoreText()">less</a></div>
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
            <form id="editDetailsForm" class="form-horizontal user-profile-table" onsubmit="return false;">
                    <%@include file="editUserDetailsRows.jspf" %>
            </form>
        </div>
        <div class="span2"></div>
    </div>
</fieldset>