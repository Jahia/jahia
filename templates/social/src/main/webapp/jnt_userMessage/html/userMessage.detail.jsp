<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:set var="fromUser" value="${currentNode.properties['j:from'].node}"/>
<div class="messageSenderImage">
    <a href="${url.base}${fromUser.path}.html"><img src="${url.currentModule}/images/friend.png"
                                                         alt="friend" border="0"/></a>
</div>
<div class="messageSenderName">
    <a href="${usl.base}${fromUser.path}.html">${fromUser.properties['j:firstName'].string} ${fromUser.properties['j:lastName'].string}</a>
</div>
<h2>${currentNode.properties['j:subject'].string}</h2>
<p>${currentNode.properties['j:body'].string}</p>
<ul class="messageActionList">
    <li><a class="messageActionReply" title="<fmt:message key="replyToMessage"/>" id="showSendMessage"
   href="#divSendMessage"><span><fmt:message key="replyToMessage"/></span></a></li>
   <li><a class="messageActionDelete" title="<fmt:message key="deleteMessage"/>" href="#"><span><fmt:message
        key="deleteMessage"/></span></a></li>
</ul>
<div class='clear'></div>
