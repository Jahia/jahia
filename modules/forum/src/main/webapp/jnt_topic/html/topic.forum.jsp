<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<jcr:sql var="numberOfPostsQuery"
         sql="select * from [jnt:post] as post  where isdescendantnode(post, ['${currentNode.path}']) order by post.[jcr:lastModified] desc"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.nodes.size}"/>
<c:forEach items="${numberOfPostsQuery.nodes}" var="node" varStatus="status" end="2">
    <c:if test="${status.first}">
        <c:set value="${node}" var="lastModifiedNode"/>
        <jcr:nodeProperty node="${node}" name="jcr:lastModified" var="lastModified"/>
        <jcr:nodeProperty node="${lastModifiedNode}" name="jcr:createdBy" var="createdBy"/>
    </c:if>
</c:forEach>
<c:if test="${jcr:hasPermission(currentNode, 'deleteTopic')}">
    <template:tokenizedForm>
        <form action="<c:url value='${url.base}${currentNode.path}'/>" method="post"
              id="jahia-forum-topic-delete-${currentNode.UUID}">
            <input type="hidden" name="redirectTo"
                   value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>"/>
                <%-- Define the output format for the newly created node by default html or by redirectTo--%>
            <input type="hidden" name="newNodeOutputFormat" value="html"/>
            <input type="hidden" name="methodToCall" value="delete"/>
        </form>
    </template:tokenizedForm>
</c:if>

<dl class="icon icontopic">
    <dt title="posts">
        <c:if test="${jcr:hasPermission(currentNode, 'deleteTopic')}"><ul class="forum-profile-icons">
        
            <li class="delete-post-icon"><a title="Delete this topic" href="#"
                                            onclick="document.getElementById('jahia-forum-topic-delete-${currentNode.UUID}').submit();"><span>Delete this topic</span></a>
            </li>
        

    </ul></c:if>
    <a class="forum-title"
                         href="<c:url value='${url.base}${currentNode.path}.html'/>">${currentNode.properties.topicSubject.string}</a>
        <br/>
    <p>
        <fmt:message key="mix_created.jcr_createdBy"/> ${currentNode.properties["jcr:createdBy"].string}  <fmt:formatDate value="${currentNode.properties['jcr:created'].time}" dateStyle="full" type="both"/>
    </p>

    </dt>
        <%--<dd class="topics">30</dd>--%>
    <dd class="posts">${numberOfPosts}</dd>
    <dd class="lastpost">
        <c:if test="${numberOfPosts > 0}">
            <span>
                        <dfn><fmt:message key="last.post"/></dfn> <fmt:message key="by"/> <a
                    href="<c:url value='${url.base}${lastModifiedNode.parent.path}.html'/>"><img height="9"
                                                                                width="11"
                                                                                title="View the latest post"
                                                                                alt="View the latest post"
                                                                                src="<c:url value='${url.currentModule}/css/img/icon_topic_latest.gif'/>"/>${createdBy.string}
            </a><br/><fmt:formatDate value="${lastModified.time}" dateStyle="full"
                                     type="both"/></span>
        </c:if>
    </dd>
</dl>
