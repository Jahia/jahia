<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="forum.css" nodetype="jnt:post"/>
<%-- Get all contents --%>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="content" var="content"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
<c:if test="${currentNode.propertiesAsString['jcr:createdBy'] == renderContext.user.name}">
    <form action="${url.base}${currentNode.path}" method="post"
          id="jahia-forum-post-delete-${currentNode.UUID}">
        <input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}"/>
            <%-- Define the output format for the newly created node by default html or by stayOnNode--%>
        <input type="hidden" name="newNodeOutputFormat" value="html"/>
        <input type="hidden" name="methodToCall" value="delete"/>
    </form>
</c:if>
<template:option node="${currentNode}" template="hidden.plusone_minorone_form" nodetype="jmix:rating"/>
<span class="forum-corners-top"><span></span></span>

<div class="forum-postbody">
    <ul class="forum-profile-icons">
        <c:if test="${renderContext.user.name != 'guest'}">
            <li class="forum-report-icon"><a title="<fmt:message key='report.post'/>" href="#"><span><fmt:message key='report.post'/></span></a></li>
            <li class="forum-quote-icon">
                <a title="Reply with quote" href="#threadPost"
                   onclick="jahiaForumQuote('jahia-forum-thread-${currentNode.parent.UUID}', '${fn:escapeXml(functions:escapeJavaScript(content.string))}');"><span>Reply with quote</span></a>
            </li>
            <li><template:option node="${currentNode}" template="hidden.plusone_minorone" nodetype="jmix:rating"/></li>
        </c:if>
        <c:if test="${currentNode.propertiesAsString['jcr:createdBy'] == renderContext.user.name}">
            <li class="delete-post-icon"><a title="<fmt:message key='delete.post'/>" href="#"
                                            onclick="document.getElementById('jahia-forum-post-delete-${currentNode.UUID}').submit();"><span><fmt:message key="delete.post"/></span></a>
            </li>
            <li class="edit-post-icon"><a title="<fmt:message key="edit.post"/>" href="#"><span><fmt:message key="edit.post"/></span></a></li>
        </c:if>

    </ul>

    <h3 class="forum-h3-first"><a href="#">${title.string}</a></h3>

    <p class="forum-author"><fmt:message key="by"/><strong> <a
            href="${url.base}/content/users/${createdBy.string}">${createdBy.string}</a></strong>&nbsp;&raquo;&nbsp;<fmt:formatDate
            value="${created.time}" type="both" dateStyle="full"/></p>

    <div class="content">${content.string}</div>
</div>
<jcr:sql var="numberOfPostsQuery"
         sql="select [jcr:uuid] from [jnt:post] as p  where p.[jcr:createdBy] = '${createdBy.string}'"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
<dl class="forum-postprofile">
    <dt>
        <jcr:node var="userNode" path="/content/users/${createdBy.string}"/>
        <template:module node="${userNode}" template="mini"/>
    </dt>
    <br/>
    <dd><strong><fmt:message key="number.of.posts"/></strong> ${numberOfPosts}</dd>
    <dd><strong><fmt:message key="registration.date"/></strong> <jcr:nodeProperty node="${userNode}" name="jcr:lastModified"
                                                   var="userCreated"/><fmt:formatDate value="${userCreated.time}"
                                                                                      type="date" dateStyle="medium"/>
    </dd>
</dl>
<div class="back2top"><a title="Top" class="top" href="#wrap">Top</a></div>
<div class="clear"></div>
<span class="forum-corners-bottom"><span></span></span>
