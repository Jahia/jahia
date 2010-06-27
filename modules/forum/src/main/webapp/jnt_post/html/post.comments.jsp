<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${currentNode}" name="content" var="content"/>
<jcr:node var="userNode" path="/users/${createdBy.string}"/>
<li class="docspaceitemcomment">
    <span class="public floatright"><input name="" type="checkbox" value=""/> <fmt:message key="docspace.label.public"/></span>

    <div class="image">
        <div class="itemImage itemImageLeft">

            <jcr:nodeProperty var="picture" node="${userNode}" name="j:picture"/>
            <c:if test="${not empty picture}">
                <a href="${url.base}${renderContext.site.path}/users/${createdBy.string}.html"><img
                        src="${picture.node.thumbnailUrls['avatar_60']}"
                        alt="${userNode.properties.title.string} ${userNode.properties.firstname.string} ${userNode.properties.lastname.string}"
                        width="60"
                        height="60"/></a>
            </c:if>
            <c:if test="${empty picture}"><a href="${url.base}${renderContext.site.path}/users/${createdBy.string}.html"><img alt=""
                                                                                                    src="${url.currentModule}/css/img/userbig.png"/></a></c:if>
        </div>
    </div>

    <h5 class="title"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h5>
    <jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/>
    <span class="docspacedate timestamp"><fmt:formatDate value="${lastModified.time}"
                                                         pattern="yyyy/MM/dd HH:mm"/></span>

    <p>
        <span class="author">
        <a href="${url.base}${renderContext.site.path}/users/${createdBy.string}.html">${createdBy.string}</a>:&nbsp;</span>
        ${content.string}
    </p>

    <div class='clear'></div>
</li>