<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:if test="${renderContext.user.name != currentNode.name}">
	<jsp:include page="default.jsp"/>
</c:if>
<c:if test="${renderContext.user.name == currentNode.name}">
<%@ include file="profileCss.jspf" %>
<c:set var="fields" value="${currentNode.propertiesAsString}"/>
<c:set var="person" value="${fields['j:title']} ${fields['j:firstName']} ${fields['j:lastName']}"/>
<form action="${url.base}${currentNode.path}" method="post">
<input type="hidden" name="methodToCall" value="put"/>
<input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}"/>
<div class="user-profile">
    <div class="user-photo">
        <jcr:nodeProperty var="picture" node="${currentNode}" name="j:picture"/>
        <c:if test="${not empty picture}">
            <img src="${picture.node.thumbnailUrls['avatar_120']}" alt="${fn:escapeXml(person)}"/>
        </c:if>
    </div>
    <div class="user-body">
        <h4>${fn:escapeXml(person)}&nbsp;(logged as: ${currentNode.name})</h4>
        <p><span class="user-label">First name:</span>&nbsp;<input type="text" name="j:firstName" value="${fn:escapeXml(fields['j:firstName'])}"/></p>
        <p><span class="user-label">Last name:</span>&nbsp;<input type="text" name="j:lastName" value="${fn:escapeXml(fields['j:lastName'])}"/></p>
        <p><span class="user-label">Organization:</span>&nbsp;<input type="text" name="j:organization" value="${fn:escapeXml(fields['j:organization'])}"/></p>
        <p><span class="user-label">Function:</span>&nbsp;<input type="text" name="j:function" value="${fn:escapeXml(fields['j:function'])}"/></p>
        <p><span class="user-label">Phone:</span>&nbsp;<input type="text" name="j:phoneNumber" value="${fn:escapeXml(fields['j:phoneNumber'])}"/></p>
        <p><span class="user-label">Fax:</span>&nbsp;<input type="text" name="j:faxNumber" value="${fn:escapeXml(fields['j:faxNumber'])}"/></p>
        <p><span class="user-label">Email:</span>&nbsp;<input type="text" name="j:email" value="${fn:escapeXml(fields['j:email'])}"/></p>
        <p><span class="user-label">Skype:</span>&nbsp;<input type="text" name="j:skypeID" value="${fn:escapeXml(fields['j:skypeID'])}"/></p>
        <p><span class="user-label">Twitter:</span>&nbsp;<input type="text" name="j:twitterID" value="${fn:escapeXml(fields['j:twitterID'])}"/></p>
        <p><span class="user-label">LinkedIn:</span>&nbsp;<input type="text" name="j:linkedinID" value="${fn:escapeXml(fields['j:linkedinID'])}"/></p>
        <p><span class="user-label">About me:</span><textarea name="j:about">${fields['j:about']}</textarea></p>
    </div>
    <input type="submit" value="Update profile" class="button"/>
</div>
</form>
<div><a href="${url.base}${currentNode.path}.html">Back to preview</a></div>
</c:if>
