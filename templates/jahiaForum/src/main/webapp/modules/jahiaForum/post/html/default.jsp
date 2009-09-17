<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<%-- Get all contents --%>
<jcr:nodeProperty node="${currentNode}" name="title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="content" var="content"/>
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


        <p class="comment_button">
            <button class="button"
                    onclick="return jahiaForumQuote('jahia-forum-thread-${currentNode.parent.UUID}', '${fn:escapeXml(functions:escapeJavaScript(content.string))}');">
                Quote it
            </button>
        </p>

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
                <%-- Define the output format for the newly created node by default html or by stayOnNode--%>
            <input type="hidden" name="newNodeOutputFormat" value="html">
        </form>
    </c:if>
    <a href="#" onclick="document.getElementById('jahia-forum-post-delete-${currentNode.UUID}').submit();"
       class="commentDelete">Delete</a>
    <a href="#" class="commentEdit">Edit</a>
    <a href="#" class="commentAlert">Alert</a>
</div>

<div class="clear"></div>