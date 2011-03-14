<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="social" uri="http://www.jahia.org/tags/socialLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<jsp:useBean id="now" class="java.util.Date"/>
<template:addResources type="css" resources="jquery.autocomplete.css" />
<template:addResources type="css" resources="simplesearchform.css" />
<template:addResources type="css" resources="jahia.fancybox-form.css"/>
<template:addResources type="css" resources="social.css"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources type="javascript" resources="jquery.js,jquery-ui.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript" resources="jquery.fancybox.js"/>
<template:addResources type="javascript" resources="ckeditor/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.autocomplete.js" />
<template:addResources type="javascript" resources="jquery.bgiframe.min.js" />
<template:addResources type="javascript" resources="thickbox-compressed.js" />
<template:addResources type="javascript" resources="timepicker.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<template:addResources type="javascript" resources="jquery.cuteTime.js"/>
<template:addResources type="javascript" resources="jahia.social.js"/>

<c:set var="user" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<%--<c:if test="${not jcr:isNodeType(user, 'jnt:user')}">--%>
<%--<jcr:node var="user" path="/users/${user.properties['jcr:createdBy'].string}"/>--%>
<%--</c:if>--%>
<c:if test="${empty user or not jcr:isNodeType(user, 'jnt:user')}">
    <jcr:node var="user" path="/users/${renderContext.user.username}"/>
</c:if>

<%--map all display values --%>
<jsp:useBean id="userProperties" class="java.util.HashMap"/>

<script type="text/javascript">


    function initCuteTime() {
        $('.timestamp').cuteTime({ refresh: 60000 });
    }

    function userNewsFeedCallback() {
        $("#statusUpdateSubmit").click(function() {
            // validate and process form here
            var updateText = $("textarea#statusUpdateText").val();
            // alert('Sending text ' + updateText);
            submitStatusUpdate('${url.base}', '${currentNode.path}', '${user.path}', '${user.identifier}', updateText);
            return false;
        });

        loadActivities('${url.base}', '${currentNode.path}', '${user.path}');

    }

    <c:if test="${not renderContext.ajaxRequest}">
    $(document).ready(function() {
        userNewsFeedCallback();
    });
    </c:if>
</script>
<c:if test="${renderContext.ajaxRequest}">
    <template:addResources>
        <script type="text/javascript">
            userNewsFeedCallback();
        </script>
    </template:addResources>
</c:if>

    <div class="boxsocial"><!--start boxsocial -->
        <div class="boxsocialpadding16 boxsocialmarginbottom16">
            <div class="boxsocial-inner">
                <div class="boxsocial-inner-border">
                    <h3><fmt:message key="userActivities"/></h3>

                    <form class="statusUpdateForm" name="statusUpdateForm" action="" method="post">
                        <textarea rows="2" cols="20" class="" onfocus="if(this.value==this.defaultValue)this.value='';"
                                  onblur="if(this.value=='')this.value=this.defaultValue;"
                                  name="statusUpdateText" id="statusUpdateText"><fmt:message
                                key="statusUpdateDefaultText"/></textarea>

                        <p>
                            <input class="button" id="statusUpdateSubmit" type="submit"
                                   title="<fmt:message key='statusUpdateSubmit'/>"/>
                        </p>
                    </form>
                </div>
            </div>
        </div>
        <div class='clear'></div>
    </div>

    <h4 class="boxsocial-title"><fmt:message key="activitiesList"/></h4>

    <div class="boxsocial">
        <div class="boxsocialpadding10 boxsocialmarginbottom16">
            <div class="boxsocial-inner">
                <div class="boxsocial-inner-border"><!--start boxsocial -->
                    <ul class="activitiesList">
                        <li><fmt:message key="userLoadingStatus"/></li>
                    </ul>
                </div>
            </div>
        </div>
        <div class='clear'></div>
    </div>

