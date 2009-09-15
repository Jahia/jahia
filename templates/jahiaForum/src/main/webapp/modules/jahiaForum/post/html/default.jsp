<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<%-- Get all contents --%>
<jcr:nodeProperty node="${currentNode}" name="title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="content" var="content"/>
<li>
    <div class="commentBodyWrapper"><!--start commentBodyWrapper-->
        <div class="commentBody">
            <p class="commentDate">${currentNode.propertiesAsString['jcr:created']}</p>

            <div class="commentBubble-container"><!--start commentBubble-->
                <div class="commentBubble-arrow"></div>
                <div class="commentBubble-topright"></div>

                <div class="commentBubble-topleft"></div>

                <div class="commentBubble-text">

                    <h4>${fn:escapeXml(title.string)}</h4>

                    <p id="jahia-forum-post-${currentNode.UUID}">${content.string}</p>
                </div>

                <div class="commentBubble-bottomright"></div>
                <div class="commentBubble-bottomleft"></div>

            </div>
            <!--stop commentBubble-->


            <p class="comment_button">
            	<button class="button" onclick="return jahiaForumQuote('jahia-forum-thread-${currentNode.parent.UUID}', '${fn:escapeXml(functions:escapeJavaScript(content.string))}');">Quote it</button>
            </p>

            <div class="clear"></div>
        </div>
    </div>
    <!--start commentBodyWrapper-->
    <div class="commentsAuthor">
        <img src="perso1.jpg" alt="perso1"/>

        <p class="AuthorId">${currentNode.propertiesAsString['jcr:createdBy']}</p>

        <p class="commentNumber">741 Messages</p>
    </div>

    <div class="commentActions">
        <a href="#" class="commentDelete">Delete</a>
        <a href="#" class="commentEdit">Edit</a>
        <a href="#" class="commentAlert">Alert</a>
    </div>

    <div class="clear"></div>
</li>