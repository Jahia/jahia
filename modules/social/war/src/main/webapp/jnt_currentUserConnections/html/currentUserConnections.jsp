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

    function userConnectionsCallback() {

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

        $(".showSendMessage").fancybox({
            'scrolling'          : 'no',
            'titleShow'          : false,
            'hideOnContentClick' : false,
            'showCloseButton'    : true,
            'overlayOpacity'     : 0.6,
            'transitionIn'       : 'none',
            'transitionOut'      : 'none',
            'centerOnScroll'     : true,
            'onStart'            : function(selectedArray, selectedIndex, selectedOpts) {
                var userKey = $(selectedArray).attr('rel');
                $('#destinationUserKey').val(userKey);
                $('#messagesubject').val('');
                $('#messagebody').val('');
            },
            'onClosed'           : function() {
                $("#login_error").hide();
            }
        });

        $("#searchUsersTerm").autocomplete("${url.findPrincipal}", {
            dataType: "json",
            cacheLength: 1,
            parse: function parse(data) {
                return $.map(data, function(row) {
                    return {
                        data: row,
                        value: getText(row),
                        result: getText(row)
                    }
                });
            },
            formatItem: function(item) {
                return format(item);
            },
            extraParams: {
                principalType : "users",
                propertyMatchRegexp : "{$q}.*",
                includeCriteriaNames : "username,j:nodename,j:firstName,j:lastName",
                "username": "{$q}*",
                "j:nodename": "{$q}*",
                "j:firstName": "{$q}*",
                "j:lastName": "{$q}*",
                removeDuplicatePropValues : "true"
            }
        }).result(function(event, item, formatted) {
			if (!item || !item.properties) {
        		return;
        	}
            $("#searchUsersResult")
            	.html("")
            	.append(
                    $("<tr/>").append($("<td/>").append($("<img/>").attr("src", item.properties['j:picture'])))
                            .append($("<td/>").attr("title", item['username']).text(getUserDisplayName(item)))
                            .append($("<td/>").attr("align", "center").append($("<a/>").attr("href", "#add")
                            .attr("class", "social-add").attr("title", "<fmt:message key='addAsFriend'/>").click(function () {
                        requestConnection('<c:url value="${url.base}${user.path}.requestsocialconnection.do"/>', item['userKey']);
                        return false;
                    })))
                );

        });

        $("#searchUsersSubmit").click(function() {
            // validate and process form here
            var term = $("input#searchUsersTerm").val();
            searchUsers('${url.findPrincipal}', '<c:url value="${url.base}${user.path}"/>', term, "<fmt:message key='addAsFriend'/>");
            return false;
        });

        $("a.removeFriendAction").click(function(e){
            e.preventDefault();
            var rel = $(this).attr('rel');
            var fromUserId = rel.substring(0, rel.indexOf(':'));
            rel = rel.substring(rel.indexOf(':') + 1);
            var toUserId = rel.substring(0, rel.indexOf(':'));
            var connectionType = rel.substring(rel.indexOf(':') + 1);
            if (confirm("<fmt:message key='message.removeFriend.confirm'/>")) {
                removeSocialConnection('<c:url value="${url.base}${user.path}"/>', fromUserId, toUserId, connectionType,
                        function() {
                            $("#connection-to-" + toUserId).remove();
                        });
            }
        });

    }

    <c:if test="${not renderContext.ajaxRequest}">
    $(document).ready(function() {
        userConnectionsCallback();
    });
    </c:if>
</script>
<c:if test="${renderContext.ajaxRequest}">
    <template:addResources>
        <script type="text/javascript">
            userConnectionsCallback();
        </script>
    </template:addResources>
</c:if>


    <h3><fmt:message key="userSearch"/></h3>

    <form method="get" class="simplesearchform" action="">
        <fmt:message key='userSearch' var="startSearching"/>
        <input type="text" id="searchUsersTerm" value="${startSearching}"
               onfocus="if(this.value==this.defaultValue)this.value='';"
               onblur="if(this.value=='')this.value=this.defaultValue;" class="text-input"/>
        <input class="searchsubmit" id="searchUsersSubmit" type="submit" title="<fmt:message key='search.submit'/>"/>
    </form>
    <br class="clear"/>

    <div>
        <table width="100%" class="table">
            <tbody id="searchUsersResult">

            </tbody>
        </table>
    </div>

    <jcr:sql var="socialConnections"
             sql="select * from [jnt:socialConnection] as uC where isdescendantnode(uC,['${user.path}'])"/>
    <template:addCacheDependency path="${user.path}/connections"/>
    <h3 class="social-title-icon titleIcon"><fmt:message key="friendsList"/><img title="" alt="" src="${url.currentModule}/images/friends.png"/>
    </h3>
    <ul class="social-list">
        <c:forEach items="${socialConnections.nodes}" var="socialConnection">
            <c:set var="connectedToUser" value="${socialConnection.properties['j:connectedTo'].node}"/>
            <li id="connection-to-${connectedToUser.identifier}">
                <div class="thumbnail">
					<jcr:nodeProperty var="picture" node="${connectedToUser}" name="j:picture"/>
					<c:if test="${not empty picture}">
			            <a href="<c:url value='${url.base}${connectedToUser.path}.html'/>"><img
			                    src="${picture.node.thumbnailUrls['avatar_60']}"
			                    alt="${userNode.properties.title.string} ${userNode.properties.firstname.string} ${userNode.properties.lastname.string}"
			                    width="32"
			                    height="32"/></a>
			        </c:if>
			        <c:if test="${empty picture}"><a href="<c:url value='${url.base}${connectedToUser.path}.html'/>">
						<img alt="" src="${url.currentModule}/images/friend.png" alt="friend" border="0"/></a></c:if>
                </div>
                <a class="social-list-remove removeFriendAction" title="<fmt:message key="removeFriend"/>" href="currentUserConnections.jsp#"
                   rel="${socialConnection.properties['j:connectedFrom'].node.identifier}:${socialConnection.properties['j:connectedTo'].node.identifier}:${socialConnection.properties['j:type'].string}"><span><fmt:message
                        key="removeFriend"/></span></a>
                <a class="social-list-sendmessage showSendMessage" title="<fmt:message key="sendMessage"/>" rel="${connectedToUser.name}"
                   href="#divSendMessage"><span><fmt:message key="sendMessage"/></span></a>
                <h4>
                    <a href="<c:url value='${url.base}${connectedToUser.path}.html'/>"><c:out value="${jcr:userFullName(connectedToUser)}"/></a>
                </h4>

                <div class='clear'></div>
            </li>
        </c:forEach>
    </ul>


    <div class='clear'></div>

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
                <input class="button" type="button" value="<fmt:message key="message.label.send"/>"
                       tabindex="28"
                       id="messagesendbutton" onclick="$('#sendMessage').submit();">
            </fieldset>
        </form>
    </div>
</div>
</div>