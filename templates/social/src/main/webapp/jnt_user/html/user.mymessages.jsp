<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="social.css"/>

<div class='grid_12 alpha'><!--start grid_12-->

    <jcr:sql var="receivedMessages"
             sql="select * from [jnt:userMessage] as uC where isdescendantnode(uC,['${currentNode.path}/inboundMessages'])"/>

    <h3 class="social-title-icon titleIcon"><a href="#"><fmt:message key="receivedMessages"/></a><a href="#"><img title="" alt=""
                                                                                    src="${url.currentModule}/images/mailbox.png"/></a>
    </h3>
    <ul class="social-list">
        <c:forEach items="${receivedMessages.nodes}" var="userMessage">
            <li>
                <c:set var="fromUser" value="${userMessage.properties['j:from'].node}"/>
                <div class="messageSenderImage">
                    <a href="${url.base}${fromUser.path}.html"><img src="${url.currentModule}/images/friend.png"
                                                                         alt="friend" border="0"/></a>
                </div>
                <div class="messageSenderName">
                    <a href="${usl.base}${fromUser.path}.html">${fromUser.properties['j:firstName'].string} ${fromUser.properties['j:lastName'].string}</a>
                </div>
                <h2>${userMessage.properties['j:subject'].string}</h2>
                <p>${userMessage.properties['j:body'].string}</p>
                <ul class="messageActionList">
                    <li><a class="messageActionReply" title="<fmt:message key="replyToMessage"/>" id="showSendMessage"
                   href="#divSendMessage"><span><fmt:message key="replyToMessage"/></span></a></li>
                   <li><a class="messageActionDelete" title="<fmt:message key="deleteMessage"/>" href="#"><span><fmt:message
                        key="deleteMessage"/></span></a></li>
                </ul>
                <div class='clear'></div>
            </li>
        </c:forEach>
    </ul>

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
            <thead>
            <tr>
                <th><fmt:message key="userIcon"/></th>
                <th><fmt:message key="userInfo"/></th>
                <th><fmt:message key="userActions"/></th>
            </tr>
            </thead>
            <tbody id="searchUsersResult">

            </tbody>
        </table>
    </div>

    <div class='clear'></div>


</div>
<!--stop grid_4-->

<div class='clear'></div>

<div id="divSendMessage">
    <div class="popup-bodywrapper">
        <h3 class="boxmessage-title"><fmt:message key="message.new"/></h3>

        <form class="formMessage" id="sendMessage" method="post" action="">
            <input type="hidden" name="j:to" value="{jcr}sjobs" />
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

