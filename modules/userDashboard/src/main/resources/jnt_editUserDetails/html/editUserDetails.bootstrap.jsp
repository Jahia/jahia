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

<template:addResources type="css" resources="userProfile.css"/>
<template:addResources type="css" resources="dashboardUserProfile.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="ckeditor/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="timepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="i18n/jquery.ui.datepicker-${currentResource.locale}.js"/>

<template:addResources type="css" resources="timepicker.css"/>
<template:addResources type="javascript" resources="jquery.jeditable.treeItemSelector.js"/>
<template:addResources type="javascript" resources="ckeditor/adapters/jquery.js"/>
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

<template:addResources>
    <script type="text/javascript">

        //More text functions
        function hideMoreText()
        {
            var showChar = 100;
            var ellipsestext = "...";
            var moretext = "more";
            var lesstext = "less";
            $('.more').each(function() {
                var content = $(this).html();

                if(content.length > showChar) {

                    var c = content.substr(0, showChar);
                    var h = content.substr(showChar-1, content.length - showChar);

                    var html = c + '<span class="moreellipses">' + ellipsestext+ '&nbsp;</span><span class="morecontent"><span>' + h + '</span>&nbsp;&nbsp;<a href="" class="morelink">' + moretext + '</a></span>';

                    $(this).html(html);
                }

            });

            $(".morelink").click(function(){
                if($(this).hasClass("less")) {
                    $(this).removeClass("less");
                    $(this).html(moretext);
                } else {
                    $(this).addClass("less");
                    $(this).html(lesstext);
                }
                $(this).parent().prev().toggle();
                $(this).prev().toggle();
                return false;
            });
        }


        function errorOnSave(thisField) {
            return function(jqXHR, textStatus, errorThrown) {
                alert(contributionI18n['invalidconstraint']);
                thisField.editing = true;
                thisField.reset();
            }
        }

        function switchRow(elementId){
            var thisField = this;
            errorOnSave(thisField);
            elementId="#"+elementId;
            var elementFormId = elementId+"_form";
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
        function ajaxReloadCallback(jcrId) {
            if (jcrId == 'preferredLanguage') {
                window.location.reload();
            } else {
                $('.user-profile-table').parent().load('<c:url value="${url.baseLive}${currentNode.path}.html.ajax?includeJavascripts=true&userUuid=${user.identifier}"/>');
            }
        }

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
                data['j:'+this.name]=this.value;
            });


            //getting the ckeditors
            var editorId = rowId+"_editor";
            console.log('editor id : '+editorId);
            console.log('div editor value : '+CKEDITOR.instances[editorId].getData());
            var editor = CKEDITOR.instances[editorId];
            if(editor != null)
            {
                data['j:'+rowId]=editor.getData();
            }


            //calling ajax POST
            var thisField = this;
            var url = '${url.basePreview}${user.path}';
            $.ajax({ type: 'POST',
                url: url,
                data: data,
                dataType: "json",
                error:errorOnSave(thisField),
                traditional: true,
                callback:ajaxReloadCallback(reloadType)
            });
        }

        function initEditUserDetails() {
            //initEditFields("${currentNode.identifier}", true, ajaxReloadCallback);
            $(".preferredLanguageEdit${currentNode.identifier}").editable(function (value, settings) {
                var data;
                var initData = $(this).attr('init:data');
                if (initData != null) {
                    data = $.parseJSON(initData);
                }
                if (data == null) {
                    data = {};
                }
                data['jcrMethodToCall'] = 'put';
                var submitId = $(this).attr('jcr:id').replace("_", ":");
                data[submitId] = value;
                var thisField = this;
                $.ajax({
                    type: 'POST',
                    url: $(this).attr('jcr:url'),
                    data: data,
                    dataType: "json",
                    error:errorOnSave(thisField),
                    traditional: true
                }).done(function() {
                    window.location.assign(window.location.href.replace(/\/${currentResource.locale}\//,"/"+value+"/"));
                });

                return eval("values=" + $(this).attr('jcr:options'))[value];
            }, {
                type    : 'select',
                data   : function() {
                    return $(this).attr('jcr:options');
                },
                onblur : 'ignore',
                submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>' + contributionI18n['ok'] + '</button>',
                cancel : '<button type="cancel"><span class="icon-contribute icon-cancel"></span>' + contributionI18n['cancel'] + '</button>',
                tooltip : contributionI18n['edit'],
                placeholder:contributionI18n['edit']
            });
            $(".userPicture${currentNode.identifier}").editable('', {
                type : 'ajaxupload',
                onblur : 'ignore',
                submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>' + contributionI18n['ok'] + '</button>',
                cancel : '<button type="cancel"><span class="icon-contribute icon-cancel"></span>' + contributionI18n['cancel'] + '</button>',
                tooltip : contributionI18n['edit'],
                placeholder : contributionI18n['edit'],
                target:$(".userPicture${currentNode.identifier}").attr('jcr:fileUrl'),
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
        $(document).ready(hideMoreText);
        //$(document).ready(initEditUserDetails);
    </script>
</template:addResources>

<%--<span class="label"><fmt:message key='jnt_user.j_picture'/></span>--%>
<fieldset class="well">
    <div class="row">
        <div class="span2">
            <div>
                <c:if test="${currentNode.properties['j:picture'].boolean}">
                    <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                    <c:choose>
                        <c:when test="${empty picture}">
                            <div class='image'>
                                <div class='itemImage itemImageLeft'>
                                    <img class="userProfileImage" src="<c:url value='${url.currentModule}/img/userbig.png'/>"
                                         alt="" border="0"/></div>
                            </div>
                            <div class="clear"></div>
                        </c:when>
                        <c:otherwise>
                            <div class='image'>
                                <div class='itemImage itemImageLeft'><img class="userProfileImage"
                                                                          src="${picture.node.thumbnailUrls['avatar_120']}"
                                                                          alt="${fn:escapeXml(person)}"/></div>
                            </div>
                            <div class="clear"></div>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </div>
            <div>
                <c:if test="${currentNode.properties['j:organization'].boolean}">
                    <c:if test="${empty fields['j:organization'] and user:isPropertyEditable(user,'j:organization')}">
                        <fmt:message key="label.clickToEdit"/>
                    </c:if>
                    <c:if test="${!empty fields['j:organization']}">
                        <h3>${fn:escapeXml(fields['j:organization'])}</h3>
                    </c:if>
                </c:if>
                <c:if test="${currentNode.properties['j:function'].boolean}">
                    <c:if test="${empty fields['j:function'] and user:isPropertyEditable(user,'j:function')}">
                        <fmt:message key="label.clickToEdit"/>
                    </c:if>
                    <c:if test="${!empty fields['j:function']}">
                        <h4>${fn:escapeXml(fields['j:function'])}</h4>
                    </c:if>
                </c:if>
            </div>
        </div>
        <div class="span10" style="height:120px">
            <div class="row">
                <div id="about" class="comment more">
                    <div class="span8">
                        <c:if test="${currentNode.properties['j:about'].boolean}">
                            <h3><fmt:message key='jnt_user.j_about'/></h3>
                            ${fields['j:about']}
                        </c:if>
                    </div>
                    <div class="span2">
                        <c:if test="${user:isPropertyEditable(user,'j:about')}">
                            <button class="btn btn-primary" type="button" onclick="switchRow('about')">
                                <fmt:message key="label.clickToEdit"/>
                            </button>
                        </c:if>
                    </div>
                </div>
                <div id="about_form" class="about_form hide">
                    <div class="span8">
                        <div id="about_editor">
                            Test sans span ${fields['j:about']}
                        </div>
                        <script type="text/javascript">
                            var editor = $( '#about_editor' ).ckeditor();
                        </script>
                    </div>
                    <div class="span2">
                        <c:if test="${user:isPropertyEditable(user,'j:about')}">
                            <button class="btn btn-primary" type="button" onclick="saveChangesByRowId('about')">
                                Save changes
                            </button>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="formtable row">
        <div class="span2"></div>
        <div class="span10">
            <form class="form-horizontal user-profile-table">
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