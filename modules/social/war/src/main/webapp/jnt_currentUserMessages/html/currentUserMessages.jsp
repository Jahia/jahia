<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:include view="hidden.header"/>
<template:addResources type="css" resources="jahia.fancybox-form.css"/>
<template:addResources type="css" resources="social.css"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<template:addResources type="javascript" resources="jquery.js,jquery.cuteTime.js"/>
<template:addResources type="javascript" resources="jquery.fancybox.js"/>
<template:addResources type="javascript" resources="jahia.social.js"/>
<c:set var="user" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<c:if test="${empty user or not jcr:isNodeType(user, 'jnt:user')}">
    <jcr:node var="user" path="${renderContext.user.localPath}"/>
</c:if>
<script type="text/javascript">

    function userMessagesCallback() {
        $(".messageDetailLink").click(function() {
            $.ajax({
                url: $(this).attr('info'),
                type : 'get',
                success : function (data) {
                    $(".social-message-detail").html(data);
                    $('.timestamp').cuteTime({ refresh: 60000 });
                    initActionDeleteLinks($("div.social-message-detail a.messageActionDelete"));
                    initShowSendMessage($("div.social-message-detail a.messageActionReply"));
                }
            });
        });

        $('.timestamp').cuteTime({ refresh: 60000 });

        $("#sendMessage").submit(function() {
            if ($("#messagesubject").val().length < 1) {
                $("#login_error").show();
                $.fancybox.resize();
                return false;
            }

            $.fancybox.showActivity();
            $.ajax({
                type        : "POST",
                cache       : false,
                url         : '<c:url value="${url.base}${user.path}.sendmessage.do"/>',
                data        : $(this).serializeArray(),
                success     : function(data) {
                    alert("<fmt:message key='message.messageSent'/>");
                    $.fancybox.resize();
                    $.fancybox.center();
                    $.fancybox.close();
                }
            });

            return false;
        });

        initActionDeleteLinks($("a.messageActionDelete"));
        initShowSendMessage($("a.messageActionReply"));
    }
    function initActionDeleteLinks(links) {
        links.click(function(e){
            e.preventDefault();
            var msgId = $(this).attr('info');
            if (confirm("<fmt:message key='message.removeSocialMessage.confirm'/>")) {
                removeSocialMessage('<c:url value="${url.base}/${user.path}"/>', msgId,
                        function() {
                            $("#social-message-" + msgId).remove();
                            if ($("div.social-message-detail div#social-message-detail-" + msgId).length > 0) {
                                $(".social-message-detail").empty();
                            }
                        });
            }
        });
    }
    function initShowSendMessage(links) {
        links.fancybox({
            'scrolling'          : 'no',
            'titleShow'          : false,
            'hideOnContentClick' : false,
            'showCloseButton'    : true,
            'overlayOpacity'     : 0.6,
            'transitionIn'       : 'none',
            'transitionOut'      : 'none',
            'centerOnScroll'     : true,
            'onStart'            : function(selectedArray, selectedIndex, selectedOpts) {
                var info = $(selectedArray).attr('info');
                if (info.indexOf('details-') == 0) {
                    info = info.substring('details-'.length);
                }
                var userKey = info.split('|')[0];
                $('#destinationUserKey').val(userKey);
                $('#messagesubject').val("<fmt:message key='label.replySubject'/>" + " " + info.substring(userKey.length + 1));
                $('#messagebody').val('');
            },
            'onClosed'           : function() {
                $("#login_error").hide();
            }
        });
    }
    <c:if test="${not renderContext.ajaxRequest}">
    $(document).ready(function() {
        userMessagesCallback();
    });
    </c:if>
</script>
<c:if test="${renderContext.ajaxRequest}">
    <template:addResources>
        <script type="text/javascript">
            userMessagesCallback();
        </script>
    </template:addResources>
</c:if>
                    <c:set var="ps" value="?pagerUrl=${url.mainResource}"/>
                    <c:if test="${!empty param.pageUrl}">
                        <c:set var="ps" value="?pagerUrl=${param.pageUrl}"/>
                    </c:if>
                    <c:set target="${moduleMap}" property="pagerUrl" value="${param.pagerUrl}"/>
                    <template:initPager totalSize="${moduleMap.end}" pageSize="${currentNode.properties['numberOfMessagesPerPage'].string}" id="${renderContext.mainResource.node.identifier}"/>
                    <template:displayPagination/>

                    <template:addCacheDependency path="${user.path}/messages/inbox"/>
                    <c:if test="${moduleMap.listTotalSize eq 0}">
                        <p><fmt:message key="message.emptyResults"/></p>
                    </c:if>
                    <c:if test="${moduleMap.listTotalSize ne 0}">
                        <ul class="userMessagesList">
                            <c:forEach items="${moduleMap.currentList}" var="userMessage" begin="${moduleMap.begin}" end="${moduleMap.end}">
                                <li id="social-message-${userMessage.identifier}">
                                    <template:module path="${userMessage.path}" />
                                </li>
                            </c:forEach>
                        </ul>
                    </c:if>
<div class='clear'></div>
<div class="jahiaFancyboxForm">
<div id="divSendMessage" >
    <div class="popup-bodywrapper">
        <h3 class="boxmessage-title"><fmt:message key="message.new"/></h3>

        <form class="formMessage" id="sendMessage" method="post" action="">
            <input type="hidden" name="j:to" id="destinationUserKey" value="" />
            <fieldset>
                <legend><fmt:message key="message.label.creation"/></legend>
                <p id="login_error" style="display:none;"><fmt:message key="message.enterData"/></p>

                <p><label for="messagesubject" class="left"><fmt:message key="message.label.subject"/></label>
                    <input type="text" name="j:subject" id="messagesubject" class="field" value=""
                           tabindex="20"/></p>


                <p><label for="messagebody" class="left"><fmt:message
                        key="message.label.body"/> :</label>
                    <textarea name="j:body" id="messagebody" cols="45" rows="3"
                              tabindex="21"></textarea></p>
                <input class="button" type="button" value="<fmt:message key='message.label.send'/>"
                       tabindex="28"
                       id="messagesendbutton" onclick="$('#sendMessage').submit();">
            </fieldset>
        </form>
    </div>
</div>
</div>
