<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:set var="fromUser" value="${currentNode.properties['j:from'].node}"/>
<ul class="messageActionList">
   <li><a class="messageActionDelete" title="<fmt:message key='deleteMessage'/>" href="#delete" info="${currentNode.identifier}"><span><fmt:message
        key="deleteMessage"/></span></a></li>
  <li><a class="messageActionReply" title="<fmt:message key='replyToMessage'/>" href="#divSendMessage" info="${fromUser.name}|${fn:escapeXml(currentNode.propertiesAsString['j:subject'])}"><span><fmt:message key="replyToMessage"/></span></a></li>

</ul>
<div class='image'>
	<div class='itemImage itemImageLeft'>
			<jcr:nodeProperty var="picture" node="${fromUser}" name="j:picture"/>
			<c:if test="${not empty picture}">
	            <a href="<c:url value='${url.base}${fromUser.path}.html'/>"><img
	                    src="${picture.node.thumbnailUrls['avatar_120']}"
	                    alt="${userNode.properties.title.string} ${userNode.properties.firstname.string} ${userNode.properties.lastname.string}"
	                    width="64"
	                    height="64"/></a>
	        </c:if>
	        <c:if test="${empty picture}"><a href="<c:url value='${url.base}${fromUser.path}.html'/>">
				<img alt="" src="<c:url value='${url.currentModule}/images/friend.png'/>" alt="friend" border="0"/></a></c:if>
	</div>
</div>
<h5 class="messageSenderName">
    <a href="<c:url value='${url.base}${fromUser.path}.html'/>"><c:out value="${jcr:userFullName(fromUser)}"/></a>
</h5><jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/><span class="timestamp"><fmt:formatDate
value="${lastModified.time}" pattern="yyyy/MM/dd HH:mm"/></span>
<h5><a class="messageDetailLink" href="#socialMessageDetail" info="<c:url value='${url.base}${currentNode.path}.detail.html.ajax'/>">${fn:escapeXml(currentNode.propertiesAsString['j:subject'])}</a></h5>
<p>${fn:escapeXml(fn:substring(currentNode.propertiesAsString['j:body'], 0, 100))}...</p>

<div class='clear'></div>