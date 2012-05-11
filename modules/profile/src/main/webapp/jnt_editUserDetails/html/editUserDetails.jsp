<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="javax.jcr.RepositoryException" %>
<%@ page import="javax.jcr.Value" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.jahia.services.content.JCRPropertyWrapper" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%!
    final String PUBLICPROPERTIES_PROPERTY = "j:publicProperties";

    String getPublicPropertiesData(JCRNodeWrapper user, String propertyName) throws RepositoryException {
        HashSet<String> publicProperties = new HashSet<String>();
        Value[] values = null;
        if (user.hasProperty(PUBLICPROPERTIES_PROPERTY)) {
            values = user.getProperty(PUBLICPROPERTIES_PROPERTY).getValues();
        }
        if (values != null) {
            for (Value value : values) {
                publicProperties.add("&quot;" + value.getString() + "&quot;");
            }
        }
        publicProperties.add("&quot;" + propertyName + "&quot;");
        return "{&quot;" + PUBLICPROPERTIES_PROPERTY + "&quot;:[" + StringUtils.join(publicProperties, ",") + "]}";
    }
%>
<%@ include file="../../getUser.jspf"%>

<template:addResources type="css" resources="userProfile.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="ckeditor/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="timepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="i18n/jquery.ui.datepicker-${currentResource.locale}.js"/>

<template:addResources type="css" resources="timepicker.css"/>
<template:addResources type="javascript" resources="jquery.jeditable.treeItemSelector.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.UILocale}.js"/>
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
    function ajaxReloadCallback(jcrId) {
    	if (jcrId == 'preferredLanguage') {
    		window.location.reload();
    	} else {
        	$('.user-profile-list').parent().load('<c:url value="${url.baseLive}${currentNode.path}.html.ajax?includeJavascripts=true&userUuid=${user.identifier}"/>');
    	}
    }

    function initEditUserDetails() {
        initEditFields("${currentNode.identifier}", true, ajaxReloadCallback);
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

    $(document).ready(initEditUserDetails);
</script>
</template:addResources>

<ul class="user-profile-list">
    <c:if test="${currentNode.properties['j:firstName'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_firstName'/></span>

            <span <c:if test="${user:isPropertyEditable(user,'j:firstName')}"> jcr:id="j:firstName" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_firstName" <c:if test="${empty fields['j:firstName']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:firstName")%>"</c:if>
                  jcr:url="<c:url value='${url.basePreview}${user.path}'/>"</c:if>><c:if test="${empty fields['j:firstName'] and user:isPropertyEditable(user,'j:firstName')}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:firstName']}">${fn:escapeXml(fields['j:firstName'])}</c:if></span>
        </li>
    </c:if>
    <c:if test="${currentNode.properties['j:lastName'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_lastName'/></span>

            <span <c:if test="${user:isPropertyEditable(user,'j:lastName')}"> jcr:id="j:lastName" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_lastName" <c:if test="${empty fields['j:lastName']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:lastName")%>"</c:if>
                  jcr:url="<c:url value='${url.basePreview}${user.path}'/>"</c:if>><c:if test="${empty fields['j:lastName'] and user:isPropertyEditable(user,'j:lastName')}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:lastName']}">${fn:escapeXml(fields['j:lastName'])}</c:if></span>
        </li>
    </c:if>
    <c:if test="${currentNode.properties['j:picture'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_picture'/></span>

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
        <c:if test="${user:isPropertyEditable(user,'j:picture')}">
        <div class="userPicture${currentNode.identifier}" jcr:id="j:picture" <c:if test="${empty fields['j:picture']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:picture")%>"</c:if>
                 jcr:url="<c:url value='${url.basePreview}${user.path}'/>" jcr:fileUrl="<c:url value='${url.basePreview}${user.path}/files/profile/*'/>">
                <span class="small colorlight"><fmt:message key="add.file"/></span>
            </div>
            </c:if>
        </li>
    </c:if>
    <c:if test="${currentNode.properties['j:gender'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.profile.gender"/> : </span>

            <span <c:if test="${user:isPropertyEditable(user,'j:gender')}"> jcr:id="j:gender" class="choicelistEdit${currentNode.identifier}"
                  jcr:url="<c:url value='${url.basePreview}${user.path}'/>" <c:if test="${empty fields['j:gender']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:gender")%>"</c:if>
                  jcr:options="{<c:forEach items="${genderInit}" varStatus="status" var="gender"><c:if test="${status.index > 0}">,</c:if>'${gender.value.string}':'${gender.displayName}'</c:forEach>}"</c:if>><jcr:nodePropertyRenderer node="${user}" name="j:gender" renderer="resourceBundle"/></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:title'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.j_title"/></span>

            <span <c:if test="${user:isPropertyEditable(user,'j:title')}"> jcr:id="j:title" class="choicelistEdit${currentNode.identifier}"
                  jcr:url="<c:url value='${url.basePreview}${user.path}'/>" <c:if test="${empty fields['j:title']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:title")%>"</c:if>
                  jcr:options="{<c:forEach items="${titleInit}" varStatus="status" var="title"><c:if test="${status.index > 0}">,</c:if>'${title.value.string}':'${title.displayName}'</c:forEach>}"</c:if>><jcr:nodePropertyRenderer node="${user}" name="j:title" renderer="resourceBundle"/></span>
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
                <c:set var="displayBirthDate"><fmt:message key="label.clickToEdit"/></c:set>
            </c:if>
            <span <c:if test="${user:isPropertyEditable(user,'j:birthDate')}"> jcr:id="j:birthDate" class="dateEdit${currentNode.identifier}"
                 id="dateEdit${currentNode.identifier}j_birthDate" <c:if test="${empty fields['j:birthDate']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:birthDate")%>"</c:if>
                 jcr:url="<c:url value='${url.basePreview}${user.path}'/>" jcr:value="${birthDate.string}" jcr:valuems="${not empty birthDate.date ? birthDate.date.timeInMillis : ''}"</c:if>>${displayBirthDate}</span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:organization'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_organization'/></span>

            <span <c:if test="${user:isPropertyEditable(user,'j:organization')}"> jcr:id="j:organization" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_organization" <c:if test="${empty fields['j:organization']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:organization")%>"</c:if>
                  jcr:url="<c:url value='${url.basePreview}${user.path}'/>"</c:if>><c:if test="${empty fields['j:organization'] and user:isPropertyEditable(user,'j:organization')}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:organization']}">${fn:escapeXml(fields['j:organization'])}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:function'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_function'/></span>

            <span <c:if test="${user:isPropertyEditable(user,'j:function')}"> jcr:id="j:function" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_function" <c:if test="${empty fields['j:function']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:function")%>"</c:if>
                  jcr:url="<c:url value='${url.basePreview}${user.path}'/>"</c:if>><c:if test="${empty fields['j:function'] and user:isPropertyEditable(user,'j:function')}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:function']}">${fn:escapeXml(fields['j:function'])}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:about'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_about'/></span>

            <span <c:if test="${user:isPropertyEditable(user,'j:about')}"> jcr:id="j:about" class="ckeditorEdit${currentNode.identifier}"
                  id="ckeditorEdit${currentNode.identifier}j_about" <c:if test="${empty fields['j:about']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:about")%>"</c:if>
                  ckeditor:type="Mini"
                  jcr:url="<c:url value='${url.basePreview}${user.path}'/>"</c:if>>${fields['j:about']}</span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:email'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.j_email"/> : </span>

            <span <c:if test="${user:isPropertyEditable(user,'j:email')}"> jcr:id="j:email" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_email" <c:if test="${empty fields['j:email']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:email")%>"</c:if>
                  jcr:url="<c:url value='${url.basePreview}${user.path}'/>"</c:if>><c:if test="${empty fields['j:email'] and user:isPropertyEditable(user,'j:email')}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:email']}">${fn:escapeXml(fields['j:email'])}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:skypeID'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_skypeID'/></span>

            <span <c:if test="${user:isPropertyEditable(user,'j:skypeID')}"> jcr:id="j:skypeID" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_skypeID" <c:if test="${empty fields['j:skypeID']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:skypeID")%>"</c:if>
                  jcr:url="<c:url value='${url.basePreview}${user.path}'/>"</c:if>><c:if test="${empty fields['j:skypeID'] and user:isPropertyEditable(user,'j:skypeID')}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:skypeID']}">${fn:escapeXml(fields['j:skypeID'])}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:twitterID'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_twitterID'/></span>

            <span <c:if test="${user:isPropertyEditable(user,'j:twitterID')}"> jcr:id="j:twitterID" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_twitterID" <c:if test="${empty fields['j:twitterID']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:twitterID")%>"</c:if>
                  jcr:url="<c:url value='${url.basePreview}${user.path}'/>"</c:if>><c:if test="${empty fields['j:twitterID'] and user:isPropertyEditable(user,'j:twitterID')}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:twitterID']}">${fn:escapeXml(fields['j:twitterID'])}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:facebookID'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_facebookID'/></span>

            <span <c:if test="${user:isPropertyEditable(user,'j:facebookID')}"> jcr:id="j:facebookID" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_facebookID" <c:if test="${empty fields['j:facebookID']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:facebookID")%>"</c:if>
                  jcr:url="<c:url value='${url.basePreview}${user.path}'/>"</c:if>><c:if test="${empty fields['j:facebookID'] and user:isPropertyEditable(user,'j:facebookID')}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:facebookID']}">${fn:escapeXml(fields['j:facebookID'])}</c:if></span>
        </li>
    </c:if>

    <c:if test="${currentNode.properties['j:linkedinID'].boolean}">
        <li>
            <span class="label"><fmt:message key='jnt_user.j_linkedinID'/></span>

            <span <c:if test="${user:isPropertyEditable(user,'j:linkedinID')}"> jcr:id="j:linkedinID" class="edit${currentNode.identifier}"
                  id="edit${currentNode.identifier}j_linkedinID" <c:if test="${empty fields['j:linkedinID']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "j:linkedinID")%>"</c:if>
                  jcr:url="<c:url value='${url.basePreview}${user.path}'/>"</c:if>><c:if test="${empty fields['j:linkedinID'] and user:isPropertyEditable(user,'j:linkedinID')}"><fmt:message key="label.clickToEdit"/></c:if><c:if test="${!empty fields['j:linkedinID']}">${fn:escapeXml(fields['j:linkedinID'])}</c:if></span>
        </li>
    </c:if>



    <c:if test="${currentNode.properties['preferredLanguage'].boolean}">
        <li>
            <span class="label"><fmt:message key="jnt_user.preferredLanguage"/></span>
            <jcr:nodeProperty node="${user}" name="preferredLanguage" var="prefLang"/><c:set var="prefLocale" value="${functions:toLocale(prefLang.string)}"/>
            <span jcr:id="preferredLanguage" class="choicelistEdit${currentNode.identifier}"
                  jcr:url="<c:url value='${url.basePreview}${user.path}'/>" <c:if test="${empty fields['preferredLanguage']}">init:data="<%= getPublicPropertiesData((JCRNodeWrapper)pageContext.getAttribute("user"), "preferredLanguage")%>"</c:if>
                  jcr:options="{<c:forEach items='${functions:availableAdminBundleLocale(renderContext.mainResourceLocale)}' var="adLocale" varStatus="status"><c:if test="${status.index > 0}">,</c:if>'${adLocale}':'${functions:escapeJavaScript(functions:displayLocaleNameWith(adLocale, adLocale))}'</c:forEach>}">${functions:displayLocaleNameWith(prefLocale, prefLocale)}</span>
        </li>

    </c:if>

    <c:if test="${currentNode.properties['age'].boolean}">
        <li><span class="label"><fmt:message key="jnt_user.age"/>:&nbsp;</span><utility:dateDiff startDate="${birthDate.date.time}" endDate="${now}" format="years"/>&nbsp;<fmt:message key="jnt_user.profile.years"/></li>
    </c:if>
    <c:if test="${currentNode.properties['password'].boolean and !(user.properties['j:external'].boolean)}">
        <div id="passwordFormContainer" style="display:none;">
        <div id="passwordForm">
            <form id="changePassword" method="post" action="">
                <c:forEach items="${param}" var="p">
                    <c:if test="${not empty ps}">
                        <c:set var="ps" value="${ps}&${p.key}=${p.value}"/>
                    </c:if>
                    <c:if test="${empty ps}">
                        <c:set var="ps" value="?${p.key}=${p.value}"/>
                    </c:if>
                </c:forEach>
                <p>
                <input type="password" id="password" name="password"/></p>
</p>
<p>
                <span class="label"><fmt:message key="label.comfirmPassword"/></span>
                <input type="password" id="passwordconfirm" name="passwordconfirm"/>
           </p>
<div>     
                <button id="passwordokbutton" ><span class="icon-contribute icon-accept"></span><fmt:message key="label.ok"/></button>
                <button type="button" id="passwordcancelbutton" ><span class="icon-contribute icon-cancel"></span><fmt:message key="label.cancel"/></button>
</div>                
            </form>
        </div>
        </div>

            <li>
                <span class="label"><fmt:message key="label.password"/></span>

                <span id="passwordEdit" class="edit${currentNode.identifier}"><fmt:message key="label.clickToEdit"/></span>
            </li>

        <script type="text/javascript">

            $(document).ready(function() {

                $('#passwordEdit').click(function() {
                    $('#passwordEdit').html('');
                    $('#passwordForm').insertAfter('#passwordEdit');
                });

                $('#passwordcancelbutton').click(function() {
                    $('#passwordForm').appendTo('#passwordFormContainer');
                    $('#passwordEdit').html('<fmt:message key="label.clickToEdit"/>');
                });

                $("#changePassword").submit(function() {
                    if ($("#password").val() == "") {
                        alert("<fmt:message key='org.jahia.admin.userMessage.specifyPassword.label'/>");
                        return false;
                    }

                    if ($("#password").val() != $("#passwordconfirm").val()) {
                        alert("<fmt:message key='org.jahia.admin.userMessage.passwdNotMatch.label'/>");
                        return false;
                    }

                    $.post('<c:url value="${url.base}${user.path}.changePassword.do"/>',
                            $(this).serializeArray(), function(data) {
                        alert(data['errorMessage']);
                        if (data['result']=='success') {
                            $('#passwordForm').appendTo('#passwordFormContainer');
                            $('#passwordEdit').html('<fmt:message key="label.clickToEdit"/>');
                        }
                    }, 'json');

                    return false;
                });
            });

        </script>
    </c:if>
</ul>

