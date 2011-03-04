<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>

<template:addResources type="css" resources="forum.css"/>
<c:set var="linked" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty linked}">
  <jcr:sql var="numberOfPostsQuery"
             sql="select [jcr:uuid] from [jnt:post] as p  where isdescendantnode(p,['${linked.path}'])"/>
  <c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
  <jcr:sql var="numberOfThreadsQuery"
             sql="select [jcr:uuid] from [jnt:page] as t  where isdescendantnode(t,['${linked.path}'])"/>
  <c:set var="numberOfThreads" value="${numberOfThreadsQuery.rows.size}"/>
  <template:addResources type="css" resources="forum.css"/>
  <template:addCacheDependency node="${linked}"/>
  <div class="topics">
    <jcr:nodeProperty node="${linked}" name="jcr:title" var="topicSubject"/>
    <c:forEach items="${jcr:getChildrenOfType(linked,'jnt:page')}" var="room">
      <template:addCacheDependency node="${room}"/>
      <div class="forum-box forum-box-style1 topics">
        <ul class="forum-list">
          <li class="forum-list-header">
            <dl class="icon">
              <dt>
                <ul class="forum-profile-icons">
                  <li class="delete-post-icon">
                  	<fmt:message key="confirm.delete.room" var="confirmMsg"/>
                  	<a title="<fmt:message key='delete.room'/>" href="#"
						onclick="if (window.confirm('${functions:escapeJavaScript(confirmMsg)}')) {document.getElementById('jahia-forum-room-delete-${room.identifier}').submit();} return false;"><span><fmt:message key='delete.room'/></span></a></li>
                </ul>
                <a href="${url.base}${room.path}.html">${room.properties['jcr:title'].string}</a>
                <c:if test="${jcr:hasPermission(room, 'deleteRoom')}">
                  <template:tokenizedForm>
                    <form action="${url.base}${room.path}" method="post"
                                              id="jahia-forum-room-delete-${room.identifier}">
                      <input type="hidden" name="redirectTo"
                                                   value="${url.base}${renderContext.mainResource.node.path}"/>
                      <%-- Define the output format for the newly created node by default html or by redirectTo--%>
                      <input type="hidden" name="newNodeOutputFormat" value="html"/>
                      <input type="hidden" name="methodToCall" value="delete"/>
                    </form>
                  </template:tokenizedForm>
                </c:if>
              </dt>
              <dd class="topics">
                <fmt:message key="topics"/>
              </dd>
              <dd class="topics">
                <fmt:message key="posts"/>
              </dd>
              <%--<dd class="posts">View</dd>--%>
              <dd class="lastpost"><span><fmt:message key="lastPosts"/></span></dd>
            </dl>
          </li>
        </ul>
        <ul class="forum-list forums">
          <c:forEach items="${room.nodes}" var="section">
            <c:if test="${jcr:isNodeType(section, 'jnt:page')}">
                <template:addCacheDependency node="${section}"/>
              <li class="row">
                <%--<template:module node="${section}" template="section"/>--%>
                <jcr:sql var="numberOfPostsQuery"
                                         sql="select * from [jnt:post] as post  where isdescendantnode(post, ['${section.path}']) order by post.[jcr:lastModified] desc"/>
                <c:set var="numberOfPosts" value="${numberOfPostsQuery.nodes.size}"/>
                <jcr:sql var="numberOfTopicsQuery"
                                             sql="select * from [jnt:topic] as topic where isdescendantnode(topic, ['${section.path}']) order by topic.[jcr:lastModified] desc"/>
                <c:set var="numberOfTopics" value="${numberOfTopicsQuery.nodes.size}"/>
                <c:forEach items="${numberOfPostsQuery.nodes}" var="node" varStatus="status" end="2">
                  <c:if test="${status.first}">
                    <c:set value="${node}" var="lastModifiedNode"/>
                    <jcr:nodeProperty node="${node}" name="jcr:lastModified" var="lastModified"/>
                    <jcr:nodeProperty node="${lastModifiedNode}" name="jcr:createdBy"
                                                          var="createdBy"/>
                  </c:if>
                </c:forEach>
                <c:if test="${jcr:hasPermission(section, 'deleteSection')}">
                  <template:tokenizedForm>
                    <form action="${url.base}${section.path}" method="post"
                                              id="jahia-forum-section-delete-${section.identifier}">
                      <input type="hidden" name="redirectTo"
                                                   value="${url.base}${renderContext.mainResource.node.path}"/>
                      <%-- Define the output format for the newly created node by default html or by redirectTo--%>
                      <input type="hidden" name="newNodeOutputFormat" value="html"/>
                      <input type="hidden" name="methodToCall" value="delete"/>
                    </form>
                  </template:tokenizedForm>
                </c:if>
                <dl class="icon iconsection">
                  <dt title="posts">
                    <c:if test="${jcr:hasPermission(section, 'deleteSection')}">
                      <ul class="forum-profile-icons">
                      	<fmt:message key='confirm.delete.section' var="confirmMsg"/>
                        <li class="delete-post-icon"><a title="<fmt:message key='delete.section'/>" href="#"
                                                                            onclick="if (window.confirm('${functions:escapeJavaScript(confirmMsg)}')) { document.getElementById('jahia-forum-section-delete-${section.identifier}').submit(); } return false;"><span><fmt:message key='delete.section'/></span></a></li>
                      </ul>
                    </c:if>
                    <a class="forum-title"
                                                         href="${url.base}${section.path}.html">
                    <jcr:nodeProperty
                                            node="${section}" name="jcr:title"/>
                    </a> <br/>
                    <p>${fn:escapeXml(section.properties['jcr:description'].string)}</p>
                  </dt>
                  <%--<dd class="topics">30</dd>--%>
                  <dd class="topics">${numberOfTopics}</dd>
                  <dd class="posts">${numberOfPosts}</dd>
                  <dd class="lastpost">
                    <c:if test="${numberOfPosts > 0}"> <span><dfn><fmt:message key="last.post"/></dfn>
                      <fmt:message key="by"/>
                      <a
                                                    href="${url.base}${lastModifiedNode.parent.path}.html"><img height="9"
                                                                                                                width="11"
                                                                                                                title="View the latest post"
                                                                                                                alt="View the latest post"
                                                                                                                src="${url.currentModule}/css/img/icon_topic_latest.gif"/>${fn:escapeXml(createdBy.string)} </a><br/>
                      <fmt:formatDate value="${lastModified.time}" dateStyle="full" type="both"/>
                      </span> </c:if>
                  </dd>
                </dl>
              </li>
              <c:set var="found" value="true"/>
            </c:if>
          </c:forEach>
          <c:if test="${not found}">
            <li class="row">
              <fmt:message key="noSectionFound"/>
            </li>
          </c:if>
        </ul>
        <div class="clear"></div>
      </div>
    </c:forEach>
    <span><fmt:message key="total.threads"/>:&nbsp;${numberOfThreads}</span>
    <span><fmt:message key="total.posts"/>:&nbsp;${numberOfPosts}</span>
    </div>
</c:if>
