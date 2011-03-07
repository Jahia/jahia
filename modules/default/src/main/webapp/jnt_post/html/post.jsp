<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${currentNode}" name="content" var="content"/>
<c:if test="${createdBy.string ne ' guest '}">
    <jcr:node var="userNode" path="/users/${createdBy.string}"/>
</c:if>
<li class="genericListCommentLi">
    <div class="image">
        <div class="itemImage itemImageLeft">

            <jcr:nodeProperty var="picture" node="${userNode}" name="j:picture"/>
            <c:if test="${not empty picture}">
                <%--a href="${url.base}${renderContext.site.path}/users/${createdBy.string}.html"--%><img
                        src="${picture.node.thumbnailUrls['avatar_60']}"
                        alt="${userNode.properties.title.string} ${userNode.properties.firstname.string} ${userNode.properties.lastname.string}"
                        width="60"
                        height="60"/><%--/a--%>
            </c:if>
            <c:if test="${empty picture}"><%--a href="${url.base}${renderContext.site.path}/users/${createdBy.string}.html"--%><img alt=""
                                                                                                    src="${url.currentModule}/images/userbig.png"/></a></c:if>
        </div>
    </div>

    <h5 class="commentTitle"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h5>
    <jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/>
    <span class="docspacedate timestamp"><fmt:formatDate value="${lastModified.time}"
                                                         pattern="yyyy/MM/dd HH:mm"/></span>

    <p>
        <span class="author">
            <c:if test="${createdBy.string ne 'guest'}">
            <a href="${url.base}/users/${createdBy.string}.html">${createdBy.string}</a></c:if>
            <c:if test="${createdBy.string eq 'guest'}">guest</c:if>:&nbsp;</span>
        ${fn:escapeXml(content.string)}
    </p>

    <div class='clear'></div>
</li>