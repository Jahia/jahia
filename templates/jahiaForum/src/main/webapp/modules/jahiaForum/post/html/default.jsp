<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<%-- Get all contents --%>
<jcr:nodeProperty node="${currentNode}" name="title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="content" var="content"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/>
<c:if test="${currentNode.propertiesAsString['jcr:createdBy'] == renderContext.user.name}">
    <form action="${url.base}${currentNode.path}" method="post"
          id="jahia-forum-post-delete-${currentNode.UUID}">
        <input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}"/>
            <%-- Define the output format for the newly created node by default html or by stayOnNode--%>
        <input type="hidden" name="newNodeOutputFormat" value="html">
        <input type="hidden" name="methodToCall" value="delete">
    </form>
</c:if>
<span class="forum-corners-top"><span></span></span>

<div class="forum-postbody">
    <ul class="forum-profile-icons">
        <c:if test="${renderContext.user.name != 'guest'}">
            <li class="forum-report-icon"><a title="Report this post" href="#"><span>Report this post</span></a></li>
            <li class="forum-quote-icon">
                <a title="Reply with quote" href="#threadPost"
                   onclick="jahiaForumQuote('jahia-forum-thread-${currentNode.parent.UUID}', '${fn:escapeXml(functions:escapeJavaScript(content.string))}');"><span>Reply with quote</span></a>
            </li>
        </c:if>
        <c:if test="${currentNode.propertiesAsString['jcr:createdBy'] == renderContext.user.name}">
            <li class="delete-post-icon"><a title="Delete with post" href="#"
                                            onclick="document.getElementById('jahia-forum-post-delete-${currentNode.UUID}').submit();"><span>Delete this post</span></a>
            </li>
            <li class="edit-post-icon"><a title="edit with post" href="#"><span>Edit this post</span></a></li>
        </c:if>

    </ul>

    <h3 class="forum-h3-first"><a href="#">${title.string}</a></h3>

    <p class="forum-author">by <strong><a
            href="${url.base}/content/users/${createdBy.string}">${createdBy.string}</a></strong> » <fmt:formatDate
            value="${lastModified.time}" type="both" dateStyle="full"/></p>

    <div class="content">${content.string}</div>
</div>
<jcr:sql var="numberOfPostsQuery"
         sql="select jcr:uuid from jahiaForum:post  where jcr:createdBy = '${createdBy.string}'"/>
<c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
<dl class="forum-postprofile">
    <dt>
        <jcr:node var="userNode" path="/content/users/${createdBy.string}"/>
        <template:module node="${userNode}" template="mini"/>
    </dt>
    <br/>
    <dd><strong>Posts:</strong> ${numberOfPosts}</dd>
    <dd><strong>Joined:</strong> <jcr:nodeProperty node="${userNode}" name="jcr:lastModified"
                                                   var="userCreated"/><fmt:formatDate value="${userCreated.time}"
                                                                                      type="both" dateStyle="full"/>
    </dd>
</dl>
<div class="back2top"><a title="Top" class="top" href="#wrap">Top</a></div>
<div class="clear"></div>
<span class="forum-corners-bottom"><span></span></span>


<%--

<div class="commentBodyWrapper"><!--start commentBodyWrapper-->
    <div class="commentBody">
        <p class="commentDate">${currentNode.propertiesAsString['jcr:created']}</p>

        <div class="commentBubble-container"><!--start commentBubble-->
            <div class="commentBubble-arrow"></div>
            <div class="commentBubble-topright"></div>

            <div class="commentBubble-topleft"></div>

            <div class="commentBubble-text">
                <jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="created"/>
                <h4>${fn:escapeXml(title.string)} posted at <fmt:formatDate value="${created.time}" dateStyle="full"
                                                                            type="both"/></h4>


                <p id="jahia-forum-post-${currentNode.UUID}">${content.string}</p>
            </div>

            <div class="commentBubble-bottomright"></div>
            <div class="commentBubble-bottomleft"></div>

        </div>
        <!--stop commentBubble-->

        <c:if test="${renderContext.user.name != 'guest'}">
            <p class="comment_button">
                <button class="button"
                        onclick="return jahiaForumQuote('jahia-forum-thread-${currentNode.parent.UUID}', '${fn:escapeXml(functions:escapeJavaScript(content.string))}');">
                    Quote it
                </button>
            </p>
        </c:if>
        <div class="clear"></div>
    </div>
</div>
<!--start commentBodyWrapper-->
<div class="commentsAuthor">
    <jcr:node var="createdBy" path="/content/users/${currentNode.propertiesAsString['jcr:createdBy']}"/>
    <template:module node="${createdBy}" template="mini"/>
    <jcr:sql var="numberOfPostsQuery"
             sql="select jcr:uuid from jahiaForum:post  where jcr:createdBy = '${createdBy.name}'"/>
    <c:set var="numberOfPosts" value="${numberOfPostsQuery.rows.size}"/>
    <p class="commentNumber">${numberOfPosts} Messages</p>
</div>

<div class="commentActions">
    <c:if test="${currentNode.propertiesAsString['jcr:createdBy'] == renderContext.user.name}">
        <form action="${url.base}${currentNode.path}" method="delete" id="jahia-forum-post-delete-${currentNode.UUID}">
            <input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}"/>
                --%>
<%-- Define the output format for the newly created node by default html or by stayOnNode--%>
<%--
            <input type="hidden" name="newNodeOutputFormat" value="html">
        </form>
        <a href="#" onclick="document.getElementById('jahia-forum-post-delete-${currentNode.UUID}').submit();"
           class="commentDelete">Delete</a>
        <a href="#" class="commentEdit">Edit</a>
    </c:if>
    <c:if test="${renderContext.user.name != 'guest'}">
        <a href="#" class="commentAlert">Alert</a>
    </c:if>
</div>

<div class="clear"></div>--%>
