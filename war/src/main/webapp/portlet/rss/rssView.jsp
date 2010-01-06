<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="p" uri="http://java.sun.com/portlet" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="org.jahia.utils.rss.RSSUtil" %>
<c:if test="${syndFeed == null}">
    <jcr:nodeProperty node="${currentNode}" name="url" var="feedUrl"/>
    Invalid feed url: ${feedUrl.string}
</c:if>
<c:if test="${syndFeed != null}">
    <div class="syndFeed">
        <div class="syndFeedTitle">
            <a href="${syndFeed.link}"> "${syndFeed.title}</a>
            <c:if test="${syndFeed.image != null}">
                  <img src="${syndFeed.image.url}" title="${syndFeed.image.title}" alt="${syndFeed.image.description}"/>
            </c:if>
        </div>
        <div class="syndFeedEntries">
            <c:forEach items="${syndFeed.entries}" var="syndEntry" begin="0" end="${entriesCount}">
                <div class="syndEntryTitle">
                    <a href="${syndEntry.link}"> ${syndEntry.title} [${syndEntry.updatedDate}]</a>
                </div>
                <div class="syndEntryDescription">
                        ${syndEntry.description.value}
                </div>
                <div class="syndEntryContents">
                    <c:forEach items="${syndEntry.contents}" var="syndContent">
                        <div class="syndEntryContent">
                                ${syndContent.value}
                        </div>
                    </c:forEach>
                </div>
                <div class="syndEntryAuthor">
                        ${syndEntry.author}
                </div>
            </c:forEach>
        </div>
    </div>
</c:if>