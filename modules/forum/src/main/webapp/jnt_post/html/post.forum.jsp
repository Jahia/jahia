<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="forum.css"/>
<template:addResources type="javascript" resources="jquery.js,jquery.cuteTime.js"/>
<%-- Get all contents --%>

<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="content" var="content"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
<c:if test="${jcr:hasPermission(currentNode, 'deletePost')}">
  <template:tokenizedForm>
    <form action="${url.base}${currentNode.path}" method="post"
          id="jahia-forum-post-delete-${currentNode.UUID}">
      <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
      <%-- Define the output format for the newly created node by default html or by redirectTo--%>
      <input type="hidden" name="newNodeOutputFormat" value="html"/>
      <input type="hidden" name="methodToCall" value="delete"/>
    </form>
  </template:tokenizedForm>
</c:if>
<template:option node="${currentNode}" template="hidden.plusone_minorone_form" nodetype="jmix:rating"/>
<div class="forum-postbody">
  <ul class="forum-profile-icons">
    <%--<c:if test="${jcr:hasPermission(currentNode, 'reportPost')}">--%>
    <%--<li class="forum-report-icon"><a title="<fmt:message key='report.post'/>" href="#"><span><fmt:message key='report.post'/></span></a></li>--%>
    <%--</c:if>--%>
    <c:if test="${jcr:hasPermission(currentNode, 'createPost')}">
      <li class="forum-quote-icon"> <a title="<fmt:message key='reply.quote'/>" href="${url.base}${renderContext.mainResource.node.path}.forum-topic-newPost.html?reply=${currentNode.UUID}"> <span>
        <fmt:message key='reply.quote'/>
        </span> </a> </li>
    </c:if>
    <li>
      <template:option node="${currentNode}" template="hidden.plusone_minorone" nodetype="jmix:rating"/>
    </li>
    <c:if test="${jcr:hasPermission(currentNode, 'deletePost')}">
      <li class="delete-post-icon"><a title="<fmt:message key='delete.post'/>" href="#delete"
                                            onclick="document.getElementById('jahia-forum-post-delete-${currentNode.UUID}').submit(); return false;"><span>
        <fmt:message key="delete.post"/>
        </span></a> </li>
    </c:if>
    <c:if test="${jcr:hasPermission(currentNode, 'editPost')}">
      <li class="edit-post-icon"><a title="<fmt:message key='edit.post'/>" href="#edit" onclick="$('#edit${currentNode.UUID}').click(); return false;"><span><fmt:message key="edit.post"/></span></a></li>
    </c:if>
  </ul>
  <h4 class="forum-h4-first">${title.string}</h4>
  <p class="forum-author">
    <c:if test="${renderContext.user.name ne 'guest'}">
      <fmt:message key="by"/>
      <strong>&nbsp;<a
            href="${url.base}${renderContext.site.path}/users/${createdBy.string}.html">${createdBy.string}</a></strong>&nbsp;&raquo;&nbsp;<span class="timestamp">
      <fmt:formatDate
            value="${created.time}" pattern="yyyy/MM/dd HH:mm"/>
      </span> </c:if>
    <c:if test="${renderContext.user.name eq 'guest'}">
      <fmt:message key="by"/>
      <strong>&nbsp;${createdBy.string}</strong>&nbsp;&raquo;&nbsp;<span class="timestamp">
      <fmt:formatDate
            value="${created.time}" pattern="yyyy/MM/dd HH:mm"/>
      </span> </c:if>
  </p>
  <c:if test="${jcr:hasPermission(currentNode, 'editPost')}">
    <div class="content editablePost" jcr:id="content"
              id="edit${currentNode.identifier}"
              jcr:url="${url.base}${currentNode.path}">${fn:escapeXml(content.string)}</div>
  </c:if>
  <c:if test="${not jcr:hasPermission(currentNode, 'editPost')}">
    <div class="content">${fn:escapeXml(content.string)}</div>
  </c:if>
</div>
<jcr:sql var="numberOfPostsQuery"
         sql="select [jcr:uuid] from [jnt:post] as p  where p.[jcr:createdBy] = '${createdBy.string}'"/>
<c:set var="numberOfPosts" value="${functions:length(numberOfPostsQuery.rows)}"/>
<dl class="forum-postprofile">
  <dt>
    <jcr:node var="userNode" path="/users/${createdBy.string}"/>
    <template:module node="${userNode}" template="mini"/>
  </dt>
  <dd><strong>
    <fmt:message key="number.of.posts"/>
    </strong>&nbsp;${numberOfPosts}</dd>
  <dd><strong>
    <fmt:message key="registration.date"/>
    </strong>
    <jcr:nodeProperty node="${userNode}" name="jcr:lastModified"
                                                   var="userCreated"/>
    <fmt:formatDate value="${userCreated.time}"
                                                                                      type="date" dateStyle="medium"/>
  </dd>
</dl>
<div class="back2top"><a title="Top" class="top" href="#bodywrapper">Top</a></div>
<div class="clear"></div>
