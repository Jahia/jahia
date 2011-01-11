<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ include file="user.profileCss.jspf" %>
<c:set var="fields" value="${currentNode.propertiesAsString}"/>
<c:set var="person" value="${fields['j:title']} ${fields['j:firstName']} ${fields['j:lastName']}"/>
<div class="user-profile">
    <div class="user-photo">
        <jcr:nodeProperty var="picture" node="${currentNode}" name="j:picture"/>
        <c:if test="${not empty picture}">
            <img src="${picture.node.thumbnailUrls['avatar_120']}" alt="${fn:escapeXml(person)}"/>
        </c:if>
    </div>
    <div class="user-body">
        <h5><template:isPublic userKey="${currentNode.name}" property="j:firstName">${fn:escapeXml(person)}&nbsp;</template:isPublic>(logged as: ${currentNode.name})</h5>
    </div>
    <div>
        <p><span class="user-label"><fmt:message key="jnt_user.j_organization"/>:</span>&nbsp;${fn:escapeXml(fields['j:organization'])}</p>
        <p><span class="user-label"><fmt:message key="jnt_user.j_function"/>:</span>&nbsp;${fn:escapeXml(fields['j:function'])}</p>
        <p><span class="user-label"><fmt:message key="jnt_user.j_phoneNumber"/>:</span>&nbsp;${fn:escapeXml(fields['j:phoneNumber'])}</p>
        <p><span class="user-label"><fmt:message key="jnt_user.j_faxNumber"/>:</span>&nbsp;${fn:escapeXml(fields['j:faxNumber'])}</p>
        <template:isPublic userKey="${currentNode.name}" property="j:email">
        <p><span class="user-label"><fmt:message key="jnt_user.j_email"/>:</span>&nbsp;<a href="mailto:${fields['j:email']}">${fields['j:email']}</a></p>
        </template:isPublic>
        <p><span class="user-label"><fmt:message key="jnt_user.j_skypeID"/>:</span>&nbsp;${fields['j:skypeID']}
        	<c:if test="${not empty fields['j:skypeID']}">
        		<a href="skype:${fields['j:skypeID']}?call"><img src="http://download.skype.com/share/skypebuttons/buttons/call_green_transparent_70x23.png" style="border: none;" width="70" height="23" alt="${fields['j:skypeID']}" /></a>
        	</c:if>
        </p>
        <p><span class="user-label"><fmt:message key="jnt_user.j_twitterID"/>:</span>&nbsp;${fields['j:twitterID']}
        	<c:if test="${not empty fields['j:twitterID']}">
        		<a href="http://twitter.com/${fields['j:twitterID']}" target="_blank"><img src="http://twitbuttons.com/buttons/siahdesign/twit1.gif" alt="${fields['j:twitterID']}" width="144" height="30"/></a>
        	</c:if>
       	</p>
        <p><span class="user-label"><fmt:message key="jnt_user.j_linkedinID"/>:</span>&nbsp;<a href="http://www.linkedin.com/in/{fields['j:linkedinID']}">${fields['j:linkedinID']}</a></p>
    </div>
    <div>
        <h5><fmt:message key="label.aboutMe"/></h5>
        <p>${fields['j:about']}</p>
    </div>
    <div>
        <jcr:sql var="result" sql="select * from [jnt:page] as p where p.[jcr:createdBy]='${currentNode.name}'"/>
        <h5><fmt:message key="label.myPages"/></h5>
        <c:if test="${result.nodes.size == 0}">
            <p>${currentNode.name} <fmt:message key="label.noPageCreated"/></p>
        </c:if>
        <ul>
            <c:forEach items="${result.nodes}" var="page">
                <jcr:nodeProperty node="${page}" name="jcr:title" var="title"/>
                <li>
                    <a href="${url.base}${page.path}.html">${title.string}</a>
                </li>
            </c:forEach>
        </ul>
    </div>
</div>
<c:if test="${renderContext.user.name == currentNode.name}">
<div><a href="${url.base}${currentNode.path}.editable.html">Edit my profile</a></div>
</c:if>