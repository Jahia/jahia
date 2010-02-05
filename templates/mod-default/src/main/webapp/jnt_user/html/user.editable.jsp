<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:if test="${renderContext.user.name != currentNode.name}">
	<jsp:include page="user.jsp"/>
</c:if>
<c:if test="${renderContext.user.name == currentNode.name}">

<c:set var="fields" value="${currentNode.propertiesAsString}"/>
<c:set var="person" value="${fields['j:title']} ${fields['j:firstName']} ${fields['j:lastName']}"/>
<form action="${url.base}${currentNode.path}" method="post" name="userProfile">
<input type="hidden" name="methodToCall" value="put"/>
<input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}"/>
<jcr:nodeProperty var="picture" node="${currentNode}" name="j:picture"/>
<input type="hidden" name="j:picture" id="myAvatar" value="${not empty picture ? picture.string : ''}"/>
<%--
<ui:fileSelector fieldId="myAvatar" filters="*.bmp,*.gif,*.jpe,*.jpeg,*.jpg,*.png,*.tif,*.tiff" onSelect="function (path, url, uuid) { document.getElementById('myAvatar').value=uuid; document.getElementById('myAvatarPreview').src=url; document.getElementById('myAvatarPreview').width=120; document.getElementById('myAvatarPreview').height=120; return false; }"/>
 --%>
<div class="user-profile">
    <div class="user-photo">
        <c:if test="${not empty picture}">
            <img src="${picture.node.thumbnailUrls['avatar_120']}" alt="${fn:escapeXml(person)}" id="myAvatarPreview" />
        </c:if>
    </div>
    <div class="user-body">
        <h4>${fn:escapeXml(person)}&nbsp;(logged as: ${currentNode.name})</h4>
        <p>
            <span class="user-label">First name:</span>&nbsp;<input class="user-prop" type="text" name="j:firstName" value="${fn:escapeXml(fields['j:firstName'])}"/>
            <% /* TODO find a cleaner way to handle the boolean for public/private properties */ %>
            &nbsp;<input type="radio" id="j:firstNamePublicTrue" name="j:firstNamePublic" value="true"/>&nbsp;<label for="j:firstNamePublicTrue">public</label>
            &nbsp;<input type="radio" id="j:firstNamePublicFalse" name="j:firstNamePublic" value="false"/>&nbsp;<label for="j:firstNamePublicFalse">private</label>
            <script type="text/javascript">
                firstNameIsPublic = <c:out value="${fields['j:firstNamePublic']}" />;
                document.getElementById("j:firstNamePublicTrue").checked=firstNameIsPublic;
                document.getElementById("j:firstNamePublicFalse").checked=!firstNameIsPublic;
            </script>
        </p>
        <p><span class="user-label">Last name:</span>&nbsp;<input class="user-prop" type="text" name="j:lastName" value="${fn:escapeXml(fields['j:lastName'])}"/>
        <p><span class="user-label">Organization:</span>&nbsp;<input class="user-prop" type="text" name="j:organization" value="${fn:escapeXml(fields['j:organization'])}"/></p>
        <p><span class="user-label">Function:</span>&nbsp;<input class="user-prop" type="text" name="j:function" value="${fn:escapeXml(fields['j:function'])}"/></p>
        <p><span class="user-label">Phone:</span>&nbsp;<input class="user-prop" type="text" name="j:phoneNumber" value="${fn:escapeXml(fields['j:phoneNumber'])}"/></p>
        <p><span class="user-label">Fax:</span>&nbsp;<input class="user-prop" type="text" name="j:faxNumber" value="${fn:escapeXml(fields['j:faxNumber'])}"/></p>
        <p>
            <span class="user-label">Email:</span>&nbsp;<input class="user-prop" type="text" name="j:email" value="${fn:escapeXml(fields['j:email'])}"/>
            &nbsp;<input type="radio" id="j:emailPublicTrue" name="j:emailPublic" value="true"/>&nbsp;<label for="j:emailPublicTrue">public</label>
            &nbsp;<input type="radio" id="j:emailPublicFalse" name="j:emailPublic" value="false"/>&nbsp;<label for="j:emailPublicFalse">private</label>
            <script type="text/javascript">
                emailIsPublic = <c:out value="${fields['j:emailPublic']}" />;
                document.getElementById("j:emailPublicTrue").checked=emailIsPublic;
                document.getElementById("j:emailPublicFalse").checked=!emailIsPublic;
            </script>
        </p>
        <p><span class="user-label">Skype:</span>&nbsp;<input class="user-prop" type="text" name="j:skypeID" value="${fn:escapeXml(fields['j:skypeID'])}"/></p>
        <p><span class="user-label">Twitter:</span>&nbsp;<input class="user-prop" type="text" name="j:twitterID" value="${fn:escapeXml(fields['j:twitterID'])}"/></p>
        <p><span class="user-label">LinkedIn:</span>&nbsp;<input class="user-prop" type="text" name="j:linkedinID" value="${fn:escapeXml(fields['j:linkedinID'])}"/></p>
        <p><span class="user-label">About me:</span><textarea class="user-prop" name="j:about">${fields['j:about']}</textarea></p>
    </div>
    <input type="submit" value="Update profile" class="button"/>
</div>
</form>
<div><a href="${url.base}${currentNode.path}.html">Back to preview</a></div>
</c:if>
