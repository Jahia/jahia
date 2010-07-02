<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="social" uri="http://www.jahia.org/tags/socialLib" %>
<c:set var="fields" value="${currentNode.propertiesAsString}"/>
<c:set var="message" value="${fields['j:message']}" />
<c:if test="${not empty fields['j:messageKey']}">
    <c:set var="message"><fmt:message key="${fields['j:messageKey']}"/></c:set>
</c:if>
<c:set var="imageURL" value="${url.currentModule}/images/friendbig.png"/>
<c:if test="${not empty fields['j:picture']}">
    <c:set var="imageURL" value="${currentNode.properties['j:picture'].node.url}"/>
</c:if>
<li>
    <div class='image'>
        <div class='itemImage itemImageLeft'>
            <img src="${imageURL}"/>
        </div>
    </div>
    <h5 class='author'> ${currentNode.properties["jcr:createdBy"].string} </h5>

    <c:set var="targetNode" value="${currentNode.properties['j:targetNode'].node}" />
    <p class='message'>${message} <a href="${url.base}${targetNode.path}">${targetNode.properties['jcr:title'].string}</a> </p>

    <jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/>
    <span class="timestamp"><fmt:formatDate value="${lastModified.time}" pattern="yyyy/MM/dd HH:mm:ss"/></span>

    <div class='clear'></div>
</li>