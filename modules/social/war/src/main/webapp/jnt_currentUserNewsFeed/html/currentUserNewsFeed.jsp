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

<c:if test="${empty user or not jcr:isNodeType(user, 'jnt:user')}">
    <jcr:node var="user" path="${renderContext.user.localPath}"/>
</c:if>
<c:set var="ps" value="?pagerUrl=${url.mainResource}"/>
<c:if test="${!empty param.pageUrl}">
    <c:set var="ps" value="?pagerUrl=${param.pageUrl}"/>
</c:if>
<c:forEach items="${param}" var="p" varStatus="status">
    <c:if test="${p.key != 'pagerUrl' && p.key != 'jsite'}">
        <c:set var="ps" value="${ps}&${p.key}=${p.value}" />
    </c:if>
</c:forEach>
<c:set target="${moduleMap}" property="pagerUrl" value="${param.pagerUrl}"/>

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
            submitStatusUpdate('<c:url value="${url.base}"/>', '${currentNode.path}', '${user.path}', updateText, '${ps}', '${currentNode.properties['numberOfNewsFeedPerPage'].string}');
            return false;
        });

        loadActivities('<c:url value="${url.base}"/>', '${currentNode.path}', '${user.path}', '${ps}', '${currentNode.properties['numberOfNewsFeedPerPage'].string}');

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


<h4><fmt:message key="userActivities"/></h4>

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

<hr />

<h4><fmt:message key="activitiesList"/></h4>

                    <ul class="activitiesList">
                        <li><fmt:message key="userLoadingStatus"/></li>
                    </ul>

