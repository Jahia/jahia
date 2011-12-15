<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${currentNode}" name="content" var="content"/>
<c:if test="${createdBy.string ne ' guest '}">
    <jcr:node var="userNode" path="${functions:lookupUser(createdBy.string).localPath}"/>
    <c:forEach items="${userNode.properties}" var="property">
        <c:if test="${property.name == 'j:firstName'}"><c:set var="firstname" value="${property.string}"/></c:if>
        <c:if test="${property.name == 'j:lastName'}"><c:set var="lastname" value="${property.string}"/></c:if>
        <c:if test="${property.name == 'j:email'}"><c:set var="email" value="${property.string}"/></c:if>
        <c:if test="${property.name == 'j:title'}"><c:set var="title" value="${property.string}"/></c:if>
    </c:forEach>
</c:if>
<li class="genericListCommentLi">
    <div class="image">
        <div class="itemImage itemImageLeft">

            <jcr:nodeProperty var="picture" node="${userNode}" name="j:picture"/>
            <c:if test="${not empty picture}"><img
                    src="${picture.node.thumbnailUrls['avatar_60']}"
                    alt="${fn:escapeXml(title)} ${fn:escapeXml(firstname)} ${fn:escapeXml(lastname)}"
                    width="60"
                    height="60"/>
            </c:if>
            <c:if test="${empty picture}"><img alt="" src="<c:url value='${url.currentModule}/images/userbig.png'/>"/></c:if>
        </div>
    </div>

    <h5 class="commentTitle"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h5>
    <jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/>
    <span class="docspacedate timestamp"><fmt:formatDate value="${lastModified.time}"
                                                         pattern="yyyy/MM/dd HH:mm"/></span>

    <p>
        <span class="author">
            <c:if test="${createdBy.string ne 'guest'}">
                <a href="<c:url value='${url.base}${functions:lookupUser(createdBy.string).localPath}.html'/>">${createdBy.string}</a></c:if>
            <c:if test="${createdBy.string eq 'guest'}">${fn:escapeXml(currentNode.properties.pseudo.string)}</c:if>:&nbsp;</span>
        ${fn:escapeXml(content.string)}
    </p>

    <div class='clear'></div>
</li>