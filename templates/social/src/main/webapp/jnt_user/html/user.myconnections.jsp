<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<jsp:useBean id="now" class="java.util.Date"/>
<template:addResources type="css" resources="social.css"/>
<template:addResources type="javascript" resources="jquery.fancybox-1.3.1.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.ajaxQueue.js" />
<template:addResources type="javascript" resources="jquery.autocomplete.js" />
<template:addResources type="javascript" resources="jquery.bgiframe.min.js" />
<template:addResources type="javascript" resources="thickbox-compressed.js" />

<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery-ui.datepicker.min.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<template:addResources type="javascript" resources="jquery.cuteTime.js"/>


<c:set var="fields" value="${currentNode.propertiesAsString}"/>
<jcr:nodePropertyRenderer node="${currentNode}" name="j:title" renderer="resourceBundle" var="title"/>
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
<jcr:nodeProperty node="${currentNode}" name="j:birthDate" var="birthDate"/>
<c:if test="${not empty birthDate}">
    <fmt:formatDate value="${birthDate.date.time}" pattern="yyyy" var="birthYear"/>
    <fmt:formatDate value="${now}" pattern="yyyy" var="currentYear"/>
</c:if>
<c:if test="${not empty birthDate}">
    <fmt:formatDate value="${birthDate.date.time}" pattern="dd/MM/yyyy" var="editBirthDate"/>
</c:if>
<fmt:formatDate value="${now}" pattern="dd/MM/yyyy" var="editNowDate"/>
<jcr:propertyInitializers node="${currentNode}" name="j:gender" var="genderInit"/>
<jcr:propertyInitializers node="${currentNode}" name="j:title" var="titleInit"/>
<%--map all display values --%>
<jsp:useBean id="userProperties" class="java.util.HashMap"/>

<script type="text/javascript">

    /**
     * As any property can match the query, we try to intelligently display properties that either matched or make
     * sense to display.
     * @param node
     */
    function getText(node) {
        if (node.matchingProperties.length > 0) {
            var firstMatchingProperty = node.matchingProperties[0];
            return node[firstMatchingProperty];
        }
        if (node["jcr:title"] != null) {
            return node["jcr:title"];
        } else if (node["text"] != null) {
            return node["text"];
        } else if (node["j:nodename"] != null) {
            return node["j:nodename"];
        }
    }

    function format(result) {
        return getText(result["node"]);
    }


    function searchUsers(term) {
        $.ajax({
            url: '${url.findPrincipal}',
            type: 'post',
            dataType : 'json',
            data : "principalType=users&wildcardTerm=" + term + "*",
            success: function(data) {
                $("#searchUsersResult").html("");
                $.each(data, function(i, item) {
                    $("#searchUsersResult").append(
                            $("<tr/>").append($("<td/>").append($("<img/>").attr("src", item.properties['j:picture'])))
                                    .append($("<td/>").text(item.properties['j:firstName'] + " " + item.properties['j:lastName']))
                                    .append($("<td/>").attr("align", "center").append($("<a/>").attr("href", "").attr("class", "social-add").click(function () {
                                requestConnection('${currentNode.path}.startWorkflow.do', item['userKey']);
                                return false;
                            }).append($("<span/>").text("<fmt:message key='addAsFriend'/>"))))
                            );
                    if (i == 10) return false;
                });
            }
        });
    }

    function requestConnection(actionURL, toUserKey) {
        $.ajax({
            url : '${url.base}' + actionURL,
            type : 'post',
            data : 'process=jBPM:user-connection&userkey=' + toUserKey,
            success : function (data) {
                alert("Request completed successfully!");
            }
        });
    }


    function submitStatusUpdate(updateText) {
        $.ajax({
            url: '${url.base}${currentNode.path}/activities/*',
            type : 'post',
            data : 'nodeType=jnt:userActivity&newNodeOutputFormat=html&j:message=' + updateText + "&j:from=${currentNode.identifier}",
            success : function (data) {
                // alert("Status update submitted successfully");
                loadActivities();
            }
        });
    }

    function loadActivities() {
        $.ajax({
            url: '${url.base}${currentNode.path}.getactivities.do',
            type: 'post',
            dataType : "json",
            success : function (data) {
                $(".activitiesList").html("");
                // alert(data.resultCount + " activities loaded properly");
                $.each(data['activities'], function(i, item) {
                    var activityDate = new Date();
                    activityDate.setTime(item['jcr:created']);
                    var imageURL = item['j:picture'];
                    if (imageURL == null) {
                        imageURL = "${url.currentModule}/images/friendbig.png";
                    }
                    $(".activitiesList").append(
                            "<li>" +
                                    "<div class='image'>" +
                                    "<div class='itemImage itemImageLeft'>" +
                                    "<img src=" + imageURL + " />" +
                                    "</div>" +
                                    "</div>" +
                                    "<h5 class='author'>" + item['jcr:createdBy'] + "</h5>" +
                                    "<span class='timestamp'>" + activityDate.toUTCString() + "</span>" +
                                    "<p class='message'>" + item['j:message'] + "</p> " +
                                    "<div class='clear'></div>" +
                                    "</li>"
                            );

                });
                initCuteTime();
            }

        });
        return false;
    }

    function initCuteTime() {
        $('.timestamp').cuteTime({ refresh: 60000 });
    }


    function tabCallback() {

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
                url         : '${url.base}${currentNode.path}.sendmessage.do',
                data        : $(this).serializeArray(),
                success     : function(data) {
                    alert("Message sent.");
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
            'onStart'            : function() {
                var userKey = $(this).attr('id');
                alert("this="+ $(this).localName + " userKey="+userKey);
                $('#destinationUserKey').val(userKey);
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
                        value: getText(row["node"]),
                        result: getText(row["node"])
                    }
                });
            },
            formatItem: function(item) {
                return format(item);
            },
            extraParams: {
                principalType : "users",
                wildcardTerm : "{$q}*",
                propertyMatchRegexp : "{$q}.*",
                removeDuplicatePropValues : "true"                
            }
        });
        $("#searchUsersSubmit").click(function() {
            // validate and process form here
            var term = $("input#searchUsersTerm").val();
            searchUsers(term);
            return false;
        });
        $("#statusUpdateSubmit").click(function() {
            // validate and process form here
            var updateText = $("textarea#statusUpdateText").val();
            // alert('Sending text ' + updateText);
            submitStatusUpdate(updateText);
            return false;
        });

        loadActivities();
        
    }

    $(document).ready(function() {
    });
</script>


<div class='grid_12 alpha'><!--start grid_12-->
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
                        <li>Loading status...</li>
                    </ul>
                </div>
            </div>
        </div>
        <div class='clear'></div>
    </div>
</div>
<!--stop grid_12-->
<div class='grid_4 omega'><!--start grid_4-->

    <h3><fmt:message key="userSearch"/></h3>

    <form method="get" class="simplesearchform" action="">

        <jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
        <c:if test="${not empty title.string}">
            <label for="searchUsersTerm">${fn:escapeXml(title.string)}:&nbsp;</label>
        </c:if>
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

    <jcr:sql var="userConnections"
             sql="select * from [jnt:userConnection] as uC where isdescendantnode(uC,['${currentNode.path}'])"/>

    <h3 class="social-title-icon titleIcon"><a href="#"><fmt:message key="friendsList"/></a><a href="#"><img title="" alt=""
                                                                                    src="${url.currentModule}/images/friends.png"/></a>
    </h3>
    <ul class="social-list">
        <c:forEach items="${userConnections.nodes}" var="userConnection">
            <li>
                <c:set var="connectedToUser" value="${userConnection.properties['j:connectedTo'].node}"/>
                <div class="thumbnail">

                    <a href="${url.base}${connectedToUser.path}.html"><img src="${url.currentModule}/images/friend.png"
                                                                         alt="friend" border="0"/></a>
                </div>
                <a class="social-list-sendmessage showSendMessage" title="<fmt:message key="sendMessage"/>" id="${connectedToUser.properties['j:nodename'].string}"
                   href="#divSendMessage"><span><fmt:message key="sendMessage"/></span></a>
                <a class="social-list-remove" title="<fmt:message key="removeFriend"/>" href="#"><span><fmt:message
                        key="removeFriend"/></span></a>
                <h4>
                    <a href="${usl.base}${connectedToUser.path}.html">${connectedToUser.properties['j:firstName'].string} ${connectedToUser.properties['j:lastName'].string}</a>
                </h4>

                <div class='clear'></div>
            </li>
        </c:forEach>
    </ul>


    <div class='clear'></div>


</div>
<!--stop grid_4-->

<div class='clear'></div>

<div id="divSendMessage">
    <div class="popup-bodywrapper">
        <h3 class="boxmessage-title"><fmt:message key="message.new"/></h3>

        <form class="formMessage" id="sendMessage" method="post" action="">
            <input type="hidden" name="j:to" id="destinationUserKey" value="{jcr}sjobs" />
            <fieldset>
                <legend><fmt:message key="message.label.creation"/></legend>
                <p id="login_error" style="display:none;">Please, enter data</p>

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

