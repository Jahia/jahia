<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:set var="fromUser" value="${currentNode.properties['j:from'].node}"/>
<div class="boxsocial userMessagesDetail"><!--start boxsocial -->
    <div class="boxsocialpadding16 boxsocialmarginbottom16">
        <div class="boxsocial-inner">
            <div class="boxsocial-inner-border">
                <ul class="messageActionList">
                   <li><a class="messageActionDelete" title="<fmt:message key="deleteMessage"/>" href="#"><span><fmt:message
                        key="deleteMessage"/></span></a></li>
                    <li><a class="messageActionReply" title="<fmt:message key="replyToMessage"/>" id="showSendMessage"
                   href="#divSendMessage"><span><fmt:message key="replyToMessage"/></span></a></li>
                </ul>
                <div class='image'>
                    <div class='itemImage itemImageLeft'>
                        <a href="${url.base}${fromUser.path}.html"><img src="${url.currentModule}/images/friendbig.png"
                                                                         alt="friend" border="0"/></a>
                    </div>
                </div>
                <h5 class="messageSenderName">
                    <a href="${usl.base}${fromUser.path}.html">${fromUser.properties['j:firstName'].string} ${fromUser.properties['j:lastName'].string}</a>
                </h5><span class="timestamp" data-timestamp="Thu, 24 Jun 2010 10:56:41 GMT">25 minutes ago</span>
                <h5>${currentNode.properties['j:subject'].string}</h5>
                <p>${currentNode.properties['j:body'].string}</p>

            <div class='clear'></div>
			</div>
		</div>
	</div>
	<div class='clear'></div>
</div>
